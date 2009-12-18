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
package org.ametys.runtime.util;

import java.util.List;

import org.apache.cocoon.transformation.I18nTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
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
     * @param catalogue the catalogue where the key is defined. Can be null. Can be overloaded by the catalogue in the key.
     * @param key the key of the text. Cannot be null. May include the catalogue using the caracter ':' as separator. CATALOG:KEY.
     */
    public I18nizableText(String catalogue, String key)
    {
        this(catalogue, key, null);
    }
    
    /**
     * Create an i18nized text
     * @param catalogue the catalogue where the key is defined. Can be null. Can be overloaded by the catalogue in the key.
     * @param key the key of the text. Cannot be null. May include the catalogue using the caracter ':' as separator. CATALOG:KEY.
     * @param parameters the parameters of the key if any. Can be null.
     */
    public I18nizableText(String catalogue, String key, List<String> parameters)
    {
        _i18n = true;
        
        String i18nKey = key.substring(key.indexOf(":") + 1);
        String i18nCatalogue = i18nKey.length() == key.length() ? catalogue : key.substring(0, key.length() - i18nKey.length() - 1);

        _catalogue = i18nCatalogue;
        _key = i18nKey;
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
     * @throws SAXException if an error occurs
     */
    @SuppressWarnings("null")
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
            if (getCatalogue() != null)
            {
                atts.addCDATAAttribute(I18nTransformer.I18N_NAMESPACE_URI, "catalogue", "i18n:catalogue", getCatalogue());
            }
            
            handler.startElement(I18nTransformer.I18N_NAMESPACE_URI, "text", "i18n:text", atts);
            handler.endElement(I18nTransformer.I18N_NAMESPACE_URI, "text", "i18n:text");

            if (hasParameter)
            {
                for (String parameter : parameters)
                {
                    if (parameter != null)
                    {
                        handler.startElement(I18nTransformer.I18N_NAMESPACE_URI, "param", "i18n:param", new AttributesImpl());
                        handler.characters(parameter.toCharArray(), 0, parameter.length());
                        handler.endElement(I18nTransformer.I18N_NAMESPACE_URI, "param", "i18n:param");
                    }
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
     * @throws SAXException if an error occurs
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
    
    @Override
    public int hashCode()
    {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
        hashCodeBuilder.append(_i18n);
        hashCodeBuilder.append(_key);
        hashCodeBuilder.append(_catalogue);
        hashCodeBuilder.append(_directLabel);
        
        if (_parameters == null)
        {
            hashCodeBuilder.append((Object) null);
        }
        else
        {
            hashCodeBuilder.append(_parameters.toArray(new String[_parameters.size()]));
        }
        
        return hashCodeBuilder.toHashCode();
    }
    
    @Override
    public boolean equals(Object obj)
    { 
        if (obj == null)
        {
            return false;
        }

        if (!(obj instanceof I18nizableText))
        {
            return false;
        }
        
        if (this == obj)
        {
            return true;
        }
        
        I18nizableText i18nObj = (I18nizableText) obj;
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(_i18n, i18nObj._i18n);
        
        if (_i18n)
        {
            equalsBuilder.append(_key, i18nObj._key);
            equalsBuilder.append(_catalogue, i18nObj._catalogue);
            
            if (_parameters == null)
            {
                equalsBuilder.append(_parameters, i18nObj._parameters);
            }
            else
            {
                String[] otherParameters = null;
                
                if (i18nObj._parameters != null)
                {
                    otherParameters = i18nObj._parameters.toArray(new String[i18nObj._parameters.size()]);
                }
                
                equalsBuilder.append(_parameters.toArray(new String[_parameters.size()]),
                                     otherParameters);
            }
        }
        else
        {
            equalsBuilder.append(_directLabel, i18nObj._directLabel);
        }

        return equalsBuilder.isEquals();
    }
}
