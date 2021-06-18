package messagemanager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AgentMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private AgentMessageType type;
	private String sender;
	private Map<String, String> attributes = new HashMap<String, String>();
	
	public AgentMessage(AgentMessageType type, String sender) {
		super();
		this.type = type;
		this.sender = sender;
	}

	public AgentMessageType getType() {
		return type;
	}
	
	public String getSender() {
		return sender;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}
	
	public void addAttribute(String key, String value) {
		attributes.put(key, value);
	}
}
