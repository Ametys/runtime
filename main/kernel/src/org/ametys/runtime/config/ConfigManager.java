/*
 *  Copyright 2014 Anyware Services
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
package org.ametys.runtime.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.util.log.SLF4JLoggerAdapter;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.util.I18nizableText;
import org.ametys.runtime.parameter.Enumerator;
import org.ametys.runtime.parameter.Errors;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerDescriptor;
import org.ametys.runtime.parameter.ParameterCheckerParser;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.parameter.Validator;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;

/**
 * This manager handle the parameters of the application that have to be stored by the plugins.
 */
public final class ConfigManager implements Contextualizable, Serviceable, Initializable
{
    // shared instance
    private static ConfigManager __manager;

    // the regular expression for ids
    private static final Pattern __ID_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9.\\-_]*");
    
    // Logger for traces
    Logger _logger = LoggerFactory.getLogger(ConfigManager.class);
    
    // Avalon stuff
    private ServiceManager _manager;
    private Context _context;

    // Used parameters (Map<id, featureId>)
    private Map<String, String> _usedParamsName;

    // Declared parameters (Map<id, configuration>)
    private Map<String, ConfigParameterInfo> _declaredParams;

    // Typed parameters
    private Map<String, ConfigParameter> _params;

    // Parameter checkers info
    private Map<String, ConfigParameterInfo> _declaredParameterCheckers;
        
    // Parsed parameter checkers
    private Map<String, ParameterCheckerDescriptor> _parameterCheckers;
    
    // The parameters classified by categories and groups
    private Map<I18nizableText, ParameterCategory> _categorizedParameters;
    
    // Determines if the extension point is initialized
    private boolean _isInitialized;

    // Determines if all parameters are valued
    private boolean _isComplete;
    
    // ComponentManager for the validators
    private ThreadSafeComponentManager<Validator> _validatorManager;
    
    // ComponentManager for the enumerators
    private ThreadSafeComponentManager<Enumerator> _enumeratorManager;
    
    // ComponentManager for the parameter checkers
    private ThreadSafeComponentManager<ParameterChecker> _parameterCheckerManager;

    
    private ConfigManager()
    {
        // empty constructor
    }

    /**
     * Returns the shared instance of the <code>PluginManager</code>
     * @return the shared instance of the PluginManager
     */
    public static ConfigManager getInstance()
    {
        if (__manager == null)
        {
            __manager = new ConfigManager();
        }

        return __manager;
    }

    /**
     * Returns false if the model is initialized and all parameters are valued
     * @return false if the model is initialized and all parameters are valued
     */
    public boolean isComplete()
    {
        return _isInitialized && _isComplete;
    }
    
    @Override
    public void contextualize(Context context)
    {
        _context = context;
    }
    
    @Override
    public void service(ServiceManager manager)
    {
        _manager = manager;
    }
    
    @Override
    public void initialize() throws Exception
    {
        _usedParamsName = new LinkedHashMap<>();
        _declaredParams = new LinkedHashMap<>();
        _params = new LinkedHashMap<>();
        _declaredParameterCheckers = new LinkedHashMap<>();
        _parameterCheckers = new LinkedHashMap<>();
        
        _validatorManager = new ThreadSafeComponentManager<>();
        _validatorManager.enableLogging(new SLF4JLoggerAdapter(LoggerFactory.getLogger("runtime.plugin.threadsafecomponent")));
        _validatorManager.contextualize(_context);
        _validatorManager.service(_manager);
        
        _enumeratorManager = new ThreadSafeComponentManager<>();
        _enumeratorManager.enableLogging(new SLF4JLoggerAdapter(LoggerFactory.getLogger("runtime.plugin.threadsafecomponent")));
        _enumeratorManager.contextualize(_context);
        _enumeratorManager.service(_manager);
        
        _parameterCheckerManager = new ThreadSafeComponentManager<>();
        _parameterCheckerManager.enableLogging(new SLF4JLoggerAdapter(LoggerFactory.getLogger("runtime.plugin.threadsafecomponent")));
        _parameterCheckerManager.contextualize(_context);
        _parameterCheckerManager.service(_manager);
    }

    /**
     * Registers a new available parameter.<br>
     * The addConfig() method allows to select which ones are actually useful.
     * @param pluginName the name of the plugin defining the parameters
     * @param configuration configuration of the plugin file
     * @throws ConfigurationException if the configuration is not correct
     */
    public void addGlobalConfig(String pluginName, Configuration configuration) throws ConfigurationException
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Adding parameters and parameters checkers");
        }

        Configuration configConfiguration = configuration.getChild("config");
        
        Configuration[] parameterConfigurations = configConfiguration.getChildren("param");
        for (Configuration parameterConfiguration : parameterConfigurations)
        {
            String id = parameterConfiguration.getAttribute("id", null);
            
            if (id == null)
            {
                throw new ConfigurationException("The mandatory attribute 'id' is missing on the config tag, in plugin '" + pluginName + "'", configuration);
            }

            Matcher idMatcher = __ID_PATTERN.matcher(id);
            if (!idMatcher.matches())
            {
                throw new ConfigurationException("The id '" + id + "' does not respect the regular expression '" + __ID_PATTERN + "'");
            }
            
            // Check if the parameter is not already declared
            if (_declaredParams.containsKey(id))
            {
                throw new ConfigurationException("The parameter '" + id + "' is already declared. Parameters ids must be unique", configuration);
            }

            // Add the new parameter to the list of unused parameters
            _declaredParams.put(id, new ConfigParameterInfo(id, pluginName, parameterConfiguration));

            if (_logger.isDebugEnabled())
            {
                _logger.debug("Parameter added: " + id);
            }
        }            
        if (_logger.isDebugEnabled())
        {
            _logger.debug(parameterConfigurations.length + " parameter(s) added");
        }
        
        Configuration[] parameterCheckerConfigurations = configConfiguration.getChildren("param-checker");
        for (Configuration paramCheckerConfiguration : parameterCheckerConfigurations)
        {
            String id = paramCheckerConfiguration.getAttribute("id", null);
            if (id == null)
            {
                throw new ConfigurationException("The mandatory attribute 'id' is missing on the config tag, in plugin '" + pluginName + "'", configuration);
            }
            
            Matcher idMatcher = __ID_PATTERN.matcher(id);
            if (!idMatcher.matches())
            {
                throw new ConfigurationException("The id '" + id + "' does not respect the regular expression '" + __ID_PATTERN + "'");
            }
            
            // Check if the parameter checker is not already declared
            if (_declaredParameterCheckers.containsKey(id))
            {
                throw new ConfigurationException("The parameter checker '" + id + "' is already declared. Parameter checkers ids must be unique", configuration);
            }
            
            // Add the new parameter to the list of the unused parameters
            _declaredParameterCheckers.put(id, new ConfigParameterInfo(id, pluginName, paramCheckerConfiguration));
            
            if (_logger.isDebugEnabled())
            {
                _logger.debug("Parameter checker added: " + id);
            }
        }
        if (_logger.isDebugEnabled())
        {
            _logger.debug(parameterCheckerConfigurations.length + " parameter(s) added");
        }
    }

    /**
     * Registers a new parameter or references a globalConfig parameter.<br>
     * @param pluginName the name of the plugin defining the parameters
     * @param featureName the name of the feature defining the parameters
     * @param configuration configuration of the plugin file
     * @throws ConfigurationException if the configuration is not correct
     */
    public void addConfig(String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Selecting parameters");
        }

        Configuration configConfiguration = configuration.getChild("config");
        
        Configuration[] parametersConfiguration = configConfiguration.getChildren("param");
        for (Configuration parameterConfiguration : parametersConfiguration)
        {
            String id = parameterConfiguration.getAttribute("id", null);
            if (id == null)
            {
                throw new ConfigurationException("The mandatory attribute 'id' is missing on the config tag, in feature '" + pluginName + "/" + featureName + "'", configuration);
            }
            
            Matcher idMatcher = __ID_PATTERN.matcher(id);
            if (!idMatcher.matches())
            {
                throw new ConfigurationException("The id '" + id + "' does not respect the regular expression '" + __ID_PATTERN + "'");
            }
            
            // Check the parameter is not already declared
            if (_declaredParams.containsKey(id))
            {
                throw new ConfigurationException("The parameter '" + id + "' is already declared. Parameters ids must be unique", configuration);
            }

            _declaredParams.put(id, new ConfigParameterInfo(id, pluginName, parameterConfiguration));
            _usedParamsName.put(id, pluginName + PluginsManager.FEATURE_ID_SEPARATOR + featureName);
        }
        
        Configuration[] referenceParametersConfiguration = configConfiguration.getChildren("param-ref");
        for (Configuration refeternceParameterConfiguration : referenceParametersConfiguration)
        {
            String id = refeternceParameterConfiguration.getAttribute("id", null);
           
            if (id == null)
            {
                throw new ConfigurationException("The mandatory attribute 'id' is missing on the config tag, in feature '" + pluginName + "/" + featureName + "'", configuration);
            }

            Matcher idMatcher = __ID_PATTERN.matcher(id);
            if (!idMatcher.matches())
            {
                throw new ConfigurationException("The id '" + id + "' does not respect the regular expression '" + __ID_PATTERN + "'");
            }
            
            _usedParamsName.put(id, pluginName + PluginsManager.FEATURE_ID_SEPARATOR + featureName);
        }

        if (_logger.isDebugEnabled())
        {
            _logger.debug(parametersConfiguration.length + referenceParametersConfiguration.length + " parameter(s) selected.");
        }

        Configuration[] parameterCheckersConfiguration = configConfiguration.getChildren("param-checker");
        for (Configuration parameterCheckerConfig : parameterCheckersConfiguration)
        {
            String id = parameterCheckerConfig.getAttribute("id", null);
            
            if (id == null)
            {
                throw new ConfigurationException("The mandatory attribute 'id' is missing on the parameter checker, in feature '" + pluginName + "/" + featureName + "'", configuration);
            }
            
            Matcher idMatcher = __ID_PATTERN.matcher(id);
            if (!idMatcher.matches())
            {
                throw new ConfigurationException("The id '" + id + "' does not respect the regular expression '" + __ID_PATTERN + "'");
            }
            
            // Check if the parameter checker is not already declared
            if (_declaredParameterCheckers.containsKey(id))
            {
                throw new ConfigurationException("The parameter checker'" + id + "' is already declared. Parameters ids must be unique", configuration);
            }

            _declaredParameterCheckers.put(id, new ConfigParameterInfo(id, pluginName, parameterCheckerConfig));
        }
        if (_logger.isDebugEnabled())
        {
            _logger.debug(parameterCheckersConfiguration.length + " parameter checker(s) added.");
        }
    }

    /**
     * Ends the initialization of the config parameters, by checking against the
     * already valued parameters.<br>
     * If at least one parameter has no value, the application won't start.
     */
    public void validate()
    {
        _logger.debug("Initialization");

        _isInitialized = false;
        _isComplete = true;

        // Dispose potential previous parameters 
        Config.dispose();
        Map<String, String> untypedValues = null;
        try
        {
            untypedValues = Config.read();
        }
        catch (Exception e)
        {
            _logger.error("Cannot read the configuration file.", e);
            _isComplete = false;
        }
        
        ConfigParameterParser configParamParser = new ConfigParameterParser(_enumeratorManager, _validatorManager);
        for (String id : _usedParamsName.keySet())
        {
            // Check if the parameter is not already used
            if (_params.get(id) == null)
            {
                // Move the parameter from the unused list, to the used list
                ConfigParameterInfo info = _declaredParams.get(id);
                
                if (info == null)
                {
                    throw new RuntimeException("The parameter '" + id + "' is used but not declared");
                }
                
                ConfigParameter parameter = null;

                try
                {
                    parameter = configParamParser.parseParameter(_manager, info.getPluginName(), info.getConfiguration());
                }
                catch (ConfigurationException ex)
                {
                    throw new RuntimeException("Unable to configure the config parameter : " + id, ex);
                }
                
                _params.put(id, parameter);
            }
        }
        
        ParameterCheckerParser parameterCheckerParser = new ParameterCheckerParser(_parameterCheckerManager);
        for (String id : _declaredParameterCheckers.keySet())
        {
            boolean invalidParameters = false;
            
            // Check if the parameter checker is not already used
            if (_parameterCheckers.get(id) == null)
            {
                ConfigParameterInfo info = _declaredParameterCheckers.get(id);
                
                ParameterCheckerDescriptor parameterChecker = null;
                try
                {
                    parameterChecker = parameterCheckerParser.parseParameterChecker(info.getPluginName(), info.getConfiguration());
                }
                catch (ConfigurationException ex)
                {
                    throw new RuntimeException("Unable to configure the parameter checker: " + id, ex);
                }
                
                for (String linkedParameterId : parameterChecker.getLinkedParamsIds())
                {
                    // If at least one parameter used is invalid, the parameter checker is invalidated
                    if (_params.get(linkedParameterId) == null)
                    {
                        invalidParameters = true;
                        break;
                    }
                }
                
                if (invalidParameters)
                {
                    if (_logger.isDebugEnabled())
                    {
                        _logger.debug("All the configuration parameters associated to the parameter checker '" + parameterChecker.getId() + "' are not used.\n"
                                    + "This parameter checker will not be used");
                    }
                }
                else
                {
                    _parameterCheckers.put(id, parameterChecker);
                }
            }
        }
        
        _categorizedParameters = _categorizeParameters(_params, _parameterCheckers);

        try
        {
            configParamParser.lookupComponents();
            parameterCheckerParser.lookupComponents();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to lookup parameter local components", e);
        }
        
        _validateParameters(untypedValues);

        _declaredParams.clear();
        _usedParamsName.clear();
        _declaredParameterCheckers.clear();

        _isInitialized = true;

        Config.setInitialized(_isComplete);
        
        _logger.debug("Initialization ended");
    }
    
    private void _validateParameters(Map<String, String> untypedValues)
    {
        if (_isComplete && untypedValues != null)
        {
            for (ParameterCategory category : _categorizedParameters.values())
            {
                for (ParameterGroup group: category.getGroups().values())
                {
                    boolean isGroupSwitchedOn = true;
                    String groupSwitch = group.getSwitch();
                    
                    if (groupSwitch != null)
                    {
                        isGroupSwitchedOn = false;
                        
                        // check if parameter is valued
                        ConfigParameter switcher = _params.get(group.getSwitch());
                        
                        // we can cast directly because we already tested that it should be a boolean while categorizing
                        isGroupSwitchedOn = BooleanUtils.toBoolean((Boolean) _validateParameter(untypedValues, switcher));
                    }
                    
                    // validate parameters if there's no switch, if the switch is on or if the the parameter is not disabled
                    if (groupSwitch == null || isGroupSwitchedOn)
                    {
                        boolean disabled = false;
                        for (ConfigParameter parameter: group.getParams())
                        {
                            DisableConditions disableConditions = parameter.getDisableConditions();
                            disabled = _evaluateDisableConditions(disableConditions, untypedValues);
                            
                            if (!StringUtils.equals(parameter.getId(), group.getSwitch()) && !disabled)
                            {
                                _validateParameter(untypedValues, parameter);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private Object _validateParameter(Map<String, String> untypedValues, ConfigParameter parameter)
    {
        String id = parameter.getId();
        Object value = ParameterHelper.castValue(untypedValues.get(id), parameter.getType());

        if (value == null && !"".equals(untypedValues.get(id)))
        {
            if (_logger.isWarnEnabled())
            {
                _logger.warn("The parameter '" + id + "' is not valued. Configuration is not initialized.");
            }
            
            _isComplete = false;
        }
        else
        {
            Validator v = parameter.getValidator();
            Errors validationErrors = new Errors();
            if (v != null)
            {
                v.validate(value, validationErrors);
            }
            
            if (validationErrors.getErrors().size() > 0)
            {
                if (_logger.isWarnEnabled())
                {
                    StringBuffer sb = new StringBuffer("The parameter '" + id + "' is not valid with value '" + untypedValues.get(id) + "' :");
                    for (I18nizableText error : validationErrors.getErrors())
                    {
                        sb.append("\n* " + error.toString());
                    }
                    sb.append("\nConfiguration is not initialized");
                    
                    _logger.warn(sb.toString());
                }
                
                _isComplete = false;
            }
        }
        
        return value;
    }

    private boolean _evaluateDisableConditions(DisableConditions disableConditions, Map<String, String> untypedValues)
    {
        if (disableConditions == null || disableConditions.getConditions().isEmpty() && disableConditions.getSubConditions().isEmpty())
        {
            return false;
        }
        
        boolean disabled;
        boolean andOperator = disableConditions.getAssociationType() == DisableConditions.ASSOCIATION_TYPE.AND;
        
        // initial value depends on OR or AND associations
        disabled = andOperator;
        
        for (DisableConditions subConditions : disableConditions.getSubConditions())
        {
            boolean result = _evaluateDisableConditions(subConditions, untypedValues);
            disabled = andOperator ?  disabled && result : disabled || result;
        }
        
        for (DisableCondition condition : disableConditions.getConditions())
        {
            boolean result = _evaluateCondition(condition, untypedValues);
            disabled = andOperator ?  disabled && result : disabled || result;
        }
                
        return disabled;
    }
    
    private boolean _evaluateCondition(DisableCondition condition, Map<String, String> untypedValues)
    {
        String id = condition.getId();
        DisableCondition.OPERATOR operator = condition.getOperator();
        String value = condition.getValue();
        
        if (untypedValues.get(id) == null)
        {
            if (_logger.isDebugEnabled())
            {
                _logger.debug("Cannot evaluate the disable condition on the undefined parameter " + id + ".\nReturning false.");
            }
            return false;
        }
        
        ParameterType type = _params.get(id).getType();
        Object parameterValue = ParameterHelper.castValue(untypedValues.get(id), type);
        Object compareValue = ParameterHelper.castValue(value, type);
        if (compareValue == null)
        {
            throw new IllegalStateException("Cannot convert '" + value + "' to a '" + type + "' for parameter '" + id + "'");
        }
        
        if (!(parameterValue instanceof Comparable) || !(compareValue instanceof Comparable))
        {
            throw new IllegalStateException("values '" + untypedValues.get(id) + "' and '" + compareValue + "' of type'" + type + "' for parameter '" + id + "' are not comparable");
        }

        @SuppressWarnings("unchecked")
        Comparable<Object> comparableParameterValue = (Comparable<Object>) parameterValue;
        @SuppressWarnings("unchecked")
        Comparable<Object> comparableCompareValue = (Comparable<Object>) compareValue;

        int comparison = comparableParameterValue.compareTo(comparableCompareValue);
        switch (operator)
        {
            case NEQ:
                return comparison != 0;
            case GEQ:
                return comparison >= 0;
            case GT:
                return comparison > 0;
            case LT:
                return comparison < 0;
            case LEQ:
                return comparison <= 0;
            case EQ:
            default:
                return comparison == 0;
        }
    }
                
    /**
     * Dispose the manager before restarting it
     */
    public void dispose()
    {
        _isInitialized = false;
        _isComplete = true;
        
        _declaredParams = null;
        _params = null;
        _usedParamsName = null;
        _validatorManager.dispose();
        _validatorManager = null;
        _enumeratorManager.dispose();
        _enumeratorManager = null;
        _parameterCheckerManager.dispose();
        _parameterCheckerManager = null;
    }
    
    /**
     * Get the id of the config parameters. Use get to retrieve the parameter
     * @return An array of String containing the id of the parameters existing in the model
     */
    public String[] getParametersIds()
    {
        Set<String> keySet = _params.keySet();
        String[] array;
        synchronized (_params)
        {
            array = new String[keySet.size()];
            _params.keySet().toArray(array);
        }
        return array;
    }

    /**
     * Gets the config parameter by its id
     * @param id Id of the config parameter to get
     * @return The config parameter.
     */
    public ConfigParameter get(String id)
    {
        return _params.get(id);
    }

    /**
     * Gets the typed configuration parameters
     * @return the _params map 
     */
    public Map<String, ConfigParameter> getParameters()
    {
        return this._params;
    }
    
    /**
     * Gets the parameter checker with its id
     * @param id the id of the parameter checker to get
     * @return the associated parameter checker descriptor
     */
    public ParameterCheckerDescriptor getParameterChecker(String id)
    {
        return _parameterCheckers.get(id);
    }
    
    /**
     * SAX the configuration parameters and values into a content handler
     * @param handler Handler for sax events
     * @throws SAXException if an error occurred
     * @throws ProcessingException if an error occurred
     */
    public void toSAX(ContentHandler handler) throws SAXException, ProcessingException
    {
        _saxParameters(handler);
    }

    private Map<I18nizableText, ParameterCategory> _categorizeParameters(Map<String, ConfigParameter> params, Map<String, ParameterCheckerDescriptor> paramCheckers)
    {
        Map<I18nizableText, ParameterCategory> categories = new HashMap<> ();
        
        // Classify parameters by groups and categories
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next();
            ConfigParameter param = params.get(key);

            I18nizableText categoryName = param.getDisplayCategory();
            I18nizableText groupName = param.getDisplayGroup();

            // Get the map of groups of the category
            ParameterCategory category = categories.get(categoryName);
            if (category == null)
            {
                category = new ParameterCategory();
                categories.put(categoryName, category);
            }

            // Get the map of parameters of the group
            ParameterGroup group = category.getGroups().get(groupName);
            if (group == null)
            {
                group = new ParameterGroup(groupName);
                category.getGroups().put(groupName, group);
            }

            group.addParam(param);
        }
        
        // Add parameter checkers to groups and categories
        Iterator<String> paramCheckersIt = paramCheckers.keySet().iterator();
        while (paramCheckersIt.hasNext())
        {
            String key = paramCheckersIt.next();
            ParameterCheckerDescriptor paramChecker = paramCheckers.get(key);
            
            I18nizableText uiCategory = paramChecker.getUiRefCategory();
            if (uiCategory != null)
            {
                ParameterCategory category = categories.get(uiCategory);
                if (category == null)
                {
                    if (_logger.isDebugEnabled())
                    {
                        _logger.debug("The category " + uiCategory.toString() + " doesn't exist,"
                                + " thus the parameter checker" + paramChecker.getId() + "will not be added");
                    }
                }
                else
                {
                    I18nizableText uiGroup = paramChecker.getUiRefGroup();
                    if (uiGroup == null)
                    {
                        category.addParamChecker(paramChecker);
                    }
                    else
                    {
                        ParameterGroup group = category.getGroups().get(uiGroup);
                        if (group == null)
                        {
                            if (_logger.isDebugEnabled())
                            {
                                _logger.debug("The group " + uiGroup.toString() + " doesn't exist."
                                        + " thus the parameter checker" + paramChecker.getId() + "will not be added");
                            }
                        } 
                        else
                        { 
                            group.addParamChecker(paramChecker);
                        }
                    }
                } 
            }
        }
        
        return categories;
    }

    private void _saxParameters(ContentHandler handler) throws SAXException, ProcessingException
    {
        // Get configuration parameters
        Map<String, String> untypedValues;
        try
        {
            untypedValues = Config.read();
        }
        catch (Exception e)
        {
            if (_logger.isWarnEnabled())
            {
                _logger.warn("Config values are unreadable. Proposing default values", e);
            }
            
            untypedValues = new HashMap<>();
        }

        // SAX classified parameters
        XMLUtils.startElement(handler, "categories");

        for (I18nizableText categoryKey : _categorizedParameters.keySet())
        {
            ParameterCategory category = _categorizedParameters.get(categoryKey);

            XMLUtils.startElement(handler, "category");
            categoryKey.toSAX(handler, "label");

            Set<ParameterCheckerDescriptor> paramCheckersCategories = category.getParamCheckers();
            if (paramCheckersCategories != null)
            {
                for (ParameterCheckerDescriptor paramChecker : paramCheckersCategories)
                {
                    _saxParameterChecker(handler, paramChecker);
                }
            }
            
            XMLUtils.startElement(handler, "groups");

            for (I18nizableText groupKey : category.getGroups().keySet())
            {
                ParameterGroup group = category.getGroups().get(groupKey);

                XMLUtils.startElement(handler, "group");
                groupKey.toSAX(handler, "label");

                if (group.getSwitch() != null)
                {
                    XMLUtils.createElement(handler, "group-switch", group.getSwitch());
                }
                
                Set<ParameterCheckerDescriptor> paramCheckersGroups = group.getParamCheckers();
                if (paramCheckersGroups != null)
                {
                    for (ParameterCheckerDescriptor paramChecker : paramCheckersGroups)
                    {
                        _saxParameterChecker(handler, paramChecker);
                    }
                }
                
                XMLUtils.startElement(handler, "parameters");
                for (ConfigParameter param : group.getParams())
                {
                    String paramId = param.getId();
                    Object value = _getValue (param.getId(), param.getType(), untypedValues);

                    AttributesImpl parameterAttr = new AttributesImpl();
                    parameterAttr.addAttribute("", "plugin", "plugin", "CDATA", param.getPluginName());
                    XMLUtils.startElement(handler, paramId, parameterAttr);
                    
                    ParameterHelper.toSAXParameterInternal(handler, param, value);
                    if (param.getDisableConditions() != null)
                    {
                        XMLUtils.createElement(handler, "disable-conditions", param.disableConditionsToJSON());
                    }
                    
                    // Sax parameter checkers attached to a single parameter
                    for (String paramCheckerId : _parameterCheckers.keySet())
                    {
                        ParameterCheckerDescriptor paramChecker = _parameterCheckers.get(paramCheckerId);
                        String uiRefParamId = paramChecker.getUiRefParamId();
                        if (uiRefParamId != null && uiRefParamId.equals(paramId))
                        {
                            _saxParameterChecker(handler, paramChecker);
                        }
                    }
                    
                    XMLUtils.endElement(handler, param.getId());
                }
                XMLUtils.endElement(handler, "parameters");
                
                XMLUtils.endElement(handler, "group");
            }

            XMLUtils.endElement(handler, "groups");

            XMLUtils.endElement(handler, "category");
        }

        XMLUtils.endElement(handler, "categories");
    }

    private void _saxParameterChecker(ContentHandler handler, ParameterCheckerDescriptor paramChecker) throws SAXException
    {
        I18nizableText uiRefGroup = paramChecker.getUiRefGroup();
        I18nizableText uiRefCategory = paramChecker.getUiRefCategory();
        int order = paramChecker.getUiRefOrder();       
        
        XMLUtils.startElement(handler, "param-checker");
        XMLUtils.valueOf(handler, paramChecker.toJson());
        paramChecker.getLabel().toSAX(handler, "label");
        paramChecker.getDescription().toSAX(handler, "description");
      
        XMLUtils.createElement(handler, "order", Integer.toString(order));
        if (uiRefGroup != null)
        {
            uiRefGroup.toSAX(handler, "group");
        }
        
        if (uiRefCategory != null)
        {
            uiRefCategory.toSAX(handler, "category");
        }
        XMLUtils.endElement(handler, "param-checker");
    }
    
    private Object _getValue (String paramID, ParameterType type, Map<String, String> untypedValues)
    {
        final String unverifiedUntypedValue = untypedValues.get(paramID);
        
        Object typedValue;        
        if (unverifiedUntypedValue == null)
        {
            typedValue = null;
        }
        else if (StringUtils.isEmpty(unverifiedUntypedValue))
        {
            typedValue = "";
        }
        else 
        {
            typedValue = ParameterHelper.castValue(unverifiedUntypedValue, type);
        }
        
        if (type.equals(ParameterType.PASSWORD) && typedValue != null && ((String) typedValue).length() > 0)
        {
            typedValue = "PASSWORD";
        }
        
        return typedValue;
    }


    /**
     * Update the configuration file with the given values<br>
     * Values are untyped (all are of type String) and might be null.
     * @param untypedValues A map (key, untyped value).
     * @param fileName the config file absolute path
     * @throws Exception If an error occured while saving values
     */
    public void save(Map<String, String> untypedValues, String fileName) throws Exception
    {
        // Retrieve the old values for password purposes
        Map<String, String> oldUntypedValues = null;
        if (Config.getInstance() == null)
        {
            try
            {
                oldUntypedValues = Config.read();
            }
            catch (Exception e)
            {
                oldUntypedValues = new HashMap<>();
            }
        }
        
        // Typed values
        Map<String, Object> typedValues = new HashMap<>();

        String[] ids = getParametersIds();
        for (String id : ids)
        {
            String untypedValue = untypedValues.get(id);
            
            Object typedValue = ParameterHelper.castValue(untypedValue, _params.get(id).getType());
            
            if (typedValue == null && _params.get(id).getType() == ParameterType.PASSWORD)
            {
                if (Config.getInstance() != null)
                {
                    // keeps the value of an empty password field
                    typedValue = Config.getInstance().getValueAsString(id);
                }
                else if (oldUntypedValues != null)
                {
                    typedValue = oldUntypedValues.get(id);
                }
            }

            typedValues.put(id, typedValue);
        }

        // SAX
        // create the result where to write
        File outputFile = new File(fileName);
        outputFile.getParentFile().mkdirs();
        
        try (OutputStream os = new FileOutputStream(fileName))
        {
            // create a transformer for saving sax into a file
            TransformerHandler th = ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();
            
            StreamResult result = new StreamResult(os);
            th.setResult(result);

            // create the format of result
            Properties format = new Properties();
            format.put(OutputKeys.METHOD, "xml");
            format.put(OutputKeys.INDENT, "yes");
            format.put(OutputKeys.ENCODING, "UTF-8");
            format.put(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");
            th.getTransformer().setOutputProperties(format);

            // sax the config into the transformer
            _toSAX(th, typedValues);
        }
        catch (Exception e)
        {
            throw new Exception("An error occured while saving the config values.", e);
        }
    }

    /**
     * SAX the config values into a content handler
     * @param handler Handler where to sax
     * @param typedValues Map (key, typed value) to sax
     * @throws SAXException if an error occurred
     */
    private void _toSAX(TransformerHandler handler, Map<String, Object> typedValues) throws SAXException
    {
        handler.startDocument();
        XMLUtils.startElement(handler, "config");
        
        Iterator<I18nizableText> catIt = _categorizedParameters.keySet().iterator();
        while (catIt.hasNext())
        {
            I18nizableText categoryKey = catIt.next();
            ParameterCategory category = _categorizedParameters.get(categoryKey);
            StringBuilder categoryLabel = new StringBuilder();
            categoryLabel.append("+\n      | ");
            categoryLabel.append(categoryKey.toString());
            categoryLabel.append("\n      +");
            
            // Commentaire de la categorie courante
            XMLUtils.data(handler, "\n  ");
            handler.comment(categoryLabel.toString().toCharArray(), 0, categoryLabel.length());
            XMLUtils.data(handler, "\n");
            XMLUtils.data(handler, "\n");

            Iterator<I18nizableText> groupIt = category.getGroups().keySet().iterator();
            while (groupIt.hasNext())
            {
                I18nizableText groupKey = groupIt.next();
                StringBuilder groupLabel = new StringBuilder();
                groupLabel.append(" ");
                groupLabel.append(groupKey.toString());
                groupLabel.append(" ");

                // Commentaire du group courant
                XMLUtils.data(handler, "  ");
                handler.comment(groupLabel.toString().toCharArray(), 0, groupLabel.length());
                XMLUtils.data(handler, "\n  ");

                ParameterGroup group = category.getGroups().get(groupKey);
                for (ConfigParameter param: group.getParams())
                {
                    Object typedValue = typedValues.get(param.getId());
                    
                    String untypedValue = ParameterHelper.valueToString(typedValue);
                    if (untypedValue == null)
                    {
                        untypedValue = "";
                    }

                    XMLUtils.createElement(handler, param.getId(), untypedValue);
                }
                
                if (groupIt.hasNext())
                {
                    XMLUtils.data(handler, "\n");
                }
            }
            
            XMLUtils.data(handler, "\n");
        }

        XMLUtils.endElement(handler, "config");
        handler.endDocument();
    }
    
    class I18nizableTextComparator implements Comparator<I18nizableText>
    {
        @Override
        public int compare(I18nizableText t1, I18nizableText t2)
        {
            return t1.toString().compareTo(t2.toString());
        }
    }
    
    class ConfigParameterInfo
    {
        private final String _id;
        private final String _pluginName;
        private final Configuration _conf;
        
        ConfigParameterInfo(String id, String pluginName, Configuration conf)
        {
            _id = id;
            _conf = conf;
            _pluginName = pluginName;
        }
        
        String getId()
        {
            return _id;
        }
        
        String getPluginName()
        {
            return _pluginName;
        }
        
        Configuration getConfiguration()
        {
            return _conf;
        }
    }
    
    /**
     * 
     * Represents a category of parameters
     */
    class ParameterCategory
    {
        private Map<I18nizableText, ParameterGroup> _groups;
        private Set<ParameterCheckerDescriptor> _paramCheckers;
       
        ParameterCategory ()
        {
            _groups = new HashMap<>();
            _paramCheckers = new HashSet<>();
        }
        
        void addParamChecker(ParameterCheckerDescriptor paramChecker)
        {
            _paramCheckers.add(paramChecker);
        }

        Map<I18nizableText, ParameterGroup> getGroups()
        {
            return _groups;
        }
        
        void setGroups(Map<I18nizableText, ParameterGroup> groups)
        {
            this._groups = groups;
        }
        
        Set<ParameterCheckerDescriptor> getParamCheckers()
        {
            return _paramCheckers;
        }
        
        void setParamCheckers(Set<ParameterCheckerDescriptor> paramCheckers)
        {
            this._paramCheckers = paramCheckers;
        }
    }

    /**
     * Represent a group of parameters
     */
    class ParameterGroup
    {
        private final Set<ConfigParameter> _groupParams;
        private String _switcher;
        private final I18nizableText _groupLabel;
        private final Set<ParameterCheckerDescriptor> _paramCheckers;
        
        /**
         * Create a group
         * @param groupLabel The label of the group
         */
        ParameterGroup (I18nizableText groupLabel)
        {
            _groupLabel = groupLabel;
            _groupParams = new TreeSet<>();
            _switcher = null;
            _paramCheckers = new HashSet<>();
        }
        
        void addParam(ConfigParameter param)
        {
            _groupParams.add(param);
            
            if (param.isGroupSwitch())
            {
                if (_switcher == null)
                {
                    _switcher = param.getId();
                    if (param.getType() != ParameterType.BOOLEAN)
                    {
                        throw new RuntimeException("The group '" + _groupLabel.toString() + "' has a switch '" + _switcher + "' that is not valid because it is not a boolean.");
                    }
                }
                else
                {
                    throw new RuntimeException("At least two group-switches have been defined for the configuration group '" + _groupLabel.toString() + "'. These parameters are '" + _switcher + "' and '" + param.getId() + "'.");
                }
            }
        }
        
        void addParamChecker(ParameterCheckerDescriptor paramChecker)
        {
            _paramCheckers.add(paramChecker);
        }
        
        Set<ParameterCheckerDescriptor> getParamCheckers()
        {
            return _paramCheckers;
        }
        
        I18nizableText getLabel()
        {
            return _groupLabel;
        }
        
        Set<ConfigParameter> getParams()
        {
            return _groupParams;
        }
        
        String getSwitch()
        {
            return _switcher;
        }
    }
}
