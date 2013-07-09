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
package org.ametys.runtime.plugins.core.user.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;

import org.ametys.runtime.authentication.AuthenticateAction;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;
import org.ametys.runtime.util.cocoon.AbstractCurrentUserProviderServiceableAction;

/**
 * This action impersonate the current user with the given login 
 */
public class ImpersonateAction extends AbstractCurrentUserProviderServiceableAction
{
    private UsersManager _usersManager;
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map<String, String> result = new HashMap<String, String>();
        
        if (!_isSuperUser())
        {
            throw new IllegalStateException("Current user is not logged as administrator");
        }

        String login = parameters.getParameter("login", null);
        if (StringUtils.isEmpty(login))
        {
            throw new IllegalArgumentException("'login' parameter is null or empty");
        }
        
        if (_usersManager == null)
        {
            _usersManager = (UsersManager) manager.lookup(UsersManager.ROLE);
        }
        
        User user = _usersManager.getUser(login);
        if (user == null)
        {
            result.put("error", " There is no user with login '" + login + "' in the user manager");   
        }
        else
        {
            Request request = ObjectModelHelper.getRequest(objectModel);
            request.getSession(true).setAttribute(AuthenticateAction.SESSION_USERLOGIN, login);
            
            result.put("login", login);
            result.put("name", user.getFullName());
            
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Impersonification of user '" + login + "' from IP " + request.getRemoteAddr());
            }
        }
        
        return result;
    }
}
