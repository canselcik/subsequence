package internal;

import internal.rpc.BitcoindClientFactory;
import internal.rpc.BitcoindInterface;
import com.google.common.primitives.Longs;
import play.db.DB;

import java.net.URL;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BitcoindClusters {
    public static class ClusterInfo {
        public String id, connString;
        public BitcoindClientFactory factory;
        public ClusterInfo(String id, String connString, BitcoindClientFactory factory){
            this.id = id;
            this.connString = connString;
            this.factory = factory;
        }
    }

    private static List<ClusterInfo> clusters = new ArrayList<ClusterInfo>();
    public static int loadBitcoindClusters(){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps = c.prepareStatement("SELECT id, conn_string, rpc_username, rpc_password FROM bitcoind_clusters ORDER BY id ASC");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String id = rs.getString("id");
                String connString = rs.getString("conn_string");
                String rpcUsername = rs.getString("rpc_username");
                String rpcPassword = rs.getString("rpc_password");
                BitcoindClientFactory bcf = new BitcoindClientFactory(new URL(connString), rpcUsername, rpcPassword);
                clusters.add( new ClusterInfo(id, connString, bcf) );
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clusters.size();
    }

    private static int assignCluster(String user){
        try {
            MessageDigest cript = MessageDigest.getInstance("SHA-1");
            cript.reset();
            cript.update(user.getBytes("utf8"));
            Long i = Longs.fromByteArray(cript.digest());
            return (int)(i % clusters.size());
        }
        catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public static List<ClusterInfo> getClusters(){
        return clusters;
    }

    public static BitcoindInterface getClusterInterface(Integer clusterId){
        for(ClusterInfo i : clusters){
            if(i.id.equals(String.valueOf(clusterId)))
                return i.factory.getClient();
        }
        return null;
    }

    public static BitcoindInterface getInterface(String user){
        Integer assignment = checkClusterAssignmentFromDB(user);
        if(assignment == null){
            assignment = assignCluster(user);
            boolean writeResult = writeClusterAssignmentToDB(user, Integer.parseInt(clusters.get(assignment).id));
            if(!writeResult)
                return null;
        }
        return clusters.get(assignment).factory.getClient();
    }

    private static boolean writeClusterAssignmentToDB(String user, Integer clusterAssignment){
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
            boolean res = (affectedRows == 1);
            return res;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Integer checkClusterAssignmentFromDB(String user){
        Connection c = DB.getConnection();
        if(c == null)
            return null;
        try {
            PreparedStatement ps = c.prepareStatement("SELECT cluster_id FROM account_holders WHERE account_name = ?");
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            if(rs == null) return null;
            if(!rs.next()) return null;
            Integer result = rs.getInt(1);
            c.close();

            for(int i = 0; i < clusters.size(); i++){
                if( clusters.get(i).id.equals(String.valueOf(result)) )
                    return i;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
