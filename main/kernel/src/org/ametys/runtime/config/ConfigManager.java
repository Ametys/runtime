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
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;
import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.LoggerFactory;
import org.ametys.runtime.util.parameter.AbstractParameterParser;
import org.ametys.runtime.util.parameter.Enumerator;
import org.ametys.runtime.util.parameter.Errors;
import org.ametys.runtime.util.parameter.Parameter;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.util.parameter.Validator;

/**
 * This manager handle the parameters of the application that have to be stored by the plugins.
 */
public final class ConfigManager implements Contextualizable, Serviceable, Initializable
{
    // shared instance
    private static ConfigManager __manager;

    // Logger for traces
    Logger _logger = LoggerFactory.getLoggerFor(ConfigManager.class);
    
    // Avalon stuff
    private ServiceManager _manager;
    private Context _context;

    // Used parameters (Map<id, featureId>)
    private Map<String, String> _usedParamsName;

    // Declared parameters (Map<id, configuration>)
    private Map<String, ConfigParameterInfo> _declaredParams;

    // Typed parameters
    private Map<String, ConfigParameter> _params;
    // The parameters classified by categories and groups
    private Map<I18nizableText, Map<I18nizableText, ParameterGroup>> _categorizedParameters;
    
    // Determines if the extension point is initialized
    private boolean _isInitialized;

    // Determines if all parameters are valued
    private boolean _isComplete;
    
    // ComponentManager for the validators
    private ThreadSafeComponentManager<Validator> _validatorManager;
    
    // ComponentManager for the enumerators
    private ThreadSafeComponentManager<Enumerator> _enumeratorManager;


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
        _usedParamsName = new LinkedHashMap<String, String>();
        _declaredParams = new LinkedHashMap<String, ConfigParameterInfo>();
        _params = new LinkedHashMap<String, ConfigParameter>();
        
        _validatorManager = new ThreadSafeComponentManager<Validator>();
        _validatorManager.enableLogging(LoggerFactory.getLoggerFor("runtime.plugin.threadsafecomponent"));
        _validatorManager.contextualize(_context);
        _validatorManager.service(_manager);
        
