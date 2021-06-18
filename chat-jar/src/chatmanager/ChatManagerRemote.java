package chatmanager;

import java.util.List;

import javax.ejb.Remote;

import model.Message;
import model.User;

@Remote
public interface ChatManagerRemote {

	public boolean register(User user);
	
	public boolean logIn(User user, String identifier);
	
	public boolean logOut(String username);
	
	public List<User> getRegistered();
	
	public List<User> getLoggedIn();
	
	public List<User> getLocallyLoggedIn();
	
	public List<User> getLoggedInByHost(String host);
	
	public void deleteLoggedInByHost(String host);
	
	public void saveMessage(Message message);
	
	public List<Message> getMessages(String username);
	
	public List<Message> getMessages();
	
	public User getRegistered(String username);

	public User getLoggedIn(String identifier);
	
	public void syncLoggedIn(String alias, List<User> users);
	
	public void syncRegistered(List<User> users);
	
	public List<Message> syncMessages(List<Message> messages);
}
