package agents;

import java.io.Serializable;

import connectionmanager.AgentCenter;

public class AID implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private AgentCenter host;
	private AgentType type;
	
	public AID() { }
	
	public AID(String name, AgentCenter host, AgentType type) {
		super();
		this.name = name;
		this.host = host;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public AgentCenter getHost() {
		return host;
	}
	public void setHost(AgentCenter host) {
		this.host = host;
	}
	public AgentType getType() {
		return type;
	}
	public void setType(AgentType type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		AID aid = (AID)obj;
		return aid.name.equals(name) && aid.host.equals(host) && aid.type.equals(type);
	}
	
	
}
