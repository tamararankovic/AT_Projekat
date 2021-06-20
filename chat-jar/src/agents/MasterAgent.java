package agents;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import messagemanager.ACLMessage;
import websocket.MatchSocket;

@Stateful
@Remote(Agent.class)
public class MasterAgent extends BaseAgent {

	private static final long serialVersionUID = 1L;

	@EJB MatchSocket socket;
	
	@Override
	public void handleMessage(ACLMessage message) {
		switch(message.getPerformative()) {
			case DISPLAY: {
				System.out.println(message.getContent());
				socket.send(message.getContent());
				break;
			}
			default: return;
		}
	}

}
