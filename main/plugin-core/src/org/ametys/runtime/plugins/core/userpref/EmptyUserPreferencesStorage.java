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
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * An empty implementation of {@link DefaultUserPreferencesStorage}.
 */
public class EmptyUserPreferencesStorage implements DefaultUserPreferencesStorage, ThreadSafe
{
    @Override
    public Map<String, String> getUnTypedUserPrefs(String login, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        return new HashMap<String, String>();
    }
    
    @Override
    public void removeUserPreferences(String login, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        // Do nothing.
    }
    
    @Override
    public void setUserPreferences(String login, String storageContext, Map<String, String> contextVars, Map<String, String> preferences) throws UserPreferencesException
    {
        // Do nothing
    }

    @Override
    public String getUserPreferenceAsString(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        return null;
    }
    
    @Override
    public Long getUserPreferenceAsLong(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        return null;
    }
    
    @Override
    public Date getUserPreferenceAsDate(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        return null;
    }
    
    @Override
    public Boolean getUserPreferenceAsBoolean(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        return null;
    }
    
    @Override
    public Double getUserPreferenceAsDouble(String login, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        return null;
    }
    
}
