package com.ev.cademeupet.services;

import android.content.Context;
import android.net.Uri;

import com.ev.cademeupet.R;
import com.ev.cademeupet.models.User;
import com.ev.cademeupet.models.Pet;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService 
{
    public static void sendEmail(Context context, Pet pet, User user)
    {
        final String sender = "";
        final String pass = "";
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication( sender, pass );
            }
        });
        
        String subject = context.getString(R.string.email_subject);
        String mapLink = "https://www.google.com/maps/search/?api=1&query=" + Uri.encode( user.getFullAddress() );
        
        String text = context.getString(R.string.email_body, 
                user.getFullName(), // %1$s
                pet.getName(),      // %2$s
                mapLink,            // %3$s
                user.getPhone()     // %4$s
        );
        
        new Thread(() -> {
            try {
                Message message = new MimeMessage( session );
                message.setFrom(new InternetAddress( sender ) );
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse( pet.getOwnerEmail() ) );
                message.setSubject( subject );
                message.setText( text );
                
                Transport.send( message );
                
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
