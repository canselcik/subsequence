package internal;

import play.*;
import play.db.DB;

import java.sql.*;


public class Global extends GlobalSettings {
    private boolean initializeSchema(){
        Connection c = DB.getConnection();
        try {
            Statement s = c.createStatement();
            s.addBatch("CREATE TABLE \"bitcoind_clusters\" (\"id\" serial PRIMARY KEY, \"conn_string\" TEXT, \"rpc_username\" TEXT, \"rpc_password\" TEXT) WITH (OIDS=FALSE);");
            s.addBatch("CREATE TABLE \"account_holders\" (\"account_id\" bigserial PRIMARY KEY, \"account_name\" TEXT, \"cluster_id\" int, \"confirmed_satoshi_balance\" bigint, \"unconfirmed_satoshi_balance\" bigint) WITH (OIDS=FALSE);");
            s.addBatch("ALTER TABLE \"account_holders\" ADD CONSTRAINT account_holders_fk0 FOREIGN KEY (cluster_id) REFERENCES bitcoind_clusters(id);");
            s.addBatch("CREATE TABLE \"transactions\" (\"internal_txid\" bigserial PRIMARY KEY, \"matched_user_id\" bigint, \"inbound\" BOOLEAN, \"tx_hash\" TEXT, \"confirmed\" BOOLEAN, \"amount_satoshi\" bigint) WITH (OIDS=FALSE);");
            s.addBatch("ALTER TABLE \"transactions\" ADD CONSTRAINT transactions_fk0 FOREIGN KEY (matched_user_id) REFERENCES account_holders(account_id);");
            s.addBatch("CREATE TABLE \"used_txos\" (\"used_txo_id\" bigserial PRIMARY KEY, \"txo\" TEXT, \"new_txid\" TEXT ) WITH (OIDS=FALSE);");
            s.addBatch("ALTER TABLE \"used_txos\" ADD CONSTRAINT used_txos_fk0 FOREIGN KEY (associated_internaltx_id) REFERENCES transactions(internal_txid);");
            s.addBatch("CREATE INDEX used_txos_txo_idx ON used_txos(txo);");
            s.addBatch("CREATE INDEX transactions_matched_user_id_idx ON transactions(matched_user_id);");
            s.addBatch("CREATE INDEX account_holders_account_name_idx ON account_holders(account_name);");
            s.addBatch("CREATE INDEX used_txos_new_txid_idx ON used_txos(new_txid);");

            s.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean tableExists(String tableName){
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT relname FROM pg_class WHERE relname = ?");
            ps.setString(1, tableName);

            boolean exists = false;
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                exists = rs.getString("relname").equals(tableName);

            c.close();
            return exists;
        }
        catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onStart(Application app) {
        Logger.error("Checking if the database schema is initialized...");
        if(!tableExists("account_holders")) {
            Logger.error("Initializing the schema...");
            if(!initializeSchema()){
                Logger.error("Failed to initialize schema. Exiting.");
                System.exit(-1);
            }
        }

        Logger.error("Fetching the available clusters...");
        int clusterCount = BitcoindClusters.loadBitcoindClusters();
        if(clusterCount == 0){
            Logger.error("Found no clusters defined in your database. Exiting. Please restart after adding your clusters.");
            System.exit(0);
        }
        Logger.error("Found " + clusterCount + " clusters. Starting service...");
    }

    @Override
    public void onStop(Application app) {

    }
}