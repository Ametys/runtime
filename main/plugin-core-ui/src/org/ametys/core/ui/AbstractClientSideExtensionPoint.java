/*
 *  Copyright 2016 Anyware Services
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

package org.ametys.core.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.MutableConfiguration;
import org.apache.avalon.framework.service.ServiceException;

import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

/**
 * Implementation of an ExtensionPoint for client side elements.
 */
public abstract class AbstractClientSideExtensionPoint extends AbstractThreadSafeComponentExtensionPoint<ClientSideElement>
{
    private Map<String, Configuration> _configurations = new HashMap<>();
    
    @Override
    public void addExtension(String id, String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        if (configuration.getAttribute("ref-id", null) != null)
        {
            _addReferencingExtension(id, pluginName, featureName, configuration);
        }
        else
        {
            _configurations.put(id, configuration);
            super.addExtension(id, pluginName, featureName, configuration);
        }
    }
    
    private void _addReferencingExtension(String id, String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        try
        {
            String refId = configuration.getAttribute("ref-id");
            String extensionPoint = configuration.getAttribute("point");
            AbstractClientSideExtensionPoint clientSideElementManager = (AbstractClientSideExtensionPoint) _cocoonManager.lookup(extensionPoint);
            Configuration baseConfiguration = clientSideElementManager._configurations.get(refId);
            DefaultConfiguration mergedConfiguration = new DefaultConfiguration(baseConfiguration);
            _mergeChildsConfiguration(configuration, mergedConfiguration);
            mergedConfiguration.setAttribute("ref-id", null);
            
            _configurations.put(id, mergedConfiguration);
            super.addExtension(id, pluginName, featureName, mergedConfiguration);
        }
        catch (ServiceException e)
        {
            throw new ConfigurationException("Invalid id referenced by the attribute 'ref-id'", configuration);
        }
    }
    
    private void _mergeChildsConfiguration(Configuration configuration, MutableConfiguration base) throws ConfigurationException
    {
        String value = configuration.getValue(null);
        if (value != null)
        {
            base.setValue(value);
        }
        
        base.addAllAttributes(configuration);
        
        Set<String> childrenToProcess = Arrays.stream(configuration.getChildren())
                                              .map(Configuration::getName)
                                              .collect(Collectors.toSet());
        for (String childName : childrenToProcess)
        {
            MutableConfiguration[] baseChildren = base.getMutableChildren(childName);
            Configuration[] newChildren = configuration.getChildren(childName);
            
            if (baseChildren.length == newChildren.length)
            {
                for (int i = 0; i < baseChildren.length; i++)
                {
                    _mergeChildsConfiguration(newChildren[i], baseChildren[i]);
                }
            }
            else
            {
                if (baseChildren.length != newChildren.length)
                {
                    for (Configuration baseChild : baseChildren)
                    {
                        base.removeChild(baseChild);
                    }
                }
                
                if (baseChildren.length == 0)
                {
                    for (Configuration newChild : newChildren)
                    {
                        base.addChild(newChild);
                    }
                }
            }
        }
    }

    /**
     * Find a dependency of this manager from the Client side elements it knows. 
     * @param pattern The matching pattern to find the dependency.
     * @return The dependency, or null if no Client side element matched.
     */
    public List<ClientSideElement> findDependency(String pattern)
    {
        ClientSideElement extension = getExtension(pattern);
        
        if (extension == null)
        {
            throw new IllegalArgumentException("Unable to find dependency with id : " + pattern + ".");
        }
        
        List<ClientSideElement> result = new ArrayList<>();
        result.add(extension);
        return result;
    }
}
