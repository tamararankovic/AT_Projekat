package rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public interface MessageEndpoint {

	@POST
	@Path("/all/{sender}/{subject}/{content}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void sendToAll(@PathParam("sender") String sender, @PathParam("subject") String subject, @PathParam("content") String content);
	
	@POST
	@Path("/user/{sender}/{receiver}/{subject}/{content}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void sendToUser(@PathParam("sender") String sender, @PathParam("receiver") String receiver, @PathParam("subject") String subject, @PathParam("content") String content);
	
	@GET
	@Path("/{sender}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void getMessages(@PathParam("sender") String sender);
}
