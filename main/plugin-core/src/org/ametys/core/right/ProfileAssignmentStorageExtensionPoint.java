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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import org.ametys.core.group.GroupIdentity;
import org.ametys.core.right.AccessController.AccessResult;
import org.ametys.core.right.AccessController.AccessResultContext;
import org.ametys.core.user.UserIdentity;
import org.ametys.runtime.plugin.ExtensionPoint;
import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

/**
 * {@link ExtensionPoint} handling {@link ProfileAssignmentStorage}s.
 */
public class ProfileAssignmentStorageExtensionPoint extends AbstractThreadSafeComponentExtensionPoint<ProfileAssignmentStorage>
{
    /** Avalon Role */
    public static final String ROLE = ProfileAssignmentStorageExtensionPoint.class.getName();
    
    /* ---------- */
    /* PUBLIC API */
    /* ---------- */
    
    /**
     * Gets the permissions a user has, given some groups and profiles, on an object.
     * @param user The user
     * @param userGroups The groups
     * @param profileIds The ids of the profiles
     * @param object The object
     * @return the permissions a user has, given some groups and profiles on an object.
     */
    public Map<String, AccessResultContext> getPermissions(UserIdentity user, Set<GroupIdentity> userGroups, Set<String> profileIds, Object object)
    {
        // For each profile, create an entry in the result map with its AccessResultContext
        return profileIds.stream().collect(Collectors.toMap(Function.identity(), profileId -> _getPermission(user, userGroups, profileId, object)));
    }
    
    private AccessResultContext _getPermission(UserIdentity user, Set<GroupIdentity> userGroups, String profileId, Object object)
    {
        getLogger().debug("Try to determine permission for user '{}' and groups {} on context {} with the single profile '{}'", user, userGroups, object, profileId);
        
        // Is part of the allowed profiles for anonymous ?
        if (_getAllowedProfilesForAnonymous(object).contains(profileId))
        {
            _logResult(user, userGroups, profileId, object, AccessResult.ANONYMOUS_ALLOWED);
            
            return new AccessResultContext(AccessResult.ANONYMOUS_ALLOWED, null);
        }
        
        // Does the profile respond "the user is denied" ?
        if (_getDeniedUsers(object, profileId).contains(user))
        {
            _logResult(user, userGroups, profileId, object, AccessResult.USER_DENIED);
            return new AccessResultContext(AccessResult.USER_DENIED, null);
        }
        
        // Does the profile respond "the user is allowed" ?
        if (_getAllowedUsers(object, profileId).contains(user))
        {
            _logResult(user, userGroups, profileId, object, AccessResult.USER_ALLOWED);
            return new AccessResultContext(AccessResult.USER_ALLOWED, null);
        }
        
        // Does the profile respond "one of the user groups is denied" ?
        Set<GroupIdentity> deniedGroups = _getDeniedGroups(object, profileId);
        Collection<GroupIdentity> intersection = CollectionUtils.intersection(deniedGroups, userGroups);
        if (!intersection.isEmpty())
        {
            _logResult(user, userGroups, profileId, object, AccessResult.GROUP_DENIED);
            return new AccessResultContext(AccessResult.GROUP_DENIED, new HashSet<>(intersection));
        }
        
        // Does the profile respond "one of the user groups is allowed" ?
        Set<GroupIdentity> allowedGroups = _getAllowedGroups(object, profileId);
        intersection = CollectionUtils.intersection(allowedGroups, userGroups);
        if (!intersection.isEmpty())
        {
            _logResult(user, userGroups, profileId, object, AccessResult.GROUP_ALLOWED);
            return new AccessResultContext(AccessResult.GROUP_ALLOWED, new HashSet<>(intersection));
        }
        
        // Is part of the denied profiles for any connected user ?
        if (_getDeniedProfilesForAnyConnectedUser(object).contains(profileId))
        {
            _logResult(user, userGroups, profileId, object, AccessResult.ANY_CONNECTED_DENIED);
            return new AccessResultContext(AccessResult.ANY_CONNECTED_DENIED, null);
        }
        
        // Is part of the allowed profiles for any connected user ?
        if (_getAllowedProfilesForAnyConnectedUser(object).contains(profileId))
        {
            _logResult(user, userGroups, profileId, object, AccessResult.ANY_CONNECTED_ALLOWED);
           
            return new AccessResultContext(AccessResult.ANY_CONNECTED_ALLOWED, null);
        }
        
        // Is part of the denied profiles for anonymous ?
        if (_getDeniedProfilesForAnonymous(object).contains(profileId))
        {
            _logResult(user, userGroups, profileId, object, AccessResult.ANONYMOUS_DENIED);
            return new AccessResultContext(AccessResult.ANONYMOUS_DENIED, null);
        }
        
        _logResult(user, userGroups, profileId, object, AccessResult.UNKNOWN);
        return new AccessResultContext(AccessResult.UNKNOWN, null);
    }
    
