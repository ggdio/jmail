package br.com.ggdio.jmail;

import java.util.EventListener;

/**
 * Event Listener for MailBox
 * 
 * @author Guilherme Dio
 *
 */
public interface MailBoxEventListener extends EventListener {

	/**
	 * Event fired when new mail messages are received
	 * <p>
	 * After processing it, consider calling {@link MailBox#clearProcessed()}
	 * 
	 * @param event - The event pack
	 */
	public void onNewMessages(NewMessagesEvent event);
	
}