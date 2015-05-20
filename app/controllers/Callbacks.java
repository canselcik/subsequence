package controllers;

import internal.Bitcoind;
import internal.rpc.BitcoindInterface;
import internal.rpc.pojo.RawTransaction;
import internal.rpc.pojo.Transaction;
import internal.BitcoindClusters;
import internal.rpc.pojo.VinBlock;
import play.db.DB;
import play.mvc.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Callbacks extends Controller {
    private enum TxDatabasePresence {
        ERROR(-1), NOT_PRESENT(0), UNCONFIRMED(1), CONFIRMED(2);
        private final int id;
        TxDatabasePresence(int id) { this.id = id; }
        public int getValue() { return id; }
    };
    private static TxDatabasePresence txPresentInDB(String txHash){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps =
                    c.prepareStatement("SELECT internal_txid, matched_user_id, inbound, tx_hash, confirmed FROM transactions WHERE tx_hash = ?");
            ps.setString(1, txHash);
            ResultSet rs = ps.executeQuery();
            c.close();
            if(rs == null)
                return TxDatabasePresence.ERROR;
            if(!rs.next())
                return TxDatabasePresence.NOT_PRESENT;
            if(!rs.getString("tx_hash").equals(txHash))
                return TxDatabasePresence.NOT_PRESENT;

            if(rs.getBoolean("confirmed"))
                return TxDatabasePresence.CONFIRMED;
            else
                return TxDatabasePresence.UNCONFIRMED;
        }
        catch(Exception e){
            e.printStackTrace();
            return TxDatabasePresence.ERROR;
        }
    }

    private static long getIdFromAccountName(String accountName){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps =
                    c.prepareStatement("SELECT account_id FROM account_holders WHERE account_name = ?");
            ps.setString(1, accountName);
            ResultSet rs = ps.executeQuery();
            c.close();

            if(rs == null)
                return -1;
            if(!rs.next())
                return -1;
            return rs.getLong("account_id");
        }
        catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    private static boolean insertTxIntoDB(String txHash, long userId, boolean inbound, boolean confirmed, long amountSatoshis){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps =
                    c.prepareStatement("INSERT INTO transactions(matched_user_id, inbound, tx_hash, confirmed, amount_satoshi) VALUES(?, ?, ?, ?, ?)");
            ps.setLong(1, userId);
            ps.setBoolean(2, inbound);
            ps.setString(3, txHash);
            ps.setBoolean(4, confirmed);
            ps.setLong(5, amountSatoshis);
            int result = ps.executeUpdate();
            c.close();
            boolean ret = (result == 1);
            return ret;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static boolean updateTxStatus(String txHash, boolean confirmed){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps =
                    c.prepareStatement("UPDATE transactions SET confirmed = ? WHERE tx_hash = ?");
            ps.setBoolean(1, confirmed);
            ps.setString(2, txHash);
            int result = ps.executeUpdate();
            c.close();
            boolean ret = (result == 1);
            return ret;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static long getUserBalance(long userId, boolean confirmed){
        Connection c = DB.getConnection();
        try {
            String fetchCol = confirmed ? "confirmed_satoshi_balance" : "unconfirmed_satoshi_balance";
            PreparedStatement ps =
                    c.prepareStatement("SELECT " + fetchCol + " FROM account_holders WHERE account_id = ?");

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            c.close();

            if(rs == null)
                return -1;
            if(!rs.next())
                return -1;
            return rs.getLong(fetchCol);
        }
        catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    private static boolean updateUserBalance(long userId, boolean confirmed, long updatedValue){
        Connection c = DB.getConnection();
        try {
            String updateCol = confirmed ? "confirmed_satoshi_balance" : "unconfirmed_satoshi_balance";
            PreparedStatement ps =
                    c.prepareStatement("UPDATE account_holders SET " + updateCol + " = ? WHERE account_id = ?");

            ps.setLong(1, updatedValue);
            ps.setLong(2, userId);

            int updatedRows = ps.executeUpdate();
            c.close();

            boolean res = (updatedRows == 1);
            return res;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static boolean txInputsUsedInDB(RawTransaction tx){
        if(tx == null)
            return true;
        List<String> inputTxIds = tx.extractInputTxIds();
        if(inputTxIds.size() == 0)
            return true;

        Connection conn = DB.getConnection();
        try {
            for(String txid : inputTxIds) {
                PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM used_txos WHERE txo = ?");
                ps.setString(1, txid);
                ResultSet rs = ps.executeQuery();
                if (rs == null || !rs.next()) {
                    conn.close();
                    return true;
                }
                Integer count = rs.getInt(1);
                if (count == null || count > 0)
                    return true;
            }
            conn.close();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private static boolean addTxInputsToDB(RawTransaction tx) {
        if(tx == null)
            return false;
        List<String> inputTxIds = tx.extractInputTxIds();
        if(inputTxIds.size() == 0)
            return false;

        Connection conn = DB.getConnection();
        try {
            for(String txid : inputTxIds){
                PreparedStatement ps = conn.prepareStatement("INSERT INTO used_txos (txo, new_txid) VALUES (?, ?)");
                ps.setString(1, txid);
                ps.setString(2, tx.getTxid());
                int res = ps.executeUpdate();
                if(res == 0)
                    return false;
            }
            conn.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Result txNotify(String payload) {
        // TODO: Figure out a way to pick cluster, maybe picking at random might make sense.
        BitcoindInterface bi = BitcoindClusters.getClusterInterface(1);
        Transaction tx = bi.gettransaction(payload);
        String txType = tx.getCategory();
        if(!txType.equals("receive"))
            return ok("Outbound tx requires no additional balance bookkeeping on txnotify");

        RawTransaction rt = bi.getrawtransaction(payload, 1);
        long confirmations = tx.getConfirmations();
        boolean confirmed = (confirmations >= Bitcoind.CONFIRM_AFTER);
        String account = tx.getAccount();
        String address = tx.getAddress();
        BigDecimal amount = tx.getAmount();
        long relevantUserId = getIdFromAccountName(account);
        long amountInSAT = amount.multiply(BigDecimal.valueOf(100000000)).longValueExact();

        if(relevantUserId < 0)
            return internalServerError("Related user account cannot be found");

        TxDatabasePresence presence = txPresentInDB(payload);

        // First time seeing the tx
        if(presence.getValue() == TxDatabasePresence.NOT_PRESENT.getValue()){
            boolean txDbPushResult = insertTxIntoDB(payload, relevantUserId, true, confirmed, amountInSAT);
            if(!txDbPushResult)
                return internalServerError("Failed to commit the tx into the DB");

            if(txInputsUsedInDB(rt))
                return internalServerError("Inputs of this transaction has already been funded (NOT_PRESENT)");

            long userBalance = getUserBalance(relevantUserId, confirmed);
            if(userBalance < 0)
                return internalServerError("Failed to retrieve unconfirmed user balance before updating it");

            if(confirmed)
                if(!addTxInputsToDB(rt))
                    return internalServerError("Failed to add tx inputs to DB");

            boolean updateBalanceResult = updateUserBalance(relevantUserId, confirmed, userBalance + amountInSAT);
            if(!updateBalanceResult)
                return internalServerError("Failed to update user balance");

            return ok("Transaction processed");
        }
        else {
            // We have seen this tx before, now we try to confirm it.
            if(txInputsUsedInDB(rt))
                return internalServerError("Inputs of this transaction has already been funded (!=NOT_PRESENT)");
            if(!confirmed)
                return internalServerError("We have a record of this tx but it still isn't confirmed. No action taken.");
            if(!addTxInputsToDB(rt))
                return internalServerError("Failed to add tx inputs to DB");
            if(!updateTxStatus(payload, true))
                return internalServerError("Failed to mark the TX confirmed");

            long unconfirmedUserBalance = getUserBalance(relevantUserId, false);
            long confirmedUserBalance = getUserBalance(relevantUserId, true);
            if(confirmedUserBalance < 0 || unconfirmedUserBalance < 0)
                return internalServerError("Failed to retrieve user balances before updating it");
            if(unconfirmedUserBalance < amountInSAT)
                return internalServerError("User account receiving confirmation for the tx has an unconfirmed balance lower than the tx amount");

            unconfirmedUserBalance -= amountInSAT;
            confirmedUserBalance += amountInSAT;

            boolean updateUnconfirmedResult = updateUserBalance(relevantUserId, false, unconfirmedUserBalance);
            boolean updateConfirmedResult = updateUserBalance(relevantUserId, true, confirmedUserBalance);
            if(!updateConfirmedResult || !updateUnconfirmedResult)
                return internalServerError("Failed to update user balances");

            return ok("Transaction processed successfully");
        }
    }

    public static Result blockNotify(String payload) {
        return ok("NOT IMPLEMENTED YET");
    }
}
