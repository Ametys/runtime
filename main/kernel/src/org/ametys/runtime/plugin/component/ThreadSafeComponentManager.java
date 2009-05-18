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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Poolable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.SingleThreaded;

import org.ametys.runtime.util.LoggerFactory;

// Le code est initialement inspiré du ExcaliburComponentManager
/**
 * Special ServiceManager able to handle extensions.<br>
 * It does not actually implements the Avalon ServiceManager interface, to be able to use the Java 5 generic parameter.<br>
 * This class is also intended to be used "locally" : its lookup(role) method does NOT propagate to the parent manager if the component does not exist.<br>
 * To use the real ServiceManager, use the PluginsServiceManager subclass instead.<br>
 * <br>
 * It handles the "PluginAware" interface, implemented by extensions.<br>
 * It handles the following Avalon interfaces :
 * <ul>
 * <li>LogEnabled
 * <li>Contextualizable
 * <li>Serviceable
 * <li>Configurable
 * <li>Parameterizable
 * <li>Initializable
 * <li>Startable
 * <li>Disposable
 * </ul>
 * All handled components are considered ThreadSafe, ie only one instance is created at initialization time and returned at each lookup()
 * @param <T> The type of handled extensions
 */
public class ThreadSafeComponentManager<T> extends AbstractLogEnabled implements Contextualizable, Initializable, Disposable, Serviceable
{
    /** The Cocoon ServiceManager */
    protected ServiceManager _manager;
    
    // Map<role, component>
    Map<String, T> _components = new HashMap<String, T>();

    /** The application context for components */
    Context _context;

    /** Used to map roles to ComponentFactories */
    private Map<String, ComponentFactory> _componentFactories = Collections.synchronizedMap(new HashMap<String, ComponentFactory>());
    
    /** Is the Manager disposed or not? */
    private boolean _disposed;

