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
package org.ametys.runtime.ui.manager;

import java.util.Map;
import java.util.Set;

import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.ui.item.Interaction;
import org.ametys.runtime.ui.item.part.Action;
import org.ametys.runtime.ui.item.part.IconSet;
import org.ametys.runtime.ui.item.part.Shortcut;
import org.ametys.runtime.util.I18nizableText;


/**
 * This utility class contains helper to sax ui items 
 * @deprecated
 */
public final class SaxUIHelper
{
    private SaxUIHelper()
    {
        // nothing
    }
    
    /**
     * SAX an interaction
     * @param handler The sax content handler
     * @param interaction The interaction to SAX
     * @throws SAXException If an error occured
     */
    public static void toSAX(ContentHandler handler, Interaction interaction) throws SAXException
    {
        AttributesImpl mainAttrs = new AttributesImpl();
        if (!interaction.isEnabled())
        {
            mainAttrs.addAttribute("", "disabled", "disabled", "CDATA", "disabled");
        }
        XMLUtils.startElement(handler, "UIItem", mainAttrs);
        
        toSAX(handler, interaction.getLabel(), "Label");
        toSAX(handler, interaction.getDescription(), "Description");
        toSAX(handler, interaction.getIconSet(), "Icons");
        toSAX(handler, interaction.getAction(), "Action");
        toSAX(handler, interaction.getShortcut(), "Shortcut");
        
        XMLUtils.endElement(handler, "UIItem");
    }
    
    /**
     * SAX a test
     * @param handler The sax content handler
     * @param text The text to sax. May be null.
     * @param tagName The tag name to create
     * @throws SAXException If an error occured
     */
    public static void toSAX(ContentHandler handler, I18nizableText text, String tagName) throws SAXException
    {
        if (text != null)
        {
            text.toSAX(handler, tagName);
        }
    }
    
    /**
     * SAX an iconset
     * @param handler The sax content handler
     * @param iconSet The icon set to sax
     * @param tagName The tag name to create
     * @throws SAXException If an error occured
     */
    public static void toSAX(ContentHandler handler, IconSet iconSet, String tagName) throws SAXException
    {
        if (iconSet == null)
        {
            return;
        }
        
        XMLUtils.startElement(handler, tagName);
        
        XMLUtils.createElement(handler, "Small", iconSet.getSmallIconPath());
        XMLUtils.createElement(handler, "Medium", iconSet.getMediumIconPath());
        XMLUtils.createElement(handler, "Large", iconSet.getLargeIconPath());
        
        XMLUtils.endElement(handler, tagName);
    }
    
    /**
     * SAX an action
     * @param handler The sax content handler
     * @param action The action to sax
     * @param tagName The tag name to create
     * @throws SAXException If an error occured
     */
    public static void toSAX(ContentHandler handler, Action action, String tagName) throws SAXException
    {
        if (action == null)
        {
            return;
        }
        
        AttributesImpl mainAttrs = new AttributesImpl();
        if (action.getPlugin() != null)
        {
            mainAttrs.addAttribute("", "plugin", "plugin", "CDATA", action.getPlugin());
        }
        XMLUtils.startElement(handler, tagName, mainAttrs);
        
        XMLUtils.createElement(handler, "ClassName", action.getScriptClassname());
        
        XMLUtils.startElement(handler, "Parameters");
        Map<String, String> parameters = action.getParameters();
        for (String parameterName : parameters.keySet())
        {
            String parameterValue = parameters.get(parameterName);
            XMLUtils.createElement(handler, parameterName, parameterValue);
        }
        XMLUtils.endElement(handler, "Parameters");
        
        XMLUtils.startElement(handler, "Imports");
        Set<String> importFiles = action.getScriptImports();
        for (String importFile : importFiles)
        {
            XMLUtils.createElement(handler, "Import", importFile);
        }
        XMLUtils.endElement(handler, "Imports");
        
        XMLUtils.endElement(handler, tagName);
    }
    
    /**
     * SAX a shortcut
     * @param handler The sax content handler
     * @param shortcut The shortcut to sax
     * @param tagName The tag name to create
     * @throws SAXException If an error occured
     */
    public static void toSAX(ContentHandler handler, Shortcut shortcut, String tagName) throws SAXException
    {
        if (shortcut == null)
        {
            return;
        }
        
        AttributesImpl attrs = new AttributesImpl();
        
        if (shortcut.hasCtrl())
        {
            attrs.addAttribute("", "CTRL", "CTRL", "CDATA", "CTRL");
        }
        
        if (shortcut.hasAlt())
        {
            attrs.addAttribute("", "ALT", "ALT", "CDATA", "ALT");
        }

        if (shortcut.hasShift())
        {
            attrs.addAttribute("", "SHIFT", "SHIFT", "CDATA", "SHIFT");
        }
        
        XMLUtils.createElement(handler, tagName, attrs, shortcut.getKey());
    }
}
