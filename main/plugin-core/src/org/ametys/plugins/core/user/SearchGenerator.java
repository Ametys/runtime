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
package org.ametys.plugins.core.user;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.core.user.UsersManager;

/**
 * Generates the result of a search in the users. 
 */
public class SearchGenerator extends ServiceableGenerator
{
    private static final int _DEFAULT_COUNT_VALUE = 100;
    private static final int _DEFAULT_OFFSET_VALUE = 0;

    @SuppressWarnings("unchecked")
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Map<String, Object> jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        // Get the wanted UsersManager avalon role, defaults to runtime-declared UsersManager.
        String role = parameters.getParameter("usersManagerRole", UsersManager.ROLE);
        if (role.length() == 0)
        {
            role = UsersManager.ROLE;
        }
        
        UsersManager usersManager = null;
        
        try
        {
            usersManager = (UsersManager) manager.lookup(role);
            
            contentHandler.startDocument();
            XMLUtils.startElement(contentHandler, "Search");
            
            if (jsParameters.get("login") != null)
            {
                XMLUtils.startElement(contentHandler, "users");
                List<String> logins = (List<String>) jsParameters.get("login");
                for (String login : logins)
                {
                    usersManager.saxUser(login, contentHandler);
                }
                XMLUtils.endElement(contentHandler, "users");
            }
            else
            {
                int count = parameters.getParameterAsInteger("limit", _DEFAULT_COUNT_VALUE);
                if (count == -1)
                {
                    count = Integer.MAX_VALUE;
                }

                int offset = parameters.getParameterAsInteger("start", _DEFAULT_OFFSET_VALUE);
                
                usersManager.toSAX(contentHandler, count, offset, _getSearchParameters());
            }
            
            XMLUtils.endElement(contentHandler, "Search");
            contentHandler.endDocument();
        }
        catch (ServiceException e)
        {
            getLogger().error("Error looking up UsersManager of role " + role, e);
            throw new ProcessingException("Error looking up UsersManager of role " + role, e);
        }
        finally
        {
            manager.release(usersManager);
        }
    }
    
    /**
     * Get the search parameters
     * @return the search parameters
     */
    protected Map<String, String> _getSearchParameters ()
    {
        Map<String, String> params = new HashMap<>();
        params.put("pattern", source);
        return params;
    }
}
