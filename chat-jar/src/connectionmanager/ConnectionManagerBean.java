package connectionmanager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.Path;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import agentmanager.AgentManagerRemote;
import agents.AID;
import agents.AgentType;
import rest.AgentEndpoint;
import util.AgentCenterManager;

@Singleton
@Startup
@Remote(ConnectionManager.class)
@LocalBean
@Path("/connection")
public class ConnectionManagerBean implements ConnectionManager {
	
	@EJB AgentCenterManager acm;
	@EJB AgentManagerRemote agm;
	
	@PostConstruct
	private void init() {
		getLocalNodeInfo();
		if(!isMaster())
			handshake();
	}
	
	@Override
	public List<String> registerNode(String nodeAlias) {
		System.out.println("Registering a node with alias: " + nodeAlias);
		postNewNode(nodeAlias);
		new Thread(new Runnable() {
			@Override
			public void run() {
				syncData(nodeAlias);
			}
		}).start();
		return getNodes(nodeAlias);
	}

	@Override
	public void addNode(String nodeAlias) {
		System.out.println("Adding node with alias: " + nodeAlias);
		acm.connectedNodes.add(nodeAlias);
	}

	@Override
	public void deleteNode(String alias) {
		System.out.println("Deleting node with alias: " + alias);
		acm.connectedNodes.remove(alias);
		agm.deleteRunningAgents(alias);
		agm.deleteAgentTypes(alias);
	}

	@Override
	public String pingNode() {
		System.out.println("Pinged");
		return "ok";
	}
	
	private void getLocalNodeInfo() {
		acm.host = acm.getLocalNodeInfo();
		acm.masterAlias = acm.getMasterAlias();
	}
	
