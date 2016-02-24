/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.core.datasource;

/**
 * Exception representing the absence of a data source
 */
public class UnknownDataSourceException extends RuntimeException
{
    /**
     * Constructor without arguments
     */
    public UnknownDataSourceException() 
    {
        super();
    }
    
    /**
     * Constructor with a message
     * @param msg The exception message
     */
    public UnknownDataSourceException(String msg) 
    {
        super(msg);
    }
    
    /**
     * Constructor with the message and the cause
     * @param cause the cause of the exception
     */
    public UnknownDataSourceException(Throwable cause) 
    {
        super(cause);
    }
    
    /**
     * Constructor with the message and the cause
     * @param msg the exception message 
     * @param cause the cause of the exception
     */
    public UnknownDataSourceException(String msg, Throwable cause) 
    {
        super(msg, cause);
    }
}
