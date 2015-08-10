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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.slf4j.LoggerFactory;

import org.ametys.runtime.plugin.ExtensionPoint;

/**
 * Avalon based implementation of an ExtensionPoint.<br>
 * Subclasses only need to call <code>addComponent()</code> for each new extension.<br>
 * @param <T> the type of the managed extensions
 */
public abstract class AbstractThreadSafeComponentExtensionPoint<T> extends AbstractLogEnabled implements ExtensionPoint<T>, Component, ThreadSafe, Serviceable, Initializable, Disposable, Contextualizable 
{
    /** Avalon ComponentManager */
    protected ThreadSafeComponentManager<T> _manager;
    /** Avalon service manager */
    protected ServiceManager _cocoonManager;
    /** Avalon context */
    protected Context _context;
    
    private Set<String> _ids = new LinkedHashSet<>();
    
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _cocoonManager = manager;
    }
    
    public void initialize() throws Exception
    {
        _manager = new ThreadSafeComponentManager<>();
        _manager.setLogger(LoggerFactory.getLogger("runtime.plugin.threadsafecomponent"));
        _manager.contextualize(_context);
        _manager.service(_cocoonManager);
    }
    
    public void dispose()
    {
        _manager.dispose();
    }
    
    /**
     * Adds a Component to the underlying ComponentManager. 
     * Each extension to this ExtensionPoint may be considered as a Component.<br>
     * @param pluginName Unique identifier for the plugin hosting the extension
     * @param pluginId Unique plugin identifier (unique for a given pluginName)
     * @param role the Avalon role
     * @param clazz the class of the component
     * @param configuration the configuration of the component
     * @throws ComponentException if an error occured whil setting up components
     */
    protected void addComponent(String pluginName, String pluginId, String role, Class<? extends T> clazz, Configuration configuration) throws ComponentException
    {
        _ids.add(role);
        _manager.addComponent(pluginName, pluginId, role, clazz, configuration);
    }
    
    public void initializeExtensions() throws Exception
    {
        _manager.initialize();
    }
    
    public T getExtension(String id) 
    {
        try
        {
            return _manager.lookup(id);
        }
        catch (ComponentException e)
        {
            throw new IllegalArgumentException(e);
        }
    }
    
    public boolean hasExtension(String id) 
    {
        return _ids.contains(id);
    }
    
    @SuppressWarnings("unchecked")
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
        
        try
        {
            addComponent(pluginName, featureName, id, extensionClass, configuration);
        }
        catch (ComponentException e)
        {
            throw new ConfigurationException("Exception loading extension " + id, e);
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Extension configured");
        }
    }
    
    public Set<String> getExtensionsIds()
    {
        return _ids;
    }
}
