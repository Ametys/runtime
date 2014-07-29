/*
 *  Copyright 2014 Anyware Services
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
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.util.LoggerFactory;

/**
 * Helper for sending mail
 */
public final class SendMailHelper
{
    /** Logger */
    protected static final Logger _LOGGER = LoggerFactory.getLoggerFor(SendMailHelper.class);

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
        long smtpPort = Config.getInstance().getValueAsLong("smtp.mail.port");
        String securityProtocol = Config.getInstance().getValueAsString("smtp.mail.security.protocol");

        sendMail(subject, htmlBody, textBody, recipient, sender, smtpHost, smtpPort, securityProtocol);
    }

    /**
     * Sends mail without authentication or attachments.
     * @param subject The mail subject
     * @param htmlBody The HTML mail body. Can be null.
     * @param textBody The text mail body. Can be null.
     * @param recipient The recipient address
     * @param sender The sender address
     * @param host The server mail host
     * @param port The server mail port
     * @param securityProtocol The server mail security protocol
     * @throws MessagingException If an error occurred while preparing or sending email
     */
    public static void sendMail(String subject, String htmlBody, String textBody, String recipient, String sender, String host, long port, String securityProtocol) throws MessagingException
    {
        sendMail(subject, htmlBody, textBody, recipient, sender, host, port, securityProtocol, null, null);
    }


    /**
     * Sends mail with authentication, without attachments.
     * @param subject The mail subject
     * @param htmlBody The HTML mail body. Can be null.
     * @param textBody The text mail body. Can be null.
     * @param recipient The recipient address
     * @param sender The sender address
     * @param host The server mail host
     * @param port The server port
     * @param securityProtocol The server mail security protocol
     * @param user The user name
     * @param password The user password
     * @throws MessagingException If an error occurred while preparing or sending email
     */
    public static void sendMail(String subject, String htmlBody, String textBody, String recipient, String sender, String host, long port, String securityProtocol, String user, String password) throws MessagingException
    {
        try
        {
            sendMail(subject, htmlBody, textBody, null, recipient, sender, null, null, false, false, host, port, securityProtocol, user, password);
        }
        catch (IOException e)
        {
            // Should never happen, as IOException can only be thrown where there are attachments.
            _LOGGER.error("Cannot send mail " + subject + " to " + recipient, e);
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
        sendMail(subject, htmlBody, textBody, attachments, recipient, sender, null, null);
    }
    
    /**
     * Sends mail without authentication, with attachments.
     * @param subject The mail subject
     * @param htmlBody The HTML mail body. Can be null.
     * @param textBody The text mail body. Can be null.
     * @param attachments the file attachments. Can be null.
     * @param recipient The recipient address
     * @param sender The sender address
     * @param cc Carbon copy adress list. Can be null.
     * @param bcc Blind carbon copy adress list. Can be null.
     * @throws MessagingException If an error occurred while preparing or sending email
     * @throws IOException if an error occurs while attaching a file.
     */
    public static void sendMail(String subject, String htmlBody, String textBody, Collection<File> attachments, String recipient, String sender, List<String> cc, List<String> bcc) throws MessagingException, IOException
    {
        sendMail(subject, htmlBody, textBody, attachments, recipient, sender, cc, bcc, false, false);
    }
    
    /**
     * Sends mail without authentication, with attachments.
     * @param subject The mail subject
     * @param htmlBody The HTML mail body. Can be null.
     * @param textBody The text mail body. Can be null.
     * @param attachments the file attachments. Can be null.
     * @param recipient The recipient address
     * @param sender The sender address
     * @param cc Carbon copy adress list. Can be null.
     * @param bcc Blind carbon copy adress list. Can be null.
     * @param deliveryReceipt true to request that the receiving mail server send a notification when the mail is received.
     * @param readReceipt true to request that the receiving mail client send a notification when the person opens the mail.
     * @throws MessagingException If an error occurred while preparing or sending email
     * @throws IOException if an error occurs while attaching a file.
     */
    public static void sendMail(String subject, String htmlBody, String textBody, Collection<File> attachments, String recipient, String sender, List<String> cc, List<String> bcc, boolean deliveryReceipt, boolean readReceipt) throws MessagingException, IOException
    {
        String smtpHost = Config.getInstance().getValueAsString("smtp.mail.host");
        long smtpPort = Config.getInstance().getValueAsLong("smtp.mail.port");
        String protocol = Config.getInstance().getValueAsString("smtp.mail.security.protocol");
        
        sendMail(subject, htmlBody, textBody, attachments, recipient, sender, cc, bcc, deliveryReceipt, readReceipt, smtpHost, smtpPort, protocol, null, null);
    }

    /**
     * Sends mail without authentication, with attachments.
     * @param subject The mail subject
     * @param htmlBody The HTML mail body. Can be null.
     * @param textBody The text mail body. Can be null.
     * @param attachments the file attachments. Can be null.
     * @param recipient The recipient address
     * @param sender The sender address
     * @param cc Carbon copy adress list. Can be null.
     * @param bcc Blind carbon copy adress list. Can be null.
     * @param deliveryReceipt true to request that the receiving mail server send a notification when the mail is received.
     * @param readReceipt true to request that the receiving mail client send a notification when the person opens the mail.
     * @param host The server mail host
     * @param port The server port
     * @param securityProtocol The server mail security protocol
     * @throws MessagingException If an error occurred while preparing or sending email
     * @throws IOException if an error occurs while attaching a file.
     */
    public static void sendMail(String subject, String htmlBody, String textBody, Collection<File> attachments, String recipient, String sender, List<String> cc, List<String> bcc, boolean deliveryReceipt, boolean readReceipt, String host, long port, String securityProtocol) throws MessagingException, IOException
    {
        String protocol = Config.getInstance().getValueAsString("smtp.mail.security.protocol");
        sendMail(subject, htmlBody, textBody, attachments, recipient, sender, cc, bcc, deliveryReceipt, readReceipt, host, port, protocol, null, null);
    }
    /**
     * Sends mail with authentication and attachments.
     * @param subject The mail subject
     * @param htmlBody The HTML mail body. Can be null.
     * @param textBody The text mail body. Can be null.
     * @param attachments the file attachments. Can be null.
     * @param recipient The recipient address
     * @param sender The sender address. Can be null when called by MailChecker.
     * @param cc Carbon copy adress list. Can be null.
     * @param bcc Blind carbon copy adress list. Can be null.
     * @param deliveryReceipt true to request that the receiving mail server send a notification when the mail is received.
     * @param readReceipt true to request that the receiving mail client send a notification when the person opens the mail.
     * @param host The server mail host. Can be null when called by MailChecker.
     * @param securityProtocol the security protocol to use when transporting the email
     * @param port The server port
     * @param user The user name
     * @param password The user password
     * @throws MessagingException If an error occurred while preparing or sending email
     * @throws IOException if an error occurs while attaching a file.
     */
    public static void sendMail(String subject, String htmlBody, String textBody, Collection<File> attachments, String recipient, String sender, List<String> cc, List<String> bcc, boolean deliveryReceipt, boolean readReceipt, String host, long port, String securityProtocol, String user, String password) throws MessagingException, IOException
    {
        Properties props = new Properties();

        // Setup mail server
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        
        // Security protocol
        if (securityProtocol.equals("starttls"))
        {
            props.put("mail.smtp.starttls.enable", "true"); 
        }
        else if (securityProtocol.equals("tlsssl"))
        {
            props.put("mail.smtp.ssl.enable", "true");
        }
        
        Session session = Session.getInstance(props, null);
        // Define message
        MimeMessage message = new MimeMessage(session);
        if (sender != null)
        {
            message.setFrom(new InternetAddress(sender));
        }
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
            htmlBodyPart.setContent(inlineCSS(htmlBody), "text/html;charset=utf-8");
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

        // Recipients
        if (recipient != null)
        {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient, false));
        }
        
        // Carbon copies
        if (cc != null)
        {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(StringUtils.join(cc, ','), false));
        }

        // Blind carbon copies
        if (bcc != null)
        {
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(StringUtils.join(bcc, ','), false));
        }
        
        // Delivery receipt : Return-Receipt-To
        if (deliveryReceipt)
        {
            message.setHeader("Return-Receipt-To", sender);
        }
        
        // Read receipt : Disposition-Notification-To
        if (readReceipt)
        {
            message.setHeader("Disposition-Notification-To", sender);
        }
        
        Transport tr = session.getTransport("smtp");
        try 
        {
            if (StringUtils.isNotBlank(user))
            {
                tr.connect(host, (int) port, user, password);
            }
            else
            {
                tr.connect(host, (int) port, null, null);
            }
            message.saveChanges();

            if (recipient != null && sender != null)
            {
                tr.sendMessage(message, message.getAllRecipients());
            }
            
            tr.close();
        }
        catch (MessagingException e)
        {
            throw new MessagingException(e.getMessage(), e);
        }
    }

    /**
     * This method inline css in &lt;style&gt; tags directly in the appropriates tags. e.g. : <style>h1 {color: red;}</style> <h1>a</h1> becomes <h1 style="color: red">a</h1>
     * @param html The initial non null html
     * @return The inlined html
     */
    public static String inlineCSS(String html)
    {
        Document doc = Jsoup.parse(html); 
        Elements els = doc.select("style");

        for (Element e : els) 
        { 
            String styleRules = e.getAllElements().get(0).data();
            styleRules = styleRules.replaceAll("\t|\n", "").replaceAll("<!--", "").replaceAll("-->", "");

            styleRules = _removeComments(styleRules);

            styleRules = styleRules.trim();

            StringTokenizer st = new StringTokenizer(styleRules, "{}"); 
            while (st.countTokens() > 1) 
            { 
                String selectors = st.nextToken();
                String properties = st.nextToken();

                String[] selector = selectors.split(",");
                for (String s : selector)
                {
                    if (StringUtils.isNotBlank(s) && !s.contains(":"))
                    {
                        try
                        {
                            Elements selectedElements = doc.select(s); 
                            for (Element selElem : selectedElements) 
                            { 
                                String oldProperties = selElem.attr("style"); 
                                selElem.attr("style", oldProperties.length() > 0 ? concatenateProperties(oldProperties, properties) : properties); 
                            } 
                        }
                        catch (Selector.SelectorParseException ex)
                        {
                            _LOGGER.error("Cannot inline CSS. Ignoring this rule and continuing.", ex);
                        }
                    }
                }
            } 
            e.remove(); 
        }

        return doc.toString();
    }

    private static String _removeComments(String styleRules)
    {
        int i = styleRules.indexOf("/*");
        int j = styleRules.indexOf("*/");

        if (i >= 0 && j > i)
        {
            return styleRules.substring(0, i) + _removeComments(styleRules.substring(j + 2));
        }

        return styleRules;
    }

    private static String concatenateProperties(String oldProp, String newProp) 
    { 
        String between = "";
        if (!newProp.endsWith(";"))
        {
            between += ";";
        }
        return newProp + between + oldProp.trim(); // The existing (old) properties should take precedence. 
    } 
}
