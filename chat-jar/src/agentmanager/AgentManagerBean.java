package agentmanager;

import java.util.HashSet;
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
import agents.CollectorAgent;
import agents.MasterAgent;
import agents.PredictorAgent;
import agents.UserAgent;
import agents.UserHelperAgent;
import rest.AgentEndpoint;
import util.AgentCenterRemote;
import util.JNDILookup;
import websocket.AgentSocket;
import websocket.AgentTypeSocket;

@Singleton
@Remote(AgentManagerRemote.class)
@LocalBean
public class AgentManagerBean implements AgentManagerRemote {
	
	Set<Agent> runningAgents = new HashSet<Agent>();
	Set<AID> otherNodeAgents = new HashSet<AID>();
	
	Set<AgentType> otherNodeTypes = new HashSet<AgentType>();
	
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
					instructNodesToUpdateAgents(agent.getAID().getHost().getAlias());
					updateViaSocket();
				}
			}
		}
		else if(otherNodeTypes.stream().anyMatch(t -> t.equals(type))) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					startAgentOnAnotherNode(type, name);
				}
			}).start();
		}
	}

	@Override
	public void stopAgent(AID aid) {
		boolean deleted = runningAgents.removeIf(a -> a.getAID().equals(aid));
		if(deleted) {
			instructNodesToUpdateAgents(aid.getHost().getAlias());
			updateViaSocket();
		}
		else if(otherNodeAgents.stream().anyMatch(a -> a.equals(aid))) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					stopAgentOnAnotherNode(aid);
				}
			}).start();
		}
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
		Set<AgentType> types = new HashSet<AgentType>(otherNodeTypes);
		types.addAll(getLocalAgentTypes());
		return types;
	}
	
	private Set<AgentType> getLocalAgentTypes() {
		Set<AgentType> types = new HashSet<AgentType>();
		types.add(new AgentType(UserAgent.class.getSimpleName(), JNDILookup.JNDIPathChat, acm.getHost().getAlias()));
		types.add(new AgentType(UserHelperAgent.class.getSimpleName(), JNDILookup.JNDIPathChat, acm.getHost().getAlias()));
		types.add(new AgentType(MasterAgent.class.getSimpleName(), JNDILookup.JNDIPathChat, acm.getHost().getAlias()));
		types.add(new AgentType(CollectorAgent.class.getSimpleName(), JNDILookup.JNDIPathChat, acm.getHost().getAlias()));
		types.add(new AgentType(PredictorAgent.class.getSimpleName(), JNDILookup.JNDIPathChat, acm.getHost().getAlias()));
		return types;
	}

	@Override
	public void updateAgentTypes(Set<AgentType> types, String nodeAlias) {
		otherNodeTypes.removeIf(t -> t.getHost().equals(nodeAlias));
		otherNodeTypes.addAll(types);
		updateViaTypeSocket();
	}

	@Override
	public void updateRunningAgents(Set<AID> agents, String nodeAlias) {
		otherNodeAgents.removeIf(a -> a.getHost().getAlias().equals(nodeAlias));
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
		otherNodeTypes.removeIf(t -> t.getHost().equals(nodeAlias));
		updateViaTypeSocket();
	}
	
	private void instructNodesToUpdateAgents(String nodeAlias) {
		for(String node : acm.getConnectedNodes()) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + node + "/chat-war/rest/agents");
			AgentEndpoint rest = rtarget.proxy(AgentEndpoint.class);
			rest.updateRunningAgents(runningAgents.stream().map(a -> a.getAID()).collect(Collectors.toSet()), nodeAlias);
			client.close();
		}
	}
	
	private void startAgentOnAnotherNode(AgentType type, String name) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + type.getHost() + "/chat-war/rest/agents");
		AgentEndpoint rest = rtarget.proxy(AgentEndpoint.class);
		rest.startAgent(type, name);
		client.close();
	}
	
	private void stopAgentOnAnotherNode(AID aid) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + aid.getHost().getAlias() + "/chat-war/rest/agents");
		AgentEndpoint rest = rtarget.proxy(AgentEndpoint.class);
		rest.stopAgent(aid);
		client.close();
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
			typeSocket.send(typesJSON);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
