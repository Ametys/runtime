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
package org.ametys.core.right;

import java.util.Map;
import java.util.Set;

/**
 * Abstraction for testing a right associated with a resource and an user from a single source.
 * The implementations of {@link RightsManager} have to prefix all the given contexts by application the rights' context prefix except if null.
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
     * The implementations have to prefix the given context by the application rights' context prefix except if null.
     * This method returns : 
     * <ul>
     * <li>Right.RIGHT_OK if there is at least one combination of profiles/groups
     *     in which the given user has the given right.
     * <li>If there is only negative rights, Right.RIGHT_NOK is returned.
     * <li>If there is no information about the context/right/login,
     *     Right.RIGHT_UNKNOWN is returned and the interpretation is left
     *     to the application.
     * </ul>
     * The implementations of {@link RightsManager} must concat the given context by the context prefix.
     * @param userLogin The user's login. Cannot be null.
     * @param right the name of the right to check. Cannot be null.
     * @param context a String representing the context of the call.<br>
     * It may be a path within a hierarchy tree, a keyword, ...<br>
     * If null the context is not test, only the user and the right
     * are tested.
     * @return RIGHT_OK, RIGHT_NOK or RIGHT_UNKNOWN
     * @throws RightsException if an error occurs.
     */
    public RightResult hasRight(String userLogin, String right, String context) throws RightsException;
    
    /**
     * Get the list of users that have a particular right in a particular context.
     * The implementations have to prefix the given context by the application rights' context prefix except if null.
     * @param right The name of the right to use. Cannot be null.
     * @param context The context to test the right.<br>May be null, in which case the returned Set contains all granted users, whatever the context.
     * @return The list of users granted with that right as a Set of String (login).
     * @throws RightsException if an error occurs.
     */
    public Set<String> getGrantedUsers(String right, String context) throws RightsException;
    
    /**
     * Get the list of users that have at least one right in a particular context.
     * The implementations have to prefix the given context by the application rights' context prefix except if null.
     * @param context The context to test the right.<br>May be null, in which case the returned Set contains all granted users, whatever the context.
     * @return The list of users granted that have at least one right as a Set of String (login).
     * @throws RightsException if an error occurs.
     */
    public Set<String> getGrantedUsers(String context) throws RightsException;
    
    /**
     * Get the list of a user's rights in a particular context.
     * The user's rights and the rights of the user's groups are returned.
     * The implementations have to prefix the given context by the application rights' context prefix except if null.
     * @param login the user's login. Cannot be null.
     * @param context The context to test the right.<br>May be null, in which case the returned Set contains all granted rights, whatever the context.
     * <br>Wilcards may also be used (eg. "ctx/*") to get all granted rights in the given context and all subcontexts.
     * @return The list of rights as a Set of String (id).
     * @throws RightsException if an error occurs.
     */
    public Set<String> getUserRights(String login, String context) throws RightsException;
    
    /**
     * Get the list of a user's rights by context
     * The user's rights and the rights of the user's groups are returned.
     * The implementations have to prefix the given context by the application rights' context prefix except if null.
     * @param login the user's login. Cannot be null.
     * @return The user rights as a Map of String (context), Set (rights' id)
     * @throws RightsException if an error occurs.
     */
    public Map<String, Set<String>> getUserRights (String login) throws RightsException;
}
