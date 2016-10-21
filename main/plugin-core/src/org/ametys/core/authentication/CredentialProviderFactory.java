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
 * This extension point handles a list of {@link CredentialProvider} handled by the plugins.
 */
public class CredentialProviderFactory extends AbstractLogEnabled implements ExtensionPoint<CredentialProviderModel>, Initializable, ThreadSafe, Component, Serviceable, Contextualizable, Disposable
{
    /** The avalon role */
    public static final String ROLE = CredentialProviderFactory.class.getName();
    
    private Map<String, CredentialProviderModel> _cpModels;

    private ServiceManager _smanager;

    private Context _context;
    
    @Override
    public void initialize() throws Exception
    {
        _cpModels = new HashMap<>();
    }
    
    @Override
    public void dispose()
    {
        _cpModels.clear();
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
     * Creates a instance of {@link CredentialProvider}
     * @param id The id of the credential provider model
     * @param paramsValues the parameters's values
     * @param label The optionnal label
     * @return a credential provider
     */
    public CredentialProvider createCredentialProvider (String id, Map<String, Object> paramsValues, String label)
    {
        if (_cpModels.containsKey(id))
        {
            CredentialProviderModel credentialProviderModel = _cpModels.get(id);
            
            CredentialProvider cp = null;
            Class<CredentialProvider> cpClass = credentialProviderModel.getCredentialProviderClass();
            
            try
            {
                cp = credentialProviderModel.getCredentialProviderClass().newInstance();
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                throw new IllegalArgumentException("Cannot instanciate the class " + cpClass.getCanonicalName() + ". Check that there is a public constructor with no arguments.");
            }
            
            Logger logger = LoggerFactory.getLogger(cpClass);
            try
            {
                if (cp instanceof LogEnabled)
                {
                    ((LogEnabled) cp).setLogger(logger);
                }
                
                LifecycleHelper.setupComponent(cp, new SLF4JLoggerAdapter(logger), _context, _smanager, credentialProviderModel.getCredentialProviderConfiguration());
            }
            catch (Exception e)
            {
                getLogger().warn("An exception occured during the setup of the component " + id, e);
            }
            try
            {
                cp.init(id, paramsValues, label);
            }
            catch (Exception e)
            {
                getLogger().error("An error occured during the initialization of the CredentialProvider " + id, e);
                return null;
            }
            
            return cp;
        }
        
        return null;
    }
    
    @Override
    public void addExtension(String id, String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Adding credential provider model from plugin " + pluginName + "/" + featureName);
        }

        try
        {
            addCredentialProviderModel(pluginName, configuration);
        }
        catch (ConfigurationException e)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The plugin '" + pluginName + "." + featureName + "' has a credential provider model extension but has an incorrect configuration", e);
            }
        }
    }
    
    /**
     * Add a credential provider model
     * @param pluginName The plugin name
     * @param configuration The configuration
     * @throws ConfigurationException when a configuration problem occurs
     */
    protected void addCredentialProviderModel (String pluginName, Configuration configuration) throws ConfigurationException
    {
        String id = configuration.getAttribute("id");
        I18nizableText label = new I18nizableText("plugin." + pluginName, configuration.getChild("label").getValue());
        I18nizableText description = new I18nizableText("plugin." + pluginName, configuration.getChild("description").getValue());
        I18nizableText connectionLabel;
        if (configuration.getChild("label-connection", false) == null)
        {
            connectionLabel = null;
        }
        else
        {
            connectionLabel = new I18nizableText("plugin." + pluginName, configuration.getChild("label-connection").getValue());
        }
        String iconGlyph = configuration.getChild("icon-glyph").getValue("");
        String iconDecorator = configuration.getChild("icon-decorator").getValue("");
        String iconSmallPath = configuration.getChild("icon-small").getValue("");
        String iconMediumPath = configuration.getChild("icon-medium").getValue("");
        String iconLargePath = configuration.getChild("icon-large").getValue("");
        String connectionColor = configuration.getChild("color").getValue("");
        
        String className = null;
        Class<?> cpClass = null;
        Configuration classConfig = null;
        try
        {
            className = configuration.getChild("class").getAttribute("name");
            cpClass = Class.forName(className);
            classConfig = configuration.getChild("class");
        }
        catch (ClassNotFoundException | ConfigurationException e)
        {
            throw new ConfigurationException("Credential provider model with id '" + id + "' has an invalid configuration for class name '" + (className != null ? className + " <class not found>" : "<missing tag <class>") + "'", e);
        }
        
        if (!CredentialProvider.class.isAssignableFrom(cpClass))
        {
            throw new ConfigurationException("Credential provider model with id '" + id + "' has an invalid configuration: '" + className + "' is not an instance of CredentialProvider");
        }
        
        Map<String, Parameter<ParameterType>> parameters = new LinkedHashMap<>();
        
        ThreadSafeComponentManager<Validator> validatorManager = new ThreadSafeComponentManager<>();
        validatorManager.setLogger(getLogger());
        validatorManager.contextualize(_context);
        validatorManager.service(_smanager);
        
        ThreadSafeComponentManager<Enumerator> enumeratorManager = new ThreadSafeComponentManager<>();
        enumeratorManager.setLogger(getLogger());
        enumeratorManager.contextualize(_context);
        enumeratorManager.service(_smanager);
        
        CredentialProviderModelParameterParser cpParser = new CredentialProviderModelParameterParser(enumeratorManager, validatorManager);
        
        Configuration[] paramsConfiguration = configuration.getChild("parameters").getChildren("param");
        for (Configuration paramConfiguration : paramsConfiguration)
        {
            configureParameters(cpParser, paramConfiguration, pluginName, parameters);
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
            cpParser.lookupComponents();
            parameterCheckerParser.lookupComponents();
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Unable to lookup parameter local components", configuration, e);
        }
        
        @SuppressWarnings("unchecked")
        CredentialProviderModel cpModel = new DefaultCredentialProviderModel(id, (Class<CredentialProvider>) cpClass, classConfig, label, description, connectionLabel, iconGlyph, iconDecorator, iconSmallPath, iconMediumPath, iconLargePath, connectionColor, parameters, parameterCheckers, pluginName);
        if (_cpModels.containsKey(id))
        {
            CredentialProviderModel oldCPModel = _cpModels.get(id);
            throw new IllegalArgumentException("Credential provider model with id '" + id + "' is already declared in plugin '" + oldCPModel.getPluginName() + "'. This second declaration is ignored.");
        }
        
        _cpModels.put(id, cpModel);
    }
    
    /**
     * Configure a parameter to access the credential provider
     * @param paramParser the parameter parser.
     * @param configuration The parameter configuration.
     * @param pluginName The plugin name
     * @param parameters The model's parameters
     * @throws ConfigurationException if configuration is incomplete or invalid.
     */
    protected void configureParameters(CredentialProviderModelParameterParser paramParser, Configuration configuration, String pluginName, Map<String, Parameter<ParameterType>> parameters) throws ConfigurationException
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
        return _cpModels.containsKey(id);
    }

    @Override
    public CredentialProviderModel getExtension(String id)
    {
        return _cpModels.get(id);
    }

    @Override
    public Set<String> getExtensionsIds()
    {
        return _cpModels.keySet();
    }
    
    /**
     * Class for parsing parameters of a {@link CredentialProviderModel}
     */
    public class CredentialProviderModelParameterParser extends AbstractParameterParser<Parameter<ParameterType>, ParameterType>
    {
        /**
         * Constructor
         * @param enumeratorManager The manager for enumeration
         * @param validatorManager The manager for validation
         */
        public CredentialProviderModelParameterParser(ThreadSafeComponentManager<Enumerator> enumeratorManager, ThreadSafeComponentManager<Validator> validatorManager)
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
