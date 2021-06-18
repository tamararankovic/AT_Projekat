package connectionmanager;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import model.Message;
import model.User;

public interface DataSync {

	@POST
	@Path("/loggedIn/{alias}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void syncLoggedIn(@PathParam("alias") String alias, List<User> users);
	
	@POST
	@Path("/registered")
	@Consumes(MediaType.APPLICATION_JSON)
	public void syncRegistered(List<User> users);
	
	@POST
	@Path("/messages")
	@Consumes(MediaType.APPLICATION_JSON)
	public void syncMessages(List<Message> messages);
}
