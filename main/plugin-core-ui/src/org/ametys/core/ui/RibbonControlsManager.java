/*
 *  Copyright 2010 Anyware Services
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
import java.util.List;
import java.util.Set;

/**
 * This extension point handle the existing ribbon controls.
 */
public class RibbonControlsManager extends AbstractClientSideExtensionPoint
{
    /** Avalon role */
    public static final String ROLE = RibbonControlsManager.class.getName();
    
    private List<AbstractClientSideExtensionPoint> _registeredManagers = new ArrayList<>();
    
    /**
     * Register a new ribbon manager whose extensions will also be managed by this RibbonControlsManager
     * @param manager The manager to register
     */
    public void registerRibbonManager(AbstractClientSideExtensionPoint manager)
    {
        _registeredManagers.add(manager);
    }
    
    /**
     * Remove a previously registered ribbon manager
     * @param manager The manager to remove
     */
    public void unregisterRibbonManager(AbstractClientSideExtensionPoint manager)
    {
        _registeredManagers.remove(manager);
    }
    
    @Override
    public ClientSideElement getExtension(String id)
    {
        ClientSideElement extension = super.getExtension(id);
        if (extension == null)
        {
            for (AbstractClientSideExtensionPoint manager : _registeredManagers)
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
        for (AbstractClientSideExtensionPoint manager : _registeredManagers)
        {
            extensionsIds.addAll(manager.getExtensionsIds());
        }
        return extensionsIds;
    }
}
