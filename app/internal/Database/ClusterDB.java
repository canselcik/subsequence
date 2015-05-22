package internal.database;

import internal.rpc.BitcoindClientFactory;
import play.db.DB;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClusterDB {
    public static class ClusterInfo {
        public int id;
        public String connString;
        public BitcoindClientFactory factory;
        public int accountCount;
        public ClusterInfo(int id, String connString, BitcoindClientFactory factory, int accountCount){
            this.id = id;
            this.connString = connString;
            this.factory = factory;
            this.accountCount = accountCount;
        }
    }

    public static ClusterInfo findLeastOccupiedCluster(){
        List<ClusterInfo> clusters = getBitcoindClusters();
        if(clusters == null || clusters.size() == 0)
            return null;
        return clusters.get(0);
    }

    public static List<ClusterInfo> getBitcoindClusters(){
        Connection c = DB.getConnection();
        List<ClusterInfo> clusters = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement("SELECT id, conn_string, rpc_username, rpc_password, account_count FROM bitcoind_clusters ORDER BY account_count ASC");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Integer id = rs.getInt("id");
                String connString = rs.getString("conn_string");
                String rpcUsername = rs.getString("rpc_username");
                String rpcPassword = rs.getString("rpc_password");
                Integer clusterSize = rs.getInt("account_count");
                BitcoindClientFactory bcf = new BitcoindClientFactory(new URL(connString), rpcUsername, rpcPassword);
                clusters.add( new ClusterInfo(id, connString, bcf, clusterSize) );
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clusters;
    }

    public static boolean incrementClusterAccountCount(int clusterId){
        Connection c = DB.getConnection();
        if(c == null)
            return false;
        try {
            PreparedStatement ps = c.prepareStatement("UPDATE bitcoind_clusters SET account_count = account_count+ 1 WHERE id = ?");
            ps.setInt(1, clusterId);
            int affectedRows = ps.executeUpdate();
            c.close();
            boolean res = (affectedRows == 1);
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Integer checkClusterAssignmentFromDB(String user){
        Connection c = DB.getConnection();
        if(c == null)
            return null;
        try {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM account_holders WHERE account_name = ?");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            if(rs == null) return null;
            if(!rs.next()) return null;
            Integer result = rs.getInt("cluster_id");
            c.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}