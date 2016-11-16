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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.core.group.GroupDirectoryContextHelper;
import org.ametys.core.group.GroupDirectoryDAO;
import org.ametys.core.group.directory.GroupDirectory;

/**
 * Get groups 
 *
 */
public class GroupSearchAction extends ServiceableAction
{
    private static final int _DEFAULT_COUNT_VALUE = 100;
    private static final int _DEFAULT_OFFSET_VALUE = 0;
    
    private GroupDirectoryDAO _groupDirectoryDAO;
    private GroupDirectoryContextHelper _directoryContextHelper;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _groupDirectoryDAO = (GroupDirectoryDAO) smanager.lookup(GroupDirectoryDAO.ROLE);
        _directoryContextHelper = (GroupDirectoryContextHelper) smanager.lookup(GroupDirectoryContextHelper.ROLE);
    }

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        List<Map<String, Object>> groups = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        List<String> contexts = (List<String>) jsParameters.get("contexts");
        
        if (contexts != null)
        {
            _searchGroupsByContext(groups, jsParameters, source, parameters, contexts);
        }
        else
        {
            String groupDirectoryId = (String) jsParameters.get("groupDirectoryId");
            _searchGroupsByDirectory(groups, jsParameters, source, parameters, groupDirectoryId);
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
    
    private void _searchGroupsByContext(List<Map<String, Object>> groups, Map<String, Object> jsParameters, String source, Parameters parameters, List<String> contexts)
    {
        @SuppressWarnings("unchecked")
        List<String> groupIds = (List) jsParameters.get("id");
        
        if (groupIds != null)
        {
            for (String groupDirectoryId : _directoryContextHelper.getGroupDirectoriesOnContexts(new HashSet<>(contexts)))
            {
                GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
                for (String groupId : groupIds)
                {
                    groups.add(groupDirectory.group2JSON(groupId));
                }
            }
        }
        else if (jsParameters.get("value") != null)
        {
            String[] values = ((String) jsParameters.get("value")).split(",");
            for (String value : values)
            {
                String[] parts = value.split("#");
                GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(parts[1]);
                groups.add(groupDirectory.group2JSON(parts[0]));
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
            
            for (String groupDirectoryId : _directoryContextHelper.getGroupDirectoriesOnContexts(new HashSet<>(contexts)))
            {
                GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
                groups.addAll(groupDirectory.groups2JSON(count, offset, _getSearchParameters(source)));
            }
        }
    }
    
    private void _searchGroupsByDirectory(List<Map<String, Object>> groups, Map<String, Object> jsParameters, String source, Parameters parameters, String groupDirectoryId)
    {
        @SuppressWarnings("unchecked")
        List<String> groupIds = (List) jsParameters.get("id");
        
        if (groupIds != null)
        {
            GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
            for (String groupId : groupIds)
            {
                groups.add(groupDirectory.group2JSON(groupId));
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
            
            GroupDirectory groupDirectory = _groupDirectoryDAO.getGroupDirectory(groupDirectoryId);
            groups.addAll(groupDirectory.groups2JSON(count, offset, _getSearchParameters(source)));
        }
    }

}
