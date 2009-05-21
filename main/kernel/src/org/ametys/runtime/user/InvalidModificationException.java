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

import java.util.Set;

/**
 * Exception for bad modification of the users list.
 */
public class InvalidModificationException extends Exception
{
    Set<String> _fields;
    
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
    public InvalidModificationException(Set<String> fields)
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
    public InvalidModificationException(String message, Set<String> fields)
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
     * Contructor with cause and error fields
     * @param cause the cause
     * @param fields The fields is having errors
     */
    public InvalidModificationException(Exception cause, Set<String> fields)
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
     * Cosntructor with a message, a cause and a set of error fields
     * @param message The message.
     * @param cause The cause.
     * @param fields The fields is having errors
     */
    public InvalidModificationException(String message, Exception cause, Set<String> fields)
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
            for (String field : _fields)
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
    public Set<String> getFields()
    {
        return _fields;
    }
}
