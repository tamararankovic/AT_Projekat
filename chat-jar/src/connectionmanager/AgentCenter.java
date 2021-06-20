package connectionmanager;

import java.io.Serializable;

public class AgentCenter implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String address;
	private String alias;
	
	public AgentCenter() { }
	
	public AgentCenter(String address, String alias) {
		super();
		this.address = address;
		this.alias = alias;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public boolean equals(Object obj) {
		AgentCenter center = (AgentCenter)obj;
		return center.alias.equals(alias);
	}
	
}
