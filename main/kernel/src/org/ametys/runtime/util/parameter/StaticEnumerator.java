/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.util.parameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.util.I18nizableText;


/**
 * This implementation enumerate the static configured elements
 */
public class StaticEnumerator extends AbstractLogEnabled implements Enumerator, Configurable, PluginAware
{
    private Collection<EnumeratorValue> _staticValues;
    private String _pluginName;
    
    public Collection<EnumeratorValue> getValues()
    {
        return _staticValues;
    }
    
    public void setPluginInfo(String pluginName, String featureName)
    {
        _pluginName = pluginName;
    }
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Configuring a " + StaticEnumerator.class.getName() + " in plugin '" + _pluginName + "'");
        }
        
        Collection<EnumeratorValue> values = new ArrayList<EnumeratorValue>();
        
        Configuration[] valuesConfigurations = configuration.getChildren();
        for (Configuration valueConfiguration : valuesConfigurations)
        {
            String value = valueConfiguration.getName();
            I18nizableText text = null;
            
            boolean i18n = valueConfiguration.getAttributeAsBoolean("i18n", true);
            if (i18n)
            {
                String key = valueConfiguration.getValue("");
                String catalogue = valueConfiguration.getAttribute("catalogue", "plugin." + _pluginName);
                text = new I18nizableText(catalogue, key);
            }
            else
            {
                String label = valueConfiguration.getValue("");
                text = new I18nizableText(label);
            }
            
            values.add(new EnumeratorValue(value, text));
        }
        
        _staticValues = Collections.unmodifiableCollection(values);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(StaticEnumerator.class.getName() + " in plugin '" + _pluginName + "' is configured with " + _staticValues.size() + " value(s)");
        }
    }
}
