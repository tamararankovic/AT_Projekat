package messagemanager;

import javax.ejb.Remote;

@Remote
public interface MessageManagerRemote {

	public void post(AgentMessage message);
}
