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
package org.ametys.runtime.plugins.core.sqlmap.dao;

/**
 * Error on accessing data.
 */
public class DataAccessException extends RuntimeException
{
    /**
     * Constructs a new data access exception with the specified detail message.
     * @param message The detail message. 
     */
    public DataAccessException(String message)
    {
        super(message);
    }
    
    /**
     * Constructs a new data access exception with the specified detail message and
     * cause.
     * @param message The detail messag.
     * @param cause The cause.
     */
    public DataAccessException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    /**
     * Constructs a new data access exception with the specified cause.
     * @param cause The cause.
     */
    public DataAccessException(Throwable cause)
    {
        super(cause);
    }
}
