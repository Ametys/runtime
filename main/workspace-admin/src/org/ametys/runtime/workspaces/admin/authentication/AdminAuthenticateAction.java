/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.runtime.workspaces.admin.authentication;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.authentication.AuthenticateAction;
import org.ametys.runtime.plugin.PluginsManager;

/**
 * Cocoon action for authenticating users in the administration workspace. 
 */
public class AdminAuthenticateAction extends AuthenticateAction
{
    /** The request attribute name for telling that super user is logged in. */
    public static final String REQUEST_ATTRIBUTE_SUPER_USER = "Runtime:SuperUser";

    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        super.act(redirector, resolver, objectModel, source, parameters);
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        if ("true".equals(request.getAttribute(REQUEST_AUTHENTICATED)))
        {
            request.setAttribute(REQUEST_ATTRIBUTE_SUPER_USER, Boolean.TRUE);
        }
        return EMPTY_MAP;
    }
    
    @Override
    protected List<String> _getUserPopulationsOnContext(String context)
    {
        if (PluginsManager.getInstance().isSafeMode() || _userPopulationDAO.getEnabledUserPopulations(false).isEmpty())
        {
            String adminPopulationId = _userPopulationDAO.getAdminPopulation().getId();
            return Collections.singletonList(adminPopulationId);
        }
        else
        {
            return super._getUserPopulationsOnContext(context);
        }
    }

}
