package internal.rpc.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.math.BigDecimal;

@JsonInclude(Include.NON_NULL)
//@JsonIgnoreProperties(ignoreUnknown=true)
public class TransactionDetails {
	private String account;
	private String address;
	private String category;
	private BigDecimal amount;
	private BigDecimal fee;
	private int vout;

	public String getAccount() { return account; }
	public String getAddress() { return address; }
	public String getCategory() { return category; }
	public BigDecimal getAmount() { return amount; }
	public BigDecimal getFee() { return fee; }
	public int getVout() { return vout; }
}
