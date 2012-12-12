/*
 *  Copyright 2011 Anyware Services
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
public interface UserPreferencesStorage
{

    /**
     * Get a user's preference values (as String) for a given context.
     * @param login the user login.
     * @param storageContext the preferences storage context.
     * @param contextVars 
     * @return the user preference values as a Map of String indexed by preference ID.
     * @throws UserPreferencesException if an error occurs getting the preferences.
     */
    public Map<String, String> getUnTypedUserPrefs(String login, String storageContext, Map<String, String> contextVars) throws UserPreferencesException;
    
    /**
     * Remove the stored user preferences for a login in a given context.
     * @param login the user login.
     * @param storageContext the preferences storage context.
     * @param contextVars the context variables.
     * @throws UserPreferencesException
     */
    public void removeUserPreferences(String login, String storageContext, Map<String, String> contextVars) throws UserPreferencesException;
    
    /**
     * Set a user's preferences for a given context.
     * @param login the user login.
     * @param storageContext the preferences storage context.
     * @param contextVars 
     * @param preferences a Map of the preference values indexed by ID.
     * @throws UserPreferencesException 
     */
    public void setUserPreferences(String login, String storageContext, Map<String, String> contextVars, Map<String, String> preferences) throws UserPreferencesException;
    
    /**
     * Get a single string user preference value for a given context.
     * @param login the user login.
     * @param storageContext the preferences storage context.
     * @param contextVars 
     * @param id the preference ID.
     * @return the user preference value as a String.
     * @throws UserPreferencesException 
     */
    public String getUserPreferenceAsString(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException;
    
    /**
     * Get a single long user preference value for a given context.
     * @param login the user login.
     * @param storageContext the preferences storage context.
     * @param contextVars 
     * @param id the preference ID.
     * @return the user preference value as a Long.
     * @throws UserPreferencesException 
     */
    public Long getUserPreferenceAsLong(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException;
    
    /**
     * Get a single date user preference value for a given context.
     * @param login the user login.
     * @param storageContext the preferences storage context.
     * @param contextVars 
     * @param id the preference ID.
     * @return the user preference value as a Date.
     * @throws UserPreferencesException 
     */
    public Date getUserPreferenceAsDate(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException;
    
    /**
     * Get a single boolean user preference value for a given context.
     * @param login the user login.
     * @param storageContext the preferences storage context.
     * @param contextVars 
     * @param id the preference ID.
     * @return the user preference value as a Boolean.
     * @throws UserPreferencesException 
     */
    public Boolean getUserPreferenceAsBoolean(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException;
    
    /**
     * Get a single double user preference value for a given context.
     * @param login the user login.
     * @param storageContext the preferences storage context.
     * @param contextVars 
     * @param id the preference ID.
     * @return the user preference value as a Double.
     * @throws UserPreferencesException 
     */
    public Double getUserPreferenceAsDouble(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException;
}