	private void handshake() {
		System.out.println("Initiating a handshake, master: " + acm.masterAlias);
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + acm.masterAlias + "/chat-war/rest/connection");
		ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
		acm.connectedNodes = rest.registerNode(acm.host.getAlias());
		client.close();
		System.out.println("Handshake successful. Connected nodes: " + acm.connectedNodes);
	}
	
	@Schedule(hour = "*", minute="*", second="*/120", persistent=false)
	private void heartbeat() {
		System.out.println("Heartbeat protocol initiated");
		for(String node : acm.connectedNodes) {
			System.out.println("Pinging node with alias: " + node);
			new Thread(new Runnable() {
				@Override
				public void run() {
					boolean pingSuccessful = pingNode(node);
					if(!pingSuccessful) {
						System.out.println("Node with alias: " + node + " not alive. Deleting..");
						acm.connectedNodes.remove(node);
						try {
							instructNodeToDeleteNode(acm.host.getAlias(), node);
						} catch (Exception e) { }
						instructNodesToDeleteNode(node);
					}
				}
			}).start();;
		}
	}

	private boolean pingNode(String node) {
		int triesLeft = 2;
		boolean pingSuccessful = false;
		while(triesLeft > 0) {
			try {
				ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget rtarget = client.target("http://" + node + "/chat-war/rest/connection");
				ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
				String response = rest.pingNode();
				client.close();
				if(response.equals("ok")) {
					pingSuccessful = true;
					break;
				}
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				triesLeft--;
			}
		}
		return pingSuccessful;
	}
	
	@PreDestroy
	private void shutDown() {
		instructNodesToDeleteNode(acm.host.getAlias());
	}
	
	private void instructNodeToDeleteNode(String nodeAlias, String receiver) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + receiver + "/chat-war/rest/connection");
		ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
		rest.deleteNode(nodeAlias);
		client.close();
	}
	
	private void instructNodesToDeleteNode(String nodeAlias) {
		for(String node : acm.connectedNodes) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + node + "/chat-war/rest/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.deleteNode(nodeAlias);
			client.close();
		}
	}
	
	private void postNewNode(String nodeAlias) {
		for (String n : acm.connectedNodes) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + n + "/siebog-war/rest/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.addNode(nodeAlias);
			client.close();
		}
	}
	
	private void syncData(String nodeAlias) {
		try {
			syncAgentTypes(nodeAlias);
			syncAgents(nodeAlias);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				syncAgentTypes(nodeAlias);
				syncAgents(nodeAlias);
			} catch (Exception e1) {
				e1.printStackTrace();
				deleteNode(nodeAlias);
				try {
					instructNodeToDeleteNode(acm.host.getAlias(), nodeAlias);
				} catch (Exception e2) { }
				instructNodesToDeleteNode(nodeAlias);
			}
		}
	}
	
	private void syncAgentTypes(String nodeAlias) {
		Set<AgentType> newTypes = getAgentTypes(nodeAlias);
		postAgentTypes(agm.getAgentTypes(), nodeAlias);
		Set<String> receivers = new HashSet<String>(acm.connectedNodes);
		receivers.remove(nodeAlias);
		postAgentTypesToAll(newTypes, receivers);
		agm.updateAgentTypes(newTypes, nodeAlias);
	}
	
	private void syncAgents(String nodeAlias) {
		Set<AID> newAgents = getRunningAgents(nodeAlias);
		postAgents(agm.getRunningAgents(), nodeAlias);
		Set<String> receivers = new HashSet<String>(acm.connectedNodes);
		receivers.remove(nodeAlias);
		postAgentsToAll(newAgents, receivers);
		agm.updateRunningAgents(newAgents);
	}
	
	private Set<AgentType> getAgentTypes(String node) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + node + "/chat-war/rest/agents");
		AgentEndpoint rest = rtarget.proxy(AgentEndpoint.class);
		Set<AgentType> result =  rest.getAllAgentTypes();
		client.close();
		return result;
	}
	
	private Set<AID> getRunningAgents(String node) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + node + "/chat-war/rest/agents");
		AgentEndpoint rest = rtarget.proxy(AgentEndpoint.class);
		Set<AID> result =  rest.getAllRunningAgents();
		client.close();
		return result;
	}
	
	private void postAgentTypesToAll(Set<AgentType> types, Set<String> receivers) {
		for(String receiver : receivers)
			postAgentTypes(types, receiver);
	}
	
	private void postAgentsToAll(Set<AID> agents, Set<String> receivers) {
		for(String receiver : receivers)
			postAgents(agents, receiver);
	}
	
	private void postAgentTypes(Set<AgentType> types, String nodeAlias) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + nodeAlias + "/chat-war/rest/agents");
		AgentEndpoint rest = rtarget.proxy(AgentEndpoint.class);
		rest.updateAgentTypes(types, nodeAlias);
		client.close();
	}
	
	private void postAgents(Set<AID> agents, String nodeAlias) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + nodeAlias + "/chat-war/rest/agents");
		AgentEndpoint rest = rtarget.proxy(AgentEndpoint.class);
		rest.updateRunningAgents(agents);
		client.close();
	}
	
	private List<String> getNodes(String nodeAlias) {
		try {
			List<String> returnNodes = new ArrayList<String>(acm.connectedNodes);
			returnNodes.add(acm.host.getAlias());
			acm.connectedNodes.add(nodeAlias);
			return returnNodes;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				acm.connectedNodes.remove(nodeAlias);
				List<String> returnNodes = new ArrayList<String>(acm.connectedNodes);
				returnNodes.add(acm.host.getAlias());
				acm.connectedNodes.add(nodeAlias);
				return returnNodes;
			} catch (Exception e1) {
				e1.printStackTrace();
				deleteNode(nodeAlias);
				try {
					instructNodeToDeleteNode(acm.host.getAlias(), nodeAlias);
				} catch (Exception e2) { }
				instructNodesToDeleteNode(nodeAlias);
				return null;
			}
		}
	}
	
	private boolean isMaster() {
		return acm.masterAlias == null || acm.masterAlias.length() == 0;
	}
}
