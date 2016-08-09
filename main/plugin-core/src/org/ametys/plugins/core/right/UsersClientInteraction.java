/*
 *  Copyright 2014 Anyware Services
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
package org.ametys.plugins.core.right;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.right.Right;
import org.ametys.core.right.RightsExtensionPoint;
import org.ametys.core.right.RightsManager;
import org.ametys.core.right.profile.Profile;
import org.ametys.core.ui.Callable;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;
import org.ametys.plugins.core.impl.right.profile.DefaultProfileBasedRightsManager;
import org.ametys.plugins.core.impl.right.profile.ProfileBasedRightsManager;
import org.ametys.plugins.core.user.UserHelper;

/**
 * Component for users client interaction
 *
 */
public class UsersClientInteraction extends AbstractLogEnabled implements Serviceable, Component
{
    private RightsExtensionPoint _rights;
    private RightsManager _rightsManager;
    private UserManager _userManager;
    private UserHelper _userHelper;

    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        _rights = (RightsExtensionPoint) m.lookup(RightsExtensionPoint.ROLE);
        _rightsManager = (RightsManager) m.lookup(RightsManager.ROLE);
        _userManager = (UserManager) m.lookup(UserManager.ROLE);
        _userHelper = (UserHelper) m.lookup(UserHelper.ROLE);
    }

    /**
     * Structuring information about the user's rights in the JSON format
     * @param login the login of the user
     * @param populationId The id of the population of the user
     * @return result the user's rights as JSON object
     */
    @Callable
    public Map<String, Object> getUserRights(String login, String populationId) 
    {
        Map<String, Object> result = new HashMap<>();

        result.put("user", new ArrayList<Map<String, Object>>());

        User user = _userManager.getUser(populationId, login);
        if (user == null)
        {
            throw new IllegalStateException("The user ('" + login + "', '" + populationId + "') does not exist");
        }
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.putAll(_userHelper.user2json(user, true));

        if (_rightsManager instanceof DefaultProfileBasedRightsManager)
        {
            List<Object> profiles = new ArrayList<>();
            
            Map<String, Set<String>> userProfiles = ((DefaultProfileBasedRightsManager) _rightsManager).getProfilesAndContextByUser(new UserIdentity(login, populationId));
            for (String profileId : userProfiles.keySet())
            {
                Map<String, Object> profileInfo = new HashMap<>();
         
                Profile profile = ((ProfileBasedRightsManager) _rightsManager).getProfile(profileId);
                profileInfo.put("id", profileId);
                profileInfo.put("label", profile.getName());

                Set<String> contexts = userProfiles.get(profileId);

                // Sort contexts by alphabetical order
                ArrayList<String> orderedContexts = new ArrayList<>(contexts);
                Collections.sort(orderedContexts);

                profileInfo.put("contexts", orderedContexts);
                profiles.add(profileInfo);
            }
            userInfo.put("profiles", profiles);
        }
        else
        {
            // The rights
            Map<String, Set<String>> rightsByContext = _rightsManager.getUserRights(new UserIdentity(login, populationId));
            List<Object> rightsByContextToJSON = new ArrayList<>();

            for (String context : rightsByContext.keySet())
            {
                Map<String, Object> info = new HashMap<>();
                info.put("context", context);
                
                List<Map<String, Object>> rights = new ArrayList<>();

                Set<String> rightIds = rightsByContext.get(context);
                for (String rightId : rightIds)
                {
                    Right right = _rights.getExtension(rightId);
                    if (right != null)
                    {
                        rights.add(right.toJSON());
                    }
                }
                
                info.put("rights", rights);
                
                rightsByContextToJSON.add(info);
            }
            userInfo.put("rights", rightsByContextToJSON);
        }
        
        result.put("user", userInfo);
        return result;
    }
}
