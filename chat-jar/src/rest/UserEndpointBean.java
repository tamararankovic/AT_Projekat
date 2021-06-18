package rest;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import messagemanager.AgentMessage;
import messagemanager.AgentMessageType;
import messagemanager.MessageManagerRemote;
import model.User;

@Stateless
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/users")
@LocalBean
@Remote(UserEndpoint.class)
public class UserEndpointBean implements UserEndpoint {

	@EJB private MessageManagerRemote msm;
	
	@Override
	public void logIn(User user, String sender) {
		AgentMessage message = new AgentMessage(AgentMessageType.LOG_IN, sender);
		message.addAttribute("username", user.getUsername());
		message.addAttribute("password", user.getPassword());
		msm.post(message);
	}

	@Override
	public void register(User user, String sender) {
		AgentMessage message = new AgentMessage(AgentMessageType.REGISTER, sender);
		message.addAttribute("username", user.getUsername());
		message.addAttribute("password", user.getPassword());
		msm.post(message);
	}

	@Override
	public void logOut(String sender, String identifier) {
		AgentMessage message = new AgentMessage(AgentMessageType.LOG_OUT, sender);
		message.addAttribute("identifier", identifier);
		msm.post(message);
	}

	@Override
	public void getLoggedInUsers(String sender) {
		AgentMessage message = new AgentMessage(AgentMessageType.LOGGED_IN_LIST, sender);
		msm.post(message);
	}

	@Override
	public void getRegisteredUsers(String sender) {
		AgentMessage message = new AgentMessage(AgentMessageType.REGISTERED_LIST, sender);
		msm.post(message);
	}

}
