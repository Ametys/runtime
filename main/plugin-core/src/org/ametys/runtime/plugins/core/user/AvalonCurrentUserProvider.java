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
package org.ametys.runtime.plugins.core.user;

import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;

import org.ametys.runtime.authentication.AuthenticateAction;
import org.ametys.runtime.user.CurrentUserProvider;
import org.ametys.runtime.workspaces.admin.authentication.AdminAuthenticateAction;

/**
 * Provides the current user by searching into the object model.<br>
 * If not found a default login is used (read from XML configuration).
 */
public class AvalonCurrentUserProvider extends AbstractLogEnabled implements CurrentUserProvider, Contextualizable, ThreadSafe
{
    /** Avalon context. */
    protected Context _context;
    
    public void contextualize(Context context) throws ContextException
    {
        _context = context;        
    }
    
    
    public boolean isSuperUser()
    {
        try
        {
            Map objectModel = ContextHelper.getObjectModel(_context);
            Request request = ObjectModelHelper.getRequest(objectModel);
            return request.getAttribute(AdminAuthenticateAction.REQUEST_ATTRIBUTE_SUPER_USER) != null;
        }
        catch (Exception e)
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Unable to retrieve object model", e);
            }
        }
        
        // No object model, no super user
        return false;
    }
    
    public String getUser()
    {
        if (isSuperUser())
        {
            throw new IllegalStateException("The current user is the super user");
        }
        
        String user = null;
        
        try
        {
            Map objectModel = ContextHelper.getObjectModel(_context);
            Request request = ObjectModelHelper.getRequest(objectModel);
            Session session = request.getSession(false);
            
            if (session != null)
            {
                user = (String) session.getAttribute(AuthenticateAction.SESSION_USERLOGIN);
            }
        }
        catch (Exception e)
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Unable to retrieve current authenticated user, fallback to default user", e);
            }
        }
        
        if (user == null)
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("There is no current user");
            }
        }
        else
        {
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Providing current user as: " + user);
            }
        }
        
        return user;
    }
}
