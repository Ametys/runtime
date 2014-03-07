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
package org.ametys.runtime.right;

/**
 * Common exception for all rights related problems.
 */
public class RightsException extends RuntimeException
{
    /**
     * Constructs a new exception with the specified detail message.
     * @param message the detail message.
     */
    public RightsException(String message)
    {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause.
     */
    public RightsException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     * @param cause the specified cause.
     */
    public RightsException(Throwable cause)
    {
        super(cause);
    }
}
