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
package org.ametys.core.userpref;

/**
 * User preferences exception, used whenever there is an error specific to user preferences.
 */
public class UserPreferencesException extends Exception
{

    /**
     * User preferences exception.
     */
    public UserPreferencesException()
    {
        super();
    }
    
    /**
     * User preferences exception.
     * @param message the message.
     * @param cause the cause.
     */
    public UserPreferencesException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    /**
     * User preferences exception.
     * @param message the message.
     */
    public UserPreferencesException(String message)
    {
        super(message);
    }
    
    /**
     * User preferences exception.
     * @param cause the cause.
     */
    public UserPreferencesException(Throwable cause)
    {
        super(cause);
    }

}
