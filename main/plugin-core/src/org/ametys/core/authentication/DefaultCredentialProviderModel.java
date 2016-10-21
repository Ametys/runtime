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
package org.ametys.core.authentication;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterCheckerDescriptor;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * Default implementation of {@link CredentialProviderModel}
 */
public class DefaultCredentialProviderModel implements CredentialProviderModel
{
    private String _id;
    private Class<CredentialProvider> _cpClass;
    private Configuration _cpConfig;
    private I18nizableText _label;
    private I18nizableText _description;
    private I18nizableText _connectionLabel;
    private String _iconGlyph;
    private String _iconDecorator;
    private String _iconSmall;
    private String _iconMedium;
    private String _iconLarge;
    private String _color;
    private Map<String, ? extends Parameter<ParameterType>> _parameters;
    private Map<String, ? extends ParameterCheckerDescriptor> _parameterCheckers;
    private String _pluginName;
    
    /**
     * Constructor
     * @param id The unique identifier of this credential provider model
     * @param udClass The {@link CredentialProvider} class
     * @param cpConfig Additional configuration for {@link CredentialProvider} class. Can be empty.
     * @param label The i18n label
     * @param description The i18n description
     * @param connectionLabel The i18n label for the connection screen
     * @param iconGlyph The CSS class for glyph icon 
     * @param iconDecorator The CSS class for glyph decorator icon
     * @param iconSmall The path of the small icon resource
     * @param iconMedium The path of the medium icon resource
     * @param iconLarge The path of the large icon resource
     * @param color The string representation of the color which will be used for the button in the connection screen
     * @param parameters The parameters
     * @param parameterCheckers the parameter checkers
     * @param pluginName The plugin's name of declaration (for debug purpose)
     */
    public DefaultCredentialProviderModel (String id, Class<CredentialProvider> udClass, Configuration cpConfig, I18nizableText label, I18nizableText description, I18nizableText connectionLabel, String iconGlyph, String iconDecorator, String iconSmall, String iconMedium, String iconLarge, String color, Map<String, ? extends Parameter<ParameterType>> parameters, Map<String, ? extends ParameterCheckerDescriptor> parameterCheckers, String pluginName)
    {
        _id = id;
        _cpClass = udClass;
        _cpConfig = cpConfig;
        _label = label;
        _description = description;
        _connectionLabel = connectionLabel;
        _iconGlyph = iconGlyph;
        _iconDecorator = iconDecorator;
        _iconSmall = iconSmall;
        _iconLarge = iconLarge;
        _color = color;
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
    public I18nizableText getConnectionLabel()
    {
        return _connectionLabel;
    }

    @Override
    public String getIconGlyph()
    {
        return _iconGlyph;
    }

    @Override
    public String getIconDecorator()
    {
        return _iconDecorator;
    }

    @Override
    public String getIconSmall()
    {
        return _iconSmall;
    }

    @Override
    public String getIconMedium()
    {
        return _iconMedium;
    }

    @Override
    public String getIconLarge()
    {
        return _iconLarge;
    }

    @Override
    public String getColor()
    {
        return _color;
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
    public Class<CredentialProvider> getCredentialProviderClass()
    {
        return _cpClass;
    }
    
    @Override
    public Configuration getCredentialProviderConfiguration ()
    {
        return _cpConfig;
    }

}
