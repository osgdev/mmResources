package uk.gov.dvla.osg.ukmail.resources;

public class SoapFileEntry {
	private String runNo;
	private String jid;
	private Integer pid;
	private String appName;
	private String batchRef;
	private String scid;
	private String clasz;
	private String dps;
	private String itemId;
	private String format;
	private String machineable;
	private String mailType;
	private Integer noOfAddressLines;
	private String postcode;
	private String product;
	private Integer weight;
	private String spare8;
	
	
	public String print(){
		return String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|", 
				this.appName, this.batchRef, this.scid, this.clasz, this.dps,
				this.itemId, this.format, this.machineable, this.mailType, this.noOfAddressLines, 
				this.postcode, this.product, this.spare8, this.weight, this.runNo, this.jid, this.pid);
	}
	public String getRunNo() {
		return runNo;
	}
	public void setRunNo(String runNo) {
		this.runNo = runNo;
	}
	public String getJid() {
		return jid;
	}
	public void setJid(String jid) {
		this.jid = jid;
	}
	public Integer getPid() {
		return pid;
	}
	public void setPid(Integer pid) {
		this.pid = pid;
	}
	public String getClasz() {
		return clasz;
	}
	public void setClasz(String clasz) {
		this.clasz = clasz;
	}
	public String getDps() {
		return dps;
	}
	public void setDps(String dps) {
		this.dps = dps;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getMachineable() {
		return machineable;
	}
	public void setMachineable(String machineable) {
		this.machineable = machineable;
	}
	public String getMailType() {
		return mailType;
	}
	public void setMailType(String mailType) {
		this.mailType = mailType;
	}
	public Integer getNoOfAddressLines() {
		return noOfAddressLines;
	}
	public void setNoOfAddressLines(Integer noOfAddressLines) {
		this.noOfAddressLines = noOfAddressLines;
	}
	public String getPostcode() {
		return postcode;
	}
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public Integer getWeight() {
		return weight;
	}
	public void setWeight(Integer weight) {
		this.weight = weight;
	}
	public String getSpare8() {
		return spare8;
	}
	public void setSpare8(String spare8) {
		this.spare8 = spare8;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getBatchRef() {
		return batchRef;
	}
	public void setBatchRef(String batchRef) {
		this.batchRef = batchRef;
	}
	public String getScid() {
		return scid;
	}
	public void setScid(String scid) {
		this.scid = scid;
	}
}
