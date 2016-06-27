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
package org.ametys.plugins.core.ui.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import org.ametys.core.ui.ClientSideElement.ScriptFile;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.i18n.I18nizableText;

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
     * Parse a plugin resource list configuration.
     * @param configuration The plugin resource list configuration.
     * @param defaultPluginName The default plugin name to use for the resources. 
     * @param logger The logger.
     * @return The list of complete URLs of files to import.
     * @throws ConfigurationException If an error occurs
     */
    public static List<ScriptFile> parsePluginResourceList(Configuration configuration, String defaultPluginName, Logger logger) throws ConfigurationException
    {
        List<ScriptFile> resourceFiles = new ArrayList<>();
        String listDefaultPlugin = configuration.getAttribute("plugin", defaultPluginName);
        for (Configuration fileConfiguration : configuration.getChildren("file"))
        {
            ScriptFile scriptFile = _getPluginResourceValue(fileConfiguration, listDefaultPlugin, logger);
            
            resourceFiles.add(scriptFile);
        }
        return resourceFiles;
    }

    private static ScriptFile _getPluginResourceValue(Configuration fileConfiguration, String listDefaultPlugin, Logger logger) throws ConfigurationException
    {
        ScriptFile scriptFile;
        String debug = fileConfiguration.getAttribute("debug", "all");

        boolean langAware = fileConfiguration.getAttributeAsBoolean("lang", false);
        if (langAware)
        {
            String defaultLang = fileConfiguration.getAttribute("defaultLang", null);
            
            Map<String, String> langPaths = new HashMap<>();
            for (Configuration langConfiguration : fileConfiguration.getChildren("lang"))
            {
                String code = langConfiguration.getAttribute("code", null);
                if (StringUtils.isBlank(code))
                {
                    throw new ConfigurationException("Code attribute is mandatory for lang tag", langConfiguration);
                }
                
                String path = _getPluginResourceValue(langConfiguration, listDefaultPlugin, langConfiguration.getValue(), logger);
                langPaths.put(code, path);
            }
            
            scriptFile = new ScriptFile(debug, langPaths, defaultLang);
        }
        else
        {
            String rtl = fileConfiguration.getAttribute("rtl", "all");
            String path = _getPluginResourceValue(fileConfiguration, listDefaultPlugin, fileConfiguration.getValue(), logger);
            
            scriptFile = new ScriptFile(debug, rtl, path);
        }
        
        return scriptFile;
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
     * Parse a plugin files list configuration and return the list of URIs.
     * @param configuration The plugin files list configuration.
     * @param defaultPluginName The default plugin name to use for the files. 
     * @param logger The logger.
     * @return The list of complete URIs of files to import.
     * @throws ConfigurationException If an error occurs
     */
    public static List<String> parsePluginResourceUri(Configuration configuration, String defaultPluginName, Logger logger) throws ConfigurationException
    {
        List<String> fileURIs = new ArrayList<>();
        String listDefaultPlugin = configuration.getAttribute("plugin", defaultPluginName);
        for (Configuration fileConfiguration : configuration.getChildren("file"))
        {
            String fileURI = _getPluginResourceUri(fileConfiguration, listDefaultPlugin, logger);
            
            fileURIs.add(fileURI);
        }
        return fileURIs;
    }
    
    /**
     * Get a plugin resource configuration value.
     * @param configuration The plugin resource configuration.
     * @param defaultPluginName The default plugin name to use for the resources. 
     * @param logger The logger.
     * @return The plugin resource full URL.
     * @throws ConfigurationException If an error occurs
     */
    private static String _getPluginResourceUri(Configuration configuration, String defaultPluginName, Logger logger) throws ConfigurationException
    {
        String pluginName = configuration.getAttribute("plugin", defaultPluginName);
        
        String value = configuration.getValue();
        
        String fullUrl = "plugin:" + pluginName + "://" + value;
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Importing file uri '" + fullUrl + "'");
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
     * @param value The key to get in configuration
     * @return the configuration parameter value.
     */
    private static String _getConfigParameterValue(Configuration configuration, String value)
    {
        return Config.getInstance().getValueAsString(value);
    }
    
    /**
     * Parse a structured Configuration to an Object.
     * The returned Object can be either a String or a Map&lt;String, Object&gt;.
     * The map's values are either a String, a Map, or a List&lt;String&gt;.
     * @param configuration The structured Configuration to parse.
     * @return The parsed Object.
     */
    public static Object parseObject(Configuration configuration)
    {
        return parseObject(configuration, null);
    }
    
    /**
     * Parse a structured Configuration to an Object.
     * The returned Object can be either a String or a Map&lt;String, Object&gt;.
     * The map's values are either a String, a Map, or a List&lt;String&gt;.
     * @param configuration The structured Configuration to parse.
     * @param defaultValue The value to use when an empty tag is found (at any level in the tree).
     * @return The parsed Object.
     */
    @SuppressWarnings("unchecked")
    public static Object parseObject(Configuration configuration, Object defaultValue)
    {
        String value = configuration.getValue(null);
        Configuration[] children = configuration.getChildren();
        
        if (value != null)
        {
            // Mixed content cannot be found in a Configuration: if it has a value,
            // it won't have any child.
            return value;
        }
        else if (children.length > 0)
        {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Configuration subConf : children)
            {
                String name = subConf.getName();
                Object subValue = parseObject(subConf, defaultValue);
                boolean multiple = configuration.getChildren(name).length > 1;
                
                if (multiple)
                {
                    // More than one value with the same name: make it a list and append the current value.
                    List<Object> values = null;
                    if (result.containsKey(name))
                    {
                        values = (List<Object>) result.get(name);
                    }
                    else
                    {
                        values = new ArrayList<>();
                        result.put(name, values);
                    }
                    values.add(subValue);
                }
                else
                {
                    // Only one value with this name: add it as a single value.
                    result.put(name, subValue);
                }
            }
            return result;
        }
        else
        {
            return defaultValue;
        }
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
                addParameter(parameters, name, I18nizableText.getI18nizableTextValue(paramConfiguration, "plugin." + defaultPluginName, value));
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
    
    private static boolean isResourceFile(Configuration config)
    {
        return config.getAttributeAsBoolean("file", false) || config.getAttribute("type", "").equals("file");
    }
    
    private static boolean isConfigParameter(Configuration config)
    {
        return config.getAttributeAsBoolean("config", false) || config.getAttribute("type", "").equals("config");
    }
    
    private static boolean isI18n(Configuration config)
    {
        return config.getAttributeAsBoolean("i18n", false) || config.getAttribute("type", "").equals("i18n");
    }
}
