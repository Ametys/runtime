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

import java.util.Set;

import org.ametys.core.group.GroupIdentity;
import org.ametys.core.user.UserIdentity;

/**
 * This interface is for modifiable profile assignments storage
 */
public interface ModifiableProfileAssignmentStorage extends ProfileAssignmentStorage
{
    /* --------------------------------------- */
    /* ALLOWED PROFILES FOR ANY CONNECTED USER */
    /* --------------------------------------- */
    
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
     * Removes all the assignments between this profile and users/groups/anonymous/any connected
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
}
