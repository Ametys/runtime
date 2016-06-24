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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.WrapperComponentManager;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.core.ui.ribbonconfiguration.RibbonConfiguration;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;
import org.ametys.runtime.plugin.component.PluginsComponentManager;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;

/**
 * Helper for RibbonManager, that support Thread safe usage of the managers, while implementing a cache for performances.
 */
public class RibbonManagerCache extends AbstractLogEnabled implements Contextualizable, Serviceable, Component
{
    /** Avalon role */
    public static final String ROLE = RibbonManagerCache.class.getName();
    
    private Map<String, RibbonManager> _ribbonManagerCache = new HashMap<>();
    private Map<RibbonManager, Map<String, Long>> _ribbonManagerCacheValidity = new HashMap<>();
    private Map<RibbonManager, Integer> _ribbonManagerUsageCache = new HashMap<>();
    private Map<RibbonManager, RibbonConfiguration> _ribbonConfigurationCache = new HashMap<>();
    private Map<RibbonManager, ThreadSafeComponentManager<Object>> _ribbonServiceManagers = new HashMap<>();
    
    private Context _context;

    private ServiceManager _cocoonManager;

    private RibbonControlsManager _ribbonControlsManager;

    private RibbonTabsManager _ribbonTabsManager;
    
    private SourceResolver _resolver;
    
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _cocoonManager = manager;
        _ribbonControlsManager = (RibbonControlsManager) manager.lookup(RibbonControlsManager.ROLE);
        _ribbonTabsManager = (RibbonTabsManager) manager.lookup(RibbonTabsManager.ROLE);
        _resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }
    
    /**
     * Create the RibbonManager associated with the given ribbon file. 
     * If the RibbonManager already exists for the ribbon file and is still valid, it is simply returned.  
     * @param uri The ribbon configuration uri
     * @return The RibbonManager
     * @throws Exception If an error occurs
     */
    public synchronized RibbonManager getManager(String uri) throws Exception
    {
        RibbonManager ribbonManager = _ribbonManagerCache.get(uri);
        if (ribbonManager != null)
        {
            if (_isRibbonManagerStillValid(ribbonManager))
            {
                _increaseUsage(ribbonManager);
                return ribbonManager;
            }
            else
            {
                _ribbonManagerCacheValidity.remove(ribbonManager);
            }
        }
        
        ribbonManager = _createRibbonManager();
        _ribbonManagerCache.put(uri, ribbonManager);
        _ribbonManagerCacheValidity.put(ribbonManager, null);
        _increaseUsage(ribbonManager);
        
        return ribbonManager;
    }

    private boolean _isRibbonManagerStillValid(RibbonManager ribbonManager)
    {
        Map<String, Long> validity = _ribbonManagerCacheValidity.get(ribbonManager);
        if (validity == null)
        {
            return false;
        }
        
        for (Entry<String, Long> entry : validity.entrySet())
        {
            String uri = entry.getKey();
            try
            {
                Source importSource = _resolver.resolveURI(uri);
                if (!importSource.exists() || importSource.getLastModified() != entry.getValue())
                {
                    return false;
                }
            }
            catch (IOException e)
            {
                // invalid import, uri is out of date
                return false;
            }
        }
        
        return true;
    }

    private RibbonManager _createRibbonManager() throws Exception
    {
        PluginsComponentManager ribbonServiceManager = new PluginsComponentManager(new WrapperComponentManager(_cocoonManager));
        ribbonServiceManager.contextualize(_context);
        ribbonServiceManager.service(_cocoonManager);
        ribbonServiceManager.setLogger(getLogger());
        ribbonServiceManager.addExtensionPoint("core-ui", RibbonManager.ROLE, RibbonManager.class, null, new ArrayList<>());
        ribbonServiceManager.initialize();
        
        RibbonManager ribbonManager = (RibbonManager) ribbonServiceManager.lookup(RibbonManager.ROLE);
        ribbonManager.initialize();

        _ribbonServiceManagers.put(ribbonManager, ribbonServiceManager);
        
        _ribbonControlsManager.registerRibbonManager(ribbonManager);
        _ribbonTabsManager.registerRibbonManager(ribbonManager);
        return ribbonManager;
    }
    
    /**
     * Dispose of a RibbonManager that was previously retrieve with this helper
     * @param ribbonManager The ribbon manager
     */
    public void dispose(RibbonManager ribbonManager)
    {
        _decreaseUsage(ribbonManager);
    }

    private synchronized void _increaseUsage(RibbonManager ribbonManager)
    {
        Integer usage = _ribbonManagerUsageCache.get(ribbonManager);
        _ribbonManagerUsageCache.put(ribbonManager, usage != null ? usage + 1 : 1);
    }
    
    private synchronized void _decreaseUsage(RibbonManager ribbonManager)
    {
        Integer usage = _ribbonManagerUsageCache.get(ribbonManager);
        if (usage != null)
        {
            if (usage > 1)
            {
                _ribbonManagerUsageCache.put(ribbonManager, usage - 1);
            }
            else
            {
                _ribbonManagerUsageCache.remove(ribbonManager);
                if (!_ribbonManagerCacheValidity.containsKey(ribbonManager))
                {
                    _ribbonConfigurationCache.remove(ribbonManager);
                    ThreadSafeComponentManager<Object> serviceManager = _ribbonServiceManagers.get(ribbonManager);
                    _ribbonServiceManagers.remove(ribbonManager);
                    _ribbonControlsManager.unregisterRibbonManager(ribbonManager);
                    _ribbonTabsManager.unregisterRibbonManager(ribbonManager);
                    serviceManager.dispose();
                }
            }
        }
    }

    /**
     * Get the ribbon configuration managed by the RibbonManager
     * @param ribbonManager The ribbon manager
     * @return The configuration, or null if no configuration was cached
     */
    public RibbonConfiguration getCachedConfiguration(RibbonManager ribbonManager)
    {
        return _ribbonConfigurationCache.get(ribbonManager);
    }
    
    /**
     * Add a ribbon configuration to the cache
     * @param ribbonManager The ribbon manager
     * @param configuration The configuration
     * @param importsValidity The list of imports for this ribbon configuration and their validity
     */
    public void addCachedConfiguration(RibbonManager ribbonManager, RibbonConfiguration configuration, Map<String, Long> importsValidity)
    {
        _ribbonConfigurationCache.put(ribbonManager, configuration);
        _ribbonManagerCacheValidity.put(ribbonManager, importsValidity);
    }
}
