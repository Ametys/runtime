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
package org.ametys.runtime.right;

import java.util.Set;

/**
 * Abstraction for testing a right associated with a resource and an user from a single source.
 */
public interface RightsManager
{
    /** For avalon service manager */
    public static final String ROLE = RightsManager.class.getName();
    
    /**
     * Enumeration of all possible values returned by hasRight(user, right, context)
     */
    public enum RightResult
    {
        /**
         * Indicates that a given user has the required right.
         */
        RIGHT_OK,
        
        /**
         * Indicates that a given user does NOT have the required right.
         */
        RIGHT_NOK,
        
        /**
         * Indicates that the system knows nothing about the fact that a given user has a right or not.
         */
        RIGHT_UNKNOWN;
    }
    
    /**
     * Check a permission for a user, in a given context.<br>
     * This method returns : 
     * <ul>
     * <li>Right.RIGHT_OK if there is at least one combination of profiles/groups
     *     in which the given user has the given right.
     * <li>If there is only negative rights, Right.RIGHT_NOK is returned.
     * <li>If there is no information about the context/right/login,
     *     Right.RIGHT_UNKNOWN is returned and the interpretation is left
     *     to the application.
     * </ul>
     * @param userLogin The user's login. Cannot be null.
     * @param right the name of the right to check. Cannot be null.
     * @param context a String representing the context of the call.<br>
     * It may be a path within a hierarchy tree, a keyword, ...<br>
     * If null the context is not test, only the user and the right
     * are tested.
     * @return RIGHT_OK, RIGHT_NOK or RIGHT_UNKNOWN
     */
    public RightResult hasRight(String userLogin, String right, String context);
    
    /**
     * Get the list of users that have a particular right in a particular context.
     * @param right The name of the right to use. Cannot be null.
     * @param context The context to test the right.<br>May be null, in which case the returned Set contains all granted users, whatever the context.
     * @return The list of users granted with that right as a Set of String (login).
     */
    public Set<String> getGrantedUsers(String right, String context);
    
    /**
     * Get the list of a user's rights in a particular context.
     * The user's rights and the rights of the user's groups are returned.
     * @param login the user's login. Cannot be null.
     * @param context The context to test the right.<br>May be null, in which case the returned Set contains all granted rights, whatever the context.
     * <br>Wilcards may also be used (eg. "ctx/*") to get all granted rights in the given context and all subcontexts.
     * @return The list of rights as a Set of String (id).
     */
    public Set<String> getUserRights(String login, String context);
}
