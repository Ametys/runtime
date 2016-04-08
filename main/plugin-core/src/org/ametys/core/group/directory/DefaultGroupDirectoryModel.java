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
package org.ametys.core.group.directory;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * Default implementation of {@link GroupDirectoryModel}
 */
public class DefaultGroupDirectoryModel implements GroupDirectoryModel
{
    private String _id;
    private Class<GroupDirectory> _groupDirectoryClass;
    private Configuration _groupDirectoryConfig;
    private I18nizableText _label;
    private I18nizableText _description;
    private Map<String, ? extends Parameter<ParameterType>> _parameters;
    private String _pluginName;
    
    /**
     * Constructor
     * @param id The unique identifier of this group directory model
     * @param groupDirectoryClass The {@link GroupDirectory} class
     * @param groupDirectoryConfig Additional configuration for {@link GroupDirectory} class. Can be empty.
     * @param label The i18n label
     * @param description The i18n description
     * @param parameters the parameters
     * @param pluginName the plugin's name of declaration (for debug purpose)
     */
    public DefaultGroupDirectoryModel (String id, Class<GroupDirectory> groupDirectoryClass, Configuration groupDirectoryConfig, I18nizableText label, I18nizableText description, Map<String, ? extends Parameter<ParameterType>> parameters, String pluginName)
    {
        _id = id;
        _groupDirectoryClass = groupDirectoryClass;
        _groupDirectoryConfig = groupDirectoryConfig;
        _label = label;
        _description = description;
        _parameters = parameters;
        _pluginName = pluginName;
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
    public I18nizableText getDescription()
    {
        return _description;
    }
    
    @Override
    public Map<String, ? extends Parameter<ParameterType>> getParameters()
    {
        return _parameters;
    }
    
    @Override
    public String getPluginName()
    {
        return _pluginName;
    }
    
    @Override
    public Class<GroupDirectory> getGroupDirectoryClass()
    {
        return _groupDirectoryClass;
    }
    
    @Override
    public Configuration getGroupDirectoryConfiguration()
    {
        return _groupDirectoryConfig;
    }
}
