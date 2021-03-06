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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.SingleThreaded;
import org.apache.cocoon.util.log.SLF4JLoggerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    /** The application context for components */
    Context _context;

    // Map<role, component>
    Map<String, T> _componentsInitializing = new HashMap<>();
    
    // Map<role, component>
    private Map<String, T> _components = new LinkedHashMap<>();
    
    /** Used to map roles to ComponentFactories */
    private Map<String, ComponentFactory> _componentFactories = Collections.synchronizedMap(new LinkedHashMap<String, ComponentFactory>());
    
    /** Is the Manager disposed or not? */
    private boolean _disposed;

    /** Is the Manager initialized? */
    private boolean _initialized;
    
    public void service(ServiceManager manager)
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
        
        if (component == null)
        {
            if (_componentsInitializing.containsKey(role))
            {
                getLogger().debug("Trying to lookup the component '{}' during its own initialization", role);
                return _componentsInitializing.get(role);
            }
            else if (_componentFactories.containsKey(role))
            {
                try
                {
                    getLogger().debug("Trying to lookup an uninitializing component '{}'. It will be initialized right now.", role);
                    component = _componentFactories.get(role).newInstance();
                }
                catch (Exception e)
                {
                    throw new ComponentException(role, "Unable to initialize component " + role, e);
                }
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
                        getLogger().debug("Initializing component for role {}", role);
                        
                        T component = factory.newInstance();
                        _components.put(role, component);
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
     */
    public void addComponent(String pluginName, String featureName, String role, Class<? extends T> component, Configuration configuration)
    {
        if (Poolable.class.isAssignableFrom(component) || SingleThreaded.class.isAssignableFrom(component))
        {
            throw new IllegalArgumentException("The class " + component.getName() + " implements SingleThreaded, or Poolable, which is not allowed by this ComponentManager");
        }

        // get the factory to use to create the instance of the Component.
        ComponentFactory factory = getComponentFactory(pluginName, featureName, role, component, configuration);
        
        _addComponent(role, factory);
    }
    
    void _addComponent(String role, ComponentFactory factory)
    {
        if (_initialized)
        {
            throw new IllegalStateException("Cannot add components to an initialized ComponentManager");
        }
        
        if (_componentFactories.containsKey(role))
        {
            throw new IllegalArgumentException("A component for the role '" + role + "' is already registered on this ComponentManager.");
        }

        getLogger().debug("Registering factory for role [{}]", role);

        _componentFactories.put(role, factory);
    }
    
    ComponentFactory getComponentFactory(String pluginName, String featureName, String role, Class<? extends T> componentClass, Configuration configuration)
    {
        return new ComponentFactory(pluginName, featureName, role, componentClass, configuration, _manager, getLogger());
    }
    
    class ComponentFactory
    {
        String _pluginName;
        String _featureName;
        String _role;
        Class<? extends T> _componentClass;
        Configuration _configuration;
        ServiceManager _serviceManager;
        Logger _logger;

        ComponentFactory(String pluginName, String featureName, String role, Class<? extends T> componentClass, Configuration configuration, ServiceManager serviceManager, Logger logger)
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
            
            String logger = _configuration == null ? null : _configuration.getAttribute("logger", null);
            logger = logger != null ? logger : _componentClass.getName();
            
            if (component instanceof LogEnabled)
            {
                ((LogEnabled) component).setLogger(LoggerFactory.getLogger(logger));
            }

            if (component instanceof org.apache.avalon.framework.logger.LogEnabled)
            {
                ContainerUtil.enableLogging(component, new SLF4JLoggerAdapter(LoggerFactory.getLogger(logger)));
            }

            if (component instanceof Contextualizable)
            {
                ContainerUtil.contextualize(component, _context);
            }

            if (component instanceof PluginAware)
            {
                ((PluginAware) component).setPluginInfo(_pluginName, _featureName, _role);
            }
            
            return component;
        }
        
        void configureAndStart(T component) throws Exception
        {
            ContainerUtil.service(component, _serviceManager);
            
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
            
            _componentsInitializing.put(_role, component);
            
            configureAndStart(component);

            _componentsInitializing.remove(_role);
            
            return component;
        }
    }
}
