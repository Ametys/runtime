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
package org.ametys.runtime.i18n;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
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
    private final boolean _i18n;
    private String _directLabel;
    private String _key;
    private String _catalogue;
    private List<String> _parameters;
    private Map<String, I18nizableText> _parameterMap;
    private String _catalogueLocation;
    private String _catalogueBundleName;

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
     * @param key the key of the text. Cannot be null. May include the catalogue using the character ':' as separator. CATALOG:KEY.
     */
    public I18nizableText(String catalogue, String key)
    {
        this(catalogue, key, (List<String>) null);
    }

    /**
     * Create an i18nized text with ordered parameters.
     * @param catalogue the catalogue where the key is defined. Can be null. Can be overloaded by the catalogue in the key.
     * @param key the key of the text. Cannot be null. May include the catalogue using the character ':' as separator. CATALOG:KEY.
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
        _parameterMap = null;
    }

    /**
     * Create an i18nized text with named parameters.
     * @param catalogue the catalogue where the key is defined. Can be null. Can be overloaded by the catalogue in the key.
     * @param key the key of the text. Cannot be null. May include the catalogue using the character ':' as separator. CATALOG:KEY.
     * @param parameters the named parameters of the message, as a Map of name -&gt; value. Value can itself be an i18n key but must not have parameters.
     */
    public I18nizableText(String catalogue, String key, Map<String, I18nizableText> parameters)
    {
        _i18n = true;

        String i18nKey = key.substring(key.indexOf(":") + 1);
        String i18nCatalogue = i18nKey.length() == key.length() ? catalogue : key.substring(0, key.length() - i18nKey.length() - 1);

        _catalogue = i18nCatalogue;
        _key = i18nKey;
        _parameterMap = parameters;
        _parameters = null;
    }
    
    /**
     * Create an i18nized text. 
     * Use this constructor only when the catalogue is an external catalogue, not managed by Ametys application
     * @param catalogueLocation the file location URI of the catalogue where the key is defined. 
     * @param catalogueFilename the catalogue bundle name such as 'messages'
     * @param key the key of the text. Cannot be null.
     */
    public I18nizableText(String catalogueLocation, String catalogueFilename, String key)
    {
        this(catalogueLocation, catalogueFilename, key, (List<String>) null);
    }

    /**
     * Create an i18nized text with ordered parameters.
     * Use this constructor only when the catalogue is an external catalogue, not managed by Ametys application
     * @param catalogueLocation the file location URI of the catalogue where the key is defined. 
     * @param catalogueFilename the catalogue bundle name such as 'messages'
     * @param key the key of the text. Cannot be null.
     * @param parameters the parameters of the key if any. Can be null.
     */
    public I18nizableText(String catalogueLocation, String catalogueFilename, String key, List<String> parameters)
    {
        _i18n = true;

        _catalogueLocation = catalogueLocation;
        _catalogueBundleName = catalogueFilename;
        _catalogue = null;
        _key = key;
        _parameters = parameters;
        _parameterMap = null;
    }

    /**
     * Create an i18nized text with named parameters.
     * @param catalogueLocation the file location URI of the catalogue where the key is defined. 
     * @param catalogueFilename the catalogue bundle name such as 'messages'
     * @param key the key of the text. Cannot be null.
     * @param parameters the named parameters of the message, as a Map of name -&gt; value. Value can itself be an i18n key but must not have parameters.
     */
    public I18nizableText(String catalogueLocation, String catalogueFilename, String key, Map<String, I18nizableText> parameters)
    {
        _i18n = true;

        _catalogueLocation = catalogueLocation;
        _catalogueBundleName = catalogueFilename;
        _catalogue = null;
        _key = key;
        _parameterMap = parameters;
        _parameters = null;
    }

    /**
     * Determine whether the text is i18nized or a simple cross languages text.
     * @return true if the text is i18nized and so defined by a catalogue, a key and optionaly parameters.<br>false if the text is a simple label
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
     * Get the file location URI of the i18nized text.
     * @return The catalog location where the key is defined
     */
    public String getLocation()
    {
        if (_i18n)
        {
            return _catalogueLocation;
        }
        else
        {
            throw new IllegalArgumentException("This text is not i18nized and so do not have location. Use the 'isI18n' method to know whether a text is i18nized");
        }
    }
    
    /**
     * Get the files name of catalog
     * @return bundle name
     */
    public String getBundleName()
    {
        if (_i18n)
        {
            return _catalogueBundleName;
        }
        else
        {
            throw new IllegalArgumentException("This text is not i18nized and so do not have location. Use the 'isI18n' method to know whether a text is i18nized");
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
     * Get the parameters of the key of the i18nized text.
     * @return The list of parameters' values or null if there is no parameters
     */
    public Map<String, I18nizableText> getParameterMap()
    {
        if (_i18n)
        {
            return _parameterMap;
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
    public void toSAX(ContentHandler handler) throws SAXException
    {
        if (isI18n())
        {
            List<String> parameters = getParameters();
            Map<String, I18nizableText> parameterMap = getParameterMap();
            boolean hasParameter = parameters != null && parameters.size() > 0 || parameterMap != null && !parameterMap.isEmpty();

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
                if (parameters != null)
                {
                    // Ordered parameters version.
                    for (String parameter : parameters)
                    {
                        if (parameter != null)
                        {
                            handler.startElement(I18nTransformer.I18N_NAMESPACE_URI, "param", "i18n:param", new AttributesImpl());
                            handler.characters(parameter.toCharArray(), 0, parameter.length());
                            handler.endElement(I18nTransformer.I18N_NAMESPACE_URI, "param", "i18n:param");
                        }
                    }
                }
                else if (parameterMap != null)
                {
                    // Named parameters version.
                    for (String parameterName : parameterMap.keySet())
                    {
                        I18nizableText value = parameterMap.get(parameterName);
                        AttributesImpl attrs = new AttributesImpl();
                        attrs.addCDATAAttribute("name", parameterName);
                        handler.startElement(I18nTransformer.I18N_NAMESPACE_URI, "param", "i18n:param", attrs);
                        value._toSAXAsParam(handler);
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

    private void _toSAXAsParam(ContentHandler handler) throws SAXException
    {
        if (isI18n())
        {
            AttributesImpl atts = new AttributesImpl();
            if (getCatalogue() != null)
            {
                atts.addCDATAAttribute(I18nTransformer.I18N_NAMESPACE_URI, "catalogue", "i18n:catalogue", getCatalogue());
            }
    
            handler.startElement(I18nTransformer.I18N_NAMESPACE_URI, "text", "i18n:text", atts);
            handler.characters(_key.toCharArray(), 0, _key.length());
            handler.endElement(I18nTransformer.I18N_NAMESPACE_URI, "text", "i18n:text");
        }
        else
        {
            handler.characters(getLabel().toCharArray(), 0, getLabel().length());
        }
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
        hashCodeBuilder.append(_catalogueLocation);
        hashCodeBuilder.append(_catalogueBundleName);
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

        hashCodeBuilder.append(_parameterMap);

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
            
            if (_catalogue == null)
            {
                equalsBuilder.append(_catalogueLocation, i18nObj._catalogueLocation);
                equalsBuilder.append(_catalogueBundleName, i18nObj._catalogueBundleName);
            }
            else
            {
                equalsBuilder.append(_catalogue, i18nObj._catalogue);
            }

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

            equalsBuilder.append(_parameterMap, i18nObj.getParameterMap());
        }
        else
        {
            equalsBuilder.append(_directLabel, i18nObj._directLabel);
        }

        return equalsBuilder.isEquals();
    }

    private static boolean isI18n(Configuration config)
    {
        return config.getAttributeAsBoolean("i18n", false) || config.getAttribute("type", "").equals("i18n");
    }

    /**
     * Get an i18n text configuration (can be a key or a "direct" string).
     * @param config The configuration to parse.
     * @param catalogueLocation The i18n catalogue location URI
     * @param catalogueFilename The i18n catalogue bundle name
     * @param value The i18n text, can be a key or a "direct" string.
     * @return The i18nizable text
     */
    private static I18nizableText getI18nizableTextValue(Configuration config, String catalogueLocation, String catalogueFilename, String value)
    {
        return new I18nizableText(catalogueLocation, catalogueFilename, value);
    }

    /**
     * Get an i18n text configuration (can be a key or a "direct" string).
     * @param config The configuration to parse.
     * @param defaultCatalogue The i18n catalogue to use when not specified.  
     * @param value The i18n text, can be a key or a "direct" string.
     * @return The i18nizable text
     */
    public static I18nizableText getI18nizableTextValue(Configuration config, String defaultCatalogue, String value)
    {
        if (I18nizableText.isI18n(config))
        {
            String catalogue = config.getAttribute("catalogue", defaultCatalogue);
            
            return new I18nizableText(catalogue, value);
        }
        else
        {
            return new I18nizableText(value);
        }
    }

    /**
     * Parse a i18n text configuration.
     * @param config the configuration to use.
     * @param catalogueLocation The i18n catalogue location URI
     * @param catalogueFilename The i18n catalogue bundle name
     * @param defaultValue The default value key in configuration
     * @return the i18n text.
     * @throws ConfigurationException if the configuration is not valid.
     */
    public static I18nizableText parseI18nizableText(Configuration config, String catalogueLocation, String catalogueFilename, String defaultValue) throws ConfigurationException
    {
        String text = config.getValue(defaultValue);
        return I18nizableText.getI18nizableTextValue(config, catalogueLocation, catalogueFilename, text);
    }

    /**
     * Parse an optional i18n text configuration, with a default value.
     * @param config the configuration to use.
     * @param defaultCatalogue the i18n catalogue to use when not specified. 
     * @param defaultValue the default value key in configuration.
     * @return the i18n text.
     */
    public static I18nizableText parseI18nizableText(Configuration config, String defaultCatalogue, String defaultValue)
    {
        String text = config.getValue(defaultValue);
        return I18nizableText.getI18nizableTextValue(config, defaultCatalogue, text);
    }

    /**
     * Parse a mandatory i18n text configuration, throwing an exception if empty.
     * @param config the configuration to use.
     * @param defaultCatalogue the i18n catalogue to use when not specified.
     * @return the i18n text.
     * @throws ConfigurationException if the configuration is not valid.
     */
    public static I18nizableText parseI18nizableText(Configuration config, String defaultCatalogue) throws ConfigurationException
    {
        String text = config.getValue();
        return I18nizableText.getI18nizableTextValue(config, defaultCatalogue, text);
    }
    
    /**
     * Gets a string representation of a i18n text
     * @param i18nizableText The i18 text
     * @return The string representation of the i18n text
     */
    public static String i18nizableTextToString(I18nizableText i18nizableText)
    {
        Map<String, Object> map = new HashMap<>();
        if (i18nizableText.isI18n())
        {
            map.put("key", i18nizableText.getKey());
            map.put("catalogue", i18nizableText.getCatalogue());
            map.put("parameters", i18nizableText.getParameters());
        }
        else
        {
            map.put("label", i18nizableText.getLabel());
        }
        
        // Use Java Bean XMLEncoder
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (XMLEncoder xmlEncoder = new XMLEncoder(bos))
        {
            xmlEncoder.writeObject(map);
            xmlEncoder.flush();
        }
        return bos.toString();
    }
    
    /**
     * Returns the i18n text from its string representation
     * @param str The string representation of the i18n text
     * @return The i18n text
     */
    @SuppressWarnings("unchecked")
    public static I18nizableText stringToI18nizableText(String str)
    {
        Map<String, Object> map;
        // Use Java Bean XMLDecoder
        try (XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(str.getBytes())))
        {
            map = (Map<String, Object>) xmlDecoder.readObject();
        }
        
        if (map.get("label") != null)
        {
            return new I18nizableText((String) map.get("label"));
        }
        else
        {
            String key = (String) map.get("key");
            String catalogue = (String) map.get("catalogue");
            List<String> parameters = (List) map.get("parameters");
            return new I18nizableText(catalogue, key, parameters);
        }
    }
}
