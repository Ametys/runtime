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
import org.ametys.core.group.GroupDirectoryDAO;
import org.ametys.core.group.GroupManager;
import org.ametys.core.group.InvalidModificationException;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.group.directory.ModifiableGroupDirectory;
import org.ametys.core.ui.Callable;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.UserIdentity;

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
    /** The group manager */
    protected GroupManager _groupManager;
    /** The DAO for group directories */
    protected GroupDirectoryDAO _groupDirectoryDAO;

    public void service(ServiceManager smanager) throws ServiceException
    {
        _smanager = smanager;
        _groupManager = (GroupManager) smanager.lookup(GroupManager.ROLE);
        _groupDirectoryDAO = (GroupDirectoryDAO) smanager.lookup(GroupDirectoryDAO.ROLE);
    }
    
    /**
     * Creates a new group
     * @param groupDirectoryId The id of the group directory
     * @param name The group's name
     * @return The group's information
     * @throws InvalidModificationException If modification are not possible
     */
    @Callable
    public Map<String, Object> addGroup(String groupDirectoryId, String name) throws InvalidModificationException
    {
        GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
        
        if (!(groupDirectory instanceof ModifiableGroupDirectory))
        {
            getLogger().error("Groups are not modifiable !");
            throw new InvalidModificationException("Groups are not modifiable !");
        }
        
        ModifiableGroupDirectory modifiableGroupDirectory = (ModifiableGroupDirectory) groupDirectory;
        
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
            getLogger().info(String.format("User %s is adding a new group '%s'", _getCurrentUser(), name));
        }

        Group group = modifiableGroupDirectory.add(name);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group creation");
        }
        
        return group2Json(group);
    }
    
    /**
     * Set the users' group
     * @param groupDirectoryId The id of the group directory
     * @param groupId The group's id
     * @param users The group's users
     * @return The group's information
     * @throws InvalidModificationException If modification are not possible
     */
    @Callable
    public Map<String, Object> setUsersGroup (String groupDirectoryId, String groupId, List<List<String>> users) throws InvalidModificationException
    {
        GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
        
        if (!(groupDirectory instanceof ModifiableGroupDirectory))
        {
            getLogger().error("Groups are not modifiable !");
            throw new InvalidModificationException("Groups are not modifiable !");
        }
        
        ModifiableGroupDirectory modifiableGroupDirectory = (ModifiableGroupDirectory) groupDirectory;
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting group modification");
        }
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(String.format("User %s is editing the group '%s'", _getCurrentUser(), groupId));
        }

        Group group = modifiableGroupDirectory.getGroup(groupId);
        if (group == null)
        {
            getLogger().warn(String.format("User %s tries to edit the group '%s' but the group does not exist.", _getCurrentUser(), groupId));
            
            Map<String, Object> result = new HashMap<>();
            result.put("error", "unknown-group");
            return result;
        }
        else
        {
            // Edit users
            Group newUserGroup = new Group(group.getIdentity(), group.getLabel(), group.getGroupDirectory());

            for (List<String> user : users)
            {
                String login = user.get(0);
                String populationId = user.get(1);
                if (StringUtils.isNotBlank(login) && StringUtils.isNotBlank(populationId))
                {
                    newUserGroup.addUser(new UserIdentity(login, populationId));
                }
            }
            modifiableGroupDirectory.update(newUserGroup);
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group modification");
        }

        return group2Json(group);
    }
    
    /**
     * Add users to group
     * @param groupDirectoryId The id of the group directory
     * @param groupId The group's id
     * @param users The users to add
     * @return The group's information
     * @throws InvalidModificationException If modification are not possible
     */
    @Callable
    public Map<String, Object> addUsersGroup (String groupDirectoryId, String groupId, List<Map<String, String>> users) throws InvalidModificationException
    {
        return _updateUsersGroup(groupDirectoryId, groupId, users, false);
    }
    
    /**
     * Remove users from group
     * @param groupDirectoryId The id of the group directory
     * @param groupId The group's id
     * @param users The users to add
     * @return The group's information
     * @throws InvalidModificationException If modification are not possible
     */
    @Callable
    public Map<String, Object> removeUsersGroup (String groupDirectoryId, String groupId, List<Map<String, String>> users) throws InvalidModificationException
    {
        return _updateUsersGroup(groupDirectoryId, groupId, users, true);
    }
    
    private Map<String, Object> _updateUsersGroup (String groupDirectoryId, String groupId, List<Map<String, String>> users, boolean remove) throws InvalidModificationException
    {
        GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
        
        if (!(groupDirectory instanceof ModifiableGroupDirectory))
        {
            getLogger().error("Groups are not modifiable !");
            throw new InvalidModificationException("Groups are not modifiable !");
        }
        
        ModifiableGroupDirectory modifiableGroupDirectory = (ModifiableGroupDirectory) groupDirectory;
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting group modification");
        }
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info(String.format("User %s is editing the group '%s'", _getCurrentUser(), groupId));
        }

        Group group = modifiableGroupDirectory.getGroup(groupId);
        if (group == null)
        {
            getLogger().warn(String.format("User %s tries to edit the group '%s' but the group does not exist.", _getCurrentUser(), groupId));
            
            Map<String, Object> result = new HashMap<>();
            result.put("error", "unknown-group");
            return result;
        }
        else
        {
            for (Map<String, String> user : users)
            {
                String login = user.get("login");
                String populationId = user.get("population");
                if (StringUtils.isNotBlank(login) && StringUtils.isNotBlank(populationId))
                {
                    if (remove)
                    {
                        group.removeUser(new UserIdentity(login, populationId));
                    }
                    else
                    {
                        group.addUser(new UserIdentity(login, populationId));
                    }
                }
            }
            modifiableGroupDirectory.update(group);
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group modification");
        }

        return group2Json(group);
    }
    
    /**
     * Renames a group
     * @param groupDirectoryId The id of the group directory
     * @param groupId The group'sid
     * @param name The new name
     * @return The group's information
     * @throws InvalidModificationException If modification are not possible
     */
    @Callable
    public Map<String, Object> renameGroup (String groupDirectoryId, String groupId, String name) throws InvalidModificationException
    {
        GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
        
        if (!(groupDirectory instanceof ModifiableGroupDirectory))
        {
            getLogger().error("Groups are not modifiable !");
            throw new InvalidModificationException("Groups are not modifiable !");
        }
        
        ModifiableGroupDirectory modifiableGroupDirectory = (ModifiableGroupDirectory) groupDirectory;
        
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
            getLogger().info(String.format("User %s is renaming the group '%s' to '%s'", _getCurrentUser(), groupId, name));
        }
        
        Group group = modifiableGroupDirectory.getGroup(groupId);
        if (group == null)
        {
            getLogger().warn(String.format("User %s tries to rename the group '%s' but the group does not exist.", _getCurrentUser(), groupId));
            
            Map<String, Object> result = new HashMap<>();
            result.put("error", "unknown-group");
            return result;
        }
        else
        {
            group.setLabel(name);
            modifiableGroupDirectory.update(group);
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group renaming");
        }
        
        return group2Json(group);
    }
    
    /**
     * Deletes groups
     * @param groupDirectoryId The id of the group directory
     * @param groupIds The ids of groups to delete
     * @throws InvalidModificationException If modification are not possible
     */
    @Callable
    public void deleteGroups (String groupDirectoryId, List<String> groupIds) throws InvalidModificationException
    {
        GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
        
        if (!(groupDirectory instanceof ModifiableGroupDirectory))
        {
            getLogger().error("Groups are not modifiable !");
            throw new InvalidModificationException("Groups are not modifiable !");
        }
        
        ModifiableGroupDirectory modifiableGroupDirectory = (ModifiableGroupDirectory) groupDirectory;
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting group removal");
        }
        
        for (String groupId : groupIds)
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(String.format("User %s is is removing group '%s'", _getCurrentUser(), groupId));
            }
            
            modifiableGroupDirectory.remove(groupId);
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group removal");
        }
    }
    
    /**
     * Get group's information
     * @param groupDirectoryId The id of the group directory
     * @param id the group id
     * @return group's information
     */
    @Callable
    public Map<String, Object> getGroup (String groupDirectoryId, String id)
    {
        GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
        
        Group group = groupDirectory.getGroup(id);
        if (group != null)
        {
            return group2Json(group);
        }
        return null;
    }
    
    /**
     * Checks if the group is modifiable
     * @param groupDirectoryId The id of the group directory
     * @param id The group id
     * @return True if the group is modifiable
     */
    @Callable
    public boolean isModifiable (String groupDirectoryId, String id)
    {
        return _groupDirectoryDAO.getGroupDirectory(groupDirectoryId) instanceof ModifiableGroupDirectory;
    }
    
    /**
     * Get the JSON representation of a group
     * @param group The group
     * @return The group
     */
    protected Map<String, Object> group2Json (Group group)
    {
        Map<String, Object> infos = new HashMap<>();
        infos.put("id", group.getIdentity().getId());
        infos.put("label", group.getLabel());
        infos.put("groupDirectory", group.getIdentity().getDirectoryId());
        return infos;
    }
    
    /**
     * Provides the current user.
     * @return the user which cannot be <code>null</code>.
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
