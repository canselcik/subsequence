package internal.database;

import play.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDB {
    public static long getIdFromAccountName(String accountName){
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

    public static long getUserBalance(long userId, boolean confirmed){
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

    public static boolean updateUserBalance(long userId, boolean confirmed, long updatedValue){
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

    public static boolean writeNodeAssignmentToDB(String user, Integer clusterAssignment){
        Connection c = DB.getConnection();
        if(c == null)
            return false;
        try {
            PreparedStatement ps =
                    c.prepareStatement("INSERT INTO account_holders" +
                            "(account_name, cluster_id, confirmed_satoshi_balance, unconfirmed_satoshi_balance) VALUES(?, ?, ?, ?)");
            ps.setString(1, user);
            ps.setInt(2, clusterAssignment);
            ps.setLong(3, 0);
            ps.setLong(4, 0);
            int affectedRows = ps.executeUpdate();
            c.close();
            if(affectedRows != 1) return false;
            return NodeDB.incrementNodeAccountCount(clusterAssignment);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
