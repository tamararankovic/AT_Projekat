package util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import agentmanager.AgentManagerBean;
import agentmanager.AgentManagerRemote;
import agents.Agent;
import agents.UserAgent;
import chatmanager.ChatManagerBean;
import chatmanager.ChatManagerRemote;
import messagemanager.MessageManagerBean;
import messagemanager.MessageManagerRemote;

public abstract class JNDILookup {

	public static final String JNDIPathChat = "ejb:chat-ear/chat-jar//";
	public static final String AgentManagerLookup = JNDIPathChat + AgentManagerBean.class.getSimpleName() + "!"
			+ AgentManagerRemote.class.getName();
	public static final String MessageManagerLookup = JNDIPathChat + MessageManagerBean.class.getSimpleName() + "!"
			+ MessageManagerRemote.class.getName();
	public static final String ChatManagerLookup = JNDIPathChat + ChatManagerBean.class.getSimpleName() + "!"
			+ ChatManagerRemote.class.getName();
	public static final String UserAgentLookup = JNDIPathChat + UserAgent.class.getSimpleName() + "!"
			+ Agent.class.getName() + "?stateful";

	@SuppressWarnings("unchecked")
	public static <T> T lookUp(String name, Class<T> c) {
		T bean = null;
		try {
			Context context = new InitialContext();

			System.out.println("Looking up: " + name);
			bean = (T) context.lookup(name);

			context.close();

		} catch (NamingException e) {
		
		}
		return bean;
	}

}
