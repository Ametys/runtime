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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dependencies manager, able to compute the full chain of dependencies. 
 */
public class ClientSideElementDependenciesManager
{
    /** Logger */
    protected Logger _logger = LoggerFactory.getLogger(ClientSideElementDependenciesManager.class);
    
    private Map<String, List<String>> _dependencies;
    private ServiceManager _manager;

    /**
     * Default constructor for the dependencies manager.
     * @param manager The service manager, used to resolve dependencies.
     */
    public ClientSideElementDependenciesManager(ServiceManager manager)
    {
        _dependencies = new HashMap<>();
        _manager = manager;
    }
    
    /**
     * Register a new dependency
     * @param extensionPoint The dependency extension point
     * @param extensionId The dependency extension
     */
    public void register(String extensionPoint, String extensionId)
    {
        if (!_dependencies.containsKey(extensionPoint))
        {
            _dependencies.put(extensionPoint, new ArrayList<>());
        }
        
        List<String> extensions = _dependencies.get(extensionPoint);
        
        if (!extensions.contains(extensionId))
        {
            extensions.add(extensionId);
        }
    }
    
    /**
     * Register a new dependency to a client side element
     * @param element The client side element
     */
    public void register(ClientSideElement element)
    {
        Map<String, List<String>> dependencies = element.getDependencies();
        
        for (Entry<String, List<String>> entry : dependencies.entrySet())
        {
            String extensionPoint = entry.getKey();
            
            for (String extensionId : entry.getValue())
            {
                if (_logger.isDebugEnabled())
                {
                    _logger.debug("Register dependency : The extension '" + element.getId() + "' depends on '" + extensionId + "' of extension point '" + extensionPoint + "')");
                }
                
                register(extensionPoint, extensionId);
            }
        }
    }
    
    /**
     * Compute the chain of dependencies
     * @return The list of dynamic dependencies calculated from the registered dependencies.
     * @throws ServiceException If an error occurs with the list of ExtensionPoints
     */
    public Map<String, List<ClientSideElement>> computeDependencies() throws ServiceException
    {
        Map<String, List<ClientSideElement>> computedDependencies = new HashMap<>();
        computeDependencies(computedDependencies, _dependencies, new ArrayList<>());
        return computedDependencies;
    }
    
    /**
     * Recursively Compute the chain of dependency.
     * @param computedDependencies The list of dependencies that have already been computed from the chain. This map is filled with the full dependencies chain.
     * @param dependenciesToProcess The list of extensions to parse, mapped by extension points, that can have additional dependencies.
     * @param knownElements The list of elements that were already handled (to avoid infinite loop)
     * @throws ServiceException If an error occurs
     */
    private void computeDependencies(Map<String, List<ClientSideElement>> computedDependencies, Map<String, List<String>> dependenciesToProcess, List<ClientSideElement> knownElements) throws ServiceException
    {
        for (Entry<String, List<String>> dependencyToProcess : dependenciesToProcess.entrySet())
        {
            String extensionPointId = dependencyToProcess.getKey();
            AbstractClientSideExtensionPoint<ClientSideElement> extensionPoint = (AbstractClientSideExtensionPoint<ClientSideElement>) _manager.lookup(extensionPointId);
            
            if (!computedDependencies.containsKey(extensionPointId))
            {
                computedDependencies.put(extensionPointId, new ArrayList<>());
            }
            
            for (String extensionIdToProcess : dependencyToProcess.getValue())
            {
                for (ClientSideElement extension : extensionPoint.findDependency(extensionIdToProcess))
                {
                    if (extension != null && !knownElements.contains(extension))
                    {
                        // only process extension once
                        knownElements.add(extension);
                        
                        Map<String, List<String>> dependencies = extension.getDependencies();
                        
                        if (_logger.isDebugEnabled())
                        {
                            _logger.debug("Computing dependencies : For extension point '" + extensionPointId + "', the extension : '" + extensionIdToProcess + "' requires the following dependencies : " + dependencies.toString());
                        }
                        
                        // Fill the computedDependencies list recursively, with the dependency's own dependencies.
                        computeDependencies(computedDependencies, dependencies, knownElements);
    
                        computedDependencies.get(extensionPointId).add(extension);
                    }
                }
            }
        }
    }
}
