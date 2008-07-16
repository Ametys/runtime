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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.ametys.runtime.plugin.component.PluginAware;


/**
 * This default implementation validates the following configurable stuffs :<br/>
 * <ul>
 *  <li>mandatory : check the parameter is set</li>
 *  <li>regexp : check the string parameter respect a regexp</li>
 * </ul> 
 */
public class DefaultValidator extends AbstractLogEnabled implements Validator, Configurable, PluginAware
{
    /** The validator will check if value is not null */
    protected boolean _mandatory;
    /** The validator will check if the value match the following pattern */
    protected Pattern _pattern;

    private String _pluginName;

    public void setPluginInfo(String pluginName, String featureName)
    {
        _pluginName = pluginName;        
    }
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Configuring a " + DefaultValidator.class.getName() + " in plugin '" + _pluginName + "'");
        }
        
        _mandatory = configuration.getChild("mandatory", false) != null;
        
        String regexp = configuration.getChild("regexp").getValue("");
        if (regexp.length() == 0)
        {
            _pattern = null;
        }
        else
        {
            try
            {
                _pattern = Pattern.compile(regexp);
            }
            catch (PatternSyntaxException e)
            {
                String errorMessage = "The configuration of the default validator of config parameters use an incorrect 'regexp'. Initialisation will stop.";
                ConfigurationException ce = new ConfigurationException(errorMessage, configuration, e); 
                getLogger().error(errorMessage, ce);
                throw ce;
            }
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug(DefaultValidator.class.getName() + " configured in plugin '" + _pluginName + "' with mandatory : " + _mandatory  + " and regexp : " + (_pattern != null));
        }
    }
    
    public boolean validate(Object value)
    {
        if (_mandatory && (value == null || value.toString().length() == 0))
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The validator refused a missing or empty value for a mandatory parameter");
            }
            return false;
        }
        
        if (_pattern != null && value != null && value.toString().length() != 0 && !_pattern.matcher(value.toString()).matches())
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The validator refused a value for a parameter that should respect a regexep");
            }
            return false;
        }
        
        return true;
    }
}
