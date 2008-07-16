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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;
import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.LoggerFactory;
import org.ametys.runtime.util.parameter.Enumerator;
import org.ametys.runtime.util.parameter.EnumeratorValue;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.StaticEnumerator;
import org.ametys.runtime.util.parameter.Validator;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

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
    
    // ComponentManager pour les TypedParameterValidator
    private ThreadSafeComponentManager<Validator> _validatorManager;
    
    //ComponentManager pour les TypedParameterValuesEnumerator
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
        _usedParamsName = new HashMap<String, String>();
        _declaredParams = new HashMap<String, ConfigParameterInfo>();
        _params = new HashMap<String, ConfigParameter>();        
        
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
                String errorMessage = "The mandatory attribute 'id' is missing on the config tag, in plugin '" + pluginName + "'";
                _logger.error(errorMessage);
                throw new ConfigurationException(errorMessage, configuration);
            }

            // Check the parameter is not already declared
            if (_declaredParams.get(id) != null)
            {
                String msg = "The parameter '" + id + "' is already declared. Parameters ids must be unique.";
                _logger.error(msg);
                throw new ConfigurationException(msg, configuration);
            }

            // Add the new parameter to the list of the unused parameters
            _declaredParams.put(id, new ConfigParameterInfo(id, pluginName, params[i]));

            if (_logger.isDebugEnabled())
            {
                _logger.debug("Parameter added : " + id);
            }
        }

        if (_logger.isDebugEnabled())
        {
            _logger.debug(params.length + " parameter(s) added.");
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

        Configuration[] params = configuration.getChild("config").getChildren("param");

        for (int i = 0; i < params.length; i++)
        {
            String id = params[i].getAttribute("id", null);
            if (id == null)
            {
                String errorMessage = "The mandatory attribute 'id' is missing on the config tag, in feature '" + pluginName + "/" + featureName + "'";
                _logger.error(errorMessage);
                throw new ConfigurationException(errorMessage, configuration);
            }

            if (params[i].getChildren().length > 0)
            {
                _declaredParams.put(id, new ConfigParameterInfo(id, pluginName, params[i]));
            }

            _usedParamsName.put(id, pluginName + PluginsManager.FEATURE_ID_SEPARATOR + featureName);
        }

        if (_logger.isDebugEnabled())
        {
            _logger.debug(params.length + " parameter(s) selected.");
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

        for (String id : _usedParamsName.keySet())
        {
            // Check if the parameter is not already used
            if (_params.get(id) == null)
            {
                // Move the parameter from the unused list, to the used list
                ConfigParameterInfo info = _declaredParams.get(id);
                if (info == null)
                {
                    String message = "The parameter '" + id + "' is used but not declared.";
                    _logger.error(message);
                    throw new IllegalArgumentException(message);
                }
                
                ConfigParameter parameter = _configureParameter(info);
                
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
            _validatorManager.initialize();
            _enumeratorManager.initialize();
        }
        catch (Exception e)
        {
            String errorMessage = "Exception while initializing components";
            _logger.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
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
    
    private ConfigParameter _configureParameter(ConfigParameterInfo info)
    {
        String label;
        String description;
        ParameterHelper.TYPE type;
        Object defaultValue;
        String widget;
        String displayCategory;
        String displayGroup;
        int order;
        Configuration enumeratorConf;
        Configuration validatorConf;
        
        Configuration conf = info.getConfiguration();
        String id = info.getId();
        
        try
        {
            label = _configureLabel(conf, info.getPluginName());
            description = _configureDescription(conf, info.getPluginName());
            type = _configureType(conf);
            defaultValue = _configureDefaultValue(conf, info.getId(), type);
            widget = _configureWidget(conf);
            displayCategory = _configureCategory(conf, info.getPluginName());
            displayGroup = _configureGroup(conf, info.getPluginName());
            order = _configureOrder(conf);

            // Add components if needed
            enumeratorConf = conf.getChild("Enumeration", false);
            if (enumeratorConf != null)
            {
                String enumeratorClassName = enumeratorConf.getAttribute("class", null);
                Class enumeratorClass = enumeratorClassName != null ? Class.forName(enumeratorClassName) : StaticEnumerator.class;
                _enumeratorManager.addComponent(info.getPluginName(), null, id, enumeratorClass, enumeratorConf);
            }
            
            validatorConf = conf.getChild("Validator", false);
            if (validatorConf != null)
            {
                String validatorClassName = validatorConf.getAttribute("class", null);
                
                if (validatorClassName != null)
                {
                    Class validatorClass = Class.forName(validatorClassName);
                    _validatorManager.addComponent(info.getPluginName(), null, id, validatorClass, validatorConf);
                }
            }
        }
        catch (Exception ex)
        {
            String errorMessage = "Unable to configure the config parameter : " + id;
            _logger.error(errorMessage);
            throw new RuntimeException(errorMessage, ex);
        }
        
        return new ConfigParameter(id, info.getPluginName(), label, description, type, defaultValue, widget, displayCategory, displayGroup, order, enumeratorConf != null, validatorConf != null);
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
     * SAX the config parameters and values into a content handler
     * @param handler Handler for sax events
     * @throws SAXException if an error occured
     */
    public void toSAX(ContentHandler handler) throws SAXException
    {
        Map<String, Map<String, List<ConfigParameter>>> categories = _categorizeParameters();
        _saxParameters(categories, handler);
    }

    private Map<String, Map<String, List<ConfigParameter>>> _categorizeParameters()
    {
        Map<String, Map<String, List<ConfigParameter>>> categories = new HashMap<String, Map<String, List<ConfigParameter>>>();

        // Classe les paramètres par catégorie et par groupe
        Iterator<String> it = _params.keySet().iterator();
        while (it.hasNext())
        {
            String key = it.next();
            ConfigParameter param = get(key);

            String categoryName = param.getDisplayCategory();
            String groupName = param.getDisplayGroup();

            // Get the map of groups of the category
            Map<String, List<ConfigParameter>> category = categories.get(categoryName);
            if (category == null)
            {
                category = new HashMap<String, List<ConfigParameter>>();
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

    private void _saxParameters(Map<String, Map<String, List<ConfigParameter>>> categories, ContentHandler handler) throws SAXException
    {
        // Récupère les paramètres
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

        // Sax les paramètres classés
        XMLUtils.startElement(handler, "categories");

        Iterator<String> catIt = categories.keySet().iterator();
        while (catIt.hasNext())
        {
            String categoryKey = catIt.next();
            Map<String, List<ConfigParameter>> category = categories.get(categoryKey);

            AttributesImpl catAttrs = new AttributesImpl();
            catAttrs.addCDATAAttribute("label", categoryKey.substring(categoryKey.indexOf(PluginsManager.FEATURE_ID_SEPARATOR) + PluginsManager.FEATURE_ID_SEPARATOR.length()));
            catAttrs.addCDATAAttribute("catalogue", categoryKey.substring(0, categoryKey.indexOf(PluginsManager.FEATURE_ID_SEPARATOR)));
            XMLUtils.startElement(handler, "category", catAttrs);

            XMLUtils.startElement(handler, "groups");

            Iterator<String> groupIt = category.keySet().iterator();
            while (groupIt.hasNext())
            {
                String groupKey = groupIt.next();
                List<ConfigParameter> group = category.get(groupKey);

                AttributesImpl gpAttrs = new AttributesImpl();
                gpAttrs.addCDATAAttribute("label", groupKey.substring(groupKey.indexOf(PluginsManager.FEATURE_ID_SEPARATOR) + PluginsManager.FEATURE_ID_SEPARATOR.length()));
                gpAttrs.addCDATAAttribute("catalogue", groupKey.substring(0, groupKey.indexOf(PluginsManager.FEATURE_ID_SEPARATOR)));
                XMLUtils.startElement(handler, "group", gpAttrs);

                Iterator<ConfigParameter> gIt = group.iterator();
                while (gIt.hasNext())
                {
                    _saxParameter(handler, gIt.next(), untypedValues);
                }

                XMLUtils.endElement(handler, "group");
            }

            XMLUtils.endElement(handler, "groups");

            XMLUtils.endElement(handler, "category");
        }

        XMLUtils.endElement(handler, "categories");
    }

    private void _saxParameter(ContentHandler handler, ConfigParameter param, Map<String, String> untypedValues) throws SAXException
    {
        AttributesImpl parameterAttr = new AttributesImpl();
        parameterAttr.addAttribute("", "plugin", "plugin", "CDATA", param.getPluginName());
        XMLUtils.startElement(handler, param.getId(), parameterAttr);
        
        AttributesImpl labelAttrs = new AttributesImpl();
        labelAttrs.addAttribute("", "catalogue", "catalogue", "CDATA", param.getLabel().substring(0, param.getLabel().indexOf(PluginsManager.FEATURE_ID_SEPARATOR)));
        XMLUtils.createElement(handler, "label", labelAttrs, param.getLabel().substring(param.getLabel().indexOf(PluginsManager.FEATURE_ID_SEPARATOR) + PluginsManager.FEATURE_ID_SEPARATOR.length()));
        
        AttributesImpl descAttrs = new AttributesImpl();
        descAttrs.addAttribute("", "catalogue", "catalogue", "CDATA", param.getDescription().substring(0, param.getDescription().indexOf(PluginsManager.FEATURE_ID_SEPARATOR)));
        XMLUtils.createElement(handler, "description", descAttrs, param.getDescription().substring(param.getDescription().indexOf(PluginsManager.FEATURE_ID_SEPARATOR) + PluginsManager.FEATURE_ID_SEPARATOR.length()));
        
        XMLUtils.createElement(handler, "type", param.getTypeAsString());
        
        if (param.getWidget() != null)
        {
            XMLUtils.createElement(handler, "widget", param.getWidget());
        }
        
        XMLUtils.createElement(handler, "order", Integer.toString(param.getOrder()));

        // Le plus complexe: envoyer la valeur
        _saxValue(handler, param, untypedValues);

        // Les types énumérés
        if (param.hasEnumerator())
        {
            Enumerator enumerator;
            try
            {
                enumerator = _enumeratorManager.lookup(param.getId());
            }
            catch (ComponentException e)
            {
                String errorMessage = "Unable to get ValueEnumerator for config parameter " + param.getId();
                _logger.error(errorMessage, e);
                throw new SAXException(errorMessage, e);
            }
            
            XMLUtils.startElement(handler, "enumeration");

            Collection<EnumeratorValue> values = enumerator.getValues();
            for (EnumeratorValue enumeratorValue : values)
            {
                Object value = enumeratorValue.getValue();
                String valueAsString = ParameterHelper.valueToString(value);
                I18nizableText label = enumeratorValue.getLabel();

                // Produit l'option
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute("", "value", "value", "CDATA", valueAsString);
                
                XMLUtils.startElement(handler, "option", attrs);
                label.toSAX(handler);
                XMLUtils.endElement(handler, "option");
            }

            XMLUtils.endElement(handler, "enumeration");
        }
        
        /* TODO en 1.1 gérer le validator = envoyer le message d'erreur */

        XMLUtils.endElement(handler, param.getId());
    }

    private void _saxValue(ContentHandler handler, ConfigParameter param, Map<String, String> untypedValues) throws SAXException
    {
        XMLUtils.startElement(handler, "value");

        // Get the typed value selon si elle existe dans le fichier ou pas
        Object typedValue;
        if (untypedValues.keySet().contains(param.getId()))
        {
            final String unverifiedUntypedValue = untypedValues.get(param.getId());
            typedValue = ParameterHelper.castValue(unverifiedUntypedValue, param.getType());
        }
        else
        {
            typedValue = param.getDefaultValue();
        }
        
        // Untype value
        String untypedValue = ParameterHelper.valueToString(typedValue);
        if (untypedValue != null)
        {
            if (untypedValue.length() > 0 && param.getType() == ParameterHelper.TYPE.PASSWORD)
            {
                untypedValue = "PASSWORD";
            }
            
            handler.characters(untypedValue.toCharArray(), 0, untypedValue.length());
        }
        
        XMLUtils.endElement(handler, "value");
    }

    /**
     * Update the config file with the given values<br>
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
            if (typedValue == null && _params.get(id).getType() == ParameterHelper.TYPE.PASSWORD)
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
            format.put("{http://xml.apache.org/xalan}indent-amount", "2");
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
        
        Map<String, Map<String, List<ConfigParameter>>> categories = _categorizeParameters();
        Iterator<String> catIt = categories.keySet().iterator();
        while (catIt.hasNext())
        {
            String categoryKey = catIt.next();
            Map<String, List<ConfigParameter>> category = categories.get(categoryKey);
            StringBuilder categoryLabel = new StringBuilder();
            categoryLabel.append("+\n      | ");
            categoryLabel.append(categoryKey.substring(categoryKey.indexOf(PluginsManager.FEATURE_ID_SEPARATOR) + PluginsManager.FEATURE_ID_SEPARATOR.length()));
            categoryLabel.append("\n      +");
            
            // Commentaire de la categorie courante
            XMLUtils.data(handler, "\n  ");
            handler.comment(categoryLabel.toString().toCharArray(), 0, categoryLabel.length());
            XMLUtils.data(handler, "\n");
            XMLUtils.data(handler, "\n");

            Iterator<String> groupIt = category.keySet().iterator();
            while (groupIt.hasNext())
            {
                String groupKey = groupIt.next();
                StringBuilder groupLabel = new StringBuilder();
                groupLabel.append(" ");
                groupLabel.append(groupKey.substring(groupKey.indexOf(PluginsManager.FEATURE_ID_SEPARATOR) + PluginsManager.FEATURE_ID_SEPARATOR.length()));
                groupLabel.append(" ");

                // Commentaire du group courant
                XMLUtils.data(handler, "  ");
                handler.comment(groupLabel.toString().toCharArray(), 0, groupLabel.length());
                XMLUtils.data(handler, "\n  ");

                List<ConfigParameter> group = category.get(groupKey);
                // Trier les paramètres
                Collections.sort(group, new Comparator<ConfigParameter>()
                {
                    public int compare(ConfigParameter param1, ConfigParameter param2)
                    {
                        int order1 = param1 != null ? param1.getOrder() : 0;
                        int order2 = param2 != null ? param2.getOrder() : 0;
                        
                        if (order1 < order2)
                        {
                            return -1;
                        }
                        
                        if (order1 == order2)
                        {
                            return 0;
                        }
                        
                        return 1;
                    }
                });
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
    
    private String _configureLabel(Configuration configuration, String pluginName) throws ConfigurationException
    {
        String labelKey = configuration.getChild("LabelKey").getValue("");
        
        if (labelKey.length() == 0)
        {
            throw new ConfigurationException("The mandatory element 'LabelKey' is missing or empty", configuration);
        }
        
        String catalogue = configuration.getChild("LabelKey").getAttribute("Catalogue", "plugin." + pluginName);
        
        return catalogue + PluginsManager.FEATURE_ID_SEPARATOR + labelKey;
    }

    private String _configureDescription(Configuration configuration, String pluginName) throws ConfigurationException
    {
        String descriptionKey = configuration.getChild("DescriptionKey").getValue("");
        
        if (descriptionKey.length() == 0)
        {
            throw new ConfigurationException("The mandatory element 'DescriptionKey' is missing or empty", configuration);
        }
        
        String catalogue = configuration.getChild("DescriptionKey").getAttribute("Catalogue", "plugin." + pluginName);
        
        return catalogue + PluginsManager.FEATURE_ID_SEPARATOR + descriptionKey;
    }
    
    private ParameterHelper.TYPE _configureType(Configuration configuration) throws ConfigurationException
    {
        String typeAsString = configuration.getChild("Type").getValue("");
        
        if (typeAsString.length() == 0)
        {
            throw new ConfigurationException("The mandatory element 'Type' is missing or empty", configuration);
        }
        
        ParameterHelper.TYPE type;
        
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
    
    private Object _configureDefaultValue(Configuration configuration, String id, ParameterHelper.TYPE type) throws ConfigurationException
    {
        String defaultValueAsString = configuration.getChild("DefaultValue").getValue(null);
        
        try
        {
            Object defaultValue = ParameterHelper.castValue(defaultValueAsString, type);
            return defaultValue;
        }
        catch (Exception e)
        {
            throw new ConfigurationException("The default value '" + (defaultValueAsString != null ? defaultValueAsString : "[null]") + "' for parameter with id '" + id + "' cannot be cast in its type '" + ParameterHelper.typeToString(type) + "'", configuration);
        }
    }
    
    private String _configureWidget(Configuration configuration)
    {
        String widget = configuration.getChild("Widget").getValue(null);
        return widget;
    }
    
    private String _configureCategory(Configuration configuration, String pluginName) throws ConfigurationException
    {
        String categoryKey = configuration.getChild("Category").getValue("");
        
        if (categoryKey.length() == 0)
        {
            throw new ConfigurationException("The mandatory element 'Category' is missing or empty", configuration);
        }
        
        String catalogue = configuration.getChild("Category").getAttribute("Catalogue", "plugin." + pluginName);
        
        return catalogue + PluginsManager.FEATURE_ID_SEPARATOR + categoryKey;
    }
    
    private String _configureGroup(Configuration configuration, String pluginName) throws ConfigurationException
    {
        String groupKey = configuration.getChild("Group").getValue("");
        
        if (groupKey.length() == 0)
        {
            throw new ConfigurationException("The mandatory element 'Group' is missing or empty", configuration);
        }
        
        String catalogue = configuration.getChild("Group").getAttribute("Catalogue", "plugin." + pluginName);
        
        return catalogue + PluginsManager.FEATURE_ID_SEPARATOR + groupKey;
    }
    
    private int _configureOrder(Configuration configuration)
    {
        int order = configuration.getChild("Order").getValueAsInteger(0);
        return order;
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
    
    class ConfigParameter
    {
        // Nom du plugin déclarant
        private String _pluginName;

        private String _id;
        private String _label;
        private String _description;
        private ParameterHelper.TYPE _type;
        private Object _defaultValue;
        private String _widget;
        private String _displayCategory;
        private String _displayGroup;
        private int _order;
        private boolean _hasEnumerator;
        private boolean _hasValidator;

        ConfigParameter(String id, String pluginName, String label, String description, ParameterHelper.TYPE type, Object defaultValue, String widget, String displayCategory, String displayGroup, int order, boolean hasEnumerator, boolean hasValidator)
        {
            _id = id;
            _pluginName = pluginName;
            _label = label;
            _description = description;
            _type = type;
            _defaultValue = defaultValue;
            _widget = widget;
            _displayCategory = displayCategory;
            _displayGroup = displayGroup;
            _order = order;
            _hasEnumerator = hasEnumerator;
            _hasValidator = hasValidator;
        }
        
        Object getDefaultValue()
        {
            return _defaultValue;
        }

        String getLabel()
        {
            return _label;
        }

        String getDescription()
        {
            return _description;
        }

        String getWidget()
        {
            return _widget;
        }
        
        String getId()
        {
            return _id;
        }

        ParameterHelper.TYPE getType()
        {
            return _type;
        }

        String getTypeAsString()
        {
            try
            {
                return ParameterHelper.typeToString(_type);
            }
            catch (IllegalArgumentException e)
            {
                _logger.error("A config parameter as an unknown type : " + _type, e);
                return "unknown";
            }
        }

        String getDisplayCategory()
        {
            return _displayCategory;
        }

        String getDisplayGroup()
        {
            return _displayGroup;
        }
        
        int getOrder()
        {
            return _order;
        }

        String getPluginName()
        {
            return _pluginName;
        }
        
        boolean hasEnumerator()
        {
            return _hasEnumerator;
        }
        
        boolean hasValidator()
        {
            return _hasValidator;
        }
    }
}
