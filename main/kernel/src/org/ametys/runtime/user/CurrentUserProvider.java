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
package org.ametys.runtime.user;


/**
 * Component which:
 * <ul>
 *  <li>test if the current logged in user is the super user.
 *  <li>provides the login of the current user.
 * </ul>
 */
public interface CurrentUserProvider
{
    /** Avalon role. */
    public static final String ROLE = CurrentUserProvider.class.getName();

    /**
     * Determine if current user is the super user.
     * @return <code>true</code> if the super user is logged in,
     *         <code>false</code> otherwise.
     */
    boolean isSuperUser();
    
    /**
     * Provides the login of the current logged user.
     * @return the login of current user or <code>null</code> if there is no logged user.
     */
    String getUser();
}
