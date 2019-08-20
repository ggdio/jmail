package br.com.ggdio.jmail;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Connection factory for smtp
 * 
 * @author Guilherme Dio
 *
 */
public class SMTPConnectionFactory {

	/**
	 * Connects to a MailBox
	 * 
	 * @param protocol
	 * @param host
	 * @param username
	 * @param password
	 * @return {@link Store} connection
	 * @throws MessagingException
	 */
	public static final Store getConnection(String protocol, String host, String username, String password) throws MessagingException {
		Properties props = new Properties();
		props.put("mail.smtp.timeout", 30000);
		props.put("mail.smtps.timeout", 30000);
		props.put("mail.smtp.connectiontimeout", 30000);
		props.put("mail.smtps.connectiontimeout", 30000);
		
		Session session = Session.getDefaultInstance(props);
		Store store = session.getStore(protocol);
		store.connect(host, username, password);
		
		return store;
			
	}
	
}
