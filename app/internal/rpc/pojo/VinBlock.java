package internal.rpc.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown=true)
public class VinBlock {
    private String txid;
    private int vout;
    private ScriptSignature scriptSig;
    private long sequence;

    public String getTxid() { return txid; }
    public int getVout() { return vout; }
    public ScriptSignature getScriptSig() { return scriptSig; }
    public long getSequence() { return sequence; }
}