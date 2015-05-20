package internal.rpc.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown=true)
public class ScriptSignature {
    private String asm;
    private String hex;
    public String getAsm() { return asm; }
    public String getHex() { return hex; }
}
