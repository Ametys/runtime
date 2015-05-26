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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.LifecycleHelper;

import org.ametys.runtime.plugin.ExtensionPoint;

/**
 * Avalon based implementation of an ExtensionPoint.
 * @param <T> the type of the managed extensions
 */
public abstract class AbstractComponentExtensionPoint<T> extends AbstractLogEnabled implements ExtensionPoint<T>, Component, ThreadSafe, Serviceable, Contextualizable, Initializable, Disposable
{
    /** Avalon service manager */
    protected ServiceManager _manager;
    /** Avalon context */
    protected Context _context;
    /** Extensions id and configuration associated */
    protected Map<String, ExtensionConfiguration> _extensions;
    
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
    }
    
    @Override
    public void initialize() throws Exception
    {
        _extensions = new HashMap<>();
    }
    
    @Override
    public void dispose()
    {
        _extensions = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addExtension(String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        // Check extension id
        String id = configuration.getAttribute("id");
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Configuring extension '" + id + "' in plugin '" + pluginName + "' id '" + featureName + "'.");
        }
        
        String className = configuration.getAttribute("class", null);
        
        if (className == null)
        {
            throw new ConfigurationException("In plugin '" + pluginName + "' id '" + featureName + "', extension '" + id + "' does not defines any class", configuration);
        }
        
        Class<T> extensionClass;
        
        try
        {
            extensionClass = (Class<T>) Class.forName(className);
        }
        catch (ClassNotFoundException ex)
        {
            throw new ConfigurationException("Unable to instanciate class '" + className + "' for plugin '" + pluginName + "' / '" + featureName + "'", configuration, ex);
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Extension configured");
        }
        
        ExtensionConfiguration ec = new ExtensionConfiguration(pluginName, featureName, configuration, extensionClass);
        _extensions.put(id, ec);
    }

    @Override
    public T getExtension(String id)
    {
        ExtensionConfiguration ec = _extensions.get(id);
        if (ec == null)
        {
            throw new IllegalArgumentException("Id " + id + " is not a correct component identifier.");
        }
        
        Class<T> extensionClass = ec.getExtensionClass();
        T t;
        try
        {
            t = extensionClass.newInstance();
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Cannot instanciate the class " + extensionClass.getCanonicalName() + ". Check that there is a public constructor with no arguments.");
        }

        try
        {
            if (t instanceof PluginAware)
            {
                ((PluginAware) t).setPluginInfo(ec.getPluginName(), ec.getFeatureName());
            }
            
            LifecycleHelper.setupComponent(t, getLogger(), _context, _manager, ec.getConfiguration(), true);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Cannot initiate the class " + extensionClass.getCanonicalName() + ". Check that there is a public constructor with no arguments.");        
        }

        return t;
    }

    @Override
    public Set<String> getExtensionsIds()
    {
        return _extensions.keySet();
    }

    @Override
    public boolean hasExtension(String id)
    {
        return _extensions.containsKey(id);
    }

    @Override
    public void initializeExtensions() throws Exception
    {
        // nothing
    }
    
    /**
     * This class is a storage for an extension informations 
     */
    protected class ExtensionConfiguration
    {
        private String _pluginName;
        private String _featureName;
        private Configuration _configuration;
        private Class<T> _extensionClass;
        
        /**
         * Store elements about an extension
         * @param pluginName The name of the plugin declaring the extension
         * @param featureName The name of the feature declaring the extension
         * @param configuration The configuration declaring the extension
         * @param extensionClass The class of the extension
         */
        protected ExtensionConfiguration(String pluginName, String featureName, Configuration configuration, Class<T> extensionClass)
        {
            _pluginName = pluginName;
            _featureName = featureName;
            _configuration = configuration;
            _extensionClass = extensionClass;
        }
        
        /**
         * Returns the extension class
         * @return the extension class
         */
        public Class<T> getExtensionClass()
        {
            return _extensionClass;
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
}
