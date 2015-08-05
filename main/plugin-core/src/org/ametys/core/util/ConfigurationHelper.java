/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.core.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;

import org.ametys.runtime.config.Config;

/**
 * Helper class providing methods to deal with common {@link Configuration} tasks.
 */
public final class ConfigurationHelper
{
    
    private ConfigurationHelper()
    {
        // Helper class, never to be instantiated.
    }
    
    /**
     * Parse a mandatory i18n text configuration, throwing an exception if empty.
     * @param config the configuration to use.
     * @param defaultCatalogue the i18n catalogue to use when not specified.
     * @return the i18n text.
     * @throws ConfigurationException if the configuration is not valid.
     */
    public static I18nizableText parseI18nizableText(Configuration config, String defaultCatalogue) throws ConfigurationException
    {
        String text = config.getValue();
        return _getI18nizableTextValue(config, defaultCatalogue, text);
    }
    
    /**
     * Parse an optional i18n text configuration, with a default value.
     * @param config the configuration to use.
     * @param defaultCatalogue the i18n catalogue to use when not specified. 
     * @param defaultValue the default value.
     * @return the i18n text.
     */
    public static I18nizableText parseI18nizableText(Configuration config, String defaultCatalogue, String defaultValue)
    {
        String text = config.getValue(defaultValue);
        return _getI18nizableTextValue(config, defaultCatalogue, text);
    }
    
    /**
     * Get an i18n text configuration (can be a key or a "direct" string).
     * @param config The configuration to parse.
     * @param defaultCatalogue The i18n catalogue to use when not specified.  
     * @param value The i18n text, can be a key or a "direct" string.
     * @return
     */
    private static I18nizableText _getI18nizableTextValue(Configuration config, String defaultCatalogue, String value)
    {
//        boolean isI18n = config.getAttributeAsBoolean("i18n", false);
//        if (isI18n)
        if (isI18n(config))
        {
            String catalogue = config.getAttribute("catalogue", defaultCatalogue);
            
            return new I18nizableText(catalogue, value);
        }
        else
        {
            return new I18nizableText(value);
        }
    }
    
    /**
     * Parse a plugin resource list configuration.
     * @param configuration The plugin resource list configuration.
     * @param defaultPluginName The default plugin name to use for the resources. 
     * @param logger The logger.
     * @return The list of complete URLs of files to import.
     * @throws ConfigurationException If an error occurs
     */
    public static List<String> parsePluginResourceList(Configuration configuration, String defaultPluginName, Logger logger) throws ConfigurationException
    {
        List<String> resourceUrls = new ArrayList<>();
        String listDefaultPlugin = configuration.getAttribute("plugin", defaultPluginName);
        for (Configuration fileConfiguration : configuration.getChildren("file"))
        {
            resourceUrls.add(parsePluginResource(fileConfiguration, listDefaultPlugin, logger));
        }
        return resourceUrls;
    }
    
    /**
     * Parse a mandatory plugin resource configuration.
     * @param configuration The plugin resource configuration.
     * @param defaultPluginName The default plugin name to use for the resources. 
     * @param logger The logger.
     * @return The plugin resource full URL.
     * @throws ConfigurationException If an error occurs
     */
    public static String parsePluginResource(Configuration configuration, String defaultPluginName, Logger logger) throws ConfigurationException
    {
        String url = configuration.getValue();
        return _getPluginResourceValue(configuration, defaultPluginName, url, logger);
    }
    
    /**
     * Parse an optional plugin resource configuration.
     * @param configuration The plugin resource configuration.
     * @param defaultPluginName The default plugin name to use for the resources. 
     * @param defaultValue The default value
     * @param logger The logger.
     * @return The plugin resource full URL.
     */
    public static String parsePluginResource(Configuration configuration, String defaultPluginName, String defaultValue, Logger logger)
    {
        String url = configuration.getValue(defaultValue);
        return _getPluginResourceValue(configuration, defaultPluginName, url, logger);
    }
    
