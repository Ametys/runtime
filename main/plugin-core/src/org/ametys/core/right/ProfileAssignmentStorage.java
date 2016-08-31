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

import java.util.Map;
import java.util.Set;

import org.ametys.core.group.GroupIdentity;
import org.ametys.core.user.UserIdentity;

/**
 * This interface is for storing the profile assignments of users/groups on objects.
 */
public interface ProfileAssignmentStorage
{
    /** Minimum priority. */
    public static final int MIN_PRIORITY = Integer.MAX_VALUE;
    /** Maximum priority. */
    public static final int MAX_PRIORITY = 0;
    
    /* -------------- */
    /* HAS PERMISSION */
    /* -------------- */
    
    /**
     * Returns true if the user has a permission on at least one object supported by this profile asssignment storage, given some groups and profiles
     * @param user The user
     * @param userGroups The groups
     * @param profileIds The ids of the profiles
     * @return true if the user has a permission on at least one object supported by this profile asssignment storage, given some groups and profiles
     */
    public boolean hasPermission(UserIdentity user, Set<GroupIdentity> userGroups, Set<String> profileIds);
    
    /* --------------------------------------- */
    /* ALLOWED PROFILES FOR ANY CONNECTED USER */
    /* --------------------------------------- */
    
    /**
     * Gets the allowed profiles any connected user has on the given object
     * @param object The object
     * @return the allowed profiles any connected user has on the given object
     */
    public Set<String> getAllowedProfilesForAnyConnectedUser(Object object);
    
    /**
     * Returns true if any connected user is allowed with the given profile
     * @param object The object
     * @param profileId The id of the profile
     * @return true if any connected user is allowed with the given profile
     */
    public boolean isAnyConnectedUserAllowed(Object object, String profileId);
    
    /**
     * Adds allowed profiles any connected user has on the given object
     * @param object The object
     * @param profileIds The profiles to add
     */
    public void addAllowedProfilesForAnyConnectedUser(Object object, Set<String> profileIds);
    
    /**
     * Removes allowed profiles any connected user has on the given object
     * @param object The object
     * @param profileIds The profiles to remove
     */
    public void removeAllowedProfilesForAnyConnectedUser(Object object, Set<String> profileIds);
    
    
    /* --------------------------------------- */
    /* DENIED PROFILES FOR ANY CONNECTED USER */
    /* --------------------------------------- */
    
    /**
     * Gets the denied profiles any connected user has on the given object
     * @param object The object
     * @return the denied profiles any connected user has on the given object
     */
    public Set<String> getDeniedProfilesForAnyConnectedUser(Object object);
    
    /**
     * Returns true if any connected user is denied with the given profile
     * @param object The object
     * @param profileId The id of the profile
     * @return true if any connected user is denied with the given profile
     */
    public boolean isAnyConnectedUserDenied(Object object, String profileId);
    
    /**
     * Adds denied profiles any connected user has on the given object
     * @param object The object
     * @param profileIds The profiles to add
     */
    public void addDeniedProfilesForAnyConnectedUser(Object object, Set<String> profileIds);
    
    /**
     * Removes denied profiles any connected user has on the given object
     * @param object The object
     * @param profileIds The profiles to remove
     */
    public void removeDeniedProfilesForAnyConnectedUser(Object object, Set<String> profileIds);
    
    
    /* ------------------------------ */
    /* ALLOWED PROFILES FOR ANONYMOUS */
    /* ------------------------------ */
    
    /**
     * Gets the allowed profiles an anonymous user has on the given object
     * @param object The object
     * @return the allowed profiles an anonymous user has on the given object
     */
    public Set<String> getAllowedProfilesForAnonymous(Object object);
    
    /**
     * Returns true if anonymous is allowed with the given profile
     * @param object The object
     * @param profileId The id of the profile
     * @return true if anonymous is allowed with the given profile
     */
    public boolean isAnonymousAllowed(Object object, String profileId);
    
    /**
     * Adds allowed profiles an anonymous user has on the given object
     * @param object The object
     * @param profileIds The profiles to add
     */
    public void addAllowedProfilesForAnonymous(Object object, Set<String> profileIds);
    
