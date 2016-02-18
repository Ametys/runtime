/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.runtime.plugin.component;

/**
 * Used by components needing to know their declaring plugin.
 */
public interface PluginAware
{
    /**
     * Sets the plugin info relative to the current component.<br>
     * <i>Note : </i>The feature name may be null if the targeted component in declared at plugin level.
     * @param pluginName Unique identifier for the plugin hosting the extension
     * @param featureName Unique feature identifier (unique for a given pluginName)
     * @param id Unique identifier of this component
     */
    public void setPluginInfo(String pluginName, String featureName, String id);
}
