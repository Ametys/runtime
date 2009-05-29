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
package org.ametys.runtime.plugins.core.user.jdbc;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.LifecycleHelper;

import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.util.LoggerFactory;
import org.ametys.runtime.util.parameter.DefaultValidator;
import org.ametys.runtime.util.parameter.Enumerator;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.StaticEnumerator;
import org.ametys.runtime.util.parameter.Validator;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;


/**
 * Handle a typed parameter for the config extension point.
 */
public class JdbcParameter
{
    // Logger for traces
    private Logger _logger = LoggerFactory.getLoggerFor(JdbcParameter.class);

    private String _pluginName;

    private String _id;
    private String _labelKey;
    private String _descriptionKey;
    private String _column;
    private ParameterType _type;
    private String _widget;
    private Enumerator _enumerator;
    private Validator _validator;

    /**
     * Create a parameter
     * @param pluginName The name of the plugin owner of the parameter (name of its directory)
     * @param id The id
     * @param column The jdbc column
     * @param configuration Configuration giving the parameter
     * @param context Avalon context
     * @param manager Avalon manager
     * @throws ConfigurationException If mandatory parameters are missing, or default value are not matching type
     */
    public JdbcParameter(String pluginName, String id, String column, Configuration configuration, Context context, ServiceManager manager) throws ConfigurationException
    {
        _pluginName = pluginName;

        _id = id;
        _column = column;
        _labelKey = _configureLabelKey(configuration);
        _descriptionKey = _configureDescriptionKey(configuration);
        _type = _configureType(configuration);
        _widget = _configureWidget(configuration);
        _enumerator = _configureEnumerator(configuration, context, manager);
        _validator = _configureValidator(configuration, context, manager);
    }
    
    /**
     * Create a parameter
     * @param pluginName The name of the plugin
     * @param id The parameter id
     * @param column The jdbc column
     * @param labelKey The i18n label key (in plugin catalogue)
     * @param descriptionKey The i18n description key (in plugin catalogue)
     * @param type The type (see <code>TypedParameters</code>)
     * @param widget The widget (can be null)
     * @param enumerator Enumerator of possible values
     * @param validator Validator of entered value
     */
    public JdbcParameter(String pluginName, String id, String column, String labelKey, String descriptionKey, ParameterType type, String widget, Enumerator enumerator, Validator validator)
    {
        _pluginName = pluginName;
        _id = id;
        _column = column;
        _labelKey = labelKey;
        _descriptionKey = descriptionKey;
        _type = type;
        _widget = widget;
        _enumerator = enumerator;
        _validator = validator;
    }
    
    private String _configureLabelKey(Configuration configuration) throws ConfigurationException
    {
        String labelKey = configuration.getChild("LabelKey").getValue("");
        if (labelKey.length() == 0)
        {
            throw new ConfigurationException("The mandatory element 'LabelKey' is missing or empty", configuration);
        }
        return labelKey;
    }

    private String _configureDescriptionKey(Configuration configuration) throws ConfigurationException
    {
        String descriptionKey = configuration.getChild("DescriptionKey").getValue("");
        if (descriptionKey.length() == 0)
        {
            throw new ConfigurationException("The mandatory element 'DescriptionKey' is missing or empty", configuration);
        }
        return descriptionKey;
    }
    
    private ParameterType _configureType(Configuration configuration) throws ConfigurationException
    {
        String typeAsString = configuration.getChild("Type").getValue("");
        if (typeAsString.length() == 0)
        {
            throw new ConfigurationException("The mandatory element 'Type' is missing or empty", configuration);
        }
        
        ParameterType type;
        try
        {
            type = ParameterHelper.stringToType(typeAsString);
        }
        catch (IllegalArgumentException e)
        {
            throw new ConfigurationException("The mandatory element 'Type' references an unknown type", configuration, e);
        }
        return type;
    }
    
    private String _configureWidget(Configuration configuration)
    {
        String widget = configuration.getChild("Widget").getValue(null);
        return widget;
    }
    
