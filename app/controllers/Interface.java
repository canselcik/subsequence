package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import internal.CaptchaGenerator;
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
        if(addresses == null) {
            ObjectNode res = mapper.createObjectNode();
            res.put("error", "Failed to get addresses for the user. Bitcoind instance might be down.");
            return internalServerError(res);
        }
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
        if(info == null) {
            ObjectNode res = mapper.createObjectNode();
            res.put("error", "An error occurred while querying the cluster.");
            return internalServerError(res);
        }
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
        ObjectNode root = mapper.createObjectNode();
        Bitcoind.Pair<String, Long> sweepResult = Bitcoind.sweepFunds(id, target);
        if(sweepResult == null) {
            root.put("error", "An error occurred while sweeping funds");
            return internalServerError(root);
        }
        if(sweepResult.u == -1l) {
            root.put("error", sweepResult.t);
            return internalServerError(root);
        }

        root.put("tx", sweepResult.t);
        root.put("satoshi_amount", sweepResult.u);
        return ok(root);
    }

    public static Result withdrawAmount(String name, long amount, String address) {
        ObjectNode root = mapper.createObjectNode();
        // Checking if the withdrawal amount is zero
        if(amount == 0){
            root.put("error", "You cannot withdraw zero SAT");
            return internalServerError(root);
        }

        // Fetch user account
        ObjectNode user = UserDB.getUser(name);
        if(user == null || user.has("error")){
            root.put("error", "Unable to retrieve user information");
            return internalServerError(root);
        }

        // Check if user has enough funds
        Long confirmedBalance = user.get("confirmed_satoshi_balance").asLong();
        Long userId = user.get("account_id").asLong();
        Integer clusterId = user.get("node_id").asInt();
        if( (amount + Bitcoind.TX_FEE_SAT) > confirmedBalance )
            return internalServerError("Insufficient confirmed balance");

        // Check if we have enough funds in the cluster that the user is assigned to.
        BitcoindInterface bi = BitcoindNodes.getNodeInterface(clusterId);
        if(bi == null){
            root.put("error", "Failed to contact the bitcoind instance to which the client is assigned.");
            return internalServerError(root);
        }

        BigDecimal balance = bi.getbalance();
        if(balance == null) {
            root.put("error", "Failed to contact the client's assigned bitcoind instance, got null balance for cluster");
            return internalServerError(root);
        }

        // Getting the balance for the cluster
        long amountWithFee = amount + Bitcoind.TX_FEE_SAT;
        BigDecimal amountToWithdrawInBTC = BigDecimal.valueOf(amount).divide( BigDecimal.valueOf(100000000) );
        BigDecimal amountToWithdrawInBTCwithFee = BigDecimal.valueOf(amountWithFee).divide( BigDecimal.valueOf(100000000) );
        // If the balance is less than the amount to be withdrawn + fee
        if(balance.compareTo(amountToWithdrawInBTCwithFee) == -1){
            root.put("error", "The assigned cluster doesn't have the balance to allow this withdrawal. No withdrawal has been made.");
            return internalServerError(root);
        }

        // Update user balance on record
        boolean updateConfirmedBalanceSuccess = UserDB.updateUserBalance(userId, true, confirmedBalance - amountWithFee);
        if(!updateConfirmedBalanceSuccess){
            root.put("error", "Withdrawal failed due to a database error. Funds haven't been touched.");
            return internalServerError(root);
        }

        // Now we actually create and submit the tx
        String withdrawalTxId = bi.sendtoaddress(address, amountToWithdrawInBTC);
        if(withdrawalTxId == null) {
            root.put("error", "Failed to create the withdrawal transaction but subtracted user balance already");
            return internalServerError(root);
        }

        boolean insertTxSuccess = TransactionDB.insertTxIntoDB(withdrawalTxId, userId, false, false, amountWithFee);

        if(!insertTxSuccess) {
            root.put("error", "Updated user balance and created withdrawal request but failed to add the tx to the DB <txid: " +
                    withdrawalTxId + ">");
            return internalServerError(root);
        }

        root.put("txid", withdrawalTxId);
        return ok(root);
    }

    public static Result incrementUserBalanceWithDescription(String name, long amount, String desc) {
        ObjectNode userInfo = UserDB.getUser(name);
        ObjectNode ret = mapper.createObjectNode();
        if(userInfo == null || userInfo.has("error")) {
            ret.put("error", "Failed to retrieve user");
            return internalServerError(ret);
        }

        Long accountId = userInfo.get("account_id").asLong();
        Long confirmedBalance = userInfo.get("confirmed_satoshi_balance").asLong();

        boolean txInsertResult = TransactionDB.insertTxIntoDB("internalTransaction - " + desc, accountId, true, true, amount);
        if(!txInsertResult) {
            ret.put("error", "Failed to create the internal transaction");
            return internalServerError(ret);
        }

        boolean updateUserBalanceResult = UserDB.updateUserBalance(accountId, true, amount + confirmedBalance);
        if(!updateUserBalanceResult) {
            ret.put("error", "Failed to update user balance");
            return internalServerError(ret);
        }

        ret.put("confirmed_satoshi_balance", confirmedBalance + amount);
        return ok(ret);
    }

    public static Result decrementUserBalanceWithDescription(String name, long amount, String desc) {
        ObjectNode userInfo = UserDB.getUser(name);
        ObjectNode ret = mapper.createObjectNode();
        if(userInfo == null || userInfo.has("error")) {
            ret.put("error", "Failed to retrieve user");
            return internalServerError(ret);
        }

        Long accountId = userInfo.get("account_id").asLong();
        Long confirmedBalance = userInfo.get("confirmed_satoshi_balance").asLong();

        if(confirmedBalance < amount){
            ret.put("error", "The user doesn't have high enough of a confirmed balance to complete this internal transaction.");
            return internalServerError(ret);
        }

        boolean txInsertResult = TransactionDB.insertTxIntoDB("internalTransaction - " + desc, accountId, false, true, amount);
        if(!txInsertResult) {
            ret.put("error", "Failed to create the internal transaction\"");
            return internalServerError(ret);
        }

        boolean updateUserBalanceResult = UserDB.updateUserBalance(accountId, true, confirmedBalance - amount);
        if(!updateUserBalanceResult) {
            ret.put("error", "Failed to update the user balance");
            return internalServerError(ret);
        }

        ret.put("confirmed_satoshi_balance", confirmedBalance - amount);
        return ok(ret);
    }


    public static Result generateCaptcha() {
        CaptchaGenerator.CaptchaPackage p = CaptchaGenerator.next();
        return ok(p.captcha).as("image/jpg");
    }
}
