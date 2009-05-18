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
 * This action deletes the profile with id corresponding to the one specified in request
 */
public class DeleteProfileAction extends CurrentUserProviderServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting profile removal");
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        String profileId = request.getParameter("id");
        
        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is removing the profile '" + profileId + "'";
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
        
        // Suppression du profil
        Profile profile = profileBasedRightsManager.getProfile(profileId);
        if (profile != null)
        {
            profile.remove();
        }
        else if (getLogger().isWarnEnabled())
        {
            String userMessage = null;
            String endMessage = "is trying to remove an unexisting profile '" + profileId + "'";
            if (_isSuperUser())
            {
                userMessage = "Administrator";
            }
            else
            {
                String currentUserLogin = _getCurrentUser();
                userMessage = "User '" + currentUserLogin + "'";
            }
            
            getLogger().warn(userMessage + " " + endMessage);
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending profile removal");
        }
        
        return EMPTY_MAP;
    }
}
