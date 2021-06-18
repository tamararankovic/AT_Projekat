package agents;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import chatmanager.ChatManagerRemote;
import connectionmanager.ConnectionManagerBean;
import messagemanager.AgentMessage;
import messagemanager.AgentMessageType;
import model.User;
import websocket.WebSocket;

@Stateful
@Remote(Agent.class)
public class UserAgent implements Agent {

	private static final long serialVersionUID = 1L;
	
	private String agentId;
	
	@EJB private ChatManagerRemote chatManager;
	@EJB WebSocket ws;
	@EJB ConnectionManagerBean cm;

	@Override
	public void init(String agentId) {
		this.agentId = agentId;
	}

	@Override
	public void handleMessage(AgentMessage message) {
		System.out.println("entered agent");
		AgentMessageType type = message.getType();
		switch (type) {
			case LOG_IN: {
				String username = message.getAttributes().get("username");
				String password = message.getAttributes().get("password");
				logIn(username, password);
				break;
			}
			case REGISTER: {
				String username = message.getAttributes().get("username");
				String password = message.getAttributes().get("password");
				register(username, password);
				break;
			}
			case LOG_OUT: {
				String identifier = message.getAttributes().get("identifier");
				logOut(identifier);
				break;
			}
			case REGISTERED_LIST: {
				getRegistered();
				break;
			}
			case LOGGED_IN_LIST: {
				getLoggedIn();
				break;
			}
			case SEND_MESSAGE_ALL: {
				String subject = message.getAttributes().get("subject");
				String content = message.getAttributes().get("content");
				sendMessageToAll(subject, content);
				break;
			}
			case SEND_MESSAGE_USER: {
				String receiver = message.getAttributes().get("receiver");
				String subject = message.getAttributes().get("subject");
				String content = message.getAttributes().get("content");
				sendMessage(receiver, subject, content);
				break;
			}
			case GET_MESSAGES: {
				getAllMessages();
				break;
			}
		}
	}

	@Override
	public String getAgentId() {
		return agentId;
	}

	private void logIn(String username, String password) {
		String currentUsername = ws.getUsernameBoundToSession(agentId);
		if(currentUsername == null) {
			String identifier = UUID.randomUUID().toString();
			if (chatManager.logIn(new User(username, password), identifier)) {
				ws.bindUsernameToSession(username, agentId);
				ws.send(agentId, "login:OK " + identifier);
				ws.sendToAllLoggedIn(ws.getLoggedInListTextMessage(chatManager.getLoggedIn()));
				cm.postLoggedIn();
			} else {
				ws.send(agentId, "login:Logging in was unsuccessful! Incorrect username or password");
			}
		} else {
			ws.send(agentId, "login:You are already logged in!");
		}
	}
	
	private void logOut(String identifier) {
		User user = chatManager.getLoggedIn(identifier);
		if(user != null && user.getUsername().equals(ws.getUsernameBoundToSession(agentId)) && chatManager.logOut(identifier)) {
			ws.unbindUsernameFromSession(agentId);
			ws.send(agentId, "logout:OK");
			ws.sendToAllLoggedIn(ws.getLoggedInListTextMessage(chatManager.getLoggedIn()));
			cm.postLoggedIn();
		} else {
			ws.send(agentId, "logout:Logging out was unsuccessful!");
		}
	}
	
	private void register(String username, String password) {
		if(chatManager.register(new User(username, password))) {
			ws.send(agentId, "register:Registration was successful!");
			ws.sendToAllLoggedIn(ws.getRegisteredListTextMessage(chatManager.getRegistered()));
			cm.postRegistered();
		} else {
			ws.send(agentId, "register:Registration was unsuccessful! Try another username");
		}
	}
	
	private void getLoggedIn() {
		if(ws.getUsernameBoundToSession(agentId) != null)
			ws.send(agentId, ws.getLoggedInListTextMessage(chatManager.getLoggedIn()));
	}
	
	private void getRegistered() {
		if(ws.getUsernameBoundToSession(agentId) != null)
			ws.send(agentId, ws.getRegisteredListTextMessage(chatManager.getRegistered()));
	}
	
	private void getAllMessages() {
		String username = ws.getUsernameBoundToSession(agentId);
		if(username != null)
			ws.send(agentId, ws.getMessageListTextMessage(chatManager.getMessages(username), username));
	}
	
	private void sendMessage(String receiverUsername, String subject, String content) {
		String username = ws.getUsernameBoundToSession(agentId);
		if(chatManager.getRegistered(receiverUsername) != null && username != null) {
			User receiver = chatManager.getRegistered(receiverUsername);
			User sender = chatManager.getRegistered(username);
			model.Message message = new model.Message(sender, receiver, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), subject, content);
			chatManager.saveMessage(message);
			ws.sendToOneLoggedIn(receiverUsername, ws.getMessageTextMessage(message));
			ws.send(agentId, ws.getMessageListTextMessage(chatManager.getMessages(username), username));
			cm.postMessages();
		}
	}
	
	private void sendMessageToAll(String subject, String content) {
		List<User> loggedInUsers = chatManager.getLoggedIn();
		for(User user : loggedInUsers)
			sendMessage(user.getUsername(), subject, content);
	}
}