        _enumeratorManager = new ThreadSafeComponentManager<Enumerator>();
        _enumeratorManager.enableLogging(LoggerFactory.getLoggerFor("runtime.plugin.threadsafecomponent"));
        _enumeratorManager.contextualize(_context);
        _enumeratorManager.service(_manager);
    }

    /**
     * Registers a new available parameter.<br>
     * The addConfig() method allow to actually select which ones are useful.
     * @param pluginName the name of the plugin defining the parameters
     * @param configuration configuration of the plugin file
     * @throws ConfigurationException if the configuration is not correct
     */
    public void addGlobalConfig(String pluginName, Configuration configuration) throws ConfigurationException
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Adding parameters");
        }

        Configuration[] params = configuration.getChild("config").getChildren("param");
        for (Configuration param : params)
        {
            String id = param.getAttribute("id", null);
            
            if (id == null)
            {
                throw new ConfigurationException("The mandatory attribute 'id' is missing on the config tag, in plugin '" + pluginName + "'", configuration);
            }

            // Check if the parameter is not already declared
            if (_declaredParams.containsKey(id))
            {
                throw new ConfigurationException("The parameter '" + id + "' is already declared. Parameters ids must be unique", configuration);
            }

            // Add the new parameter to the list of the unused parameters
            _declaredParams.put(id, new ConfigParameterInfo(id, pluginName, param));

            if (_logger.isDebugEnabled())
            {
                _logger.debug("Parameter added: " + id);
            }
        }

        if (_logger.isDebugEnabled())
        {
            _logger.debug(params.length + " parameter(s) added");
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

        Configuration config = configuration.getChild("config");
        Configuration[] paramsConfig = config.getChildren("param");

        for (Configuration paramConfig : paramsConfig)
        {
            String id = paramConfig.getAttribute("id", null);
           
            if (id == null)
            {
                throw new ConfigurationException("The mandatory attribute 'id' is missing on the config tag, in feature '" + pluginName + "/" + featureName + "'", configuration);
            }
            
            // Check the parameter is not already declared
            if (_declaredParams.containsKey(id))
            {
                throw new ConfigurationException("The parameter '" + id + "' is already declared. Parameters ids must be unique", configuration);
            }

            _declaredParams.put(id, new ConfigParameterInfo(id, pluginName, paramConfig));
            _usedParamsName.put(id, pluginName + PluginsManager.FEATURE_ID_SEPARATOR + featureName);
        }
        
        Configuration[] refParamsConfig = config.getChildren("param-ref");

        for (Configuration refParamConfig : refParamsConfig)
        {
            String id = refParamConfig.getAttribute("id", null);
           
            if (id == null)
            {
                throw new ConfigurationException("The mandatory attribute 'id' is missing on the config tag, in feature '" + pluginName + "/" + featureName + "'", configuration);
            }

            _usedParamsName.put(id, pluginName + PluginsManager.FEATURE_ID_SEPARATOR + featureName);
        }

        if (_logger.isDebugEnabled())
        {
            _logger.debug(paramsConfig.length + refParamsConfig.length + " parameter(s) selected.");
        }
    }

    /**
     * Ends the initialization of the config parameters, by checking against the
     * already valued parameters.<br>
     * If at least one parameter has no value, the application won't start.
     */
    public void validate()
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Initialization");
        }

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
            if (_logger.isWarnEnabled())
            {
                _logger.warn("Cannot read the configuration file.", e);
            }
            
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
        
        _categorizedParameters = _categorizeParameters(_params);

        try
        {
            configParamParser.lookupComponents();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to lookup parameter local components", e);
        }
        
        _validateParameters(untypedValues);

        _declaredParams.clear();
        _usedParamsName.clear();

        _isInitialized = true;

        Config.setInitialized(_isComplete);
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Initialization ended");
        }
    }
    
    private void _validateParameters(Map<String, String> untypedValues)
    {
        if (_isComplete && untypedValues != null)
        {
            for (Map<I18nizableText, ParameterGroup> groups : _categorizedParameters.values())
            {
                for (ParameterGroup group: groups.values())
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
            default:
            case EQ:
                return comparison == 0;
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
     * SAX the configuration parameters and values into a content handler
     * @param handler Handler for sax events
     * @throws SAXException if an error occurred
     * @throws ProcessingException if an error occurred
     */
    public void toSAX(ContentHandler handler) throws SAXException, ProcessingException
    {
        _saxParameters(handler);
    }

    private Map <I18nizableText, Map<I18nizableText, ParameterGroup>>  _categorizeParameters(Map<String, ConfigParameter> params)
    {
        Map <I18nizableText, Map<I18nizableText, ParameterGroup>> categories = new HashMap<I18nizableText, Map<I18nizableText, ParameterGroup>>();
        
        // Classify parameters by groups and categories
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next();
            ConfigParameter param = params.get(key);

            I18nizableText categoryName = param.getDisplayCategory();
            I18nizableText groupName = param.getDisplayGroup();

            // Get the map of groups of the category
            Map<I18nizableText, ParameterGroup> category = categories.get(categoryName);
            if (category == null)
            {
                category = new TreeMap<I18nizableText, ParameterGroup>(new I18nizableTextComparator());
                categories.put(categoryName, category);
            }

            // Get the map of parameters of the group
            ParameterGroup group = category.get(groupName);
            if (group == null)
            {
                group = new ParameterGroup(groupName);
                category.put(groupName, group);
            }

            group.addParam(param);
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
            
            untypedValues = new HashMap<String, String>();
        }

        // SAX classified parameters
        XMLUtils.startElement(handler, "categories");

        for (I18nizableText categoryKey : _categorizedParameters.keySet())
        {
            Map<I18nizableText, ParameterGroup> category = _categorizedParameters.get(categoryKey);

            XMLUtils.startElement(handler, "category");
            categoryKey.toSAX(handler, "label");

            XMLUtils.startElement(handler, "groups");

            for (I18nizableText groupKey : category.keySet())
            {
                ParameterGroup group = category.get(groupKey);

                XMLUtils.startElement(handler, "group");
                groupKey.toSAX(handler, "label");

                if (group.getSwitch() != null)
                {
                    XMLUtils.createElement(handler, "group-switch", group.getSwitch());
                }
                
                XMLUtils.startElement(handler, "parameters");
                for (ConfigParameter param : group.getParams())
                {
                    Object value = _getValue (param.getId(), param.getType(), untypedValues);

                    AttributesImpl parameterAttr = new AttributesImpl();
                    parameterAttr.addAttribute("", "plugin", "plugin", "CDATA", param.getPluginName());
                    XMLUtils.startElement(handler, param.getId(), parameterAttr);
                    
                    ParameterHelper.toSAXParameterInternal(handler, param, value);
                    if (param.getDisableConditions() != null)
                    {
                        XMLUtils.createElement(handler, "disable-conditions", param.disableConditionsToJSON());
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
                oldUntypedValues = new HashMap<String, String>();
            }
        }
        
        // Typed values
        Map<String, Object> typedValues = new HashMap<String, Object>();

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
        OutputStream os = null;
        try
        {
            // create a transformer for saving sax into a file
            TransformerHandler th = ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();

            // create the result where to write
            File outputFile = new File(fileName);
            outputFile.getParentFile().mkdirs();
            
            os = new FileOutputStream(fileName);
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
        finally
        {
            if (os != null)
            {
                os.close();
            }
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
            Map<I18nizableText, ParameterGroup> category = _categorizedParameters.get(categoryKey);
            StringBuilder categoryLabel = new StringBuilder();
            categoryLabel.append("+\n      | ");
            categoryLabel.append(categoryKey.toString());
            categoryLabel.append("\n      +");
            
            // Commentaire de la categorie courante
            XMLUtils.data(handler, "\n  ");
            handler.comment(categoryLabel.toString().toCharArray(), 0, categoryLabel.length());
            XMLUtils.data(handler, "\n");
            XMLUtils.data(handler, "\n");

            Iterator<I18nizableText> groupIt = category.keySet().iterator();
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

                ParameterGroup group = category.get(groupKey);
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
     * Represent a group of parameters
     */
    class ParameterGroup
    {
        private final Set<ConfigParameter> _groupParams;
        private String _switcher;
        private final I18nizableText _groupLabel;
        
        /**
         * Create a group
         * @param groupLabel The label of the group
         */
        ParameterGroup (I18nizableText groupLabel)
        {
            _groupLabel = groupLabel;
            _groupParams = new TreeSet<ConfigParameter>();
            _switcher = null;
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
        
    class ConfigParameter extends Parameter<ParameterType> implements Comparable<ConfigParameter>
    {
        private I18nizableText _displayCategory;
        private I18nizableText _displayGroup;
        private boolean _groupSwitch;
        private long _order;
        private DisableConditions _disableConditions;
        private final JsonFactory _jsonFactory = new JsonFactory();
        private final ObjectMapper _objectMapper = new ObjectMapper();

        I18nizableText getDisplayCategory()
        {
            return _displayCategory;
        }
        
        void setDisplayCategory(I18nizableText displayCategory)
        {
            _displayCategory = displayCategory;
        }

        I18nizableText getDisplayGroup()
        {
            return _displayGroup;
        }
        
        void setDisplayGroup(I18nizableText displayGroup)
        {
            _displayGroup = displayGroup;
        }
        
        boolean isGroupSwitch()
        {
            return _groupSwitch;
        }
        
        void setGroupSwitch(boolean groupSwitch)
        {
            _groupSwitch = groupSwitch;
        }
        
        long getOrder()
        {
            return _order;
        }
        
        void setOrder(long order)
        {
            _order = order;
        }
        

        /**
         * Retrieves the disable condition.
         * @return the disable condition or <code>null</code> if none is defined.
         */
        public DisableConditions getDisableConditions()
        {
            return _disableConditions;
        }

        /**
         * Sets the disable condition.
         * @param disableConditions the disable condition.
         */
        public void setDisableConditions(DisableConditions disableConditions)
        {
            _disableConditions = disableConditions;
        }
        
        @Override
        public int compareTo(ConfigParameter o)
        {
            int cat = getDisplayCategory().toString().compareTo(o.getDisplayCategory().toString());
            if (cat != 0)
            {
                return cat;
            }
            
            int gro = getDisplayGroup().toString().compareTo(o.getDisplayGroup().toString());
            if (gro != 0)
            {
                return gro;
            }
            
            int ord = ((Long) this.getOrder()).compareTo(o.getOrder());
            if (ord != 0)
            {
                return ord;
            }
            
            return getId().compareTo(o.getId());
        }
        
        /**
         * Formats disable conditions into JSON. 
         * @return the Object as a JSON string.
         */
        public String disableConditionsToJSON()
        {
            try
            {
                StringWriter writer = new StringWriter();
                
                JsonGenerator jsonGenerator = _jsonFactory.createJsonGenerator(writer);
                
                Map<String, Object> asJson = _disableConditionsAsMap(this.getDisableConditions());
                _objectMapper.writeValue(jsonGenerator, asJson);
                
                return writer.toString();
            }
            catch (IOException e)
            {
                throw new IllegalArgumentException("The object can not be converted to json string", e);
            }
        }

        private Map<String, Object> _disableConditionsAsMap(DisableConditions disableConditions)
        {
            Map<String, Object> map = new HashMap<String, Object>();
            
            // Handle simple conditions
            List<Map<String, String>> disableConditionList = new ArrayList<Map<String, String>>();
            map.put("condition", disableConditionList);
            for (DisableCondition disableCondition : disableConditions.getConditions())
            {
                Map<String, String> disableConditionAsMap = _disableConditionAsMap(disableCondition);
                disableConditionList.add(disableConditionAsMap);
            }

            // Handle nested conditions
            List<Map<String, Object>> disableConditionsList = new ArrayList<Map<String, Object>>();
            map.put("conditions", disableConditionsList);
            for (DisableConditions subDisableConditions : disableConditions.getSubConditions())
            {
                Map<String, Object> disableConditionsAsMap = _disableConditionsAsMap(subDisableConditions);
                disableConditionsList.add(disableConditionsAsMap);
            }
            
            // Handle type
            map.put("type", disableConditions.getAssociationType().toString().toLowerCase());
            
            return map; 
        }

        private Map<String, String> _disableConditionAsMap(DisableCondition disableCondition)
        {
            Map<String, String> map = new HashMap<String, String>();
            map.put("id", disableCondition.getId());
            map.put("operator", disableCondition.getOperator().toString().toLowerCase());
            map.put("value", disableCondition.getValue());
            return map;
        }
    }
        
    class ConfigParameterParser extends AbstractParameterParser<ConfigParameter, ParameterType>
    {
        public ConfigParameterParser(ThreadSafeComponentManager<Enumerator> enumeratorManager, ThreadSafeComponentManager<Validator> validatorManager)
        {
            super(enumeratorManager, validatorManager);
        }

        @Override
        protected ConfigParameter _createParameter(Configuration parameterConfig) throws ConfigurationException
        {
            return new ConfigParameter();
        }
        
        @Override
        protected String _parseId(Configuration parameterConfig) throws ConfigurationException
        {
            return parameterConfig.getAttribute("id");
        }
        
        @Override
        protected ParameterType _parseType(Configuration parameterConfig) throws ConfigurationException
        {
            try
            {
                return ParameterType.valueOf(parameterConfig.getAttribute("type").toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                throw new ConfigurationException("Invalid type", parameterConfig, e);
            }
        }
        
        @Override
        protected Object _parseDefaultValue(Configuration parameterConfig, ConfigParameter parameter)
        {
            String value;
            
            Configuration childNode = parameterConfig.getChild("default-value", false);
            if (childNode == null)
            {
                value = null;
            }
            else
            {
                value = childNode.getValue("");
            }
            
            return ParameterHelper.castValue(value, parameter.getType());
        }        
        
        protected DisableConditions _parseDisableConditions(Configuration disableConditionConfiguration) throws ConfigurationException
        {
            if (disableConditionConfiguration == null)
            {
                return null;
            }
             
            DisableConditions conditions = new DisableConditions();

            Configuration[] conditionsConfiguration = disableConditionConfiguration.getChildren();
            for (Configuration conditionConfiguration : conditionsConfiguration)
            {
                String tagName = conditionConfiguration.getName();
                
                // Recursive case
                if (tagName.equals("conditions"))
                {
                    conditions.getSubConditions().add(_parseDisableConditions(conditionConfiguration));
                }
                else if (tagName.equals("condition"))
                {
                    String id = conditionConfiguration.getAttribute("id");
                    DisableCondition.OPERATOR operator = DisableCondition.OPERATOR.valueOf(conditionConfiguration.getAttribute("operator", "eq").toUpperCase());
                    String value = conditionConfiguration.getValue("");
                    
                    
                    DisableCondition condition = new DisableCondition(id, operator, value);
                    conditions.getConditions().add(condition);
                }
            }
            
            conditions.setAssociation(DisableConditions.ASSOCIATION_TYPE.valueOf(disableConditionConfiguration.getAttribute("type", "and").toUpperCase()));
            
            return conditions;
        }
        
        @Override
        protected void _additionalParsing(ServiceManager manager, String pluginName, Configuration parameterConfig, String parameterId, ConfigParameter parameter) throws ConfigurationException
        {
            super._additionalParsing(manager, pluginName, parameterConfig, parameterId, parameter);
            
            parameter.setId(parameterId);
            parameter.setDisplayCategory(_parseI18nizableText(parameterConfig, pluginName, "category"));
            parameter.setDisplayGroup(_parseI18nizableText(parameterConfig, pluginName, "group"));
            parameter.setGroupSwitch(parameterConfig.getAttributeAsBoolean("group-switch", false));
            parameter.setOrder(parameterConfig.getChild("order").getValueAsLong(0));
            parameter.setDisableConditions(_parseDisableConditions(parameterConfig.getChild("disable-conditions", false)));
        }
    }
}
