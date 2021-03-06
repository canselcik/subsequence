package internal;

import internal.rpc.BitcoindInterface;
import internal.rpc.pojo.Info;
import internal.rpc.pojo.Transaction;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Bitcoind {
    public static final BigDecimal TX_FEE = BigDecimal.valueOf(0.0001);
    public static final Long TX_FEE_SAT = Global.BTCtoSAT(TX_FEE).longValueExact();
    public static final int CONFIRM_AFTER = 1;

    public static class Pair<T, U> {
        public final T t;
        public final U u;

        public Pair(T t, U u) {
            this.t= t;
            this.u= u;
        }
    }

    public static String getNewAddress(String user) {
        BitcoindInterface btcdInterface = BitcoindNodes.getInterface(user);
        if(btcdInterface == null)
            return null;
        return btcdInterface.getnewaddress(user);
    }

    public static Pair<String, Long> sweepFunds(Integer id, String target) {
        BitcoindInterface btcdInterface = BitcoindNodes.getNodeInterface(id);
        if(btcdInterface == null)
            return new Pair<String, Long>("Cannot find cluster", -1l);
        BigDecimal clusterBalance = btcdInterface.getbalance();
        if(clusterBalance == null)
            return new Pair<String, Long>("Cannot fetch cluster balance", -1l);

        BigDecimal netSweepAmount = clusterBalance.subtract(TX_FEE);
        // If the netSweepAmount is negative, we don't create the tx
        if(netSweepAmount.compareTo(BigDecimal.valueOf(0)) == -1)
            return new Pair<String, Long>("The cluster doesn't have large enough of a balance to create a sweep tx", -1l);

        String txHash = btcdInterface.sendtoaddress(target, netSweepAmount);
        return new Pair<String, Long>(txHash, Global.BTCtoSAT(netSweepAmount).longValueExact());
    }

    public static Info getInfo(Integer clusterId){
        BitcoindInterface btcdInterface = BitcoindNodes.getNodeInterface(clusterId);
        if(btcdInterface == null)
            return null;
        return btcdInterface.getinfo();
    }

    public static List<String> getAddresses(String user){
        BitcoindInterface btcdInterface = BitcoindNodes.getInterface(user);
        if(btcdInterface == null)
            return null;
        return btcdInterface.getaddressesbyaccount(user);
    }

    public static List<ObjectNode> getInboundTransactionsFromBitcoindNode(String user, int page){
        BitcoindInterface btcdInterface = BitcoindNodes.getInterface(user);
        if(btcdInterface == null)
            return null;
        int count = 20;
        int offset = count * page;

        List<Transaction> a = btcdInterface.listtransactions(user, count, offset);
        List<ObjectNode> txs = new ArrayList<>();
        for(Transaction tx : a){
            ObjectNode node = Global.mapper.createObjectNode();
            node.put("txid", tx.getTxid());
            node.put("amount", tx.getAmount());
            node.put("time", tx.getTime());
            txs.add(node);
        }
        return txs;
    }
}
