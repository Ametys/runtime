/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.plugins.core.administrator.system;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.ModifiableSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This action update the system info.
 */
public class UpdateSystemAction extends AbstractAction
{
    private Pattern _langPattern = Pattern.compile("[a-zA-Z]{2}");
    
    private Request _request;
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        _request = ObjectModelHelper.getRequest(objectModel);
        
        // The map to keep the messages
        Map<String, String> messages = new HashMap<String, String>();
        
        // Is maintenance
        // boolean maintenace = "true".equals(_request.getParameter("maintenance"));
        
        // Is announcement
        boolean announcement = "true".equals(_request.getParameter("announcement"));
        
        // Get the language codes and announcement messages and check them
        if (!_getMessages(messages))
        {
            return null;
        }
        
        // Save the file
        ModifiableSource saveAnnouncementSource = null;
        OutputStream os = null;
        try
        {
            saveAnnouncementSource = (ModifiableSource) resolver.resolveURI("context://" + SystemHelper.ADMINISTRATOR_SYSTEM_FILE);
            
            os = saveAnnouncementSource.getOutputStream();
            
            _save(os, announcement, messages);
            
            // RuntimeServlet.setRunMode(maintenace ? RuntimeServlet.RunMode.MAINTENANCE : RuntimeServlet.RunMode.NORMAL);
        }
        catch (Exception e)
        {
            getLogger().error("An error occured while saving administrator's properties to file '" + SystemHelper.ADMINISTRATOR_SYSTEM_FILE + "'", e);
            return null;
        }
        finally 
        {
            if (os != null)
            {
                os.close();
            }
            if (saveAnnouncementSource != null)
            {
                resolver.release(saveAnnouncementSource);
            }
        }
        
        return EMPTY_MAP;
    }
    
    private boolean _getMessages(Map<String, String> messages)
    {
        String[] langCodes = _request.getParameterValues("lang");
        
        for (String lang : langCodes)
        {
            // Lang code
            if (lang == null)
            {
                getLogger().error("One of the administrator's announcements language code is not specified.");
                return false;
            }
            else if (!lang.equals("*") && !_langPattern.matcher(lang).matches())
            {
                getLogger().error("Incorrect language code encountered while saving administrator's announcements : '" + lang + "'.");
                return false;
            }

            // message
            String message = _request.getParameter("message_" + lang);
            if (message == null || message.length() == 0)
            {
                getLogger().error("No message specified for language '" + lang + "'. Unable to save administrator's announcements.");
                return false;
            }
            
            messages.put(lang, message);
        }
        
        return true;
    }

    private void _save(OutputStream os, boolean announcement, Map<String, String> messages) throws TransformerConfigurationException, TransformerFactoryConfigurationError, SAXException
    {
        // create a transformer for saving sax into a file
        TransformerHandler th = ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();
        // create the result where to write
        StreamResult sResult = new StreamResult(os);
        th.setResult(sResult);

        // create the format of result
        Properties format = new Properties();
        format.put(OutputKeys.METHOD, "xml");
        format.put(OutputKeys.INDENT, "yes");
        format.put(OutputKeys.ENCODING, "UTF-8");
        th.getTransformer().setOutputProperties(format);

        // Envoi des événements sax
        th.startDocument();

        AttributesImpl announcementsAttrs = new AttributesImpl();
        announcementsAttrs.addAttribute("", "state", "state", "CDATA", announcement ? "on" : "off");
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
}
