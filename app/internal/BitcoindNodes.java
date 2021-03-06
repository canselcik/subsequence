package internal;

import internal.database.NodeDB;
import internal.database.UserDB;
import internal.rpc.BitcoindClientFactory;
import internal.rpc.BitcoindInterface;

import play.Logger;
import play.Play;

import java.net.URL;
import java.util.List;

public class BitcoindNodes {
    public static int getNodeCount() {
        List<NodeDB.BitcoindNodeInfo> clusters = NodeDB.getBitcoindNodes();
        if(clusters == null)
            return 0;
        return clusters.size();
    }

    public static BitcoindInterface getNodeInterface(Integer clusterId){
        List<NodeDB.BitcoindNodeInfo> clusters = NodeDB.getBitcoindNodes();
        if(clusters == null || clusters.size() == 0)
            return null;
        for(NodeDB.BitcoindNodeInfo i : clusters){
            if(i.id == clusterId)
                return i.factory.getClient();
        }
        return null;
    }

    private static BitcoindClientFactory localInterfaceFactory;
    public static BitcoindInterface getLocalNodeInferface(){
        if(localInterfaceFactory != null)
            return localInterfaceFactory.getClient();
        try {
            play.Configuration conf = Play.application().configuration();

            String externalIP = conf.getString("subseq.waniphost");
            Integer bitcoindPort = conf.getInt("subseq.bitcoindport");
            if(externalIP == null || bitcoindPort == null) {
                Logger.error("Local bitcoind configuration not defined but getLocalNodeInterface() method is called");
                return null;
            }

            String connString = "http://" + externalIP + ":" + bitcoindPort;
            String rpcUsername = conf.getString("subseq.rpcUsername");
            String rpcPassword = conf.getString("subseq.rpcPassword");
            if(connString == null || rpcUsername == null || rpcPassword == null)
                return null;
            localInterfaceFactory = new BitcoindClientFactory(new URL(connString), rpcUsername, rpcPassword);
            return localInterfaceFactory.getClient();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BitcoindInterface getInterface(String user){
        Integer assignment = UserDB.checkNodeAssignmentFromDB(user);
        if(assignment != null)
            return getNodeInterface(assignment);

        NodeDB.BitcoindNodeInfo leastOccupied = NodeDB.findLeastOccupiedBitcoindNode();
        if(leastOccupied == null)
            return null;

        boolean writeResult = UserDB.writeNodeAssignmentToDB(user, leastOccupied.id);
        if(!writeResult)
            return null;
        else
            return leastOccupied.factory.getClient();
    }
}
