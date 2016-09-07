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
package org.ametys.core.right;

import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.ObservationConstants;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.observation.Event;
import org.ametys.core.observation.Observer;
import org.ametys.core.user.UserIdentity;

/**
 * This observer listens for events which may have an impact on the storage of assignments
 *
 */
public class ProfileAssignmentStorageObserver implements Serviceable, Observer
{
    private ProfileAssignmentStorageExtensionPoint _profileAssignmentStorageEP;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _profileAssignmentStorageEP = (ProfileAssignmentStorageExtensionPoint) manager.lookup(ProfileAssignmentStorageExtensionPoint.ROLE);
    }
    
    @Override
    public boolean supports(Event event)
    {
        return event.getId().equals(ObservationConstants.EVENT_PROFILE_DELETED)
                || event.getId().equals(ObservationConstants.EVENT_USER_DELETED)
                || event.getId().equals(ObservationConstants.EVENT_GROUP_DELETED);
    }

    @Override
    public int getPriority(Event event)
    {
        return MAX_PRIORITY;
    }

    @Override
    public void observe(Event event, Map<String, Object> transientVars) throws Exception
    {
        String id = event.getId();
        
        if (id.equals(ObservationConstants.EVENT_PROFILE_DELETED))
        {
            _onProfileRemoved (event);
        }
        else if (id.equals(ObservationConstants.EVENT_USER_DELETED))
        {
            _onUserRemoved(event);
        }
        else if (id.equals(ObservationConstants.EVENT_GROUP_DELETED))
        {
            _onGroupRemoved(event);
        }
    }
    
    private void _onProfileRemoved (Event event)
    {
        Map<String, Object> arguments = event.getArguments();
        
        Profile profile = (Profile) arguments.get(ObservationConstants.ARGS_PROFILE);
        _profileAssignmentStorageEP.getExtensionsIds().stream()
            .map(_profileAssignmentStorageEP::getExtension)
            .forEach(pas -> pas.removeProfile(profile.getId()));
    }
    
    private void _onUserRemoved (Event event)
    {
        Map<String, Object> arguments = event.getArguments();
        
        UserIdentity user = (UserIdentity) arguments.get(ObservationConstants.ARGS_USER);
        _profileAssignmentStorageEP.getExtensionsIds().stream()
            .map(_profileAssignmentStorageEP::getExtension)
            .forEach(pas -> pas.removeUser(user));
    }
    
    private void _onGroupRemoved (Event event)
    {
        Map<String, Object> arguments = event.getArguments();
        
        GroupIdentity group = (GroupIdentity) arguments.get(ObservationConstants.ARGS_GROUP);
        _profileAssignmentStorageEP.getExtensionsIds().stream()
            .map(_profileAssignmentStorageEP::getExtension)
            .forEach(pas -> pas.removeGroup(group));
    }
}
