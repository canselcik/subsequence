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
import play.libs.Json;
import play.mvc.*;

import java.math.BigDecimal;
import java.util.List;

public class Interface extends Controller {
    public static Result getUser(String name){
        ObjectNode res = UserDB.getUser(name);
        if(res == null)
            return internalServerError("Failed to get user information. Please try again later.");
        if(res.has("error"))
            return internalServerError(res);
        return ok(res);
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

    public static Result getNodes(){
        List<NodeDB.BitcoindNodeInfo> clusters = NodeDB.getBitcoindNodes();
        if(clusters == null)
            return internalServerError("An error occurred while fetching available clusters");

        ObjectNode root = mapper.createObjectNode();
        for(NodeDB.BitcoindNodeInfo info : clusters)
            root.put(String.valueOf(info.id), info.connString);
        return ok(root);
    }

    private static final ObjectMapper mapper = new ObjectMapper();
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
        try {
            // Fetch user account
            ObjectNode user = UserDB.getUser(name);
            if(user == null || user.has("error"))
                return internalServerError("Unable to retrieve user information");

            // Check if user has enough funds
            Long confirmedBalance = user.get("confirmed_satoshi_balance").asLong();
            Long userId = user.get("account_id").asLong();
            Integer clusterId = user.get("node_id").asInt();
            if( (amount + Bitcoind.TX_FEE_SAT) > confirmedBalance )
                return internalServerError("Insufficient confirmed balance");

            // Check if we have enough funds in the cluster that the user is assigned to.
            BitcoindInterface bi = BitcoindNodes.getNodeInterface(clusterId);
            if(bi == null)
                return internalServerError("Failed to contact the bitcoind instance to which the client is assigned.");

            BigDecimal balance = bi.getbalance();
            if(balance == null)
                return internalServerError("Failed to contact the client's assigned bitcoind instance, got null balance for cluster");

            // Getting the balance for the cluster
            long amountWithFee = amount + Bitcoind.TX_FEE_SAT;
            BigDecimal amountToWithdrawInBTC = BigDecimal.valueOf(amount).divide( BigDecimal.valueOf(100000000) );
            BigDecimal amountToWithdrawInBTCwithFee = BigDecimal.valueOf(amountWithFee).divide( BigDecimal.valueOf(100000000) );
            // If the balance is less than the amount to be withdrawn + fee
            if(balance.compareTo(amountToWithdrawInBTCwithFee) == -1)
                return internalServerError("The assigned cluster doesn't have the balance to allow this withdrawal. No withdrawal has been made.");

            // Update user balance on record
            boolean updateConfirmedBalanceSuccess = UserDB.updateUserBalance(userId, true, confirmedBalance - amountWithFee);
            if(!updateConfirmedBalanceSuccess)
                return internalServerError("Withdrawal failed due to a database error. Funds haven't been touched.");

            // Now we actually create and submit the tx
            String withdrawalTxId = bi.sendtoaddress(address, amountToWithdrawInBTC);
            if(withdrawalTxId == null)
                return internalServerError("Failed to create the withdrawal transaction but subtracted user balance already");

            boolean insertTxSuccess = TransactionDB.insertTxIntoDB(withdrawalTxId, userId, false, false, amountWithFee);

            if(!insertTxSuccess)
                return internalServerError("Updated user balance and created withdrawal request but failed to add the tx to the DB <txid: " + withdrawalTxId + ">");

            return ok(withdrawalTxId);
        } catch(Exception e){
            e.printStackTrace();
            return internalServerError("An error occurred while fetching user information");
        }
    }
}
