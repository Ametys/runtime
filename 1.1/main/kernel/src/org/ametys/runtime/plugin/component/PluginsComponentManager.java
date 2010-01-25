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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * Implementation of an Avalon ComponentManager handling the PluginAware and ParentAware interfaces.<br>
 * See the {@link ThreadSafeComponentManager} for more details.
 */
public class PluginsComponentManager extends ThreadSafeComponentManager<Object> implements ComponentManager
{
    ComponentManager _parentManager;
    
    /**
     * Contructor
     * @param parentManager the parent manager
     */
    public PluginsComponentManager(ComponentManager parentManager)
    {
        _parentManager = parentManager;
    }
    
    public boolean hasComponent(String role)
    {
        if (super.hasRole(role))
        {
            return true;
        }
        else
        {
            return _parentManager.hasComponent(role);
        }
    }
    
    @Override
    public Component lookup(String role) throws ComponentException
    {
        Component component = (Component) super.lookup(role);
        
        if (component != null)
        {
            return component;
        }
        else
        {
            return _parentManager.lookup(role);
        }
    }

    public void release(Component object)
    {
        if (super.hasComponent(object))
        {
            // does nothing, all components are ThreadSafe
        }
        else
        {
            _parentManager.release(object);
        }
    }
    
    @Override
    ComponentFactory getComponentFactory(String pluginName, String featureName, String role, Class<Object> componentClass, Configuration configuration)
    {
        return new ProxyComponentFactory(pluginName, featureName, role, componentClass, configuration, _manager, getLogger());
    }
    
    private class ProxyComponentFactory extends ComponentFactory
    {
        ProxyComponentFactory(String pluginName, String featureName, String role, Class<Object> componentClass, Configuration configuration, ServiceManager serviceManager, Logger logger)
        {
            super(pluginName, featureName, role, componentClass, configuration, serviceManager, logger);
        }
        
        @Override
        Component newInstance() throws Exception
        {
            Object component = instanciate();
            
            if (component instanceof ParentAware)
            {
                Object parent = _parentManager.lookup(_role);
                ((ParentAware) component).setParent(parent);
            }
            
            configureAndStart(component);
            
            if (component instanceof Component)
            {
                return (Component) component;
            }
            
            Set<Class> interfaces = new HashSet<Class>();
            getAllInterfaces(component.getClass(), interfaces);

            interfaces.add(Component.class);

            Class[] proxyInterfaces = interfaces.toArray(new Class[0]);

            return (Component) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), proxyInterfaces, new ComponentInvocationHandler(component));
        }
        
        private void getAllInterfaces(Class clazz, Set<Class> interfaces) throws Exception
        {
            if (clazz == null)
            {
                return;
            }

            Class[] objectInterfaces = clazz.getInterfaces();
            for (int i = 0; i < objectInterfaces.length; i++)
            {
                interfaces.add(objectInterfaces[i]);
            }

            getAllInterfaces(clazz.getSuperclass(), interfaces);
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
}
