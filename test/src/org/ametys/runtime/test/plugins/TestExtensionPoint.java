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
package org.ametys.runtime.test.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.ametys.runtime.plugin.ExtensionPoint;

/**
 * Simple EP used for tests
 */
public class TestExtensionPoint implements ExtensionPoint<Object>
{
    Map<String, Object> _objects = new HashMap<String, Object>();
    
    public void addExtension(String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        try
        {
            String id = configuration.getAttribute("id");
            Object object = Class.forName(configuration.getAttribute("class")).newInstance();
            _objects.put(id, object);
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Unable to instanciate", e);
        }
    }
    
    public Object getExtension(String id)
    {
        return _objects.get(id);
    }
    
    public Set<String> getExtensionsIds()
    {
        return _objects.keySet();
    }
    
    public boolean hasExtension(String id)
    {
        return _objects.containsKey(id);
    }
    
    public void initializeExtensions() throws Exception
    {
        // empty
    }
}
