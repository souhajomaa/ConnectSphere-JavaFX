package com.example.projetjavafx.root.auth;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailSender {

    /**
     * Sends an email using Gmail SMTP.
     *
     * @param username Your Gmail address (e.g., "yourname@gmail.com")
     * @param password Your Gmail password or App Password
     * @param from     The sender email address
     * @param to       The recipient email address (comma separated if multiple)
     * @param subject  The subject of the email
     * @param body     The body text of the email
     */
    public static void sendEmail(String username, String password, String from, String to, String subject, String body) {
        // Set up the SMTP properties for Gmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");                  // Enable authentication
        props.put("mail.smtp.starttls.enable", "true");       // Enable TLS
        props.put("mail.smtp.host", "smtp.gmail.com");        // Gmail SMTP host
        props.put("mail.smtp.port", "587");                   // TLS port

        // Create a session with an Authenticator using your Gmail credentials
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set the "From" header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set the "To" header field of the header.
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            // Set the "Subject" header field.
            message.setSubject(subject);

            // Set the content of the message (text body)
            message.setText(body);

            // Send the email
            Transport.send(message);

            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    // Example usage:
//    public static void main(String[] args) {
//        // Replace these values with your credentials and desired message details.
//        String username = "meyssemmufti@gmail.com";
//        String password = "owcl kwes xlmh jkot"; // If 2FA is enabled, use an App Password.
//        String from = "meyssemmufti@gmail.com";
//        String to = "maycemmofti55@gmail.com";
//        String subject = "Test Email from Java";
//        String body = "Hello,\n\nThis is a test email sent from Java using Gmail SMTP.";
//
//        sendEmail(username, password, from, to, subject, body);
//    }
}

