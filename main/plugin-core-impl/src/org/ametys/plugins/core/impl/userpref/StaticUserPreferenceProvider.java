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
package org.ametys.plugins.core.impl.userpref;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.parameter.AbstractParameterParser;
import org.ametys.core.userpref.UserPreference;
import org.ametys.core.userpref.UserPreferenceProvider;
import org.ametys.runtime.parameter.Enumerator;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.parameter.Validator;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;
import org.ametys.runtime.plugin.component.PluginAware;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;

/**
 * Provides user preferences based on static configuration.
 */
public class StaticUserPreferenceProvider extends AbstractLogEnabled implements UserPreferenceProvider, Contextualizable, Serviceable, Configurable, PluginAware, Disposable
{
    
    /** The user preferences, indexed by ID. */
    protected Map<String, UserPreference> _preferences;
    
    /** ComponentManager for {@link Validator}s. */
    protected ThreadSafeComponentManager<Validator> _validatorManager;
    
    /** ComponentManager for {@link Enumerator}s. */
    protected ThreadSafeComponentManager<Enumerator> _enumeratorManager;
    
    /** Avalon service manager */
    protected ServiceManager _serviceManager;
    
    /** Avalon context */
    protected Context _context;
    
    /** The plugin name. */
    protected String _pluginName;
    
    @Override
    public void setPluginInfo(String pluginName, String featureName)
    {
        _pluginName = pluginName;
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _serviceManager = manager;
    }
    
    @Override
    public void dispose()
    {
        _validatorManager.dispose();
        _validatorManager = null;
        _enumeratorManager.dispose();
        _enumeratorManager = null;
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        try
        {
            _validatorManager = new ThreadSafeComponentManager<>();
            _validatorManager.enableLogging(getLogger());
            _validatorManager.contextualize(_context);
            _validatorManager.service(_serviceManager);
            
            _enumeratorManager = new ThreadSafeComponentManager<>();
            _enumeratorManager.enableLogging(getLogger());
            _enumeratorManager.contextualize(_context);
            _enumeratorManager.service(_serviceManager);
        }
        catch (ServiceException e)
        {
            throw new ConfigurationException("Unable to create local component managers", configuration, e);
        }
        
        UserPreferenceParser prefParser = new UserPreferenceParser(_enumeratorManager, _validatorManager);
        
        _preferences = new HashMap<>();
        
        Configuration[] prefConfigurations = configuration.getChildren("param");
        for (Configuration prefConfiguration : prefConfigurations)
        {
            configurePreference(prefParser, prefConfiguration);
        }
        
        try
        {
            prefParser.lookupComponents();
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Unable to lookup parameter local components", configuration, e);
        }
    }
    
    /**
     * Configure a user preference.
     * @param prefParser the preference parser.
     * @param configuration The preference configuration.
     * @throws ConfigurationException if configuration is incomplete or invalid.
     */
    protected void configurePreference(UserPreferenceParser prefParser, Configuration configuration) throws ConfigurationException
    {
        UserPreference preference = prefParser.parseParameter(_serviceManager, _pluginName, configuration);
        String id = preference.getId();
        
        if (_preferences.containsKey(id))
        {
            throw new ConfigurationException("The user preference '" + id + "' is already declared. Preference IDs must be unique.", configuration);
        }
        
        _preferences.put(id, preference);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("User preference added: " + id);
        }
    }
    
    @Override
    public Collection<UserPreference> getPreferences(Map<String, String> context)
    {
        return Collections.unmodifiableCollection(_preferences.values());
    }
    
    /**
     * Parser for UserPreference.
     */
    class UserPreferenceParser extends AbstractParameterParser<UserPreference, ParameterType>
    {
        public UserPreferenceParser(ThreadSafeComponentManager<Enumerator> enumeratorManager, ThreadSafeComponentManager<Validator> validatorManager)
        {
            super(enumeratorManager, validatorManager);
        }
        
        @Override
        protected UserPreference _createParameter(Configuration parameterConfig) throws ConfigurationException
        {
            return new UserPreference();
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
        protected Object _parseDefaultValue(Configuration parameterConfig, UserPreference preference)
        {
            String defaultValue = parameterConfig.getChild("default-value").getValue(null);
            return ParameterHelper.castValue(defaultValue, preference.getType());
        }
        
        @Override
        protected void _additionalParsing(ServiceManager manager, String pluginName, Configuration preferenceConfig, String parameterId, UserPreference preference) throws ConfigurationException
        {
            super._additionalParsing(manager, pluginName, preferenceConfig, parameterId, preference);
            
            boolean multiple = preferenceConfig.getAttributeAsBoolean("multiple", false);
            int order = preferenceConfig.getChild("order").getValueAsInteger(1000);
            String managerRole = preferenceConfig.getChild("manager-role").getValue(null);
            boolean privateStatus = preferenceConfig.getAttributeAsBoolean("private", false);
            
            preference.setId(parameterId);
            preference.setDisplayGroup(_parseI18nizableText(preferenceConfig, pluginName, "group"));
            preference.setMultiple(multiple);
            preference.setManagerRole(managerRole);
            preference.setOrder(order);
            preference.setPrivate(privateStatus);
        }
    }
    
}
