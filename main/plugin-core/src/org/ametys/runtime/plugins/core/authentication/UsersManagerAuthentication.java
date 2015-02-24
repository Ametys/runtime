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
package org.ametys.runtime.plugins.core.authentication;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.runtime.authentication.Authentication;
import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.plugins.core.authentication.token.TokenCredentials;
import org.ametys.runtime.user.CredentialsAwareUsersManager;
import org.ametys.runtime.user.UsersManager;


/**
 * Authenticate http request with the current base users extension (which must
 * implements AuthenticatingBaseUsers).
 */
public class UsersManagerAuthentication extends AbstractLogEnabled implements Authentication, Serviceable
{
    /** The users manager */
    protected UsersManager _users;
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _users = (UsersManager) manager.lookup(UsersManager.ROLE);
    }
    
    /**
     * Check if a User can log in. Returns the User identified by this login or null if none.
     * @param credentials Contains user information with an unencrypted password
     * @return true if the user is authenticated, false otherwise.
     */
    public boolean login(Credentials credentials)
    {
        // Check users manager can authenticate
        if (_users instanceof CredentialsAwareUsersManager)
        {
            // If the credentials come from a SSO (like CAS), they are already authenticated : grant access.
            if (credentials instanceof TokenCredentials)
            {
                return ((TokenCredentials) credentials).checkToken();
            }
            
            // Do authenticate encrypting password
            CredentialsAwareUsersManager auth = (CredentialsAwareUsersManager) _users;
            return auth.checkCredentials(credentials);
            
        }
        
        getLogger().error("UsersManager cannot authenticate");
        
        // Invalid authentication class, forbid access
        return false;
    }
}
