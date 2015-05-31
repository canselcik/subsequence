package internal.database;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import internal.Global;
import internal.rpc.pojo.RawTransaction;
import play.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class TransactionDB {
    public enum TxDatabasePresence {
        ERROR(-1), NOT_PRESENT(0), UNCONFIRMED(1), CONFIRMED(2);
        private final int id;
        TxDatabasePresence(int id) { this.id = id; }
        public int getValue() { return id; }
    };
    public static TxDatabasePresence txPresentInDB(String txHash){
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

    public static boolean insertTxIntoDB(String txHash, long userId, boolean inbound, boolean confirmed, long amountSatoshis){
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

    public static boolean updateTxStatus(String txHash, boolean confirmed){
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

    public static boolean txInputsUsedInDB(RawTransaction tx){
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

    public static boolean addTxInputsToDB(List<String> inputTxIds, String parentTx) {
        if(inputTxIds == null)
            return false;
        if(inputTxIds.size() == 0)
            return false;

        Connection conn = DB.getConnection();
        try {
            for(String txid : inputTxIds){
                PreparedStatement ps = conn.prepareStatement("INSERT INTO used_txos (txo, new_txid) VALUES (?, ?)");
                ps.setString(1, txid);
                ps.setString(2, parentTx);
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

    public static ArrayNode getTransactions(Long accountId){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps =
                    c.prepareStatement("SELECT * FROM transactions WHERE matched_user_id = ?");
            ps.setLong(1, accountId);
            ResultSet rs = ps.executeQuery();
            if(rs == null)
                return null;

            ArrayNode nodes = Global.mapper.createArrayNode();
            while(rs.next()){
                ObjectNode root = Global.mapper.createObjectNode();
                root.put("internal_txid", rs.getLong("internal_txid"));
                root.put("matched_user_id", rs.getLong("matched_user_id"));
                root.put("inbound", rs.getBoolean("inbound"));
                root.put("txhash", rs.getString("tx_hash"));
                if(rs.getBoolean("inbound"))
                    root.put("confirmed", rs.getBoolean("confirmed"));
                root.put("satoshi_amount", rs.getLong("amount_satoshi"));
                nodes.add(root);
            }
            c.close();
            return nodes;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
