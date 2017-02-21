package uk.gov.dvla.osg.ukmail.resources;

public class UkMailManifest {
private String msc;
private Integer trayVol;
private Integer trayWeight;
private String format;
private String machinable;
private String serviceCode;
private String accountNo;
private String mailingId;
private String appName;
private String runNo;
private String runDate;
private Integer itemId;
private String customerRef;
private Integer firstPieceId;
private Integer lastPieceId;
private String altRef;
private String collectionDate;
private String batchId;
private String jid;
private String manifestFilename;

	public String print(boolean mailmark){
		String str = "";
		if(mailmark){
			str = String.format("%-10.10s%-5.5s%-6.6s%-1.1s%-1.1s%-3.3s%-10.10s%-20.20s%-20.20s%-20.20s%-8.8s%-20.20s",
					this.msc,this.trayVol,this.trayWeight,this.format,this.machinable,this.serviceCode,
					this.accountNo,this.mailingId,this.customerRef,this.altRef,this.collectionDate,this.batchId);
		}else{
			str = String.format("%-10.10s%-5.5s%-6.6s%-1.1s%-1.1s%-3.3s%-10.10s%-20.20s%-20.20s%-20.20s%-8.8s%-20.20s",
					this.msc,this.trayVol,this.trayWeight,this.format,this.machinable,this.serviceCode,
					this.accountNo,this.mailingId,this.customerRef,"",this.collectionDate,this.batchId);
		}
		return str;
	}

	public String getMsc() {
		return msc;
	}

	public void setMsc(String msc) {
		this.msc = msc;
	}

	public Integer getTrayVol() {
		return trayVol;
	}

	public void setTrayVol(Integer trayVol) {
		this.trayVol = trayVol;
	}

	public Integer getTrayWeight() {
		return trayWeight;
	}

	public void setTrayWeight(Integer trayWeight) {
		this.trayWeight = trayWeight;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getMachinable() {
		return machinable;
	}

	public void setMachinable(String machinable) {
		this.machinable = machinable;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getAccountNo() {
		return accountNo;
	}

	public void setAccountNo(String accountNo) {
		this.accountNo = accountNo;
		setAltRef();
	}

	public String getMailingId() {
		return mailingId;
	}

	public void setMailingId() {
		this.mailingId = this.appName + this.runNo;
	}

	public String getCustomerRef() {
		return customerRef;
	}

	public void setCustomerRef() {
		if(this.firstPieceId != null & this.lastPieceId != null & this.jid != null){
			this.customerRef = String.format("%-6s / %-6s /%-3s",
			String.format("%06d", this.firstPieceId), 
			String.format("%06d", this.lastPieceId),
			this.jid.substring(7, 10));
		}
	}

	public String getAltRef() {
		return altRef;
	}

	public void setAltRef() {
		if(this.accountNo != null && this.runDate != null && this.itemId != null){
			this.altRef = this.accountNo + "_" + this.runDate + "_" + String.format("%05d",this.itemId);
		}
	}

	public String getCollectionDate() {
		return collectionDate;
	}

	public void setCollectionDate(String collectionDate) {
		this.collectionDate = collectionDate;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId() {
		if(this.appName !=null && this.runNo != null && this.jid != null){
			this.batchId = String.format("%-4.4s %-5.5s%-10.10s", this.appName, this.runNo, this.jid);
		}
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
		setMailingId();
		setBatchId();
	}

	public String getRunNo() {
		return runNo;
	}

	public void setRunNo(String runNo) {
		this.runNo = runNo;
		setMailingId();
		setBatchId();
	}

	public Integer getFirstPieceId() {
		return firstPieceId;
	}

	public void setFirstPieceId(Integer firstPieceId) {
		this.firstPieceId = firstPieceId;
		setCustomerRef();
	}

	public Integer getLastPieceId() {
		return lastPieceId;
	}

	public void setLastPieceId(Integer lastPieceId) {
		this.lastPieceId = lastPieceId;
		setCustomerRef();
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
		setCustomerRef();
		setBatchId();
	}

	public String getRunDate() {
		return runDate;
	}

	public void setRunDate(String runDate) {
		this.runDate = runDate;
		setAltRef();
	}

	public Integer getItemId() {
		return itemId;
	}

	public void setItemId(Integer itemId) {
		this.itemId = itemId;
		setAltRef();
	}

	public void setMailingId(String mailingId) {
		this.mailingId = mailingId;
	}

	public void setCustomerRef(String customerRef) {
		this.customerRef = customerRef;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getManifestFilename() {
		return manifestFilename;
	}

	public void setManifestFilename(String manifestFilename) {
		this.manifestFilename = manifestFilename;
	}
}
