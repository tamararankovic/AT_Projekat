package messagemanager;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import agentmanager.AgentManagerRemote;
import agents.AID;
import agents.Agent;
import websocket.Logger;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/chat-queue") })
public class MDBConsumer implements MessageListener {

	@EJB private AgentManagerRemote agentManager;
	@EJB private Logger logger;
	
	@Override
	public void onMessage(Message message) {
		try {
			ACLMessage agentMessage = (ACLMessage) ((ObjectMessage) message).getObject();
			for(AID aid : agentMessage.getReceivers()) {
				Agent agent = agentManager.getRunningAgentByAID(aid);
				if (agent != null) {
					log(agentMessage);
					agent.handleMessage(agentMessage);
				}
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	private void log(ACLMessage message) {
		logger.send(logger.ACLToString(message));
	}

}
