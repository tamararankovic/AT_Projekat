package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import connectionmanager.AgentCenter;
import connectionmanager.ConnectionManager;

@Stateless
@LocalBean
public class AgentCenterManager {

	public AgentCenter host;
	public String masterAlias;
	public List<String> connectedNodes = new ArrayList<String>();
	
	public AgentCenter getLocalNodeInfo() {
		String nodeAddress = getNodeAddress();
		String nodeAlias = getNodeAlias() + ":8080";
		AgentCenter host = new AgentCenter(nodeAddress, nodeAlias);
		System.out.println("node alias: " + host.getAlias() + ", node address: " + 
		host.getAddress());
		return host;
	}
	
	public String getMasterAlias() {
		try {
			File f = ResourceLoader.getFile(ConnectionManager.class, "", "connection.properties");
			FileInputStream fileInput = new FileInputStream(f);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();
			String masterAlias = properties.getProperty("master");
			System.out.println("Master alias: " + masterAlias);
			return masterAlias;
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
}
