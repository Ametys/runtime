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
package org.ametys.runtime.group;

/**
 * Exception for bad modification of the groups.
 */
public class InvalidModificationException extends Exception
{
    /**
     * Default constructor.
     */
    public InvalidModificationException()
    {
        // Nothing to do
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
      * Constructor with a cause.
      * @param cause The cause.
      */
    public InvalidModificationException(Exception cause)
    {
        super(cause);
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
}
