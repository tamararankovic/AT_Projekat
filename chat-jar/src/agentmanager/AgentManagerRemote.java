package agentmanager;

import java.io.Serializable;
import java.util.List;

import agents.Agent;

public interface AgentManagerRemote extends Serializable {

	public void startAgent(String agentId, String name);
	
	public void stopAgent(String agentId);
	
	public List<Agent> getRunningAgents();
	
	public Agent getRunningAgentByid(String agentId);
}
