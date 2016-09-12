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
package org.ametys.core.group.directory;

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
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.Validator;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.plugin.ExtensionPoint;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;
import org.ametys.runtime.plugin.component.LogEnabled;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;

/**
 * This extension point handles a list of {@link GroupDirectoryModel} handled by the plugins. 
 */
public class GroupDirectoryFactory extends AbstractLogEnabled implements ExtensionPoint<GroupDirectoryModel>, Initializable, ThreadSafe, Component, Serviceable, Contextualizable, Disposable
{
    /** The avalon role */
    public static final String ROLE = GroupDirectoryFactory.class.getName();
    
    private Map<String, GroupDirectoryModel> _models;
    
    private ServiceManager _smanager;

    private Context _context;

    @Override
    public void initialize() throws Exception
    {
        _models = new HashMap<>();
    }
    
    @Override
    public void dispose()
    {
        _models.clear();
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
     * Creates a instance of {@link GroupDirectory}
     * @param id The id of the group directory
     * @param label The label of the group directory
     * @param modelId The id of the group directory model
     * @param paramsValues The parameters's values
     * @return a group directory
     */
    public GroupDirectory createGroupDirectory (String id, I18nizableText label, String modelId, Map<String, Object> paramsValues)
    {
        if (_models.containsKey(modelId))
        {
            GroupDirectoryModel groupDirectoryModel = _models.get(modelId);
            
            GroupDirectory groupDirectory = null;
            Class<GroupDirectory> groupDirectoryClass = groupDirectoryModel.getGroupDirectoryClass();
            try
            {
                groupDirectory = groupDirectoryClass.newInstance();
            }
            catch (InstantiationException | IllegalAccessException  e)
            {
                throw new IllegalArgumentException("Cannot instanciate the class " + groupDirectoryClass.getCanonicalName() + ". Check that there is a public constructor with no arguments.");
            }
            
            Logger logger = LoggerFactory.getLogger(groupDirectoryClass);
            try
            {
                if (groupDirectory instanceof LogEnabled)
                {
                    ((LogEnabled) groupDirectory).setLogger(logger);
                }
                
                LifecycleHelper.setupComponent(groupDirectory, new SLF4JLoggerAdapter(logger), _context, _smanager, groupDirectoryModel.getGroupDirectoryConfiguration());
            }
            catch (Exception e)
            {
                getLogger().warn("An exception occured during the setup of the component " + modelId, e);
            }
            
            groupDirectory.setId(id);
            groupDirectory.setLabel(label);
            try
            {
                groupDirectory.init(modelId, paramsValues);
            }
            catch (Exception e)
            {
                getLogger().error("An error occured during the initialization of the GroupDirectory " + id, e);
                return null;
            }
            
            return groupDirectory;
        }
        
        return null;
    }

    @Override
    public void addExtension(String id, String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Adding group directory model from plugin " + pluginName + "/" + featureName);
        }

        try
        {
            addGroupDirectoryModel(pluginName, configuration);
        }
        catch (ConfigurationException e)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The plugin '" + pluginName + "." + featureName + "' has a group directory model extension but has an incorrect configuration", e);
            }
        }
    }
    
    /**
     * Add a group directory model
     * @param pluginName The plugin name
     * @param configuration The configuration
     * @throws ConfigurationException  when a configuration problem occurs
     */
    protected void addGroupDirectoryModel (String pluginName, Configuration configuration) throws ConfigurationException
    {
        String id = configuration.getAttribute("id");
        I18nizableText label = new I18nizableText("plugin." + pluginName, configuration.getChild("label").getValue());
        I18nizableText description = new I18nizableText("plugin." + pluginName, configuration.getChild("description").getValue());
        
        String className = null;
        Class<?> groupDirectoryClass = null;
        Configuration classConfig = null;
        try
        {
            className = configuration.getChild("class").getAttribute("name");
            groupDirectoryClass = Class.forName(className);
            classConfig = configuration.getChild("class");
        }
        catch (ClassNotFoundException | ConfigurationException e)
        {
            throw new ConfigurationException("Group directory model with id '" + id + "' has an invalid configuration for class name " + (className != null ? className + " <class not found>" : "<missing tag <class>") + "'", e);
        }
        
        if (!GroupDirectory.class.isAssignableFrom(groupDirectoryClass))
        {
            throw new ConfigurationException("Group directory model with id '" + id + "' has an invalid configuration: '" + className + "' is not an instance of GroupDirectory");
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
        
        GroupDirectoryModelParameterParser groupDirectoryParser = new GroupDirectoryModelParameterParser(enumeratorManager, validatorManager);
        
        Configuration[] paramsConfiguration = configuration.getChild("parameters").getChildren("param");
        for (Configuration paramConfiguration : paramsConfiguration)
        {
            configureParameters(groupDirectoryParser, paramConfiguration, pluginName, parameters);
        }
        
        try
        {
            groupDirectoryParser.lookupComponents();
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Unable to lookup parameter local components", configuration, e);
        }
        
        @SuppressWarnings("unchecked")
        GroupDirectoryModel groupDirectoryModel = new DefaultGroupDirectoryModel(id, (Class<GroupDirectory>) groupDirectoryClass, classConfig, label, description, parameters, pluginName);
        if (_models.containsKey(id))
        {
            GroupDirectoryModel oldUDModel = _models.get(id);
            throw new IllegalArgumentException("Group directory model with id '" + id + "' is already declared in plugin '" + oldUDModel.getPluginName() + "'. This second declaration is ignored.");
        }
        
        _models.put(id, groupDirectoryModel);
    }
    
    /**
     * Configure a parameter to access the group directory
     * @param paramParser the parameter parser.
     * @param configuration The parameter configuration.
     * @param pluginName The plugin name
     * @param parameters The model's parameters
     * @throws ConfigurationException if configuration is incomplete or invalid.
     */
    protected void configureParameters(GroupDirectoryModelParameterParser paramParser, Configuration configuration, String pluginName, Map<String, Parameter<ParameterType>> parameters) throws ConfigurationException
    {
        Parameter<ParameterType> parameter = paramParser.parseParameter(_smanager, pluginName, configuration);
        String id = parameter.getId();
        
        if (parameters.containsKey(id))
        {
            throw new ConfigurationException("The parameter '" + id + "' is already declared. IDs must be unique.", configuration);
        }
        
        parameters.put(id, parameter);
    }

    @Override
    public void initializeExtensions() throws Exception
    {
        // Nothing to do
    }

    @Override
    public boolean hasExtension(String id)
    {
        return _models.containsKey(id);
    }

    @Override
    public GroupDirectoryModel getExtension(String id)
    {
        return _models.get(id);
    }

    @Override
    public Set<String> getExtensionsIds()
    {
        return _models.keySet();
    }
    
    /**
     * Class for parsing parameters of a {@link GroupDirectoryModel}
     */
    public class GroupDirectoryModelParameterParser extends AbstractParameterParser<Parameter<ParameterType>, ParameterType>
    {
        /**
         * Constructor
         * @param enumeratorManager The manager for enumeration
         * @param validatorManager The manager for validation
         */
        public GroupDirectoryModelParameterParser(ThreadSafeComponentManager<Enumerator> enumeratorManager, ThreadSafeComponentManager<Validator> validatorManager)
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
