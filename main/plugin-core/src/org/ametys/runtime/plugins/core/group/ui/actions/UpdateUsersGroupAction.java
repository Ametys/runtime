/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.plugins.core.group.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.group.Group;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.group.ModifiableGroupsManager;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;

/**
 * Add or remove users from an existing group
 */
public class UpdateUsersGroupAction extends CurrentUserProviderServiceableAction
{
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        // Renommage du groupe
        String role = parameters.getParameter("groupsManagerRole", GroupsManager.ROLE);
        if (role.length() == 0)
        {
            role = GroupsManager.ROLE;
        }
        
        GroupsManager groupsManager;
        try
        {
            groupsManager = (GroupsManager) manager.lookup(role);
        }
        catch (ServiceException e)
        {
            throw new IllegalStateException(e);
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        String groupId = request.getParameter("id");
        String[] usersList = request.getParameterValues("objects");
        boolean add = parameters.getParameterAsBoolean("add", true);
        
        if (!(groupsManager instanceof ModifiableGroupsManager))
        {
            throw new IllegalArgumentException("The group manager used is not modifiable");
        }
        
        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is modifying the group '" + groupId + "'.";
            if (_isSuperUser())
            {
                userMessage = "Administrator";
            }
            else
            {
                String currentUserLogin = _getCurrentUser();
                userMessage = "User '" + currentUserLogin + "'";
            }
            
            getLogger().info(userMessage + " " + endMessage);
        }
        
        ModifiableGroupsManager mgm = (ModifiableGroupsManager) groupsManager;
        Group ug = mgm.getGroup(groupId);
        if (ug == null)
        {
            if (getLogger().isWarnEnabled())
            {
                String userMessage = null;
                String endMessage = "is modifying a group '" + groupId + "' but the group does not exists.";
                if (_isSuperUser())
                {
                    userMessage = "Administrator";
                }
                else
                {
                    String currentUserLogin = _getCurrentUser();
                    userMessage = "User '" + currentUserLogin + "'";
                }
                
                getLogger().warn(userMessage + " " + endMessage);
            }
            
            Map<String, String> result = new HashMap<String, String>();
            result.put("message", "missing");
            return result;
        }
        else
        {
            for (int i = 0; usersList != null && i < usersList.length; i++)
            {
                if (add)
                {
                    ug.addUser(usersList[i]);
                }
                else
                {
                    ug.removeUser(usersList[i]);
                }
            }
        }
        mgm.update(ug);
        
        return EMPTY_MAP;
    }
}
