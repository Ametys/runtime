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
import java.util.List;
import java.util.Map;

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
        
        String context = (String) jsParameters.get("context");
        
        if (context != null)
        {
            // search over the populations of the given context
            _searchUsersByContext(users, jsParameters, source, parameters, context);
        }
        else
        {
            // search over the given population
            String userPopulationId = (String) jsParameters.get("userPopulationId");
            int userDirectoryIndex = -1;
            if (jsParameters.get("userDirectoryIndex") != null)
            {
                userDirectoryIndex = (int) jsParameters.get("userDirectoryIndex");
            }
            _searchUsersByPopulation(users, jsParameters, source, parameters, userPopulationId, userDirectoryIndex);
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
    
    private void _searchUsersByContext(List<Map<String, Object>> users, Map<String, Object> jsParameters, String source, Parameters parameters, String context)
    {
        if (jsParameters.get("login") != null)
        {
            @SuppressWarnings("unchecked")
            List<String> logins = (List<String>) jsParameters.get("login");
            for (String login : logins)
            {
                users.add(_userHelper.user2Map(_userManager.getUserByContext(context, login)));
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
            
            users.addAll(_userHelper.users2MapList(_userManager.getUsersByContext(context, count, offset, _getSearchParameters(source))));
        }
    }
    
    private void _searchUsersByPopulation(List<Map<String, Object>> users, Map<String, Object> jsParameters, String source, Parameters parameters, String userPopulationId, int userDirectoryIndex)
    {
        if (jsParameters.get("login") != null && userDirectoryIndex != -1)
        {
            @SuppressWarnings("unchecked")
            List<String> logins = (List<String>) jsParameters.get("login");
            for (String login : logins)
            {
                users.add(_userHelper.user2Map(_userManager.getUserByDirectory(userPopulationId, userDirectoryIndex, login)));
            }
        }
        else if (jsParameters.get("login") != null)
        {
            // userDirectoryIndex = -1 => take account of all the user directories
            @SuppressWarnings("unchecked")
            List<String> logins = (List<String>) jsParameters.get("login");
            for (String login : logins)
            {
                users.add(_userHelper.user2Map(_userManager.getUser(userPopulationId, login)));
            }
        }
        else if (userDirectoryIndex != -1)
        {
            int count = parameters.getParameterAsInteger("limit", _DEFAULT_COUNT_VALUE);
            if (count == -1)
            {
                count = Integer.MAX_VALUE;
            }
            int offset = parameters.getParameterAsInteger("start", _DEFAULT_OFFSET_VALUE);
            
            users.addAll(_userHelper.users2MapList(_userManager.getUsersByDirectory(userPopulationId, userDirectoryIndex, count, offset, _getSearchParameters(source))));
        }
        else
        {
            // userDirectoryIndex = -1 => take account of all the user directories
            int count = parameters.getParameterAsInteger("limit", _DEFAULT_COUNT_VALUE);
            if (count == -1)
            {
                count = Integer.MAX_VALUE;
            }
            int offset = parameters.getParameterAsInteger("start", _DEFAULT_OFFSET_VALUE);
            
            users.addAll(_userHelper.users2MapList(_userManager.getUsers(userPopulationId, count, offset, _getSearchParameters(source))));
        }
    }
}
