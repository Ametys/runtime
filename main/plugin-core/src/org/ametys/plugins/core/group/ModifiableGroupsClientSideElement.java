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
package org.ametys.plugins.core.group;

import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.core.group.GroupsManager;
import org.ametys.core.group.ModifiableGroupsManager;
import org.ametys.core.ui.StaticClientSideElement;

/**
 * This implementation creates a control only available if the groups manager is a {@link ModifiableGroupsManager}
 *
 */
public class ModifiableGroupsClientSideElement extends StaticClientSideElement
{
    /** The groups manager */
    protected GroupsManager _groupsManager;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _groupsManager = (GroupsManager) smanager.lookup(GroupsManager.ROLE);
    }
    
    @Override
    public Script getScript(Map<String, Object> contextParameters)
    {
        if (_groupsManager instanceof ModifiableGroupsManager)
        {
            return super.getScript(contextParameters);
        }
        
        return null;
    }
}
