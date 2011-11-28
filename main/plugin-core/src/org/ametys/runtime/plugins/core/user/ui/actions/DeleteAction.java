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
package org.ametys.runtime.plugins.core.user.ui.actions;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.UsersManager;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;


/**
 * This action deletes a user given by its login 
 */
public class DeleteAction extends CurrentUserProviderServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters params) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String[] login = request.getParameterValues("login");
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting user's removal");
        }

        String role = params.getParameter("usersManagerRole", UsersManager.ROLE);
        if (role.length() == 0)
        {
            role = UsersManager.ROLE;
        }
        
        UsersManager u = (UsersManager) manager.lookup(role);
        if (!(u instanceof ModifiableUsersManager))
        {
            return null;
        }
        
        ModifiableUsersManager users = (ModifiableUsersManager) u;
        
        for (int i = 0; i < login.length; i++)
        {
            if (getLogger().isInfoEnabled())
            {
                String userMessage = null;
                String endMessage = "is removing user '" + login[i] + "' from the application";
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

            users.remove(login[i]);
        }
       
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending user's removal");
        }

        return EMPTY_MAP;
    }
}
