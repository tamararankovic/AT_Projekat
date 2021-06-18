package util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public abstract class JNDILookup {

	public static final String JNDIPathChat = "ejb:chat-ear/chat-jar//";

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
