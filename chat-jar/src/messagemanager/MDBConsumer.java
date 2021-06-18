package messagemanager;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import agentmanager.AgentManagerRemote;
import agents.Agent;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/chat-queue") })
public class MDBConsumer implements MessageListener {

	@EJB private AgentManagerRemote agentManager;
	
	@Override
	public void onMessage(Message message) {
		try {
			AgentMessage agentMessage = (AgentMessage) ((ObjectMessage) message).getObject();
			Agent agent = agentManager.getRunningAgentByid(agentMessage.getSender());
			System.out.println("agent: " + agent);
			System.out.println("id: " + agentMessage.getSender());
			if (agent != null)
				agent.handleMessage(agentMessage);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
