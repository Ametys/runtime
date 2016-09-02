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

import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;

/**
 * Wrapper class to represent a set of allowed users, which can eventually be anonymous or any connected user.
 */
public class AllowedUsers
{
    private boolean _anonymous;
    private boolean _anyConnectedUser;
    private Set<UserIdentity> _allowedUsers;
    private Set<UserIdentity> _deniedUsers;
    private UserManager _userManager;
    private Set<String> _populationContexts;

    /**
     * Creates an object representing allowed users.
     * @param anonymous true to indicate any anonymous user is allowed
     * @param anyConnectedUser if anonymous is false, true to indicate any connected user is allowed  
     * @param allowedUsers the allowed users, not taking into account the denied users. Must be null if anonymous or anyConnectedUser is true, must not otherwise.
     * @param deniedUsers the denied users. Must be null if anonymous is true, must not otherwise.
     * @param userManager The user manager
     * @param populationContexts The population contexts for retrieving users from user manager. Can be null if anyConnectedUser is false
     */
    public AllowedUsers(boolean anonymous, boolean anyConnectedUser, Set<UserIdentity> allowedUsers, Set<UserIdentity> deniedUsers, UserManager userManager, Set<String> populationContexts)
    {
        _anonymous = anonymous;
        _anyConnectedUser = anyConnectedUser;
        _allowedUsers = allowedUsers;
        _deniedUsers = deniedUsers;
        _userManager = userManager;
        _populationContexts = populationContexts;
    }
    
    /**
     * Returns true if any anonymous user is allowed
     * @return true if any anonymous user is allowed
     */
    public boolean isAnonymousAllowed()
    {
        return _anonymous;
    }
    
    /**
     * Returns true if any connected user is allowed
     * @return true if any connected user is allowed
     */
    public boolean isAnyConnectedUserAllowed()
    {
        return !_anonymous && _anyConnectedUser;
    }
    
    /**
     * Computes the actual allowed users, taking into account the anyconnected, allowed and denied users.
     * If anonymous is allowed, it will return an empty list.
     * @param returnAll Set this to true to have normal behavior, and return all users in case {@link AllowedUsers#isAnyConnectedUserAllowed()} returns true. Set this to false to return an empty list in case {@link AllowedUsers#isAnyConnectedUserAllowed()} returns true
     * @return the computed actual allowed users
     */
    public Set<UserIdentity> actualAllowedUsers(boolean returnAll)
    {
        if (_anonymous || _anyConnectedUser && !returnAll)
        {
            return Collections.EMPTY_SET;
        }
        else if (_anyConnectedUser)
        {
            // Retrieve all users from the user manager, and remove just the denied ones
            Set<UserIdentity> allUsers = _userManager.getUsersByContext(_populationContexts).stream().map(User::getIdentity).collect(Collectors.toSet());
            
            return new HashSet<>(CollectionUtils.removeAll(allUsers, _deniedUsers));
        }
        else
        {
            // It's just _allowedUsers, minus _deniedUsers
            return new HashSet<>(CollectionUtils.removeAll(_allowedUsers, _deniedUsers));
        }
    }
}