    /** Is the Manager initialized? */
    private boolean _initialized;
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
    }

    /**
     * Set up the Component Context.
     */
    public void contextualize(final Context context)
    {
        _context = context;
    }

    /**
     * Return an instance of a component based on a Role. The Role is usually the Interface Fully Qualified Name(FQN).
     * @param role The key name of the <code>Component</code> to retrieve.
     * @return the desired component or null if no component exist for the given role
     * @throws ComponentException if an error occurs
     */
    public T lookup(String role) throws ComponentException
    {
        if (!_initialized)
        {
            String message = "Looking up component on an uninitialized ThreadSafeComponentManager [" + role + "]";
            getLogger().debug(message);
            return null;
        }

        if (_disposed)
        {
            throw new IllegalStateException("You cannot lookup components on a disposed ThreadSafeComponentManager");
        }

        if (role == null)
        {
            throw new ComponentException(role, "ThreadSafeComponentManager attempted to retrieve component with null role.");
        }
        
        T component = _components.get(role);
        
        if (component == null && _componentFactories.containsKey(role))
        {
            try
            {
                component = _componentFactories.get(role).newInstance();
            }
            catch (Exception e)
            {
                throw new ComponentException(role, "Unable to initialize component " + role, e);
            }
            
            _components.put(role, component);
        }

        return component;
    }

    /**
     * Tests for existence of a component for a given role.
     * @param role a string identifying the key to check.
     * @return true if there is a component for the given role, false if not.
     */
    public boolean hasRole(String role)
    {
        if (!_initialized || _disposed)
        {
            return false;
        }

        return _components.containsKey(role);
    }

    /**
     * Tests for existence of a component.
     * @param component to component to check
     * @return true if the component exists, false if it does not.
     */
    public boolean hasComponent(T component)
    {
        if (!_initialized || _disposed)
        {
            return false;
        }

        return _components.containsValue(component);
    }

    /**
     * Properly initialize of the Child handlers.
     */
    public void initialize() throws Exception
    {
        if (_initialized)
        {
            throw new IllegalStateException("ComponentManager has already been initialized");
        }
        
        synchronized (this)
        {
            _initialized = true;
            
            for (String role : _componentFactories.keySet())
            {
                if (!_components.containsKey(role)) 
                {
                    ComponentFactory factory = _componentFactories.get(role);

                    try
                    {
                        if (getLogger().isDebugEnabled())
                        {
                            getLogger().debug("Instanciating component for role " + role);
                        }
                        
                        _components.put(role, factory.newInstance());
                    }
                    catch (Exception e)
                    {
                        // Rethrow the exception
                        throw new Exception("Caught an exception trying to initialize the component " + role, e);
                    }
                }
            }

            _componentFactories.clear();
        }
    }

    /**
     * Properly dispose of the Child handlers.
     */
    public void dispose()
    {
        synchronized (this)
        {
            for (String role : _components.keySet())
            {
                T component = _components.get(role);

                try
                {
                    if (component instanceof Startable)
                    {
                        ((Startable) component).stop();
                    }
    
                    if (component instanceof Disposable)
                    {
                        ((Disposable) component).dispose();
                    }
                }
                catch (Exception e)
                {
                    getLogger().error("Caught an exception trying to dispose the component " + role, e);
                }
            }
            
            _components.clear();

            _disposed = true;
        }
    }

    /**
     * Add a new component to the manager.
     * @param pluginName the plugin containing the component
     * @param featureName the feature containing the component
     * @param role the role name for the new component.
     * @param component the class of this component.
     * @param configuration the configuration for this component.
     * @throws ComponentException if an error occured setting up component 
     */
    public void addComponent(String pluginName, String featureName, String role, Class<T> component, Configuration configuration) throws ComponentException
    {
        if (_initialized)
        {
            throw new ComponentException(role, "Cannot add components to an initialized ComponentManager");
        }
        
        if (_componentFactories.containsKey(role))
        {
            throw new ComponentException(role, "A component for the role '" + role + "' is already registered on this ComponentManager.");
        }

        try
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Attempting to get Handler for role [" + role + "]");
            }

            if (Poolable.class.isAssignableFrom(component) || SingleThreaded.class.isAssignableFrom(component))
            {
                throw new ServiceException(role, "The class " + component.getName() + "  implements SingleThreaded, or Poolable, which is not allowed by this ComponentManager");
            }

            // get the factory to use to create the instance of the Component.
            ComponentFactory factory = getComponentFactory(pluginName, featureName, role, component, configuration);

            _componentFactories.put(role, factory);
        }
        catch (Exception e)
        {
            throw new ComponentException(role, "Could not set up component", e);
        }
    }
    
    ComponentFactory getComponentFactory(String pluginName, String featureName, String role, Class<T> componentClass, Configuration configuration)
    {
        return new ComponentFactory(pluginName, featureName, role, componentClass, configuration, _manager, getLogger());
    }
    
    class ComponentFactory
    {
        String _pluginName;

        String _featureName;

        String _role;

        Class<T> _componentClass;

        Configuration _configuration;

        ServiceManager _serviceManager;

        Logger _logger;

        ComponentFactory(String pluginName, String featureName, String role, Class<T> componentClass, Configuration configuration, ServiceManager serviceManager, Logger logger)
        {
            _pluginName = pluginName;
            _featureName = featureName;
            _componentClass = componentClass;
            _configuration = configuration;
            _role = role;
            _serviceManager = serviceManager;
            _logger = logger;
        }
        
        T instanciate() throws Exception
        {
            T component = _componentClass.newInstance();

            if (_logger.isDebugEnabled())
            {
                _logger.debug("ComponentFactory creating new instance of " + _componentClass.getName() + ".");
            }

            if (component instanceof LogEnabled)
            {
                if (_configuration == null)
                {
                    ContainerUtil.enableLogging(component, LoggerFactory.getLoggerFor(_componentClass.getName()));
                }
                else
                {
                    String logger = _configuration.getAttribute("logger", null);
                    if (logger == null)
                    {
                        if (_logger.isDebugEnabled())
                        {
                            _logger.debug("no logger attribute available, using standard logger");
                        }
                        ContainerUtil.enableLogging(component, LoggerFactory.getLoggerFor(_componentClass.getName()));
                    }
                    else
                    {
                        if (_logger.isDebugEnabled())
                        {
                            _logger.debug("logger attribute is " + logger);
                        }
                        ContainerUtil.enableLogging(component, LoggerFactory.getLoggerFor(logger));
                    }
                }
            }

            if (component instanceof Contextualizable)
            {
                ContainerUtil.contextualize(component, _context);
            }

            // Infos spéciales plugins
            // Tout ça pour ça ...
            if (component instanceof PluginAware)
            {
                ((PluginAware) component).setPluginInfo(_pluginName, _featureName);
            }

            if (component instanceof Serviceable)
            {
                ContainerUtil.service(component, _serviceManager);
            }
            
            return component;
        }
        
        void configureAndStart(T component) throws Exception
        {
            ContainerUtil.configure(component, _configuration);

            if (component instanceof Parameterizable)
            {
                final Parameters parameters = Parameters.fromConfiguration(_configuration);
                ContainerUtil.parameterize(component, parameters);
            }

            ContainerUtil.initialize(component);

            ContainerUtil.start(component);
        }
        
        T newInstance() throws Exception
        {
            T component = instanciate();
            
            configureAndStart(component);

            return component;
        }
    }
}
