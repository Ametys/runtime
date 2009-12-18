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
package org.ametys.runtime.user;

import org.ametys.runtime.authentication.Credentials;

/**
 * Abstraction for getting users list and verify the presence of a particular
 * user and authenticating users.
 */
public interface CredentialsAwareUsersManager extends UsersManager
{
    /**
     * Authenticate a user with its credentials
     * @param credentials the credentials of the user. Cannot be null.
     * @return true if the user is authenticated, false otherwise.
     */
    public boolean checkCredentials(Credentials credentials);
}
