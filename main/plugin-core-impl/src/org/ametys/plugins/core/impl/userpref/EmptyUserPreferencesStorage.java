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
package org.ametys.plugins.core.impl.userpref;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.thread.ThreadSafe;

import org.ametys.core.user.UserIdentity;
import org.ametys.core.userpref.DefaultUserPreferencesStorage;
import org.ametys.core.userpref.UserPreferencesException;

/**
 * An empty implementation of {@link DefaultUserPreferencesStorage}.
 */
public class EmptyUserPreferencesStorage implements DefaultUserPreferencesStorage, ThreadSafe
{
    @Override
    public Map<String, String> getUnTypedUserPrefs(UserIdentity user, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        return new HashMap<>();
    }
    
    @Override
    public void removeUserPreferences(UserIdentity user, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        // Do nothing.
    }
    
    @Override
    public void setUserPreferences(UserIdentity user, String storageContext, Map<String, String> contextVars, Map<String, String> preferences) throws UserPreferencesException
    {
        // Do nothing
    }

    @Override
    public String getUserPreferenceAsString(UserIdentity user, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        return null;
    }
    
    @Override
    public Long getUserPreferenceAsLong(UserIdentity user, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        return null;
    }
    
    @Override
    public Date getUserPreferenceAsDate(UserIdentity user, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        return null;
    }
    
    @Override
    public Boolean getUserPreferenceAsBoolean(UserIdentity user, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        return null;
    }
    
    @Override
    public Double getUserPreferenceAsDouble(UserIdentity user, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        return null;
    }
    
}
