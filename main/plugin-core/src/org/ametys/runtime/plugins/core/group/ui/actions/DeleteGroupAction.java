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

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.group.ModifiableGroupsManager;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;


/**
 * This action deletes the profile with id corresponding to the one specified in request
 */
public class DeleteGroupAction extends CurrentUserProviderServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting group removal");
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        String[] groudIds = request.getParameterValues("id");
        
        // Suppression du groupe
        GroupsManager gm = (GroupsManager) manager.lookup(GroupsManager.ROLE);
        if (!(gm instanceof ModifiableGroupsManager))
        {
            throw new IllegalArgumentException("The group manager used is not modifiable");
        }

        for (int i = 0; i < groudIds.length; i++)
        {
            if (getLogger().isInfoEnabled())
            {
                String userMessage = null;
                String endMessage = "is removing the group '" + groudIds[i] + "'";
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
            mgm.remove(groudIds[i]);
            
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Ending group removal");
            }
        }
        return EMPTY_MAP;
    }
}
