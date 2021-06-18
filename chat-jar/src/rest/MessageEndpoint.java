package rest;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import messagemanager.ACLMessage;

public interface MessageEndpoint {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void sendMessage(ACLMessage message);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Set<String> getPerformatives();
}
