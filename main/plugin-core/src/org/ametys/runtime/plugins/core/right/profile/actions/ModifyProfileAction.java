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
 * This action modify the composition of the profile 'id' with the / sperated list given in 'rights' (that are request parameters)
 */
public class ModifyProfileAction extends CurrentUserProviderServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting profile modification");
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        String profileId = request.getParameter("id");
        String rightsList = request.getParameter("objects");
        
        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is modifying the profile '" + profileId + "'";
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

        // Modification du profil
        Profile profile = profileBasedRightsManager.getProfile(profileId);
        if (profile == null)
        {
            Map<String, String> result = new HashMap<String, String>();
            result.put("message", "missing");
            return result;
        }
        else
        {
            // ... effacer les droits actuels
            profile.removeRights();
             
            if (rightsList != null && !"".equals(rightsList))
            {
                // Liste des droits composant désormais le profil
                String[] rightsId = rightsList.split("/");
                
                // ... insérer les nouveaux droits
                for (int i = 0; i < rightsId.length; i++)
                {
                    profile.addRight(rightsId[i]);
                }
            }
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending profile modification");
        }
        
        return EMPTY_MAP;
    }

}
