package internal.rpc.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown=true)
public class ScriptPubKey {
    private String asm;
    private String hex;
    private int reqSigs;
    private String type;
    private List<String> addresses;

    public String getAsm(){ return asm; }
    public String getHex(){ return hex; }
    public int getReqSigs() { return reqSigs; }
    public String getType() { return type; }
    public List<String> getAddresses() { return addresses; }
}
