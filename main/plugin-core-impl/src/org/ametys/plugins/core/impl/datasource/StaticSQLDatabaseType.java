/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.plugins.core.impl.datasource;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.ametys.core.datasource.dbtype.SQLDatabaseType;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;
import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.util.ConfigurationHelper;

/**
 * Default implementation for a {@link SQLDatabaseType}.
 */
public class StaticSQLDatabaseType extends AbstractLogEnabled implements Configurable, SQLDatabaseType, PluginAware
{
    /** The id of the database type */
    private String _id;
    
    /** The label of the database type */
    private I18nizableText _label;

    /** The driver of the database type */
    private String _driver;
    
    /** The url template for this database type */
    private String _template;
    
    /** The plugin's name */
    private String _pluginName;
    
    /** The driver not found message */
    private I18nizableText _driverNotFoundMessage;
    
    public void setPluginInfo(String pluginName, String featureName, String id)
    {
        _pluginName = pluginName;
        _id = id;
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Configuring database type with id '" + _id  + "'");
        }
        
        _label = ConfigurationHelper.parseI18nizableText(configuration.getChild("label"), "plugin." + _pluginName);
        _driver = configuration.getChild("driver").getValue();
        _driverNotFoundMessage = ConfigurationHelper.parseI18nizableText(configuration.getChild("driver-not-found-message"), "plugin." + _pluginName); 
        _template = configuration.getChild("template").getValue("");
    }
    
    @Override
    public String getId()
    {
        return _id;
    }
    
    @Override
    public I18nizableText getLabel() 
    {
        return _label;
    }
    
    @Override
    public String getDriver() 
    {
        return _driver;
    }
    
    @Override
    public String getTemplate()
    {
        return _template;
    }
    
    @Override
    public I18nizableText getDriverNotFoundMessage()
    {
        return _driverNotFoundMessage;
    }
}
