/*
 *  Copyright 2013 Anyware Services
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
package org.ametys.runtime.plugins.core.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;
import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.util.parameter.Enumerator;

/**
 * Enumerator for users
 *
 */
public class UsersEnumerator implements Enumerator, Serviceable
{
    private UsersManager _usersManager;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _usersManager = (UsersManager) manager.lookup(UsersManager.ROLE);
    }

    @Override
    public I18nizableText getEntry(String value) throws Exception
    {
        User user = _usersManager.getUser(value);
        if (user != null)
        {
            return new I18nizableText(user.getFullName()); 
        }
        else
        {
            return new I18nizableText("");
        }
    }

    @Override
    public Map<Object, I18nizableText> getEntries() throws Exception
    {
        Map<Object, I18nizableText> entries = new HashMap<Object, I18nizableText>();
        
        Collection<User> users = _usersManager.getUsers();
        for (User user : users)
        {
            entries.put(user.getName(), new I18nizableText(user.getFullName()));
        }
        return entries;
    }

}
