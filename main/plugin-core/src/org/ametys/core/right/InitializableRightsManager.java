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

import org.ametys.core.user.UserIdentity;

/**
 * This right manager support an initialisation that grant all privileges to a user .<br>
 * Extends this implementation if you want that the administrator to be able to initialize your right manager.<br>
 * If your right manager doest not implement this interface, the administrator will have to set the initial rights another way.
 */
public interface InitializableRightsManager extends RightsManager
{
    /**
     * This method has to ensure that the user identified by its login will have all power.
     * @param user The user that will obtain all privilege on the right manager.
     * @param context The context of the right (cannot be null)
     * @throws RightsException if an error occurs.
     */
    public void grantAllPrivileges(UserIdentity user, String context) throws RightsException;
}
