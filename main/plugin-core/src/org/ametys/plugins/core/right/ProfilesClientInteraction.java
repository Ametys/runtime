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
import org.ametys.plugins.core.impl.right.profile.ProfileBasedRightsManager;
import org.ametys.runtime.i18n.I18nizableText;

/**
 * Component for profiles client interaction
 *
 */
public class ProfilesClientInteraction extends AbstractLogEnabled implements Component, Serviceable
{
    private RightsExtensionPoint _rights;
    private RightsManager _rightsManager;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _rights = (RightsExtensionPoint) manager.lookup(RightsExtensionPoint.ROLE);
        _rightsManager = (RightsManager) manager.lookup(RightsManager.ROLE);
    }

    /**
     * Get the rights of a profile classified by category
     * @param profileId the id of the profile
     * @return the profile's rights
     */
    @Callable
    public Map<String, Object> getProfileRights(String profileId)
    {
        Map<String, Object> result = new HashMap<>();
        
        Map<I18nizableText, List<Map<String, Object>>> rightsByCategory = new HashMap<>();
        
        if (_rightsManager instanceof ProfileBasedRightsManager)
        {
            Profile profile = ((ProfileBasedRightsManager) _rightsManager).getProfile(profileId);
            if (profile != null)
            {
                result.put("profile", profileId);
                
                Set<String> rights = profile.getRights();
                for (String rightId : rights)
                {
                    Right right = _rights.getExtension(rightId);
                    if (right != null)
                    {
                        I18nizableText categoryLabel = right.getCategory();
                        if (!rightsByCategory.containsKey(categoryLabel))
                        {
                            rightsByCategory.put(categoryLabel, new ArrayList<Map<String, Object>>());
                        }
                        
                        rightsByCategory.get(categoryLabel).add(right.toJSON());
                    }
                }
            }
        }
        
        List<Map<String, Object>> categories = new ArrayList<>();
        
        for (I18nizableText categoryLabel : rightsByCategory.keySet())
        {
            Map<String, Object> category = new HashMap<>();
            category.put("label", categoryLabel);
            category.put("rights", rightsByCategory.get(categoryLabel));
            categories.add(category);
        }
        
        result.put("categories", categories);

        return result;
    }
}
