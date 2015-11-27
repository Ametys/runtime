/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.runtime.parameter;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.parameter.DefaultValidator;
import org.ametys.core.parameter.StaticEnumerator;
import org.ametys.core.util.ConfigurationHelper;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;

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
    private final Map<P, String> _validatorsToLookup = new HashMap<>();
    private final Map<P, String> _enumeratorsToLookup = new HashMap<>();

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
        P parameter = _createParameter(parameterConfig);
        String parameterId = _parseId(parameterConfig);
        
        parameter.setId(parameterId);
        parameter.setPluginName(pluginName);
        parameter.setLabel(_parseI18nizableText(parameterConfig, pluginName, "label"));
        parameter.setDescription(_parseI18nizableText(parameterConfig, pluginName, "description"));
        parameter.setType(_parseType(parameterConfig));
        parameter.setWidget(_parseWidget(parameterConfig));
        parameter.setWidgetParameters(_parseWidgetParameters(parameterConfig, pluginName));
        _parseAndSetEnumerator(pluginName, parameter, parameterId, parameterConfig);
        _parseAndSetValidator(pluginName, parameter, parameterId, parameterConfig);
        parameter.setDefaultValue(_parseDefaultValue(parameterConfig, parameter));
        
        _additionalParsing(manager, pluginName, parameterConfig, parameterId, parameter);
        
        return parameter;
    }

    /**
     * Retrieves local validators and enumerators components and set them into
     * previous parameter parsed.
     * @throws Exception if an error occurs.
     */
    public void lookupComponents() throws Exception
    {
        _validatorManager.initialize();
        _enumeratorManager.initialize();
        
        for (Map.Entry<P, String> entry : _validatorsToLookup.entrySet())
        {
            P parameter = entry.getKey();
            String validatorRole = entry.getValue();
            
            try
            {
                parameter.setValidator(_validatorManager.lookup(validatorRole));
            }
            catch (ComponentException e)
            {
                throw new Exception("Unable to lookup validator role: '" + validatorRole + "' for parameter: " + parameter, e);
            }
        }
        
        for (Map.Entry<P, String> entry : _enumeratorsToLookup.entrySet())
        {
            P parameter = entry.getKey();
            String enumeratorRole = entry.getValue();
            
            try
            {
                parameter.setEnumerator(_enumeratorManager.lookup(enumeratorRole));
            }
            catch (ComponentException e)
            {
                throw new Exception("Unable to lookup enumerator role: '" + enumeratorRole + "' for parameter: " + parameter, e);
            }
        }
    }

    /**
     * Create the parameter to populate it.
     * @param parameterConfig the parameter configuration to use.
     * @return the parameter instantiated.
     * @throws ConfigurationException if the configuration is not valid.
     */
    protected abstract P _createParameter(Configuration parameterConfig) throws ConfigurationException;
    
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
        return ConfigurationHelper.parseI18nizableText(config.getChild(name), "plugin." + pluginName);
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
     * Parses the widget's parameters
     * @param parameterConfig the parameter configuration to use.
     * @param pluginName the current plugin name.
     * @return the widget's parameters in a Map
     * @throws ConfigurationException if the configuration is not valid.
     */
    protected Map<String, I18nizableText> _parseWidgetParameters(Configuration parameterConfig, String pluginName) throws ConfigurationException
    {
        Map<String, I18nizableText> widgetParams = new HashMap<>();
        
        Configuration widgetParamsConfig = parameterConfig.getChild("widget-params", false);
        if (widgetParamsConfig != null)
        {
            Configuration[] params = widgetParamsConfig.getChildren("param");
            for (Configuration paramConfig : params)
            {
                boolean i18nSupported = paramConfig.getAttributeAsBoolean("i18n", false);
                if (i18nSupported)
                {
                    String catalogue = paramConfig.getAttribute("catalogue", null);
                    
                    if (catalogue == null)
                    {
                        catalogue = "plugin." + pluginName;
                    }
                    
                    widgetParams.put(paramConfig.getAttribute("name"), new I18nizableText(catalogue, paramConfig.getValue()));
                }
                else
                {
                    widgetParams.put(paramConfig.getAttribute("name"), new I18nizableText(paramConfig.getValue("")));
                }
            }
        }
        
        return widgetParams;
    }

    /**
     * Parses the enumerator.
     * @param pluginName the plugin name.
     * @param parameter the parameter.
     * @param parameterId the parameter id.
     * @param parameterConfig the parameter configuration.
     * @throws ConfigurationException if the configuration is not valid.
     */
    @SuppressWarnings("unchecked")
    protected void _parseAndSetEnumerator(String pluginName, P parameter, String parameterId, Configuration parameterConfig) throws ConfigurationException
    {
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
                }
                catch (Exception e)
                {
                    throw new ConfigurationException("Unable to instantiate enumerator for class: " + enumeratorClassName, e);
                }

                // This enumerator will be affected later when validatorManager
                // will be initialized in lookupComponents() call
                _enumeratorsToLookup.put(parameter, parameterId);
                
            }
            else
            {
                StaticEnumerator staticEnumerator = new StaticEnumerator();
                
                for (Configuration entryConfig : enumeratorConfig.getChildren("entry"))
                {
                    String value = entryConfig.getChild("value").getValue("");
                    I18nizableText label = null;
                    
                    if (entryConfig.getChild("label", false) != null)
                    {
                        label = _parseI18nizableText(entryConfig, pluginName, "label");
                    }
                    
                    staticEnumerator.add(label, value);
                }
                
                parameter.setEnumerator(staticEnumerator);
            }
        }
    }

    /**
     * Parses the validator.
     * @param pluginName the plugin name.
     * @param parameter the parameter.
     * @param parameterId the parameter id.
     * @param parameterConfig the parameter configuration.
     * @throws ConfigurationException if the configuration is not valid.
     */
    @SuppressWarnings("unchecked")
    protected void _parseAndSetValidator(String pluginName, P parameter, String parameterId, Configuration parameterConfig) throws ConfigurationException
    {
        Configuration validatorConfig = parameterConfig.getChild("validation", false);
        
        if (validatorConfig != null)
        {
            String validatorClassName = StringUtils.defaultIfBlank(validatorConfig.getChild("custom-validator").getAttribute("class", ""), DefaultValidator.class.getName());
            
            try
            {
                Class validatorClass = Class.forName(validatorClassName);
                _validatorManager.addComponent(pluginName, null, parameterId, validatorClass, parameterConfig);
            }
            catch (Exception e)
            {
                throw new ConfigurationException("Unable to instantiate validator for class: " + validatorClassName, e);
            }

            // Will be affected later when validatorManager will be initialized
            // in lookupComponents() call
            _validatorsToLookup.put(parameter, parameterId);
        }
    }

    /**
     * Parses the default value.
     * @param parameterConfig the parameter configuration.
     * @param parameter the parameter.
     * @return the default value or <code>null</code> if none defined.
     * @throws ConfigurationException if the configuration is not valid.
     */
    protected abstract Object _parseDefaultValue(Configuration parameterConfig, P parameter) throws ConfigurationException;

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
