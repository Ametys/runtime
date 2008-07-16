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
package org.ametys.runtime.util.parameter;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.avalon.framework.logger.Logger;

import org.ametys.runtime.util.LoggerFactory;


/**
 * This class handles all needed to use typed parameters
 */
public final class ParameterHelper
{
    /** Enumeration of supported types */
    public static enum TYPE 
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
        BINARY
    }
    /** Constants to type values as string : boolean */
    public static final String TYPE_BOOLEAN_LABEL = "boolean";

    /** Constants to type values as string : string */
    public static final String TYPE_STRING_LABEL = "string";

    /** Constants to type values as string : password */
    public static final String TYPE_PASSWORD_LABEL = "password";

    /** Constants to type values as string : long */
    public static final String TYPE_LONG_LABEL = "long";

    /** Constants to type values as string : double */
    public static final String TYPE_DOUBLE_LABEL = "double";

    /** Constants to type values as string : date */
    public static final String TYPE_DATE_LABEL = "date";

    /** Constants to set date format */
    public static final String TYPE_DATE_TYPE = "yyyy-MM-dd'T'HH:mm";
    
    /** Constants to type values as string : binary */
    public static final String TYPE_BINARY_LABEL = "binary";

    // Logger for traces
    private static Logger _logger = LoggerFactory.getLoggerFor(ParameterHelper.class);

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
    public static String typeToString(TYPE type)
    {
        if (type == TYPE.BOOLEAN)
        {
            return TYPE_BOOLEAN_LABEL;
        }
        else if (type == TYPE.DATE)
        {
            return TYPE_DATE_LABEL;
        }
        else if (type == TYPE.DOUBLE)
        {
            return TYPE_DOUBLE_LABEL;
        }
        else if (type == TYPE.LONG)
        {
            return TYPE_LONG_LABEL;
        }
        else if (type == TYPE.STRING)
        {
            return TYPE_STRING_LABEL;
        }
        else if (type == TYPE.PASSWORD)
        {
            return TYPE_PASSWORD_LABEL;
        }
        else if (type == TYPE.BINARY)
        {
            return TYPE_BINARY_LABEL;
        }
        else
        {
            throw new IllegalArgumentException("The value '" + type + "' is unknown for a config parameter type");
        }
    }
    
    /**
     * Convert a string containing a type to its value
     * 
     * @param type Name of the type
     * @return Type 
     * @throws IllegalArgumentException if the type is unknown
     */
    public static TYPE stringToType(String type)
    {
        if (TYPE_BOOLEAN_LABEL.equalsIgnoreCase(type))
        {
            return TYPE.BOOLEAN;
        }
        else if (TYPE_STRING_LABEL.equalsIgnoreCase(type))
        {
            return TYPE.STRING;
        }
        else if (TYPE_PASSWORD_LABEL.equalsIgnoreCase(type))
        {
            return TYPE.PASSWORD;
        }
        else if (TYPE_LONG_LABEL.equalsIgnoreCase(type))
        {
            return TYPE.LONG;
        }
        else if (TYPE_DOUBLE_LABEL.equalsIgnoreCase(type))
        {
            return TYPE.DOUBLE;
        }
        else if (TYPE_DATE_LABEL.equalsIgnoreCase(type))
        {
            return TYPE.DATE;
        }
        else if (TYPE_BINARY_LABEL.equalsIgnoreCase(type))
        {
            return TYPE.BINARY;
        }
        else
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
    public static Object castValue(String value, TYPE type)
    {
        if (value == null)
        {
            return null;
        }

        try
        {
            if (type == TYPE.BOOLEAN)
            {
                return new Boolean(value);
            }
            else if (type == TYPE.STRING)
            {
                return value;
            }
            else if (type == TYPE.PASSWORD)
            {
                return value;
            }
            else if (type == TYPE.LONG)
            {
                return new Long(value);
            }
            else if (type == TYPE.DOUBLE)
            {
                return new Double(value);
            }
            else if (type == TYPE.DATE)
            {
                return new SimpleDateFormat(TYPE_DATE_TYPE).parse(value);
            }
            else if (type == TYPE.BINARY)
            {
                return null;
            }
            else
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
            return null;
        }
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
            return new SimpleDateFormat(TYPE_DATE_TYPE).format((Date) value);
        }
        
        if (value instanceof InputStream)
        {
            throw new IllegalArgumentException("The object to convert is a input stream");
        }

        return value.toString();
    }
}
