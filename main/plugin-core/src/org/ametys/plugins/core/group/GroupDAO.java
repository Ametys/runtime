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
package org.ametys.plugins.core.group;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.group.Group;
import org.ametys.core.group.GroupsManager;
import org.ametys.core.group.InvalidModificationException;
import org.ametys.core.group.ModifiableGroupsManager;
import org.ametys.core.ui.Callable;
import org.ametys.core.user.CurrentUserProvider;

/**
 * DAO for manipulating {@link Group}
 *
 */
public class GroupDAO extends AbstractLogEnabled implements Serviceable, Component
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
     * Creates a new group
     * @param name The group's name
     * @return The group's information
     * @throws InvalidModificationException
     * @throws ServiceException
     */
    @Callable
    public Map<String, Object> addGroup(String name) throws InvalidModificationException, ServiceException
    {
        return addGroup(name, null);
    }
    
    /**
     * Creates a new group
     * @param name The group's name
     * @param groupManagerRole The groups manager's role. Can be null or empty to use the default one.
     * @return The group's information
     * @throws InvalidModificationException
     * @throws ServiceException
     */
    @Callable
    public Map<String, Object> addGroup(String name, String groupManagerRole) throws InvalidModificationException, ServiceException
    {
        GroupsManager g = (GroupsManager) _smanager.lookup(StringUtils.isEmpty(groupManagerRole) ? GroupsManager.ROLE : groupManagerRole);
        if (!(g instanceof ModifiableGroupsManager))
        {
            getLogger().error("Groups are not modifiable !");
            throw new InvalidModificationException("Groups are not modifiable !");
        }
        
        ModifiableGroupsManager groups = (ModifiableGroupsManager) g;
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting group creation");
        }
        
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("The new group name cannot be empty");
        }
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(String.format("User %s is adding a new group '%s'", _isSuperUser() ? "Administrator" : _getCurrentUser(), name));
        }

        Group group = groups.add(name);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group creation");
        }
        
        return group2Json(group);
    }
    
    
    /**
     * Set the users' group
     * @param groupId The group's id
     * @param users The group's users
     * @param groupManagerRole groupManagerRole The groups manager's role. Can be null or empty to use the default one.
     * @return The group's information
     * @throws InvalidModificationException
     * @throws ServiceException
     */
    @Callable
    public Map<String, Object> setUsersGroup (String groupId, List<String> users) throws InvalidModificationException, ServiceException
    {
        return setUsersGroup(groupId, users, null);
    }
    
    /**
     * Set the users' group
     * @param groupId The group's id
     * @param users The group's users
     * @param groupManagerRole groupManagerRole The groups manager's role. Can be null or empty to use the default one.
     * @return The group's information
     * @throws InvalidModificationException
     * @throws ServiceException
     */
    @Callable
    public Map<String, Object> setUsersGroup (String groupId, List<String> users, String groupManagerRole) throws InvalidModificationException, ServiceException
    {
        GroupsManager g = (GroupsManager) _smanager.lookup(StringUtils.isEmpty(groupManagerRole) ? GroupsManager.ROLE : groupManagerRole);
        if (!(g instanceof ModifiableGroupsManager))
        {
            getLogger().error("Groups are not modifiable !");
            throw new InvalidModificationException("Groups are not modifiable !");
        }
        
        ModifiableGroupsManager groups = (ModifiableGroupsManager) g;
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting group modification");
        }
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(String.format("User %s is editing the group '%s'", _isSuperUser() ? "Administrator" : _getCurrentUser(), groupId));
        }

        Group group = groups.getGroup(groupId);
        if (group == null)
        {
            getLogger().warn(String.format("User %s tries to edit the group '%s' but the group does not exist.", _isSuperUser() ? "Administrator" : _getCurrentUser(), groupId));
            
            Map<String, Object> result = new HashMap<>();
            result.put("error", "unknown-group");
            return result;
        }
        else
        {
            // Edit users
            Group newUserGroup = new Group(group.getId(), group.getLabel());

            for (String login : users)
            {
                if (StringUtils.isNotBlank(login))
                {
                    newUserGroup.addUser(login);
                }
            }
            groups.update(newUserGroup);
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group modification");
        }

        return group2Json(group);
    }
    
    /**
     * Add users to group
     * @param groupId The group's id
     * @param users The users to add
     * @return The group's information
     * @throws InvalidModificationException
     * @throws ServiceException
     */
    @Callable
    public Map<String, Object> addUsersGroup (String groupId, List<String> users) throws InvalidModificationException, ServiceException
    {
        return addUsersGroup(groupId, users, null);
    }
    
    /**
     * Add users to group
     * @param groupId The group's id
     * @param users The users to add
     * @param groupManagerRole groupManagerRole groupManagerRole The groups manager's role. Can be null or empty to use the default one.
     * @return The group's information
     * @throws InvalidModificationException
     * @throws ServiceException
     */
    @Callable
    public Map<String, Object> addUsersGroup (String groupId, List<String> users, String groupManagerRole) throws InvalidModificationException, ServiceException
    {
        return _updateUsersGroup(groupId, users, groupManagerRole, false);
    }
    
    /**
     * Remove users from group
     * @param groupId The group's id
     * @param users The users to add
     * @return The group's information
     * @throws InvalidModificationException
     * @throws ServiceException
     */
    @Callable
    public Map<String, Object> removeUsersGroup (String groupId, List<String> users) throws InvalidModificationException, ServiceException
    {
        return removeUsersGroup(groupId, users, null);
    }
    
    /**
     * Remove users from group
     * @param groupId The group's id
     * @param users The users to add
     * @param groupManagerRole groupManagerRole groupManagerRole The groups manager's role. Can be null or empty to use the default one.
     * @return The group's information
     * @throws InvalidModificationException
     * @throws ServiceException
     */
    @Callable
    public Map<String, Object> removeUsersGroup (String groupId, List<String> users, String groupManagerRole) throws InvalidModificationException, ServiceException
    {
        return _updateUsersGroup(groupId, users, groupManagerRole, true);
    }
    
    private Map<String, Object> _updateUsersGroup (String groupId, List<String> users, String groupManagerRole, boolean remove) throws InvalidModificationException, ServiceException
    {
        GroupsManager g = (GroupsManager) _smanager.lookup(StringUtils.isEmpty(groupManagerRole) ? GroupsManager.ROLE : groupManagerRole);
        if (!(g instanceof ModifiableGroupsManager))
        {
            getLogger().error("Groups are not modifiable !");
            throw new InvalidModificationException("Groups are not modifiable !");
        }
        
        ModifiableGroupsManager groups = (ModifiableGroupsManager) g;
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting group modification");
        }
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(String.format("User %s is editing the group '%s'", _isSuperUser() ? "Administrator" : _getCurrentUser(), groupId));
        }

        Group group = groups.getGroup(groupId);
        if (group == null)
        {
            getLogger().warn(String.format("User %s tries to edit the group '%s' but the group does not exist.", _isSuperUser() ? "Administrator" : _getCurrentUser(), groupId));
            
            Map<String, Object> result = new HashMap<>();
            result.put("error", "unknown-group");
            return result;
        }
        else
        {
            for (String login : users)
            {
                if (StringUtils.isNotBlank(login))
                {
                    if (remove)
                    {
                        group.removeUser(login);
                    }
                    else
                    {
                        group.addUser(login);
                    }
                }
            }
            groups.update(group);
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group modification");
        }

        return group2Json(group);
    }
    
    /**
     * Renames a group
     * @param name The group's new name
     * @return The group's information
     * @throws InvalidModificationException
     * @throws ServiceException
     */
    @Callable
    public Map<String, Object> renameGroup(String id, String name) throws InvalidModificationException, ServiceException
    {
        return renameGroup(id, name, null);
    }
    
    /**
     * Renames a group
     * @param groupId The group'sid
     * @param name The new name
     * @param groupManagerRole groupManagerRole The groups manager's role. Can be null or empty to use the default one.
     * @return The group's information
     * @throws ServiceException
     * @throws InvalidModificationException
     */
    @Callable
    public Map<String, Object> renameGroup (String groupId, String name, String groupManagerRole) throws ServiceException, InvalidModificationException
    {
        GroupsManager g = (GroupsManager) _smanager.lookup(StringUtils.isEmpty(groupManagerRole) ? GroupsManager.ROLE : groupManagerRole);
        if (!(g instanceof ModifiableGroupsManager))
        {
            getLogger().error("Groups are not modifiable !");
            throw new InvalidModificationException("Groups are not modifiable !");
        }
        
        ModifiableGroupsManager groups = (ModifiableGroupsManager) g;
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting group renaming");
        }
        
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("The new group name cannot be empty");
        }
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(String.format("User %s is renaming the group '%s' to '%s'", _isSuperUser() ? "Administrator" : _getCurrentUser(), groupId, name));
        }
        
        Group group = groups.getGroup(groupId);
        if (group == null)
        {
            getLogger().warn(String.format("User %s tries to rename the group '%s' but the group does not exist.", _isSuperUser() ? "Administrator" : _getCurrentUser(), groupId));
            
            Map<String, Object> result = new HashMap<>();
            result.put("error", "unknown-group");
            return result;
        }
        else
        {
            group.setLabel(name);
            groups.update(group);
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group renaming");
        }
        
        return group2Json(group);
    }
    
    /**
     * Deletes groups
     * @param groupIds The ids of groups to delete
     * @param groupManagerRole The group manager's role. Can be null or empty to use the default one.
     * @throws ServiceException
     * @throws InvalidModificationException
     */
    @Callable
    public void deleteGroups (List<String> groupIds, String groupManagerRole) throws InvalidModificationException, ServiceException
    {
        GroupsManager g = (GroupsManager) _smanager.lookup(StringUtils.isEmpty(groupManagerRole) ? GroupsManager.ROLE : groupManagerRole);
        if (!(g instanceof ModifiableGroupsManager))
        {
            getLogger().error("Groups are not modifiable !");
            throw new InvalidModificationException("Groups are not modifiable !");
        }
        
        ModifiableGroupsManager groups = (ModifiableGroupsManager) g;
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting group removal");
        }
        
        for (String groupId : groupIds)
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(String.format("User %s is is removing group '%s'", _isSuperUser() ? "Administrator" : _getCurrentUser(), groupId));
            }
            
            groups.remove(groupId);
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group removal");
        }
    }
    
    /**
     * Get group's information
     * @param id the group id
     * @return group's information
     * @throws ServiceException 
     */
    @Callable
    public Map<String, Object> getGroup (String id) throws ServiceException
    {
        return getGroup(id, null);
    }
    
    /**
     * Get group's information
     * @param id the group id
     * @param groupManagerRole The group manager's role. Can be null or empty to use the default one.
     * @return group's information
     * @throws ServiceException 
     */
    @Callable
    public Map<String, Object> getGroup (String id, String groupManagerRole) throws ServiceException
    {
        GroupsManager groups = (GroupsManager) _smanager.lookup(StringUtils.isEmpty(groupManagerRole) ? GroupsManager.ROLE : groupManagerRole);
        Group group = groups.getGroup(id);
        if (group != null)
        {
            return group2Json(group);
        }
        return null;
    }
    
    /**
     * Get the JSON representation of a group
     * @param group The group
     * @return The group
     */
    protected Map<String, Object> group2Json (Group group)
    {
        Map<String, Object> infos = new HashMap<>();
        infos.put("id", group.getId());
        infos.put("label", group.getLabel());
        return infos;
    }
    
    /**
     * Provides the login of the current user.
     * @return the login which cannot be <code>null</code>.
     */
    protected String _getCurrentUser()
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
        
        if (!_currentUserProvider.isSuperUser())
        {
            return _currentUserProvider.getUser();
        }
        
        return "admin";
    }
    
    /**
     * Determine if current user is the super user.
     * @return <code>true</code> if the super user is logged in,
     *         <code>false</code> otherwise.
     */
    protected boolean _isSuperUser()
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
        
        return _currentUserProvider.isSuperUser();
    }
}
