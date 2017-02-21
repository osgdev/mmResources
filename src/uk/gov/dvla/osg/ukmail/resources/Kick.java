package uk.gov.dvla.osg.ukmail.resources;

public class Kick {
	private String jid;
	private Integer pid;
	
	public String print(){
		String str = String.format("%-10s%06d",this.jid, this.pid);
		return str;
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
	
}
