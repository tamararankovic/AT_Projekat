package connectionmanager;

public class AgentCenter {

	private String address;
	private String alias;
	
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
		return center.address.equals(address) && center.alias.equals(alias);
	}
	
	
}
