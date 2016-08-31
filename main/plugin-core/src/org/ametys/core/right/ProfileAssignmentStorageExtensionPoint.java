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
        if (getAllowedProfilesForAnonymous(object).contains(profileId))
        {
            _logResult(user, userGroups, profileId, object, AccessResult.ANONYMOUS_ALLOWED);
            
            return new AccessResultContext(AccessResult.ANONYMOUS_ALLOWED, null);
        }
        
        if (getDeniedProfilesForUser(object, user).contains(profileId))
        {
            _logResult(user, userGroups, profileId, object, AccessResult.USER_DENIED);
            return new AccessResultContext(AccessResult.USER_DENIED, null);
        }
        
        // Does the profile respond "the user is allowed" ?
        if (getAllowedProfilesForUser(object, user).contains(profileId))
        {
            _logResult(user, userGroups, profileId, object, AccessResult.USER_ALLOWED);
            return new AccessResultContext(AccessResult.USER_ALLOWED, null);
        }
        
        // Does the profile respond "one of the user groups is denied" ?
        Set<GroupIdentity> deniedGroups = new HashSet<>();
        for (GroupIdentity userGroup : userGroups)
        {
            if (getDeniedProfilesForGroup(object, userGroup).contains(profileId))
            {
                deniedGroups.add(userGroup);
            }
        }
        if (deniedGroups.size() > 0)
        {
            _logResult(user, userGroups, profileId, object, AccessResult.GROUP_DENIED);
            return new AccessResultContext(AccessResult.GROUP_DENIED, deniedGroups);
        }
        
        Set<GroupIdentity> allowedGroups = new HashSet<>();
        for (GroupIdentity userGroup : userGroups)
        {
            if (getAllowedProfilesForGroup(object, userGroup).contains(profileId))
            {
                allowedGroups.add(userGroup);
            }
        }
        if (allowedGroups.size() > 0)
        {
            _logResult(user, userGroups, profileId, object, AccessResult.GROUP_ALLOWED);
            return new AccessResultContext(AccessResult.GROUP_ALLOWED, allowedGroups);
        }
        
        // Is part of the denied profiles for any connected user ?
        if (getDeniedProfilesForAnyConnectedUser(object).contains(profileId))
        {
            _logResult(user, userGroups, profileId, object, AccessResult.ANY_CONNECTED_DENIED);
            return new AccessResultContext(AccessResult.ANY_CONNECTED_DENIED, null);
        }
        
        // Is part of the allowed profiles for any connected user ?
        if (getAllowedProfilesForAnyConnectedUser(object).contains(profileId))
        {
            _logResult(user, userGroups, profileId, object, AccessResult.ANY_CONNECTED_ALLOWED);
           
            return new AccessResultContext(AccessResult.ANY_CONNECTED_ALLOWED, null);
        }
        
        // Is part of the denied profiles for anonymous ?
        if (getDeniedProfilesForAnonymous(object).contains(profileId))
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
        Set<String> allowedProfilesForAnonymous = getAllowedProfilesForAnonymous(object);
        _updatePermissionsMap(result, allowedProfilesForAnonymous, AccessResult.ANONYMOUS_ALLOWED);
        
        // Denied profiles for user
        Set<String> deniedProfilesForUser = getDeniedProfilesForUser(object, user);
        _updatePermissionsMap(result, deniedProfilesForUser, AccessResult.USER_DENIED);
        
        // Allowed profiles for user
        Set<String> allowedProfilesForUser = getAllowedProfilesForUser(object, user);
        _updatePermissionsMap(result, allowedProfilesForUser, AccessResult.USER_ALLOWED);
        
        // Denied profiles for groups
        for (GroupIdentity group : userGroups)
        {
            Set<String> deniedProfilesForGroup = getDeniedProfilesForGroup(object, group);
            _updatePermissionsMap(result, deniedProfilesForGroup, AccessResult.GROUP_DENIED);
        }
        
        // Allowed profiles for groups
        for (GroupIdentity group : userGroups)
        {
            Set<String> allowedProfilesForGroup = getAllowedProfilesForGroup(object, group);
            _updatePermissionsMap(result, allowedProfilesForGroup, AccessResult.GROUP_ALLOWED);
        }
        
        // Denied profiles for any connected user
        Set<String> deniedProfilesForAnyConnectedUser = getDeniedProfilesForAnyConnectedUser(object);
        _updatePermissionsMap(result, deniedProfilesForAnyConnectedUser, AccessResult.ANY_CONNECTED_DENIED);
        
        // Allowed profiles for any connected user
        Set<String> allowedProfilesForAnyConnectedUser = getAllowedProfilesForAnyConnectedUser(object);
        _updatePermissionsMap(result, allowedProfilesForAnyConnectedUser, AccessResult.ANY_CONNECTED_ALLOWED);
        
        // Denied profiles for anonymous
        Set<String> deniedProfilesForAnonymous = getDeniedProfilesForAnonymous(object);
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
            Set<String> allowedProfiles = getAllowedProfilesForAnonymous(object);
            Set<String> deniedProfiles = getDeniedProfilesForAnonymous(object);
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
            Set<String> allowedProfiles = getAllowedProfilesForAnyConnectedUser(object);
            Set<String> deniedProfiles = getDeniedProfilesForAnyConnectedUser(object);
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
            for (UserIdentity user : getAllowedUsers(object, profileId))
            {
                if (!result.containsKey(user))
                {
                    // User never found before, he is allowed so far
                    result.put(user, AccessResult.USER_ALLOWED);
                }
            }
            for (UserIdentity user : getDeniedUsers(object, profileId))
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
            for (GroupIdentity group : getAllowedGroups(object, profileId))
            {
                if (!result.containsKey(group))
                {
                    // Group never found before, it is allowed so far
                    result.put(group, AccessResult.GROUP_ALLOWED);
                }
            }
            for (GroupIdentity group : getDeniedGroups(object, profileId))
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
     * Gets the allowed profiles for any connected user on the given object
     * @param context The object context
     * @return the allowed profiles for any connected user on the given object
     */
    public Set<String> getAllowedProfilesForAnyConnectedUser(Object context)
    {
        return _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getAllowedProfilesForAnyConnectedUser(context))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the denied profiles for any connected user on the given object
     * @param context The object context
     * @return the denied profiles for any connected user on the given object
     */
    public Set<String> getDeniedProfilesForAnyConnectedUser(Object context)
    {
        return _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getDeniedProfilesForAnyConnectedUser(context))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Adds allowed profile any connected user has on the given object
     * @param context The object context
     * @param profileId The profile to add
     */
    public void allowProfileToAnyConnectedUser(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addAllowedProfilesForAnyConnectedUser(context, Collections.singleton(profileId)));
    }
    
    /**
     * Adds denied profile any connected user has on the given object
     * @param profileId The profile to add
     * @param context The object context
     */
    public void denyProfileToAnyConnectedUser(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addDeniedProfilesForAnyConnectedUser(context, Collections.singleton(profileId)));
    }
    
    /**
     * Removes allowed profile any connected user has on the given object
     * @param profileId The profile to remove
     * @param context The object context
     */
    public void removeAllowedProfileFromAnyConnectedUser(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeAllowedProfilesForAnyConnectedUser(context, Collections.singleton(profileId)));
    }
    
    /**
     * Removes denied profile any connected user has on the given object
     * @param context The object context
     * @param profileId The profile to remove
     */
    public void removeDeniedProfileFromAnyConnectedUser(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeDeniedProfilesForAnyConnectedUser(context, Collections.singleton(profileId)));
    }
    
    /* --------- */
    /* ANONYMOUS */
    /* --------- */
    /**
     * Gets the allowed profiles for Anonymous user on the given object
     * @param context The object context
     * @return the allowed profiles for Anonymous user on the given object
     */
    public Set<String> getAllowedProfilesForAnonymous(Object context)
    {
        Set<String> allowedProfiles = _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getAllowedProfilesForAnonymous(context))
                .orElse(Collections.EMPTY_SET);
        
        Set<String> deniedProfiles = getDeniedProfilesForAnonymous(context);
        
        return new HashSet<>(CollectionUtils.removeAll(allowedProfiles, deniedProfiles));
    }
    
    /**
     * Gets the denied profiles for Anonymous user on the given object
     * @param context The object context
     * @return the denied profiles for Anonymous user on the given object
     */
    public Set<String> getDeniedProfilesForAnonymous(Object context)
    {
        return _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getDeniedProfilesForAnonymous(context))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Adds allowed profile an anonymous user has on the given object
     * @param profileId The profile to add
     * @param context The object context
     */
    public void allowProfileToAnonymous(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addAllowedProfilesForAnonymous(context, Collections.singleton(profileId)));
    }
    
    /**
     * Adds denied profile an anonymous user has on the given object
     * @param profileId The profile to add
     * @param context The object context
     */
    public void denyProfileToAnonymous(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addDeniedProfilesForAnonymous(context, Collections.singleton(profileId)));
    }
    
    /**
     * Removes allowed profile an anonymous user has on the given object
     * @param profileId The profile to remove
     * @param context The object context
     */
    public void removeAllowedProfileFromAnonymous(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeAllowedProfilesForAnonymous(context, Collections.singleton(profileId)));
    }
    
    /**
     * Removes denied profile an anonymous user has on the given object
     * @param context The object context
     * @param profileId The profile to remove
     */
    public void removeDeniedProfileFromAnonymous(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeDeniedProfilesForAnonymous(context, Collections.singleton(profileId)));
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
    public Set<UserIdentity> getAllowedUsers(Object object, String profileId)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedUsers(object, profileId))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the users that have the given denied profile on the given object
     * @param object The object to test 
     * @param profileId The id of the profile
     * @return The allowed users with that profile on that object
     */
    public Set<UserIdentity> getDeniedUsers(Object object, String profileId)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedUsers(object, profileId))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the allowed profiles for the given user on the given object
     * @param object The object to test
     * @param user The user
     * @return The allowed profiles for the user
     */
    public Set<String> getAllowedProfilesForUser(Object object, UserIdentity user)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedProfilesForUser(user, object))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the denied profiles for the given user on the given object
     * @param object The object to test
     * @param user The user
     * @return The denied profiles for the user
     */
    public Set<String> getDeniedProfilesForUser(Object object, UserIdentity user)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedProfilesForUser(user, object))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the allowed profiles by users on the given object
     * @param object The context object
     * @return The allowed profiles by users
     */
    public Map<UserIdentity, Set<String>> getAllowedProfilesForUsers(Object object)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedProfilesForUsers(object))
                .orElse(Collections.EMPTY_MAP);
    }
    
    /**
     * Gets the denied profiles by users on the given object
     * @param object The context object
     * @return The denied profiles by users
     */
    public Map<UserIdentity, Set<String>> getDeniedProfilesForUsers(Object object)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedProfilesForUsers(object))
                .orElse(Collections.EMPTY_MAP);
    }
    
    /**
     * Allows a user to a profile on a given object
     * @param user The user to add
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void allowProfileToUser(UserIdentity user, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addAllowedUsers(Collections.singleton(user), context, profileId));
    }
    
    /**
     * Denies a user to a profile on a given object
     * @param user The user to add
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void denyProfileToUser(UserIdentity user, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addDeniedUsers(Collections.singleton(user), context, profileId));
    }
    
    /**
     * Removes the association between a user and an allowed profile on a given object
     * @param user The user to remove
     * @param context The object context
     * @param profileId The id of the profile
     */
    public void removeAllowedProfileFromUser(UserIdentity user, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeAllowedUsers(Collections.singleton(user), context, profileId));
    }
    
    /**
     * Removes the association between a user and a denied profile on a given object
     * @param user The user to remove
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void removeDeniedProfileFromUser(UserIdentity user, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeDeniedUsers(Collections.singleton(user), context, profileId));
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
    public Set<GroupIdentity> getAllowedGroups(Object object, String profileId)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedGroups(object, profileId))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the groups that have the given denied profile on the given object
     * @param object The object to test 
     * @param profileId The id of the profile
     * @return The denied groups with that profile on that object
     */
    public Set<GroupIdentity> getDeniedGroups(Object object, String profileId)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedGroups(object, profileId))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the allowed profiles for the given group on the given object
     * @param object The object to test
     * @param group The group
     * @return The allowed profiles for the group
     */
    public Set<String> getAllowedProfilesForGroup(Object object, GroupIdentity group)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedProfilesForGroups(object).get(group))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the denied profiles for the given group on the given object
     * @param object The object to test
     * @param group The group
     * @return The denied profiles for the group
     */
    public Set<String> getDeniedProfilesForGroup(Object object, GroupIdentity group)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedProfilesForGroups(object).get(group))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the allowed profiles by groups on the given object
     * @param object The context object
     * @return The allowed profiles by groups
     */
    public Map<GroupIdentity, Set<String>> getAllowedProfilesForGroups(Object object)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedProfilesForGroups(object))
                .orElse(Collections.EMPTY_MAP);
    }
    
    /**
     * Gets the denied profiles by groups on the given object
     * @param context The object context to test 
     * @return The denied profiles by groups
     */
    public Map<GroupIdentity, Set<String>> getDeniedProfilesForGroups(Object context)
    {
        return _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getDeniedProfilesForGroups(context))
                .orElse(Collections.EMPTY_MAP);
    }
    
    /**
     * Allows a group to a profile on a given object
     * @param group The group to add
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void allowProfileToGroup(GroupIdentity group, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addAllowedGroups(Collections.singleton(group), context, profileId));
    }
    
    /**
     * Denies a group to a profile on a given object
     * @param group The group to add
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void denyProfileToGroup(GroupIdentity group, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addDeniedGroups(Collections.singleton(group), context, profileId));
    }
    
    /**
     * Removes the association between a group and an allowed profile on a given object
     * @param group The group to remove
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void removeAllowedProfileFromGroup(GroupIdentity group, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeAllowedGroups(Collections.singleton(group), context, profileId));
    }
    
    /**
     * Removes the association between a group and a denied profile on a given object
     * @param group The group to remove
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void removeDeniedProfileFromGroup(GroupIdentity group, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeDeniedGroups(Collections.singleton(group), context, profileId));
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
