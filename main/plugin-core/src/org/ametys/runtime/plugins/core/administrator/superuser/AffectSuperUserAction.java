/*
 *  Copyright 2010 Anyware Services
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
package org.ametys.runtime.plugins.core.administrator.superuser;

import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.right.InitializableRightsManager;
import org.ametys.runtime.right.RightsManager;

/**
 * Give all rights to given users on context '/application'
 */
public class AffectSuperUserAction extends ServiceableAction implements ThreadSafe
{
    /** The service manager */
    protected ServiceManager _sManager;
    /** The rights manager */
    protected RightsManager _rightsManager;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _sManager = smanager;
    }
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (_rightsManager == null)
        {
            _rightsManager = (RightsManager) _sManager.lookup(RightsManager.ROLE);
        }
        
        Map<String, Object> jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        List<String> logins = (List<String>) jsParameters.get("login");
        
        if (logins.size() == 0)
        {
            getLogger().error("No login to initialize.");
            return null;
        }
        
        if (!(_rightsManager instanceof InitializableRightsManager))
        {
            getLogger().error("Right manager is not initializable !");
            return null;
        }
        
        InitializableRightsManager initRightsManager = (InitializableRightsManager) _rightsManager;
        for (String login : logins)
        {
            initRightsManager.grantAllPrivileges(login, "");
        }
        
        return EMPTY_MAP;
    }
}
