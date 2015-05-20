package controllers;

import internal.rpc.pojo.Info;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import internal.Bitcoind;
import internal.BitcoindClusters;
import play.db.DB;
import play.libs.Json;
import play.mvc.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class Interface extends Controller {
    public static Result getUser(String name) {
        Connection c = DB.getConnection();
        try {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM account_holders WHERE account_name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if(rs == null || !rs.next()) {
                c.close();
                return internalServerError("User not found");
            }
            c.close();
            ObjectNode root = mapper.createObjectNode();
            root.put("user_id", rs.getLong("account_id"));
            root.put("account_name", rs.getString("account_name"));
            root.put("cluster_id", rs.getLong("cluster_id"));
            root.put("confirmed_satoshi_balance", rs.getLong("confirmed_satoshi_balance"));
            root.put("unconfirmed_satoshi_balance", rs.getLong("unconfirmed_satoshi_balance"));
            return ok(root);
        } catch(Exception e){
            e.printStackTrace();
            return internalServerError("An error occurred while fetching user information");
        }
    }

    public static Result getTransactionsForUser(String name, Integer page) {
        List<ObjectNode> txs = Bitcoind.getTransactions(name, page);
        if(txs == null)
            return internalServerError();
        return ok(Json.toJson(txs));
    }

    public static Result getAddressesForUser(String name) {
        List<String> addresses = Bitcoind.getAddresses(name);
        if(addresses == null)
            return internalServerError();
        return ok(Json.toJson(addresses));
    }

    public static Result getNewAddressForUser(String name) {
        String address = Bitcoind.getNewAddress(name);
        if(address == null)
            return internalServerError();
        return ok(Json.toJson(address));
    }

    public static Result getClusterStatus(Integer id) {
        Info info = Bitcoind.getInfo(id);
        if(info == null)
            return internalServerError("An error occurred while querying the cluster.");
        return ok(Json.toJson(info));
    }

    private static final ObjectMapper mapper = new ObjectMapper();
    public static Result getClusters(){
        List<BitcoindClusters.ClusterInfo> clusters = BitcoindClusters.getClusters();
        if(clusters == null)
            return internalServerError("An error occurred while fetching available clusters");

        ObjectNode root = mapper.createObjectNode();
        for(BitcoindClusters.ClusterInfo info : clusters)
            root.put(info.id, info.connString);
        return ok(root);
    }

    public static Result sweepFunds(Integer id, String target) {
        Bitcoind.Pair<String, Long> sweepResult = Bitcoind.sweepFunds(id, target);
        if(sweepResult == null)
            return internalServerError("An error occurred while sweeping funds");
        if(sweepResult.u == -1l)
            return internalServerError(sweepResult.t);

        ObjectNode root = mapper.createObjectNode();
        root.put("tx", sweepResult.t);
        root.put("satoshi_amount", sweepResult.u);
        return ok(root);
    }
}
