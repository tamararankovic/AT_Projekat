package chatmanager;

import java.util.List;

import javax.ejb.Remote;

import model.Message;
import model.User;

@Remote
public interface ChatManagerRemote {

	public boolean register(String username, String password);
	
	public boolean logIn(String username, String password);
	
	public boolean logOut(String username);
	
	public List<User> getRegistered();
	
	public List<User> getLoggedIn();
	
	public Message saveMessage(String sender, String receiver, String subject, String content);
	
	public List<Message> getMessages(String username);
	
	public boolean existsLoggedIn(String username);
	
	public boolean existsRegistered(String username);
	
	public void addMessage(Message message);
	
	public void addRegistered(User user);
	
	public void addLoggedIn(User user);
	
	public void removeLoggedIn(User user);
}
