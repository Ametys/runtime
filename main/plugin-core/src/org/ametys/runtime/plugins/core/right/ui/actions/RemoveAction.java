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
 * This class removes profiles'assignment from users and groups for a given context
 */
public class RemoveAction extends CurrentUserProviderServiceableAction
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
        String[] users = request.getParameterValues("users");
        String[] groups = request.getParameterValues("groups");
        String context = request.getParameter("context");
        String profileId = request.getParameter("profileId");
        
        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is removing all the rights'assignment on context '" + context + "' for Users [" + _toString(users) + "] and Groups [" + _toString(groups) + "]";
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
        _removeProfile (users, groups, profileId, context);

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Endinf removing assignment");
        }

        return EMPTY_MAP;
    }
    
    private void _removeProfile(String[] users, String[] groups, String profileId, String context)
    {
        for (int i = 0; users != null && i < users.length; i++)
        {
            _rightsManager.removeUserProfile(users[i], profileId, context);
        }
        for (int i = 0; groups != null && i < groups.length; i++)
        {
            _rightsManager.removeGroupProfile(groups[i], profileId, context);
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
