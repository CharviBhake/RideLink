package Smart_Carpooling.demo.Configuration;
import jakarta.mail.*;

import java.util.Properties;
public class GmailTest {
    public static void main(String[] args) throws Exception {
        String username = "charvibhake@gmail.com"; // your Gmail
        String password = "yhhnqwiajkgqkwxd";      // your App Password (no spaces)

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        Transport transport = session.getTransport("smtp");
        try {
            System.out.println("Connecting...");
            transport.connect();
            System.out.println("✅ Connected successfully!");
        } catch (AuthenticationFailedException e) {
            System.out.println("❌ Gmail rejected credentials: " + e.getMessage());
            e.printStackTrace();
        } finally {
            transport.close();
        }
    }
}
