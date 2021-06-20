package agents;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import chatmanager.ChatManagerRemote;
import messagemanager.ACLMessage;
import messagemanager.MessageManagerRemote;
import messagemanager.Performative;
import model.Message;
import model.User;

@Stateful
@Remote(Agent.class)
public class UserHelperAgent extends BaseAgent {

	private static final long serialVersionUID = 1L;
	
	@EJB ChatManagerRemote chm;
	@EJB MessageManagerRemote msm;

	@Override
	public void handleMessage(ACLMessage message) {
		switch(message.getPerformative()) {
			case ADD_MESSAGE: {
				addMessage(message);
				report(message.getSender());
				break;
			}
			case ADD_REGISTERED: {
				addRegistered(message);
				report(message.getSender());
				break;
			}
			case ADD_LOGGED_IN: {
				addLoggedIn(message);
				report(message.getSender());
				break;
			}
			case REMOVE_LOGGED_IN: {
				removeLoggedIn(message);
				report(message.getSender());
				break;
			}
			default: return;
		}
	}
	
	private void addMessage(ACLMessage message) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Message msg = mapper.readValue(message.getContent(), Message.class);
			if(msg != null)
				chm.addMessage(msg);;
		} catch (JsonProcessingException e) { }
	}
	
	private void addRegistered(ACLMessage message) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			User user = mapper.readValue(message.getContent(), User.class);
			if(user != null)
				chm.addRegistered(user);
		} catch (JsonProcessingException e) { }
	}
	
	private void addLoggedIn(ACLMessage message) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			User user = mapper.readValue(message.getContent(), User.class);
			if(user != null)
				chm.addLoggedIn(user);
		} catch (JsonProcessingException e) { }
	}
	
	private void removeLoggedIn(ACLMessage message) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			User user = mapper.readValue(message.getContent(), User.class);
			if(user != null)
				chm.removeLoggedIn(user);
		} catch (JsonProcessingException e) { }
	}
	
	private void report(AID receiver) {
		ACLMessage message = new ACLMessage();
		message.setSender(aid);
		Set<AID> receivers = new HashSet<AID>();
		receivers.add(receiver);
		message.setReceivers(receivers);
		message.setPerformative(Performative.PERFORMED);
		message.setContentObj(new User("zoka", "zoka"));
		msm.post(message);
	}

}
