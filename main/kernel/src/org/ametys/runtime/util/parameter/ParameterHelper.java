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
        BINARY
    }

    /** Constants to set date format */
    public static final String TYPE_DATE_TYPE = "yyyy-MM-dd'T'HH:mm";

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
            else if (type == ParameterType.LONG)
            {
                return new Long(value);
            }
            else if (type == ParameterType.DOUBLE)
            {
                return new Double(value);
            }
            else if (type == ParameterType.DATE)
            {
                return new SimpleDateFormat(TYPE_DATE_TYPE).parse(value);
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
            return new SimpleDateFormat(TYPE_DATE_TYPE).format((Date) value);
        }
        
        if (value instanceof InputStream)
        {
            throw new IllegalArgumentException("The object to convert is a input stream");
        }

        return value.toString();
    }
}
