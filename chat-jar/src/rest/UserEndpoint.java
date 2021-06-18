package rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import model.User;

public interface UserEndpoint {
	
	@POST
	@Path("/login/{sender}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void logIn(User user, @PathParam("sender") String sender);
	
	@POST
	@Path("/register/{sender}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void register(User user, @PathParam("sender") String sender);
	
	@DELETE
	@Path("/loggedIn/{sender}/{identifier}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void logOut(@PathParam("sender") String sender, @PathParam("identifier") String identifier);

	@GET 
	@Path("/loggedIn/{sender}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void getLoggedInUsers(@PathParam("sender") String sender);
	
	@GET 
	@Path("/registered/{sender}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void getRegisteredUsers(@PathParam("sender") String sender);

}
