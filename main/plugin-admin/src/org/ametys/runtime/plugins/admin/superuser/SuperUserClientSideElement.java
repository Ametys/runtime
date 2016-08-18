/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.runtime.plugins.admin.superuser;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.core.right.RightManager;
import org.ametys.core.ui.Callable;
import org.ametys.core.ui.StaticClientSideElement;
import org.ametys.core.user.UserIdentity;

/**
 * This implementation creates a control allowing to affect a super user to a given context
 */
public class SuperUserClientSideElement extends StaticClientSideElement
{
    /** The service manager */
    private ServiceManager _sManager;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _sManager = smanager;
    }
    
    /**
     * Affect a user as super user on a given context
     * @param users the list of users to affect
     * @param context the context 
     * @return A map containing the assigned profile ids
     */
    @Callable
    public Map<String, Object> affectSuperUser(List<Map<String, String>> users, String context)
    {
        try
        {
            if (_rightManager == null)
            {
                _rightManager = (RightManager) _sManager.lookup(RightManager.ROLE);
            }
        }
        catch (ServiceException e)
        {
            throw new IllegalStateException(e);
        }
        
        if (users.size() == 0)
        {
            throw new IllegalArgumentException("No login to initialize.");
        }
        
        Map<String, Object> result = new LinkedHashMap<>();
        Set<String> profileIds = new HashSet<>();
        result.put("profileIds", profileIds);
        for (Map<String, String> user : users)
        {
            String login = user.get("login");
            String populationId = user.get("population");
            UserIdentity userIdentity = new UserIdentity(login, populationId);
            String profileId = _rightManager.grantAllPrivileges(userIdentity, context);
            profileIds.add(profileId);
        }
        
        return result;
    }
}
