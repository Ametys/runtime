/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
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

}
