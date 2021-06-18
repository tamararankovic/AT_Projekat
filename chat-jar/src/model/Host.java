package model;

import java.io.Serializable;

public class Host implements Serializable {

	private static final long serialVersionUID = 1L;

	private String address;
	private String alias;
	private String masterAlias;
	
	public Host() {
		
	}
	
	public Host(String address, String alias, String masterAlias) {
		super();
		this.address = address;
		this.alias = alias;
		this.masterAlias = masterAlias;
	}
	
	public String getAddress() {
		return address;
	}
	public String getAlias() {
		return alias;
	}
	public String getMasterAlias() {
		return masterAlias;
	}
	
	public boolean isMaster() {
		return masterAlias == null || masterAlias.equals("");
	}

	@Override
	public boolean equals(Object obj) {
		return this.alias.equals(((Host)obj).alias);
	}
}
