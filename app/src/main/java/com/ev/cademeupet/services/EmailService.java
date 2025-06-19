package com.ev.cademeupet.services;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

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
    public static void sendEmail( String to, String petName, String userName ) {
        final String remetente = "dev.ev.sender@gmail.com";
        final String senha = "htua pssa etxj rbht";
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remetente, senha);
            }
        });
        
        String assunto = "Boas notícias! Seu pet pode ter sido encontrado!";
        String corpo = "Olá!\n\n" +
                "Alguém viu o seu pet \"" + petName + "\" e clicou em 'Encontrei seu pet' no aplicativo Cadê Meu Pet!\n\n" +
                "Veja onde essa pessoa estava: \n" +
                "https://www.google.com/maps/search/?api=1&query=" +
                Uri.encode(userName) + "\n\n" +
                "Esperamos que vocês se reencontrem em breve! 🐾";
        
        new Thread(() -> {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(remetente));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(assunto);
                message.setText( corpo );
                
                Transport.send(message);
                
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
