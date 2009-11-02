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
package org.ametys.runtime.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
import org.apache.cocoon.xml.XMLUtils;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;
import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.LoggerFactory;
import org.ametys.runtime.util.parameter.AbstractParameterParser;
import org.ametys.runtime.util.parameter.Enumerator;
import org.ametys.runtime.util.parameter.Parameter;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.Validator;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;

/**
 * This manager handle the parameters of the application that have to be stored, by the plugins.
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

    // Paramètres utilisés (Map<id, featureId>)
    private Map<String, String> _usedParamsName;

    // Paramètres déclarés (Map<id, configuration>)
    private Map<String, ConfigParameterInfo> _declaredParams;

    // Map de paramètres typés (ConfigParameter)
    private Map<String, ConfigParameter> _params;

    // Determines if the extension point is initialized
    private boolean _isInitialized;

    // Determines if all parameters are valued
    private boolean _isComplete;
    
    // ComponentManager pour les Validator
    private ThreadSafeComponentManager<Validator> _validatorManager;
    
    //ComponentManager pour les Enumerator
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
    
    public void contextualize(Context context)
    {
        _context = context;
    }
    
    public void service(ServiceManager manager)
    {
        _manager = manager;
    }
    
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
        for (int i = 0; i < params.length; i++)
        {
            String id = params[i].getAttribute("id", null);
            
            if (id == null)
            {
                throw new ConfigurationException("The mandatory attribute 'id' is missing on the config tag, in plugin '" + pluginName + "'", configuration);
            }

            // Check the parameter is not already declared
            if (_declaredParams.containsKey(id))
            {
                throw new ConfigurationException("The parameter '" + id + "' is already declared. Parameters ids must be unique", configuration);
            }

            // Add the new parameter to the list of the unused parameters
            _declaredParams.put(id, new ConfigParameterInfo(id, pluginName, params[i]));

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
            _logger.debug((paramsConfig.length + refParamsConfig.length) + " parameter(s) selected.");
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

        // On réinitialise la config pour éviter des fuites de mémoire
        Config.dispose();
        
        Map untypedValues = null;
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

                // check if parameter is valued
                if (_isComplete && untypedValues != null)
                {
                    Object value = ParameterHelper.castValue((String) untypedValues.get(id), parameter.getType());

                    if (value == null)
                    {
                        if (_logger.isWarnEnabled())
                        {
                            _logger.warn("The parameter '" + id + "' is not valued. Configuration is not initialized.");
                        }
                        
                        _isComplete = false;
                    }
                }
            }
        }

        try
        {
            configParamParser.lookupComponents();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to lookup parameter local components", e);
        }
        
        // On libère les ressources
        _declaredParams.clear();
        _usedParamsName.clear();

        _isInitialized = true;

        Config.setInitialized(_isComplete);
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Initialization ended");
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
     * Get the id of the config parameters. Use get to retrive the parameter
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
     * Get the config parameter by its id
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
        _saxParameters(_categorizeParameters(), handler);
    }

    private Map<I18nizableText, Map<I18nizableText, List<ConfigParameter>>> _categorizeParameters()
    {
        Map<I18nizableText, Map<I18nizableText, List<ConfigParameter>>> categories = new HashMap<I18nizableText, Map<I18nizableText, List<ConfigParameter>>>();

        // Classify parameters by groups and categories
        Iterator<String> it = _params.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next();
            ConfigParameter param = get(key);

            I18nizableText categoryName = param.getDisplayCategory();
            I18nizableText groupName = param.getDisplayGroup();

            // Get the map of groups of the category
            Map<I18nizableText, List<ConfigParameter>> category = categories.get(categoryName);
            if (category == null)
            {
                category = new HashMap<I18nizableText, List<ConfigParameter>>();
                categories.put(categoryName, category);
            }

            // Get the map of parameters of the group
            List<ConfigParameter> group = category.get(groupName);
            if (group == null)
            {
                group = new ArrayList<ConfigParameter>();
                category.put(groupName, group);
            }

            group.add(param);
        }
        return categories;
    }

    private void _saxParameters(Map<I18nizableText, Map<I18nizableText, List<ConfigParameter>>> categories, ContentHandler handler) throws SAXException, ProcessingException
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

        for (I18nizableText categoryKey : categories.keySet())
        {
            Map<I18nizableText, List<ConfigParameter>> category = categories.get(categoryKey);

            XMLUtils.startElement(handler, "category");
            categoryKey.toSAX(handler, "label");

            XMLUtils.startElement(handler, "groups");

            for (I18nizableText groupKey : category.keySet())
            {
                List<ConfigParameter> group = category.get(groupKey);

                XMLUtils.startElement(handler, "group");
                groupKey.toSAX(handler, "label");

                XMLUtils.startElement(handler, "parameters");
                Iterator<ConfigParameter> gIt = group.iterator();
                while (gIt.hasNext())
                {
                    ConfigParameter param = gIt.next();
                    Object value = _getValue (param.getId(), param.getType(), untypedValues);
                    ParameterHelper.toSAXParameter (handler, param, value);
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
        Object typedValue = ParameterHelper.castValue(unverifiedUntypedValue, type);
        
        if (type.equals(ParameterType.PASSWORD) && typedValue != null && ((String) typedValue).length() > 0)
        {
            typedValue = "PASSWORD";
        }
        
        if (type.equals(ParameterType.STRING) && typedValue != null && ((String) typedValue).length() > 0)
        {
            typedValue = ((String) typedValue).replaceAll("\\\\", "\\\\\\\\");
        }
        
        return typedValue;
    }


    /**
     * Update the configuartion file with the given values<br>
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
        for (int i = 0; i < ids.length; i++)
        {
            String id = ids[i];
            String untypedValue = untypedValues.get(id);
            
            Object typedValue = ParameterHelper.castValue(untypedValue, _params.get(id).getType());
            if (typedValue == null && _params.get(id).getType() == ParameterType.PASSWORD)
            {
                if (Config.getInstance() != null)
                {
                    // garde la valeur d'un champ password vide
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
     * SAX the config values into a contant handler
     * @param handler Handler where to sax
     * @param typedValues Map (key, typed value) to sax
     * @throws SAXException if an error occured
     */
    private void _toSAX(TransformerHandler handler, Map<String, Object> typedValues) throws SAXException
    {
        handler.startDocument();
        XMLUtils.startElement(handler, "config");
        
        Map<I18nizableText, Map<I18nizableText, List<ConfigParameter>>> categories = _categorizeParameters();
        Iterator<I18nizableText> catIt = categories.keySet().iterator();
        while (catIt.hasNext())
        {
            I18nizableText categoryKey = catIt.next();
            Map<I18nizableText, List<ConfigParameter>> category = categories.get(categoryKey);
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

                List<ConfigParameter> group = category.get(groupKey);
                
                Iterator<ConfigParameter> gIt = group.iterator();
                while (gIt.hasNext())
                {
                    ConfigParameter param = gIt.next();
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
    
    class ConfigParameterInfo
    {
        private String _id;
        private String _pluginName;
        private Configuration _conf;
        
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
    
    class ConfigParameter extends Parameter<ParameterType>
    {
        private I18nizableText _displayCategory;
        private I18nizableText _displayGroup;


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
            String defaultValue = parameterConfig.getChild("default-value").getValue(null);
            return ParameterHelper.castValue(defaultValue, parameter.getType());
        }
        
        @Override
        protected void _additionalParsing(ServiceManager manager, String pluginName, Configuration parameterConfig, String parameterId, ConfigParameter parameter) throws ConfigurationException
        {
            super._additionalParsing(manager, pluginName, parameterConfig, parameterId, parameter);
            
            parameter.setId(parameterId);
            parameter.setDisplayCategory(_parseI18nizableText(parameterConfig, pluginName, "category"));
            parameter.setDisplayGroup(_parseI18nizableText(parameterConfig, pluginName, "group"));
        }
    }
}
