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
package org.ametys.runtime.parameter;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;

/**
 * Parameter checker parser from an XML configuration. 
 */
public class ParameterCheckerParser
{
    /** The parameter checker component manager. */
    protected ThreadSafeComponentManager<ParameterChecker> _parameterCheckerManager;
    
    /** The name of the plugin declaring this parameter checker */
    protected String _pluginName;
    
    private final Map<ParameterCheckerDescriptor, String> _parameterCheckersToLookup = new HashMap<>();
    
    /**
     * Create a parameter checker parser
     * @param paramCheckerManager the parameter checker.
     */
    public ParameterCheckerParser(ThreadSafeComponentManager<ParameterChecker> paramCheckerManager)
    {
        _parameterCheckerManager = paramCheckerManager;
    }
    
    /**
     * Parse a parameter checker from a XML configuration.
     * @param pluginName the plugin's name declaring this parameter.
     * @param paramCheckerConfig the XML configuration.
     * @return the {@link ParameterCheckerDescriptor} for the parsed parameter checker
     * @throws ConfigurationException if the configuration is not valid.
     */
    public ParameterCheckerDescriptor parseParameterChecker(String pluginName, Configuration paramCheckerConfig) throws ConfigurationException
    {
        ParameterCheckerDescriptor parameterChecker = _getParameterCheckerDescriptorInstance();
         
        String parameterId = paramCheckerConfig.getAttribute("id");
        String concreteClass = paramCheckerConfig.getAttribute("class");

        Configuration smallIconConfig = paramCheckerConfig.getChild("icon-small");
        String smallIconPath = smallIconConfig.getValue("");
        if (StringUtils.isNotEmpty(smallIconPath))
        {
            String plugin = smallIconConfig.getAttribute("plugin", pluginName);
            smallIconPath = "/plugins/" + plugin + "/resources/" + smallIconPath;
        }
        
        Configuration mediumIconConfig = paramCheckerConfig.getChild("icon-medium");
        String mediumIconPath = mediumIconConfig.getValue("");
        if (StringUtils.isNotEmpty(mediumIconPath))
        {
            String plugin = mediumIconConfig.getAttribute("plugin", pluginName);
            mediumIconPath = "/plugins/" + plugin + "/resources/" + mediumIconPath;
        }
        
        Configuration largeIconConfig = paramCheckerConfig.getChild("icon-large");
        String largeIconPath = largeIconConfig.getValue("");
        if (StringUtils.isNotEmpty(largeIconPath))
        {
            String plugin = largeIconConfig.getAttribute("plugin", pluginName);
            largeIconPath = "/plugins/" + plugin + "/resources/" + largeIconPath;
        }
        
        I18nizableText label = _parseI18nizableText(paramCheckerConfig, pluginName, "label");
        I18nizableText description = _parseI18nizableText(paramCheckerConfig, pluginName, "description");
        
        Configuration uiRefConfig = paramCheckerConfig.getChild("ui-ref");
        int uiRefOrder = uiRefConfig.getChild("order").getValueAsInteger(1);
        
        String uiRefLocation = null;
        Configuration uiRefLocationConfig = uiRefConfig.getChild("location", false);
        if (uiRefLocationConfig != null)
        {
            uiRefLocation = uiRefLocationConfig.getAttribute("path");
        }
        
        Set<String >linkedParamsPaths = new LinkedHashSet<>();
        for (Configuration linkedParamConfig : paramCheckerConfig.getChild("linked-params").getChildren("param-ref"))
        {
            linkedParamsPaths.add(linkedParamConfig.getAttribute("id"));
        }
        
        parameterChecker.setId(parameterId);
        parameterChecker.setLabel(label);
        parameterChecker.setDescription(description);
        parameterChecker.setClass(concreteClass);
        parameterChecker.setSmallIconPath(smallIconPath);
        parameterChecker.setMediumIconPath(mediumIconPath);
        parameterChecker.setLargeIconPath(largeIconPath);
        parameterChecker.setUiRefOrder(uiRefOrder);
        parameterChecker.setUiRefLocation(uiRefLocation);

        parameterChecker.setLinkedParamsPaths(linkedParamsPaths);
        
        _setParameterChecker(pluginName, parameterChecker, parameterId, paramCheckerConfig);
        return parameterChecker;
    }
    
    /**
     * Sets the parameter checker.
     * @param pluginName the plugin's name.
     * @param parameterChecker the parameter checker.
     * @param parameterCheckerId the parameter chekcer's id.
     * @param parameterCheckerConfig the parameter checker's configuration.
     * @throws ConfigurationException if the configuration is not valid.
     */
    @SuppressWarnings("unchecked")
    protected void _setParameterChecker(String pluginName, ParameterCheckerDescriptor parameterChecker, String parameterCheckerId, Configuration parameterCheckerConfig) throws ConfigurationException
    {
        if (parameterCheckerConfig != null)
        {
            String parameterCheckerClassName = parameterCheckerConfig.getAttribute("class");
            
            try
            {
                Class parameterCheckerClass = Class.forName(parameterCheckerClassName);
                _parameterCheckerManager.addComponent(pluginName, null, parameterCheckerId, parameterCheckerClass, parameterCheckerConfig);
            }
            catch (Exception e)
            {
                throw new ConfigurationException("Unable to instantiate parameter checker for class: " + parameterCheckerClassName, e);
            }

            // Will be affected later when parameterCheckerManager will be initialized
            // in lookupComponents() call
            _parameterCheckersToLookup.put(parameterChecker, parameterCheckerId);
        }
    }
    
    /**
     * Retrieves local parameter checkers components and sets them into
     * the previous parameter checker parsed.
     * @throws Exception if an error occurs.
     */
    public void lookupComponents() throws Exception
    {
        _parameterCheckerManager.initialize();
        
        for (Map.Entry<ParameterCheckerDescriptor, String> entry : _parameterCheckersToLookup.entrySet())
        {
            ParameterCheckerDescriptor parameterChecker = entry.getKey();
            String parameterCheckerRole = entry.getValue();
            
            try
            {
                parameterChecker.setParameterChecker(_parameterCheckerManager.lookup(parameterCheckerRole));
            }
            catch (ComponentException e)
            {
                throw new Exception("Unable to lookup parameter checker role: '" + parameterCheckerRole + "' for parameter: " + parameterChecker, e);
            }
        }
    }
    
    /**
     * Parses an i18n text.
     * @param config the configuration to use.
     * @param pluginName the current plugin name.
     * @param name the child name.
     * @return the i18n text.
     * @throws ConfigurationException if the configuration is not valid.
     */
    protected I18nizableText _parseI18nizableText(Configuration config, String pluginName, String name) throws ConfigurationException
    {
        return I18nizableText.parseI18nizableText(config.getChild(name), "plugin." + pluginName);
    }
    
    /**
     * Get the parameter checker descriptor instance to use
     * @return the parameter checker descriptor instance to use
     */
    protected ParameterCheckerDescriptor _getParameterCheckerDescriptorInstance()
    {
        return new ParameterCheckerDescriptor();
    }
}
