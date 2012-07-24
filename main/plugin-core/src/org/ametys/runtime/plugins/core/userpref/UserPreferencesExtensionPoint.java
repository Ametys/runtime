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
package org.ametys.runtime.plugins.core.userpref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.commons.lang.StringUtils;

import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;
import org.ametys.runtime.plugin.component.ThreadSafeComponentManager;
import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.parameter.AbstractParameterParser;
import org.ametys.runtime.util.parameter.Enumerator;
import org.ametys.runtime.util.parameter.Errors;
import org.ametys.runtime.util.parameter.ParameterHelper;
import org.ametys.runtime.util.parameter.Validator;
import org.ametys.runtime.util.parameter.ParameterHelper.ParameterType;

/**
 * Extension point holding all {@link UserPreference} definitions.
 */
public class UserPreferencesExtensionPoint extends AbstractThreadSafeComponentExtensionPoint<UserPreference>
{
    /** Avalon Role */
    public static final String ROLE = UserPreferencesExtensionPoint.class.getName();
    
    /** The user preferences, indexed by ID. */
    private Map<String, UserPreference> _preferences;
    
    /** ComponentManager for {@link Validator}s. */
    private ThreadSafeComponentManager<Validator> _validatorManager;
    
    /** ComponentManager for {@link Enumerator}s. */
    private ThreadSafeComponentManager<Enumerator> _enumeratorManager;
    
    /** User preference parser. */
    private UserPreferenceParser _prefParser;
    
    /** User preference parser. */
    private UserPrefOrderComparator _comparator;
    
    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        
        _preferences = new LinkedHashMap<String, UserPreference>();
        
        _validatorManager = new ThreadSafeComponentManager<Validator>();
        _validatorManager.enableLogging(getLogger());
        _validatorManager.contextualize(_context);
        _validatorManager.service(_cocoonManager);
        
        _enumeratorManager = new ThreadSafeComponentManager<Enumerator>();
        _enumeratorManager.enableLogging(getLogger());
        _enumeratorManager.contextualize(_context);
        _enumeratorManager.service(_cocoonManager);
        
        _comparator = new UserPrefOrderComparator();
        _prefParser = new UserPreferenceParser(_enumeratorManager, _validatorManager);
    }
    
    /**
     * Dispose the manager before restarting it
     */
    @Override
    public void dispose()
    {
        _prefParser = null;
        
        _preferences = null;
        _validatorManager.dispose();
        _validatorManager = null;
        _enumeratorManager.dispose();
        _enumeratorManager = null;
        
        super.dispose();
    }
    
    @Override
    public boolean hasExtension(String id)
    {
        return _preferences.containsKey(id);
    }
    
    @Override
    public void addExtension(String pluginName, String featureName, Configuration configuration) throws ConfigurationException
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Adding user preferences from feature " + pluginName + "/" + featureName);
        }
        
        try
        {
            Configuration[] prefConfigurations = configuration.getChildren("param");
            for (Configuration prefConfiguration : prefConfigurations)
            {
                _addPreference(pluginName, prefConfiguration);
            }
        }
        catch (ConfigurationException e)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The feature '" + pluginName + "/" + featureName + "' has a user preferences extension which has an incorrect configuration.", e);
            }
        }
    }
    
    @Override
    public UserPreference getExtension(String id)
    {
        return _preferences.get(id);
    }
    
    @Override
    public Set<String> getExtensionsIds()
    {
        return Collections.unmodifiableSet(_preferences.keySet());
    }
    
    @Override
    public void initializeExtensions() throws Exception
    {
        super.initializeExtensions();
        _prefParser.lookupComponents();
    }
    
    /**
     * Get all the declared user preferences.
     * @return the user preferences (read-only collection).
     */
    public Map<String, UserPreference> getUserPreferences()
    {
        return Collections.unmodifiableMap(_preferences);
    }
    
    /**
     * Get all the preferences, classified by group and ordered.
     * @return the preferences classified by group and ordered.
     */
    public Map<I18nizableText, List<UserPreference>> getCategorizedPreferences()
    {
        return _categorize(_preferences.values());
    }
    
    /**
     * Validate preference values.
     * @param values the values.
     * @param errors the errors object to fill in.
     */
    public void validatePreferences(Map<String, String> values, UserPreferencesErrors errors)
    {
        for (Entry<String, String> entry : values.entrySet())
        {
            if (hasExtension(entry.getKey()))
            {
                UserPreference pref = _preferences.get(entry.getKey());
                String value = entry.getValue();
                
                Object castValue = ParameterHelper.castValue(value, pref.getType());
                if (StringUtils.isNotEmpty(value) && castValue == null)
                {
                    errors.addError(pref.getId(), new I18nizableText("plugin.core", "PLUGINS_CORE_USER_PREFERENCES_INVALID_TYPE"));
                }
                else
                {
                    Validator validator = pref.getValidator();
                    if (validator != null)
                    {
                        Errors fieldErrors = new Errors();
                        pref.getValidator().validate(castValue == null ? value : castValue, fieldErrors);
                        
                        if (fieldErrors.hasErrors())
                        {
                            errors.addErrors(pref.getId(), fieldErrors.getErrors());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Declare a user preference.
     * @param pluginName The name of the plugin declaring the extension.
     * @param configuration The preference configuration.
     * @throws ConfigurationException if configuration is incomplete or invalid.
     */
    protected void _addPreference(String pluginName, Configuration configuration) throws ConfigurationException
    {
        UserPreference preference = _prefParser.parseParameter(_cocoonManager, pluginName, configuration);
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
    
    /**
     * Organize a collection of user preferences by group.
     * @param preferences a collection of user preferences.
     * @return a Map of preferences sorted by group.
     */
    protected Map<I18nizableText, List<UserPreference>> _categorize(Collection<UserPreference> preferences)
    {
        Map<I18nizableText, List<UserPreference>> groups = new HashMap<I18nizableText, List<UserPreference>>();

        // Classify preferences by groups.
        for (UserPreference userPref : preferences)
        {
            I18nizableText groupName = userPref.getDisplayGroup();

            // Get the map of preferences of the group.
            List<UserPreference> group = groups.get(groupName);
            if (group == null)
            {
                group = new ArrayList<UserPreference>();
                groups.put(groupName, group);
            }
            
            group.add(userPref);
        }
        
        // Sort all groups.
        for (List<UserPreference> group : groups.values())
        {
            Collections.sort(group, _comparator);
        }
        
        return groups;
    }
    
    /**
     * Compares user preferences on their "order" attribute.
     */
    class UserPrefOrderComparator implements Comparator<UserPreference>
    {
        
        @Override
        public int compare(UserPreference pref1, UserPreference pref2)
        {
            return pref1.getOrder() - pref2.getOrder();
        }
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
            
            preference.setId(parameterId);
            preference.setDisplayGroup(_parseI18nizableText(preferenceConfig, pluginName, "group"));
            preference.setMultiple(multiple);
            preference.setOrder(order);
        }
    }

}
