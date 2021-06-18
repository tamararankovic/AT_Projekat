package connectionmanager;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public interface ConnectionManager {

	@POST
	@Path("/register")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<String> registerNode(String nodeAlias);
	
	@POST
	@Path("/node")
	@Consumes(MediaType.APPLICATION_JSON)
	public void addNode(String nodeAlias);
	
	@DELETE
	@Path("/node/{alias}")
	public void deleteNode(@PathParam("alias") String alias);
	
	@GET
	@Path("/node")
	@Produces(MediaType.TEXT_PLAIN)
	public String pingNode();
}
