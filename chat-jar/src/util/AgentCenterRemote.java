package util;

import java.io.Serializable;
import java.util.List;

import connectionmanager.AgentCenter;

public interface AgentCenterRemote extends Serializable {

	public void setLocalNodeInfo();
	
	public void setMasterAlias();
	
	public String getMasterAlias();
	
	public AgentCenter getHost();
	
	public List<String> getConnectedNodes();
	
	public void addConnectedNode(String node);
	
	public void removeConnectedNode(String node);
	
	public void setConnectedNodes(List<String> nodes);
}
