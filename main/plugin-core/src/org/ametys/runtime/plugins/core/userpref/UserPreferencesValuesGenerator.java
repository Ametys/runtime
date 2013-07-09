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
package org.ametys.runtime.plugins.core.userpref;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.cocoon.AbstractCurrentUserProviderServiceableGenerator;

/**
 * SAX user preferences values
 */
public class UserPreferencesValuesGenerator extends AbstractCurrentUserProviderServiceableGenerator
{
    /** The user preferences manager. */
    protected UserPreferencesManager _userPrefManager;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _userPrefManager = (UserPreferencesManager) serviceManager.lookup(UserPreferencesManager.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String storageContext = request.getParameter("prefContext");
        if (storageContext == null)
        {
            storageContext = parameters.getParameter("prefContext", null);
        }
        String username = getUsername();
        Map<String, String> contextVars = getContextVars(request);
        
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "userprefs");
        
        try
        {
            Map<String, String> prefValues = _userPrefManager.getUnTypedUserPrefs(username, storageContext, contextVars);
            
            Map<String, Object> jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
            List<String> userprefs = null;
            
            if (jsParameters != null)
            {
                userprefs = (List<String>) jsParameters.get("userprefs");
            }
            
            if (userprefs != null && userprefs.size() > 0)
            {
                for (String userpref : userprefs)
                {
                    if (prefValues.containsKey(userpref))
                    {
                        XMLUtils.createElement(contentHandler, userpref, prefValues.get(userpref));
                    }
                }
            }
            else
            {
                for (String name : prefValues.keySet())
                {
                    XMLUtils.createElement(contentHandler, name, prefValues.get(name));
                }
            }
        }
        catch (UserPreferencesException e)
        {
            getLogger().error("Cannot get user preferences.", e);
            throw new ProcessingException("Cannot get user preferences.", e);
        }
        
        XMLUtils.endElement(contentHandler, "userprefs");
        contentHandler.endDocument();
    }
    
    /**
     * Get the user name in the user manager.
     * @return the user name (login).
     */
    protected String getUsername()
    {
        return parameters.getParameter("username", _getCurrentUser());
    }
    
    /**
     * Get the preferences context.
     * @param request the request.
     * @return the preferences context as a Map.
     */
    protected Map<String, String> getContextVars(Request request)
    {
        return Collections.emptyMap();
    }
    
}
