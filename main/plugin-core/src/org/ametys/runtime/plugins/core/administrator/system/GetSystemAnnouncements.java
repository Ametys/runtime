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
package org.ametys.runtime.plugins.core.administrator.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.cocoon.JSonReader;
import org.ametys.runtime.plugins.core.administrator.system.SystemHelper.SystemAnnouncement;

/**
 * Get the system announces from the system.xml file
 */
public class GetSystemAnnouncements extends ServiceableAction implements Contextualizable
{
    private SystemHelper _systemHelper;
    
    private org.apache.cocoon.environment.Context _environmentContext;

    @Override
    public void contextualize(Context context) throws ContextException
    {
        _environmentContext = (org.apache.cocoon.environment.Context) context.get(org.apache.cocoon.Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _systemHelper = (SystemHelper) serviceManager.lookup(SystemHelper.ROLE);
    }
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map<String, Object> result = new HashMap<> ();
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        List<Map<String, String>> announcements = new ArrayList<> ();
        
        String contextPath = _environmentContext.getRealPath("/");
        SystemAnnouncement systemAnnouncement = _systemHelper.readValues(contextPath);
        
        Map<String, String> messages = systemAnnouncement.getMessages();
        
        for (String lang : messages.keySet())
        {
            Map<String, String> message = new HashMap<> ();
            message.put("language", lang);
            message.put("message", messages.get(lang));
            
            announcements.add(message);
        }
        
        result.put("announcements", announcements);
        
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        return EMPTY_MAP;
    }
}
