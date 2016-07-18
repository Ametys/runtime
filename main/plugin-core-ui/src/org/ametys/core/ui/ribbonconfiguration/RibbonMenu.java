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
import java.util.List;

import org.slf4j.Logger;

/**
 * The class represents a menu, containing a list of ribbon elements
 */
public class RibbonMenu
{
    /** The App menu elements of the ribbon */
    protected List<Element> _menuElements = new ArrayList<>();
    
    private RibbonElementsInjectionHelper<Element> _injectionHelper;
    
    /**
     * Type of exclusion in the ribbon configuration
     */
    public enum MENUTYPE
    {
        /** Excluding an extension */
        APPMENU("app-menu"),
        /** Excluding a file */
        USERMENU("user-menu");
        
        private String _value;
        
        private MENUTYPE(String value)
        {
            this._value = value;   
        }
           
        @Override
        public String toString() 
        {
            return _value;
        }   
        
        /**
         * Converts a string to a MENUTYPE
         * @param type The type to convert
         * @return The exclude type corresponding to the string or null if unknown
         */
        public static MENUTYPE createsFromString(String type)
        {
            for (MENUTYPE v : MENUTYPE.values())
            {
                if (v.toString().equals(type))
                {
                    return v;
                }
            }
            return null;
        }
    }
    
    /**
     * Get the list of elements contains in the menu
     * @return The list of elements
     */
    public List<Element> getElements()
    {
        return _menuElements;
    }

    /**
     * Add a list of elements to the menu at the specified order index
     * @param elements The list of elements
     * @param order The order
     * @param logger The logger
     */
    public void addElements(List<Element> elements, String order, Logger logger)
    {
        if (elements.size() == 0)
        {
            return;
        }
        
        if (_injectionHelper == null)
        {
            _injectionHelper = new RibbonElementsInjectionHelper<>(_menuElements, logger);
        }
        
        for (Element element : elements)
        {
            _injectionHelper.injectElements(element, order);
        }
    }
    
}
