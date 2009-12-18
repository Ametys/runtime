/*
 *  Copyright 2009 Anyware Services
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
