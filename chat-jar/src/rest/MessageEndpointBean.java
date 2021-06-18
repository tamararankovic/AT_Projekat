package rest;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import messagemanager.AgentMessage;
import messagemanager.AgentMessageType;
import messagemanager.MessageManagerRemote;

@Stateless
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/messages")
@LocalBean
@Remote(MessageEndpoint.class)
public class MessageEndpointBean implements MessageEndpoint {

	@EJB private MessageManagerRemote msm;
	
	@Override
	public void sendToAll(@PathParam("sender") String sender, @PathParam("subject") String subject, @PathParam("content") String content) {
		AgentMessage agentMessage = new AgentMessage(AgentMessageType.SEND_MESSAGE_ALL, sender);
		agentMessage.addAttribute("subject", subject);
		agentMessage.addAttribute("content", content);
		msm.post(agentMessage);
	}

	@Override
	public void sendToUser(@PathParam("sender") String sender, @PathParam("receiver") String receiver, @PathParam("subject") String subject, @PathParam("content") String content) {
		AgentMessage agentMessage = new AgentMessage(AgentMessageType.SEND_MESSAGE_USER, sender);
		agentMessage.addAttribute("receiver", receiver);
		agentMessage.addAttribute("subject", subject);
		agentMessage.addAttribute("content", content);
		msm.post(agentMessage);
	}

	@Override
	public void getMessages(String sender) {
		AgentMessage agentMessage = new AgentMessage(AgentMessageType.GET_MESSAGES, sender);
		msm.post(agentMessage);
	}

}
