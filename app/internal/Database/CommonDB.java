package internal.database;

import play.db.DB;

import java.sql.*;

public class CommonDB {
    public static boolean initializeSchema(){
        Connection c = DB.getConnection();
        try {
            Statement s = c.createStatement();
            s.addBatch("CREATE TABLE \"bitcoind_clusters\" (\"id\" serial PRIMARY KEY, \"conn_string\" TEXT, \"rpc_username\" TEXT, \"rpc_password\" TEXT, \"account_count\" int) WITH (OIDS=FALSE);");
            s.addBatch("CREATE TABLE \"account_holders\" (\"account_id\" bigserial PRIMARY KEY, \"account_name\" TEXT, \"cluster_id\" int, \"confirmed_satoshi_balance\" bigint, \"unconfirmed_satoshi_balance\" bigint) WITH (OIDS=FALSE);");
            s.addBatch("ALTER TABLE \"account_holders\" ADD CONSTRAINT account_holders_fk0 FOREIGN KEY (cluster_id) REFERENCES bitcoind_clusters(id);");
            s.addBatch("CREATE TABLE \"transactions\" (\"internal_txid\" bigserial PRIMARY KEY, \"matched_user_id\" bigint, \"inbound\" BOOLEAN, \"tx_hash\" TEXT UNIQUE, \"confirmed\" BOOLEAN, \"amount_satoshi\" bigint) WITH (OIDS=FALSE);");
            s.addBatch("ALTER TABLE \"transactions\" ADD CONSTRAINT transactions_fk0 FOREIGN KEY (matched_user_id) REFERENCES account_holders(account_id);");
            s.addBatch("CREATE TABLE \"used_txos\" (\"used_txo_id\" bigserial PRIMARY KEY, \"txo\" TEXT, \"new_txid\" TEXT ) WITH (OIDS=FALSE);");
            s.addBatch("ALTER TABLE \"used_txos\" ADD CONSTRAINT used_txos_fk0 FOREIGN KEY (new_txid) REFERENCES transactions(tx_hash);");
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

    public static boolean tableExists(String tableName){
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
}
