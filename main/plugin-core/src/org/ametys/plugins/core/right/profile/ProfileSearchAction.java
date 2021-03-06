/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.plugins.core.right.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.core.right.Profile;
import org.ametys.core.right.RightProfilesDAO;

/**
 * Get profiles
 *
 */
public class ProfileSearchAction extends ServiceableAction
{
    private RightProfilesDAO _profilesDAO;
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (_profilesDAO == null)
        {
            // Lazy loading for safe mode
            _profilesDAO = (RightProfilesDAO) manager.lookup(RightProfilesDAO.ROLE);
        }
        
        Map<String, Object> result = new HashMap<>();
        
        List<Map<String, Object>> profiles = new ArrayList<>();
        
        for (Profile profile : _profilesDAO.getProfiles())
        {
            Map<String, Object> profile2json = profile.toJSON();
            profile2json.put("rights", _profilesDAO.getRights(profile));
            profiles.add(profile2json);
        }
        
        result.put("profiles", profiles);
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);

        return EMPTY_MAP;
    }
    
}
