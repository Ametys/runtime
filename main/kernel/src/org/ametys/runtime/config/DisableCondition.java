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
package org.ametys.runtime.config;

/**
 * A disable condition for config parameters 
 */
public class DisableCondition
{
    /**
     * The available operators
     */
    public enum OPERATOR
    {
        /** Equals */
        EQ,
        /** Non equals */
        NEQ,
        /** Greater than */
        GT,
        /** Greater or equals */
        GEQ,
        /** Less or equals */ 
        LEQ,
        /** Less than */
        LT
    }
    
    private final String _id;
    private final OPERATOR _operator;
    private final String _value;
    
    /**
     * Creates a condition
     * @param id The parameter id
     * @param operator comparison operator of the condition ('eq'...)
     * @param value value to compare to
     */
    public DisableCondition(String id, OPERATOR operator, String value)
    {
        _id = id;
        _operator = operator;
        _value = value;
    }

    /**
     * Get the id
     * @return the parameter identifier
     */
    public String getId()
    {
        return _id;
    }

    /**
     * Get the operator
     * @return The comparison operator
     */
    public OPERATOR getOperator()
    {
        return _operator;
    }

    /**
     * Get the value
     * @return The value to compare to
     */
    public String getValue()
    {
        return _value;
    }
}
