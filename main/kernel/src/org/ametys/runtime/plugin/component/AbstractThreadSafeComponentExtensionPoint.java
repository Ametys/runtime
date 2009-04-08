/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.plugin.component;

import java.util.HashSet;
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
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.ametys.runtime.plugin.ExtensionPoint;
import org.ametys.runtime.util.LoggerFactory;

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
    
    private Set<String> _ids = new HashSet<String>();
    
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
        _manager = new ThreadSafeComponentManager<T>();
        _manager.enableLogging(LoggerFactory.getLoggerFor("runtime.plugin.threadsafecomponent"));
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
    protected void addComponent(String pluginName, String pluginId, String role, Class<T> clazz, Configuration configuration) throws ComponentException
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
    
    public void addExtension(String pluginName, String pluginId, Configuration configuration) throws ConfigurationException
    {
        // Vérifie l'id de l'extension
        String id = configuration.getAttribute("id");
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Configuring extension '" + id + "' in plugin '" + pluginName + "' id '" + pluginId + "'.");
        }
        
        String className = configuration.getAttribute("class", null);
        
        if (className == null)
        {
            String errorMessage = "In plugin '" + pluginName + "' id '" + pluginId + "', extension '" + id + "' does not defines any class";
            getLogger().error(errorMessage);
            throw new ConfigurationException(errorMessage, configuration);
        }
        
        Class<T> extensionClass;
        
        try
        {
            extensionClass = (Class<T>) Class.forName(className);
        }
        catch (ClassNotFoundException ex)
        {
            String errorMessage = "Unable to instanciate class '" + className + "' for plugin '" + pluginName + "' / '" + pluginId + "'";
            getLogger().error(errorMessage);
            throw new ConfigurationException(errorMessage, configuration, ex);
        }
        
        try
        {
            addComponent(pluginName, pluginId, id, extensionClass, configuration);
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
