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
package org.ametys.core.group;

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
