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
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.core.right.Profile;
import org.ametys.core.right.RightManager;
import org.ametys.core.right.RightProfilesDAO;
import org.ametys.core.right.RightsExtensionPoint;
import org.ametys.core.ui.Callable;
import org.ametys.core.ui.StaticClientSideElement;
import org.ametys.core.user.UserIdentity;
import org.ametys.plugins.core.right.profile.ProfileDAO;

/**
 * This implementation creates a control allowing to affect a super user to a given context
 */
public class SuperUserClientSideElement extends StaticClientSideElement
{
    /** The service manager */
    private ServiceManager _sManager;
    
    /** The extension point for the rights */
    private RightsExtensionPoint _rightsEP;
    
    private RightProfilesDAO _profilesDAO;
    
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
     * @param context The context
     * @param additionalParameters The additional parameters
     */
    @Callable
    public void affectUserToProfile(String user, String profileId, String context, Map<String, Object> additionalParameters)
    {
        try
        {
            if (_rightManager == null)
            {
                _rightManager = (RightManager) _sManager.lookup(RightManager.ROLE);
            }
        }
        catch (ServiceException e)
        {
            throw new IllegalStateException(e);
        }
        
        UserIdentity userIdentity = UserIdentity.stringToUserIdentity(user);
        _rightManager.allowProfileToUser(userIdentity, profileId, context);
    }
    
    /**
     * Affect a user to a new super profile on the given context. First, a new profile with the given name will be created and filled with all rights, and then the user will be affected.
     * @param user The user
     * @param newProfileName The name of the super profile to create
     * @param context The context
     * @param additionalParameters The additional parameters
     * @return The id of the created profile
     */
    @Callable
    public String affectUserToNewProfile(String user, String newProfileName, String context, Map<String, Object> additionalParameters)
    {
        try
        {
            if (_rightManager == null)
            {
                _rightManager = (RightManager) _sManager.lookup(RightManager.ROLE);
            }
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
        String id = _generateUniqueId(newProfileName);
        Profile newSuperProfile = _rightManager.addProfile(id, newProfileName, null);
        _profilesDAO.addRights(newSuperProfile, new ArrayList<>(_rightsEP.getExtensionsIds()));
        
        // Affect user to this profile
        UserIdentity userIdentity = UserIdentity.stringToUserIdentity(user);
        _rightManager.allowProfileToUser(userIdentity, id, context);
        
        return id;
    }
    
    private String _generateUniqueId(String label)
    {
        // Id generated from name lowercased, trimmed, and spaces and underscores replaced by dashes
        String value = label.toLowerCase().trim().replaceAll("[\\W_]", "-").replaceAll("-+", "-").replaceAll("^-", "");
        int i = 2;
        String suffixedValue = value;
        while (_rightManager.getProfile(suffixedValue) != null)
        {
            suffixedValue = value + i;
            i++;
        }
        
        return suffixedValue;
    }
}
