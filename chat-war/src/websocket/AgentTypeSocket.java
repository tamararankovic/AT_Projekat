package websocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/type")
@Singleton
@LocalBean
public class AgentTypeSocket {

	private Set<Session> sessions = new HashSet<Session>();
	
	@OnOpen
	public void onOpen(Session session) {
		sessions.add(session);
	}
	
	@OnClose
	public void onClose(Session session) {
		sessions.remove(session);
	}
	
	public void send(String message) {
		try {
			for(Session session : sessions)
				session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
