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
package org.ametys.runtime.plugins.core.right.profile.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.plugins.core.right.profile.Profile;
import org.ametys.runtime.plugins.core.right.profile.ProfileBasedRightsManager;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;


/**
 * This action creates a new profile with name given in request parameter, and return its id
 */
public class CreateProfileAction extends CurrentUserProviderServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting profile creation");
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        String newProfilesName = request.getParameter("name");
        if (newProfilesName == null || newProfilesName.trim().length() == 0)
        {
            throw new IllegalArgumentException("The new profile name cannot be empty");
        }
        
        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is adding a new profile '" + newProfilesName + "'";
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
        
        ProfileBasedRightsManager profileBasedRightsManager;
        try
        {
            RightsManager rightsManager = (RightsManager) manager.lookup(RightsManager.ROLE);
            if (rightsManager instanceof ProfileBasedRightsManager)
            {
                profileBasedRightsManager = (ProfileBasedRightsManager) rightsManager;
            }
            else
            {
                throw new IllegalStateException("RightsManager is of class '" + rightsManager.getClass().getName() + "' that is not an instance of ProfileBasedRightsManager");
            }
        }
        catch (Exception e)
        {
            String message = "Cannot retrieve a ProfileBasedRightsManager.";
            getLogger().error(message, e);
            throw new ProcessingException(message, e);
        }

        String context = request.getParameter("context");
        
        // Création du profil
        Profile profile = profileBasedRightsManager.addProfile(newProfilesName, context);
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending profile creation");
        }
        
        Map<String, String> result = new HashMap<String, String>();
        result.put("id", profile.getId());
        return result;
    }
}
