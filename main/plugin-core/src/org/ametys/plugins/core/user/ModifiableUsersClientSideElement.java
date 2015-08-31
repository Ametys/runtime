/*
 *  Copyright 2010 Anyware Services
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
package org.ametys.plugins.core.user;

import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.core.ui.StaticClientSideElement;
import org.ametys.core.user.ModifiableUsersManager;
import org.ametys.core.user.UsersManager;

/**
 * This implementation creates a control that needs a <code>ModifiableUsersManager</code>
 */
public class ModifiableUsersClientSideElement extends StaticClientSideElement
{
    /** The users manager */
    protected UsersManager _usersManager;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _usersManager = (UsersManager) smanager.lookup(UsersManager.ROLE);
    }
    
    @Override
    public Script getScript(Map<String, Object> contextParameters)
    {
        Script script = super.getScript(contextParameters);
        if (script == null)
        {
            return null;
        }
        
        if (_usersManager instanceof ModifiableUsersManager)
        {
            return script;
        }
        
        return null;
    }
}
