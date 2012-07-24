/*
 *  Copyright 2012 Anyware Services
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

import org.ametys.runtime.right.RightsException;
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
     * @throws RightsException if an error occurs.
     */
    public Profile addProfile(String name) throws RightsException;
    
    /**
     * Add a new Profile
     * @param name the name of the new Profile
     * @param context the context. Can be null.
     * @return the newly created Profile
     * @throws RightsException if an error occurs.
     */
    public Profile addProfile(String name, String context) throws RightsException;
    
    /**
     * Returns the Profile with the given Id
     * @param id the id oif the wanted Profile
     * @return the Profile with the given Id
     * @throws RightsException if an error occurs.
     */
    public Profile getProfile(String id) throws RightsException;

    /**
     * Returns all known profiles
     * @return all known profiles
     * @throws RightsException if an error occurs.
     */
    public Set<Profile> getAllProfiles() throws RightsException;
    
    /**
     * Returns profiles with no context
     * @return profiles with no context
     * @throws RightsException if an error occurs.
     */
    public Set<Profile> getProfiles() throws RightsException;
    
    /**
     * Returns profiles of a given context
     * @param context The context. Can be null. If null, the profiles with no context are returned.
     * @return profiles of a given context 
     * @throws RightsException if an error occurs.
     */
    public Set<Profile> getProfiles(String context) throws RightsException;

    /**
     * Associates a profile with a group for a given Context
     * 
     * @param groupId the id of the group
     * @param context the current context
     * @param profileId the id of the profile to link with the user
     * @throws RightsException if an error occurs.
     */
    public void addGroupRight(String groupId, String context, String profileId) throws RightsException;

    /**
     * Associates a profile with a user for a given Context
     * 
     * @param login the login of the user
     * @param context the current context
     * @param profileId the id of the profile to link with the user
     * @throws RightsException if an error occurs.
     */
    public void addUserRight(String login, String context, String profileId) throws RightsException;

    /**
     * Removes a profile associated with a user for a given context
     * 
     * @param login the login of the user
     * @param profile the profile to remove
     * @param context the current context
     * @throws RightsException if an error occurs.
     */
    public void removeUserProfile(String login, String profile, String context) throws RightsException;

    /**
     * Removes all profiles associated with a user for a given context
     * 
     * @param login the login of the user
     * @param context the current context
     * @throws RightsException if an error occurs.
     */
    public void removeUserProfiles(String login, String context) throws RightsException;
    
    /**
     * Removes all profiles associated with a group for a given context
     * 
     * @param groupId the if of the group
     * @param profile the profile to remove
     * @param context the current context
     * @throws RightsException if an error occurs.
     */
    public void removeGroupProfile(String groupId, String profile, String context) throws RightsException;
    
    /**
     * Removes all profiles associated with a group for a given context
     * 
     * @param groupId the if of the group
     * @param context the current context
     * @throws RightsException if an error occurs.
     */
    public void removeGroupProfiles(String groupId, String context) throws RightsException;
    
    /**
     * Move all profiles on a given context to a new context.
     * 
     * @param oldContext the old context.
     * @param newContext the new context.
     * @throws RightsException if an error occurs.
     */
    public void updateContext(String oldContext, String newContext) throws RightsException;
    
    /**
     * Remove all profiles that concerned a given context.
     * 
     * @param context Context concerned
     * @throws RightsException if an error occurs.
     */
    public void removeAll(String context) throws RightsException;
    
    /**
     * This method has to ensure that the user identified by its login will have all power by assigning a profile containing all rights.
     * @param login The login of the user that will obtain all privilege on the right manager.
     * @param context The context of the right (cannot be null)
     * @param profileName The name of the profile to affect
     * @throws RightsException if an error occurs.
     */
    public void grantAllPrivileges(String login, String context, String profileName) throws RightsException;

}
