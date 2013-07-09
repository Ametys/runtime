/*
 *  Copyright 2012 Anyware Services
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
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.group.Group;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.group.ModifiableGroupsManager;
import org.ametys.runtime.util.cocoon.AbstractCurrentUserProviderServiceableAction;


/**
 * This action rename the profile 'id' with 'name' (that are request parameter)
 */
public class RenameGroupAction extends AbstractCurrentUserProviderServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting group renaming");
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        String newGroupName = request.getParameter("name");
        if (newGroupName == null || newGroupName.trim().length() == 0)
        {
            throw new IllegalArgumentException("The new group name cannot be empty");
        }
        
        String groupId = request.getParameter("id");
        
        // Renommage du groupe
        GroupsManager gm = (GroupsManager) manager.lookup(GroupsManager.ROLE);
        if (!(gm instanceof ModifiableGroupsManager))
        {
            throw new IllegalArgumentException("The group manager used is not modifiable");
        }

        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is renaming the group '" + groupId + "' to '" + newGroupName + "'";
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
        
        ModifiableGroupsManager mgm = (ModifiableGroupsManager) gm;
        Group ug = mgm.getGroup(groupId);
        if (ug == null)
        {
            if (getLogger().isWarnEnabled())
            {
                String userMessage = null;
                String endMessage = "is renaming a group '" + groupId + "' but the group does not exists";
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
            ug.setLabel(newGroupName);
            mgm.update(ug);
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group renaming");
        }
        
        return EMPTY_MAP;
    }
}
