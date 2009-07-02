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

        UsersManager u = (UsersManager) manager.lookup(UsersManager.ROLE);
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
