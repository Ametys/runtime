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
package org.ametys.core.ui.ribbonconfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A ribbon configuration, with tab, user and app menus
 */
public class RibbonConfiguration
{
    /** The tabs of the ribbon */
    protected LinkedList<Tab> _tabs = new LinkedList<>();

    /** The App menu of the ribbon */
    protected RibbonMenu _appMenu = new RibbonMenu();
    
    /** The user menu elements of the ribbon */
    protected RibbonMenu _userMenu = new RibbonMenu();
    
    /** The dependencies of the ribbon */
    protected Map<String, List<String>> _dependencies = new HashMap<>();
    
    /**
     * Get the tabs for this configuration
     * @return the tabs
     */
    public LinkedList<Tab> getTabs()
    {
        return _tabs;
    }
    
    /**
     * Set the tabs for this configuration
     * @param tabs The tabs
     */
    public void setTabs(LinkedList<Tab> tabs)
    {
        _tabs = tabs;
    }

    /**
     * Get the app menu for this configuration
     * @return the appMenu
     */
    public RibbonMenu getAppMenu()
    {
        return _appMenu;
    }

    /**
     * Get the user menu for this configuration
     * @return the userMenu
     */
    public RibbonMenu getUserMenu()
    {
        return _userMenu;
    }
    
    /**
     * Add a new ribbon dependency
     * @param extensionPoint The dependency extension point
     * @param extensionId The dependency extension id
     */
    public void addDependency(String extensionPoint, String extensionId)
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
     * Get the list of dependencies
     * @return The list of extensions id, mapped by extension point
     */
    public Map<String, List<String>> getDependencies()
    {
        return _dependencies;
    }
    
}
