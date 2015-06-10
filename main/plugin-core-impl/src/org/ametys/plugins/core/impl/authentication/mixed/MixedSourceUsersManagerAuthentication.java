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
package org.ametys.plugins.core.impl.authentication.mixed;

import org.ametys.core.authentication.Credentials;
import org.ametys.core.user.CredentialsAwareUsersManager;
import org.ametys.plugins.core.impl.authentication.UsersManagerAuthentication;

/**
 * Authenticate Credentials with the current UsersManager (which must implement CredentialsAwareUsersManager).
 * Replace standard UsersManagerAuthentication with this one when using CASBasicCredentialsProvider.
 */
public class MixedSourceUsersManagerAuthentication extends UsersManagerAuthentication
{
    @Override
    public boolean login(Credentials credentials)
    {
        // Check that the UsersManager knows how to authenticate credentials.
        if (_users instanceof CredentialsAwareUsersManager)
        {
            // If the credentials come from a SSO (like CAS), they are already authenticated : grant access.
            if (credentials instanceof MixedSourceCredentials)
            {
                if (((MixedSourceCredentials) credentials).isAuthenticated())
                {
                    return true;
                }
            }
        }
        
        return super.login(credentials);
    }
}
