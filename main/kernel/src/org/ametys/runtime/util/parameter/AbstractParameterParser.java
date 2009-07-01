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

import java.util.regex.PatternSyntaxException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;
import org.ametys.runtime.util.I18nizableText;

/**
 * {@link Parameter} parser from an XML configuration.
 * @param <P> the actual type of parameter.
 * @param <T> the actual type of parameter type.
 */
public abstract class AbstractParameterParser<P extends Parameter<T>, T>
{
    /** The enumerators component manager. */
    protected ThreadSafeComponentManager<Enumerator> _enumeratorManager;
    /** The validators component manager. */
    protected ThreadSafeComponentManager<Validator> _validatorManager;

    /**
     * Creates an AbstractParameterParser.
     * @param enumeratorManager the enumerator component manager.
     * @param validatorManager the validator component manager.
     */
    public AbstractParameterParser(ThreadSafeComponentManager<Enumerator> enumeratorManager, ThreadSafeComponentManager<Validator> validatorManager)
    {
        _enumeratorManager = enumeratorManager;
        _validatorManager = validatorManager;
    }

    /**
     * Parses a parameter from a XML configuration.
     * @param manager the service manager.
     * @param pluginName the plugin name declaring this parameter.
     * @param parameterConfig the XML configuration.
     * @return the parsed parameter.
     * @throws ConfigurationException if the configuration is not valid.
     */
    public P parseParameter(ServiceManager manager, String pluginName, Configuration parameterConfig) throws ConfigurationException
    {
        P parameter = _createParameter();
        String parameterId = _parseId(parameterConfig);
        
        parameter.setPluginName(pluginName);
        parameter.setLabel(_parseI18nizableText(parameterConfig, pluginName, "label"));
        parameter.setDescription(_parseI18nizableText(parameterConfig, pluginName, "description"));
        parameter.setType(_parseType(parameterConfig));
        parameter.setWidget(_parseWidget(parameterConfig));
        parameter.setEnumerator(_parseEnumerator(pluginName, parameterId, parameterConfig));
        parameter.setValidator(_parseValidator(pluginName, parameterId, parameterConfig));
        parameter.setDefaultValue(_parseDefaultValue(parameterConfig, parameter));
        
        _additionalParsing(manager, pluginName, parameterConfig, parameterId, parameter);
        
        return parameter;
    }

    /**
     * Create the parameter to populate it.
     * @return the parameter instantiated.
     */
    protected abstract P _createParameter();
    
    /**
     * Parses the id.
     * @param parameterConfig the parameter configuration to use.
     * @return the id.
     * @throws ConfigurationException if the configuration is not valid.
     */
    protected abstract String _parseId(Configuration parameterConfig) throws ConfigurationException;

    /**
     * Parses an i18n text.
     * @param config the configuration to use.
     * @param pluginName the current plugin name.
     * @param name the child name.
     * @return the i18n text.
     * @throws ConfigurationException if the configuration is not valid.
     */
    protected I18nizableText _parseI18nizableText(Configuration config, String pluginName, String name) throws ConfigurationException
    {
        Configuration textConfig = config.getChild(name);
        boolean i18nSupported = textConfig.getAttributeAsBoolean("i18n", false);
        String text = textConfig.getValue();
        
        if (i18nSupported)
        {
            String catalogue = textConfig.getAttribute("catalogue", null);
            
            if (catalogue == null)
            {
                catalogue = "plugin." + pluginName;
            }
            
            return new I18nizableText(catalogue, text);
        }
        else
        {
            return new I18nizableText(text);
        }
    }

    /**
     * Parses the type.
     * @param parameterConfig the parameter configuration to use.
     * @return the type.
     * @throws ConfigurationException if the configuration is not valid.
     */
    protected abstract T _parseType(Configuration parameterConfig) throws ConfigurationException;

    /**
     * Parses the widget.
     * @param parameterConfig the parameter configuration to use.
     * @return the widget or <code>null</code> if none defined.
     * @throws ConfigurationException if the configuration is not valid.
     */
    protected String _parseWidget(Configuration parameterConfig) throws ConfigurationException
    {
        return parameterConfig.getChild("widget").getValue(null);
    }