    /**
     * Get a plugin resource configuration value.
     * @param configuration The plugin resource configuration.
     * @param defaultPluginName The default plugin name to use for the resources. 
     * @param value The value to parse. 
     * @param logger The logger.
     * @return The plugin resource full URL.
     */
    private static String _getPluginResourceValue(Configuration configuration, String defaultPluginName, String value, Logger logger)
    {
        String pluginName = configuration.getAttribute("plugin", defaultPluginName);
        
        String fullUrl = "/plugins/" + pluginName + "/resources/" + value;
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Importing file '" + fullUrl + "'");
        }
        
        return fullUrl;
    }
    
    /**
     * Parse a mandatory configuration parameter {@link Configuration}.
     * @param configuration The {@link Configuration} to parse.
     * @return the configuration parameter value.
     * @throws ConfigurationException if an error occurs.
     */
    public static String parseConfigParameter(Configuration configuration) throws ConfigurationException
    {
        return _getConfigParameterValue(configuration, configuration.getValue());
    }
    
    /**
     * Parse an optional configuration parameter {@link Configuration}.
     * @param configuration The {@link Configuration} to parse.
     * @param defaultValue The default value.
     * @return the configuration parameter value.
     */
    public static String parseConfigParameter(Configuration configuration, String defaultValue)
    {
        return _getConfigParameterValue(configuration, configuration.getValue(defaultValue));
    }
    
    /**
     * Get a configuration parameter value.
     * @param configuration The {@link Configuration} to parse.
     * @return the configuration parameter value.
     * @throws ConfigurationException if an error occurs.
     */
    private static String _getConfigParameterValue(Configuration configuration, String value)
    {
        return Config.getInstance().getValueAsString(value);
    }
    
    /**
     * Parse parameters recursively.
     * @param configuration the parameters configuration.
     * @param defaultPluginName The default plugin name.
     * @param logger The logger.
     * @return parameters in a Map
     * @throws ConfigurationException If the configuration is incorrect.
     */
    public static Map<String, Object> parsePluginParameters(Configuration configuration, String defaultPluginName, Logger logger) throws ConfigurationException
    {
        Map<String, Object> parameters = new LinkedHashMap<>();
        
        for (Configuration paramConfiguration : configuration.getChildren())
        {
            String name;
            if (paramConfiguration.getName().equals("param"))
            {
                name = paramConfiguration.getAttribute("name");
            }
            else
            {
                name = paramConfiguration.getName();
            }
            String value = paramConfiguration.getValue("");
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Configured with parameter '" + name + "' : '" + value + "'");
            }
            
            if (isI18n(paramConfiguration))
            {
                addParameter(parameters, name, _getI18nizableTextValue(paramConfiguration, "plugin." + defaultPluginName, value));
            }
            else if (isResourceFile(paramConfiguration))
            {
                addParameter(parameters, name, _getPluginResourceValue(paramConfiguration, defaultPluginName, value, logger));
            }
            else if (isConfigParameter(paramConfiguration))
            {
                addParameter(parameters, name, _getConfigParameterValue(paramConfiguration, value));
            }
            else if (paramConfiguration.getChildren().length != 0)
            {
                addParameter(parameters, name, parsePluginParameters(paramConfiguration, defaultPluginName, logger));
            }
            else 
            {
                addParameter(parameters, name, value);
            }
        }
        
        return parameters;
    }
    
    @SuppressWarnings("unchecked")
    private static void addParameter(Map<String, Object> parameters, String name, Object newValue)
    {
        if (parameters.containsKey(name))
        {
            Object values = parameters.get(name);
            if (values instanceof List)
            {
                ((List<Object>) values).add(newValue);
            }
            else
            {
                List list = new ArrayList<>();
                list.add(values);
                list.add(newValue);
                parameters.put(name, list);
            }
        }
        else
        {
            parameters.put(name, newValue);
        }
    }
    
    private static boolean isI18n(Configuration config)
    {
        return config.getAttributeAsBoolean("i18n", false) || config.getAttribute("type", "").equals("i18n");
    }
    
    private static boolean isResourceFile(Configuration config)
    {
        return config.getAttributeAsBoolean("file", false) || config.getAttribute("type", "").equals("file");
    }
    
    private static boolean isConfigParameter(Configuration config)
    {
        return config.getAttributeAsBoolean("config", false) || config.getAttribute("type", "").equals("config");
    }
    
}
