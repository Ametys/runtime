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
package org.ametys.runtime.plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;

import org.ametys.runtime.config.ConfigParameterInfo;

/**
 * Object representation of a plugin.xml
 */
public class Plugin
{
    private String _pluginName;
    private Configuration _configuration;
    
    private Map<String, ConfigParameterInfo> _configParameters = new HashMap<>();
    private Map<String, ConfigParameterInfo> _paramCheckers = new HashMap<>();
    private Map<String, Feature> _features = new HashMap<>();
    private Map<String, ExtensionPointDefinition> _extensionPoints = new HashMap<>();
    
    Plugin(String pluginName)
    {
        _pluginName = pluginName;
    }
    
    /**
     * Returns this plugin's name.
     * @return this plugin's name.
     */
    public String getName()
    {
        return _pluginName;
    }
    
    /**
     * Returns the {@link Feature}s defined by this plugin.
     * @return the {@link Feature}s defined by this plugin.
     */
    public Map<String, Feature> getFeatures()
    {
        return _features;
    }
    
    /**
     * Returns the ids of the extension points defined by this plugin.
     * @return the ids of the extension points defined by this plugin.
     */
    public Collection<String> getExtensionPoints()
    {
        return Collections.unmodifiableSet(_extensionPoints.keySet());
    }
    
    Configuration getConfiguration()
    {
        return _configuration;
    }
    
    Map<String, ExtensionPointDefinition> getExtensionPointDefinitions()
    {
        return _extensionPoints;
    }
    
    Map<String, ConfigParameterInfo> getConfigParameters()
    {
        return _configParameters;
    }
    
    Map<String, ConfigParameterInfo> getParameterCheckers()
    {
        return _paramCheckers;
    }
    
    void configure(Configuration configuration)
    {
        _configuration = configuration;
        
        Configuration configConfiguration = configuration.getChild("config");
        
        _configureConfigParameters(configConfiguration);
        _configureParametersCheckers(configConfiguration);
        
        _configureFeatures();
        _configureExtensionPoints();
    }
    
    private void _configureConfigParameters(Configuration configConfiguration)
    {
        Configuration[] parameterConfigurations = configConfiguration.getChildren("param");
        for (Configuration parameterConfiguration : parameterConfigurations)
        {
            // XML schema requires attributes id and enforces id uniqueness
            String id = parameterConfiguration.getAttribute("id", null);
            
            // Add the new parameter to the list of declared parameters
            _configParameters.put(id, new ConfigParameterInfo(id, _pluginName, parameterConfiguration));
        }            
    }
    
    private void _configureParametersCheckers(Configuration configConfiguration)
    {
        Configuration[] parameterConfigurations = configConfiguration.getChildren("param-checker");
        for (Configuration parameterConfiguration : parameterConfigurations)
        {
            String id = parameterConfiguration.getAttribute("id", null);
            
            // Add the new parameter to the list of declared parameters
            _paramCheckers.put(id, new ConfigParameterInfo(id, _pluginName, parameterConfiguration));
        }            
    }

    private void _configureFeatures()
    {
        Configuration[] featuresConf = _configuration.getChildren("feature");
        
        for (Configuration conf : featuresConf)
        {
            // XML schema requires attributes name and enforces name uniqueness
            String featureName = conf.getAttribute("name", null);
            
            Feature feature = new Feature(_pluginName, featureName);
            feature.configure(conf);
            _features.put(feature.getFeatureId(), feature);
        }
    }
    
    private void _configureExtensionPoints()
    {
        Configuration[] extPointConfs = _configuration.getChild("extension-points").getChildren("extension-point");
        
        for (Configuration conf : extPointConfs)
        {
            // XML schema requires attributes id and class and enforces id uniqueness
            String id = conf.getAttribute("id", null);
            boolean safe = conf.getAttributeAsBoolean("safe", false);
            _extensionPoints.put(id, new ExtensionPointDefinition(id, conf, _pluginName, safe));
        }
    }
}
