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
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.authentication.AuthenticateAction;
import org.ametys.core.right.RightManager;
import org.ametys.core.right.RightManager.RightResult;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.runtime.plugin.PluginsManager;

/**
 * Cocoon action for authenticating users in the administration workspace. 
 */
public class AdminAuthenticateAction extends AuthenticateAction
{
    /** The right context for administration area */
    public static final String ADMIN_RIGHT_CONTEXT = "/admin";
    /** The current user provider */
    protected  CurrentUserProvider _currentUserProvider;
    /** The runtime rights manager */
    protected RightManager _rightManager;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _currentUserProvider = (CurrentUserProvider) smanager.lookup(CurrentUserProvider.ROLE);
        _rightManager = (RightManager) manager.lookup(RightManager.ROLE);
    }
    
    @Override
    protected Set<String> _getUserPopulationsOnContext(String context)
    {
        if (PluginsManager.getInstance().isSafeMode() || _userPopulationDAO.getEnabledUserPopulations(false).isEmpty())
        {
            String adminPopulationId = _userPopulationDAO.getAdminPopulation().getId();
            return Collections.singleton(adminPopulationId);
        }
        else
        {
            return super._getUserPopulationsOnContext(context);
        }
    }

    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        boolean wasConnected = _currentUserProvider.getUser() != null;
        
        Map act = super.act(redirector, resolver, objectModel, source, parameters);

        // When the user just connected, letting the HasNotAdminRightAction throw an AccessDeniedException will clear cookie, and the user will not be really connected
        // So we do a redirect to here that will store the cookie and then we would let the AccessDeniedException plays
        if (_currentUserProvider.getUser() != null && _rightManager.currentUserHasRight("Runtime_Rights_Admin_Access", ADMIN_RIGHT_CONTEXT) != RightResult.RIGHT_ALLOW && !wasConnected)
        {
            Request request = ObjectModelHelper.getRequest(objectModel);
            String queryString = request.getQueryString();
            redirector.globalRedirect(true, request.getRequestURI() + (queryString != null ? "?" + queryString : ""));
        }
        
        return act;
    }
}
