package agents;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

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
		Message msg = (Message)message.getContentObj();
		if(msg != null)
			chm.addMessage(msg);
	}
	
	private void addRegistered(ACLMessage message) {
		User user = (User)message.getContentObj();
		if(user != null)
			chm.addRegistered(user);
	}
	
	private void addLoggedIn(ACLMessage message) {
		User user = (User)message.getContentObj();
		if(user != null)
			chm.addLoggedIn(user);
	}
	
	private void removeLoggedIn(ACLMessage message) {
		User user = (User)message.getContentObj();
		if(user != null)
			chm.removeLoggedIn(user);
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
