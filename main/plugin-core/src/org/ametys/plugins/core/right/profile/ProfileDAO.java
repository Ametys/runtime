/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.plugins.core.right.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.ObservationConstants;
import org.ametys.core.group.InvalidModificationException;
import org.ametys.core.observation.Event;
import org.ametys.core.observation.ObservationManager;
import org.ametys.core.right.Profile;
import org.ametys.core.right.RightManager;
import org.ametys.core.right.RightProfilesDAO;
import org.ametys.core.right.RightsException;
import org.ametys.core.ui.Callable;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.UserIdentity;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * DAO for manipulating {@link Profile}
 *
 */
public class ProfileDAO extends AbstractLogEnabled implements Serviceable, Component
{
    /** The service manager */
    protected ServiceManager _smanager;
    /** The current user provider. */
    protected CurrentUserProvider _currentUserProvider;
    /** The right manager */
    protected RightManager _rightManager;
    /** The SQL DAO */
    protected RightProfilesDAO _profilesDAO;

    public void service(ServiceManager smanager) throws ServiceException
    {
        _smanager = smanager;
        _rightManager = (RightManager) smanager.lookup(RightManager.ROLE);
        _profilesDAO = (RightProfilesDAO) smanager.lookup(RightProfilesDAO.ROLE);
    }
    
    /**
     * Get profile's properties
     * @param id The profile's id
     * @return The profile's information
     * @throws InvalidModificationException If modification are not possible
     * @throws ServiceException If there is an issue with the service manager
     */
    @Callable
    public Map<String, Object> getProfile (String id) throws ServiceException, InvalidModificationException
    {
        Profile profile = _profilesDAO.getProfile(id);
        if (profile == null)
        {
            return null;
        }
        return profile.toJSON();
        
    }

    /**
     * Creates a new profile
     * @param name The profile's name
     * @param context The profile's context
     * @return The profile's information
     * @throws InvalidModificationException If modification are not possible
     * @throws ServiceException If there is an issue with the service manager
     */
    @Callable
    public Map<String, Object> addProfile (String name, String context) throws ServiceException, InvalidModificationException
    {
        getLogger().debug("Starting profile creation");
    
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("The profile name cannot be empty");
        }
        
        getLogger().info("User {} is adding a new profile '{}'", _getCurrentUser(), name);
        
        Profile profile = _profilesDAO.addProfile(name, context);
        
        getLogger().debug("Ending profile creation");
        
        return profile.toJSON();
    }
    
    /**
     * Renames a profile
     * @param id The profile's id
     * @param name The profile's new name
     * @return The profile's information
     * @throws InvalidModificationException If modification are not possible
     * @throws ServiceException If there is an issue with the service manager
     */
    @Callable
    public Map<String, Object> renameProfile (String id, String name) throws ServiceException, InvalidModificationException
    {
        getLogger().debug("Starting profile modification");
        
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("The profile new name cannot be empty");
        }
        
        getLogger().info("User {} is renaming the profile '{}' to '{}'", _getCurrentUser(), id, name);
        
        Profile profile = _profilesDAO.getProfile(id);
        if (profile == null)
        {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "unknown-profile");
            return result;
        }
        else
        {
            _profilesDAO.renameProfile(profile, name);
        }
        
        getLogger().debug("Ending profile modification");
        
        return profile.toJSON();
    }
    
    /**
     * Edit profile's rights
     * @param id The profile's id
     * @param rights The profile's rights
     * @return The profile's information
     * @throws InvalidModificationException If modification are not possible
     * @throws ServiceException If there is an issue with the service manager
     */
    @Callable
    public Map<String, Object> editProfileRights (String id, List<String> rights) throws ServiceException, InvalidModificationException
    {
        getLogger().debug("Starting profile modification");
        
        getLogger().info("User {} is edit rights of profile '{}'", _getCurrentUser(), id);
        
        Profile profile = _profilesDAO.getProfile(id);
        if (profile == null)
        {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "unknown-profile");
            return result;
        }
        else
        {
            _profilesDAO.updateRights(profile, rights);
        }
        
        getLogger().debug("Ending profile modification");
        
        return profile.toJSON();
    }
    
    
    /**
     * Deletes profiles
     * @param ids The ids of profiles to delete
     * @throws InvalidModificationException If modification are not possible
     * @throws ServiceException If there is an issue with the service manager
     */
    @Callable
    public void deleteProfiles (List<String> ids) throws InvalidModificationException, ServiceException
    {
        getLogger().debug("Starting profile removal");
        
        for (String id : ids)
        {
            if (RightManager.READER_PROFILE_ID.equals(id))
            {
                throw new RightsException("You cannot remove the system profile 'READER'");
            }
            
            getLogger().info("User {} is is removing profile '{}'", _getCurrentUser(), id);
            
            Profile profile = _profilesDAO.getProfile(id);
            if (profile != null)
            {
                _profilesDAO.deleteProfile(profile);
            }
            else
            {
                getLogger().info("User {} is trying to remove an unexisting profile '{}'", _getCurrentUser(), id);
            }
        }

        getLogger().debug("Ending profile removal");
    }
    
    /**
     * Provides the login of the current user.
     * @return the login which cannot be <code>null</code>.
     */
    protected UserIdentity _getCurrentUser()
    {
        if (_currentUserProvider == null)
        {
            try
            {
                _currentUserProvider = (CurrentUserProvider) _smanager.lookup(CurrentUserProvider.ROLE);
            }
            catch (ServiceException e)
            {
                throw new IllegalStateException(e);
            }
        }
        
        return _currentUserProvider.getUser();
    }
}
