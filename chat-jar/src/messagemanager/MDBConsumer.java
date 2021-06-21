package messagemanager;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import agentmanager.AgentManagerRemote;
import agents.AID;
import agents.Agent;
import rest.MessageEndpoint;
import util.AgentCenterRemote;
import websocket.Logger;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/queue/chat-queue") })
public class MDBConsumer implements MessageListener {

	@EJB private AgentCenterRemote acm;
	@EJB private AgentManagerRemote agentManager;
	@EJB private Logger logger;
	
	@Override
	public void onMessage(Message message) {
		try {
			ACLMessage agentMessage = (ACLMessage) ((ObjectMessage) message).getObject();
			for(AID aid : agentMessage.getReceivers()) {
				Set<AID> receivers = new HashSet<AID>();
				receivers.add(aid);
				agentMessage.setReceivers(receivers);
				if(!aid.getHost().getAlias().equals(acm.getHost().getAlias())) {
					forwardMessage(agentMessage, aid.getHost().getAlias());
				}
				else {
					Agent agent = agentManager.getRunningAgentByAID(aid);
					if (agent != null) {
						log(agentMessage);
						agent.handleMessage(agentMessage);
					}
				}
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	private void forwardMessage(ACLMessage message, String receiver) {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget rtarget = client.target("http://" + receiver + "/chat-war/rest/messages");
		MessageEndpoint rest = rtarget.proxy(MessageEndpoint.class);
		rest.sendMessage(message);
		client.close();
	}
	
	private void log(ACLMessage message) {
		logger.send(logger.ACLToString(message));
	}

}
