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
package org.ametys.core.user;

/**
 * Listener for users
 */
public interface UserListener
{
    /**
     * When an user is removed
     * @param user the user
     */
    public void userRemoved(UserIdentity user);
    
    /**
     * When an user is added
     * @param user the user
     */
    public void userAdded(UserIdentity user);
    
    /**
     * When an user is updated
     * @param user the user
     */
    public void userUpdated(UserIdentity user);
}
