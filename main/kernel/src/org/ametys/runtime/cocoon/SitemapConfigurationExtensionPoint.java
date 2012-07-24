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
package org.ametys.runtime.cocoon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ametys.runtime.plugin.ExtensionPoint;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Allows to dynamically declare sitemap components to the main sitemap, subsequently available to all plugins and workspaces
 */
public class SitemapConfigurationExtensionPoint extends AbstractLogEnabled implements ExtensionPoint<Configuration[]>, Component
{
    /** Avalon Role */
    public static final String ROLE = SitemapConfigurationExtensionPoint.class.getName();
    
    private static final Collection<String> __COMPONENTS = new ArrayList<String>();
    
    static
    {
        __COMPONENTS.add("actions");
        __COMPONENTS.add("generators");
        __COMPONENTS.add("transformers");
        __COMPONENTS.add("serializers");
        __COMPONENTS.add("readers");
        __COMPONENTS.add("matchers");
        __COMPONENTS.add("selectors");
        __COMPONENTS.add("pipes");
    }
    
    private Map<String, Collection<Configuration>> _sitemapConfigurations = new HashMap<String, Collection<Configuration>>();
    private Map<String, Configuration[]> _extensions = new HashMap<String, Configuration[]>();
    
    public void addExtension(String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        String id = configuration.getAttribute("id");

        Configuration[] configurations = configuration.getChildren();
        
        _extensions.put(id, configurations);
        
        for (Configuration config : configurations)
        {
            String name = config.getName();
            String componentName = name + 's';
            
            if (!__COMPONENTS.contains(componentName))
            {
                String errorMessage = "The feature '" + pluginName + "." + featureName + "' declares an invalid sitemap component : " + name;
                getLogger().error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            
            Collection<Configuration> configs = _sitemapConfigurations.get(componentName);
            if (configs == null)
            {
                configs = new ArrayList<Configuration>();
                _sitemapConfigurations.put(componentName, configs);
            }
            
            configs.add(config);
        }
    }
    
    public Configuration[] getExtension(String id)
    {
        return _extensions.get(id);
    }
    
    public Set<String> getExtensionsIds()
    {
        return _extensions.keySet();
    }
    
    public boolean hasExtension(String id)
    {
        return _extensions.containsKey(id);
    }
    
    public void initializeExtensions() throws Exception
    {
        // nothing to do
    }
    
    /**
     * Returns all the Configurations for a given sitemap component type 
     * @param component a sitemap component type (eg. "action", "generator", ...)
     * @return all the Configurations for a given sitemap component type. May be null if the given component type does not exist or have not been extended.
     */
    public Collection<Configuration> getConfigurations(String component)
    {
        if (!_sitemapConfigurations.containsKey(component))
        {
            return null;
        }
        
        return _sitemapConfigurations.get(component);
    }
}
