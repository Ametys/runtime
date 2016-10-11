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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.util.log.SLF4JLoggerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.AbstractParameterParser;
import org.ametys.runtime.parameter.Enumerator;
import org.ametys.runtime.parameter.Parameter;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerDescriptor;
import org.ametys.runtime.parameter.ParameterCheckerParser;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.parameter.Validator;
import org.ametys.runtime.plugin.ExtensionPoint;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;
import org.ametys.runtime.plugin.component.LogEnabled;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;

/**
 * This extension point handles a list of {@link UserDirectoryModel} handled by the plugins.
 */
public class UserDirectoryFactory extends AbstractLogEnabled implements ExtensionPoint<UserDirectoryModel>, Initializable, ThreadSafe, Component, Serviceable, Contextualizable, Disposable
{
    /** The avalon role */
    public static final String ROLE = UserDirectoryFactory.class.getName();
    
    private Map<String, UserDirectoryModel> _udModels;

    private ServiceManager _smanager;

    private Context _context;
    
    @Override
    public void initialize() throws Exception
    {
        _udModels = new HashMap<>();
    }
    
    @Override
    public void dispose()
    {
        _udModels.clear();
    }
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _smanager = smanager;
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    /**
     * Creates a instance of {@link UserDirectory}
     * @param modelId The id of the user directory model
     * @param id A non-null and non-empty unique identifier
     * @param paramsValues The parameters's values
     * @param populationId The id of the population the created user directory belongs to.
     * @param label The directory optional label
     * @return a user's directory
     */
    public UserDirectory createUserDirectory(String id, String modelId, Map<String, Object> paramsValues, String populationId, String label)
    {
        if (_udModels.containsKey(modelId))
        {
            UserDirectoryModel userDirectoryModel = _udModels.get(modelId);
            
            UserDirectory ud = null;
            Class<UserDirectory> udClass = userDirectoryModel.getUserDirectoryClass();
            try
            {
                ud = userDirectoryModel.getUserDirectoryClass().newInstance();
            }
            catch (InstantiationException | IllegalAccessException  e)
            {
                throw new IllegalArgumentException("Cannot instanciate the class " + udClass.getCanonicalName() + ". Check that there is a public constructor with no arguments.");
            }
            
            Logger logger = LoggerFactory.getLogger(udClass);
            try
            {
                if (ud instanceof LogEnabled)
                {
                    ((LogEnabled) ud).setLogger(logger);
                }
                
                LifecycleHelper.setupComponent(ud, new SLF4JLoggerAdapter(logger), _context, _smanager, userDirectoryModel.getUserDirectoryConfiguration());
            }
            catch (Exception e)
            {
                getLogger().warn("An exception occured during the setup of the component " + modelId, e);
            }
            
            ud.setPopulationId(populationId);
            try
            {
                ud.init(id, modelId, paramsValues, label);
            }
            catch (Exception e)
            {
                throw new IllegalStateException("An error occured during the initialization of the UserDirectory '" + modelId + "' of the UserPopulation '" + populationId + "'", e);
            }
            
            return ud;
        }
        
        return null;
    }

