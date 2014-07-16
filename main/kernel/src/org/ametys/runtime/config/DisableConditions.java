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

import java.util.ArrayList;
import java.util.List;

/**
 * Disable conditions for config parameters.
 * Composed by a list of conditions or disable conditions and a association operator 
 */
public class DisableConditions
{
    /** Association between sub conditions*/
    public enum ASSOCIATION_TYPE
    {
        /** And */
        AND,
        /** Or */
        OR
    }
    
    private ASSOCIATION_TYPE _associationType;
    
    private final List<DisableConditions> _subConditionsList;
    
    private final List<DisableCondition> _conditionList;
    
    /**
     * Creates a disable conditions
     */
    public DisableConditions()
    {
        _associationType = ASSOCIATION_TYPE.AND;
        _subConditionsList = new ArrayList<DisableConditions>();
        _conditionList = new ArrayList<DisableCondition>();
    }
    
    /**
     * Get the conditions
     * @return The list of conditions
     */
    public List<DisableCondition> getConditions()
    {
        return _conditionList;
    }
    
    /**
     * Get the sub conditions
     * @return The list of underlying conditions
     */
    public List<DisableConditions> getSubConditions()
    {
        return _subConditionsList;
    }
    
    /**
     * Get the association
     * @return The association
     */
    public ASSOCIATION_TYPE getAssociationType()
    {
        return _associationType;
    }
    
    /**
     * Set the association
     * @param associationType The new association between underlying conditions
     */
    public void setAssociation(ASSOCIATION_TYPE associationType)
    {
        _associationType = associationType;
    }
}
