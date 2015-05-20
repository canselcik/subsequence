package internal.rpc.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.math.BigDecimal;

@JsonInclude(Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown=true)
public class VoutBlock {
    private BigDecimal value;
    private long n;
    private ScriptPubKey scriptPubKey;

    public BigDecimal getValue() { return value; }
    public long getN() { return n; }
    public ScriptPubKey getScriptPubKey() { return scriptPubKey; }
}