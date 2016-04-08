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
package org.ametys.core.user.directory;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterCheckerDescriptor;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * Default implementation of {@link UserDirectoryModel}
 */
public class DefaultUserDirectoryModel implements UserDirectoryModel
{
    private String _id;
    private Class<UserDirectory> _udClass;
    private Configuration _udConfig;
    private I18nizableText _label;
    private I18nizableText _description;
    private Map<String, ? extends Parameter<ParameterType>> _parameters;
    private Map<String, ? extends ParameterCheckerDescriptor> _parameterCheckers;
    private String _pluginName;
    
    /**
     * Constructor
     * @param id The unique identifier of this user directory model
     * @param udClass The {@link UserDirectory} class
     * @param udConfig Additional configuration for {@link UserDirectory} class. Can be empty.
     * @param label The i18n label
     * @param description The i18n description
     * @param parameters the parameters
     * @param parameterCheckers the parameter checkers
     * @param pluginName the plugin's name of declaration (for debug purpose)
     */
    public DefaultUserDirectoryModel(String id, Class<UserDirectory> udClass, Configuration udConfig, I18nizableText label, I18nizableText description, Map<String, ? extends Parameter<ParameterType>> parameters, Map<String, ? extends ParameterCheckerDescriptor> parameterCheckers, String pluginName)
    {
        _id = id;
        _udClass = udClass;
        _udConfig = udConfig;
        _label = label;
        _description = description;
        _parameters = parameters;
        _parameterCheckers = parameterCheckers;
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
    public Map<String, ? extends ParameterCheckerDescriptor> getParameterCheckers()
    {
        return _parameterCheckers;
    }
    
    @Override
    public String getPluginName()
    {
        return _pluginName;
    }
    
    @Override
    public Class<UserDirectory> getUserDirectoryClass()
    {
        return _udClass;
    }
    
    @Override
    public Configuration getUserDirectoryConfiguration()
    {
        return _udConfig;
    }
}

