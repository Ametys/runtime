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

import org.apache.avalon.framework.configuration.Configuration;

/**
 * This class is a storage for an extension informations 
 */
public class ComponentDefinition
{
    private String _id;
    private String _role;
    private String _pluginName;
    private String _featureName;
    private Configuration _configuration;
    
    /**
     * Store elements about a component.
     * @param id the component's id.
     * @param role the component's role.
     * @param pluginName The name of the plugin declaring the component.
     * @param featureName The name of the feature declaring the component.
     * @param configuration The configuration of the component.
     */
    public ComponentDefinition(String id, String role, String pluginName, String featureName, Configuration configuration)
    {
        _id = id;
        _role = role;
        _pluginName = pluginName;
        _featureName = featureName;
        _configuration = configuration;
    }
    
    /**
     * Returns the component id.
     * @return the component id.
     */
    public String getId()
    {
        return _id;
    }
    
    /**
     * Returns the component role.
     * @return the component role.
     */
    public String getRole()
    {
        return _role;
    }
    
    /**
     * Returns the configuration
     * @return the configuration
     */
    public Configuration getConfiguration()
    {
        return _configuration;
    }
    
    /**
     * Returns the feature name
     * @return the feature name
     */
    public String getFeatureName()
    {
        return _featureName;
    }
    
    /**
     * Returns the plugin name
     * @return the plugin name
     */
    public String getPluginName()
    {
        return _pluginName;
    }
}
