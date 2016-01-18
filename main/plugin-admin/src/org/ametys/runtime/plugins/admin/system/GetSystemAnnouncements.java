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
package org.ametys.runtime.plugins.admin.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.runtime.plugins.admin.system.SystemHelper.SystemAnnouncement;

/**
 * Get the system announces from the system.xml file
 */
public class GetSystemAnnouncements extends ServiceableAction
{
    private SystemHelper _systemHelper;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
    }
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (_systemHelper == null)
        {
            _systemHelper = (SystemHelper) manager.lookup(SystemHelper.ROLE);
        }
        
        Map<String, Object> result = new HashMap<> ();
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        List<Map<String, String>> announcements = new ArrayList<> ();
        
        SystemAnnouncement systemAnnouncement = _systemHelper.readValues();
        
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
