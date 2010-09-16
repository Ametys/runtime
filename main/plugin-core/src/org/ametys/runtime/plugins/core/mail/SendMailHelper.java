/*
 *  Copyright 2010 Anyware Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ametys.runtime.plugins.core.mail;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.ametys.runtime.config.Config;

/**
 * Helper for sending mail
 *
 */
public final class SendMailHelper
{
    private SendMailHelper ()
    {
        // Nothing
    }
    
    /**
     * Sends mail without authentication or attachments.
     * @param subject The mail subject
     * @param htmlBody The HTML mail body. Can be null.
     * @param textBody The text mail body. Can be null.
     * @param recipient The recipient address
     * @param sender The sender address
     * @throws MessagingException If an error occurred while preparing or sending email
     */
    public static void sendMail(String subject, String htmlBody, String textBody, String recipient, String sender) throws MessagingException
    {
        String smtpHost = Config.getInstance().getValueAsString("smtp.mail.host");
        String smtpUser = Config.getInstance().getValueAsString("smtp.mail.user");
        String smtpPass = Config.getInstance().getValueAsString("smtp.mail.password");

        try
        {
            sendMail(subject, htmlBody, textBody, null, recipient, sender, smtpHost, smtpUser, smtpPass);
        }
        catch (IOException e)
        {
            // Should never happen, as IOException can only be thrown where there are attachments.
        }
    }
    
    /**
     * Sends mail without authentication or attachments.
     * @param subject The mail subject
     * @param htmlBody The HTML mail body. Can be null.
     * @param textBody The text mail body. Can be null.
     * @param recipient The recipient address
     * @param sender The sender address
     * @param host The server mail host
     * @throws MessagingException If an error occurred while preparing or sending email
     */
    public static void sendMail(String subject, String htmlBody, String textBody, String recipient, String sender, String host) throws MessagingException
    {
        try
        {
            sendMail(subject, htmlBody, textBody, null, recipient, sender, host, null, null);
        }
        catch (IOException e)
        {
            // Should never happen, as IOException can only be thrown where there are attachments.
        }
    }
    
    /**
     * Sends mail without authentication, with attachments.
     * @param subject The mail subject
     * @param htmlBody The HTML mail body. Can be null.
     * @param textBody The text mail body. Can be null.
     * @param attachments the file attachments. Can be null.
     * @param recipient The recipient address
     * @param sender The sender address
     * @throws MessagingException If an error occurred while preparing or sending email
     * @throws IOException if an error occurs while attaching a file.
     */
    public static void sendMail(String subject, String htmlBody, String textBody, Collection<File> attachments, String recipient, String sender) throws MessagingException, IOException
    {
        String smtpHost = Config.getInstance().getValueAsString("smtp.mail.host");
        String smtpUser = Config.getInstance().getValueAsString("smtp.mail.user");
        String smtpPass = Config.getInstance().getValueAsString("smtp.mail.password");

        sendMail(subject, htmlBody, textBody, attachments, recipient, sender, smtpHost, smtpUser, smtpPass);
    }
    

    /**
     * Sends mail without authentication, with attachments.
     * @param subject The mail subject
     * @param htmlBody The HTML mail body. Can be null.
     * @param textBody The text mail body. Can be null.
     * @param attachments the file attachments. Can be null.
     * @param recipient The recipient address
     * @param sender The sender address
     * @param host The server mail host
     * @throws MessagingException If an error occurred while preparing or sending email
     * @throws IOException if an error occurs while attaching a file.
     */
    public static void sendMail(String subject, String htmlBody, String textBody, Collection<File> attachments, String recipient, String sender, String host) throws MessagingException, IOException
    {
        sendMail(subject, htmlBody, textBody, attachments, recipient, sender, host, null, null);
    }
    
    /**
     * Sends mail with authentication, without attachments.
     * @param subject The mail subject
     * @param htmlBody The HTML mail body. Can be null.
     * @param textBody The text mail body. Can be null.
     * @param recipient The recipient address
     * @param sender The sender address
     * @param host The server mail host
     * @param user The user name
     * @param password The user password
     * @throws MessagingException If an error occurred while preparing or sending email
     */
    public static void sendMail(String subject, String htmlBody, String textBody, String recipient, String sender, String host, String user, String password) throws MessagingException
    {
        try
        {
            sendMail(subject, htmlBody, textBody, null, recipient, sender, host, user, password);
        }
        catch (IOException e)
        {
            // Should never happen, as IOException can only be thrown where there are attachments.
        }
    }
    
    /**
     * Sends mail with authentication and attachments.
     * @param subject The mail subject
     * @param htmlBody The HTML mail body. Can be null.
     * @param textBody The text mail body. Can be null.
     * @param attachments the file attachments. Can be null.
     * @param recipient The recipient address
     * @param sender The sender address
     * @param host The server mail host
     * @param user The user name
     * @param password The user password
     * @throws MessagingException If an error occurred while preparing or sending email
     * @throws IOException if an error occurs while attaching a file.
     */
    public static void sendMail(String subject, String htmlBody, String textBody, Collection<File> attachments, String recipient, String sender, String host, String user, String password) throws MessagingException, IOException
    {
        Properties props = new Properties();

        // Setup mail server
        props.put("mail.smtp.host", host);

        // Get session
        Session session = Session.getInstance(props, null);

        // Define message
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender));
        message.setSentDate(new Date());
        message.setSubject(subject);
        
        // Root multipart
        Multipart multipart = new MimeMultipart("mixed");
        
        // Message body part.
        Multipart messageMultipart = new MimeMultipart("alternative");
        MimeBodyPart messagePart = new MimeBodyPart();
        messagePart.setContent(messageMultipart);
        multipart.addBodyPart(messagePart);
        
        if (textBody != null)
        {
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setContent(textBody, "text/plain;charset=utf-8");
            textBodyPart.addHeader("Content-Type", "text/plain;charset=utf-8");
            messageMultipart.addBodyPart(textBodyPart);
        }
        
        if (htmlBody != null)
        {
            MimeBodyPart htmlBodyPart = new MimeBodyPart();
            htmlBodyPart.setContent(htmlBody, "text/html;charset=utf-8");
            htmlBodyPart.addHeader("Content-Type", "text/html;charset=utf-8");
            messageMultipart.addBodyPart(htmlBodyPart);
        }
        
        if (attachments != null)
        {
            for (File attachment : attachments)
            {
                MimeBodyPart fileBodyPart = new MimeBodyPart();
                fileBodyPart.attachFile(attachment);
                multipart.addBodyPart(fileBodyPart);
            }
        }
        
        message.setContent(multipart);
        
        // Recipient
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

        Transport tr = session.getTransport("smtp");
        if (user != null)
        {
            tr.connect(host, user, password);
        }
        else
        {
            tr.connect();
        }
        

        message.saveChanges();
        tr.sendMessage(message, message.getAllRecipients());
        tr.close();
    }
}
