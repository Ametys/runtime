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
package org.ametys.plugins.core.ui.groupdirectories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.ametys.core.group.directory.ModifiableGroupDirectory;

/**
 * Action for generating the modifiable group directories (all of it or by a given context)
 */
public class GetModifiableGroupDirectoriesAction extends ServiceableAction
{
    /** The DAO for group directories */
    private GroupDirectoryDAO _groupDirectoryDAO;
    /** The helper for the associations group directory/context */
    private GroupDirectoryContextHelper _directoryContextHelper;

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _groupDirectoryDAO = (GroupDirectoryDAO) smanager.lookup(GroupDirectoryDAO.ROLE);
        _directoryContextHelper = (GroupDirectoryContextHelper) smanager.lookup(GroupDirectoryContextHelper.ROLE);
    }
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        List<Object> groupDirectories = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        @SuppressWarnings("unchecked")
        List<String> contexts = (List<String>) jsParameters.get("contexts");
        if (contexts == null)
        {
            for (GroupDirectory gd : _groupDirectoryDAO.getGroupDirectories())
            {
                if (gd instanceof ModifiableGroupDirectory)
                {
                    groupDirectories.add(_groupDirectoryDAO.getGroupDirectory2Json(gd));
                }
            }
        }
        else
        {
            Set<String> groupDirectoryIds = _directoryContextHelper.getGroupDirectoriesOnContexts(new HashSet<>(contexts));
            for (String gdId : groupDirectoryIds)
            {
                GroupDirectory gd = _groupDirectoryDAO.getGroupDirectory(gdId);
                if (gd instanceof ModifiableGroupDirectory)
                {
                    groupDirectories.add(_groupDirectoryDAO.getGroupDirectory2Json(gd));
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("groupDirectories", groupDirectories);
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        
        return EMPTY_MAP;
    }

}
