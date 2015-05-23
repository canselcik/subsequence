![](https://raw.githubusercontent.com/canselcik/subsequence/master/public/images/logo.png)
---
- Subsequence simplifies accepting Bitcoin payments without relying on any third party service.
- Scales horizontally, allows clustering users into different bitcoind instances
- Keeps track of confirmed and unconfirmed user balances
- Allows sweeping funds from individual bitcoind instances
- Exposes all of this data through a simple HTTP API with JSON responses
- Based on Java Play 2 Framework, uses Postgresql as its database. Communicates with the bitcoind instances 
through the JSON-RPC API.

### Architecture Overview
![](https://raw.githubusercontent.com/canselcik/subsequence/master/public/images/architecture.png)

### Installation Instructions
**Step 1:** Make sure you have Java 8 installed along with a PostgreSQL database set up.

**Step 2:** Download and install Play 2 Framework from `playframework.com`. This guide will assume you're extracting Play
into your `$PATH`.

**Step 3:** Subsequence uses the following environment variables to know how to communicate with the database and the
local bitcoind instance. Add the following into your `.bashrc`, `.zshrc`etc.:

```bash
    export SUBSEQ_DB_CONN_STRING="jdbc:postgresql://<database-hostname>:<port>/<database-name>"
    export SUBSEQ_DB_USER="<database-username>"
    export SUBSEQ_DB_PASS="<database-password>"
    export SUBSEQ_LOCAL_BITCOIND_RPCCONNSTRING="http://<local-bitcoind-hostname-likely-localhost>:<port-likely-8332>"
    export SUBSEQ_LOCAL_BITCOIND_RPCUSER="<local-bitcoind-rpc-username>"
    export SUBSEQ_LOCAL_BITCOIND_RPCPASS="<local-bitcoind-rpc-password>"
```

**Step 4:** Clone this repository and `cd subsequence`.
**Step 5:** Run `activator dist` to generate the production image, but we will run `sudo activator "run 80"` in `screen` for now to start Subsequence development mode.

**Step 6:** Make an HTTP request to the server. You'll see that it will initialize the schema but exit because it fails to find any bitcoind instances in the database.

**Step 7:** Run the following query on your PostgreSQL database for each and every one of your bitcoind instances:

```sql
    INSERT INTO bitcoind_nodes (conn_string, rpc_username, rpc_password, account_count) 
      VALUES ('http://<bitcoind-hostname>:<bitcoind-port>',
              '<bitcoind-rpc-username>',
              '<bitcoind-rpc-password>', 0);
```

**Step 8:** Repeat step 5 and step 6. You'll notice that Subsequence is up and accessible through port 9000.

### HTTP API Details (To be expanded...)
```
/accounts/:name
/accounts/:name/transactions
/accounts/:name/addresses
/accounts/:name/addresses/new
/accounts/:name/withdraw/:amount/:address

/nodes
/nodes/:id

/nodes/:id/sweep/:target
```