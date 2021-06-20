package agents;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import agentmanager.AgentManagerRemote;
import chatmanager.ChatManagerRemote;
import connectionmanager.AgentCenter;
import messagemanager.ACLMessage;
import messagemanager.MessageManagerRemote;
import messagemanager.Performative;
import model.Message;
import model.User;
import util.AgentCenterRemote;
import websocket.Logger;

@Stateful
@Remote(Agent.class)
public class UserAgent extends BaseAgent {

	private static final long serialVersionUID = 1L;
	
	@EJB ChatManagerRemote chm;
	@EJB AgentManagerRemote agm;
	@EJB AgentCenterRemote acm;
	@EJB MessageManagerRemote msm;
	@EJB Logger logger;
	
	@Override
	public void handleMessage(ACLMessage message) {
		switch(message.getPerformative()) {
		case LOG_IN: {
			String username = message.getUserArg("username").toString();
			String password = message.getUserArg("password").toString();
			logIn(username, password);
			break;
		}
		case REGISTER: {
			String username = message.getUserArg("username").toString();
			String password = message.getUserArg("password").toString();
			register(username, password);
			break;
		}
		case LOG_OUT: {
			String username = message.getUserArg("username").toString();
			logOut(username);
			break;
		}
		case REGISTERED_LIST: {
			String username = message.getUserArg("username").toString();
			getRegistered(username);
			break;
		}
		case LOGGED_IN_LIST: {
			String username = message.getUserArg("username").toString();
			getLoggedIn(username);
			break;
		}
		case SEND_MESSAGE_ALL: {
			String subject = message.getUserArg("subject").toString();
			String content = message.getUserArg("content").toString();
			String sender = message.getUserArg("sender").toString();
			sendMessageToAll(sender, subject, content);
			break;
		}
		case SEND_MESSAGE_USER: {
			String subject = message.getUserArg("subject").toString();
			String content = message.getUserArg("content").toString();
			String sender = message.getUserArg("sender").toString();
			String receiver = message.getUserArg("receiver").toString();
			sendMessage(sender, receiver, subject, content);
			break;
		}
		case GET_MESSAGES: {
			String username = message.getUserArg("username").toString();
			getAllMessages(username);
			break;
		}
		case PERFORMED: {
			AID agentToStop = message.getSender();
			agm.stopAgent(agentToStop);
			break;
		}
		default: return;
		}
	}
	
	private void logIn(String username, String password) {
		boolean success = chm.logIn(username, password);
		if(success) {
			logger.send("User with username " + username + " successfully logged in");
			informHelperAgents(Performative.ADD_LOGGED_IN, new User(username, password));
		}
		else
			logger.send("User with username " + username + " doesn't exist or the password is incorrect");
	}
	
	private void register(String username, String password) {
		boolean success = chm.register(username, password);
		if(success) {
			logger.send("User with username " + username + " successfully registered");
			informHelperAgents(Performative.ADD_REGISTERED, new User(username, password));
		}
		else
			logger.send("User with username " + username + " already exists");
	}
	
	private void logOut(String username) {
		if(loggedIn(username)) {
			chm.logOut(username);
			logger.send("User with username " + username + " successfully logged out");
			informHelperAgents(Performative.REMOVE_LOGGED_IN, new User(username, ""));
		}
	}
	
	private void getLoggedIn(String username) {
		if(loggedIn(username)) {
			List<User> users = chm.getLoggedIn();
			logger.send("Logged in users: " + users);
		}
	}
	
	private void getRegistered(String username) {
		if(loggedIn(username)) {
			List<User> users = chm.getRegistered();
			logger.send("Registered users: " + users);
		}
	}
	
	private void sendMessage(String sender, String receiver, String subject, String content) {
		if(loggedIn(sender)) {
			if(!chm.existsRegistered(receiver)) {
				logger.send("Reciever with username " + receiver + " doesn't exist");
				return;
			}
			Message message = chm.saveMessage(sender, receiver, subject, content);
			informHelperAgents(Performative.ADD_MESSAGE, message);
			logger.send("Message: " + message + " sent");
		}
	}
	
	private void sendMessageToAll(String sender, String subject, String content) {
		if(loggedIn(sender))
			for(User user : chm.getLoggedIn())
				sendMessage(sender, user.getUsername(), subject, content);
	}
	
	private void getAllMessages(String username) {
		if(loggedIn(username)) {
			List<Message> messages = chm.getMessages(username);
			logger.send("Messages for user with username " + username + ": " + messages);
		}
	}
	
	private boolean loggedIn(String username) {
		if(!chm.existsLoggedIn(username)) {
			logger.send("User with username " + username + " is not logged in");
			return false;
		}
		return true;
	}
	
	private void informHelperAgents(Performative performative, Object obj) {
		ACLMessage message = new ACLMessage();
		message.setSender(aid);
		message.setPerformative(performative);
		message.setContent(obj.toString());
		message.setReceivers(getHelperAgents());
		msm.post(message);
	}
	
	private Set<AID> getHelperAgents() {
		Set<AID> agents = new HashSet<AID>();
		for(String node : acm.getConnectedNodes()) {
			AgentType type = getAgentType(node, UserHelperAgent.class.getSimpleName());
			if(type != null) {
				agm.startAgent(type, "helper");
				AgentCenter center = new AgentCenter("", node);
				AID aid = new AID("helper", center, type);
				agents.add(aid);
			}
		}
		return agents;
	}
	
	private AgentType getAgentType(String node, String name) {
		return agm.getAgentTypes().stream()
				.filter(t -> t.getName().equals(UserHelperAgent.class.getSimpleName())
						&& t.getHost().equals(node)).findFirst().orElse(null);
	}
}
