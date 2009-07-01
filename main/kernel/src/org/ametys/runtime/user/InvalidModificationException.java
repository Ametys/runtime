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
package org.ametys.runtime.user;

import java.util.Map;

import org.ametys.runtime.util.parameter.Errors;

/**
 * Exception for bad modification of the users list.
 */
public class InvalidModificationException extends Exception
{
    private Map<String, Errors>  _fields;
    
    /**
     * Default constructor.
     */
    public InvalidModificationException()
    {
        // Nothing to do
    }

    /**
     * Constructor with error fields
     * @param fields The fields is having errors
     */
    public InvalidModificationException(Map<String, Errors> fields)
    {
        _fields = fields;
    }

    /**
     * Constructor with a message.
     * @param message The message.
     */
    public InvalidModificationException(String message)
    {
        super(message);
    }

    /**
     * Constructor with a message and error fields
     * @param message the message
     * @param fields The fields is having errors
     */
    public InvalidModificationException(String message, Map<String, Errors> fields)
    {
        super(message);
        _fields = fields;
    }

     /**
      * Constructor with a cause.
      * @param cause The cause.
      */
    public InvalidModificationException(Exception cause)
    {
        super(cause);
    }

    /**
     * Constructor with cause and error fields
     * @param cause the cause
     * @param fields The fields is having errors
     */
    public InvalidModificationException(Exception cause, Map<String, Errors> fields)
    {
        super(cause);
        _fields = fields;
    }

     /**
      * Constructor with a message and a cause.
      * @param message The message.
      * @param cause The cause.
      */
    public InvalidModificationException(String message, Exception cause)
    {
        super(message, cause);
    }

    /**
     * Constructor with a message, a cause and a set of error fields
     * @param message The message.
     * @param cause The cause.
     * @param fields The fields is having errors
     */
    public InvalidModificationException(String message, Exception cause, Map<String, Errors> fields)
    {
        super(message, cause);
        _fields = fields;
    }
    
    @Override
    public String getMessage()
    {
        if (_fields != null && _fields.size() > 0)
        {
            StringBuffer fields = new StringBuffer(" [");
            for (String field : _fields.keySet())
            {
                if (fields.length() > 2)
                {
                    fields.append(", ");
                }
                fields.append(field);
            }
            fields.append("]");
        }
        return super.getMessage();
    }
    
    /**
     * Get the error fields
     * @return fields as a set of ids (may be null or empty)
     */
    public Map<String, Errors> getFieldErrors()
    {
        return _fields;
    }
}
