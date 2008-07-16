/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
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

import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;


/**
 * This rights manager anwswer always OK 
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
