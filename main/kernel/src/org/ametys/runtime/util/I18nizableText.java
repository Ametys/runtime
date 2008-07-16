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

import org.apache.cocoon.transformation.I18nTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

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
     * @throws SAXException
     */
    public void toSAX(ContentHandler handler) throws SAXException
    {
        if (isI18n())
        {
            List<String> parameters = getParameters();
            boolean hasParameter = parameters != null && parameters.size() > 0;
            
            handler.startPrefixMapping("i18n", I18nTransformer.I18N_NAMESPACE_URI);
            
            if (hasParameter)
            {
                handler.startElement(I18nTransformer.I18N_NAMESPACE_URI, "translate", "i18n:translate", new AttributesImpl());
            }
            
            AttributesImpl atts = new AttributesImpl();
            atts.addCDATAAttribute(I18nTransformer.I18N_NAMESPACE_URI, "key", "i18n:key", getKey());
            atts.addCDATAAttribute(I18nTransformer.I18N_NAMESPACE_URI, "catalogue", "i18n:catalogue", getCatalogue());
            
            handler.startElement(I18nTransformer.I18N_NAMESPACE_URI, "text", "i18n:text", atts);
            handler.endElement(I18nTransformer.I18N_NAMESPACE_URI, "text", "i18n:text");

            if (hasParameter)
            {
                for (String parameter : parameters)
                {
                    
                    handler.startElement(I18nTransformer.I18N_NAMESPACE_URI, "param", "i18n:param", new AttributesImpl());
                    handler.characters(parameter.toCharArray(), 0, parameter.length());
                    handler.endElement(I18nTransformer.I18N_NAMESPACE_URI, "param", "i18n:param");
                }
                
                handler.endElement(I18nTransformer.I18N_NAMESPACE_URI, "translate", "i18n:translate");
            }
            
            handler.endPrefixMapping("i18n");
        }
        else
        {
            handler.characters(getLabel().toCharArray(), 0, getLabel().length());
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
        XMLUtils.startElement(handler, tagName);
        toSAX(handler);
        XMLUtils.endElement(handler, tagName);
    }
    
    @Override
    public String toString()
    {
        String result = "";
        if (isI18n())
        {
            result += getCatalogue() + ":" + getKey();
            List<String> parameters = getParameters();
            if (parameters != null)
            {
                result += "[";
                boolean isFirst = true;
                for (String parameter : parameters)
                {
                    if (!isFirst)
                    {                        
                        result += "; param : " + parameter;
                    }
                    else
                    {                        
                        result += "param : " + parameter;
                        isFirst = false;
                    }
                }
                result += "]";
            }
        }
        else
        {
            result = getLabel();
        }
        return result;
    }
}
