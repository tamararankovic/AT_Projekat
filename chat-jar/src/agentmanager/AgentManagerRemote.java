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
	
	public void updateAgentTypes(Set<AgentType> types, String nodeAlias);
	
	public void updateRunningAgents(Set<AID> agents, String nodeAlias);
	
	public void deleteAgentTypes(String nodeAlias);
	
	public void deleteRunningAgents(String nodeAlias);
}
