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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.MutableConfiguration;
import org.apache.avalon.framework.service.ServiceException;

import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

/**
 * Implementation of an ExtensionPoint for client side elements.
 * @param <T> The client side element implementation
 */
public abstract class AbstractClientSideExtensionPoint<T extends ClientSideElement> extends AbstractThreadSafeComponentExtensionPoint<T>
{
    private Map<String, Configuration> _configurations = new HashMap<>();
    private Map<String, String> _configurationPlugins = new HashMap<>();
    
    private List<AbstractClientSideExtensionPoint<T>> _registeredManagers = new ArrayList<>();
    
    private List<ReferencingExtension> _referencingExtensions = new ArrayList<>();
    
    private boolean _initialized;

    @Override
    public void addExtension(String id, String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        if (configuration.getAttribute("ref-id", null) != null)
        {
            if (_initialized)
            {
                _addReferencingExtension(id, pluginName, featureName, configuration);
            }
            else
            {
                _referencingExtensions.add(new ReferencingExtension(id, pluginName, featureName, configuration));
            }
        }
        else
        {
            _configurations.put(id, configuration);
            _configurationPlugins.put(id, pluginName);
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
            Configuration baseConfiguration = clientSideElementManager._getConfiguration(refId);
            if (baseConfiguration == null)
            {
                throw new ConfigurationException("Unknow ref-id '" + refId + "' for the extension point '" + extensionPoint + "'", configuration);
            }
            
            DefaultConfiguration mergedConfiguration = new DefaultConfiguration(baseConfiguration, true);
            _getMerdedChildsConfiguration(configuration, mergedConfiguration);
            mergedConfiguration.setAttribute("ref-id", null);
            
            _configurations.put(id, mergedConfiguration);
            _configurationPlugins.put(id, pluginName);
            super.addExtension(id, pluginName, featureName, mergedConfiguration);
        }
        catch (ServiceException e)
        {
            throw new ConfigurationException("Invalid id referenced by the attribute 'ref-id'", configuration);
        }
    }
    
    private Configuration _getConfiguration(String id) throws ConfigurationException
    {
        Configuration configuration = _configurations.get(id);
        if (configuration != null)
        {
            return _getContexutalizedConfiguration(configuration, _configurationPlugins.get(id));
        }
        
        for (AbstractClientSideExtensionPoint manager : _registeredManagers)
        {
            configuration = manager._getConfiguration(id);
            if (configuration != null)
            {
                return configuration;
            }
        }
        
        return null;
    }
    
    private Configuration _getContexutalizedConfiguration(Configuration configuration, String pluginName) throws ConfigurationException
    {
        DefaultConfiguration contextualizedConfiguration = new DefaultConfiguration(configuration);
        
        _contexutalizeConfiguration(contextualizedConfiguration, pluginName);
        
        return contextualizedConfiguration;
    }
    
    private void _contexutalizeConfiguration(MutableConfiguration configuration, String pluginName) throws ConfigurationException
    {
        for (MutableConfiguration child : configuration.getMutableChildren())
        {
            if ((child.getName().equals("file") || child.getAttributeAsBoolean("file", false) || "file".equals(child.getAttribute("type", null))) && child.getAttribute("plugin", null) == null)
            {
                child.setAttribute("plugin", pluginName);
            }
            else if (("true".equals(child.getAttribute("i18n", "false")) || "i18n".equals(child.getAttribute("type", null))) && child.getValue().indexOf(":") < 0 && child.getValue().length() > 0)
            {
                child.setValue("plugin." + pluginName + ":" + child.getValue());
            }
            _contexutalizeConfiguration(child, pluginName);
        }
    }

