package model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import util.JsonDateDeserializer;
import util.JsonDateSerializer;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private User sender;
	private User receiver;
	@JsonDeserialize(using = JsonDateDeserializer.class)
	@JsonSerialize(using = JsonDateSerializer.class)
	private LocalDateTime created;
	private String subject;
	private String content;
	
	public Message() {
		
	}
	
	public Message(User sender, User receiver, LocalDateTime created, String subject, String content) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.created = created;
		this.subject = subject;
		this.content = content;
	}

	public User getSender() {
		return sender;
	}

	public User getReceiver() {
		return receiver;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public String getSubject() {
		return subject;
	}

	public String getContent() {
		return content;
	}

	@Override
	public boolean equals(Object obj) {
		Message message = (Message)obj;
		return sender.equals(message.sender) &&
				receiver.equals(message.receiver) &&
				created.equals(message.created) &&
				subject.equals(message.subject) &&
				content.equals(message.content);
	}
	
	@Override
	public String toString() {
		return "sender: " + sender.getUsername() + " receiver: " + receiver.getUsername() + " subject: " + subject + " content: " + content + " created: " + created;
	}
}
