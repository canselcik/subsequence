package controllers;

import internal.Bitcoind;
import internal.rpc.BitcoindInterface;
import internal.rpc.pojo.RawTransaction;
import internal.rpc.pojo.Transaction;
import internal.BitcoindNodes;
import internal.database.TransactionDB;
import internal.database.UserDB;
import play.mvc.*;

import java.math.BigDecimal;

public class Callbacks extends Controller {
    public static Result txNotify(String payload) {
        String remoteAddress = request().remoteAddress();
        if(!remoteAddress.equals("127.0.0.1") && !remoteAddress.equals("0:0:0:0:0:0:0:1"))
            return unauthorized("This endpoint can only reached through localhost");

        // We need to always pick the local cluster because that's the only one that would be calling txNotify on us
        BitcoindInterface bi = BitcoindNodes.getLocalNodeInferface();
        Transaction tx = bi.gettransaction(payload);
        String txType = tx.getCategory();
        if(!txType.equals("receive"))
            return ok("Outbound tx requires no additional balance bookkeeping on txnotify");

        RawTransaction rt = bi.getrawtransaction(payload, 1);
        long confirmations = tx.getConfirmations();
        boolean confirmed = (confirmations >= Bitcoind.CONFIRM_AFTER);
        String account = tx.getAccount();
        BigDecimal amount = tx.getAmount();
        long relevantUserId = UserDB.getIdFromAccountName(account);
        long amountInSAT = amount.multiply(BigDecimal.valueOf(100000000)).longValueExact();

        if(relevantUserId < 0)
            return internalServerError("Related user account cannot be found");

        TransactionDB.TxDatabasePresence presence = TransactionDB.txPresentInDB(payload);

        // First time seeing the tx
        if(presence.getValue() == TransactionDB.TxDatabasePresence.NOT_PRESENT.getValue()){
            boolean txDbPushResult = TransactionDB.insertTxIntoDB(payload, relevantUserId, true, confirmed, amountInSAT);
            if(!txDbPushResult)
                return internalServerError("Failed to commit the tx into the DB");

            if(TransactionDB.txInputsUsedInDB(rt))
                return internalServerError("Inputs of this transaction has already been funded (NOT_PRESENT)");

            long userBalance = UserDB.getUserBalance(relevantUserId, confirmed);
            if(userBalance < 0)
                return internalServerError("Failed to retrieve unconfirmed user balance before updating it");

            if(confirmed)
                if(!TransactionDB.addTxInputsToDB(rt))
                    return internalServerError("Failed to add tx inputs to DB");

            boolean updateBalanceResult = UserDB.updateUserBalance(relevantUserId, confirmed, userBalance + amountInSAT);
            if(!updateBalanceResult)
                return internalServerError("Failed to update user balance");

            return ok("Transaction processed");
        }
        else {
            // We have seen this tx before, now we try to confirm it.
            if(TransactionDB.txInputsUsedInDB(rt))
                return internalServerError("Inputs of this transaction has already been funded (!=NOT_PRESENT)");
            if(!confirmed)
                return internalServerError("We have a record of this tx but it still isn't confirmed. No action taken.");
            if(!TransactionDB.addTxInputsToDB(rt))
                return internalServerError("Failed to add tx inputs to DB");
            if(!TransactionDB.updateTxStatus(payload, true))
                return internalServerError("Failed to mark the TX confirmed");

            long unconfirmedUserBalance = UserDB.getUserBalance(relevantUserId, false);
            long confirmedUserBalance = UserDB.getUserBalance(relevantUserId, true);
            if(confirmedUserBalance < 0 || unconfirmedUserBalance < 0)
                return internalServerError("Failed to retrieve user balances before updating it");
            if(unconfirmedUserBalance < amountInSAT)
                return internalServerError("User account receiving confirmation for the tx has an unconfirmed balance lower than the tx amount");

            unconfirmedUserBalance -= amountInSAT;
            confirmedUserBalance += amountInSAT;

            boolean updateUnconfirmedResult = UserDB.updateUserBalance(relevantUserId, false, unconfirmedUserBalance);
            boolean updateConfirmedResult = UserDB.updateUserBalance(relevantUserId, true, confirmedUserBalance);
            if(!updateConfirmedResult || !updateUnconfirmedResult)
                return internalServerError("Failed to update user balances");

            return ok("Transaction processed successfully");
        }
    }

    public static Result blockNotify(String payload) {
        return ok("NOT IMPLEMENTED YET");
    }
}
