package internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import internal.database.CommonDB;
import internal.rpc.BitcoindInterface;

import play.Application;
import play.GlobalSettings;
import play.Logger;
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

        Logger.info("Fetching the available Bitcoind nodes...");
        int clusterCount = BitcoindNodes.getNodeCount();
        if(clusterCount == 0){
            Logger.error("Found no nodes defined in your database. Exiting. Please restart after adding your Bitcoind nodes.");
            System.exit(-1);
        }
        Logger.info("Found " + clusterCount + " clusters. Starting service...");

        Logger.info("Checking if the local bitcoind node (defined by environment vars) is up...");
        BitcoindInterface localInterface = BitcoindNodes.getLocalNodeInferface();
        if(localInterface == null){
            Logger.error("Failed to connect to the local bitcoind instance... exiting.");
            System.exit(-1);
        }
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

    public final ObjectMapper mapper = new ObjectMapper();
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
            res.put("stacktrace", st);
        } catch (Exception e) {
            e.printStackTrace();
            res.put("stacktrace", "An error occurred while getting the stacktrace");
        }

        return play.mvc.Results.internalServerError(res);
    }


}