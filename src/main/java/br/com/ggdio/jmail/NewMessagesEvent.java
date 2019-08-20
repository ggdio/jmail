package br.com.ggdio.jmail;

import java.util.List;

import javax.mail.Message;

/**
 * Report pack data for {@link MailBox}
 * <p>
 * May be used with {@link MailBoxEventListener}
 * 
 * @author Guilherme Dio
 *
 */
public class NewMessagesEvent implements MailBoxEvent {

	private final MailBox instance;
	private final List<Message> messages;
	
	public NewMessagesEvent(MailBox instance, List<Message> messages) {
		this.instance = instance;
		this.messages = messages;
	}
	
	public MailBox getInstance() {
		return instance;
	}
	
	public List<Message> getMessages() {
		return messages;
	}
	
}