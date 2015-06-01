package internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import internal.database.CommonDB;
import internal.database.NodeDB;
import internal.rpc.BitcoindInterface;

import internal.rpc.pojo.Info;
import play.*;
import play.libs.F;
import play.mvc.*;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Global extends GlobalSettings {
    @Override
    public void onStart(Application app) {
        Logger.info("Checking if the database schema is initialized...");
        if(!CommonDB.tableExists("account_holders")) {
            Logger.info("Initializing the schema...");
            if(!CommonDB.initializeSchema()){
                Logger.error("Failed to initialize schema. Exiting.");
                System.exit(-1);
            }
        }

        // Checking if we have a local bitcoind node defined by the environment variables
        play.Configuration conf = Play.application().configuration();
        BitcoindInterface localInterface = BitcoindNodes.getLocalNodeInferface();
        Info localNodeInfo = null;
        if(localInterface != null){
            try {
                localNodeInfo = localInterface.getinfo();
            } catch (Exception e){ e.printStackTrace(); }

            if(localNodeInfo != null){
                Logger.info("Successfully fetched getinfo() from bitcoind");

                String humanReadableName = conf.getString("subseq.nodeinfo.name");
                NodeDB.BitcoindNodeInfo nodeInfo = NodeDB.getNodeInformationByName(humanReadableName);
                if(nodeInfo != null){
                    boolean informationCorrect = conf.getString("subseq.nodeinfo.name").equals(nodeInfo.humanReadableName) &&
                                                 conf.getString("subseq.nodeinfo.externalip").equals(nodeInfo.externalIP) &&
                                                 conf.getString("subseq.localbitcoind.connString").equals(nodeInfo.connString) &&
                                                 conf.getString("subseq.localbitcoind.rpcUsername").equals(nodeInfo.rpcUsername) &&
                                                 conf.getString("subseq.localbitcoind.rpcPassword").equals(nodeInfo.rpcPassword) &&
                                                 conf.getInt("subseq.nodeinfo.httpport").equals(nodeInfo.httpPort) &&
                                                 conf.getInt("subseq.nodeinfo.bitcoindport").equals(nodeInfo.bitcoindPort);
                    if(!informationCorrect) {
                        Logger.error("Another node has been found with the name '" + humanReadableName + "\n" +
                                "Either remove the row containing the node and re-run Subsequence or pick a new name for this node.");
                        System.exit(-1);
                    }
                    else
                        Logger.info("Database contains the correct information for this node.");
                }
                else {
                    if(NodeDB.registerLocalNode())
                        Logger.info("Added this node to the database");
                    else {
                        Logger.error("An error occured when adding this node to the database. Exiting...");
                        System.exit(-1);
                    }
                }
            }
            else
                Logger.info("Subsequence failed to connect to your local bitcoind instance. This server will run in API-only mode.");
        }
        else
            Logger.info("Subsequence isn't configured to communicate with a local bitcoind instance. This server will run in API-only mode.");

        Logger.info("Fetching the available Bitcoind nodes...");
        int clusterCount = BitcoindNodes.getNodeCount();
        if(clusterCount == 0){
            Logger.error("Found no nodes defined in your database. You can't run an API-only node without any bitcoind nodes.");
            System.exit(-1);
        }
        Logger.info("Found " + clusterCount + " nodes. Starting service...");
    }

    @Override
    public void onStop(Application app) { }

    @Override
    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader request) {
        ObjectNode res = mapper.createObjectNode();
        res.put("error", "The API endpoint you requested does not exist");
        return F.Promise.promise(() -> play.mvc.Results.notFound(res));
    }

    @Override
    public F.Promise<Result> onError(Http.RequestHeader request, Throwable t) {
        return F.Promise.promise(() -> errorHandler(t));
    }

    public static final ObjectMapper mapper = new ObjectMapper();
    private Result errorHandler(Throwable t){
        ObjectNode res = mapper.createObjectNode();
        if(t == null) {
            res.put("error", "An unknown error occured");
            return play.mvc.Results.internalServerError(res);
        }

        String errorMessage = t.getMessage();
        res.put("error", errorMessage != null ? errorMessage : "An error occured and getMessage() on error returned null");

        try {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            String st = sw.toString();
            String[] items = st.split("\n");
            ArrayNode jsonItems = mapper.createArrayNode();
            for(String item : items){
                jsonItems.add(item.replace("\t", "  "));
            }
            res.put("stacktrace", jsonItems);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("stacktrace", "An error occurred while getting the stacktrace");
        }

        return play.mvc.Results.internalServerError(res);
    }
}