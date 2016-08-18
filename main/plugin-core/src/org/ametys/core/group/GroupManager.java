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
package org.ametys.core.group;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.user.UserIdentity;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Component for getting group list.
 */
public class GroupManager extends AbstractLogEnabled implements Component, Serviceable
{
    /** Avalon Role */
    public static final String ROLE = GroupManager.class.getName();
    
    /** The DAO for group directories */
    protected GroupDirectoryDAO _groupDirectoryDAO;
    /** The helper for the associations group directory/context */
    protected GroupDirectoryContextHelper _directoryContextHelper;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _groupDirectoryDAO = (GroupDirectoryDAO) manager.lookup(GroupDirectoryDAO.ROLE);
        _directoryContextHelper = (GroupDirectoryContextHelper) manager.lookup(GroupDirectoryContextHelper.ROLE);
    }
    
    // ------------------------------
    //    GET A PARTICULAR GROUP
    // ------------------------------
    
    /**
     * Get a particular group of the given {@link GroupDirectory} by its id
     * @param groupIdentity The group identity
     * @return The group or null if not found
     */
    public Group getGroup(GroupIdentity groupIdentity)
    {
        return getGroup(groupIdentity.getDirectoryId(), groupIdentity.getId());
    }
    
    /**
     * Get a particular group of the given {@link GroupDirectory} by its id
     * @param groupDirectory The group directory
     * @param groupId The id of the group
     * @return The group or null if not found
     */
    public Group getGroup(GroupDirectory groupDirectory, String groupId)
    {
        return groupDirectory.getGroup(groupId);
    }
    
    /**
     * Get a particular group of the given {@link GroupDirectory} by its id
     * @param groupDirectoryId The id of the group directory
     * @param groupId The id of the group
     * @return The group or null if not found
     */
    public Group getGroup(String groupDirectoryId, String groupId)
    {
        GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
        if (groupDirectory != null)
        {
            return getGroup(groupDirectory, groupId);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Get a particular group on a given context
     * @param context The context
     * @param groupId The id of the group
     * @return The group or null if not found
     */
    public Group getGroupByContext(String context, String groupId)
    {
        for (String directoryId : _directoryContextHelper.getGroupDirectoriesOnContext(context))
        {
            Group group = getGroup(directoryId, groupId);
            if (group != null)
            {
                return group;
            }
        }
        return null;
    }
    
    // ------------------------------
    //    GET A SET OF GROUPS
    // ------------------------------
    
    /**
     * Get all the groups of a given {@link GroupDirectory}
     * @param groupDirectory The group directory
     * @return The set of groups of the group directory
     */
    public Set<Group> getGroups(GroupDirectory groupDirectory)
    {
        return groupDirectory.getGroups();
    }
    
    /**
     * Get all the groups of a given {@link GroupDirectory}
     * @param groupDirectoryId The id of the group directory
     * @return The set of groups of the group directory
     */
    public Set<Group> getGroups(String groupDirectoryId)
    {
        GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
        if (groupDirectory != null)
        {
            return getGroups(groupDirectory);
        }
        else
        {
            return Collections.emptySet();
        }
    }
    
    /**
     * Get all the groups on a given context
     * @param context The context
     * @return The set of groups
     */
    public Set<Group> getGroupsByContext(String context)
    {
        Set<Group> groups = new LinkedHashSet<>();
        
        for (String directoryId : _directoryContextHelper.getGroupDirectoriesOnContext(context))
        {
            groups.addAll(getGroups(directoryId));
        }
        
        return groups;
    }
    
    // ------------------------------
    //    GET GROUPS A USER IS IN
    // ------------------------------
    
    /**
     * Get all the groups the given user is in
     * @param userId The user identity.
     * @return The set of groups the user is in
     */
    public Set<GroupIdentity> getUserGroups(UserIdentity userId)
    {
        return getUserGroups(userId.getLogin(), userId.getPopulationId());
    }
    
    /**
     * Get all the groups the given user is in
     * @param login The login of the user
     * @param populationId The id of the population of the user
     * @return  A set of groups the user is in
     */
    public Set<GroupIdentity> getUserGroups(String login, String populationId)
    {
        Set<GroupIdentity> result = new HashSet<>();
        for (GroupDirectory groupDirectory : _groupDirectoryDAO.getGroupDirectories())
        {
            for (String groupId : getUserGroups(groupDirectory, login, populationId))
            {
                result.add(new GroupIdentity(groupId, groupDirectory.getId()));
            }
        }
        
        return result;
    }
    
    /**
     * Get all the groups of a {@link GroupDirectory} the given user is in
     * @param groupDirectory The group directory
     * @param login The login of the user
     * @param populationId The id of the population of the user
     * @return A set of groups the user is in
     */
    public Set<String> getUserGroups(GroupDirectory groupDirectory, String login, String populationId)
    {
        return groupDirectory.getUserGroups(login, populationId);
    }
    
    /**
     * Get all the groups of a {@link GroupDirectory} the given user is in
     * @param groupDirectoryId The id of the group directory
     * @param login The login of the user
     * @param populationId The id of the population of the user
     * @return A set of groups the user is in
     */
    public Set<String> getUserGroups(String groupDirectoryId, String login, String populationId)
    {
        GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
        if (groupDirectory != null)
        {
            return getUserGroups(groupDirectory, login, populationId);
        }
        else
        {
            return Collections.emptySet();
        }
    }

}