    private void _logResult(UserIdentity user, Set<GroupIdentity> userGroups, String profileId, Object object, AccessResult result)
    {
        getLogger().debug("Access result found is {} for user '{}' and groups {} on context {} with the single profile '{}'", result, user, userGroups, object, profileId);
    }
    
    /**
     * Returns true if the user has a permission on at least one object, given some groups and profiles
     * @param user The user
     * @param userGroups The groups
     * @param profileIds The ids of the profiles
     * @return true if the user has a permission on at least one object, given some groups and profiles
     */
    public boolean hasPermission(UserIdentity user, Set<GroupIdentity> userGroups, Set<String> profileIds)
    {
        getLogger().debug("Try to determine permissions on any context for user '{}' and groups {} with profiles {}", user, userGroups, profileIds);
        
        List<ProfileAssignmentStorage> sortedPas = getExtensionsIds().stream()
                .map(this::getExtension)
                .sorted(Comparator.comparing(ProfileAssignmentStorage::getPriority))
                .collect(Collectors.toList());
        
        for (ProfileAssignmentStorage profileAssignmentStorage : sortedPas)
        {
            if (profileAssignmentStorage.hasPermission(user, userGroups, profileIds))
            {
                getLogger().debug("Find permission on any context for user '{}' and groups {} with profiles {}", user, userGroups, profileIds);
                return true;
            }
        }
        
        getLogger().debug("Find no permission on any context for user '{}' and groups {} with profiles {}", user, userGroups, profileIds);
        return false;
    }
    
    /**
     * Gets the permissions a user has on an object, for every profile in the application.
     * @param user The user
     * @param userGroups The groups
     * @param object The object
     * @return the permissions a user has on an object, for every profile in the application.
     */
    public Map<String, AccessResult> getPermissionsByProfile(UserIdentity user, Set<GroupIdentity> userGroups, Object object)
    {
        getLogger().debug("Try to determine permissions for each profile on context {} for user '{}' and groups {}", object, user, userGroups);
        
        Map<String, AccessResult> result = new HashMap<>();
        
        // Allowed profiles for anonymous
        Set<String> allowedProfilesForAnonymous = _getAllowedProfilesForAnonymous(object);
        _updatePermissionsMap(result, allowedProfilesForAnonymous, AccessResult.ANONYMOUS_ALLOWED);
        
        // Denied profiles for user
        Set<String> deniedProfilesForUser = _getDeniedProfilesForUser(object, user);
        _updatePermissionsMap(result, deniedProfilesForUser, AccessResult.USER_DENIED);
        
        // Allowed profiles for user
        Set<String> allowedProfilesForUser = _getAllowedProfilesForUser(object, user);
        _updatePermissionsMap(result, allowedProfilesForUser, AccessResult.USER_ALLOWED);
        
        // Denied profiles for groups
        for (GroupIdentity group : userGroups)
        {
            Set<String> deniedProfilesForGroup = _getDeniedProfilesForGroup(object, group);
            _updatePermissionsMap(result, deniedProfilesForGroup, AccessResult.GROUP_DENIED);
        }
        
        // Allowed profiles for groups
        for (GroupIdentity group : userGroups)
        {
            Set<String> allowedProfilesForGroup = _getAllowedProfilesForGroup(object, group);
            _updatePermissionsMap(result, allowedProfilesForGroup, AccessResult.GROUP_ALLOWED);
        }
        
        // Denied profiles for any connected user
        Set<String> deniedProfilesForAnyConnectedUser = _getDeniedProfilesForAnyConnectedUser(object);
        _updatePermissionsMap(result, deniedProfilesForAnyConnectedUser, AccessResult.ANY_CONNECTED_DENIED);
        
        // Allowed profiles for any connected user
        Set<String> allowedProfilesForAnyConnectedUser = _getAllowedProfilesForAnyConnectedUser(object);
        _updatePermissionsMap(result, allowedProfilesForAnyConnectedUser, AccessResult.ANY_CONNECTED_ALLOWED);
        
        // Denied profiles for anonymous
        Set<String> deniedProfilesForAnonymous = _getDeniedProfilesForAnonymous(object);
        _updatePermissionsMap(result, deniedProfilesForAnonymous, AccessResult.ANONYMOUS_DENIED);
        
        getLogger().debug("The permissions by profile on context {} for user '{}' and groups {} are : {}", object, user, userGroups, result);
        return result;
    }
    
