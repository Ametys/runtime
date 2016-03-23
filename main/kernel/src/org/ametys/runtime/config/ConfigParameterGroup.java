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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterCheckerDescriptor;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * Represent a group of parameters.
 */
public class ConfigParameterGroup
{
    private Map<String, ConfigParameter> _groupParams;
    private String _switcher;
    private I18nizableText _groupLabel;
    private Set<ConfigParameterCheckerDescriptor> _paramCheckers;
    
    /**
     * Create a group
     * @param groupLabel The label of the group
     */
    public ConfigParameterGroup (I18nizableText groupLabel)
    {
        _groupLabel = groupLabel;
        _groupParams = new TreeMap<>();
        _switcher = null;
        _paramCheckers = new HashSet<>();
    }
    
    void addParam(ConfigParameter param)
    {
        String id = param.getId();
        _groupParams.put(id, param);
        
        if (param.isGroupSwitch())
        {
            if (_switcher == null)
            {
                _switcher = id;
                if (param.getType() != ParameterType.BOOLEAN)
                {
                    throw new RuntimeException("The group '" + _groupLabel.toString() + "' has a switch '" + _switcher + "' that is not valid because it is not a boolean.");
                }
            }
            else
            {
                throw new RuntimeException("At least two group-switches have been defined for the configuration group '" + _groupLabel.toString() + "'. These parameters are '" + _switcher + "' and '" + param.getId() + "'.");
            }
        }
    }
    
    void addParamChecker(ConfigParameterCheckerDescriptor paramChecker)
    {
        _paramCheckers.add(paramChecker);
    }
    
    /**
     * Returns the {@link ParameterCheckerDescriptor}s associated with this group.
     * @return the {@link ParameterCheckerDescriptor}s associated with this group.
     */
    public Set<ConfigParameterCheckerDescriptor> getParamCheckers()
    {
        return _paramCheckers;
    }
    
    /**
     * Returns the label.
     * @return the label.
     */
    public I18nizableText getLabel()
    {
        return _groupLabel;
    }
    
    /**
     * Returns the {@link ConfigParameter} contained in this group.
     * @param withSwitch if the returned parameters should contains the group switcher, if any.
     * @return the {@link ConfigParameter} contained in this group.
     */
    public Set<ConfigParameter> getParams(boolean withSwitch)
    {
        if (withSwitch)
        {
            return new TreeSet<>(_groupParams.values());
        }
        else
        {
            Map<String, ConfigParameter> groupParams = new TreeMap<>(_groupParams);
            
            if (_switcher != null)
            {
                groupParams.remove(_switcher);
            }
            
            return new TreeSet<>(groupParams.values());
        }
    }
    
    /**
     * Returns the named {@link ConfigParameter}.
     * @param id the id to retrieve.
     * @return the named {@link ConfigParameter}.
     */
    public ConfigParameter getParameter(String id)
    {
        return _groupParams.get(id);
    }
    
    /**
     * Returns the group switch, if any.
     * @return the group switch, if any.
     */
    public String getSwitch()
    {
        return _switcher;
    }
}
