package internal;

import internal.database.CommonDB;
import internal.rpc.BitcoindInterface;

import play.*;

public class Global extends GlobalSettings {
    @Override
    public void onStart(Application app) {
        Logger.error("Checking if the database schema is initialized...");
        if(!CommonDB.tableExists("account_holders")) {
            Logger.error("Initializing the schema...");
            if(!CommonDB.initializeSchema()){
                Logger.error("Failed to initialize schema. Exiting.");
                System.exit(-1);
            }
        }

        Logger.error("Fetching the available Bitcoind nodes...");
        int clusterCount = BitcoindNodes.getNodeCount();
        if(clusterCount == 0){
            Logger.error("Found no nodes defined in your database. Exiting. Please restart after adding your Bitcoind nodes.");
            System.exit(-1);
        }
        Logger.error("Found " + clusterCount + " clusters. Starting service...");

        Logger.error("Checking if the local bitcoind node (defined by environment vars) is up...");
        BitcoindInterface localInterface = BitcoindNodes.getLocalNodeInferface();
        if(localInterface == null){
            Logger.error("Failed to connect to the local bitcoind instance... exiting.");
            System.exit(-1);
        }
    }

    @Override
    public void onStop(Application app) { }
}