package agentmanager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Singleton;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import agents.AID;
import agents.Agent;
import agents.AgentType;
import agents.UserAgent;
import rest.AgentEndpoint;
import util.AgentCenterRemote;
import util.JNDILookup;
import websocket.AgentSocket;
import websocket.AgentTypeSocket;

@Singleton
@Remote(AgentManagerRemote.class)
@LocalBean
public class AgentManagerBean implements AgentManagerRemote {

	private static final long serialVersionUID = 1L;
	
	Set<Agent> runningAgents = new HashSet<Agent>();
	Set<AID> otherNodeAgents = new HashSet<AID>();
	
	Map<String, Set<AgentType>> otherNodeTypes = new HashMap<String, Set<AgentType>>();
	
	@EJB AgentCenterRemote acm;
	@EJB AgentSocket agentSocket;
	@EJB AgentTypeSocket typeSocket;
	
	@Override
	public void startAgent(AgentType type, String name) {
		if(getLocalAgentTypes().stream().anyMatch(t -> t.equals(type))) {
			Agent agent = (Agent) JNDILookup.lookUp(type.getModule() + type.getName() + "!"
									+ Agent.class.getName() + "?stateful", Agent.class);
			if(agent != null) {
				agent.init(new AID(name, acm.getHost(), type));
				if(runningAgents.stream().noneMatch(a -> a.getAID().equals(agent.getAID()))) {
					runningAgents.add(agent);
					instructNodesToUpdateAgents();
					updateViaSocket();
				}
			}
		}
	}

	@Override
	public void stopAgent(AID aid) {
		runningAgents.removeIf(a -> a.getAID().equals(aid));
		instructNodesToUpdateAgents();
		updateViaSocket();
	}

	@Override
	public Agent getRunningAgentByAID(AID aid) {
		return runningAgents.stream().filter(a -> a.getAID().equals(aid)).findFirst().orElse(null);
	}
	
	@Override
	public Set<AID> getRunningAgents() {
		Set<AID> agents = runningAgents.stream().map(a -> a.getAID()).collect(Collectors.toSet());
		agents.addAll(otherNodeAgents);
		return agents;
	}

	@Override
	public Set<AgentType> getAgentTypes() {
		Set<AgentType> types = getLocalAgentTypes();
		for(Set<AgentType> otherTypes : otherNodeTypes.values())
			types.addAll(otherTypes);
		return types;
	}
	
	private Set<AgentType> getLocalAgentTypes() {
		Set<AgentType> types = new HashSet<AgentType>();
		types.add(new AgentType(UserAgent.class.getSimpleName(), JNDILookup.JNDIPathChat));
		return types;
	}

	@Override
	public void updateAgentTypes(Set<AgentType> types, String nodeAlias) {
		otherNodeTypes.put(nodeAlias, types);
		updateViaTypeSocket();
	}

	@Override
	public void updateRunningAgents(Set<AID> agents) {
		otherNodeAgents.addAll(agents);
		updateViaSocket();
	}
	
	@Override
	public void deleteRunningAgents(String nodeAlias) {
		otherNodeAgents.removeIf(a -> a.getHost().getAlias().equals(nodeAlias));
		updateViaSocket();
	}
	
	@Override
	public void deleteAgentTypes(String nodeAlias) {
		otherNodeTypes.remove(nodeAlias);
		updateViaTypeSocket();
	}
	
	private void instructNodesToUpdateAgents() {
		for(String node : acm.getConnectedNodes()) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + node + "/chat-war/rest/agents");
			AgentEndpoint rest = rtarget.proxy(AgentEndpoint.class);
			rest.updateRunningAgents(runningAgents.stream().map(a -> a.getAID()).collect(Collectors.toSet()));
			client.close();
		}
	}
	
	private void updateViaSocket() {
	    try {
	    	Set<AID> agents = getRunningAgents();
			ObjectMapper mapper = new ObjectMapper();
			String agentsJSON = mapper.writeValueAsString(agents);
			agentSocket.send(agentsJSON);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
	
	private void updateViaTypeSocket() {
		try {
	    	Set<AgentType> types = getAgentTypes();
			ObjectMapper mapper = new ObjectMapper();
			String typesJSON = mapper.writeValueAsString(types);
			agentSocket.send(typesJSON);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
