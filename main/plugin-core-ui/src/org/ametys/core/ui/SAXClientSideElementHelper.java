/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.core.ui;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.ui.ClientSideElement.Script;
import org.ametys.core.ui.ClientSideElement.ScriptFile;
import org.ametys.core.util.JSONUtils;

/**
 * This helper allow to sax a client side element
 */
public class SAXClientSideElementHelper extends AbstractLogEnabled implements Component, Serviceable, ThreadSafe
{
    /** Avalon role */
    public static final String ROLE = SAXClientSideElementHelper.class.getName();
    private JSONUtils _jsonUtils;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _jsonUtils = (JSONUtils) smanager.lookup(JSONUtils.ROLE);
    }
    /**
     * SAX a client side element
     * @param tagName the tag name to create
     * @param element The client side element to sax. Can not be null.
     * @param handler The handler where to sax
     * @param contextualParameters Contextuals parameters transmitted by the environment.
     * @throws SAXException If an error occured
     */
    public void saxDefinition(String tagName, ClientSideElement element, ContentHandler handler, Map<String, Object> contextualParameters) throws SAXException
    {
        List<Script> scripts = element.getScripts(contextualParameters);
        
        for (Script script : scripts)
        {
            AttributesImpl clientSideElementAttrs = new AttributesImpl();
            clientSideElementAttrs.addCDATAAttribute("id", script.getId());
            clientSideElementAttrs.addCDATAAttribute("serverId", script.getServerId());
            clientSideElementAttrs.addCDATAAttribute("plugin", element.getPluginName());
            XMLUtils.startElement(handler, tagName, clientSideElementAttrs);
            
            // Initial parameters
            Map<String, Object> parameters = script.getParameters();
            
            // SAX Action (classname and initial parameters)
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("class", script.getScriptClassname());
            XMLUtils.createElement(handler, "action", attrs, _jsonUtils.convertObjectToJson(parameters));
            
            // SAX Scripts
            XMLUtils.startElement(handler, "scripts");
            for (ScriptFile scriptFile : script.getScriptFiles())
            {
                saxScriptFile(handler, scriptFile);
            }
            XMLUtils.endElement(handler, "scripts");
    
            // SAX CSS
            XMLUtils.startElement(handler, "css");
            for (ScriptFile scriptFile : script.getCSSFiles())
            {
                saxScriptFile(handler, scriptFile);
            }
            XMLUtils.endElement(handler, "css");
    
            XMLUtils.endElement(handler, tagName);
        }
    }
    
    private void saxScriptFile(ContentHandler handler, ScriptFile scriptFile) throws SAXException
    {
        AttributesImpl fileAttrs = new AttributesImpl();
        String debugMode = scriptFile.getDebugMode();
        if (debugMode != null && !"all".equals(debugMode))
        {
            fileAttrs.addCDATAAttribute("debug", debugMode);
        }
        
        if (!scriptFile.isLangSpecific())
        {
            String rtlMode = scriptFile.getRtlMode();
            if (rtlMode != null && !"all".equals(rtlMode))
            {
                fileAttrs.addCDATAAttribute("rtl", rtlMode);
            }
            
            XMLUtils.createElement(handler, "file", fileAttrs, scriptFile.getPath());
        }
        else
        {
            fileAttrs.addCDATAAttribute("lang", "true");
            XMLUtils.startElement(handler, "file", fileAttrs);
            
            String defaultLang = scriptFile.getDefaultLang();
            Map<String, String> langPaths = scriptFile.getLangPaths();
            
            for (Entry<String, String> langPath : langPaths.entrySet())
            {
                AttributesImpl langAttrs = new AttributesImpl();
                
                String codeLang = langPath.getKey();
                langAttrs.addCDATAAttribute("code", codeLang);
                if (codeLang.equals(defaultLang))
                {
                    langAttrs.addCDATAAttribute("default", "true");
                }
                
                XMLUtils.createElement(handler, "lang", langAttrs, langPath.getValue());
            }

            XMLUtils.endElement(handler, "file");
        }
        
    }
}
