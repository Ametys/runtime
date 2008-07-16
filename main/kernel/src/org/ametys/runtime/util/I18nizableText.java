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
package org.ametys.runtime.util;

import java.util.List;

import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class wraps a text that <i>may</i> be internationalized. 
 */
public final class I18nizableText
{
    private boolean _i18n;
    private String _directLabel;
    private String _key;
    private String _catalogue;
    private List<String> _parameters;

    /**
     * Create a simple international text
     * @param label The text. Cannot be null.
     */
    public I18nizableText(String label)
    {
        _i18n = false;
        _directLabel = label;
    }
    
    /**
     * Create an i18nized text
     * @param catalogue the catalogue where the key is defined. Cannot be null.
     * @param key the key of the text. Cannot be null.
     */
    public I18nizableText(String catalogue, String key)
    {
        this(catalogue, key, null);
    }
    
    /**
     * Create an i18nized text
     * @param catalogue the catalogue where the key is defined. Cannot be null.
     * @param key the key of the text. Cannot be null.
     * @param parameters the parameters of the key if any. Can be null.
     */
    public I18nizableText(String catalogue, String key, List<String> parameters)
    {
        _i18n = true;
        _catalogue = catalogue;
        _key = key;
        _parameters = parameters;
    }
    
    /**
     * Determine whether the text is i18nized or a simple cross languages text.
     * @return true if the text is i18nized and so defined by a catalogue, a key and optionaly parameters.<br/>false if the text is a simple label
     */
    public boolean isI18n()
    {
        return _i18n;
    }
    
    /**
     * Get the catalogue of the i18nized text.
     * @return The catalogue where the key is defined
     */
    public String getCatalogue()
    {
        if (_i18n)
        {
            return _catalogue;
        }
        else
        {
            throw new IllegalArgumentException("This text is not i18nized and so do not have catalogue. Use the 'isI18n' method to know whether a text is i18nized");
        }
    }
    
    /**
     * Get the key of the i18nized text.
     * @return The key in the catalogue
     */
    public String getKey()
    {
        if (_i18n)
        {
            return _key;
        }
        else
        {
            throw new IllegalArgumentException("This text is not i18nized and so do not have key. Use the 'isI18n' method to know whether a text is i18nized");
        }
    }
    
    /**
     * Get the parameters of the key of the i18nized text.
     * @return The list of parameters' values or null if there is no parameters
     */
    public List<String> getParameters()
    {
        if (_i18n)
        {
            return _parameters;
        }
        else
        {
            throw new IllegalArgumentException("This text is not i18nized and so do not have parameters. Use the 'isI18n' method to know whether a text is i18nized");
        }
    }
    
    /**
     * Get the label if a text is not i18nized.
     * @return The label
     */
    public String getLabel()
    {
        if (!_i18n)
        {
            return _directLabel;
        }
        else
        {
            throw new IllegalArgumentException("This text is i18nized and so do not have label. Use the 'isI18n' method to know whether a text is i18nized");
        }
    }
    
    /**
     * SAX a text
     * @param handler The sax content handler
     * @param tagName The tag name
     * @throws SAXException
     */
    public void toSAX(ContentHandler handler, String tagName) throws SAXException
    {
        if (isI18n())
        {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("", "i18n", "i18n", "CDATA", "true");
            XMLUtils.startElement(handler, tagName, attrs);
            
            XMLUtils.createElement(handler, "catalogue", getCatalogue());
            XMLUtils.createElement(handler, "key", getKey());

            List<String> parameters = getParameters();
            if (parameters != null && parameters.size() > 0)
            {
                XMLUtils.startElement(handler, "parameters");
                
                for (String parameter : parameters)
                {
                    XMLUtils.createElement(handler, "parameter", parameter);
                }
                
                XMLUtils.endElement(handler, "parameters");
            }

            XMLUtils.endElement(handler, tagName);
        }
        else
        {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("", "i18n", "i18n", "CDATA", "false");
            XMLUtils.createElement(handler, tagName, attrs, getLabel());
        }
    }
}
