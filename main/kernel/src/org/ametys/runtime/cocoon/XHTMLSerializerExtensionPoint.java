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

package org.ametys.runtime.cocoon;

import java.util.HashSet;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;

import org.ametys.runtime.plugin.AbstractExtensionPoint;

/**
 * This extension point allows to configure the xhtml serializer by adding new plugins
 */
public class XHTMLSerializerExtensionPoint extends AbstractExtensionPoint<String>
{
    /** The avalon role */
    public static final String ROLE = XHTMLSerializerExtensionPoint.class.getName(); 
    
    private Logger _logger;
    
    private Set<String> _allowedNamespaces;
    
    @Override
    public void enableLogging(Logger logger)
    {
        _logger = logger;
    }
    
    @Override
    public void addExtension(String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        String id = configuration.getAttribute("id");
        String namespace = configuration.getChild("namespace-allowed").getValue("");
        _extensions.put(id, namespace);
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Adding namespace '" + namespace + "' from '" + pluginName + "/" + featureName + "/" + id + "'.");
        }
    }

    @Override
    public void initializeExtensions() throws Exception
    {
        _allowedNamespaces = new HashSet<String>(_extensions.values());
    }
    
    /**
     * Get the list of allowed namespace
     * @return The non null list of namespaces
     */
    public Set<String> getAllowedNamespaces()
    {
        return _allowedNamespaces;
    }
}
