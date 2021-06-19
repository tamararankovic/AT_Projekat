package rest;

import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

import agentmanager.AgentManagerRemote;
import agents.AID;
import agents.AgentType;

@Stateless
@Path("/agents")
@Remote(AgentEndpoint.class)
public class AgentEndpointBean implements AgentEndpoint {

	@EJB AgentManagerRemote agm;
	
	@Override
	public Set<AgentType> getAllAgentTypes() {
		return agm.getAgentTypes();
	}

	@Override
	public Set<AID> getAllRunningAgents() {
		return agm.getRunningAgents();
	}

	@Override
	public void startAgent(AgentType type, String name) {
		agm.startAgent(type, name); 
	}

	@Override
	public void stopAgent(AID aid) {
		agm.stopAgent(aid);
	}

	@Override
	public void updateAgentTypes(Set<AgentType> types, String nodeAlias) {
		agm.updateAgentTypes(types, nodeAlias);
	}

	@Override
	public void updateRunningAgents(Set<AID> agents, String nodeAlias) {
		agm.updateRunningAgents(agents, nodeAlias);
	}

}
