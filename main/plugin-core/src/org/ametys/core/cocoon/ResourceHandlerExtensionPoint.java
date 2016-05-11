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
package org.ametys.core.cocoon;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;

import org.ametys.runtime.plugin.component.AbstractComponentExtensionPoint;

/**
 * Extension point for resources handler
 */
public class ResourceHandlerExtensionPoint extends AbstractComponentExtensionPoint<ResourceHandler>
{
    /** Avalon Role */
    public static final String ROLE = ResourceHandlerExtensionPoint.class.getName();
    
    private Map<String, String> _idsBySuffixes = new TreeMap<>(new Comparator<String>()
    {
        @Override
        public int compare(String s1, String s2)
        {
            return s1.length() > s2.length() ? -1 : (s1.length() < s2.length() ? 1 : s1.compareTo(s2));
        }
    });
    
    /**
     * Get the extension matching the provided source, based on the registered suffixes
     * @param source The source
     * @return The corresponding extension
     */
    public ResourceHandler getExtensionBySuffix(String source)
    {
        String lowerCaseSource = StringUtils.lowerCase(source);
        
        for (Entry<String, String> idBySuffix : _idsBySuffixes.entrySet())
        {
            String suffix = idBySuffix.getKey();
            if (StringUtils.endsWith(lowerCaseSource, suffix))
            {
                return getExtension(idBySuffix.getValue());
            }
        }
        
        return null;
    }
    
    @Override
    public void addExtension(String id, String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        super.addExtension(id, pluginName, featureName, configuration);
        
        if (configuration.getChild("suffixes").getChildren("suffix").length > 0)
        {
            for (Configuration suffix : configuration.getChild("suffixes").getChildren("suffix"))
            {
                _idsBySuffixes.put(StringUtils.lowerCase(suffix.getValue()), id);
            }
        }
        else
        {
            _idsBySuffixes.put("", id);
        }
    }
}
