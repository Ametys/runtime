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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerParser;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;

/**
 * Parameter checker parser for configuration parameters
 */
public class ConfigParameterCheckerParser extends ParameterCheckerParser
{
    /**
     * Instantiate a configuration parameter checker parser
     * @param paramCheckerManager the parameter checker.
     */
    public ConfigParameterCheckerParser(ThreadSafeComponentManager<ParameterChecker> paramCheckerManager)
    {
        super(paramCheckerManager);
    }
    
    /**
     * Parse a configuration parameter checker from an XML configuration.
     * @param pluginName the plugin's name declaring this parameter.
     * @param paramCheckerConfig the XML configuration.
     * @return the {@link ConfigParameterCheckerDescriptor} for the parsed configuration parameter checker
     * @throws ConfigurationException if the configuration is not valid.
     */
    @Override
    public ConfigParameterCheckerDescriptor parseParameterChecker(String pluginName, Configuration paramCheckerConfig) throws ConfigurationException
    {
        ConfigParameterCheckerDescriptor configParamChecker = (ConfigParameterCheckerDescriptor) super.parseParameterChecker(pluginName, paramCheckerConfig);
        
        Configuration uiRefConfig = paramCheckerConfig.getChild("ui-ref");
        
        String uiRefParamId = null;
        Configuration uiRefParamConfig = uiRefConfig.getChild("param-ref", false);
        if (uiRefParamConfig != null)
        {
            uiRefParamId = uiRefParamConfig.getAttribute("id");
        }
        
        I18nizableText uiRefGroup = null;
        Configuration uiRefGroupConfig = uiRefConfig.getChild("group", false);
        if (uiRefGroupConfig != null)
        {
            uiRefGroup = _parseI18nizableText(uiRefConfig, pluginName, "group");
        }
        
        I18nizableText  uiRefCategory = null;
        Configuration uiRefCategoryConfig = uiRefConfig.getChild("category", false);
        if (uiRefCategoryConfig != null)
        {
            uiRefCategory = _parseI18nizableText(uiRefConfig, pluginName, "category");
        }
        
        configParamChecker.setUiRefParamPath(uiRefParamId);
        configParamChecker.setUiRefGroup(uiRefGroup);
        configParamChecker.setUiRefCategory(uiRefCategory);
        
        return configParamChecker;
    }
    
    @Override
    protected ConfigParameterCheckerDescriptor _getParameterCheckerDescriptorInstance()
    {
        return new ConfigParameterCheckerDescriptor();
    }
}
