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
package org.ametys.plugins.core.impl.userpref;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.ametys.core.user.InvalidModificationException;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;
import org.ametys.core.user.directory.ModifiableUserDirectory;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.userpref.UserPreferencesException;
import org.ametys.core.userpref.UserPreferencesStorage;

/**
 * This class is a implementation of {@link UserPreferencesStorage} based on {@link UserManager}.
 * The supported user preferences are only the firstname, lastname and email of the user.
 * If the user is issued from a non-modifiable user directory, the user preferences could not be edited.
 */
public class UserManagerUserPreferenceStorage extends AbstractLogEnabled implements UserPreferencesStorage, ThreadSafe, Serviceable
{
    private UserManager _userManager;

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _userManager = (UserManager) smanager.lookup(UserManager.ROLE);
    }

    @Override
    public Map<String, String> getUnTypedUserPrefs(UserIdentity userIdentity, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        Map<String, String> userPrefs = new HashMap<>();
        User user = _userManager.getUser(userIdentity);
        
        userPrefs.put("firstname", user.getFirstName());
        userPrefs.put("lastname", user.getLastName());
        userPrefs.put("email", user.getEmail());
        
        return userPrefs;
    }

    @Override
    public void removeUserPreferences(UserIdentity user, String storageContext, Map<String, String> contextVars) throws UserPreferencesException
    {
        throw new UserPreferencesException("The user preferences issued from the users manager can not be removed ");
    }

    @Override
    public void setUserPreferences(UserIdentity userIdentity, String storageContext, Map<String, String> contextVars, Map<String, String> preferences) throws UserPreferencesException
    {
        User user = _userManager.getUser(userIdentity);
        
        if (!_hasChanges(user, preferences))
        {
            return;
        }
        
        UserDirectory userDirectory = user.getUserDirectory();
        if (userDirectory instanceof ModifiableUserDirectory)
        {
            try
            {
                preferences.put("login", userIdentity.getLogin());
                ((ModifiableUserDirectory) userDirectory).update(preferences);
            }
            catch (InvalidModificationException e)
            {
                throw new UserPreferencesException("Failed to update user informations", e);
            }
        }
        else
        {
            throw new UserPreferencesException("Try to update user informations on a non-modifiable user directory");
        }
    }
    
    private boolean _hasChanges (User user, Map<String, String> preferences)
    {
        return (preferences.containsKey("firstname") && !preferences.get("firstname").equals(user.getFirstName()))
                || (preferences.containsKey("lastname") && !preferences.get("lastname").equals(user.getLastName()))
                || (preferences.containsKey("email") && !preferences.get("email").equals(user.getEmail()));
    }

    @Override
    public String getUserPreferenceAsString(UserIdentity userIdentity, String storageContext, Map<String, String> contextVars, String id) throws UserPreferencesException
    {
        User user = _userManager.getUser(userIdentity);
        
        if ("firstname".equals(id))
        {
            return user.getFirstName();
        }
        else if ("lastname".equals(id))
        {
            return user.getLastName();
        }
        else if ("email".equals(id))
        {
            return user.getEmail();
        }
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