    /**
     * Parses the enumerator.
     * @param pluginName the plugin name.
     * @param parameterId the parameter id.
     * @param parameterConfig the parameter configuration.
     * @return the enumerator or <code>null</code> if none defined.
     * @throws ConfigurationException if the configuration is not valid.
     */
    @SuppressWarnings("unchecked")
    protected Enumerator _parseEnumerator(String pluginName, String parameterId, Configuration parameterConfig) throws ConfigurationException
    {
        Enumerator enumerator = null;
        Configuration enumeratorConfig = parameterConfig.getChild("enumeration", false);
        
        if (enumeratorConfig != null)
        {
            Configuration customEnumerator = enumeratorConfig.getChild("custom-enumerator", false);
            
            if (customEnumerator != null)
            {
                String enumeratorClassName = customEnumerator.getAttribute("class");
                
                try
                {
                    Class enumeratorClass = Class.forName(enumeratorClassName);
                    _enumeratorManager.addComponent(pluginName, null, parameterId, enumeratorClass, parameterConfig);
                    return _enumeratorManager.lookup(parameterId);
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("Unable to instantiate enumerator for class: " + enumeratorClassName, e);
                }
            }
            else
            {
                StaticEnumerator staticEnumerator = new StaticEnumerator();
                
                for (Configuration entryConfig : enumeratorConfig.getChildren("entry"))
                {
                    String value = entryConfig.getChild("value").getValue();
                    I18nizableText label = null;
                    
                    if (entryConfig.getChild("label", false) != null)
                    {
                        label = _parseI18nizableText(entryConfig, pluginName, "label");
                    }
                    
                    staticEnumerator.add(label, value);
                }
                
                enumerator = staticEnumerator;
            }
        }
        
        return enumerator;
    }

    /**
     * Parses the validator.
     * @param pluginName the plugin name.
     * @param parameterId the parameter id.
     * @param parameterConfig the parameter configuration.
     * @return the validator or <code>null</code> if none defined.
     * @throws ConfigurationException if the configuration is not valid.
     */
    @SuppressWarnings("unchecked")
    protected Validator _parseValidator(String pluginName, String parameterId, Configuration parameterConfig) throws ConfigurationException
    {
        Validator validator = null;
        Configuration validatorConfig = parameterConfig.getChild("validation", false);
        
        if (validatorConfig != null)
        {
            Configuration customValidator = validatorConfig.getChild("custom-validator", false);
            
            if (customValidator != null)
            {
                String validatorClassName = customValidator.getAttribute("class");
                
                try
                {
                    Class validatorClass = Class.forName(validatorClassName);
                    _validatorManager.addComponent(pluginName, null, parameterId, validatorClass, parameterConfig);
                    return _validatorManager.lookup(parameterId);
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("Unable to instantiate validator for class: " + validatorClassName, e);
                }
            }
            else
            {
                
                boolean isMandatory = validatorConfig.getChild("mandatory", false) != null;
                String regexp = validatorConfig.getChild("regexp").getValue(null);

                try
                {
                    validator = new DefaultValidator(regexp, isMandatory);
                }
                catch (PatternSyntaxException e)
                {
                    throw new ConfigurationException("Parameter configuration contains a invalid regexp", validatorConfig, e);
                }
            }
        }
        
        return validator;
    }

    /**
     * Parses the default value.
     * @param parameterConfig the parameter configuration.
     * @param parameter the parameter.
     * @return the default value or <code>null</code> if none defined.
     * @throws ConfigurationException if the configuration is not valid.
     */
    protected abstract Object _parseDefaultValue(Configuration parameterConfig, P parameter);

    /**
     * Called for additional parsing.<br>
     * Default implementation does nothing.
     * @param manager the sservice manager.
     * @param pluginName the plugin name.
     * @param parameterConfig the parameter configuration.
     * @param parameterId the parameter id.
     * @param parameter the parameter to populate.
     * @throws ConfigurationException if the configuration is not valid.
     */
    protected void _additionalParsing(ServiceManager manager, String pluginName, Configuration parameterConfig, String parameterId, P parameter) throws ConfigurationException
    {
        // Nothing to do
    }
}