    /**
     * Removes allowed profiles an anonymous user has on the given object
     * @param object The object
     * @param profileIds The profiles to remove
     */
    public void removeAllowedProfilesForAnonymous(Object object, Set<String> profileIds);
    
    
    /* --------------------------------------- */
    /* DENIED PROFILES FOR ANONYMOUS */
    /* --------------------------------------- */
    
    /**
     * Gets the denied profiles an anonymous user has on the given object
     * @param object The object
     * @return the denied profiles an anonymous user has on the given object
     */
    public Set<String> getDeniedProfilesForAnonymous(Object object);
    
    /**
     * Returns true if anonymous is denied with the given profile
     * @param object The object
     * @param profileId The id of the profile
     * @return true if anonymous is denied with the given profile
     */
    public boolean isAnonymousDenied(Object object, String profileId);
    
    /**
     * Adds denied profiles an anonymous user has on the given object
     * @param object The object
     * @param profileIds The profiles to add
     */
    public void addDeniedProfilesForAnonymous(Object object, Set<String> profileIds);
    
    /**
     * Removes denied profiles an anonymous user has on the given object
     * @param object The object
     * @param profileIds The profiles to remove
     */
    public void removeDeniedProfilesForAnonymous(Object object, Set<String> profileIds);
    
    
    /* --------------------------- */
    /* MANAGEMENT OF ALLOWED USERS */
    /* --------------------------- */
    
    /**
     * Get the allowed profiles assigned on the given object for the given user
     * @param user The user
     * @param object The object to test 
     * @return The allowed profiles
     */
    public Set<String> getAllowedProfilesForUser(UserIdentity user, Object object);
    
    /**
     * Gets the users that have allowed profiles assigned on the given object
     * @param object The object to test 
     * @return The map of allowed users (keys) with their assigned profiles (values)
     */
    public Map<UserIdentity, Set<String>> getAllowedProfilesForUsers(Object object);
    
    /**
     * Gets the users that have the given allowed profile on the given object
     * @param object The object to test 
     * @param profileId The id of the profile
     * @return The allowed users with that profile on that object
     */
    public Set<UserIdentity> getAllowedUsers(Object object, String profileId);
    
    /**
     * Associates some users with an allowed profile on a given object
     * @param users The users to add
     * @param object The object
     * @param profileId The id of the profile
     */
    public void addAllowedUsers(Set<UserIdentity> users, Object object, String profileId);
    
    /**
     * Removes the association between some users and an allowed profile on a given object
     * @param users The users to remove
     * @param object The object
     * @param profileId The id of the profile
     */
    public void removeAllowedUsers(Set<UserIdentity> users, Object object, String profileId);
    
    /**
     * Removes the association between some users and all allowed profiles on a given object
     * @param users The users to remove
     * @param object The object
     */
    public void removeAllowedUsers(Set<UserIdentity> users, Object object);
    
    
    /* ---------------------------- */
    /* MANAGEMENT OF ALLOWED GROUPS */
    /* ---------------------------- */
    
    /**
     * Gets the groups that have allowed profiles assigned on the given object
     * @param object The object to test 
     * @return The map of allowed groups (keys) with their assigned profiles (values)
     */
    public Map<GroupIdentity, Set<String>> getAllowedProfilesForGroups(Object object);
    
    /**
     * Gets the groups that have the given allowed profile on the given object
     * @param object The object to test 
     * @param profileId The id of the profile
     * @return The allowed groups with that profile on that object
     */
    public Set<GroupIdentity> getAllowedGroups(Object object, String profileId);
    
    /**
     * Associates some groups with an allowed profile on a given object
     * @param groups The groups to add
     * @param object The object
     * @param profileId The id of the profile
     */
    public void addAllowedGroups(Set<GroupIdentity> groups, Object object, String profileId);
    
    /**
     * Removes the association between some groups and an allowed profile on a given object
     * @param groups The groups to remove
     * @param object The object
     * @param profileId The id of the profile
     */
    public void removeAllowedGroups(Set<GroupIdentity> groups, Object object, String profileId);
    