    private void _updatePermissionsMap(Map<String, AccessResult> permissionsMap, Set<String> keys, AccessResult value)
    {
        for (String key : keys)
        {
            if (!permissionsMap.containsKey(key))
            {
                permissionsMap.put(key, value);
            }
        }
    }
    
    /**
     * Gets the permissions for Anonymous for the given profiles
     * @param profileIds The profiles to get permissions on
     * @param object The object
     * @return the access result for each profile
     */
    public AccessResult getPermissionForAnonymous (Set<String> profileIds, Object object)
    {
        getLogger().debug("Try to determine permission for Anonymous on context {} and profiles {}", object, profileIds);
     
        AccessResult result = AccessResult.UNKNOWN;
        
        for (String profileId : profileIds)
        {
            Set<String> allowedProfiles = _getAllowedProfilesForAnonymous(object);
            Set<String> deniedProfiles = _getDeniedProfilesForAnonymous(object);
            if (deniedProfiles.contains(profileId))
            {
                return AccessResult.ANONYMOUS_DENIED;
            }
            else if (allowedProfiles.contains(profileId))
            {
                result = AccessResult.ANONYMOUS_ALLOWED;
            }
        }
        
        return result;
    }
    
    /**
     * Gets the permissions for Anonymous for the given profiles
     * @param profileIds The profiles to get permissions on
     * @param object The object
     * @return the access result for each profile
     */
    public AccessResult getPermissionForAnyConnectedUser (Set<String> profileIds, Object object)
    {
        getLogger().debug("Try to determine permission for Anonymous on context {} and profiles {}", object, profileIds);
     
        AccessResult result = AccessResult.UNKNOWN;
        
        for (String profileId : profileIds)
        {
            Set<String> allowedProfiles = _getAllowedProfilesForAnyConnectedUser(object);
            Set<String> deniedProfiles = _getDeniedProfilesForAnyConnectedUser(object);
            if (deniedProfiles.contains(profileId))
            {
                return AccessResult.ANONYMOUS_DENIED;
            }
            else if (allowedProfiles.contains(profileId))
            {
                result = AccessResult.ANONYMOUS_ALLOWED;
            }
        }
        
        return result;
    }
    
    /**
     * Gets the permission by user only on an object, according to the given profiles. It does not take account of the groups of the user, etc.
     * @param profileIds The ids of the profiles
     * @param object The object
     * @return the permission by user only on an object, according to the given profiles
     */
    public Map<UserIdentity, AccessResult> getPermissionsByUser(Set<String> profileIds, Object object)
    {
        getLogger().debug("Try to determine permissions by users on context {} and profiles {}", object, profileIds);
        
        Map<UserIdentity, AccessResult> result = new HashMap<>();
        
        for (String profileId : profileIds)
        {
            for (UserIdentity user : _getAllowedUsers(object, profileId))
            {
                if (!result.containsKey(user))
                {
                    // User never found before, he is allowed so far
                    result.put(user, AccessResult.USER_ALLOWED);
                }
            }
            for (UserIdentity user : _getDeniedUsers(object, profileId))
            {
                // Even if already found before, in allowed for example, we override it to set him as denied
                result.put(user, AccessResult.USER_DENIED);
            }
        }
        
        getLogger().debug("The permissions by users on context {} and profiles {} are: ", object, profileIds, result);
        return result;
    }
    
    /**
     * Gets the permission by group only on an object, according to the given profiles.
     * @param profileIds The ids of the profiles
     * @param object The object
     * @return the permission by group only on an object, according to the given profiles
     */
    public Map<GroupIdentity, AccessResult> getPermissionsByGroup(Set<String> profileIds, Object object)
    {
        getLogger().debug("Try to determine permissions by groups on context {} and profiles {}", object, profileIds);
        
        Map<GroupIdentity, AccessResult> result = new HashMap<>();
        
        for (String profileId : profileIds)
        {
            for (GroupIdentity group : _getAllowedGroups(object, profileId))
            {
                if (!result.containsKey(group))
                {
                    // Group never found before, it is allowed so far
                    result.put(group, AccessResult.GROUP_ALLOWED);
                }
            }
            for (GroupIdentity group : _getDeniedGroups(object, profileId))
            {
                // Even if already found before, in allowed for example, we override it to set it as denied
                result.put(group, AccessResult.GROUP_DENIED);
            }
        }
        
        getLogger().debug("The permissions by groups on context {} and profiles {} are: ", object, profileIds, result);
        return result;
    }
    
    
    /* ------------------ */
    /* ANY CONNECTED USER */
    /* ------------------ */
    
