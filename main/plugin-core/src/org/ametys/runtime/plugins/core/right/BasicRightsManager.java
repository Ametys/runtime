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
package org.ametys.runtime.plugins.core.right;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.ametys.runtime.right.RightsContextPrefixExtensionPoint;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;


/**
 * This rights manager always answers OK. 
 */
public class BasicRightsManager implements RightsManager, Serviceable, ThreadSafe, Component
{
    private UsersManager _users;
    private RightsExtensionPoint _rightsExtensionPoint;
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _users = (UsersManager) manager.lookup(UsersManager.ROLE);
        _rightsExtensionPoint = (RightsExtensionPoint) manager.lookup(RightsExtensionPoint.ROLE);
    }
    
    public Set<String> getGrantedUsers(String right, String context)
    {
        Set<String> usersLogin = new HashSet<String>();
        
        Collection<User> users = _users.getUsers();
        for (User user : users)
        {
            usersLogin.add(user.getName());
        }
        
        return usersLogin;
    }

    public Set<String> getUserRights(String login, String context)
    {
        return _rightsExtensionPoint.getExtensionsIds();
    }

    public RightResult hasRight(String userLogin, String right, String context)
    {
        return RightsManager.RightResult.RIGHT_OK;
    }

}
