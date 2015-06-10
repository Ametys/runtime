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
package org.ametys.plugins.core.group;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.group.Group;
import org.ametys.core.group.GroupsManager;
import org.ametys.core.group.ModifiableGroupsManager;
import org.ametys.core.util.cocoon.AbstractCurrentUserProviderServiceableAction;

/**
 * This action creates a new profile with name given in request parameter, and return its id
 */
public class CreateGroupAction extends AbstractCurrentUserProviderServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting group creation");
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        String newGroupName = request.getParameter("name");
        if (newGroupName == null || newGroupName.trim().length() == 0)
        {
            throw new IllegalArgumentException("The new group name cannot be empty");
        }
        
        // Création du groupe
        String role = parameters.getParameter("groupsManagerRole", GroupsManager.ROLE);
        if (role.length() == 0)
        {
            role = GroupsManager.ROLE;
        }
        GroupsManager gm = (GroupsManager) manager.lookup(role);
        if (!(gm instanceof ModifiableGroupsManager))
        {
            throw new IllegalArgumentException("The group manager used is not modifiable");
        }

        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is adding a new group '" + newGroupName + "'";
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
        Group ug = mgm.add(newGroupName);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group creation");
        }
        
        Map<String, String> result = new HashMap<>();
        result.put("id", ug.getId());
        return result;
    }
}
