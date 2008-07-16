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
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.UserHelper;
import org.ametys.runtime.user.UsersManager;


/**
 * This action deletes a user given by its login 
 */
public class DeleteAction extends ServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters params) throws Exception
    {
        String login = src;
        
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
        
        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is removing user '" + login + "' from the application";
            if (UserHelper.isAdministrator(objectModel))
            {
                userMessage = "Administrator";
            }
            else
            {
                String currentUserLogin = UserHelper.getCurrentUser(objectModel);
                userMessage = "User '" + currentUserLogin + "'";
            }
            
            getLogger().info(userMessage + " " + endMessage);
        }

        users.remove(login);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending user's removal");
        }

        return EMPTY_MAP;
    }
}
