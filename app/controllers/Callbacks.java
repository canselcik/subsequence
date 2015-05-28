package controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import internal.Bitcoind;
import internal.rpc.BitcoindInterface;
import internal.rpc.pojo.RawTransaction;
import internal.rpc.pojo.Transaction;
import internal.BitcoindNodes;
import internal.database.TransactionDB;
import internal.database.UserDB;
import play.libs.Json;
import play.mvc.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Callbacks extends Controller {
    private static class ProcessDepositResult{
        public final ObjectNode result;
        public final List<String> depositInputs;

        private ProcessDepositResult(ObjectNode result, List<String> depositInputs) {
            this.result = result;
            this.depositInputs = depositInputs;
        }
    }
    private static final ObjectMapper mapper = new ObjectMapper();
    private static ProcessDepositResult processDeposit(Transaction.Deposit d, String txHash, BitcoindInterface bi){
        RawTransaction rt = bi.getrawtransaction(txHash, 1);
        long confirmations = rt.getConfirmations();
        boolean confirmed = (confirmations >= Bitcoind.CONFIRM_AFTER);

        long relevantUserId = UserDB.getIdFromAccountName(d.account);
        long amountInSAT = d.amount.multiply(BigDecimal.valueOf(100000000)).longValueExact();

        ObjectNode result = mapper.createObjectNode();
        result.put("txId", txHash);
        if(relevantUserId < 0){
            result.put("error", "Related user account cannot be found");
            return new ProcessDepositResult(result, null);
        }

        TransactionDB.TxDatabasePresence presence = TransactionDB.txPresentInDB(txHash);

        // First time seeing the tx
        if(presence.getValue() == TransactionDB.TxDatabasePresence.NOT_PRESENT.getValue()){
            boolean txDbPushResult = TransactionDB.insertTxIntoDB(txHash, relevantUserId, true, confirmed, amountInSAT);
            if(!txDbPushResult){
                result.put("error", "Failed to commit the tx into the DB");
                return new ProcessDepositResult(result, null);
            }
            if(TransactionDB.txInputsUsedInDB(rt)) {
                result.put("error", "Inputs of this transaction has already been funded (NOT_PRESENT)");
                return new ProcessDepositResult(result, null);
            }
            long userBalance = UserDB.getUserBalance(relevantUserId, confirmed);
            if(userBalance < 0) {
                result.put("error", "Failed to retrieve unconfirmed user balance before updating it");
                return new ProcessDepositResult(result, null);
            }
            boolean updateBalanceResult = UserDB.updateUserBalance(relevantUserId, confirmed, userBalance + amountInSAT);
            if(!updateBalanceResult){
                result.put("error", "Failed to update user balance");
                return new ProcessDepositResult(result, null);
            }
        }
        else {
            // We have seen this tx before, now we try to confirm it.
            if(TransactionDB.txInputsUsedInDB(rt)){
                result.put("error", "Inputs of this transaction has already been funded (!=NOT_PRESENT)");
                return new ProcessDepositResult(result, null);
            }
            if(!confirmed) {
                result.put("error", "We have a record of this tx but it still isn't confirmed. No action taken.");
                return new ProcessDepositResult(result, null);
            }
            if(!TransactionDB.updateTxStatus(txHash, true)) {
                result.put("error", "Failed to mark the TX confirmed");
                return new ProcessDepositResult(result, null);
            }
            long unconfirmedUserBalance = UserDB.getUserBalance(relevantUserId, false);
            long confirmedUserBalance = UserDB.getUserBalance(relevantUserId, true);
            if(confirmedUserBalance < 0 || unconfirmedUserBalance < 0) {
                result.put("error", "Failed to retrieve user balances before updating it");
                return new ProcessDepositResult(result, null);
            }
            if(unconfirmedUserBalance < amountInSAT) {
                result.put("error", "User account receiving confirmation for the tx has an unconfirmed balance lower than the tx amount");
                return new ProcessDepositResult(result, null);
            }
            unconfirmedUserBalance -= amountInSAT;
            confirmedUserBalance += amountInSAT;

            boolean updateUnconfirmedResult = UserDB.updateUserBalance(relevantUserId, false, unconfirmedUserBalance);
            boolean updateConfirmedResult = UserDB.updateUserBalance(relevantUserId, true, confirmedUserBalance);
            if(!updateConfirmedResult || !updateUnconfirmedResult) {
                result.put("error", "Failed to update user balances");
                return new ProcessDepositResult(result, null);
            }
        }
        result.put("result", "Deposit " + amountInSAT + " into " + relevantUserId + " (confirmed=" + confirmed + ")");
        return new ProcessDepositResult(result, rt.extractInputTxIds());
    }

    public static Result txNotify(String payload) {
        String remoteAddress = request().remoteAddress();
        if(!remoteAddress.equals("127.0.0.1") && !remoteAddress.equals("0:0:0:0:0:0:0:1"))
            return unauthorized("This endpoint can only reached through localhost");

        // We need to always pick the local cluster because that's the only one that would be calling txNotify on us
        BitcoindInterface bi = BitcoindNodes.getLocalNodeInferface();
        Transaction tx = bi.gettransaction(payload);
        long confirmations = tx.getConfirmations();
        boolean confirmed = (confirmations >= Bitcoind.CONFIRM_AFTER);

        List<Transaction.Deposit> deposits = tx.extractDeposits();
        if(deposits == null || deposits.size() == 0)
            return ok("This transaction doesn't contain any deposits");

        List<ObjectNode> results = new ArrayList<>();
        for(Transaction.Deposit d : deposits){
            ProcessDepositResult result = processDeposit(d, payload, bi);
            if(confirmed && !TransactionDB.addTxInputsToDB(result.depositInputs, tx.getTxid()))
                result.result.put("error", "Failed to add tx inputs to DB");
            results.add(result.result);
        }

        return ok(Json.toJson(results));
    }

    public static Result blockNotify(String payload) {
        return ok("NOT IMPLEMENTED YET");
    }
}
