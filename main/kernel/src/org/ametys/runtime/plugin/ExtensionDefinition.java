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
public class ExtensionDefinition
{
    private String _id;
    private String _point;
    private String _pluginName;
    private String _featureName;
    private Configuration _configuration;
    
    /**
     * Store elements about an extension
     * @param id the extension's id.
     * @param point the extension point name.
     * @param pluginName The name of the plugin declaring the extension.
     * @param featureName The name of the feature declaring the extension.
     * @param configuration The configuration of the extension.
     */
    public ExtensionDefinition(String id, String point, String pluginName, String featureName, Configuration configuration)
    {
        _id = id;
        _point = point;
        _pluginName = pluginName;
        _featureName = featureName;
        _configuration = configuration;
    }
    
    /**
     * Returns the extension id.
     * @return the extension id.
     */
    public String getId()
    {
        return _id;
    }
    
    /**
     * Returns the extension point name.
     * @return the extension point name.
     */
    public String getPoint()
    {
        return _point;
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