    private Enumerator _configureEnumerator(Configuration configuration, Context context, ServiceManager manager) throws ConfigurationException
    {
        Configuration enumeratorConfiguration = configuration.getChild("Enumeration", false);
        if (enumeratorConfiguration != null)
        {
            String enumeratorClassName = enumeratorConfiguration.getAttribute("class", StaticEnumerator.class.getName());
            try
            {
                Enumerator enumerator = (Enumerator) Class.forName(enumeratorClassName).newInstance();
                if (enumerator instanceof PluginAware)
                {
                    ((PluginAware) enumerator).setPluginInfo(_pluginName, null);
                }
                LifecycleHelper.setupComponent(enumerator, _logger, context, manager, enumeratorConfiguration, true);
                
                return enumerator;
            }
            catch (Exception e)
            {
                String errorMessage = "Impossible to instanciate '" + enumeratorClassName + "' to create an enumerator for parameter '" + _id + "' of the plugin '" + _pluginName + "'"; 
                _logger.error(errorMessage, e);
                throw new ConfigurationException(errorMessage, configuration, e);
            }
        }
        
        return null;
    }
    
    private Validator _configureValidator(Configuration configuration, Context context, ServiceManager manager) throws ConfigurationException
    {
        Configuration validationConfiguration = configuration.getChild("Validation", false);
        if (validationConfiguration != null)
        {
            String validationClassName = validationConfiguration.getAttribute("class", DefaultValidator.class.getName());
            try
            {
                Validator validator = (Validator) Class.forName(validationClassName).newInstance();
                if (validator instanceof PluginAware)
                {
                    ((PluginAware) validator).setPluginInfo(_pluginName, null);
                }
                LifecycleHelper.setupComponent(validator, _logger, context, manager, validationConfiguration, true);
                
                return validator;
            }
            catch (Exception e)
            {
                String errorMessage = "Impossible to instanciate '" + validationClassName + "' to create a validator for parameter '" + _id + "' of the plugin '" + _pluginName + "'"; 
                _logger.error(errorMessage, e);
                throw new ConfigurationException(errorMessage, configuration, e);
            }
        }
        
        return null;
    }    
   
    /**
     * Get the label.
     * @return Returns the i18n key of the label.
     */
    public String getLabelKey()
    {
        return _labelKey;
    }

    /**
     * Get the description.
     * @return Returns the i18n key of the description.
     */
    public String getDescriptionKey()
    {
        return _descriptionKey;
    }

    /**
     * Get the widget.
     * @return Returns the widget id.
     */
    public String getWidget()
    {
        return _widget;
    }
    
    /**
     * Get the id.
     * @return Returns the id.
     */
    public String getId()
    {
        return _id;
    }

    /**
     * Get the jdbc column.
     * @return Returns the column.
     */
    public String getColumn()
    {
        return _column;
    }

    /**
     * Get the type.
     * @return Returns the type.
     */
    public ParameterType getType()
    {
        return _type;
    }

    /**
     * Get a textual name for the parameter type
     * @return Returns the name of the type
     */
    public String getTypeAsString()
    {
        try
        {
            return ParameterHelper.typeToString(_type);
        }
        catch (IllegalArgumentException e)
        {
            _logger.error("A config parameter as an unknown type : " + _type, e);
            return "unknwon";
        }
    }

    /**
     * Get the enumerator.
     * @return The enumerator of this parameter or null if this parameter is not
     *         enumerated
     */
    public Enumerator getEnumerator()
    {
        return _enumerator;
    }

    /**
     * Get the validator.
     * @return The validator of this parameter or null if this parameter is not
     *         enumerated
     */
    public Validator getValidator()
    {
        return _validator;
    }
    
    /**
     * Get the plugin name
     * @return Returns the plugin Name.
     */
    public String getPluginName()
    {
        return _pluginName;
    }

    @Override
    public String toString()
    {
        return "'" + getId() + "' (type:    " + getTypeAsString() + ", label ley:    " + getLabelKey() + ", description key:    " + getDescriptionKey()
                + ", widget:    " + getWidget() + ", enumeration:   " + (getEnumerator() != null ? getEnumerator() : "no enumerator")
                + ", validation:   " + (getValidator() != null ? getValidator() : "no validator") + ")";
    }
}
