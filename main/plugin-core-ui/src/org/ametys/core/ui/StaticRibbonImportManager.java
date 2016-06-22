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
package org.ametys.core.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.ametys.plugins.core.ui.util.ConfigurationHelper;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;
import org.ametys.runtime.plugin.component.PluginAware;

/**
 * Static implementation for the ribbon import manager.
 * The expected configuration is one or more "workspace" with a match attribute (default to ".*"), and a list of files to include : 
 *    &lt;workspace match=".*"&gt;
 *      &lt;file plugin="core-ui"&gt;ribbon/ribbon.xml&lt;/file&gt;
 *      &lt;file&gt;ribbon/ribbon.xml&lt;/file&gt;
 *    &lt;/workspace&gt; 
 */
public class StaticRibbonImportManager extends AbstractLogEnabled implements RibbonImport, Configurable, PluginAware
{
    private Map<List<String>, Pattern> _filesList;
    private String _pluginName;
    
    @Override
    public void setPluginInfo(String pluginName, String featureName, String id)
    {
        _pluginName = pluginName;
    }
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _filesList = new LinkedHashMap<>();
        
        for (Configuration workspaceConfig : configuration.getChildren("workspace"))
        {
            String pattern = workspaceConfig.getAttribute("match", ".*");
            
            _filesList.put(ConfigurationHelper.parsePluginResourceUri(workspaceConfig, _pluginName, getLogger()), Pattern.compile(pattern));
        }
    }

    @Override
    public Map<List<String>, Pattern> getImports()
    {
        return _filesList;
    }
}
