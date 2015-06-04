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
package org.ametys.runtime.plugins.core.administrator.system;

import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.runtime.ui.StaticClientSideElement;

/**
 * This client side element toggles controller according system announcement's state
 *
 */
public class SystemAnnouncementClientSideElement extends StaticClientSideElement implements Contextualizable
{
    private org.apache.cocoon.environment.Context _environmentContext;
    private SystemHelper _systemHelper;
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _environmentContext = (org.apache.cocoon.environment.Context) context.get(org.apache.cocoon.Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _systemHelper = (SystemHelper) smanager.lookup(SystemHelper.ROLE);
    }
    
    @Override
    public Map<String, Object> getParameters(Map<String, Object> contextualParameters)
    {
        Map<String, Object> parameters = super.getParameters(contextualParameters);
        
        String contextPath = _environmentContext.getRealPath("/");
        boolean isAvailable = _systemHelper.isSystemAnnouncementAvailable(contextPath);
        
        if ("true".equals(parameters.get("toggle-enabled")))
        {
            parameters.put("toggle-state", isAvailable);
        }
        
        parameters.put("available", isAvailable);
        
        return parameters;
    }
}
