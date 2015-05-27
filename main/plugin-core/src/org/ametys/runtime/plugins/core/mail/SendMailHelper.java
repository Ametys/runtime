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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.apache.commons.lang3.StringUtils;
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

    /** Attribute selectors pattern for CSS specificity processing */
    protected static final Pattern __CSS_SPECIFICITY_ATTR_PATTERN = Pattern.compile("(\\[[^\\]]+\\])");
    /** ID selectors pattern for CSS specificity processing */
    protected static final Pattern __CSS_SPECIFICITY_ID_PATTERN = Pattern.compile("(#[^\\s\\+>~\\.\\[:]+)");
    /** Class selectors pattern for CSS specificity processing */
    protected static final Pattern __CSS_SPECIFICITY_CLASS_PATTERN = Pattern.compile("(\\.[^\\s\\+>~\\.\\[:]+)");
    /** Pseudo-element selectors pattern for CSS specificity processing */
    protected static final Pattern __CSS_SPECIFICITY_PSEUDO_ELEMENT_PATTERN = Pattern.compile("(::[^\\s\\+>~\\.\\[:]+|:first-line|:first-letter|:before|:after)", Pattern.CASE_INSENSITIVE);
    /** Pseudo-class (with bracket) selectors pattern for CSS specificity processing */
    protected static final Pattern __CSS_SPECIFICITY_PSEUDO_CLASS_WITH_BRACKETS_PATTERN = Pattern.compile("(:[\\w-]+\\([^\\)]*\\))", Pattern.CASE_INSENSITIVE);
    /** Pseudo-class selectors pattern for CSS specificity processing */
    protected static final Pattern __CSS_SPECIFICITY_PSEUDO_CLASS_PATTERN = Pattern.compile("(:[^\\s\\+>~\\.\\[:]+)");
    /** Element selectors pattern for CSS specificity processing */
    protected static final Pattern __CSS_SPECIFICITY_ELEMENT_PATTERN = Pattern.compile("([^\\s\\+>~\\.\\[:]+)");
    
    /** Specific :not pseudo-class selectors pattern for CSS specificity processing */
    protected static final Pattern __CSS_SPECIFICITY_PSEUDO_CLASS_NOT_PATTERN = Pattern.compile(":not\\(([^\\)]*)\\)");
    /** Universal and separator characters pattern for CSS specificity processing */
    protected static final Pattern __CSS_SPECIFICITY_UNIVERSAL_AND_SEPARATOR_PATTERN = Pattern.compile("[\\*\\s\\+>~]");
    
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
        
        message.saveChanges();
        
        Transport tr = session.getTransport("smtp");
        
        try
        {
            tr.connect(host, (int) port, StringUtils.trimToNull(user), StringUtils.trimToNull(password));
            
            if (recipient != null && sender != null)
            {
                tr.sendMessage(message, message.getAllRecipients());
            }
        }
        finally
        {
            tr.close();
        }
    }

    /**
     * This method inline css in &lt;style&gt; tags directly in the appropriates tags. e.g. : &lt;style&gt;h1 {color: red;}&lt;/style&gt; &lt;h1&gt;a&lt;/h1&gt; becomes &lt;h1 style="color: red"&gt;a&lt;/h1&gt;
     * @param html The initial non null html
     * @return The inlined html
     */
    public static String inlineCSS(String html)
    {
        List<CssRule> rules = new LinkedList<>();
        
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
                    if (StringUtils.isNotBlank(s))
                    {
                        rules.add(new CssRule(s, properties, rules.size()));
                    }
                }
            } 
            e.remove(); 
        }
        
        // Sort rules by specificity
        Collections.sort(rules, Collections.reverseOrder());
    
        for (CssRule rule : rules)
        {
            try
            {
                Elements selectedElements = doc.select(rule.getSelector());
                for (Element selElem : selectedElements)
                {
                    String oldProperties = selElem.attr("style");
                    selElem.attr("style", oldProperties.length() > 0 ? concatenateProperties(oldProperties, rule.getProperties()) : rule.getProperties());
                }
            }
            catch (Selector.SelectorParseException ex)
            {
                _LOGGER.error("Cannot inline CSS. Ignoring this rule and continuing.", ex);
            }
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
    
    private static class CssRule implements Comparable<CssRule>
    {
        private String _selector;
        private String _properties;
        private CssSpecificity _specificity;
        
        /**
         * CSSRule constructor
         * @param selector css selector
         * @param properties css properties for this rule
         * @param positionIdx The rules declaration index
         */
        public CssRule(String selector, String properties, int positionIdx)
        {
            _selector = selector;
            _properties = properties;
            _specificity = new CssSpecificity(_selector, positionIdx);
        }
        
        /**
         * Selector getter
         * @return the selector
         */
        public String getSelector()
        {
            return _selector;
        }
        
        /**
         * Properties getter
         * @return the properties
         */
        public String getProperties()
        {
            return _properties;
        }
        
        public int compareTo(CssRule r)
        {
            return _specificity.compareTo(r._specificity);
        }
    }
    
    private static class CssSpecificity implements Comparable<CssSpecificity>
    {
        private int[] _weights;
        
        public CssSpecificity(String selector, int positionIdx)
        {
            // Position index is used to differentiate equality cases
            // -> latest declaration should be the one applied
            _weights = new int[]{0, 0, 0, 0, positionIdx};
            
            String input = selector;
            
            // This part is loosely based on https://github.com/keeganstreet/specificity
            
            // Remove :not pseudo-class but leave its argument
            input = __CSS_SPECIFICITY_PSEUDO_CLASS_NOT_PATTERN.matcher(input).replaceAll(" $1 ");
            
            // The following regular expressions assume that selectors matching the preceding regular expressions have been removed
            input = _countReplaceAll(__CSS_SPECIFICITY_ATTR_PATTERN, input, 2);
            input = _countReplaceAll(__CSS_SPECIFICITY_ID_PATTERN, input, 1);
            input = _countReplaceAll(__CSS_SPECIFICITY_CLASS_PATTERN, input, 2);
            input = _countReplaceAll(__CSS_SPECIFICITY_PSEUDO_ELEMENT_PATTERN, input, 3);
            // A regex for pseudo classes with brackets - :nth-child(), :nth-last-child(), :nth-of-type(), :nth-last-type(), :lang()
            input = _countReplaceAll(__CSS_SPECIFICITY_PSEUDO_CLASS_WITH_BRACKETS_PATTERN, input, 2);
            // A regex for other pseudo classes, which don't have brackets
            input = _countReplaceAll(__CSS_SPECIFICITY_PSEUDO_CLASS_PATTERN, input, 2);
            
            // Remove universal selector and separator characters
            input = __CSS_SPECIFICITY_UNIVERSAL_AND_SEPARATOR_PATTERN.matcher(input).replaceAll(" ");
            
            _countReplaceAll(__CSS_SPECIFICITY_ELEMENT_PATTERN, input, 3);
        }
        
        private String _countReplaceAll(Pattern pattern, String selector, int sIndex)
        {
            Matcher m = pattern.matcher(selector);
            StringBuffer sb = new StringBuffer();
            
            while (m.find())
            {
                // Increment desired weight counter
                _weights[sIndex]++;
                
                // Replace matched selector part with whitespace
                m.appendReplacement(sb, " ");
            }
            
            m.appendTail(sb);
            
            return sb.toString();
        }
        
        public int compareTo(CssSpecificity o)
        {
            for (int i = 0; i < _weights.length; i++)
            {
                if (_weights[i] != o._weights[i])
                {
                    return _weights[i] - o._weights[i];
                }
            }
            
            return 0;
        }
    }
}
