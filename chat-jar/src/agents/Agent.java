package agents;

import java.io.Serializable;

import javax.ejb.Remote;

import messagemanager.AgentMessage;

@Remote
public interface Agent extends Serializable {

	public void init(String agentId);
	
	public void handleMessage(AgentMessage message);
	
	public String getAgentId();
}
