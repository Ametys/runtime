/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
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
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;


/**
 * This action creates a new profile with name given in request parameter, and return its id
 */
public class CreateGroupAction extends CurrentUserProviderServiceableAction
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
        GroupsManager gm = (GroupsManager) manager.lookup(GroupsManager.ROLE);
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
        
        Map<String, String> result = new HashMap<String, String>();
        result.put("id", ug.getId());
        return result;
    }
}
