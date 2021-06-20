package rest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

import agentmanager.AgentManagerRemote;
import agents.AID;
import agents.AgentType;
import agents.CollectorAgent;
import agents.MasterAgent;
import agents.PredictorAgent;
import connectionmanager.AgentCenter;
import messagemanager.ACLMessage;
import messagemanager.MessageManagerRemote;
import messagemanager.Performative;
import util.AgentCenterRemote;
import util.JNDILookup;

@Stateless
@Path("/match")
@Remote(MatchEndpoint.class)
public class MatchEndpointBean implements MatchEndpoint {

	@EJB AgentManagerRemote agm;
	@EJB AgentCenterRemote acm;
	@EJB MessageManagerRemote msm;
	
	@Override
	public void predict(String team1, String team2) {
		AID master = startMasterAgent();
		AID predictor = startPredictorAgent();
		Set<AID> collectors = startCollectorAgents();
		
		ACLMessage message = new ACLMessage();
		message.setSender(master);
		message.setReceivers(collectors);
		message.setReplyTo(predictor);
		message.setPerformative(Performative.COLLECT);
		HashMap<String, Object> userArgs = new HashMap<String, Object>();
		userArgs.put("team1", team1);
		userArgs.put("team2", team2);
		message.setUserArgs(userArgs);
		msm.post(message);
	}
	
	private AID startMasterAgent() {
		AgentType type = new AgentType(MasterAgent.class.getSimpleName(), JNDILookup.JNDIPathChat, acm.getHost().getAlias());
		agm.startAgent(type, "master");
		return new AID("master", acm.getHost(), type);
	}
	
	private AID startPredictorAgent() {
		AgentType type = new AgentType(PredictorAgent.class.getSimpleName(), JNDILookup.JNDIPathChat, acm.getHost().getAlias());
		agm.startAgent(type, "predictor");
		return new AID("predictor", acm.getHost(), type);
	}
	
	private Set<AID> startCollectorAgents() {
		Set<AID> result = new HashSet<AID>();
		result.add(startCollectorAgent(acm.getHost()));
		for(String node : acm.getConnectedNodes())
			result.add(startCollectorAgent(new AgentCenter("", node)));
		return result;
	}
	
	private AID startCollectorAgent(AgentCenter host) {
		AgentType type = new AgentType(CollectorAgent.class.getSimpleName(), JNDILookup.JNDIPathChat, host.getAlias());
		agm.startAgent(type, "collector");
		return new AID("collector", host, type);
	}
}
