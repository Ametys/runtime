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
package org.ametys.core.right;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import org.ametys.core.group.GroupIdentity;
import org.ametys.core.group.GroupManager;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;

/**
 * Wrapper class to represent a set of allowed users, which can eventually be anonymous or any connected user.
 */
public class AllowedUsers
{
    private boolean _anonymousAllowed;
    private boolean _anyConnectedUserAllowed;
    private Set<UserIdentity> _allowedUsers;
    private Set<UserIdentity> _deniedUsers;
    private Set<GroupIdentity> _allowedGroups;
    private Set<GroupIdentity> _deniedGroups;
    
    private UserManager _userManager;
    private Set<String> _populationContexts;
    private GroupManager _groupManager;

    /**
     * Creates an object representing allowed users.
     * @param anonymousAllowed true to indicate any anonymous user is allowed
     * @param anyConnectedUserAllowed if anonymous is false, true to indicate any connected user is allowed  
     * @param allowedUsers the allowed users, not taking into account the denied users. Must be null if anonymous or anyConnectedUser is true, must not otherwise.
     * @param deniedUsers the denied users. Must be null if anonymous is true, must not otherwise.
     * @param userManager The user manager
     * @param populationContexts The population contexts for retrieving users from user manager. Can be null if anyConnectedUser is false
     */
    AllowedUsers(boolean anonymousAllowed, boolean anyConnectedUserAllowed, Set<UserIdentity> allowedUsers, Set<UserIdentity> deniedUsers, Set<GroupIdentity> allowedGroups, Set<GroupIdentity> deniedGroups, UserManager userManager, GroupManager groupManager, Set<String> populationContexts)
    {
        _anonymousAllowed = anonymousAllowed;
        _anyConnectedUserAllowed = anyConnectedUserAllowed;
        _allowedUsers = allowedUsers;
        _deniedUsers = deniedUsers;
        _allowedGroups = allowedGroups;
        _deniedGroups = deniedGroups;
        
        _userManager = userManager;
        _groupManager = groupManager;
        _populationContexts = populationContexts;
    }
    
    /**
     * Returns true if any anonymous user is allowed
     * @return true if any anonymous user is allowed
     */
    public boolean isAnonymousAllowed()
    {
        return _anonymousAllowed;
    }
    
    /**
     * Returns true if any connected user is allowed
     * @return true if any connected user is allowed
     */
    public boolean isAnyConnectedUserAllowed()
    {
        return !_anonymousAllowed && _anyConnectedUserAllowed;
    }
    
    /**
     * Get the allowed users
     * @return The allowed users
     */
    public Set<UserIdentity> getAllowedUsers()
    {
        return _allowedUsers;
    }
    
    /**
     * Get the denied users
     * @return The denied users
     */
    public Set<UserIdentity> getDeniedUsers()
    {
        return _deniedUsers;
    }
    
    /**
     * Get the allowed groups
     * @return The allowed groups
     */
    public Set<GroupIdentity> getAllowedGroups()
    {
        return _allowedGroups;
    }
    
    /**
     * Get the allowed groups
     * @return The allowed groups
     */
    public Set<GroupIdentity> getDeniedGroups()
    {
        return _deniedGroups;
    }
    
    /**
     * Resolve the actual allowed users, taking into account the anyconnected, allowed and denied users and groups.
     * If anonymous is allowed, it will return an empty list.
     * @param returnAll Set to <code>true</code> to resolve all users if any connected user is allowed. If <code>false</code>, returns an empty Set if any connected user is allowed.
     * @return the computed actual allowed users
     */
    public Set<UserIdentity> resolveAllowedUsers (boolean returnAll)
    {
        if (_anonymousAllowed || (_anyConnectedUserAllowed && !returnAll))
        {
            return Collections.EMPTY_SET;
        }
        else if (_anyConnectedUserAllowed)
        {
            // Retrieve all users from the user manager, and remove just the denied ones
            Set<UserIdentity> allowedUsers = _userManager.getUsersByContext(_populationContexts).stream().map(User::getIdentity).collect(Collectors.toSet());
            
            Set<UserIdentity> resolvedDeniedUsers = new HashSet<>();
            resolvedDeniedUsers.addAll(_deniedUsers);
            
            // Remove the users of the denied groups to the resolvedDeniedUsers
            // The users to remove are only those which are in deniedGroups and not in allAllowedUsers
            for (GroupIdentity deniedGroup : _deniedGroups)
            {
                Set<UserIdentity> groupUsers = _groupManager.getGroup(deniedGroup).getUsers();
                for (UserIdentity groupUser : groupUsers)
                {
                    if (!_allowedUsers.contains(groupUser))
                    {
                        resolvedDeniedUsers.add(groupUser);
                    }
                }
            }
            
            return new HashSet<>(CollectionUtils.removeAll(allowedUsers, resolvedDeniedUsers));
        }
        else
        {
            Set<UserIdentity> resolvedAllowedUsers = new HashSet<>();
            
            // Retrieve the users from the allowed groups
            for (GroupIdentity allowedGroup : _allowedGroups)
            {
                Set<UserIdentity> groupUsers = _groupManager.getGroup(allowedGroup).getUsers();
                resolvedAllowedUsers.addAll(groupUsers);
            }
            
            // Remove the users of the denied groups
            for (GroupIdentity deniedGroup : _deniedGroups)
            {
                Set<UserIdentity> groupUsers = _groupManager.getGroup(deniedGroup).getUsers();
                resolvedAllowedUsers.removeAll(groupUsers);
            }
            
            // Add the allowed users
            resolvedAllowedUsers.addAll(_allowedUsers);
            
            // Remove the denied users
            resolvedAllowedUsers.removeAll(_deniedUsers);
            
            return resolvedAllowedUsers;
        }
    }
}
