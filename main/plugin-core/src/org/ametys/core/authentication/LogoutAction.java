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
package org.ametys.core.authentication;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang.StringUtils;

import org.ametys.core.user.CurrentUserProvider;

/**
 * If the user is loggedin, it will be loggouted and his session destroyed
 * If the current credential provider does not support loggout, it will silently be ignore
 */
public class LogoutAction extends ServiceableAction
{
    /** The current user provider */
    protected CurrentUserProvider _currentUserProvider;

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        
        _currentUserProvider = (CurrentUserProvider) smanager.lookup(CurrentUserProvider.ROLE);
    }
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled() && _currentUserProvider.getUser() != null)
        {
            getLogger().debug("User " + _currentUserProvider.getUser() + " logs out");
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession();
        if (session != null)
        {
            session.invalidate();
        }
        
        if (_currentUserProvider.canLogout())
        {
            _currentUserProvider.logout(redirector);
        }
        
        redirector.globalRedirect(true, StringUtils.defaultIfEmpty(request.getContextPath(), "/"));
        
        return EMPTY_MAP;
    }

}
