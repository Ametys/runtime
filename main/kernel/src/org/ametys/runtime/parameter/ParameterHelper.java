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
package org.ametys.runtime.parameter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.i18n.I18nizableText;


/**
 * This class handles all needed to use typed parameters
 */
public final class ParameterHelper
{
    /** Enumeration of supported types */
    public static enum ParameterType
    {
        /** boolean values */
        BOOLEAN,
        /** string values */
        STRING,
        /** password values */
        PASSWORD,
        /** long values */
        LONG,
        /** double values */
        DOUBLE,
        /** date values */
        DATE,
        /** binary values */
        BINARY,
        /** datasource values */
        DATASOURCE
    }

    // Logger for traces
    private static Logger _logger = LoggerFactory.getLogger(ParameterHelper.class);

    private ParameterHelper ()
    {
        // empty
    }
    
    /**
     * Return the readable name of a type
     * 
     * @param type Type to convert
     * @return Returns the name of the type
     * @throws IllegalArgumentException If the type is unknwon
     */
    public static String typeToString(ParameterType type)
    {
        return type.name().toLowerCase();
    }
    
    /**
     * Convert a string containing a type to its value
     * 
     * @param type Name of the type
     * @return Type
     * @throws IllegalArgumentException if the type is unknown
     */
    public static ParameterType stringToType(String type)
    {
        try
        {
            return ParameterType.valueOf(type.toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The type '" + type + "' is unknown for config parameters");
        }
    }
    
    /**
     * Cast a untyped value (string) to an object of the type
     * 
     * @param value Value to cast
     * @param type Type to cast value in
     * @return An object of the type 'type' with value 'value', or null if type
     *         is unknown or value cannot be cast
     */
    public static Object castValue(String value, ParameterType type)
    {
        
        if (value == null)
        {
            return null;
        }

        try
        {
            if (type == ParameterType.BOOLEAN)
            {
                return new Boolean(value);
            }
            else if (type == ParameterType.STRING)
            {
                return value;
            }
            else if (type == ParameterType.PASSWORD)
            {
                return value;
            }
            else if (type == ParameterType.DATASOURCE)
            {
                return value;
            }
            else if (type == ParameterType.LONG)
            {
                return  new Long(value);
            }
            else if (type == ParameterType.DOUBLE)
            {
                return new Double(value);
            }
            else if (type == ParameterType.DATE)
            {
                return ISODateTimeFormat.dateTime().parseDateTime(value).toDate();
            }
            else if (type == ParameterType.BINARY)
            {
                return null;
            }
        }
        catch (Exception nfe)
        {
            if (value.length() != 0)
            {
                _logger.error("Cannot cast value '" + value + "' into type '" + typeToString(type) + "'. Null object will be used.", nfe);
            }
            else if (_logger.isDebugEnabled())
            {
                _logger.debug("Failed to cast empty string to type '" + typeToString(type) + "'. Null object will be used.", nfe);
            }
        }
        return null;
    }
    

    /**
     * Converts known types to string
     * 
     * @param value Typed value
     * @return String readable by the config bean
     * @throws IllegalArgumentException if the object is a InputStream
     */
    public static String valueToString(Object value)
    {
        if (value == null)
        {
            return null;
        }

        if (value instanceof Date)
        {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime((Date) value);
            calendar.setTimeZone(DateTimeZone.getDefault().toTimeZone());
            
            DateTime date = new DateTime(calendar.get(Calendar.YEAR),
                                         calendar.get(Calendar.MONTH) + 1,
                                         calendar.get(Calendar.DAY_OF_MONTH),
                                         calendar.get(Calendar.HOUR_OF_DAY),
                                         calendar.get(Calendar.MINUTE),
                                         calendar.get(Calendar.SECOND),
                                         calendar.get(Calendar.MILLISECOND));
            
            return ISODateTimeFormat.dateTime().print(date);
        }
        
        if (value instanceof InputStream)
        {
            throw new IllegalArgumentException("The object to convert is an input stream");
        }

        return value.toString();
    }
    
    /**
     * SAX a parameter
     * @param handler The content handler where to SAX
     * @param parameter The parameter to SAX
     * @param value The parameter value. Can be null.
     * @throws SAXException If an error occurred while SAXing
     * @throws ProcessingException If an error occurred
     */
    public static void toSAXParameter (ContentHandler handler, Parameter parameter, Object value) throws SAXException, ProcessingException
    {
        AttributesImpl parameterAttr = new AttributesImpl();
        parameterAttr.addAttribute("", "plugin", "plugin", "CDATA", parameter.getPluginName());
        XMLUtils.startElement(handler, parameter.getId(), parameterAttr);
        
        toSAXParameterInternal(handler, parameter, value);
        
        XMLUtils.endElement(handler, parameter.getId());
    }

    /**
     * SAX a parameter except the root tag
     * @param handler The content handler where to SAX
     * @param parameter The parameter to SAX
     * @param value The parameter value. Can be null.
     * @throws SAXException If an error occurred while SAXing
     * @throws ProcessingException If an error occurred
     */
    public static void toSAXParameterInternal(ContentHandler handler, Parameter parameter, Object value) throws SAXException, ProcessingException
    {
        parameter.getLabel().toSAX(handler, "label");
        parameter.getDescription().toSAX(handler, "description");
        
        XMLUtils.createElement(handler, "type", ParameterHelper.typeToString((ParameterType) parameter.getType()));
        
        Object defaultValue = parameter.getDefaultValue();
        
        if (defaultValue != null)
        {
            XMLUtils.createElement(handler, "default-value", ParameterHelper.valueToString(defaultValue));
        }
        
        if (value != null)
        {
            XMLUtils.createElement(handler, "value", ParameterHelper.valueToString(value));
        }
        
        if (parameter.getWidget() != null)
        {
            XMLUtils.createElement(handler, "widget", parameter.getWidget());
        }
        
        Map<String, I18nizableText> widgetParameters = parameter.getWidgetParameters();
        if (widgetParameters.size() > 0)
        {
            XMLUtils.startElement(handler, "widget-params");
            for (String paramName : widgetParameters.keySet())
            {
                XMLUtils.startElement(handler, paramName);
                widgetParameters.get(paramName).toSAX(handler);
                XMLUtils.endElement(handler, paramName);
            }
            XMLUtils.endElement(handler, "widget-params");
        }
        
        Enumerator enumerator = parameter.getEnumerator();
        if (enumerator != null)
        {
            toSAXEnumerator(handler, enumerator);
        }
        
        Validator validator = parameter.getValidator();
        toSAXValidator(handler, validator);
    }
    
    /**
     * SAX parameter enumerator
     * @param handler The content handler where to SAX
     * @param enumerator The enumerator to SAX
     * @throws SAXException If an error occurred to SAX
     * @throws ProcessingException If an error occurred
     */
    public static void toSAXEnumerator (ContentHandler handler, Enumerator enumerator) throws SAXException, ProcessingException
    {
        XMLUtils.startElement(handler, "enumeration");
        
        try
        {
            for (Map.Entry<Object, I18nizableText> entry : enumerator.getEntries().entrySet())
            {
                String valueAsString = ParameterHelper.valueToString(entry.getKey());
                I18nizableText label = entry.getValue();

                // Generate option
                AttributesImpl attrs = new AttributesImpl();
                attrs.addCDATAAttribute("value", valueAsString);
                
                XMLUtils.startElement(handler, "option", attrs);
                
                if (label != null)
                {
                    label.toSAX(handler);
                }
                else
                {
                    XMLUtils.data(handler, valueAsString);
                }
                
                XMLUtils.endElement(handler, "option");
            }
        }
        catch (Exception e)
        {
            throw new ProcessingException("Unable to enumerate entries with enumerator: " + enumerator, e);
        }

        XMLUtils.endElement(handler, "enumeration");
    }
    
    /**
     * SAX parameter validator
     * @param handler The content handler where to SAX
     * @param validator The validator to SAX
     * @throws SAXException If an error occurred while SAXing
     */
    public static void toSAXValidator (ContentHandler handler, Validator validator) throws SAXException
    {
        if (validator != null)
        {
            XMLUtils.startElement(handler, "validation");
            validator.saxConfiguration(handler);
            XMLUtils.endElement(handler, "validation");
        }
    }
    
    /**
     * Convert the parameter in a JSON map
     * @param parameter The parameter to convert
     * @return The Parameter as a map
     * @throws Exception If an error occured when converting the parameter
     */
    public static Map<String, Object> toJSON(Parameter parameter) throws Exception
    {
        Map<String, Object> result = new HashMap<>();
        
        result.put("id", parameter.getId());
        result.put("type", parameter.getType());
        result.put("default-value", parameter.getDefaultValue());
        result.put("description", parameter.getDescription());
        result.put("label", parameter.getLabel());
        result.put("plugin", parameter.getPluginName());
        
        if (parameter.getValidator() != null)
        {
            result.put("validation", parameter.getValidator().toJson());
        }
        
        if (parameter.getEnumerator() != null)
        {
            List<Map<String, Object>> enumeration = new ArrayList<>();
            
            Map<Object, I18nizableText> entries = parameter.getEnumerator().getEntries();
            for (Object entryKey : entries.keySet())
            {
                Map<String, Object> option = new HashMap<>();
                option.put("value", entryKey);
                option.put("label", entries.get(entryKey));
                enumeration.add(option);
            }
            
            result.put("enumeration", enumeration);
        }
        
        result.put("widget", parameter.getWidget());
        result.put("widget-params", parameter.getWidgetParameters());
        
        return result;
    }
}
