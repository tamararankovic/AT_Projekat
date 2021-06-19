package rest;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
	
	@POST
	@Path("/classes/{nodeAlias}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void updateAgentTypes(Set<AgentType> types, @PathParam("nodeAlias") String nodeAlias);
	
	@GET
	@Path("/running")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Set<AID> getAllRunningAgents();
	
	@POST
	@Path("/running/{nodeAlias}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void updateRunningAgents(Set<AID> agents, @PathParam("nodeAlias") String nodeAlias);
	
	@PUT
	@Path("/running/{name}")
	@Consumes(MediaType.APPLICATION_JSON)
	public void startAgent(AgentType type, @PathParam("name") String name);
	
	@PUT
	@Path("/running")
	@Consumes(MediaType.APPLICATION_JSON)
	public void stopAgent(AID aid);
}
