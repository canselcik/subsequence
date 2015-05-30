package internal.database;

import internal.rpc.BitcoindClientFactory;
import play.Logger;
import play.Play;
import play.db.DB;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NodeDB {
    public static class BitcoindNodeInfo {
        public final int id, bitcoindPort, httpPort, accountCount;
        public final String externalIP, connString, humanReadableName, rpcUsername, rpcPassword;
        public final BitcoindClientFactory factory;
        public BitcoindNodeInfo(int id, String humanReadableName, String externalIP, String connString,
                                int bitcoindPort, int httpPort, String rpcUsername, String rpcPassword, BitcoindClientFactory factory, int accountCount){
            this.id = id;
            this.connString = connString;
            this.factory = factory;
            this.accountCount = accountCount;
            this.httpPort = httpPort;
            this.bitcoindPort = bitcoindPort;
            this.externalIP = externalIP;
            this.humanReadableName = humanReadableName;
            this.rpcPassword = rpcPassword;
            this.rpcUsername = rpcUsername;
        }
    }

    public static BitcoindNodeInfo findLeastOccupiedBitcoindNode(){
        List<BitcoindNodeInfo> clusters = getBitcoindNodes();
        if(clusters == null || clusters.size() == 0)
            return null;
        return clusters.get(0);
    }

    public static List<BitcoindNodeInfo> getBitcoindNodes(){
        Connection c = DB.getConnection();
        List<BitcoindNodeInfo> clusters = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM bitcoind_nodes ORDER BY account_count ASC");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Integer id = rs.getInt("id");
                String connString = rs.getString("conn_string");
                String rpcUsername = rs.getString("rpc_username");
                String rpcPassword = rs.getString("rpc_password");
                Integer clusterSize = rs.getInt("account_count");
                String humanReadableName = rs.getString("human_readable_name");
                Integer httpPort = rs.getInt("http_port");
                Integer bitcoindPort = rs.getInt("bitcoind_port");
                String externalIP = rs.getString("external_ip");
                BitcoindClientFactory bcf = new BitcoindClientFactory(new URL(connString), rpcUsername, rpcPassword);
                clusters.add( new BitcoindNodeInfo(id, humanReadableName, externalIP, connString, bitcoindPort, httpPort,
                        rpcUsername, rpcPassword, bcf, clusterSize) );
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clusters;
    }

    public static boolean incrementNodeAccountCount(int clusterId){
        Connection c = DB.getConnection();
        if(c == null)
            return false;
        try {
            PreparedStatement ps = c.prepareStatement("UPDATE bitcoind_nodes SET account_count = account_count+ 1 WHERE id = ?");
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

    public static boolean registerLocalNode(){
        play.Configuration conf = Play.application().configuration();

        String humanReadableName = conf.getString("subseq.nodeinfo.name");
        String externalIP        = conf.getString("subseq.nodeinfo.externalip");
        String connString        = conf.getString("subseq.localbitcoind.connString");
        String rpcUsername       = conf.getString("subseq.localbitcoind.rpcUsername");
        String rpcPassword       = conf.getString("subseq.localbitcoind.rpcPassword");
        Integer httpPort         = conf.getInt("subseq.nodeinfo.httpport");
        Integer bitcoindPort     = conf.getInt("subseq.nodeinfo.bitcoindport");
        if(humanReadableName == null || externalIP == null || connString == null || rpcUsername == null ||
                 rpcPassword == null || httpPort == null || bitcoindPort == null) {
            Logger.info("Environment variables aren't set for Subsequence function.");
            return false;
        }

        Connection conn = DB.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO bitcoind_nodes(human_readable_name, external_ip, " +
                    "http_port, bitcoind_port, conn_string, rpc_username, rpc_password, account_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, humanReadableName);
            ps.setString(2, externalIP);
            ps.setInt(3, httpPort);
            ps.setInt(4, bitcoindPort);
            ps.setString(5, connString);
            ps.setString(6, rpcUsername);
            ps.setString(7, rpcPassword);
            ps.setInt(8, 0);
            int affectedRows = ps.executeUpdate();
            conn.close();
            return (affectedRows == 1);
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static BitcoindNodeInfo getNodeInformationByName(String name){
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM bitcoind_nodes ORDER BY account_count ASC");
            ResultSet rs = ps.executeQuery();
            if(!rs.next()) {
                c.close();
                return null;
            }
            c.close();
            Integer id = rs.getInt("id");
            String connString = rs.getString("conn_string");
            String rpcUsername = rs.getString("rpc_username");
            String rpcPassword = rs.getString("rpc_password");
            Integer clusterSize = rs.getInt("account_count");
            String humanReadableName = rs.getString("human_readable_name");
            Integer httpPort = rs.getInt("http_port");
            Integer bitcoindPort = rs.getInt("bitcoind_port");
            String externalIP = rs.getString("external_ip");
            BitcoindClientFactory bcf = new BitcoindClientFactory(new URL(connString), rpcUsername, rpcPassword);
            return new BitcoindNodeInfo(id, humanReadableName, externalIP, connString, bitcoindPort, httpPort, rpcUsername, rpcPassword, bcf, clusterSize);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
