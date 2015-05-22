package internal;

import internal.database.ClusterDB;
import internal.database.UserDB;
import internal.rpc.BitcoindClientFactory;
import internal.rpc.BitcoindInterface;

import play.Play;

import java.net.URL;
import java.util.List;

public class BitcoindClusters {
    public static int getClusterCount() {
        List<ClusterDB.ClusterInfo> clusters = ClusterDB.getBitcoindClusters();
        if(clusters == null)
            return 0;
        return clusters.size();
    }

    public static BitcoindInterface getClusterInterface(Integer clusterId){
        List<ClusterDB.ClusterInfo> clusters = ClusterDB.getBitcoindClusters();
        if(clusters == null || clusters.size() == 0)
            return null;
        for(ClusterDB.ClusterInfo i : clusters){
            if(i.id == clusterId)
                return i.factory.getClient();
        }
        return null;
    }

    private static BitcoindClientFactory localInterfaceFactory;
    public static BitcoindInterface getLocalClusterInferface(){
        if(localInterfaceFactory != null)
            return localInterfaceFactory.getClient();
        try {
            play.Configuration conf = Play.application().configuration();
            String connString = conf.getString("subseq.localbitcoind.connString");
            String rpcUsername = conf.getString("subseq.localbitcoind.rpcUsername");
            String rpcPassword = conf.getString("subseq.localbitcoind.rpcPassword");
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
        Integer assignment = ClusterDB.checkClusterAssignmentFromDB(user);
        if(assignment != null)
            return getClusterInterface(assignment);

        ClusterDB.ClusterInfo leastOccupied = ClusterDB.findLeastOccupiedCluster();
        boolean writeResult = UserDB.writeClusterAssignmentToDB(user, leastOccupied.id);
        if(!writeResult)
            return null;
        else
            return leastOccupied.factory.getClient();
    }
}
