package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import internal.database.NodeDB;
import internal.database.TransactionDB;
import internal.database.UserDB;
import internal.rpc.BitcoindInterface;
import internal.rpc.pojo.Info;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import internal.Bitcoind;
import internal.BitcoindNodes;
import play.db.DB;
import play.libs.Json;
import play.mvc.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class Interface extends Controller {
    public static Result getUser(String name) {
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM account_holders WHERE account_name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if(rs == null || !rs.next()) {
                c.close();
                return internalServerError("User not found");
            }
            c.close();
            ObjectNode root = mapper.createObjectNode();
            root.put("user_id", rs.getLong("account_id"));
            root.put("account_name", rs.getString("account_name"));
            root.put("cluster_id", rs.getLong("cluster_id"));
            root.put("confirmed_satoshi_balance", rs.getLong("confirmed_satoshi_balance"));
            root.put("unconfirmed_satoshi_balance", rs.getLong("unconfirmed_satoshi_balance"));
            return ok(root);
        } catch(Exception e){
            e.printStackTrace();
            return internalServerError("An error occurred while fetching user information");
        }
    }

    public static Result getTransactionsForUser(String name) {
        long userId = UserDB.getIdFromAccountName(name);
        if(userId < 0)
            return internalServerError("User cannot be found");
        ArrayNode txs = TransactionDB.getTransactions(userId);
        if(txs == null)
            return internalServerError("Failed to fetch transactions");
        return ok(Json.toJson(txs));
    }

    public static Result getAddressesForUser(String name) {
        List<String> addresses = Bitcoind.getAddresses(name);
        if(addresses == null)
            return internalServerError();
        return ok(Json.toJson(addresses));
    }

    public static Result getNewAddressForUser(String name) {
        String address = Bitcoind.getNewAddress(name);
        if(address == null)
            return internalServerError();
        return ok(Json.toJson(address));
    }

    public static Result getNodeStatus(Integer id) {
        Info info = Bitcoind.getInfo(id);
        if(info == null)
            return internalServerError("An error occurred while querying the cluster.");
        return ok(Json.toJson(info));
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    public static Result getNodes(){
        List<NodeDB.BitcoindNodeInfo> clusters = NodeDB.getBitcoindNodes();
        if(clusters == null)
            return internalServerError("An error occurred while fetching available clusters");

        ObjectNode root = mapper.createObjectNode();
        for(NodeDB.BitcoindNodeInfo info : clusters)
            root.put(String.valueOf(info.id), info.connString);
        return ok(root);
    }

    public static Result sweepFunds(Integer id, String target) {
        Bitcoind.Pair<String, Long> sweepResult = Bitcoind.sweepFunds(id, target);
        if(sweepResult == null)
            return internalServerError("An error occurred while sweeping funds");
        if(sweepResult.u == -1l)
            return internalServerError(sweepResult.t);

        ObjectNode root = mapper.createObjectNode();
        root.put("tx", sweepResult.t);
        root.put("satoshi_amount", sweepResult.u);
        return ok(root);
    }

    public static Result withdrawAmount(String name, long amount, String address) {
        Connection c = DB.getConnection();
        try {
            // Fetch user account
            PreparedStatement ps = c.prepareStatement("SELECT * FROM account_holders WHERE account_name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if(rs == null || !rs.next()) {
                c.close();
                return internalServerError("User not found");
             }

            // Check if user has enough funds
            Long confirmedBalance = rs.getLong("confirmed_satoshi_balance");
            Long userId = rs.getLong("account_id");
            Integer clusterId = rs.getInt("cluster_id");
            if( (amount + Bitcoind.TX_FEE_SAT) > confirmedBalance ){
                c.close();
                return internalServerError("Insufficient confirmed balance");
            }

            // Check if we have enough funds in the cluster that the user is assigned to.
            BitcoindInterface bi = BitcoindNodes.getNodeInterface(clusterId);
            if(bi == null) {
                c.close();
                return internalServerError("Failed to contact the bitcoind instance to which the client is assigned.");
            }
            BigDecimal balance = bi.getbalance();
            if(balance == null){
                c.close();
                return internalServerError("Failed to contact the client's assigned bitcoind instance, got null balance for cluster");
            }

            // Getting the balance for the cluster
            long amountWithFee = amount + Bitcoind.TX_FEE_SAT;
            BigDecimal amountToWithdrawInBTC = BigDecimal.valueOf(amount).divide( BigDecimal.valueOf(100000000) );
            BigDecimal amountToWithdrawInBTCwithFee = BigDecimal.valueOf(amountWithFee).divide( BigDecimal.valueOf(100000000) );
            // If the balance is less than the amount to be withdrawn + fee
            if(balance.compareTo(amountToWithdrawInBTCwithFee) == -1){
                c.close();
                return internalServerError("The assigned cluster doesn't have the balance to allow this withdrawal. No withdrawal has been made");
            }

            // Update user balance on record
            PreparedStatement psBalanceUpdate =
                    c.prepareStatement("UPDATE account_holders SET confirmed_satoshi_balance = confirmed_satoshi_balance - ? WHERE account_name = ?");
            psBalanceUpdate.setLong(1, amountWithFee);
            psBalanceUpdate.setString(2, name);
            int res = psBalanceUpdate.executeUpdate();
            if(res == 0) {
                c.close();
                return internalServerError("Withdrawal failed due to a database error. Funds haven't been touched.");
            }

            // Now we actually create and submit the tx
            String withdrawalTxId = bi.sendtoaddress(address, amountToWithdrawInBTC);
            if(withdrawalTxId == null){
                c.close();
                return internalServerError("Failed to create the withdrawal transaction but subtracted user balance already");
            }

            // And create the transaction records
            PreparedStatement txPs =
                    c.prepareStatement("INSERT INTO transactions(matched_user_id, inbound, tx_hash, confirmed, amount_satoshi) VALUES(?, ?, ?, ?, ?) ");
            txPs.setLong(1, userId);           // userid
            txPs.setBoolean(2, false);         // outbound tx
            txPs.setString(3, withdrawalTxId); // txhash for the created tx
            txPs.setBoolean(4, false);         // tx not confirmed yet but we do not care about confirmation for outbound tx
            txPs.setLong(5, amountWithFee);    // we note down the fee as an expense to the account as well
            int affectedRows = txPs.executeUpdate();
            c.close();

            if(affectedRows != 1)
                return internalServerError("Updated user balance and created withdrawal request but failed to add the tx to the DB <txid: " + withdrawalTxId + ">");

            // Now we are all set and we can return the txid to the user
            return ok(withdrawalTxId);
        } catch(Exception e){
            e.printStackTrace();
            return internalServerError("An error occurred while fetching user information");
        }
    }
}
