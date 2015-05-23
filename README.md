![](https://raw.githubusercontent.com/canselcik/subsequence/master/public/images/logo.png)
---
- Subsequence simplifies accepting Bitcoin payments without relying on any third party service.
- Scales horizontally, allows clustering users into different bitcoind instances
- Keeps track of confirmed and unconfirmed user balances
- Allows sweeping funds from individual bitcoind instances
- Exposes all of this data through a simple HTTP API with JSON responses
- Based on Java Play 2 Framework, uses Postgresql as its database. Communicates with the bitcoind instances through the JSON-RPC API.

### Architecture Overview
![](https://raw.githubusercontent.com/canselcik/subsequence/master/public/images/architecture.png)
