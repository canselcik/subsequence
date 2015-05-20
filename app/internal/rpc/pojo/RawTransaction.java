package internal.rpc.pojo;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown=true)
public class RawTransaction {
    private String txid;
    private int version;
    private String hex;
    private List<VinBlock> vin;
    private List<VoutBlock> vout;
    private String blockhash;
    private long confirmations;
    private long time;
    private long blocktime;
    private long locktime;

    public String getHex() { return hex; }
    public long getLocktime() { return locktime; }
    public String getTxid() { return txid; }
    public int getVersion() { return version; }
    public List<VinBlock> getVin() { return vin; }
    public List<VoutBlock> getVout() { return vout; }
    public String getBlockhash() { return blockhash; }
    public long getConfirmations() { return confirmations; }
    public long getTime() { return time; }
    public long getBlocktime() { return blocktime; }

    @JsonIgnore
    public List<String> extractInputTxIds(){
        List<String> inputTxIds = new ArrayList<>();
        List<VinBlock> vins = this.getVin();
        if(vins == null || vins.size() == 0)
            return inputTxIds;
        for(VinBlock block : vins){
            if(block == null)
                continue;
            String txId = block.getTxid();
            if(txId == null)
                continue;
            inputTxIds.add(txId);
        }
        return inputTxIds;
    }
}
