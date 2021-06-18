package rest;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import agents.AID;
import agents.AgentType;

public interface AgentEndpoint {

	@GET
	@Path("/classes")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Set<AgentType> getAllAgentTypes();
	
	@GET
	@Path("/running")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Set<AID> getAllRunningAgents();
	
	@PUT
	@Path("/running/{name}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void startAgent(AgentType type, @PathParam("name") String name);
	
	@PUT
	@Path("/running")
	@Consumes(MediaType.APPLICATION_JSON)
	public void stopAgent(AID aid);
}
