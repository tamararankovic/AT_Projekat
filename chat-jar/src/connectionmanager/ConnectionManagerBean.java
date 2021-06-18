package connectionmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.ws.rs.Path;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import chatmanager.ChatManagerRemote;
import model.Host;
import model.User;
import util.ResourceLoader;
import websocket.WebSocket;

@Singleton
@Startup
@Remote(ConnectionManager.class)
@LocalBean
@Path("/connection")
public class ConnectionManagerBean implements ConnectionManager {

	private Host localNode;
	private List<String> connectedNodes = new ArrayList<String>();
	
	@EJB ChatManagerRemote chm;
	@EJB WebSocket ws;
	
	@PostConstruct
	private void init() {
		getLocalNodeInfo();
		if(!localNode.isMaster())
			handshake();
	}
	
	@Override
	public List<String> registerNode(String nodeAlias) {
		System.out.println("Registering a node with alias: " + nodeAlias);
		for (String n : connectedNodes) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + n + "/siebog-war/rest/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.addNode(nodeAlias);
			client.close();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					postRegistered(nodeAlias);
					postLoggedIn(nodeAlias, localNode.getAlias(), chm.getLocallyLoggedIn());
					for(String host : connectedNodes) {
						List<User> users = chm.getLoggedInByHost(host);
						if(users != null)
							postLoggedIn(nodeAlias, host, users);
					}
					postMessages(nodeAlias);
				} catch (Exception e) {
					e.printStackTrace();
					try {
						postRegistered(nodeAlias);
						postLoggedIn(nodeAlias, localNode.getAlias(), chm.getLocallyLoggedIn());
						for(String host : connectedNodes) {
							List<User> users = chm.getLoggedInByHost(host);
							if(users != null)
								postLoggedIn(nodeAlias, host, users);
						}
						postMessages(nodeAlias);
					} catch (Exception e1) {
						e1.printStackTrace();
						deleteNode(nodeAlias);
						instructNodesToDeleteNode(nodeAlias);
					}
				}
			}
		}).start();
		try {
			List<String> returnNodes = new ArrayList<String>(connectedNodes);
			returnNodes.add(localNode.getAlias());
			connectedNodes.add(nodeAlias);
			return returnNodes;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				connectedNodes.remove(nodeAlias);
				List<String> returnNodes = new ArrayList<String>(connectedNodes);
				returnNodes.add(localNode.getAlias());
				connectedNodes.add(nodeAlias);
				return returnNodes;
			} catch (Exception e1) {
				e1.printStackTrace();
				deleteNode(nodeAlias);
				instructNodesToDeleteNode(nodeAlias);
				return null;
			}
		}
	}

	@Override
	public void addNode(String nodeAlias) {
		System.out.println("Adding node with alias: " + nodeAlias);
		connectedNodes.add(nodeAlias);
	}

	@Override
	public void deleteNode(String alias) {
		System.out.println("Deleting node with alias: " + alias);
		connectedNodes.remove(alias);
		chm.deleteLoggedInByHost(alias);
		ws.sendToAllLoggedIn(ws.getLoggedInListTextMessage(chm.getLoggedIn()));
	}

	@Override
	public String pingNode() {
		System.out.println("Pinged");
		return "ok";
	}
	
	private void getLocalNodeInfo() {
		String nodeAddress = getNodeAddress();
		String nodeAlias = getNodeAlias() + ":8080";
		String masterAlias = getMasterAlias();
		localNode = new Host(nodeAddress, nodeAlias, masterAlias);
		System.out.println("node alias: " + localNode.getAlias() + ", node address: " + 
		localNode.getAddress() + ", master alias: " + 
		localNode.getMasterAlias());
	}
	
	private String getMasterAlias() {
		try {
			File f = ResourceLoader.getFile(ConnectionManager.class, "", "connection.properties");
			FileInputStream fileInput = new FileInputStream(f);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();
			return properties.getProperty("master");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String getNodeAddress() {
		try {
			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
			ObjectName http = new ObjectName("jboss.as:socket-binding-group=standard-sockets,socket-binding=http");
			return (String) mBeanServer.getAttribute(http, "boundAddress");
		} catch (MalformedObjectNameException | InstanceNotFoundException | AttributeNotFoundException | ReflectionException | MBeanException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	private String getNodeAlias() {
		return System.getProperty("jboss.node.name");
	}
	
	private void handshake() {
		System.out.println("Initiating a handshake, master: " + localNode.getMasterAlias());
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + localNode.getMasterAlias() + "/chat-war/rest/connection");
		ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
		connectedNodes = rest.registerNode(localNode.getAlias());
		client.close();
		System.out.println("Handshake successful. Connected nodes: " + connectedNodes);
	}
	
	@Schedule(hour = "*", minute="*", second="*/120", persistent=false)
	private void heartbeat() {
		System.out.println("Heartbeat protocol initiated");
		for(String node : connectedNodes) {
			System.out.println("Pinging node with alias: " + node);
			new Thread(new Runnable() {
				@Override
				public void run() {
					boolean pingSuccessful = pingNode(node);
					if(!pingSuccessful) {
						System.out.println("Node with alias: " + node + " not alive. Deleting..");
						connectedNodes.remove(node);
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
		instructNodesToDeleteNode(localNode.getAlias());
	}
	
	private void instructNodesToDeleteNode(String nodeAlias) {
		for(String node : connectedNodes) {
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget rtarget = client.target("http://" + node + "/chat-war/rest/connection");
			ConnectionManager rest = rtarget.proxy(ConnectionManager.class);
			rest.deleteNode(nodeAlias);
			client.close();
		}
	}
	
	public void postLoggedIn() {
		for(String alias : connectedNodes)
			postLoggedIn(alias, localNode.getAlias(), chm.getLocallyLoggedIn());
	}
	
	private void postLoggedIn(String alias, String host, List<User> users) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + alias + "/chat-war/rest/sync");
		DataSync rest = rtarget.proxy(DataSync.class);
		rest.syncLoggedIn(host, users);
		client.close();
	}
	
	public void postRegistered() {
		for(String alias : connectedNodes)
			postRegistered(alias);
	}
	
	private void postRegistered(String alias) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + alias + "/chat-war/rest/sync");
		DataSync rest = rtarget.proxy(DataSync.class);
		rest.syncRegistered(chm.getRegistered());
		client.close();
	}
	
	public void postMessages() {
		for(String alias : connectedNodes)
			postMessages(alias);
	}
	
	private void postMessages(String alias) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + alias + "/chat-war/rest/sync");
		DataSync rest = rtarget.proxy(DataSync.class);
		rest.syncMessages(chm.getMessages());
		client.close();
	}
}
