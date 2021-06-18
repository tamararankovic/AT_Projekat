package agentmanager;

import java.io.Serializable;
import java.util.Set;

import agents.AID;
import agents.Agent;
import agents.AgentType;

public interface AgentManagerRemote extends Serializable {

	public void startAgent(AgentType type, String name);
	
	public void stopAgent(AID aid);
	
	public Set<AID> getRunningAgents();
	
	public Set<AgentType> getAgentTypes();
	
	public Agent getRunningAgentByAID(AID aid);
}
