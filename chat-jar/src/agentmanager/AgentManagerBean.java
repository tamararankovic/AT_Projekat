package agentmanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Singleton;

import agents.Agent;
import util.JNDILookup;

@Singleton
@Remote(AgentManagerRemote.class)
@LocalBean
public class AgentManagerBean implements AgentManagerRemote {

	private static final long serialVersionUID = 1L;
	
	Map<String, Agent> runningAgents = new HashMap<String, Agent>();
	
	@Override
	public void startAgent(String agentId, String name) {
		Agent agent = (Agent) JNDILookup.lookUp(name, Agent.class);
		agent.init(agentId);
		runningAgents.put(agentId, agent);
	}

	@Override
	public void stopAgent(String agentId) {
		runningAgents.remove(agentId);
	}

	@Override
	public List<Agent> getRunningAgents() {
		return (List<Agent>) runningAgents.values();
	}

	@Override
	public Agent getRunningAgentByid(String agentId) {
		return runningAgents.get(agentId);
	}
}
