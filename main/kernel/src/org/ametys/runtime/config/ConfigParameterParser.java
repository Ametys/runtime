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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;
import org.ametys.runtime.util.parameter.AbstractParameterParser;
import org.ametys.runtime.util.parameter.Enumerator;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.util.parameter.Validator;

/**
 * This class parses the configuration parameters to help SAX them later.
 */
public class ConfigParameterParser extends AbstractParameterParser<ConfigParameter, ParameterType>
{
    /**
     * The configuration parameter parser constructor.
     * @param enumeratorManager
     * @param validatorManager
     */
    public ConfigParameterParser(ThreadSafeComponentManager<Enumerator> enumeratorManager, ThreadSafeComponentManager<Validator> validatorManager)
    {
        super(enumeratorManager, validatorManager);
    }

    @Override
    protected ConfigParameter _createParameter(Configuration parameterConfig) throws ConfigurationException
    {
        return new ConfigParameter();
    }
    
    @Override
    protected String _parseId(Configuration parameterConfig) throws ConfigurationException
    {
        return parameterConfig.getAttribute("id");
    }
    
    @Override
    protected ParameterType _parseType(Configuration parameterConfig) throws ConfigurationException
    {
        try
        {
            return ParameterType.valueOf(parameterConfig.getAttribute("type").toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            throw new ConfigurationException("Invalid type", parameterConfig, e);
        }
    }
    
    @Override
    protected Object _parseDefaultValue(Configuration parameterConfig, ConfigParameter parameter)
    {
        String value;
        
        Configuration childNode = parameterConfig.getChild("default-value", false);
        if (childNode == null)
        {
            value = null;
        }
        else
        {
            value = childNode.getValue("");
        }
        
        return ParameterHelper.castValue(value, parameter.getType());
    }
    
    /**
     * Parses the disable condition.
     * @param disableConditionConfiguration the configuration of the disable condition
     * @return result the parsed disable condition to be converted in JSON
     * @throws ConfigurationException
     */
    protected DisableConditions _parseDisableConditions(Configuration disableConditionConfiguration) throws ConfigurationException
    {
        if (disableConditionConfiguration == null)
        {
            return null;
        }
         
        DisableConditions conditions = new DisableConditions();

        Configuration[] conditionsConfiguration = disableConditionConfiguration.getChildren();
        for (Configuration conditionConfiguration : conditionsConfiguration)
        {
            String tagName = conditionConfiguration.getName();
            
            // Recursive case
            if (tagName.equals("conditions"))
            {
                conditions.getSubConditions().add(_parseDisableConditions(conditionConfiguration));
            }
            else if (tagName.equals("condition"))
            {
                String id = conditionConfiguration.getAttribute("id");
                DisableCondition.OPERATOR operator = DisableCondition.OPERATOR.valueOf(conditionConfiguration.getAttribute("operator", "eq").toUpperCase());
                String value = conditionConfiguration.getValue("");
                
                
                DisableCondition condition = new DisableCondition(id, operator, value);
                conditions.getConditions().add(condition);
            }
        }
        
        conditions.setAssociation(DisableConditions.ASSOCIATION_TYPE.valueOf(disableConditionConfiguration.getAttribute("type", "and").toUpperCase()));
        
        return conditions;
    }
    
    @Override
    protected void _additionalParsing(ServiceManager manager, String pluginName, Configuration parameterConfig, String parameterId, ConfigParameter parameter) throws ConfigurationException
    {
        super._additionalParsing(manager, pluginName, parameterConfig, parameterId, parameter);
        
        parameter.setId(parameterId);
        parameter.setDisplayCategory(_parseI18nizableText(parameterConfig, pluginName, "category"));
        parameter.setDisplayGroup(_parseI18nizableText(parameterConfig, pluginName, "group"));
        parameter.setGroupSwitch(parameterConfig.getAttributeAsBoolean("group-switch", false));
        parameter.setOrder(parameterConfig.getChild("order").getValueAsLong(0));
        parameter.setDisableConditions(_parseDisableConditions(parameterConfig.getChild("disable-conditions", false)));
    }
}