    private void _getMerdedChildsConfiguration(Configuration configuration, MutableConfiguration base) throws ConfigurationException
    {
        base.addAllAttributes(configuration);
        for (Configuration child : configuration.getChildren())
        {
            String tagName = child.getName();
            MutableConfiguration baseChild = base.getMutableChild(tagName);
            if ("scripts".equals(tagName) || "css".equals(tagName))
            {
                for (Configuration fileChild : child.getChildren())
                {
                    baseChild.addChild(fileChild);
                }
            }
            else
            {
                _mergeChildsConfiguration(child, baseChild);
            }
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
                for (Configuration baseChild : baseChildren)
                {
                    base.removeChild(baseChild);
                }
                
                for (Configuration newChild : newChildren)
                {
                    base.addChild(newChild);
                }
            }
        }
    }
    
    @Override
    public void initializeExtensions() throws Exception
    {
        Map<String, ReferencingExtension> refExtIds = _referencingExtensions.stream().collect(Collectors.toMap(ReferencingExtension::getId, Function.identity()));
        List<String> processing = new ArrayList<>();
        for (ReferencingExtension refExt : _referencingExtensions)
        {
            _lazyInitializeReferencingExtension(refExt, refExtIds, processing);
        }
        
        super.initializeExtensions();
        _initialized = true;
    }
    
    private void _lazyInitializeReferencingExtension(ReferencingExtension ext, Map<String, ReferencingExtension> refExtIds, List<String> processing) throws ConfigurationException
    {
        if (!processing.contains(ext.getId()))
        {
            processing.add(ext.getId());
            if (refExtIds.containsKey(ext.getRefId()))
            {
                // if we are referencing another referencing extension, make sure it is initialized before
                _lazyInitializeReferencingExtension(refExtIds.get(ext.getRefId()), refExtIds, processing);
            }

            _addReferencingExtension(ext.getId(), ext.getPluginName(), ext.getFeatureName(), ext.getConfiguration());
        }
    }
    
    /**
     * Register a new ribbon manager whose extensions will also be managed by this RibbonControlsManager
     * @param manager The manager to register
     */
    public void registerRibbonManager(AbstractClientSideExtensionPoint<T> manager)
    {
        _registeredManagers.add(manager);
    }
    
    /**
     * Remove a previously registered ribbon manager
     * @param manager The manager to remove
     */
    public void unregisterRibbonManager(AbstractClientSideExtensionPoint<T> manager)
    {
        _registeredManagers.remove(manager);
    }
    
    @Override
    public T getExtension(String id)
    {
        T extension = super.getExtension(id);
        if (extension == null)
        {
            for (AbstractClientSideExtensionPoint<T> manager : _registeredManagers)
            {
                extension = manager.getExtension(id);
                if (extension != null)
                {
                    return extension;
                }
            }
        }
        
        return extension;
    }
    
    @Override
    public Set<String> getExtensionsIds()
    {
        Set<String> extensionsIds = super.getExtensionsIds();
        for (AbstractClientSideExtensionPoint<T> manager : _registeredManagers)
        {
            extensionsIds.addAll(manager.getExtensionsIds());
        }
        return extensionsIds;
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
    
    private class ReferencingExtension
    {
        private String _id;
        private String _pluginName;
        private String _featureName;
        private Configuration _configuration;
        private String _refId;

        public ReferencingExtension(String id, String pluginName, String featureName, Configuration configuration) throws ConfigurationException
        {
            _id = id;
            _pluginName = pluginName;
            _featureName = featureName;
            _configuration = configuration;
            
            _refId = configuration.getAttribute("ref-id");
        }

        /**
         * Return the id
         * @return the id
         */
        public String getId()
        {
            return _id;
        }

        /**
         * Return the plugin name
         * @return the plugin name
         */
        public String getPluginName()
        {
            return _pluginName;
        }

        /**
         * Return the feature name
         * @return the feature name
         */
        public String getFeatureName()
        {
            return _featureName;
        }

        /**
         * Return the configuration
         * @return the configuration
         */
        public Configuration getConfiguration()
        {
            return _configuration;
        }
        
        /**
         * Get the referenced extension id
         * @return The ref-id
         */
        public String getRefId()
        {
            return _refId;
        }
    }
}
