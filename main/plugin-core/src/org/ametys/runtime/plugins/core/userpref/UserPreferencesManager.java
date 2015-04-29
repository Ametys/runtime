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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.lang.StringUtils;

import org.ametys.runtime.util.parameter.ParameterHelper;

/**
 * Component handling user preference values retrieval and storage.
 */
public class UserPreferencesManager extends AbstractLogEnabled implements ThreadSafe, Component, Serviceable, Configurable, Initializable
{
    
    /** The avalon role. */
    public static final String ROLE = UserPreferencesManager.class.getName();
    
    /** The user preferences extensions point. */
    protected UserPreferencesExtensionPoint _userPrefEP;
    
    /** A list of storage managers. */
    protected Map<String, UserPreferencesStorage> _storageManagers;
    
    /** The default storage component role. */
    protected String _defaultStorageRole;
    
    /** The avalon service manager. */
    protected ServiceManager _serviceManager;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _serviceManager = manager;
        _userPrefEP = (UserPreferencesExtensionPoint) manager.lookup(UserPreferencesExtensionPoint.ROLE);
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _defaultStorageRole = configuration.getChild("default-storage-role").getValue();
    }
    
    @Override
    public void initialize() throws Exception
    {
        _storageManagers = new HashMap<String, UserPreferencesStorage>();
    }
    
    /**
     * Get a user's preference values (as String) for a given context.
     * @param login the user login.
     * @param storageContext the preferences context.
     * @param contextVars the context variables.
     * @return the user preference values as a Map of String indexed by preference ID.
     * @throws UserPreferencesException if an error occurs getting the preferences.
     */
    public Map<String, String> getUnTypedUserPrefs(String login, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        Map<String, String> preferences = new HashMap<String, String>();
        
//        Map<String, Collection<UserPreference>> userPrefsByStorage = getUserPrefsByStorage(contextVars);
        Set<String> storageRoles = getStorageRoles(contextVars);
        
        for (String storageRole : storageRoles)
        {
            UserPreferencesStorage storageManager = getStorageManager(storageRole);
            
            preferences.putAll(storageManager.getUnTypedUserPrefs(login, storageContext, contextVars));
        }
        
        return preferences;
    }
    
    
    /**
     * Get a user's preference values cast as their own type for a given context.
     * @param login the user login.
     * @param storageContext the preferences context.
     * @param contextVars the context variables.
     * @return the user preference values as a Map of Object indexed by preference ID.
     * @throws UserPreferencesException if an error occurs getting the preferences.
     */
    public Map<String, Object> getTypedUserPrefs(String login, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        Map<String, String> unTypedUserPrefs = getUnTypedUserPrefs(login, storageContext, contextVars);
        
        return _castValues(unTypedUserPrefs, contextVars);
    }
    
    /**
     * Add a user preference 
     * @param login the user login
     * @param storageContext the preferences context.
     * @param contextVars the context variables.
     * @param name the user pref name
     * @param value the user pref value
     * @throws UserPreferencesException
     */
    public void addUserPreference(String login, String storageContext, Map<String, String> contextVars, String name, String value) throws UserPreferencesException
    {
        Map<String, String> userPrefs = getUnTypedUserPrefs(login, storageContext, contextVars);
        userPrefs.put(name, value);
        
        setUserPreferences(login, storageContext, contextVars, userPrefs);
    }
    
    /**
     * Add a user preference 
     * @param login the user login
     * @param storageContext the preferences context.
     * @param contextVars the context variables.
     * @param values the user prefs to add
     * @throws UserPreferencesException
     */
    public void addUserPreferences(String login, String storageContext, Map<String, String> contextVars, Map<String, String> values) throws UserPreferencesException
    {
        Map<String, String> userPrefs = getUnTypedUserPrefs(login, storageContext, contextVars);
        userPrefs.putAll(values);
        
        setUserPreferences(login, storageContext, contextVars, userPrefs);
    }
    
    /**
     * Remove a user preference 
     * @param login the user login
     * @param storageContext the preferences context.
     * @param contextVars the context variables.
     * @param name the user pref name
     * @throws UserPreferencesException
     */
    public void removeUserPreference(String login, String storageContext, Map<String, String> contextVars, String name) throws UserPreferencesException
    {
        Map<String, String> userPrefs = getUnTypedUserPrefs(login, storageContext, contextVars);
        if (userPrefs.containsKey(name))
        {
            userPrefs.remove(name);
        }
        setUserPreferences(login, storageContext, contextVars, userPrefs);
    }
    
    /**
     * Remove all user preferences. 
     * @param login the user login.
     * @param storageContext the preferences context.
     * @param contextVars the context variables.
     * @throws UserPreferencesException
     */
    public void removeAllUserPreferences(String login, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        Set<String> storageRoles = getStorageRoles(contextVars);
        
        for (String storageRole : storageRoles)
        {
            UserPreferencesStorage storageManager = getStorageManager(storageRole);
            
            storageManager.removeUserPreferences(login, storageContext, contextVars);
        }
    }
    
    /**
     * Set a user's preferences for a given context.
     * @param login the user login.
     * @param storageContext the preferences context.
     * @param contextVars the context variables.
     * @param preferenceValues a Map of the preference values indexed by ID.
     * @throws UserPreferencesException 
     */
    public void setUserPreferences(String login, String storageContext, Map<String, String> contextVars, Map<String, String> preferenceValues) throws UserPreferencesException
    {
        Map<String, Map<String, String>> preferenceValuesByStorage = getUserPrefsValuesByStorage(contextVars, preferenceValues);
        
        for (String managerRole : preferenceValuesByStorage.keySet())
        {
            Map<String, String> storagePrefValues = preferenceValuesByStorage.get(managerRole);
            
            // Retrieve the right storage manager.
            UserPreferencesStorage storageManager = getStorageManager(managerRole);
            
            // Call set on the storage manager.
            storageManager.setUserPreferences(login, storageContext, contextVars, storagePrefValues);
        }
    }
    
    /**
     * Get a single string user preference value for a given context.
     * @param login the user login.
     * @param storageContext the preferences context.
     * @param contextVars the context variables.
     * @param id the preference ID.
     * @return the user preference value as a String.
     * @throws UserPreferencesException 
     */
    public String getUserPreferenceAsString(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        UserPreferencesStorage storageManager = getStorageManager(contextVars, id);
        
        return storageManager.getUserPreferenceAsString(login, storageContext, contextVars, id);
    }
    
    /**
     * Get a single long user preference value for a given context.
     * @param login the user login.
     * @param storageContext the preferences context.
     * @param contextVars the context variables.
     * @param id the preference ID.
     * @return the user preference value as a Long.
     * @throws UserPreferencesException 
     */
    public Long getUserPreferenceAsLong(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        UserPreferencesStorage storageManager = getStorageManager(contextVars, id);
        
        return storageManager.getUserPreferenceAsLong(login, storageContext, contextVars, id);
    }
    
    /**
     * Get a single date user preference value for a given context.
     * @param login the user login.
     * @param storageContext the preferences context.
     * @param contextVars the context variables.
     * @param id the preference ID.
     * @return the user preference value as a Date.
     * @throws UserPreferencesException 
     */
    public Date getUserPreferenceAsDate(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        UserPreferencesStorage storageManager = getStorageManager(contextVars, id);
        
        return storageManager.getUserPreferenceAsDate(login, storageContext, contextVars, id);
    }
    
    /**
     * Get a single boolean user preference value for a given context.
     * @param login the user login.
     * @param storageContext the preferences context.
     * @param contextVars the context variables.
     * @param id the preference ID.
     * @return the user preference value as a Boolean.
     * @throws UserPreferencesException 
     */
    public Boolean getUserPreferenceAsBoolean(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        UserPreferencesStorage storageManager = getStorageManager(contextVars, id);
        
        return storageManager.getUserPreferenceAsBoolean(login, storageContext, contextVars, id);
    }
    
    /**
     * Get a single double user preference value for a given context.
     * @param login the user login.
     * @param storageContext the preferences context.
     * @param contextVars the context variables.
     * @param id the preference ID.
     * @return the user preference value as a Double.
     * @throws UserPreferencesException 
     */
    public Double getUserPreferenceAsDouble(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        UserPreferencesStorage storageManager = getStorageManager(contextVars, id);
        
        return storageManager.getUserPreferenceAsDouble(login, storageContext, contextVars, id);
    }
    
    /**
     * Get all user preference storages.
     * @param contextVars the context variables.
     * @return a Set of storage roles.
     */
    protected Set<String> getStorageRoles(Map<String, String> contextVars)
    {
        Set<String> storageRoles = new HashSet<String>();
        
        // Add the default storage role.
        storageRoles.add(_defaultStorageRole);
        
        for (UserPreference preference : _userPrefEP.getUserPreferences(contextVars).values())
        {
            String role = StringUtils.defaultIfEmpty(preference.getManagerRole(), _defaultStorageRole);
            
            storageRoles.add(role);
        }
        
        return storageRoles;
    }
    
    /**
     * Get all user preferences, grouped by storage point.
     * @param contextVars the context variables.
     * @return a Map of storage role -&gt; collection of user preferences.
     */
    protected Map<String, Collection<UserPreference>> getUserPrefsByStorage(Map<String, String> contextVars)
    {
        Map<String, Collection<UserPreference>> userPrefs = new HashMap<String, Collection<UserPreference>>();
        
        for (UserPreference preference : _userPrefEP.getUserPreferences(contextVars).values())
        {
            String role = StringUtils.defaultIfEmpty(preference.getManagerRole(), _defaultStorageRole);
            
            Collection<UserPreference> rolePrefs = userPrefs.get(role);
            if (rolePrefs == null)
            {
                rolePrefs = new ArrayList<UserPreference>();
                userPrefs.put(role, rolePrefs);
            }
            
            rolePrefs.add(preference);
        }
        
        return userPrefs;
    }
    
    /**
     * Get user preference values, divided up by storage role.
     * @param contextVars the context variables.
     * @param preferenceValues the unsorted preference values.
     * @return the preference values, divided up by storage role, as a Map of storage role -gt; preference values.
     */
    protected Map<String, Map<String, String>> getUserPrefsValuesByStorage(Map<String, String> contextVars, Map<String, String> preferenceValues)
    {
        Map<String, Map<String, String>> preferenceValuesByStorage = new HashMap<String, Map<String, String>>();
        
        // Initialize with empty maps.
        for (String storageRole : getStorageRoles(contextVars))
        {
            preferenceValuesByStorage.put(storageRole, new HashMap<String, String>());
        }
        
        Map<String, Collection<UserPreference>> userPrefsByStorage = getUserPrefsByStorage(contextVars);
        Map<String, String> unknownPreferenceValues = new HashMap<String, String>(preferenceValues);
        
        for (String storageRole : userPrefsByStorage.keySet())
        {
            Map<String, String> storagePrefValues = preferenceValuesByStorage.get(storageRole);
            
            // Iterate over declared user preferences for this storage.
            Collection<UserPreference> storageUserPrefs = userPrefsByStorage.get(storageRole);
            for (UserPreference pref : storageUserPrefs)
            {
                String prefId = pref.getId();
                if (preferenceValues.containsKey(prefId))
                {
                    // Add the value to the corresponding storage map.
                    storagePrefValues.put(prefId, preferenceValues.get(prefId));
                    // Remove the value from the unknown preferences.
                    unknownPreferenceValues.remove(prefId);
                }
            }
        }
        
        // At this point, the unknownPreferenceValues map contains only undeclared preferences:
        // add them to the default storage.
        preferenceValuesByStorage.get(_defaultStorageRole).putAll(unknownPreferenceValues);
        
        return preferenceValuesByStorage;
    }
    
    /**
     * Get the storage component for a given role.
     * @param role the storage component role.
     * @return the storage component.
     * @throws UserPreferencesException if an error occurs looking up the storage manager.
     */
    protected UserPreferencesStorage getStorageManager(String role) throws UserPreferencesException
    {
        UserPreferencesStorage storageManager = null;
        
        String componentRole = role;
        
        if (_storageManagers.containsKey(componentRole))
        {
            storageManager = _storageManagers.get(componentRole);
        }
        else
        {
            try
            {
                storageManager = (UserPreferencesStorage) _serviceManager.lookup(componentRole);
                _storageManagers.put(componentRole, storageManager);
            }
            catch (ServiceException e)
            {
                throw new UserPreferencesException("Error looking up the user preference storage component of role " + componentRole, e);
            }
        }
        
        return storageManager;
    }
    
    /**
     * Get the storage component for a given role.
     * @param contextVars 
     * @param id 
     * @return the storage component.
     * @throws UserPreferencesException if an error occurs looking up the storage manager.
     */
    protected UserPreferencesStorage getStorageManager(Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        String storageManagerRole = _defaultStorageRole;
        
        UserPreference preference = _userPrefEP.getUserPreference(contextVars, id);
        
        if (preference != null && StringUtils.isNotEmpty(preference.getManagerRole()))
        {
            storageManagerRole = preference.getManagerRole();
        }
        
        return getStorageManager(storageManagerRole);
    }
    
    /**
     * Cast the preference values as their real type.
     * @param untypedValues the untyped user preferences
     * @param contextVars the context variables.
     * @return typed user preferences
     */
    protected Map<String, Object> _castValues(Map<String, String> untypedValues, Map<String, String> contextVars)
    {
        Map<String, Object> typedValues = new HashMap<String, Object>(untypedValues.size());
        
        for (Entry<String, String> entry : untypedValues.entrySet())
        {
            UserPreference userPref = _userPrefEP.getUserPreference(contextVars, entry.getKey());
            if (userPref != null)
            {
                Object value = ParameterHelper.castValue(entry.getValue(), userPref.getType());
                typedValues.put(userPref.getId(), value);
            }
            else
            {
                typedValues.put(entry.getKey(), entry.getValue());
            }
        }
        
        return typedValues;
    }
    
}
