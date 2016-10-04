package mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class mailSender {

	private static Properties PROPERTIES;
	private static String USER_NAME = "justracemail";
	private static String PASSWORD = "Supertajnehaslo1";
	private static String HOST = "smtp.gmail.com";
	 
	public static void initialization()
	{
		PROPERTIES = System.getProperties();
		
        PROPERTIES.put("mail.smtp.starttls.enable", "true");
        PROPERTIES.put("mail.smtp.host", HOST);
        PROPERTIES.put("mail.smtp.user", USER_NAME);
        PROPERTIES.put("mail.smtp.password", PASSWORD);
        PROPERTIES.put("mail.smtp.port", 587);
        PROPERTIES.put("mail.smtp.auth", "true");
        PROPERTIES.put("mail.pop3.charset", "iso-8859-2");
        PROPERTIES.put("mail.pop3.charset", "UTF-8" );
	}
	
	
	public static void sendMail(String recipent, String subject, String body)
	{
		Session session = Session.getDefaultInstance(PROPERTIES);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(USER_NAME));
            InternetAddress toAddress = new InternetAddress(recipent);

            message.addRecipient(Message.RecipientType.TO, toAddress);

            String html = body + "<br><br><br><img src=https://scontent-frt3-1.xx.fbcdn.net/v/t34.0-12/13454156_10206399820786619_53151215_n.png?oh=233a08e78dc642249780b3d481653187&oe=5763B3D2>";
            message.setContent(html, "text/html; charset=ISO-8859-2");
            message.setSubject(subject);
            Transport transport = session.getTransport("smtp");
            transport.connect(HOST, USER_NAME, PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
	}
}