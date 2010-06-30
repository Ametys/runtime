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

import java.util.Set;

import org.ametys.runtime.authentication.Authentication;
import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.right.RightsManager;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;


/**
 * Authenticate http request with the current base users extension (which must
 * implements AuthenticatingBaseUsers).
 */
public class HasRightAuthentication extends AbstractLogEnabled implements Authentication, Serviceable
{
    private RightsManager _rightMgr;
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _rightMgr = (RightsManager) manager.lookup(RightsManager.ROLE);
    }
    
    /**
     * Check if a User can log in. 
     * @param credentials Contains user information with an unencrypted password
     * @return true if the user is authenticated, false otherwise.
     */
    public boolean login(Credentials credentials)
    {
        String login = credentials.getLogin();
        Set<String> userRightsSet = _rightMgr.getUserRights(login, null);
        return !(userRightsSet == null || userRightsSet.size() == 0);
    }
}
