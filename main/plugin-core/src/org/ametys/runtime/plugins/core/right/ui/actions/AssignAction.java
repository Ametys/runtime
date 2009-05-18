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
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.plugins.core.right.profile.ProfileBasedRightsManager;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;


/**
 * This action assigns profiles to users and groups.
 */
public class AssignAction extends CurrentUserProviderServiceableAction
{
    /** The profile base impl of rights'manager*/
    protected ProfileBasedRightsManager _rightsManager;
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting assignment");
        }

        if (_rightsManager == null)
        {
            _rightsManager = (ProfileBasedRightsManager) manager.lookup(RightsManager.ROLE);
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        String[] users = _getList(request.getParameter("users"));
        String[] groups = _getList(request.getParameter("groups"));
        String[] profiles = _getList(request.getParameter("profiles"));
        String context = request.getParameter("context");
        
        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is modifying the rights'assignment on context '" + context 
                + "'. New assignment concerns Users [" + _toString(users) + "]"
                + "'. and Groups [" + _toString(groups) + "]"
                + "'. with profiles [" + _toString(profiles) + "]";
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
        
        // Retire les profils existants
        _removeProfiles (users, groups, context);
        
        // Ajoute les nouveau profils
        _addProfiles (users, groups, context, profiles);

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending assignment");
        }

        return EMPTY_MAP;
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
    
    private void _addProfiles(String[] users, String[] groups, String context, String[] profiles)
    {
        for (int i = 0; users != null && i < users.length; i++)
        {
            for (int j = 0; profiles != null && j < profiles.length; j++)
            {
                _rightsManager.addUserRight(users[i], context, profiles[j]);
            }
        }
    
        for (int i = 0; groups != null && i < groups.length; i++)
        {
            for (int j = 0; profiles != null && j < profiles.length; j++)
            {
                _rightsManager.addGroupRight(groups[i], context, profiles[j]);
            }
        }
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

    static String[] _getList(String list)
    {
        if (list.length() > 0)
        {
            return list.split("/");
        }
        else
        {
            return null;
        }
    }
}
