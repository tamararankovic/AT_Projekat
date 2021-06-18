package agentmanager;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Singleton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import agents.AID;
import agents.Agent;
import agents.AgentType;
import agents.UserAgent;
import util.AgentCenterManager;
import util.JNDILookup;
import websocket.AgentSocket;

@Singleton
@Remote(AgentManagerRemote.class)
@LocalBean
public class AgentManagerBean implements AgentManagerRemote {

	private static final long serialVersionUID = 1L;
	
	Set<Agent> runningAgents = new HashSet<Agent>();
	
	@EJB AgentCenterManager acm;
	@EJB AgentSocket socket;
	
	@Override
	public void startAgent(AgentType type, String name) {
		if(getAgentTypes().stream().anyMatch(t -> t.equals(type))) {
			Agent agent = (Agent) JNDILookup.lookUp(type.getModule() + type.getName() + "!"
									+ Agent.class.getName() + "?stateful", Agent.class);
			if(agent != null) {
				agent.init(new AID(name, acm.getLocalNodeInfo(), type));
				if(runningAgents.stream().noneMatch(a -> a.getAID().equals(agent.getAID()))) {
					runningAgents.add(agent);
					updateViaSocket();
				}
			}
		}
	}

	@Override
	public void stopAgent(AID aid) {
		runningAgents.removeIf(a -> a.getAID().equals(aid));
		updateViaSocket();
	}

	@Override
	public Agent getRunningAgentByAID(AID aid) {
		return runningAgents.stream().filter(a -> a.getAID().equals(aid)).findFirst().orElse(null);
	}
	
	@Override
	public Set<AID> getRunningAgents() {
		return runningAgents.stream().map(a -> a.getAID()).collect(Collectors.toSet());
	}

	@Override
	public Set<AgentType> getAgentTypes() {
		Set<AgentType> types = new HashSet<AgentType>();
		types.add(new AgentType(UserAgent.class.getSimpleName(), JNDILookup.JNDIPathChat));
		return types;
	}
	
	private void updateViaSocket() {
	    try {
	    	Set<AID> agents = getRunningAgents();
			ObjectMapper mapper = new ObjectMapper();
			String agentsJSON = mapper.writeValueAsString(agents);
			socket.send(agentsJSON);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
