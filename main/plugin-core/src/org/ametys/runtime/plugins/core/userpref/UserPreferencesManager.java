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

import java.util.Date;
import java.util.Map;

/**
 * Manager for handling user preferences.
 */
public interface UserPreferencesManager
{
    /** The avalon role. */
    public static final String ROLE = UserPreferencesManager.class.getName();
    
    /**
     * Get a user's preference values (as String) for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @return the user preference values as a Map of String indexed by preference ID.
     * @throws UserPreferencesException if an error occurs getting the preferences.
     */
    public Map<String, String> getUnTypedUserPrefs(String login, String context) throws UserPreferencesException;
    
    
    /**
     * Get a user's preference values cast as their own type for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @return the user preference values as a Map of Object indexed by preference ID.
     * @throws UserPreferencesException if an error occurs getting the preferences.
     */
    public Map<String, Object> getTypedUserPrefs(String login, String context) throws UserPreferencesException;
    
    
    /**
     * Add a user preference 
     * @param login the user login
     * @param context the preferences context.
     * @param name the user pref name
     * @param value the user pref value
     * @throws UserPreferencesException
     */
    public void addUserPreference (String login, String context, String name, String value) throws UserPreferencesException;
    
    
    /**
     * Add a user preference 
     * @param login the user login
     * @param context the preferences context.
     * @param values the user prefs to add
     * @throws UserPreferencesException
     */
    public void addUserPreferences (String login, String context, Map<String, String> values) throws UserPreferencesException;
    
    /**
     * Remove a user preference 
     * @param login the user login
     * @param context the preferences context.
     * @param name the user pref name
     * @throws UserPreferencesException
     */
    public void removeUserPreference (String login, String context, String name) throws UserPreferencesException;
    
    
    /**
     * Set a user's preferences for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @param preferences a Map of the preference values indexed by ID.
     * @throws UserPreferencesException 
     */
    public void setUserPreferences(String login, String context, Map<String, String> preferences) throws UserPreferencesException;
    
    /**
     * Get a single string user preference value for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @param id the preference ID.
     * @return the user preference value as a String.
     * @throws UserPreferencesException 
     */
    public String getUserPreferenceAsString(String login, String context, String id) throws UserPreferencesException;
    
    /**
     * Get a single long user preference value for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @param id the preference ID.
     * @return the user preference value as a Long.
     * @throws UserPreferencesException 
     */
    public Long getUserPreferenceAsLong(String login, String context, String id) throws UserPreferencesException;
    
    /**
     * Get a single date user preference value for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @param id the preference ID.
     * @return the user preference value as a Date.
     * @throws UserPreferencesException 
     */
    public Date getUserPreferenceAsDate(String login, String context, String id) throws UserPreferencesException;
    
    /**
     * Get a single boolean user preference value for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @param id the preference ID.
     * @return the user preference value as a Boolean.
     * @throws UserPreferencesException 
     */
    public Boolean getUserPreferenceAsBoolean(String login, String context, String id) throws UserPreferencesException;
    
    /**
     * Get a single double user preference value for a given context.
     * @param login the user login.
     * @param context the preferences context.
     * @param id the preference ID.
     * @return the user preference value as a Double.
     * @throws UserPreferencesException 
     */
    public Double getUserPreferenceAsDouble(String login, String context, String id) throws UserPreferencesException;
}
