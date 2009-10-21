/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.ui;

import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.ui.ClientSideElement.Script;
import org.ametys.runtime.util.I18nizableText;

/**
 * This helper allow to sax a client side element
 */
public class SAXClientSideElementHelper extends AbstractLogEnabled implements Component, Contextualizable, ThreadSafe
{
    /** Avalon role */
    public static final String ROLE = SAXClientSideElementHelper.class.getName();
    
    private Context _context;
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    /**
     * SAX a client side element
     * @param clientSideElementId The client side element id
     * @param tagName the tag name to create
     * @param element The client side element to sax. Can not be null.
     * @param handler The handler where to sax
     * @param contextualParameters Contextuals parameters transmitted by the environment.
     * @throws SAXException If an error occured
     */
    public void saxDefinition(String clientSideElementId, String tagName, ClientSideElement element, ContentHandler handler, Map<String, Object> contextualParameters) throws SAXException
    {
        Script script = element.getScript();
        
        if (script != null)
        {
            AttributesImpl clientSideElementAttrs = new AttributesImpl();
            clientSideElementAttrs.addCDATAAttribute("id", clientSideElementId);
            clientSideElementAttrs.addCDATAAttribute("plugin", element.getPluginName());
            XMLUtils.startElement(handler, tagName, clientSideElementAttrs);
            
            // SAX Action (classname and initial parameters)
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("class", script.getScriptClassname());
            XMLUtils.startElement(handler, "action", attrs);
            
            Map<String, I18nizableText> parameters = element.getParameters(contextualParameters);
            for (String paramName : parameters.keySet())
            {
                AttributesImpl paramAttrs = new AttributesImpl();
                paramAttrs.addCDATAAttribute("name", paramName);
                XMLUtils.startElement(handler, "param", paramAttrs);
                parameters.get(paramName).toSAX(handler);
                XMLUtils.endElement(handler, "param");
            }
            
            XMLUtils.endElement(handler, "action");
            
            // SAX Scripts
            XMLUtils.startElement(handler, "scripts");
            for (String fileName : script.getScriptFiles())
            {
                XMLUtils.createElement(handler, "file", fileName);
            }
            XMLUtils.endElement(handler, "scripts");
    
            // SAX CSS
            XMLUtils.startElement(handler, "css");
            for (String fileName : script.getCSSFiles())
            {
                XMLUtils.createElement(handler, "file", fileName);
            }
            XMLUtils.endElement(handler, "css");
    
            XMLUtils.endElement(handler, tagName);
        }
    }

    /**
     * SAX a client side element state
     * @param clientSideElementId The client side element id
     * @param tagName the tag name to create
     * @param element The client side element to sax. Can not be null.
     * @param handler The handler where to sax
     * @throws SAXException If an error occured
     */
    @SuppressWarnings("unchecked")
    public void saxCurrentState(String clientSideElementId, String tagName, ContextualClientSideElement element, ContentHandler handler) throws SAXException
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        Map<String, Object> jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);

        AttributesImpl clientSideElementAttrs = new AttributesImpl();
        clientSideElementAttrs.addCDATAAttribute("id", clientSideElementId);
        XMLUtils.startElement(handler, tagName, clientSideElementAttrs);
        
        Map<String, I18nizableText> parameters = element.getCurrentParameters(jsParameters);
        for (String paramName : parameters.keySet())
        {
            AttributesImpl paramAttrs = new AttributesImpl();
            paramAttrs.addCDATAAttribute("name", paramName);
            XMLUtils.startElement(handler, "param", paramAttrs);
            parameters.get(paramName).toSAX(handler);
            XMLUtils.endElement(handler, "param");
        }
        
        XMLUtils.endElement(handler, tagName);
    }
}
