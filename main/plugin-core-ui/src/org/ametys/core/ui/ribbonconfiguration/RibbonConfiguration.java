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
import java.util.LinkedList;
import java.util.List;

/**
 * A ribbon configuration, with tab, user and app menus
 */
public class RibbonConfiguration
{
    /** The tabs of the ribbon */
    protected LinkedList<Tab> _tabs = new LinkedList<>();

    /** The App menu elements of the ribbon */
    protected List<Element> _appMenu = new ArrayList<>();
    
    /** The user menu elements of the ribbon */
    protected List<Element> _userMenu = new ArrayList<>();
    
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
     * Get the app menu elements for this configuration
     * @return the appMenu
     */
    public List<Element> getAppMenu()
    {
        return _appMenu;
    }

    /**
     * Get the user menu elements for this configuration
     * @return the userMenu
     */
    public List<Element> getUserMenu()
    {
        return _userMenu;
    }
}
