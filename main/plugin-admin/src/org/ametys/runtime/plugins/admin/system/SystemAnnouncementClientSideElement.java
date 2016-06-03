/*
 *  Copyright 2015 Anyware Services
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

import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.core.ui.StaticClientSideElement;

/**
 * This client side element toggles controller according system announcement's state
 */
public class SystemAnnouncementClientSideElement extends StaticClientSideElement
{
    private SystemHelper _systemHelper;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _systemHelper = (SystemHelper) smanager.lookup(SystemHelper.ROLE);
    }
    
    @Override
    public List<Script> getScripts(boolean ignoreRights, Map<String, Object> contextParameters)
    {
        List<Script> scripts = super.getScripts(ignoreRights, contextParameters);
        if (scripts.size() > 0)
        {
            Map<String, Object> parameters = scripts.get(0).getParameters();
            
            boolean isAvailable = _systemHelper.isSystemAnnouncementAvailable();
            
            if ("true".equals(parameters.get("toggle-enabled")))
            {
                parameters.put("toggle-state", isAvailable);
            }
            
            parameters.put("available", isAvailable);
        }
        return scripts;
    }
}
