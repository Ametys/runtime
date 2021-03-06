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
package org.ametys.runtime.plugins.admin;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang3.BooleanUtils;

/**
 * Action for reloading application
 */
public class RestartAction extends AbstractAction implements ThreadSafe
{
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        // Set the request attribute for Cocoon reloading
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Positionning org.ametys.runtime.reload=true for Cocoon reloading");
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute("org.ametys.runtime.reload", true);
        
        if (BooleanUtils.toBoolean(request.getParameter("normalMode")))
        {
            request.setAttribute("org.ametys.runtime.reload.normalMode", true);
        }
        else if (BooleanUtils.toBoolean(request.getParameter("safeMode")))
        {
            request.setAttribute("org.ametys.runtime.reload.safeMode", true);
        }
        
        return EMPTY_MAP;
    }
}
