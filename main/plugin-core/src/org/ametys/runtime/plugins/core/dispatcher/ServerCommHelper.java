/*
 *  Copyright 2013 Anyware Services
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
package org.ametys.runtime.plugins.core.dispatcher;

import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.lang.StringUtils;

import org.ametys.runtime.util.JSONUtils;

/**
 * Helper to get JS parameters from request
 *
 */
public class ServerCommHelper implements Component, Serviceable, Contextualizable
{
    /** The Avalon Role */
    public static final String ROLE = ServerCommHelper.class.getName();
    
    private JSONUtils _jsonUtils;
    private Context _context;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _jsonUtils = (JSONUtils) manager.lookup(JSONUtils.ROLE);
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    /**
     * Get the JS parameters as a Map object
     * @return The JS parameters
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getJsParameters()
    {
        Map<String, Object> objectModel = ContextHelper.getObjectModel(_context);
        
        Map<String, Object> jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        if (jsParameters != null && !jsParameters.isEmpty())
        {
            return jsParameters;
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        String parametersAsJSONString = request.getParameter("parameters");
        if (StringUtils.isNotEmpty(parametersAsJSONString))
        {
            jsParameters = _jsonUtils.convertJsonToMap(parametersAsJSONString);
        }
        
        // Set context parameters in request attributes
        String contextAsJSONString = request.getParameter("context.parameters");
        if (StringUtils.isNotEmpty(contextAsJSONString))
        {
            Map<String, Object> contextAsMap = _jsonUtils.convertJsonToMap(contextAsJSONString);
            for (String name : contextAsMap.keySet())
            {
                request.setAttribute(name, contextAsMap.get(name));
            }
        }
        
        return jsParameters;
    }
}
