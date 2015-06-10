/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.runtime.plugins.admin.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.core.ui.Callable;
import org.ametys.core.util.I18nUtils;
import org.ametys.core.util.I18nizableText;

/**
 * Helper for manipulating system announcement 
 */
public class SystemHelper extends AbstractLogEnabled implements Component, Serviceable, Contextualizable
{
    /** The relative path to the file where system information are saved (announcement, maintenance...) */
    public static final String ADMINISTRATOR_SYSTEM_FILE = "WEB-INF/data/administrator/system.xml";
    /** Avalon role */
    public static final String ROLE = SystemHelper.class.getName();
    
    private org.apache.cocoon.environment.Context _environmentContext;
    private I18nUtils _i18nUtils;
    private Context _context;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _i18nUtils = (I18nUtils) serviceManager.lookup(I18nUtils.ROLE);
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
        _environmentContext = (org.apache.cocoon.environment.Context) context.get(org.apache.cocoon.Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    /**
     * Enables or disable system announcement
     * @param available true to enable system announcement
     * @throws ProcessingException if an error occurred
     */
    @Callable
    public void setAnnouncementAvailable (boolean available) throws ProcessingException
    {
        SystemAnnouncement systemAnnouncement = readValues(_environmentContext.getRealPath("/"));
        
        String contextPath = _environmentContext.getRealPath("/");
        _save(contextPath, available, systemAnnouncement.getMessages());
    }
    
    /**
     * Add or edit a system announcement
     * @param language the language typed in by the user or "*" if modifying the default message
     * @param message the message to add nor edit
     * @param override true to override the existing value if exists
     * @return the result map
     * @throws Exception if an exception occurs 
     */
    @Callable
    public Map<String, Object> editAnnouncement(String language, String message, boolean override) throws Exception
    {
        Map<String, Object> result = new HashMap<String, Object> ();
        
        SystemAnnouncement sytemAnnouncement = readValues(_environmentContext.getRealPath("/"));
        
        Map<String, String> messages = sytemAnnouncement.getMessages();
        if (messages.containsKey(language) && !override)
        {
            result.put("already-exists", true);
            return result;
        }
        
        // Add or edit message
        messages.put(language, message);
        
        String contextPath = _environmentContext.getRealPath("/");
        _save(contextPath, sytemAnnouncement.isAvailable(), messages);
        
        return result;
    }
    
    /**
     * Delete a announcement 
     * @param language the language of the announcement to delete
     * @throws ProcessingException if an exception occurs
     * @return an empty map
     */
    @Callable
    public Map deleteAnnouncement(String language) throws ProcessingException
    {
        Map<String, Object> result = new HashMap<String, Object> ();
        
        SystemAnnouncement sytemAnnouncement = readValues(_environmentContext.getRealPath("/"));
        
        Map<String, String> messages = sytemAnnouncement.getMessages();
        if (messages.containsKey(language))
        {
            messages.remove(language);
            
            String contextPath = _environmentContext.getRealPath("/");
            _save(contextPath, sytemAnnouncement.isAvailable(), messages);
        }
        
        return result;
    }
    
    /**
     * Saves the system announcement's values
     * @param contextPath The context path
     * @param state true to enable system announcement
     * @param messages the messages
     * @throws ProcessingException if an error ocurred
     */
    private void _save (String contextPath, boolean state, Map<String, String> messages) throws ProcessingException
    {
        File systemFile = new File(contextPath, ADMINISTRATOR_SYSTEM_FILE);
        
        OutputStream os = null;
        try
        {
            // Create file if not exists
            if (!systemFile.exists())
            {
                systemFile.getParentFile().mkdirs();
                systemFile.createNewFile();
            }
            
            // create a transformer for saving sax into a file
            TransformerHandler th = ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();

            // create the result where to write
            os = new FileOutputStream(systemFile);
            StreamResult sResult = new StreamResult(os);
            th.setResult(sResult);

            // create the format of result
            Properties format = new Properties();
            format.put(OutputKeys.METHOD, "xml");
            format.put(OutputKeys.INDENT, "yes");
            format.put(OutputKeys.ENCODING, "UTF-8");
            th.getTransformer().setOutputProperties(format);

            // Send SAX events
            th.startDocument();

            AttributesImpl announcementsAttrs = new AttributesImpl();
            announcementsAttrs.addAttribute("", "state", "state", "CDATA", state ? "on" : "off");
            XMLUtils.startElement(th, "announcements", announcementsAttrs);
            
            for (String id : messages.keySet())
            {
                AttributesImpl announcementAttrs = new AttributesImpl();
                if (!"*".equals(id))
                {
                    announcementAttrs.addAttribute("", "lang", "lang", "CDATA", id);
                }
                
                XMLUtils.createElement(th, "announcement", announcementAttrs, messages.get(id));
            }
            
            XMLUtils.endElement(th, "announcements");
            
            th.endDocument();
        }
        catch (Exception e)
        {
            throw new ProcessingException("Unable to save system announcement values", e);
        }
        finally
        {
            IOUtils.closeQuietly(os);
        }
        
    }
    
    /**
     * Tests if system announcements are active.
     * @param contextPath the webapp context path
     * @return true if system announcements are active.
     */
    public boolean isSystemAnnouncementAvailable(String contextPath)
    {
        SystemAnnouncement systemAnnouncement = readValues(contextPath);
        return systemAnnouncement.isAvailable();
    }
    
    /**
     * Return the date of the last modification of the annonce
     * @param contextPath the webapp context path
     * @return The date of the last modification or 0 if there is no announce file
     */
    public long getSystemAnnoucementLastModificationDate(String contextPath)
    {
        try
        {
            File systemFile = new File(contextPath, ADMINISTRATOR_SYSTEM_FILE);
            if (!systemFile.exists() || !systemFile.isFile())
            {
                return 0;
            }
            
            return systemFile.lastModified();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to get system announcements", e);
        }
    }

    /**
     * Returns the system announcement for the given language code, or for the default language code if there is no specified announcement for the given language code.<br>
     * Returns null if the system announcements are not activated.
     * @param languageCode the desired language code of the system announcement
     * @param contextPath the webapp context path
     * @return the system announcement in the specified language code, or in the default language code, or null if announcements are not active.
     */
    public String getSystemAnnouncement(String languageCode, String contextPath)
    {
        SystemAnnouncement systemAnnouncement = readValues(contextPath);
        
        if (!systemAnnouncement.isAvailable())
        {
            return null;
        }
        
        Map<String, String> messages = systemAnnouncement.getMessages();
        
        String announcement = null;
        if (messages.containsKey(languageCode))
        {
            announcement = messages.get(languageCode);
        }
        
        if (StringUtils.isEmpty(announcement))
        {
            String defaultAnnouncement = messages.containsKey("*") ? messages.get("*") : null;
            if (StringUtils.isEmpty(defaultAnnouncement))
            {
                throw new IllegalStateException("There must be a default announcement.");
            }
            
            return defaultAnnouncement;
        }
        
        return announcement;
    }
    
    /**
     * Read the system announcement's values
     * @param contextPath The application context path
     * @return The system announcement values;
     */
    public SystemAnnouncement readValues (String contextPath)
    {
        SystemAnnouncement announcement = new SystemAnnouncement();
        
        try 
        {
            File systemFile = new File(contextPath, ADMINISTRATOR_SYSTEM_FILE);
            if (!systemFile.exists() || !systemFile.isFile())
            {
                _setDefaultValues(contextPath);
            }
            
            Configuration configuration;
            try (InputStream is = new FileInputStream(systemFile))
            {
                configuration = new DefaultConfigurationBuilder().build(is);
            }
            
            // State
            String state = configuration.getAttribute("state", "off");
            boolean isAvailable = "on".equals(state);
            announcement.setAvailable(isAvailable);
            
            // Announcements
            for (Configuration announcementConfiguration : configuration.getChildren("announcement"))
            {
                String lang = announcementConfiguration.getAttribute("lang", "*");
                String message = announcementConfiguration.getValue();
                
                announcement.addMessage(lang, message);
            }
            
            return announcement;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to get system announcements", e);
        }
    }
    
    private void _setDefaultValues (String contextPath) throws ProcessingException
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        Locale locale = org.apache.cocoon.i18n.I18nUtils.findLocale(objectModel, "locale", null, Locale.getDefault(), true);
        String defaultMessage = _i18nUtils.translate(new I18nizableText("plugin.core", "PLUGINS_ADMIN_SYSTEM_DEFAULTMESSAGE"), locale.getLanguage());

        Map<String, String> messages = new HashMap<> ();
        messages.put("*", defaultMessage);
        
        _save(contextPath, false, messages);
    }
    
    /**
     * Class representing the system announcement file
     */
    public class SystemAnnouncement
    {
        private boolean _available;
        private Map<String, String> _messages;
        
        /**
         * Constructor
         */
        public SystemAnnouncement()
        {
            _available = false;
            _messages = new HashMap<String, String>();
        }
        
        /**
         * Is the system announcement available ?
         * @return true if the system announcement is available, false otherwise
         */
        public boolean isAvailable()
        {
            return _available;
        }
        
        /**
         * Get the messages by language
         * @return the messages by languaga
         */
        public Map<String, String> getMessages ()
        {
            return _messages;
        }
        
        /**
         * Set the availability of the system announcement
         * @param available true to set the system announcement available, false otherwise
         */
        public void setAvailable (boolean available)
        {
            _available = available;
        }
        
        /**
         * Add a message to the list of announcements
         * @param lang the language of the message
         * @param message the message itself
         */
        public void addMessage (String lang, String message)
        {
            _messages.put(lang, message);
        }
        
    }
}
