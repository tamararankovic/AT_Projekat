package messagemanager;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

@Stateless
@LocalBean
public class MessageManagerBean implements MessageManagerRemote {

	@EJB private JMSFactory factory;
	private Session session;
	private MessageProducer defaultProducer;

	@PostConstruct
	public void postConstruct() {
		session = factory.getSession();
		defaultProducer = factory.getProducer(session);
	}

	@PreDestroy
	public void preDestroy() {
		try {
			session.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void post(ACLMessage message) {
		try {
			defaultProducer.send(createJMSMessage(message));
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	private Message createJMSMessage(ACLMessage message) {
		ObjectMessage jmsMessage = null;
		try {
			jmsMessage = session.createObjectMessage(message);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return jmsMessage;
	}

	@Override
	public Set<String> getPerformatives() {
		Set<String> result = new HashSet<String>();
		for(Performative p : Performative.values())
			result.add(p.toString());
		return result;
	}
}
