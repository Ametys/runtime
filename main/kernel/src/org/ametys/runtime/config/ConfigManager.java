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
package org.ametys.runtime.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.Enumerator;
import org.ametys.runtime.parameter.Errors;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.parameter.Validator;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;

/**
 * This manager handle the parameters of the application that have to be stored by the plugins.
 */
public final class ConfigManager implements Contextualizable, Serviceable, Initializable
{
    /** the regular expression for ids */
    public static final Pattern CONFIG_ID_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9.\\-_]*");
    
    /** The field separator for the field hierarchy */
    private static final String FIELD_SEPARATOR = "/";
    
    // shared instance
    private static ConfigManager __manager;

    // Logger for traces
    Logger _logger = LoggerFactory.getLogger(ConfigManager.class);
    
    // Avalon stuff
    private ServiceManager _manager;
    private Context _context;
    
    // Used parameters
    private Collection<String> _usedParamsName;

    // Declared parameters (Map<id, configuration>)
    private Map<String, ConfigParameterInfo> _declaredParams;

    // Typed parameters
    private Map<String, ConfigParameter> _params;

    // Parameter checkers info
    private Map<String, ConfigParameterInfo> _declaredParameterCheckers;
        
    // Parsed parameter checkers
    private Map<String, ConfigParameterCheckerDescriptor> _parameterCheckers;
    
    // The parameters classified by categories and groups
    private Map<I18nizableText, ConfigParameterCategory> _categorizedParameters;
    
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
     * Returns true if the model is initialized and all parameters are valued
     * @return true if the model is initialized and all parameters are valued
     */
    public boolean isComplete()
    {
        return _isInitialized && _isComplete;
    }
    
    /**
     * Returns true if the config file does not exist
     * @return true if the config file does not exist
     */
    public boolean isEmpty()
    {
        return !Config.getFileExists();
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
    public void initialize()
    {
        _usedParamsName = new ArrayList<>();
        _declaredParams = new LinkedHashMap<>();
        _params = new LinkedHashMap<>();
        _declaredParameterCheckers = new LinkedHashMap<>();
        _parameterCheckers = new LinkedHashMap<>();
        
        _validatorManager = new ThreadSafeComponentManager<>();
        _validatorManager.setLogger(LoggerFactory.getLogger("runtime.plugin.threadsafecomponent"));
        _validatorManager.contextualize(_context);
        _validatorManager.service(_manager);
        
        _enumeratorManager = new ThreadSafeComponentManager<>();
        _enumeratorManager.setLogger(LoggerFactory.getLogger("runtime.plugin.threadsafecomponent"));
        _enumeratorManager.contextualize(_context);
        _enumeratorManager.service(_manager);
        
        _parameterCheckerManager = new ThreadSafeComponentManager<>();
        _parameterCheckerManager.setLogger(LoggerFactory.getLogger("runtime.plugin.threadsafecomponent"));
        _parameterCheckerManager.contextualize(_context);
        _parameterCheckerManager.service(_manager);
    }
    
    /**
     * Registers new available parameters.<br>
     * The addConfig() method allows to select which ones are actually useful.
     * @param pluginName the name of the plugin defining the parameters
     * @param parameters the config parameters definition
     * @param paramCheckers the parameters checkers definition
     */
    public void addGlobalConfig(String pluginName, Map<String, ConfigParameterInfo> parameters, Map<String, ConfigParameterInfo> paramCheckers)
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Adding parameters and parameters checkers for plugin " + pluginName);
        }

        for (String id : parameters.keySet())
        {
            ConfigParameterInfo info = parameters.get(id);
            
            // Check if the parameter is not already declared
            if (_declaredParams.containsKey(id))
            {
                throw new IllegalArgumentException("The config parameter '" + id + "' is already declared. Parameters ids must be unique");
            }

            // Add the new parameter to the list of declared parameters
            _declaredParams.put(id, info);

            if (_logger.isDebugEnabled())
            {
                _logger.debug("Parameter added: " + id);
            }
        }
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug(parameters.size() + " parameter(s) added");
        }
        
        for (String id : paramCheckers.keySet())
        {
            ConfigParameterInfo info = paramCheckers.get(id);
            
            // Check if the parameter is not already declared
            if (_declaredParams.containsKey(id))
            {
                throw new IllegalArgumentException("The parameter checker '" + id + "' is already declared. Parameter checkers ids must be unique.");
            }

            // Add the new parameter to the list of declared parameters checkers
            _declaredParameterCheckers.put(id, info);

            if (_logger.isDebugEnabled())
            {
                _logger.debug("Parameter checker added: " + id);
            }
        }
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug(paramCheckers.size() + " parameter checker(s) added");
        }
    }

    /**
     * Registers a new parameter or references a globalConfig parameter.<br>
     * @param featureId the id of the feature defining the parameters
     * @param parameters the config parameters definition
     * @param parametersReferences references to already defined parameters
     * @param paramCheckers the parameters checkers definition
     */
    public void addConfig(String featureId, Map<String, ConfigParameterInfo> parameters, Collection<String> parametersReferences, Map<String, ConfigParameterInfo> paramCheckers)
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Selecting parameters for feature " + featureId);
        }

        for (String id : parameters.keySet())
        {
            ConfigParameterInfo info = parameters.get(id);
            
            // Check if the parameter is not already declared
            if (_declaredParams.containsKey(id))
            {
                throw new IllegalArgumentException("The config parameter '" + id + "' is already declared. Parameters ids must be unique");
            }

            // Add the new parameter to the list of unused parameters
            _declaredParams.put(id, info);
            _usedParamsName.add(id);

            if (_logger.isDebugEnabled())
            {
                _logger.debug("Parameter added: " + id);
            }
        }
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug(parameters.size() + " parameter(s) added");
        }
        
        for (String id : parametersReferences)
        {
            _usedParamsName.add(id);
        }
        
        for (String id : paramCheckers.keySet())
        {
            ConfigParameterInfo info = paramCheckers.get(id);
            
            // Check if the parameter is not already declared
            if (_declaredParams.containsKey(id))
            {
                throw new IllegalArgumentException("The parameter checker '" + id + "' is already declared. Parameter checkers ids must be unique.");
            }

            // Add the new parameter to the list of unused parameters
            _declaredParameterCheckers.put(id, info);

            if (_logger.isDebugEnabled())
            {
                _logger.debug("Parameter checker added: " + id);
            }
        }
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug(paramCheckers.size() + " parameter checker(s) added");
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
        for (String id : _usedParamsName)
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
        
        ConfigParameterCheckerParser parameterCheckerParser = new ConfigParameterCheckerParser(_parameterCheckerManager);
        for (String id : _declaredParameterCheckers.keySet())
        {
            boolean invalidParameters = false;
            
            // Check if the parameter checker is not already used
            if (_parameterCheckers.get(id) == null)
            {
                ConfigParameterInfo info = _declaredParameterCheckers.get(id);
                
                ConfigParameterCheckerDescriptor parameterChecker = null;
                try
                {
                    
                    parameterChecker = parameterCheckerParser.parseParameterChecker(info.getPluginName(), info.getConfiguration());
                }
                catch (ConfigurationException ex)
                {
                    throw new RuntimeException("Unable to configure the parameter checker: " + id, ex);
                }
                
                for (String linkedParameterPath : parameterChecker.getLinkedParamsPaths())
                {
                    ConfigParameter linkedParameter = null;

                    // Linked parameters can be declared with an absolute path, in which case they are prefixed with '/
                    if (linkedParameterPath.startsWith(FIELD_SEPARATOR))
                    {
                        linkedParameter = _params.get(linkedParameterPath.substring(FIELD_SEPARATOR.length()));
                    }
                    else
                    {
                        linkedParameter = _params.get(linkedParameterPath);
                    }
                    
                    // If at least one parameter used is invalid, the parameter checker is invalidated
                    if (linkedParameter == null)
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
                                    + "This parameter checker will be ignored");
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
            for (ConfigParameterCategory category : _categorizedParameters.values())
            {
                for (ConfigParameterGroup group: category.getGroups().values())
                {
                    boolean isGroupSwitchedOn = true;
                    String groupSwitch = group.getSwitch();
                    
                    if (groupSwitch != null)
                    {
                        // Check if group switch is active
                        ConfigParameter switcher = _params.get(group.getSwitch());
                        isGroupSwitchedOn = (Boolean) ParameterHelper.castValue(untypedValues.get(switcher.getId()), switcher.getType());
                    }
                    
                    // validate parameters if there's no switch, if the switch is on or if the the parameter is not disabled
                    if (isGroupSwitchedOn)
                    {
                        boolean disabled = false;
                        for (ConfigParameter parameter: group.getParams(true))
                        {
                            DisableConditions disableConditions = parameter.getDisableConditions();
                            disabled = evaluateDisableConditions(disableConditions, untypedValues);
                            
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
            
            // Make sure valued configuration parameters with an enumerator have their value in the enumeration values
            Enumerator enumerator = parameter.getEnumerator();
            if (enumerator != null)
            {
                I18nizableText entry = null;
                try
                {
                    entry = enumerator.getEntry(ParameterHelper.valueToString(value));
                }
                catch (Exception e)
                {
                    if (_logger.isWarnEnabled())
                    {
                        _logger.warn("The value '" + value + "' for the parameter '" + id + "' led to an exception. Configuration is not initialized." , e);
                    }
                    
                    _isComplete = false;
                }
                
                if (entry == null)
                {
                    if (_logger.isWarnEnabled())
                    {
                        _logger.warn("The value '" + value + "' for the parameter '" + id + "' is not allowed. Configuration is not initialized.");
                    }
                    
                    _isComplete = false;
                }
            }
            
        }
        
        return value;
    }

    /**
     * Recursively evaluate the {@link DisableConditions} against the configuration values
     * @param disableConditions the disable conditions to evaluate
     * @param untypedValues the untyped configuration values
     * @return true if the disable conditions are true, false otherwise
     */
    public boolean evaluateDisableConditions(DisableConditions disableConditions, Map<String, String> untypedValues)
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
            boolean result = evaluateDisableConditions(subConditions, untypedValues);
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
        if (_validatorManager != null)
        {
            _validatorManager.dispose();
            _validatorManager = null;
        }
        if (_enumeratorManager != null)
        {
            _enumeratorManager.dispose();
            _enumeratorManager = null;
        }
        if (_parameterCheckerManager != null)
        {
            _parameterCheckerManager.dispose();
            _parameterCheckerManager = null;
        }
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
     * Returns typed config values.
     * @return typed config values.
     */
    public Map<String, Object> getValues()
    {
        Map<String, Object> result = new HashMap<>();
        
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
                _logger.warn("Config values are unreadable. Using default values", e);
            }
            
            untypedValues = new HashMap<>();
        }

        for (String parameterId : _params.keySet())
        {
            ConfigParameter param = _params.get(parameterId);
            Object value = _getValue (parameterId, param.getType(), untypedValues);
            
            if (value != null)
            {
                result.put(parameterId, value);
            }
        }
        
        return result;
    }
    
    /**
     * Returns all {@link ConfigParameter} grouped by categories and groups.
     * @return all {@link ConfigParameter}.
     */
    public Map<I18nizableText, ConfigParameterCategory> getCategories()
    {
        return _categorizedParameters;
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
     * Returns all {@link ConfigParameterCheckerDescriptor}s.
     * @return all {@link ConfigParameterCheckerDescriptor}s.
     */
    public Map<String, ConfigParameterCheckerDescriptor> getParameterCheckers()
    {
        return _parameterCheckers;
    }
    
    /**
     * Gets the parameter checker with its id
     * @param id the id of the parameter checker to get
     * @return the associated parameter checker descriptor
     */
    public ConfigParameterCheckerDescriptor getParameterChecker(String id)
    {
        return _parameterCheckers.get(id);
    }
    
    private Map<I18nizableText, ConfigParameterCategory> _categorizeParameters(Map<String, ConfigParameter> params, Map<String, ConfigParameterCheckerDescriptor> paramCheckers)
    {
        Map<I18nizableText, ConfigParameterCategory> categories = new HashMap<> ();
        
        // Classify parameters by groups and categories
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next();
            ConfigParameter param = params.get(key);

            I18nizableText categoryName = param.getDisplayCategory();
            I18nizableText groupName = param.getDisplayGroup();

            // Get the map of groups of the category
            ConfigParameterCategory category = categories.get(categoryName);
            if (category == null)
            {
                category = new ConfigParameterCategory();
                categories.put(categoryName, category);
            }

            // Get the map of parameters of the group
            ConfigParameterGroup group = category.getGroups().get(groupName);
            if (group == null)
            {
                group = new ConfigParameterGroup(groupName);
                category.getGroups().put(groupName, group);
            }

            group.addParam(param);
        }
        
        // Add parameter checkers to groups and categories
        Iterator<String> paramCheckersIt = paramCheckers.keySet().iterator();
        while (paramCheckersIt.hasNext())
        {
            String key = paramCheckersIt.next();
            ConfigParameterCheckerDescriptor paramChecker = paramCheckers.get(key);
            
            I18nizableText uiCategory = paramChecker.getUiRefCategory();
            if (uiCategory != null)
            {
                ConfigParameterCategory category = categories.get(uiCategory);
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
                        ConfigParameterGroup group = category.getGroups().get(uiGroup);
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
     * @return errors The fields in error
     * @throws Exception If an error occurred while saving values
     */
    public Map<String, Errors> save(Map<String, String> untypedValues, String fileName) throws Exception
    {
        Map<String, Errors> errorFields = new HashMap<>();
        
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
        
        // Bind and validate parameters
        Map<String, Object> typedValues = _bindAndValidateParameters (untypedValues, oldUntypedValues, errorFields);
        
        if (errorFields.size() > 0)
        {
            if (_logger.isDebugEnabled())
            {
                _logger.debug("Failed to save configuration because of invalid parameter values");
            }
            
            return errorFields;
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
        
        return Collections.EMPTY_MAP;
    }
    
    /**
     * Bind all parameters to typed values and for each, if enabled, validate it
     * @param untypedValues The untyped values (from client-side)
     * @param oldUntypedValues The old untyped values (before saving)
     * @param errorFields The parameters in errors to be completed by validation process
     * @return The typed values
     */
    private Map<String, Object> _bindAndValidateParameters (Map<String, String> untypedValues, Map<String, String> oldUntypedValues, Map<String, Errors> errorFields)
    {
        Map<String, Object> typedValues = new HashMap<>();
        
        // Iterate over categorized parameters
        for (ConfigParameterCategory category : _categorizedParameters.values())
        {
            for (ConfigParameterGroup group: category.getGroups().values())
            {
                boolean isGroupSwitchedOn = true;
                String groupSwitch = group.getSwitch();
                
                if (groupSwitch != null)
                {
                    // Check if group switch is active
                    ConfigParameter switcher = _params.get(groupSwitch);
                    isGroupSwitchedOn = (Boolean) ParameterHelper.castValue(untypedValues.get(switcher.getId()), switcher.getType());
                }
                
                for (ConfigParameter parameter: group.getParams(true))
                {
                    String paramId = parameter.getId();
                    Object typedValue = ParameterHelper.castValue(untypedValues.get(paramId), parameter.getType());
                    typedValues.put(parameter.getId(), typedValue);
                    
                    if (typedValue == null && parameter.getType() == ParameterType.PASSWORD)
                    {
                        if (Config.getInstance() != null)
                        {
                            // keeps the value of an empty password field
                            typedValue = Config.getInstance().getValueAsString(paramId);
                        }
                        else if (oldUntypedValues != null)
                        {
                            typedValue = oldUntypedValues.get(paramId);
                        }
                    }
                    
                    typedValues.put(paramId, typedValue);
                    
                    DisableConditions disableConditions = parameter.getDisableConditions();
                    boolean disabled = !isGroupSwitchedOn || evaluateDisableConditions(disableConditions, untypedValues);
                    
                    if (!StringUtils.equals(parameter.getId(), group.getSwitch()) && !disabled)
                    {
                        Validator validator = parameter.getValidator();
                        
                        if (validator != null)
                        {
                            Errors errors = new Errors();
                            validator.validate(typedValue, errors);
                            
                            if (errors.hasErrors())
                            {
                                if (_logger.isDebugEnabled())
                                {
                                    _logger.debug("The configuration parameter '" + parameter.getId() + "' is not valid");
                                }
                                errorFields.put(parameter.getId(), errors);
                            }
                        }
                    }
                }
            }
        }
        
        return typedValues;
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
            ConfigParameterCategory category = _categorizedParameters.get(categoryKey);
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

                ConfigParameterGroup group = category.getGroups().get(groupKey);
                for (ConfigParameter param: group.getParams(true))
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
}
