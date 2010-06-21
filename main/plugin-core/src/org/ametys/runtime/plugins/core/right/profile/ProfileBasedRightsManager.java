/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.plugins.core.right.profile;

import java.util.Set;

import org.ametys.runtime.right.RightsManager;


/**
 * Common interface for profile based rights manager 
 */
public interface ProfileBasedRightsManager extends RightsManager
{
    /**
     * Add a new Profile
     * @param name the name of the new Profile
     * @return the newly created Profile
     */
    public Profile addProfile(String name);
    
    /**
     * Returns the Profile with the given Id
     * @param id the id oif the wanted Profile
     * @return the Profile with the given Id
     */
    public Profile getProfile(String id);

    /**
     * Returns all known profiles
     * @return all known profiles
     */
    public Set<Profile> getProfiles();

    /**
     * Associates a profile with a group for a given Context
     * 
     * @param groupId the id of the group
     * @param context the current context
     * @param profileId the id of the profile to link with the user
     */
    public void addGroupRight(String groupId, String context, String profileId);

    /**
     * Associates a profile with a user for a given Context
     * 
     * @param login the login of the user
     * @param context the current context
     * @param profileId the id of the profile to link with the user
     */
    public void addUserRight(String login, String context, String profileId);

    /**
     * Removes a profile associated with a user for a given context
     * 
     * @param login the login of the user
     * @param profile the profile to remove
     * @param context the current context
     */
    public void removeUserProfile(String login, String profile, String context);

    /**
     * Removes all profiles associated with a user for a given context
     * 
     * @param login the login of the user
     * @param context the current context
     */
    public void removeUserProfiles(String login, String context);
    
    /**
     * Removes all profiles associated with a group for a given context
     * 
     * @param groupId the if of the group
     * @param profile the profile to remove
     * @param context the current context
     */
    public void removeGroupProfile(String groupId, String profile, String context);
    
    /**
     * Removes all profiles associated with a group for a given context
     * 
     * @param groupId the if of the group
     * @param context the current context
     */
    public void removeGroupProfiles(String groupId, String context);
    
    /**
     * Remove all profiles that concerned a given context.
     * 
     * @param context Context concerned
     */
    public void removeAll(String context);
    
    /**
     * This method has to ensure that the user identified by its login will have all power by assigning a profile containing all rights.
     * @param login The login of the user that will obtain all privilege on the right manager.
     * @param context The context of the right (cannot be null)
     * @param profileName The name of the profile to affect
     */
    public void grantAllPrivileges(String login, String context, String profileName);

}
