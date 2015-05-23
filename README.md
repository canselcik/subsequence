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
#### View Account Information (including confirmed/unconfirmed balance) 
```json
$ http http://ssnode0-stg/accounts/one
HTTP/1.1 200 OK
Content-Length: 116
Content-Type: application/json; charset=utf-8
{
    "account_id": 1,
    "account_name": "one",
    "confirmed_satoshi_balance": 500000,
    "node_id": 1,
    "unconfirmed_satoshi_balance": 0
}
```
#### View Transactions for Account
```json
$ http http://ssnode0-stg/accounts/one/transactions
HTTP/1.1 200 OK
Content-Length: 588
Content-Type: application/json; charset=utf-8

[
    {
        "confirmed": true,
        "inbound": true,
        "internal_txid": 1,
        "matched_user_id": 1,
        "satoshi_amount": 790000,
        "txhash": "373649ebb500d8e7a1142f14ba0ef444b621ef802cf02d08fd6f1f9a0e2ee208"
    },
    {
        "inbound": false,
        "internal_txid": 2,
        "matched_user_id": 1,
        "satoshi_amount": 290000,
        "txhash": "d504563c1810081ecd918c203b223425903edb553d8ff1b0602c6236c447c439"
    },
    {
        "confirmed": true,
        "inbound": true,
        "internal_txid": 3,
        "matched_user_id": 1,
        "satoshi_amount": 1,
        "txhash": "internalTransaction - testing"
    },
    {
        "inbound": false,
        "internal_txid": 4,
        "matched_user_id": 1,
        "satoshi_amount": 1,
        "txhash": "internalTransaction - testing chargeback"
    }
]
```
#### View Addresses for Account (creates account if account does not exist)
```json
$ http http://ssnode0-stg/accounts/one/addresses
HTTP/1.1 200 OK
Content-Length: 112
Content-Type: application/json; charset=utf-8

[
    "1LJzsxKfksDoquSvwGVKCFj1cbPKjMwM7U",
    "1G6MKBhExRBqHfDE2KNmcWFvWoLodQu7xP",
    "1L4JaWFhPGmagjboGNkRxFkNyJZ5CHrdTH"
]
```
#### Create a new Address for Account (creates account if account does not exist)
```json
$ http http://ssnode0-stg/accounts/one/addresses/new
HTTP/1.1 200 OK
Content-Length: 36
Content-Type: application/json; charset=utf-8

"166acuikhQeSrWfVw2a3n1m8XWxh5QYX84"
```

/accounts/<accountName>/withdraw/<amountSatoshisis>/<withdrawalAddress>

#### Creating an internal transaction (to increment decrement user balance without broadcasting a transaction) 
```json
$ http http://size.cselcik.com:9000/accounts/one/balance/decrement/450000/testing%20chargeback                                                                                                                                   user@UMBP
HTTP/1.1 200 OK
Content-Length: 38
Content-Type: text/plain; charset=utf-8

User balance successfully updated to 0
```

/nodes
/nodes/<nodeId>
/nodes/<nodeId>/sweep/<sweepAddress>
```