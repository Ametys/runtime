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

package org.ametys.plugins.core.user;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;

import org.ametys.core.user.InvalidModificationException;
import org.ametys.core.user.ModifiableUsersManager;
import org.ametys.core.user.UsersManager;
import org.ametys.core.util.cocoon.AbstractCurrentUserProviderServiceableAction;
import org.ametys.runtime.parameter.Errors;

/**
 * Create or modify a user
 */
public class EditAction extends AbstractCurrentUserProviderServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting user's edition");
        }
        
        String role = parameters.getParameter("usersManagerRole", UsersManager.ROLE);
        if (role.length() == 0)
        {
            role = UsersManager.ROLE;
        }
        
        UsersManager u = (UsersManager) manager.lookup(role);
        if (!(u instanceof ModifiableUsersManager))
        {
            getLogger().error("Users are not modifiable !");
            return null;
        }
        
        ModifiableUsersManager users = (ModifiableUsersManager) u;
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        Map<String, String> editParams = _getRequestParameters(request);
        String login = editParams.get("login");
        
        try
        {
            if (!"new".equals(request.getParameter("mode")))
            {
                if (getLogger().isInfoEnabled())
                {
                    String userMessage = null;
                    String endMessage = "is updating information about user '" + login + "'";
                    if (_isSuperUser())
                    {
                        userMessage = "Administrator";
                    }
                    else
                    {
                        String currentUserLogin = _getCurrentUser();
                        userMessage = "User '" + currentUserLogin + "'";
                    }
                    
                    getLogger().info(userMessage + " " + endMessage);
                }

                users.update(editParams);
            }
            else
            {
                if (getLogger().isInfoEnabled())
                {
                    String userMessage = null;
                    String endMessage = "is adding a new user '" + login + "'";
                    if (_isSuperUser())
                    {
                        userMessage = "Administrator";
                    }
                    else
                    {
                        String currentUserLogin = _getCurrentUser();
                        userMessage = "User '" + currentUserLogin + "'";
                    }
                    
                    getLogger().info(userMessage + " " + endMessage);
                }
    
                users.add(editParams);
            }
        }
        catch (InvalidModificationException e)
        {
            Map<String, Errors> fieldErrors = e.getFieldErrors();
            
            if (fieldErrors != null && fieldErrors.size() > 0)
            {
                Map<String, String> result = new HashMap<>();
                result.put("error", StringUtils.join(fieldErrors.keySet(), ","));
                return result;
            }
            else
            {
                throw e;
            }
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending user's edition");
        }

        return EMPTY_MAP;
    }
    
    private Map<String, String> _getRequestParameters(Request request)
    {
        Map<String, String> editParams = new HashMap<>();
        
        Enumeration<String> requestParameterNames = request.getParameterNames();
        while (requestParameterNames.hasMoreElements())
        {
            String parameterName = requestParameterNames.nextElement();
            if (parameterName.startsWith("field_"))
            {
                String jdbcParameterName = parameterName.substring(6);
                String value = request.getParameter(parameterName);
                editParams.put(jdbcParameterName, value);
            }
        }

        return editParams;
    }
}
