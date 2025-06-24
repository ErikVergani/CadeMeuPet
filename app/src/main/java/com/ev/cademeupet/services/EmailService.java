package com.ev.cademeupet.services;

import android.net.Uri;

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
    public static void sendEmail( Pet pet, User user ) 
    {
        final String sender = "dev.ev.sender@gmail.com";
        final String pass = "htua pssa etxj rbht"; //Old, inactive pass, use yours :)
        
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
        
        String subject = "Boas notícias! Seu pet pode ter sido encontrado!";
        
        String text = "Olá!\n\n" +
                "O usuário " + user.getFullName() + " viu o seu pet \"" + pet.getName() + "\" e clicou em 'Encontrei seu pet' no aplicativo Cadê Meu Pet!\n\n" +
                "Veja onde encontrar essa pessoa: \n" +
                "https://www.google.com/maps/search/?api=1&query=" + Uri.encode( user.getFullAddress() ) + "\n" +
                "Entre em contato com o usuário através do número: " + user.getPhone() +
                "\n\n Esperamos que vocês se reencontrem em breve! 🐾❤️";
        
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