    @Override
    public void addExtension(String id, String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        getLogger().debug("Adding user directory model from plugin {}/{}", pluginName, featureName);

        try
        {
            addUserDirectoryModel(pluginName, configuration);
        }
        catch (ConfigurationException e)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The plugin '" + pluginName + "." + featureName + "' has a user directory model extension but has an incorrect configuration", e);
            }
        }
    }
    
    /**
     * Add a user directory model
     * @param pluginName The plugin name
     * @param configuration The configuration
     * @throws ConfigurationException when a configuration problem occurs
     */
    protected void addUserDirectoryModel (String pluginName, Configuration configuration) throws ConfigurationException
    {
        String id = configuration.getAttribute("id");
        I18nizableText label = new I18nizableText("plugin." + pluginName, configuration.getChild("label").getValue());
        I18nizableText description = new I18nizableText("plugin." + pluginName, configuration.getChild("description").getValue());
        
        String className = null;
        Class<?> udClass = null;
        Configuration classConfig = null;
        try
        {
            className = configuration.getChild("class").getAttribute("name");
            udClass = Class.forName(className);
            classConfig = configuration.getChild("class");
        }
        catch (ClassNotFoundException | ConfigurationException e)
        {
            throw new ConfigurationException("User directory model with id '" + id + "' has an invalid configuration for class name " + (className != null ? className + " <class not found>" : "<missing tag <class>") + "'", e);
        }
        
        if (!UserDirectory.class.isAssignableFrom(udClass))
        {
            throw new ConfigurationException("User directory model with id '" + id + "' has an invalid configuration: '" + className + "' is not an instance of UserDirectory");
        }
        
        // Parse parameter
        Map<String, Parameter<ParameterType>> parameters = new LinkedHashMap<>();
        
        ThreadSafeComponentManager<Validator> validatorManager = new ThreadSafeComponentManager<>();
        validatorManager.setLogger(getLogger());
        validatorManager.contextualize(_context);
        validatorManager.service(_smanager);
        
        ThreadSafeComponentManager<Enumerator> enumeratorManager = new ThreadSafeComponentManager<>();
        enumeratorManager.setLogger(getLogger());
        enumeratorManager.contextualize(_context);
        enumeratorManager.service(_smanager);
        
        UserDirectoryModelParameterParser udParser = new UserDirectoryModelParameterParser(enumeratorManager, validatorManager);
        
        Configuration[] paramsConfiguration = configuration.getChild("parameters").getChildren("param");
        for (Configuration paramConfiguration : paramsConfiguration)
        {
            configureParameters(udParser, paramConfiguration, pluginName, parameters);
        }
        
        // Parse parameter checkers
        Map<String, ParameterCheckerDescriptor> parameterCheckers = new LinkedHashMap<>();
        
        ThreadSafeComponentManager<ParameterChecker> parameterCheckerManager = new ThreadSafeComponentManager<>();
        parameterCheckerManager.setLogger(getLogger());
        parameterCheckerManager.contextualize(_context);
        parameterCheckerManager.service(_smanager);
        
        ParameterCheckerParser parameterCheckerParser = new ParameterCheckerParser(parameterCheckerManager);
        
        Configuration[] paramCheckersConfiguration = configuration.getChild("parameters").getChildren("param-checker");
        for (Configuration paramCheckerConfiguration : paramCheckersConfiguration)
        {
            configureParamChecker(parameterCheckerParser, paramCheckerConfiguration, pluginName, parameterCheckers);
        }
        
        try
        {
            udParser.lookupComponents();
            parameterCheckerParser.lookupComponents();
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Unable to lookup parameter local components", configuration, e);
        }
        
        // Create and reference the model
        @SuppressWarnings("unchecked")
        UserDirectoryModel udModel = new DefaultUserDirectoryModel(id, (Class<UserDirectory>) udClass, classConfig, label, description, parameters, parameterCheckers, pluginName);
        if (_udModels.containsKey(id))
        {
            UserDirectoryModel oldUDModel = _udModels.get(id);
            throw new IllegalArgumentException("User directory model with id '" + id + "' is already declared in plugin '" + oldUDModel.getPluginName() + "'. This second declaration is ignored.");
        }
        
        _udModels.put(id, udModel);
    }
    
    /**
     * Configure a parameter to access the user directory
     * @param paramParser the parameter parser.
     * @param configuration The parameter configuration.
     * @param pluginName The plugin name
     * @param parameters The model's parameters
     * @throws ConfigurationException if configuration is incomplete or invalid.
     */
    protected void configureParameters(UserDirectoryModelParameterParser paramParser, Configuration configuration, String pluginName, Map<String, Parameter<ParameterType>> parameters) throws ConfigurationException
    {
        Parameter<ParameterType> parameter = paramParser.parseParameter(_smanager, pluginName, configuration);
        String id = parameter.getId();
        
        if (parameters.containsKey(id))
        {
            throw new ConfigurationException("The parameter '" + id + "' is already declared. IDs must be unique.", configuration);
        }
        
        parameters.put(id, parameter);
    }
    
    /**
     * Configure a parameter checker of a user directory
     * @param parser the parameter checker parser.
     * @param configuration The parameter checker configuration.
     * @param pluginName The plugin name
     * @param parameterCheckers The model's parameter checkers
     * @throws ConfigurationException if configuration is incomplete or invalid.
     */
    protected void configureParamChecker(ParameterCheckerParser parser, Configuration configuration, String pluginName, Map<String, ParameterCheckerDescriptor> parameterCheckers) throws ConfigurationException
    {
        ParameterCheckerDescriptor parameterChecker = parser.parseParameterChecker(pluginName, configuration);
        String id = parameterChecker.getId();
        
        if (parameterCheckers.containsKey(id))
        {
            throw new ConfigurationException("The parameter checker '" + id + "' is already declared. IDs must be unique.", configuration);
        }
        
        parameterCheckers.put(id, parameterChecker);
    }
    
    @Override
    public void initializeExtensions() throws Exception
    {
        // Nothing to do
    }

    @Override
    public boolean hasExtension(String id)
    {
        return _udModels.containsKey(id);
    }

    @Override
    public UserDirectoryModel getExtension(String id)
    {
        return _udModels.get(id);
    }

    @Override
    public Set<String> getExtensionsIds()
    {
        return _udModels.keySet();
    }
    
    /**
     * Class for parsing parameters of a {@link UserDirectoryModel}
     */
    public class UserDirectoryModelParameterParser extends AbstractParameterParser<Parameter<ParameterType>, ParameterType>
    {
        /**
         * Constructor
         * @param enumeratorManager The manager for enumeration
         * @param validatorManager The manager for validation
         */
        public UserDirectoryModelParameterParser(ThreadSafeComponentManager<Enumerator> enumeratorManager, ThreadSafeComponentManager<Validator> validatorManager)
        {
            super(enumeratorManager, validatorManager);
        }
        
        @Override
        protected Parameter<ParameterType> _createParameter(Configuration parameterConfig) throws ConfigurationException
        {
            return new Parameter<>();
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
                throw new ConfigurationException("Invalid parameter type", parameterConfig, e);
            }
        }
        
        @Override
        protected Object _parseDefaultValue(Configuration parameterConfig, Parameter<ParameterType> parameter) throws ConfigurationException
        {
            String defaultValue = parameterConfig.getChild("default-value").getValue(null);
            return ParameterHelper.castValue(defaultValue, parameter.getType());
        }
        
        @Override
        protected void _additionalParsing(ServiceManager manager, String pluginName, Configuration parameterConfig, String parameterId, Parameter<ParameterType> parameter)
                throws ConfigurationException
        {
            super._additionalParsing(manager, pluginName, parameterConfig, parameterId, parameter);
            parameter.setId(parameterId);
        }
    }
}
