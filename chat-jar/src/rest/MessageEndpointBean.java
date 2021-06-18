package rest;

import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ws.rs.Path;

import messagemanager.ACLMessage;
import messagemanager.MessageManagerRemote;

@Stateless
@Path("/messages")
@Remote(MessageEndpoint.class)
public class MessageEndpointBean implements MessageEndpoint {

	@EJB private MessageManagerRemote msm;
	
	@Override
	public void sendMessage(ACLMessage message) {
		msm.post(message);
	}

	@Override
	public Set<String> getPerformatives() {
		return msm.getPerformatives();
	}

}
