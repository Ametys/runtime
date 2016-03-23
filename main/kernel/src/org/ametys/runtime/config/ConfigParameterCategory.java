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
package org.ametys.runtime.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterCheckerDescriptor;

/**
 * Represents a category of configuration parameters
 */
public class ConfigParameterCategory
{
    private Map<I18nizableText, ConfigParameterGroup> _groups;
    private Set<ConfigParameterCheckerDescriptor> _paramCheckers;

    ConfigParameterCategory ()
    {
        _groups = new HashMap<>();
        _paramCheckers = new HashSet<>();
    }
    
    void addParamChecker(ConfigParameterCheckerDescriptor paramChecker)
    {
        _paramCheckers.add(paramChecker);
    }

    /**
     * Returns all {@link ConfigParameterGroup} for this category.
     * @return all {@link ConfigParameterGroup} for this category.
     */
    public Map<I18nizableText, ConfigParameterGroup> getGroups()
    {
        return _groups;
    }
    
    /**
     * Set the configuration groups of this category
     * @param groups the configuration groups
     */
    void setGroups(Map<I18nizableText, ConfigParameterGroup> groups)
    {
        this._groups = groups;
    }
    
    /**
     * Returns all {@link ParameterCheckerDescriptor} for this category.
     * @return all {@link ParameterCheckerDescriptor} for this category.
     */
    public Set<ConfigParameterCheckerDescriptor> getParamCheckers()
    {
        return _paramCheckers;
    }
    
    /**
     * Set the parameter checkers of this category
     * @param paramCheckers the parameter checkers
     */
    void setParamCheckers(Set<ConfigParameterCheckerDescriptor> paramCheckers)
    {
        this._paramCheckers = paramCheckers;
    }
}
