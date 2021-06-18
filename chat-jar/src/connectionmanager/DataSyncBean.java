package connectionmanager;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

import chatmanager.ChatManagerRemote;
import model.Message;
import model.User;
import websocket.WebSocket;

@Stateless
@Remote(DataSync.class)
@Path("/sync")
public class DataSyncBean implements DataSync {

	@EJB private ChatManagerRemote chatManager;
	@EJB WebSocket ws;
	
	@Override
	public void syncLoggedIn(String alias, List<User> users) {
		System.out.println("sync logged in from alias: " + alias);
		for(User u : users)
			System.out.println(u.getUsername());
		chatManager.syncLoggedIn(alias, users);
		ws.sendToAllLoggedIn(ws.getLoggedInListTextMessage(chatManager.getLoggedIn()));
	}

	@Override
	public void syncRegistered(List<User> users) {
		System.out.println("sync registered:");
		for(User u : users)
			System.out.println(u.getUsername());
		chatManager.syncRegistered(users);
		ws.sendToAllLoggedIn(ws.getRegisteredListTextMessage(chatManager.getRegistered()));
	}

	@Override
	public void syncMessages(List<Message> messages) {
		System.out.println("sync messages: ");
		for(Message m : messages)
			System.out.println(m.getSender().getUsername() + " " + m.getReceiver().getUsername() + " " + m.getSubject() + " " + m.getContent());
		List<Message> newMessages = chatManager.syncMessages(messages);
		for(Message message : newMessages) {
			String sender = message.getSender().getUsername();
			String receiver = message.getReceiver().getUsername();
			ws.sendToOneLoggedIn(receiver, ws.getMessageTextMessage(message));
			ws.sendToOneLoggedIn(sender, ws.getMessageListTextMessage(chatManager.getMessages(sender), sender));
		}
	}

}
