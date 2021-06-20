package model;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String username;
	private String password;
	
	public User() {
		
	}
	
	public User(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public boolean equals(Object obj) {
		return username.equals(((User)obj).username);
	}

	@Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return super.toString();
		}
	}
	
	
}