    /**
     * Removes the association between some groups and all allowed profiles on a given object
     * @param groups The groups to remove
     * @param object The object
     */
    public void removeAllowedGroups(Set<GroupIdentity> groups, Object object);
    
    
    /* ---------------------------- */
    /* MANAGEMENT OF DENIED USERS */
    /* ---------------------------- */
    
    /**
     * Get the denied profiles assigned on the given object for the given user
     * @param user The user
     * @param object The object to test 
     * @return The denied profiles
     */
    public Set<String> getDeniedProfilesForUser(UserIdentity user, Object object);
    
    /**
     * Gets the users that have denied profiles assigned on the given object
     * @param object The object to test 
     * @return The map of denied users (keys) with their assigned profiles (values)
     */
    public Map<UserIdentity, Set<String>> getDeniedProfilesForUsers(Object object);
    
    /**
     * Gets the users that have the given denied profile on the given object
     * @param object The object to test 
     * @param profileId The id of the profile
     * @return The denied users with that profile on that object
     */
    public Set<UserIdentity> getDeniedUsers(Object object, String profileId);
    
    /**
     * Associates some users with a denied profile on a given object
     * @param users The users to add
     * @param object The object
     * @param profileId The id of the profile
     */
    public void addDeniedUsers(Set<UserIdentity> users, Object object, String profileId);
    
    /**
     * Removes the association between some users and an denied profile on a given object
     * @param users The users to remove
     * @param object The object
     * @param profileId The id of the profile
     */
    public void removeDeniedUsers(Set<UserIdentity> users, Object object, String profileId);
    
    /**
     * Removes the association between some users and all denied profiles on a given object
     * @param users The users to remove
     * @param object The object
     */
    public void removeDeniedUsers(Set<UserIdentity> users, Object object);
    
    
    /* --------------------------- */
    /* MANAGEMENT OF DENIED GROUPS */
    /* --------------------------- */
    
    /**
     * Gets the groups that have denied profiles assigned on the given object
     * @param object The object to test 
     * @return The map of denied groups (keys) with their assigned profiles (values)
     */
    public Map<GroupIdentity, Set<String>> getDeniedProfilesForGroups(Object object);
    
    /**
     * Gets the groups that have the given denied profile on the given object
     * @param object The object to test 
     * @param profileId The id of the profile
     * @return The denied groups with that profile on that object
     */
    public Set<GroupIdentity> getDeniedGroups(Object object, String profileId);
    
    /**
     * Associates some groups with a denied profile on a given object
     * @param groups The groups to add
     * @param object The object
     * @param profileId The id of the profile
     */
    public void addDeniedGroups(Set<GroupIdentity> groups, Object object, String profileId);
    
    /**
     * Removes the association between some groups and a denied profile on a given object
     * @param groups The groups to remove
     * @param object The object
     * @param profileId The id of the profile
     */
    public void removeDeniedGroups(Set<GroupIdentity> groups, Object object, String profileId);
    
    /**
     * Removes the association between some groups and all denied profiles on a given object
     * @param groups The groups to remove
     * @param object The object
     */
    public void removeDeniedGroups(Set<GroupIdentity> groups, Object object);
    
    
    /* ------ */
    /* REMOVE */
    /* ------ */
    
    /**
     * Removes all the assignments betwwen this profile and users/groups/anonymous/any connected
     * @param profileId The profile to remove
     */
    public void removeProfile(String profileId);
    
    /**
     * Removes all the assignments involving this user
     * @param user The user
     */
    public void removeUser(UserIdentity user);
    
    /**
     * Removes all the assignments involving this group
     * @param group The group
     */
    public void removeGroup(GroupIdentity group);
    
    
    /* ------------------------------ */
    /* SUPPORT OF OBJECT AND PRIORITY */
    /* ------------------------------ */
    
    /**
     * Returns true if this profile storage supports the given object, 
     * i.e. if it is able to retrieve the allowed users/groups on that object
     * @param object The object to test
     * @return true if this profile storage supports the given object
     */
    public boolean isSupported(Object object);
    
    /**
     * Returns the priority of this profile storage
     * The {@link ProfileAssignmentStorageExtensionPoint} will take the profile storage
     * which supports the object with the highest priority to return the allowed/denied users/groups
     * @return the priority of this profile storage
     */
    public int getPriority();
}
