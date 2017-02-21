package uk.gov.dvla.osg.ukmail.resources;

public class BarcodeLookup {
	private String jid;
	private Integer pid;
	private String itemNo;
	
	public String print(){
		return String.format("%10s%06d%8s", this.jid, this.pid, this.itemNo);
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
	public String getItemNo() {
		return itemNo;
	}
	public void setItemNo(String itemNo) {
		this.itemNo = itemNo;
	}
}
