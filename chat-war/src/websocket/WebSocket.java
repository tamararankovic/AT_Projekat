package websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import agentmanager.AgentManagerBean;
import agentmanager.AgentManagerRemote;
import chatmanager.ChatManagerBean;
import chatmanager.ChatManagerRemote;
import model.User;
import util.JNDILookup;

@ServerEndpoint("/ws/{identifier}")
@Singleton
@LocalBean
public class WebSocket {

	private Map<Session, String> sessions = new HashMap<Session, String>();
	
	protected AgentManagerRemote agm() {
		return JNDILookup.lookUp(JNDILookup.AgentManagerLookup, AgentManagerBean.class);
	}
	
	protected ChatManagerRemote chm() {
		return JNDILookup.lookUp(JNDILookup.ChatManagerLookup, ChatManagerBean.class);
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig c, @PathParam("identifier") String identifier) {
		System.out.println("usao u websocket");
		if(!sessions.keySet().contains(session)) {
			agm().startAgent(session.getId(), JNDILookup.UserAgentLookup);
			User user = chm().getLoggedIn(identifier);
			sessions.put(session, user != null ? user.getUsername() : null);
			System.out.println("Session with id: " + session.getId() + " opened");
			try {
				session.getBasicRemote().sendText("sessionId:"+session.getId());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@OnClose
	public void onClose(Session session) {
		if(agm() != null) {
			agm().stopAgent(session.getId());
		}
		sessions.remove(session);
		System.out.println("Session with id: " + session.getId() + " closed");
	}
	
	public void send(String sessionId, String message) {
		Session session = sessions.keySet().stream().filter(s -> s.getId().equals(sessionId)).findFirst().orElse(null);
		if(session != null && session.isOpen()) {
			try {
				session.getBasicRemote().sendText(message);
			} catch (IOException e) {
				try {
					session.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}
	
	public void sendToOneLoggedIn(String username, String message) {
		for(Session session : sessions.keySet())
			if(sessions.get(session) != null && sessions.get(session).equals(username) && session.isOpen()) {
				try {
					session.getBasicRemote().sendText(message);
				} catch (IOException e) {
					try {
						session.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
	}
	
	public void sendToAllLoggedIn(String message) {
		for(Session session : sessions.keySet())
			if(sessions.get(session) != null && session.isOpen()) {
				try {
					session.getBasicRemote().sendText(message);
				} catch (IOException e) {
					try {
						session.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
	}
	
	@OnError
	public void onError(Session session, Throwable t) {
		agm().stopAgent(session.getId());
		sessions.remove(session);
	}
	
	public void bindUsernameToSession(String username, String sessionId) {
		Session session = sessions.keySet().stream().filter(s -> s.getId().equals(sessionId)).findFirst().orElse(null);
		if (session != null)
			sessions.put(session, username);
	}
	
	public void unbindUsernameFromSession(String sessionId) {
		Session session = sessions.keySet().stream().filter(s -> s.getId().equals(sessionId)).findFirst().orElse(null);
		if (session != null)
			sessions.put(session, null);
	}
	
	public String getUsernameBoundToSession(String sessionId) {
		Session session = sessions.keySet().stream().filter(s -> s.getId().equals(sessionId)).findFirst().orElse(null);
		if (session != null)
			return sessions.get(session);
		else
			return null;
	}
	
	public String getLoggedInListTextMessage(List<User> users) {
		StringBuilder loggedInList = new StringBuilder();
		loggedInList.append("loggedInList:");
		for(User u : users) {
			loggedInList.append(u.getUsername());
			loggedInList.append(",");
		}
		return loggedInList.toString().substring(0, loggedInList.length()-1);
	}
	
	public String getRegisteredListTextMessage(List<User> users) {
		StringBuilder registeredList = new StringBuilder();
		registeredList.append("registeredList:");
		for(User u : users) {
			registeredList.append(u.getUsername());
			registeredList.append(",");
		}
		return registeredList.toString().substring(0, registeredList.length()-1);
	}
	
	public String getMessageListTextMessage(List<model.Message> messages, String username) {
		StringBuilder messageList = new StringBuilder();
		messageList.append("messageList:");
		messageList.append("[");
		for(model.Message m : messages) {
			String otherUsername = m.getSender().getUsername();
			boolean incoming = true;
			if(username.equals(otherUsername)) {
				otherUsername = m.getReceiver().getUsername();
				incoming = false;
			}
			messageList.append("{");
			messageList.append("\"otherUsername\":\"");
			messageList.append(otherUsername);
			messageList.append("\", \"incoming\":\"");
			messageList.append(incoming);
			messageList.append("\", \"subject\":\"");
			messageList.append(m.getSubject());
			messageList.append("\", \"content\":\"");
			messageList.append(m.getContent());
			messageList.append("\", \"dateTime\":\"");
			messageList.append(m.getCreated());
			messageList.append("\" }, ");
		}
		if(messageList.length() > 13)
			messageList.deleteCharAt(messageList.lastIndexOf(","));
		messageList.append("]");
		return messageList.toString();
	}
	
	public String getMessageTextMessage(model.Message message) {
		StringBuilder messageText = new StringBuilder();
		messageText.append("message:");
		messageText.append("{");
		messageText.append("\"otherUsername\":\"");
		messageText.append(message.getSender().getUsername());
		messageText.append("\", \"incoming\":\"");
		messageText.append(true);
		messageText.append("\", \"subject\":\"");
		messageText.append(message.getSubject());
		messageText.append("\", \"content\":\"");
		messageText.append(message.getContent());
		messageText.append("\", \"dateTime\":\"");
		messageText.append(message.getCreated());
		messageText.append("\" }");
		return messageText.toString();
	}
}
