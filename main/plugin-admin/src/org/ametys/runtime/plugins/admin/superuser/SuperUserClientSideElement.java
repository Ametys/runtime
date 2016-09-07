/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.runtime.plugins.admin.superuser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.core.right.Profile;
import org.ametys.core.right.ProfileAssignmentStorageExtensionPoint;
import org.ametys.core.right.RightAssignmentContext;
import org.ametys.core.right.RightAssignmentContextExtensionPoint;
import org.ametys.core.right.RightProfilesDAO;
import org.ametys.core.right.RightsExtensionPoint;
import org.ametys.core.ui.Callable;
import org.ametys.core.ui.StaticClientSideElement;
import org.ametys.core.user.UserIdentity;
import org.ametys.runtime.workspace.WorkspaceMatcher;

/**
 * This implementation creates a control allowing to affect a super user to a given context
 */
public class SuperUserClientSideElement extends StaticClientSideElement
{
    /** The service manager */
    protected ServiceManager _sManager;
    /** The extension point for the rights */
    protected RightsExtensionPoint _rightsEP;
    /** The profiles DAO */
    protected RightProfilesDAO _profilesDAO;
    /** The profile assignments storage */
    protected ProfileAssignmentStorageExtensionPoint _profileAssignmentStorageEP;
    /** The extension point for right contexts */
    protected RightAssignmentContextExtensionPoint _rightCtxEP;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _sManager = smanager;
    }
    
    /**
     * Affect a user to the given profile on the given context
     * @param user The user
     * @param profileId The profile id
     * @param jsParameters The client-side parameters
     */
    @Callable
    public void affectUserToProfile(String user, String profileId, Map<String, Object> jsParameters)
    {
        try
        {
            if (_profileAssignmentStorageEP == null)
            {
                _profileAssignmentStorageEP = (ProfileAssignmentStorageExtensionPoint) _sManager.lookup(ProfileAssignmentStorageExtensionPoint.ROLE);
            }
            if (_rightCtxEP == null)
            {
                _rightCtxEP = (RightAssignmentContextExtensionPoint) _sManager.lookup(RightAssignmentContextExtensionPoint.ROLE);
            }
        }
        catch (ServiceException e)
        {
            throw new IllegalStateException(e);
        }
        
        // Get all root contexts for the configured workspace
        List<Object> rootContexts = new ArrayList<>();
        for (String id : _rightCtxEP.getExtensionsIds())
        {
            RightAssignmentContext rightCtx = _rightCtxEP.getExtension(id);
            rootContexts.addAll(rightCtx.getRootContexts(getContextualParameters(jsParameters)));
        }
        
        // Affect user to this profile
        UserIdentity userIdentity = UserIdentity.stringToUserIdentity(user);
        for (Object rootContext : rootContexts)
        {
            _profileAssignmentStorageEP.removeDeniedProfileFromUser(userIdentity, profileId, rootContext);
            _profileAssignmentStorageEP.allowProfileToUser(userIdentity, profileId, rootContext);
        }
    }
    
    /**
     * Affect a user to a new super profile on the given context. First, a new profile with the given name will be created and filled with all rights, and then the user will be affected.
     * @param user The user
     * @param newProfileName The name of the super profile to create
     * @param jsParameters The client-side parameters
     * @return The id of the created profile
     */
    @Callable
    public String affectUserToNewProfile(String user, String newProfileName, Map<String, Object> jsParameters)
    {
        try
        {
            if (_rightsEP == null)
            {
                _rightsEP = (RightsExtensionPoint) _sManager.lookup(RightsExtensionPoint.ROLE);
            }
            if (_profilesDAO == null)
            {
                _profilesDAO = (RightProfilesDAO) _sManager.lookup(RightProfilesDAO.ROLE);
            }
        }
        catch (ServiceException e)
        {
            throw new IllegalStateException(e);
        }
        
        // Create a super profile
        Profile newSuperProfile = _profilesDAO.addProfile(newProfileName, null);
        _profilesDAO.addRights(newSuperProfile, new ArrayList<>(_rightsEP.getExtensionsIds()));

        affectUserToProfile(user, newSuperProfile.getId(), jsParameters);
        
        return newSuperProfile.getId();
    }
    
    /**
     * Get the contextual parameters used to determines the root contexts
     * @param jsParameters the client-side parameters
     * @return the contextual parameters
     */
    protected Map<String, Object> getContextualParameters(Map<String, Object> jsParameters)
    {
        Map<String, Object> contextParameters = new HashMap<>(jsParameters);
        
        String workspaceName = (String) _script.getParameters().get(WorkspaceMatcher.WORKSPACE_NAME);
        contextParameters.put(WorkspaceMatcher.WORKSPACE_NAME, workspaceName);
        return contextParameters;
    }
}