    /**
     * Gets the allowed profiles any connected user has on the given object
     * @param object The object
     * @return the allowed profiles any connected user has on the given object
     */
    private Set<String> _getAllowedProfilesForAnyConnectedUser(Object object)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedProfilesForAnyConnectedUser(object))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the denied profiles any connected user has on the given object
     * @param object The object
     * @return the denied profiles any connected user has on the given object
     */
    private Set<String> _getDeniedProfilesForAnyConnectedUser(Object object)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedProfilesForAnyConnectedUser(object))
                .orElse(Collections.EMPTY_SET);
    }
    
    
    /* --------- */
    /* ANONYMOUS */
    /* --------- */
    
    private Set<String> _getAllowedProfilesForAnonymous(Object object)
    {
        Set<String> allowedProfiles = _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedProfilesForAnonymous(object))
                .orElse(Collections.EMPTY_SET);
        
        Set<String> deniedProfiles = _getDeniedProfilesForAnonymous(object);
        
        return new HashSet<>(CollectionUtils.removeAll(allowedProfiles, deniedProfiles));
    }
    
    /**
     * Gets the denied profiles an anonymous user has on the given object
     * @param object The object
     * @return the denied profiles any connected user has on the given object
     */
    private Set<String> _getDeniedProfilesForAnonymous(Object object)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedProfilesForAnonymous(object))
                .orElse(Collections.EMPTY_SET);
    }
    
    
    /* ----- */
    /* USERS */
    /* ----- */
    
    /**
     * Gets the users that have the given allowed profile on the given object
     * @param object The object to test
     * @param profileId The id of the profile
     * @return The allowed users with that profile on that object
     */
    private Set<UserIdentity> _getAllowedUsers(Object object, String profileId)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedUsers(object, profileId))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the allowed profiles for the given user on the given object
     * @param object The object to test
     * @param user The user
     * @return The allowed profiles for the user
     */
    private Set<String> _getAllowedProfilesForUser(Object object, UserIdentity user)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedProfilesForUsers(object).get(user))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the users that have the given denied profile on the given object
     * @param object The object to test 
     * @param profileId The id of the profile
     * @return The allowed users with that profile on that object
     */
    private Set<UserIdentity> _getDeniedUsers(Object object, String profileId)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedUsers(object, profileId))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the denied profiles for the given user on the given object
     * @param object The object to test
     * @param user The user
     * @return The denied profiles for the user
     */
    private Set<String> _getDeniedProfilesForUser(Object object, UserIdentity user)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedProfilesForUsers(object).get(user))
                .orElse(Collections.EMPTY_SET);
    }
    
    
    /* ------ */
    /* GROUPS */
    /* ------ */
    
    /**
     * Gets the groups that have the given allowed profile on the given object
     * @param object The object to test 
     * @param profileId The id of the profile
     * @return The allowed groups with that profile on that object
     */
    private Set<GroupIdentity> _getAllowedGroups(Object object, String profileId)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedGroups(object, profileId))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the allowed profiles for the given group on the given object
     * @param object The object to test
     * @param group The group
     * @return The allowed profiles for the group
     */
    private Set<String> _getAllowedProfilesForGroup(Object object, GroupIdentity group)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedProfilesForGroups(object).get(group))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the groups that have the given denied profile on the given object
     * @param object The object to test 
     * @param profileId The id of the profile
     * @return The denied groups with that profile on that object
     */
    private Set<GroupIdentity> _getDeniedGroups(Object object, String profileId)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedGroups(object, profileId))
                .orElse(Collections.EMPTY_SET);
    }
    
    
    /**
     * Gets the denied profiles for the given group on the given object
     * @param object The object to test
     * @param group The group
     * @return The denied profiles for the group
     */
    private Set<String> _getDeniedProfilesForGroup(Object object, GroupIdentity group)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedProfilesForGroups(object).get(group))
                .orElse(Collections.EMPTY_SET);
    }
    
    /* -------------------------- */
    /* PRIVATE CONVENIENT METHODS */
    /* -------------------------- */
    
    private Optional<ProfileAssignmentStorage> _getFirstProfileAssignmentStorage(Object object)
    {
        return getExtensionsIds().stream()
                .map(this::getExtension)
                .filter(pas -> pas.isSupported(object))
                .sorted(Comparator.comparing(ProfileAssignmentStorage::getPriority))
                .findFirst();
    }
}
