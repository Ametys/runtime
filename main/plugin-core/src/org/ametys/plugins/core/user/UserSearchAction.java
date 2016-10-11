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
package org.ametys.plugins.core.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.core.user.UserManager;

/**
 * Get users.
 */
public class UserSearchAction extends AbstractAction implements ThreadSafe, Serviceable
{
    private static final int _DEFAULT_COUNT_VALUE = 100;
    private static final int _DEFAULT_OFFSET_VALUE = 0;
    
    private UserManager _userManager;
    private UserHelper _userHelper;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _userManager = (UserManager) manager.lookup(UserManager.ROLE);
        _userHelper = (UserHelper) manager.lookup(UserHelper.ROLE);
    }
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        List<Map<String, Object>> users = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        List<String> contexts = (List<String>) jsParameters.get("contexts");
        
        if (contexts != null)
        {
            // search over the populations of the given context
            _searchUsersByContext(users, jsParameters, source, parameters, new HashSet<>(contexts));
        }
        else
        {
            // search over the given population
            String userPopulationId = (String) jsParameters.get("userPopulationId");
            String userDirectoryId = null;
            if (jsParameters.get("userDirectoryId") != null)
            {
                userDirectoryId = (String) jsParameters.get("userDirectoryId");
                userDirectoryId = "-".equals(userDirectoryId) ? null : userDirectoryId;
            }
            _searchUsersByPopulation(users, jsParameters, source, parameters, userPopulationId, userDirectoryId);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("users", users);

        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);

        return EMPTY_MAP;
    }
    
    /**
     * Get the search parameters
     * @param source The search pattern
     * @return the search parameters
     */
    protected Map<String, Object> _getSearchParameters(String source)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("pattern", source);
        return params;
    }
    
    private void _searchUsersByContext(List<Map<String, Object>> users, Map<String, Object> jsParameters, String source, Parameters parameters, Set<String> contexts)
    {
        if (jsParameters.get("login") != null)
        {
            @SuppressWarnings("unchecked")
            List<String> logins = (List<String>) jsParameters.get("login");
            for (String login : logins)
            {
                users.add(_userHelper.user2json(_userManager.getUserByContext(contexts, login), true));
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
            
            users.addAll(_userHelper.users2json(_userManager.getUsersByContext(contexts, count, offset, _getSearchParameters(source)), true));
        }
    }
    
    private void _searchUsersByPopulation(List<Map<String, Object>> users, Map<String, Object> jsParameters, String source, Parameters parameters, String userPopulationId, String userDirectoryId)
    {
        if (jsParameters.get("login") != null && userDirectoryId != null)
        {
            @SuppressWarnings("unchecked")
            List<String> logins = (List<String>) jsParameters.get("login");
            for (String login : logins)
            {
                users.add(_userHelper.user2json(_userManager.getUserByDirectory(userPopulationId, userDirectoryId, login), true));
            }
        }
        else if (jsParameters.get("login") != null)
        {
            // userDirectoryId = null => take account of all the user directories
            @SuppressWarnings("unchecked")
            List<String> logins = (List<String>) jsParameters.get("login");
            for (String login : logins)
            {
                users.add(_userHelper.user2json(_userManager.getUser(userPopulationId, login), true));
            }
        }
        else if (userDirectoryId != null)
        {
            int count = parameters.getParameterAsInteger("limit", _DEFAULT_COUNT_VALUE);
            if (count == -1)
            {
                count = Integer.MAX_VALUE;
            }
            int offset = parameters.getParameterAsInteger("start", _DEFAULT_OFFSET_VALUE);
            
            users.addAll(_userHelper.users2json(_userManager.getUsersByDirectory(userPopulationId, userDirectoryId, count, offset, _getSearchParameters(source)), true));
        }
        else
        {
            // userDirectoryId = null => take account of all the user directories
            int count = parameters.getParameterAsInteger("limit", _DEFAULT_COUNT_VALUE);
            if (count == -1)
            {
                count = Integer.MAX_VALUE;
            }
            int offset = parameters.getParameterAsInteger("start", _DEFAULT_OFFSET_VALUE);
            
            users.addAll(_userHelper.users2json(_userManager.getUsers(userPopulationId, count, offset, _getSearchParameters(source)), true));
        }
    }
}
