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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;

import org.ametys.runtime.config.ConfigParameterInfo;

/**
 * A plugin is composed by features, containing components definitions and extensions.
 */
public class Feature
{
    private String _pluginName;
    private String _featureName;
    private Configuration _configuration;
    private boolean _safe;
    private boolean _passive;
    private Collection<String> _dependencies = new ArrayList<>();
    private Collection<String> _deactivations = new ArrayList<>();

    private Map<String, ConfigParameterInfo> _configParameters = new LinkedHashMap<>();
    private Collection<String> _configParametersRefs = new ArrayList<>();
    private Map<String, ConfigParameterInfo> _paramCheckers = new LinkedHashMap<>();
    private Map<String, Map<String, ExtensionDefinition>> _extensions = new LinkedHashMap<>();
    private Map<String, ComponentDefinition> _components = new LinkedHashMap<>();

    Feature(String pluginName, String featureName)
    {
        _pluginName = pluginName;
        _featureName = featureName;
    }

    /**
     * Returns the declaring plugin name
     * @return the declaring plugin name
     */
    public String getPluginName()
    {
        return _pluginName;
    }
    
    /**
     * Returns this feature name
     * @return this feature name
     */
    public String getFeatureName()
    {
        return _featureName;
    }
    
    /**
     * Returns the feature id, ie. <code>getPluginName() + '/' + getFeatureName()</code>
     * @return the feature id.
     */
    public String getFeatureId()
    {
        return _pluginName + PluginsManager.FEATURE_ID_SEPARATOR + _featureName;
    }
    
    /**
     * Returns true if this feature is passive.
     * @return true if this feature is passive.
     */
    public boolean isPassive()
    {
        return _passive;
    }
    
    /**
     * Returns true if this feature is declared as safe.
     * @return true if this feature is declared as safe.
     */
    public boolean isSafe()
    {
        return _safe;
    }
    
    /**
     * Returns the extensions declared within this feature, grouped by extension point.
     * @return the extensions declared within this feature, grouped by extension point.
     */
    public Map<String, Collection<String>> getExtensionsIds()
    {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        
        for (String point : _extensions.keySet())
        {
            result.put(point, _extensions.get(point).keySet());
        }
        
        return Collections.unmodifiableMap(result);
    }
    
    /**
     * Returns the components declared within this feature, stored by role.
     * @return the components declared within this feature, stored by role.
     */
    public Map<String, String> getComponentsIds()
    {
        Map<String, String> result = new LinkedHashMap<>();
        
        for (String role : _components.keySet())
        {
            result.put(role, _components.get(role).getId());
        }
        
        return Collections.unmodifiableMap(result);
    }
    
    Configuration getConfiguration()
    {
        return _configuration;
    }
    
    Collection<String> getDependencies()
    {
        return _dependencies;
    }
    
    Collection<String> getDeactivations()
    {
        return _deactivations;
    }
    
    Map<String, Map<String, ExtensionDefinition>> getExtensions()
    {
        return _extensions;
    }
    
    Map<String, ComponentDefinition> getComponents()
    {
        return _components;
    }
    
    Map<String, ConfigParameterInfo> getConfigParameters()
    {
        return _configParameters;
    }
    
    Collection<String> getConfigParametersReferences()
    {
        return _configParametersRefs;
    }
    
    Map<String, ConfigParameterInfo> getParameterCheckers()
    {
        return _paramCheckers;
    }
    
    void configure(Configuration configuration)
    {
        _configuration = configuration;
        _passive = configuration.getAttributeAsBoolean("passive", false);
        _safe = configuration.getAttributeAsBoolean("safe", false);
        
        _configureDependencies();
        _configureDeactivations();
        
        _configureExtensions();
        _configureComponents();

        Configuration configConfiguration = configuration.getChild("config");

        _configureConfigParameters(configConfiguration);
        _configureConfigParameterReferences(configConfiguration);
        _configureParametersCheckers(configConfiguration);
    }
    
    private void _configureDependencies()
    {
        String depends = _configuration.getAttribute("depends", null);
        
        if (depends != null)
        {
            List<String> dependencies = Arrays.stream(StringUtils.split(depends, ','))
                                              .map(String::trim)
                                              .filter(StringUtils::isNotEmpty)
                                              .collect(Collectors.toList());
            
            for (String dependency : dependencies)
            {
                String dependingFeatureId = dependency;
                
                int i = dependency.indexOf('/');
                if (i == -1)
                {
                    dependingFeatureId = _pluginName + PluginsManager.FEATURE_ID_SEPARATOR + dependency;
                }
                
                _dependencies.add(dependingFeatureId);
            }
        }
    }
    
    private void _configureDeactivations()
    {
        String deactivates = _configuration.getAttribute("deactivates", null);
        
        if (deactivates != null)
        {
            List<String> deactivations = Arrays.stream(StringUtils.split(deactivates, ','))
                                               .map(String::trim)
                                               .filter(StringUtils::isNotEmpty)
                                               .collect(Collectors.toList());

            for (String deactivation : deactivations)
            {
                String deactivatedFeatureId = deactivation;
                
                int i = deactivation.indexOf('/');
                if (i == -1)
                {
                    deactivatedFeatureId = _pluginName + PluginsManager.FEATURE_ID_SEPARATOR + deactivation;
                }
                
                _deactivations.add(deactivatedFeatureId);
            }
        }
    }
    
    private void _configureConfigParameters(Configuration configConfiguration)
    {
        Configuration[] parameterConfigurations = configConfiguration.getChildren("param");
        for (Configuration parameterConfiguration : parameterConfigurations)
        {
            String id = parameterConfiguration.getAttribute("id", null);
            
            // Add the new parameter to the list of declared parameters
            _configParameters.put(id, new ConfigParameterInfo(id, _pluginName, parameterConfiguration));
        }            
    }
    
    private void _configureConfigParameterReferences(Configuration configConfiguration)
    {
        Configuration[] parameterConfigurations = configConfiguration.getChildren("param-ref");
        for (Configuration parameterConfiguration : parameterConfigurations)
        {
            String id = parameterConfiguration.getAttribute("id", null);
            _configParametersRefs.add(id);
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
    
    private void _configureExtensions()
    {
        Configuration[] extsConf = _configuration.getChild("extensions").getChildren("extension");
        for (Configuration extConf : extsConf)
        {
            // XML schema requires attributes id and point and enforces that the combination (id,point) is unique
            String id = extConf.getAttribute("id", null);
            String point = extConf.getAttribute("point", null);
            
            Map<String, ExtensionDefinition> confs = _extensions.get(point);
            if (confs == null)
            {
                confs = new HashMap<>();
                _extensions.put(point, confs);
            }
            
            confs.put(id, new ExtensionDefinition(id, point, _pluginName, _featureName, extConf));
        }
    }
    
    private void _configureComponents()
    {
        Configuration[] componentsConf = _configuration.getChild("components").getChildren("component");
        for (Configuration componentConf : componentsConf)
        {
            // XML schema requires attributes id and role
            String id = componentConf.getAttribute("id", null);
            String role = componentConf.getAttribute("role", null);
            
            _components.put(role, new ComponentDefinition(id, role, _pluginName, _featureName, componentConf));
        }
    }
}
