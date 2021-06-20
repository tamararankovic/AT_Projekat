package agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import com.fasterxml.jackson.databind.ObjectMapper;

import messagemanager.ACLMessage;
import messagemanager.MessageManagerRemote;
import messagemanager.Performative;
import model.Match;
import util.AgentCenterRemote;

@Stateful
@Remote(Agent.class)
public class PredictorAgent extends BaseAgent {

	private static final long serialVersionUID = 1L;
	
	Map<String, List<Match>> collectedMatches = new HashMap<String, List<Match>>();
	
	@EJB AgentCenterRemote acm;
	@EJB MessageManagerRemote msm;

	@Override
	public void handleMessage(ACLMessage message) {
		switch(message.getPerformative()) {
			case PREDICT: {
				collectedMatches.put(message.getSender().getHost().getAlias(), getMatches(message.getContent()));
				if(collected()) {
					//TODO: implement prediction
					collectedMatches = new HashMap<String, List<Match>>();
					reply(message.getReplyTo(), "PREDICTION SUCCESSFUL!!");
				}
				break;
			}
			default: return;
		}
	}

	private List<Match> getMatches(String json) {
		List<Match> result = new ArrayList<Match>();

		ObjectMapper mapper = new ObjectMapper();
		try {
			Match[] matches = mapper.readValue(json, Match[].class);
			for(Match match : matches) {
				result.add(match);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private boolean collected() {
		List<String> connectedNodes = acm.getConnectedNodes();
		for(String nodeCollected : collectedMatches.keySet())
			connectedNodes.removeIf(n -> n.equals(nodeCollected));
		return connectedNodes.size() == 0 && collectedMatches.keySet().size() == acm.getConnectedNodes().size() + 1;	
	}
	
	private void reply(AID receiver, String content) {
		ACLMessage message = new ACLMessage();
		message.setSender(aid);
		Set<AID> receivers = new HashSet<AID>();
		receivers.add(receiver);
		message.setReceivers(receivers);
		message.setPerformative(Performative.DISPLAY);
		message.setContent(content);
		msm.post(message);
	}
}
