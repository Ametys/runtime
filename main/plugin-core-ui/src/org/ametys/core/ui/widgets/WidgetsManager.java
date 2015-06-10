/*
 *  Copyright 2014 Anyware Services
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
package org.ametys.core.ui.widgets;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

/**
 * This extension point handle the existing widgets on the client side (widgets are fields for cms forms).
 */
public class WidgetsManager extends AbstractThreadSafeComponentExtensionPoint<ClientSideWidget> implements Configurable
{
    /** Avalon role */
    public static final String ROLE = WidgetsManager.class.getName();
    
    /** The configuration key for normal mode */
    public static final String MODE_CONFIG_NORMAL = "normal";
    /** The configuration key for enumerated mode */
    public static final String MODE_CONFIG_ENUMERATED = "enumerated";
    /** The configuration key for single sub mode */
    public static final String MODE_SUBCONFIG_SINGLE = "single";
    /** The configuration key for multiple sub mode */
    public static final String MODE_SUBCONFIG_MULTIPLE = "multiple";
    
    /** The default widgets map. See getDefaultWidgets */
    protected Map<String, Map<String, Map<String, String>>> _defaultWidgets;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        // Preparing
        _defaultWidgets = new HashMap<>();
        _defaultWidgets.put(MODE_CONFIG_NORMAL, new HashMap<String, Map<String, String>>());
        _defaultWidgets.put(MODE_CONFIG_ENUMERATED, new HashMap<String, Map<String, String>>());
        
        _defaultWidgets.get(MODE_CONFIG_NORMAL).put(MODE_SUBCONFIG_SINGLE, _readMap(configuration.getChild(MODE_CONFIG_NORMAL).getChild(MODE_SUBCONFIG_SINGLE).getChildren()));
        _defaultWidgets.get(MODE_CONFIG_NORMAL).put(MODE_SUBCONFIG_MULTIPLE, _readMap(configuration.getChild(MODE_CONFIG_NORMAL).getChild(MODE_SUBCONFIG_MULTIPLE).getChildren()));
        _defaultWidgets.get(MODE_CONFIG_ENUMERATED).put(MODE_SUBCONFIG_SINGLE, _readMap(configuration.getChild(MODE_CONFIG_ENUMERATED).getChild(MODE_SUBCONFIG_SINGLE).getChildren()));
        _defaultWidgets.get(MODE_CONFIG_ENUMERATED).put(MODE_SUBCONFIG_MULTIPLE, _readMap(configuration.getChild(MODE_CONFIG_ENUMERATED).getChild(MODE_SUBCONFIG_MULTIPLE).getChildren()));
        
    }
    
    private Map<String, String> _readMap(Configuration[] children)
    {
        Map<String, String> result = new HashMap<>();
        
        for (Configuration child : children)
        {
            result.put(child.getName(), child.getValue(""));
        }
        
        return result;
    }

    /**
     * Get the default widgets
     * normal/enumerated &lt;-&gt; (single/multiple &lt;-&gt; ftype &lt;-&gt; xtype) 
     * @return The map of default widgets
     */
    public Map<String, Map<String, Map<String, String>>> getDefaultWidgets()
    {
        return _defaultWidgets;
    }
}
