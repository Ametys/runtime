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

import org.ametys.core.right.RightsManager.RightResult;


/**
 * Common interface for hierarchical rights manager 
 */
public interface HierarchicalRightsManager
{
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
     * @param contextPrefix a String representing the context prefix of the call.<br>
     * It may be a path within a hierarchy tree, a keyword, ...<br>
     * If null the context is not test, only the user and the right
     * are tested.
     * @return RIGHT_OK, RIGHT_NOK or RIGHT_UNKNOWN
     * @throws RightsException if an error occurs.
     */
    public RightResult hasRightOnContextPrefix(String userLogin, String right, String contextPrefix) throws RightsException;
}
