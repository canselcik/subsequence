package internal.rpc.pojo;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true)
public class Transaction {
    private String txid;
	private BigDecimal fee;
	private BigDecimal amount;
	private long blockindex;
	private long confirmations;
	private long time;
	private long timereceived;
	private long block;
	private String hex;
	private String blockhash;
	private String otheraccount;
	private String comment;
	private String to;
	private long blocktime;
	private List<String> walletconflicts;
	private List<TransactionDetails> details;

	public String getOtheraccount() {
		return otheraccount;
	}
	public Transaction setOtheraccount(String otheraccount) {
		this.otheraccount = otheraccount;
		return this;
	}
	public String getComment() {
		return comment;
	}
	public Transaction setComment(String comment) {
		this.comment = comment;
		return this;
	}
	public BigDecimal getFee() {
		return fee;
	}
	public Transaction setFee(BigDecimal fee) {
		this.fee = fee;
		return this;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public Transaction setAmount(BigDecimal amount) {
		this.amount = amount;
		return this;
	}
	public long getBlockindex() {
		return blockindex;
	}
	public List<String> getWalletconflicts() {
		return walletconflicts;
	}
	public Transaction setWalletconflicts(List<String> walletconflicts) {
		this.walletconflicts = walletconflicts;
		return this;
	}
	public Transaction setBlockindex(long blockindex) {
		this.blockindex = blockindex;
		return this;
	}

	public String getCategory() {
		if(details == null || details.size() == 0)
			return null;
		String categoryString = null;
		for(TransactionDetails d : details){
			if(categoryString == null)
				categoryString = d.getCategory();
			else if(!categoryString.equals(d.getCategory()))
				return null;
		}
		return categoryString;
	}

	public long getConfirmations() {
		return confirmations;
	}
	public Transaction setConfirmations(long confirmations) {
		this.confirmations = confirmations;
		return this;
	}

	public String getAddress() {
		if(details == null || details.size() == 0)
			return null;
		String addrString = null;
		for(TransactionDetails d : details){
			if(addrString == null)
				addrString = d.getAddress();
			else if(!addrString.equals(d.getAddress()))
					return null;
		}
		return addrString;
	}

	public String getAccount() {
		if(details == null || details.size() == 0)
			return null;
		String accountString = null;
		for(TransactionDetails d : details){
			if(accountString == null)
				accountString = d.getAccount();
			else if(!accountString.equals(d.getAccount()))
				return null;
		}
		return accountString;
	}

	public String getTxid() {
		return txid;
	}
	public Transaction setTxid(String txid) {
		this.txid = txid;
		return this;
	}
	public long getBlock() {
		return block;
	}
	public Transaction setBlock(long block) {
		this.block = block;
		return this;
	}
	
	public String getHex() {
		return hex;
	}
	public Transaction setHex(String hex) {
		this.hex = hex;
		return this;
	}
	public String getBlockhash() {
		return blockhash;
	}
	public Transaction setBlockhash(String blockhash) {
		this.blockhash = blockhash;
		return this;
	}
	public List<TransactionDetails> getDetails() {
		return details;
	}
	public Transaction setDetails(List<TransactionDetails> details) {
		this.details = details;
		return this;
	}
	public long getTime() {
		return time;
	}
	public Transaction setTime(long time) {
		this.time = time;
		return this;
	}
	public long getTimereceived() {
		return timereceived;
	}
	public Transaction setTimereceived(long timereceived) {
		this.timereceived = timereceived;
		return this;
	}
	public long getBlocktime() {
		return blocktime;
	}
	public Transaction setBlocktime(long blocktime) {
		this.blocktime = blocktime;
		return this;
	}
	public String getTo() {
		return to;
	}
	public Transaction setTo(String to) {
		this.to = to;
		return this;
	}
    
    
}
