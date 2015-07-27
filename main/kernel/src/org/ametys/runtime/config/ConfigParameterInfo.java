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
package org.ametys.runtime.config;

import org.apache.avalon.framework.configuration.Configuration;

/**
 * Information on a configuration parameter.
 */
public class ConfigParameterInfo
{
    private final String _id;
    private final String _pluginName;
    private final Configuration _conf;
    
    /**
     * Create a configuration parameter info.
     * @param id the parameter id.
     * @param pluginName the declaring plugin name.
     * @param conf the configuration.
     */
    public ConfigParameterInfo(String id, String pluginName, Configuration conf)
    {
        _id = id;
        _conf = conf;
        _pluginName = pluginName;
    }
    
    /**
     * Get the parameter ID.
     * @return the parameter ID.
     */
    public String getId()
    {
        return _id;
    }
    
    /**
     * Get the declaring plugin name.
     * @return the declaring plugin name.
     */
    public String getPluginName()
    {
        return _pluginName;
    }
    
    /**
     * Get the declaring configuration.
     * @return the declaring configuration.
     */
    public Configuration getConfiguration()
    {
        return _conf;
    }
}
