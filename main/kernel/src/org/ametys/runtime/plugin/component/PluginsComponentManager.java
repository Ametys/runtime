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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.WrapperServiceManager;
import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;

import org.ametys.runtime.plugin.ExtensionDefinition;
import org.ametys.runtime.plugin.ExtensionPoint;

/**
 * Implementation of an Avalon ComponentManager handling the PluginAware and ParentAware interfaces.<br>
 * See the {@link ThreadSafeComponentManager} for more details.
 */
public class PluginsComponentManager extends ThreadSafeComponentManager<Object> implements ComponentManager
{
    ComponentManager _parentManager;
    private Map<String, Collection<ExtensionDefinition>> _extensionPoints = new HashMap<>();
    
    /**
     * Constructor.
     * @param parentManager the parent manager
     */
    public PluginsComponentManager(ComponentManager parentManager)
    {
        _parentManager = parentManager;
    }
    
    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        
        for (String point : _extensionPoints.keySet())
        {
            Collection<ExtensionDefinition> extensions = _extensionPoints.get(point);
            ExtensionPoint extPoint = (ExtensionPoint) super.lookup(point);
            
            for (ExtensionDefinition extension : extensions)
            {
                extPoint.addExtension(extension.getPluginName(), extension.getFeatureName(), extension.getConfiguration());
            }
            
            getLogger().debug("Initializing extensions for point {}", point);
            extPoint.initializeExtensions();
        }
    }
    
    public boolean hasComponent(String role)
    {
        if (super.hasRole(role))
        {
            return true;
        }
        else if (_parentManager != null)
        {
            return _parentManager.hasComponent(role);
        }
        
        return false;
    }
    
    @Override
    public Component lookup(String role) throws ComponentException
    {
        Component component = (Component) super.lookup(role);
        
        if (component != null)
        {
            return component;
        }
        else if (_parentManager != null)
        {
            return _parentManager.lookup(role);
        }
        
        return null;
    }

    public void release(Component object)
    {
        if (super.hasComponent(object))
        {
            // does nothing, all components are ThreadSafe
        }
        else if (_parentManager != null)
        {
            _parentManager.release(object);
        }
    }
    
    /**
     * Add a new extension point, and its extensions, to the manager.
     * @param pluginName the plugin containing the extension point
     * @param point the extension point name.
     * @param extensionPoint the class of the extension point.
     * @param configuration the configuration for the extension point.
     * @param extensions definitions of extensions.
     */
    public void addExtensionPoint(String pluginName, String point, Class<? extends ExtensionPoint> extensionPoint, Configuration configuration, Collection<ExtensionDefinition> extensions)
    {
        ExtensionPointFactory factory = new ExtensionPointFactory(pluginName, null, point, extensionPoint, configuration, new WrapperServiceManager(this), getLogger(), extensions);
        _addComponent(point, factory);
        _extensionPoints.put(point, extensions);
    }
    
    @Override
    ComponentFactory getComponentFactory(String pluginName, String featureName, String role, Class<? extends Object> componentClass, Configuration configuration)
    {
        return new ProxyComponentFactory(pluginName, featureName, role, componentClass, configuration, new WrapperServiceManager(this), getLogger());
    }
    
    private class ProxyComponentFactory extends ComponentFactory
    {
        ProxyComponentFactory(String pluginName, String featureName, String role, Class<? extends Object> componentClass, Configuration configuration, ServiceManager serviceManager, Logger logger)
        {
            super(pluginName, featureName, role, componentClass, configuration, serviceManager, logger);
        }
        
        Component proxify(Object component)
        {
            if (component instanceof Component)
            {
                return (Component) component;
            }
            
            List<Class<?>> interfaces = ClassUtils.getAllInterfaces(component.getClass());
            interfaces.add(Component.class);

            Class[] proxyInterfaces = interfaces.toArray(new Class[0]);

            return (Component) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), proxyInterfaces, new ComponentInvocationHandler(component));
        }
        
        @Override
        Component newInstance() throws Exception
        {
            Object component = instanciate();
            
            _componentsInitializing.put(_role, component);
            
            if (component instanceof ParentAware && _parentManager != null)
            {
                Object parent = _parentManager.lookup(_role);
                ((ParentAware) component).setParent(parent);
            }
            
            configureAndStart(component);
            
            _componentsInitializing.remove(_role);
            
            return proxify(component);
        }
    }

    private class ComponentInvocationHandler implements InvocationHandler
    {
        private final Object _delegate;

        ComponentInvocationHandler(Object delegate)
        {
            _delegate = delegate;
        }

        public Object invoke(Object proxy, Method meth, Object[] args) throws Throwable
        {
            try
            {
                return meth.invoke(_delegate, args);
            }
            catch (InvocationTargetException ite)
            {
                throw ite.getTargetException();
            }
        }
    }
    
    private class ExtensionPointFactory extends ProxyComponentFactory
    {
        private Collection<ExtensionDefinition> _extensions;
        
        public ExtensionPointFactory(String pluginName, String featureName, String role, Class<? extends ExtensionPoint> extensionPointClass, Configuration configuration, ServiceManager serviceManager, Logger logger, Collection<ExtensionDefinition> extensions)
        {
            super(pluginName, featureName, role, extensionPointClass, configuration, serviceManager, logger);
            _extensions = extensions;
        }
        
        @Override
        Component newInstance() throws Exception
        {
            Object component = instanciate();
            
            _componentsInitializing.put(_role, component);
            
            if (component instanceof ParentAware && _parentManager != null)
            {
                Object parent = _parentManager.lookup(_role);
                ((ParentAware) component).setParent(parent);
            }
            
            configureAndStart(component);
            
            /*ExtensionPoint extPoint = (ExtensionPoint) component;
            
            for (ExtensionDefinition extension : _extensions)
            {
                extPoint.addExtension(extension.getPluginName(), extension.getFeatureName(), extension.getConfiguration());
            }
            
            getLogger().info("Initializing extensions");
            extPoint.initializeExtensions();*/
            
            _componentsInitializing.remove(_role);
            
            return proxify(component);
        }
    }
}
