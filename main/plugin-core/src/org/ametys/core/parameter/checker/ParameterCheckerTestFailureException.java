/*
 *  Copyright 2014 Anyware Services
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
package org.ametys.core.parameter.checker;

/**
 * Class handling the parameter checkers' test failures.
 */
public class ParameterCheckerTestFailureException extends Throwable
{
    /**
     * Constructor without arguments
     */
    public ParameterCheckerTestFailureException() 
    {
        super();
    }
    
    /**
     * Constructor with a message
     * @param msg The exception message
     */
    public ParameterCheckerTestFailureException(String msg) 
    {
        super(msg);
    }
    
    /**
     * Constructor with the message and the cause
     * @param msg the message 
     * @param cause the cause
     */
    public ParameterCheckerTestFailureException(String msg, Throwable cause) 
    {
        super(msg, cause);
    }
    
    /**
     * Constructor with the message and the cause
     * @param cause the cause
     */
    public ParameterCheckerTestFailureException(Throwable cause) 
    {
        super(cause);
    }
}
