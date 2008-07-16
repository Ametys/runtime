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
package org.ametys.runtime.plugins.core.right.ui.actions;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.plugins.core.right.profile.ProfileBasedRightsManager;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.user.UserHelper;


/**
 * This class removes profiles'assignment from users and groups for a given context
 */
public class RemoveAction extends ServiceableAction implements ThreadSafe
{
    /** The profile base impl of rights'manager*/
    protected ProfileBasedRightsManager _rightsManager;
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting removing assignment");
        }

        if (_rightsManager == null)
        {
            _rightsManager = (ProfileBasedRightsManager) manager.lookup(RightsManager.ROLE);
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        String[] users = AssignAction._getList(request.getParameter("users"));
        String[] groups = AssignAction._getList(request.getParameter("groups"));
        String context = request.getParameter("context");
        
        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is removing all the rights'assignment on context '" + context + "' for Users [" + _toString(users) + "] and Groups [" + _toString(groups) + "]";
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
        
        // Retire les profils existants
        _removeProfiles (users, groups, context);

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Endinf removing assignment");
        }

        return EMPTY_MAP;
    }
    
    private void _removeProfiles(String[] users, String[] groups, String context)
    {
        for (int i = 0; users != null && i < users.length; i++)
        {
            _rightsManager.removeUserProfiles(users[i], context);
        }
        for (int i = 0; groups != null && i < groups.length; i++)
        {
            _rightsManager.removeGroupProfiles(groups[i], context);
        }
    }
    
    private String _toString(String[] string)
    {
        String result = "";
        for (int i = 0; string != null && i < string.length; i++)
        {
            if (i > 0)
            {
                result += ", ";
            }
            result += string[i];
        }
        return result;
    }
}
