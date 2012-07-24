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
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * An empty implementation of {@link UserPreferencesManager}.
 */
public class EmptyUserPreferencesManager implements UserPreferencesManager, ThreadSafe
{
    @Override
    public Map<String, String> getUnTypedUserPrefs(String login, String context) throws UserPreferencesException
    {
        return new HashMap<String, String>();
    }
    
    @Override
    public Map<String, Object> getTypedUserPrefs(String login, String context) throws UserPreferencesException
    {
        return new HashMap<String, Object>();
    }
    
    @Override
    public void addUserPreference (String login, String context, String name, String value) throws UserPreferencesException
    {
        // Do nothing
    }
    
    @Override
    public void addUserPreferences (String login, String context, Map<String, String> values) throws UserPreferencesException
    {
        // Do nothing
    }
    
    @Override
    public void removeUserPreference (String login, String context, String name) throws UserPreferencesException
    {
        // Do nothing
    }
    
    @Override
    public void setUserPreferences(String login, String context, Map<String, String> preferences) throws UserPreferencesException
    {
        // Do nothing
    }

    @Override
    public String getUserPreferenceAsString(String login, String context, String id) throws UserPreferencesException
    {
        return null;
    }
    
    @Override
    public Long getUserPreferenceAsLong(String login, String context, String id) throws UserPreferencesException
    {
        return null;
    }
    
    @Override
    public Date getUserPreferenceAsDate(String login, String context, String id) throws UserPreferencesException
    {
        return null;
    }
    
    @Override
    public Boolean getUserPreferenceAsBoolean(String login, String context, String id) throws UserPreferencesException
    {
        return null;
    }
    
    @Override
    public Double getUserPreferenceAsDouble(String login, String context, String id) throws UserPreferencesException
    {
        return null;
    }
    
}
