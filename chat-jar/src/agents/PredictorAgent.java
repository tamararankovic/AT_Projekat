package agents;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import util.LinearRegression;

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
					List<Match> matches = new ArrayList<Match>();
					for(List<Match> m : collectedMatches.values())
						matches.addAll(m);
					if(matches.size() >= 2) {
						String winner = predict(matches);
						reply(message.getReplyTo(), "The winner is " + winner);
					}
					else
						reply(message.getReplyTo(), "Not enough data to predict the outcome");
					collectedMatches = new HashMap<String, List<Match>>();
				}
				break;
			}
			default: return;
		}
	}
	
	private String predict(List<Match> matches) {
		List<Integer> x = new ArrayList<Integer>();
		List<Integer> y = new ArrayList<Integer>();
		String team1 = matches.get(0).getTeam1();
		String team2 = matches.get(0).getTeam2();
		for(Match match : matches) {
			Long days = ChronoUnit.DAYS.between(match.getDate(), LocalDateTime.now());
			x.add(days.intValue());
			if(match.getWinner().equals(team1))
				y.add(1);
			else
				y.add(0);
		}
		double prediction = LinearRegression.predictForValue(x, y, 0);
		return prediction > 0.5 ? team1 : team2;
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
