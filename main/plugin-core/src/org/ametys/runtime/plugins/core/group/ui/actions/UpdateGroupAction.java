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
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.group.Group;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.group.ModifiableGroupsManager;
import org.ametys.runtime.user.UserHelper;


/**
 * This action modify the composition of the profile 'id' with the / sperated
 * list given in 'rights' (that are request parameters)
 */
public class UpdateGroupAction extends ServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting group modification");
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        String groupId = request.getParameter("id");
        String usersList = request.getParameter("objects");

        // Renommage du groupe
        GroupsManager gm = (GroupsManager) manager.lookup(GroupsManager.ROLE);
        if (!(gm instanceof ModifiableGroupsManager))
        {
            throw new IllegalArgumentException("The group manager used is not modifiable");
        }
        
        if (getLogger().isInfoEnabled())
        {
            String userMessage = null;
            String endMessage = "is modifying the group '" + groupId + "'.";
            if (UserHelper.isAdministrator(objectModel))
            {
                userMessage = "Administrator";
            }
            else
            {
                String currentUserLogin = UserHelper.getCurrentUser(objectModel);
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
                String endMessage = "is modifying a group '" + groupId + "' but the group does not exists.";
                if (UserHelper.isAdministrator(objectModel))
                {
                    userMessage = "Administrator";
                }
                else
                {
                    String currentUserLogin = UserHelper.getCurrentUser(objectModel);
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
            // Liste des users composant désormais le group
            String[] usersLogins = usersList.split("/");

            Group newUserGroup = new Group(ug.getId(), ug.getLabel());

            for (int i = 0; i < usersLogins.length; i++)
            {
                String login = usersLogins[i];
                if (login.trim().length() == 0)
                {
                    continue;
                }

                newUserGroup.addUser(login);
            }

            mgm.update(newUserGroup);
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending group modification");
        }

        return EMPTY_MAP;
    }
}
