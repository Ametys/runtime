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
package org.ametys.plugins.core.ui.right;

import java.util.Collections;
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
import org.ametys.core.ui.UIToolsFactoriesManager;
import org.ametys.core.ui.right.ProfileAssignmentsToolClientSideElement;

/**
 * Action for generating the contexts of right assignments
 */
public class GetRightAssignmentContextsAction extends ServiceableAction
{
    /** The ribbon controls manager */
    protected UIToolsFactoriesManager _uiToolsFactoriesManager;

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _uiToolsFactoriesManager = (UIToolsFactoriesManager) smanager.lookup(UIToolsFactoriesManager.ROLE);
    }
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        ProfileAssignmentsToolClientSideElement profileAssignmentTool = (ProfileAssignmentsToolClientSideElement) _uiToolsFactoriesManager.getExtension("uitool-profile-assignment");
        Map<String, Object> result = Collections.singletonMap("contexts", profileAssignmentTool.getContextsAsJson());
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        
        return EMPTY_MAP;
    }
}
