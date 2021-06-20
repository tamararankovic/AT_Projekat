package agents;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import messagemanager.ACLMessage;
import messagemanager.MessageManagerRemote;
import messagemanager.Performative;
import model.Match;
import util.JSONFileReader;

@Stateful
@Remote(Agent.class)
public class CollectorAgent extends BaseAgent {

	private static final long serialVersionUID = 1L;

	@EJB MessageManagerRemote msm;
	
	@Override
	public void handleMessage(ACLMessage message) {
		switch(message.getPerformative()) {
			case COLLECT: {
				String team1 = message.getUserArg("team1").toString();
				String team2 = message.getUserArg("team2").toString();
				List<Match> matches = getMatches(team1, team2);
				AID receiver = message.getReplyTo();
				reply(receiver, message.getSender(), matches);
				break;
			}
			default: return;
		}
	}

	private List<Match> getMatches(String team1, String team2) {
		return JSONFileReader.getMatchOutcomes().stream()
				.filter(m -> (m.getTeam1().equals(team1) || m.getTeam1().equals(team2))
						  && (m.getTeam2().equals(team1) || m.getTeam2().equals(team2)))
			.collect(Collectors.toList());
	}
	
	private void reply(AID receiver, AID replyTo, List<Match> matches) {
		ACLMessage message = new ACLMessage();
		message.setSender(aid);
		Set<AID> receivers = new HashSet<AID>();
		receivers.add(receiver);
		message.setReceivers(receivers);
		message.setPerformative(Performative.PREDICT);
		message.setReplyTo(replyTo);
		ObjectMapper mapper = new ObjectMapper();
		try {
			String content = mapper.writeValueAsString(matches);
			message.setContent(content);
			msm.post(message);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
