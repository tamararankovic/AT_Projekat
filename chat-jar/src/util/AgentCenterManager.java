package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ejb.Remote;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import connectionmanager.AgentCenter;
import connectionmanager.ConnectionManager;

@Singleton
@Remote(AgentCenterRemote.class)
@Startup
public class AgentCenterManager implements AgentCenterRemote {

	private static final long serialVersionUID = 1L;
	
	private AgentCenter host;
	private String masterAlias;
	private List<String> connectedNodes = new ArrayList<String>();
	
	@Override
	public void setLocalNodeInfo() {
		String nodeAddress = getNodeAddress();
		String nodeAlias = getNodeAlias() + ":8080";
		this.host = new AgentCenter(nodeAddress, nodeAlias);;
		System.out.println("node alias: " + this.host.getAlias() + ", node address: " + 
		this.host.getAddress());
	}
	
	@Override
	public void setMasterAlias() {
		try {
			File f = ResourceLoader.getFile(ConnectionManager.class, "", "connection.properties");
			FileInputStream fileInput = new FileInputStream(f);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();
			masterAlias = properties.getProperty("master");
			System.out.println("Master alias: " + masterAlias);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String getMasterAlias() {
		return masterAlias;
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

	@Override
	public AgentCenter getHost() {
		return host;
	}

	@Override
	public List<String> getConnectedNodes() {
		return connectedNodes;
	}

	@Override
	public void addConnectedNode(String node) {
		connectedNodes.add(node);
	}

	@Override
	public void removeConnectedNode(String node) {
		connectedNodes.remove(node);
	}

	@Override
	public void setConnectedNodes(List<String> nodes) {
		connectedNodes = nodes;
	}
}
