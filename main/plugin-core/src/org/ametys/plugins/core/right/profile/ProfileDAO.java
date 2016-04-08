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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.group.GroupIdentity;
import org.ametys.core.group.InvalidModificationException;
import org.ametys.core.right.RightsManager;
import org.ametys.core.right.profile.Profile;
import org.ametys.core.ui.Callable;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.UserIdentity;
import org.ametys.plugins.core.impl.right.profile.ProfileBasedRightsManager;

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

    public void service(ServiceManager smanager) throws ServiceException
    {
        _smanager = smanager;
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
        RightsManager p = (RightsManager) _smanager.lookup(RightsManager.ROLE);
        
        if (!(p instanceof ProfileBasedRightsManager))
        {
            getLogger().error("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
            throw new InvalidModificationException("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
        }
        
        ProfileBasedRightsManager profiles = (ProfileBasedRightsManager) p;
        
        Profile profile = profiles.getProfile(id);
        if (profile == null)
        {
            return null;
        }
        return profile2Json(profile);
        
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
        RightsManager p = (RightsManager) _smanager.lookup(RightsManager.ROLE);
        if (!(p instanceof ProfileBasedRightsManager))
        {
            getLogger().error("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
            throw new InvalidModificationException("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
        }
        
        ProfileBasedRightsManager profiles = (ProfileBasedRightsManager) p;
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting profile creation");
        }
        
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("The profile name cannot be empty");
        }
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(String.format("User %s is adding a new profile '%s'", _getCurrentUser(), name));
        }
        
        Profile profile = profiles.addProfile(name, context);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending profile creation");
        }
        
        return profile2Json(profile);
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
        RightsManager p = (RightsManager) _smanager.lookup(RightsManager.ROLE);
        if (!(p instanceof ProfileBasedRightsManager))
        {
            getLogger().error("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
            throw new InvalidModificationException("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
        }
        
        ProfileBasedRightsManager profiles = (ProfileBasedRightsManager) p;
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting profile modification");
        }
        
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("The profile new name cannot be empty");
        }
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(String.format("User %s is renaming the profile '%s' to '%s'", _getCurrentUser(), id, name));
        }
        
        Profile profile = profiles.getProfile(id);
        if (profile == null)
        {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "unknown-profile");
            return result;
        }
        else
        {
            profile.rename(name);
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending profile modification");
        }
        
        return profile2Json(profile);
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
        RightsManager p = (RightsManager) _smanager.lookup(RightsManager.ROLE);
        if (!(p instanceof ProfileBasedRightsManager))
        {
            getLogger().error("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
            throw new InvalidModificationException("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
        }
        
        ProfileBasedRightsManager profiles = (ProfileBasedRightsManager) p;
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting profile modification");
        }
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(String.format("User %s is edit rights of profile '%s'", _getCurrentUser(), id));
        }
        
        Profile profile = profiles.getProfile(id);
        if (profile == null)
        {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "unknown-profile");
            return result;
        }
        else
        {
            profile.startUpdate();
            
            // Removes old rights first
            profile.removeRights();
            
            if (rights != null)
            {
                // Then add rights
                for (String right : rights)
                {
                    profile.addRight(right);
                }
            }
            
            profile.endUpdate();
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending profile modification");
        }
        
        return profile2Json(profile);
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
        RightsManager p = (RightsManager) _smanager.lookup(RightsManager.ROLE);
        if (!(p instanceof ProfileBasedRightsManager))
        {
            getLogger().error("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
            throw new InvalidModificationException("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
        }
        
        ProfileBasedRightsManager profiles = (ProfileBasedRightsManager) p;
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting profile removal");
        }
        
        for (String id : ids)
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(String.format("User %s is is removing profile '%s'", _getCurrentUser(), id));
            }
            
            Profile profile = profiles.getProfile(id);
            if (profile != null)
            {
                profile.remove();
            }
            else if (getLogger().isWarnEnabled())
            {
                getLogger().info(String.format("User %s is trying to remove an unexisting profile '%s'", _getCurrentUser(), id));
            }
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending profile removal");
        }
    }
    
    /**
     * Assigns profiles to users.
     * @param users The users to add
     * @param profilesIds The ids of profiles
     * @param context The context
     * @throws InvalidModificationException If modification are not possible
     * @throws ServiceException If there is an issue with the service manager
     */
    @Callable
    public void addUsers(List<Map<String, String>> users, List<String> profilesIds, String context) throws ServiceException, InvalidModificationException
    {
        List<UserIdentity> userIdentities = new ArrayList<>();
        for (Map<String, String> user : users)
        {
            String login = user.get("login");
            String populationId = user.get("population");
            userIdentities.add(new UserIdentity(login, populationId));
        }
        assignProfiles(userIdentities, Collections.EMPTY_LIST, profilesIds, context);
    }
    
    /**
     * Assigns profiles to groups.
     * @param groups The groups to add
     * @param profilesIds The ids of profiles
     * @param context The context
     * @throws InvalidModificationException If modification are not possible
     * @throws ServiceException If there is an issue with the service manager
     */
    @Callable
    public void addGroups(List<Map<String, String>> groups, List<String> profilesIds, String context) throws ServiceException, InvalidModificationException
    {
        List<GroupIdentity> groupIdentities = new ArrayList<>();
        for (Map<String, String> group : groups)
        {
            String id = group.get("id");
            String groupDirectory = group.get("groupDirectory");
            groupIdentities.add(new GroupIdentity(id, groupDirectory));
        }
        assignProfiles(Collections.EMPTY_LIST, groupIdentities, profilesIds, context);
    }
    
    /**
     * Assigns profiles to users and groups.
     * @param users The users to add
     * @param groupsIds The groups to add
     * @param profilesIds The ids of profiles
     * @param context The context
     * @throws InvalidModificationException If modification are not possible
     * @throws ServiceException If there is an issue with the service manager
     */
    public void assignProfiles(List<UserIdentity> users, List<GroupIdentity> groupsIds, List<String> profilesIds, String context) throws ServiceException, InvalidModificationException
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting assignment");
        }
        
        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is modifying the rights'assignment on context '" + context 
                + "'. New assignment concerns Users [" + _userListToString(users) + "]"
                + "'. and Groups [" + _groupListToString(groupsIds) + "]"
                + "'. with profiles [" + _stringListToString(profilesIds) + "]";
            userMessage = "User '" + _getCurrentUser() + "'";
            
            getLogger().info(userMessage + " " + endMessage);
        }
        
        // Ajoute les nouveau profils
        _addProfiles (users, groupsIds, context, profilesIds);

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending assignment");
        }
    }
    
    /**
     * Removes profile's assignment from users and groups for a given context
     * @param users The users to add
     * @param groups The groups to remove
     * @param profileId The id of the profile
     * @param context The context
     * @throws InvalidModificationException If modification are not possible
     * @throws ServiceException If there is an issue with the service manager
     */
    @Callable
    public void removeAssignment(List<Map<String, String>> users, List<Map<String, String>> groups, String profileId, String context) throws ServiceException, InvalidModificationException
    {
        List<UserIdentity> userIdentities = new ArrayList<>();
        for (Map<String, String> user : users)
        {
            String login = user.get("login");
            String populationId = user.get("population");
            userIdentities.add(new UserIdentity(login, populationId));
        }
        
        List<GroupIdentity> groupIdentities = new ArrayList<>();
        for (Map<String, String> group : groups)
        {
            String id = group.get("id");
            String groupDirectory = group.get("groupDirectory");
            groupIdentities.add(new GroupIdentity(id, groupDirectory));
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting removing assignment");
        }
        
        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is removing all the rights'assignment on context '" + context + "' for Users [" + _userListToString(userIdentities) + "] and Groups [" + _groupListToString(groupIdentities) + "]";
            userMessage = "User '" + _getCurrentUser() + "'";
            
            getLogger().info(userMessage + " " + endMessage);
        }
        
        // Retire les profils existants
        _removeProfile (userIdentities, groupIdentities, profileId, context);

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Endinf removing assignment");
        }
    }
    
    /**
     * Get the JSON representation of a profile
     * @param profile The profile
     * @return The profile
     */
    protected Map<String, Object> profile2Json (Profile profile)
    {
        Map<String, Object> infos = new HashMap<>();
        infos.put("id", profile.getId());
        infos.put("label", profile.getName());
        infos.put("context", profile.getContext());
        return infos;
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
    
    private String _stringListToString(List<String> string)
    {
        String result = "";
        for (int i = 0; string != null && i < string.size(); i++)
        {
            if (i > 0)
            {
                result += ", ";
            }
            result += string.get(i);
        }
        return result;
    }
    
    private String _userListToString(List<UserIdentity> userList)
    {
        String result = "";
        for (int i = 0; userList != null && i < userList.size(); i++)
        {
            if (i > 0)
            {
                result += ", ";
            }
            String login = userList.get(i).getLogin();
            String populationId = userList.get(i).getPopulationId();
            result += login + '(' + populationId + ')';
        }
        return result;
    }
    
    private String _groupListToString(List<GroupIdentity> groupList)
    {
        String result = "";
        for (int i = 0; groupList != null && i < groupList.size(); i++)
        {
            if (i > 0)
            {
                result += ", ";
            }
            String id = groupList.get(i).getId();
            String groupDirectory = groupList.get(i).getDirectoryId();
            result += id + '(' + groupDirectory + ')';
        }
        return result;
    }
    
    private void _addProfiles(List<UserIdentity> users, List<GroupIdentity> groups, String context, List<String> profiles) throws ServiceException, InvalidModificationException
    {
        RightsManager p = (RightsManager) _smanager.lookup(RightsManager.ROLE);
        
        if (!(p instanceof ProfileBasedRightsManager))
        {
            getLogger().error("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
            throw new InvalidModificationException("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
        }
        
        ProfileBasedRightsManager rightsManager = (ProfileBasedRightsManager) p;
        
        for (int i = 0; users != null && i < users.size(); i++)
        {
            for (int j = 0; profiles != null && j < profiles.size(); j++)
            {
                rightsManager.addUserRight(users.get(i), context, profiles.get(j));
            }
        }
    
        for (int i = 0; groups != null && i < groups.size(); i++)
        {
            for (int j = 0; profiles != null && j < profiles.size(); j++)
            {
                rightsManager.addGroupRight(groups.get(i), context, profiles.get(j));
            }
        }
    }
    
    private void _removeProfile(List<UserIdentity> users, List<GroupIdentity> groups, String profileId, String context) throws ServiceException, InvalidModificationException
    {
        RightsManager p = (RightsManager) _smanager.lookup(RightsManager.ROLE);
        
        if (!(p instanceof ProfileBasedRightsManager))
        {
            getLogger().error("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
            throw new InvalidModificationException("RightsManager is of class '" + p.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
        }
        
        ProfileBasedRightsManager rightsManager = (ProfileBasedRightsManager) p;
        
        for (int i = 0; users != null && i < users.size(); i++)
        {
            rightsManager.removeUserProfile(users.get(i), profileId, context);
        }
        for (int i = 0; groups != null && i < groups.size(); i++)
        {
            rightsManager.removeGroupProfile(groups.get(i), profileId, context);
        }
    }
}
