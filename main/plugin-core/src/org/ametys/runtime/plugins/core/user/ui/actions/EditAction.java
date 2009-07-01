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
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.user.InvalidModificationException;
import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.UsersManager;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableAction;
import org.ametys.runtime.util.parameter.Errors;


/**
 * Create or modify a user
 */
public class EditAction extends CurrentUserProviderServiceableAction
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
        
        Map<String, String> editParams = _getRequestParameters(request);
        String login = editParams.get("login");
        
        try
        {
            if (!"new".equals(request.getParameter("mode")))
            {
                if (getLogger().isInfoEnabled())
                {
                    String userMessage = null;
                    String endMessage = "is updating information about user '" + login + "'";
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

                users.update(editParams);
            }
            else
            {
                if (getLogger().isInfoEnabled())
                {
                    String userMessage = null;
                    String endMessage = "is adding a new user '" + login + "'";
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
    
                users.add(editParams);
            }
        }
        catch (InvalidModificationException e)
        {
            Map<String, Errors> fieldErrors = e.getFieldErrors();
            
            if (fieldErrors != null && fieldErrors.size() > 0)
            {
                StringBuffer fields = new StringBuffer();
                for (String field : fieldErrors.keySet())
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
    
    @SuppressWarnings("unchecked")
    private Map<String, String> _getRequestParameters(Request request)
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
