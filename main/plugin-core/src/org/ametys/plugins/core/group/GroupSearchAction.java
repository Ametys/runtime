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
package org.ametys.plugins.core.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.core.group.GroupsManager;

/**
 * Get groups 
 *
 */
public class GroupSearchAction extends ServiceableAction
{
    private static final int _DEFAULT_COUNT_VALUE = 100;
    private static final int _DEFAULT_OFFSET_VALUE = 0;

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        // Get the wanted UsersManager avalon role, defaults to runtime-declared UsersManager.
        String role = parameters.getParameter("groupsManagerRole", GroupsManager.ROLE);
        if (role.length() == 0)
        {
            role = GroupsManager.ROLE;
        }
        
        List<Map<String, Object>> groups = new ArrayList<>();
        GroupsManager groupsManager = null;
        
        try
        {
            groupsManager = (GroupsManager) manager.lookup(role);
            
            if (jsParameters.get("id") != null)
            {
                @SuppressWarnings("unchecked")
                List<String> ids = (List<String>) jsParameters.get("id");
                for (String id : ids)
                {
                    groups.add(groupsManager.group2JSON(id));
                }
            }
            else
            {
                int count = parameters.getParameterAsInteger("limit", _DEFAULT_COUNT_VALUE);
                if (count == -1)
                {
                    count = Integer.MAX_VALUE;
                }

                int offset = parameters.getParameterAsInteger("start", _DEFAULT_OFFSET_VALUE);
                
                groups.addAll(groupsManager.groups2JSON(count, offset, _getSearchParameters(source)));
            }
        }
        catch (ServiceException e)
        {
            getLogger().error("Error looking up GroupsManager of role " + role, e);
            throw new ProcessingException("Error looking up GroupsManager of role " + role, e);
        }
        finally
        {
            manager.release(groupsManager);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("groups", groups);

        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);

        return EMPTY_MAP;
    }
    
    /**
     * Get the search parameters
     * @param source The search pattern
     * @return the search parameters
     */
    protected Map<String, String> _getSearchParameters (String source)
    {
        Map<String, String> params = new HashMap<>();
        params.put("pattern", source);
        return params;
    }

}
