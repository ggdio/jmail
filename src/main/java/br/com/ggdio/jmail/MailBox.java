package br.com.ggdio.jmail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MailBox API
 * 
 * @author Guilherme Dio
 *
 */
public class MailBox {
	
	private static final Logger LOG = LoggerFactory.getLogger(MailBox.class);
	
	private static final Map<String, MailBox> INSTANCES = new HashMap<String, MailBox>();
	
	private final String protocol;
	private final String host;
	private final String mail;
	private final String password;
	
	private AtomicReference<Store> connection = new AtomicReference<>();
	
	/**
	 * @param protocol - SMTP Protocol (imap, pop3, ...)
	 * @param host     - MailBox Hostname 
	 * @param mail     - E-Mail address
	 * @param password - Password
	 * @return {@link MailBox} instance
	 * 
	 * @throws MessagingException - If something goes wront while attempting to connect
	 */
	private MailBox(String protocol, String host, String mail, String password) throws MessagingException {
		this.protocol = protocol;
		this.host = host;
		this.mail = mail;
		this.password = password;
	}
	
	/**
	 * Begin connection
	 */
	public void begin() {
		this.connection.set(connect());
	}
	
	/**
	 * End connection
	 * @throws MessagingException
	 */
	public void end() throws MessagingException {
		this.connection.getAndSet(null).close();
	}
	
	public boolean isOpen() {
		Store store = connection.get();
		return store != null && store.isConnected();
	}
	
	private void check() {
		Store connection = this.connection.get();
		if(connection == null || !connection.isConnected()) {
			throw new IllegalStateException("Connection is closed. Consider calling begin().");
		}
	}
	
	/**
	 * Gets/Creates an instance of {@link MailBox}
	 * <p>
	 * Unique per mail account
	 * 
	 * @param protocol - SMTP Protocol (imap, pop3, ...)
	 * @param host     - MailBox Hostname 
	 * @param mail     - E-Mail address
	 * @param password - Password
	 * @return {@link MailBox} instance
	 * 
	 * @throws MessagingException - If something goes wront while attempting to connect
	 */
	public static MailBox getInstance(String protocol, String host, String mail, String password) throws MessagingException {
		MailBox instance = INSTANCES.get(mail);
		if(instance == null) {
			instance = new MailBox(protocol, host, mail, password);
			INSTANCES.put(mail, instance);
		}
		return instance;
	}
	
	/**
	 * Gets an instance of {@link MailBox}
	 * 
	 * @param mail - E-Mail address
	 * @return {@link MailBox} instance
	 */
	public static MailBox getInstance(String mail) {
		return INSTANCES.get(mail);
	}
	
	/**
	 * Gets a folder by its name
	 * 
	 * @param folderName
	 * @return
	 * @throws MessagingException 
	 */
	public Folder getFolder(String folderName) throws MessagingException {
		Folder folder = getConnection().getFolder(folderName);
		if (!folder.exists() && folder.create(Folder.READ_WRITE)) {
	    	folder.setSubscribed(true);
	    	
		} else if(!folder.isOpen()) {
			folder.open(Folder.READ_WRITE);
			
		}
		return folder;
	}
	
	/**
	 * Moves a list of messages to a target folder
	 * 
	 * @param target   - Target folder name
	 * @param messages - The list of messages
	 * 
	 * @return {@link Folder} instance
	 * 
	 * @throws MessagingException - If somehting wrong occurs
	 */
	public Folder moveToFolder(String target, List<Message> messages) throws MessagingException {
		Folder folder = getFolder(target);
		moveToFolder(folder, messages);
		return folder;
	}
	
	/**
	 * Moves a list of messages to a target folder
	 * 
	 * @param target   - Target folder name
	 * @param messages - The list of messages
	 * 
	 * @throws MessagingException - If somehting wrong occurs
	 */
	public void moveToFolder(Folder target, List<Message> messages) throws MessagingException {
		target.appendMessages(messages.toArray(new Message[]{}));
	}
	
	/**
	 * Removes a list of messages from a target folder
	 * 
	 * @param folder   - The folder containing the messages
	 * @param messages - Messages to be removed from folder
	 * 
	 * @return {@link Folder} instance
	 * 
	 * @throws MessagingException - If somehting wrong occurs
	 */
	public Folder removeFromFolder(String folder, List<Message> messages) throws MessagingException {
		Folder ref = getFolder(folder);
		removeFromFolder(ref, messages);
		return ref;
	}
	
	/**
	 * Removes a list of messages from a target folder
	 * 
	 * @param folder   - The folder containing the messages
	 * @param messages - Messages to be removed from folder
	 * 
	 * @throws MessagingException - If somehting wrong occurs
	 */
	public void removeFromFolder(Folder folder, List<Message> messages) throws MessagingException {
		Flags deleted = new Flags(Flags.Flag.DELETED);
		folder.setFlags(messages.toArray(new Message[]{}), deleted, true);
		folder.expunge();
	}
	
	/**
	 * Scans a given folder by its name
	 * 
	 * @param folderName - The name of the folder
	 * 
	 * @return A {@link List} of {@link Message}s from {@link Folder}
	 * @throws MessagingException 
	 */
	public List<Message> searchUnread(String folderName) throws MessagingException {
		Folder folder = null;
		try {
			folder = getConnection().getFolder(folderName);
			return searchUnread(folder);

		} finally {
			closeFolder(folder);
			
		}
	}
	
	/**
	 * Scans a given folder reference
	 * 
	 * @param folder - The {@link Folder} reference
	 * 
	 * @return A {@link List} of {@link Message}s from {@link Folder}
	 * @throws MessagingException 
	 */
	public List<Message> searchUnread(Folder folder) throws MessagingException {
		LOG.info("MailBox::Processing MailBox [folder={}, box={}, numOfMessages={}]", folder.getName(), mail, folder.getUnreadMessageCount());
		
	    FlagTerm flag = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
		return Arrays.asList(folder.search(flag));
	}
	
	/**
	 * Quietly closes mail store folder
	 * @param folders - Var args of folders
	 */
	public  void closeFolder(Folder...folders) {
		for (Folder folder : folders) {
			if(folder != null && folder.isOpen()) {
				try {
					folder.close(false);
				} catch (MessagingException e) {
					LOG.error("Error while closing mailbox folder", e);
				}
			}
		}
	}
	
	/**
	 * Gets a smpt connection
	 * TODO: Should be cached ?
	 * 
	 * @return {@link Store}
	 */
	protected Store connect() {
		try {
			return SMTPConnectionFactory.getConnection(protocol, host, mail, password);
			
		} catch (MessagingException e) {
			throw new RuntimeException(e);
			
		}
	}
	
	private Store getConnection() {
		check();
		return connection.get();
	}
	
	public String getMailAddress() {
		return mail;
	}
	
}
