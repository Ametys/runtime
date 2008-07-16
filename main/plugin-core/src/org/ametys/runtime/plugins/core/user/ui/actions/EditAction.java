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
package org.ametys.runtime.plugins.core.user.ui.actions;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.user.InvalidModificationException;
import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.UserHelper;
import org.ametys.runtime.user.UsersManager;


/**
 * Create or modify a user
 */
public class EditAction extends ServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting user's edition");
        }
        
        UsersManager u = (UsersManager) manager.lookup(UsersManager.ROLE);
        if (!(u instanceof ModifiableUsersManager))
        {
            getLogger().error("Users are not modifiable !");
            return null;
        }
        ModifiableUsersManager users = (ModifiableUsersManager) u;
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        Map<String, String> editParams = _getJdbcParameters(request);
        String login = editParams.get("login");
        
        try
        {
            if (!"new".equals(request.getParameter("mode")))
            {
                if (getLogger().isInfoEnabled())
                {
                    String userMessage = null;
                    String endMessage = "is updating information about user '" + login + "'";
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

                users.update(editParams);
            }
            else
            {
                if (getLogger().isInfoEnabled())
                {
                    String userMessage = null;
                    String endMessage = "is adding a new user '" + login + "'";
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
    
                users.add(editParams);
            }
        }
        catch (InvalidModificationException e)
        {
            if (e.getFields() != null && e.getFields().size() > 0)
            {
                StringBuffer fields = new StringBuffer();
                for (String field : e.getFields())
                {
                    fields.append(",");
                    fields.append(field);
                }
                
                Map<String, String> result = new HashMap<String, String>();
                result.put("error", fields.toString());
                return result;
            }
            else
            {
                return null;
            }
        }

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Ending user's edition");
        }

        return EMPTY_MAP;
    }
    
    private Map<String, String> _getJdbcParameters(Request request)
    {
        Map<String, String> editParams = new HashMap<String, String>();
        
        Enumeration<String> requestParameterNames = request.getParameterNames();
        while (requestParameterNames.hasMoreElements())
        {
            String parameterName = requestParameterNames.nextElement();
            if (parameterName.startsWith("field_"))
            {
                String jdbcParameterName = parameterName.substring(6);
                String value = request.getParameter(parameterName);
                editParams.put(jdbcParameterName, value);
            }
        }

        return editParams;
    }
}
