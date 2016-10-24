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
 * Object representation of an extension point definition.
 */
public class ExtensionPointDefinition
{
    Configuration _configuration;
    String _pluginName;
    String _id;
    boolean _safe;
    
    /**
     * Constructor.
     * @param id the extension point's id
     * @param configuration the {@link Configuration}.
     * @param pluginName the plugin containing the extension point definition.
     * @param safe if the extension point is to be loaded in safe mode.
     */
    public ExtensionPointDefinition(String id, Configuration configuration, String pluginName, boolean safe)
    {
        _id = id;
        _configuration = configuration;
        _pluginName = pluginName;
        _safe = safe;
    }
}
