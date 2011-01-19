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
        String[] users = request.getParameterValues("users");
        String[] groups = request.getParameterValues("groups");
        String[] profiles = request.getParameterValues("profiles");
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
}
