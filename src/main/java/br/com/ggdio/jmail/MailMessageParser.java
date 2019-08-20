package br.com.ggdio.jmail;

import java.io.IOException;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import org.jsoup.Jsoup;

/**
 * A simple mail message parser with mimeType handler
 * 
 * @author Guilherme Dio
 *
 */
public class MailMessageParser {

	/**
	 * Parses a Mail Message
	 * @param message - The mail message to be resolved to String
	 * @return Parsed message as a String
	 * 
	 * @throws MessagingException
	 * @throws IOException
	 */
	public static String parse(Message message, boolean parseHtml) throws MessagingException, IOException {
	    if (message.isMimeType("text/plain")) {
	        return message.getContent().toString();
	        
	    } else if (message.isMimeType("multipart/*")) {
	        return parse((MimeMultipart) message.getContent(), parseHtml);
	        
	    } else {
	    	return message.getContent().toString();
	    	
	    }
	}

	/**
	 * Parses a Multipart message
	 * 
	 * @param multipart - The Mime Multipart message
	 * @return Parsed message as a String
	 * 
	 * @throws MessagingException
	 * @throws IOException
	 */
	private static String parse(MimeMultipart multipart, boolean parseHtml)  throws MessagingException, IOException {
	    String result = "";
	    for (int c = 0; c < multipart.getCount(); c++) {
	        BodyPart bodyPart = multipart.getBodyPart(c);
	        if (bodyPart.isMimeType("text/plain")) {
	            result += "\n" + bodyPart.getContent();
	            break;
	            
	        } else if (bodyPart.isMimeType("text/html")) {
	            String html = (String) bodyPart.getContent();
	            if(parseHtml)
	            	result += "\n" + Jsoup.parse(html).text();
	            else
	            	result += html;
	            
	        } else if (bodyPart.getContent() instanceof MimeMultipart){
	            result += parse((MimeMultipart)bodyPart.getContent(), parseHtml);
	            
	        }
	    }
	    
	    return result;
	}
	
}
