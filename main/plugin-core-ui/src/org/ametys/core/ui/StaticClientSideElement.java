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
package org.ametys.core.ui;

import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.ametys.plugins.core.ui.util.ConfigurationHelper;

/**
 * This implementation creates an element from a static configuration
 */
public class StaticClientSideElement extends StaticFileImportsClientSideElement
{
    @Override
    protected Script _configureScript(Configuration configuration) throws ConfigurationException
    {
        List<ScriptFile> scriptsImports = _configureImports(configuration.getChild("scripts"));
        List<ScriptFile> cssImports = _configureImports(configuration.getChild("css"));
        String jsClassName = _configureClass(configuration.getChild("class"));
        Map<String, Object> initialParameters = configureInitialParameters(configuration);
        
        return new Script(this.getId(), jsClassName, scriptsImports, cssImports, initialParameters);
    }
    
    /**
     * Configure the js class name
     * @param configuration The configuration on action tag
     * @return The js class name
     * @throws ConfigurationException If an error occurs
     */
    protected String _configureClass(Configuration configuration) throws ConfigurationException
    {
        String jsClassName = configuration.getAttribute("name");
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Js class configured is '" + jsClassName + "'");
        }
        return jsClassName;        
    }
    
    /**
     * Configure the initial parameters
     * @param configuration the global configuration
     * @return The initial parameters read
     * @throws ConfigurationException The configuration is incorrect
     */
    protected Map<String, Object> configureInitialParameters(Configuration configuration) throws ConfigurationException
    {
        Map<String, Object> initialParameters = _configureParameters(configuration.getChild("class"));
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Configuration of element '" + _id + "' is over");
        }
        
        return initialParameters;
    }
    
    /**
     * Configure parameters recursively 
     * @param configuration the parameters configuration
     * @return parameters in a Map
     * @throws ConfigurationException The configuration is incorrect
     */
    protected Map<String, Object> _configureParameters(Configuration configuration) throws ConfigurationException
    {
        return ConfigurationHelper.parsePluginParameters(configuration, getPluginName(), getLogger());
    }
}
