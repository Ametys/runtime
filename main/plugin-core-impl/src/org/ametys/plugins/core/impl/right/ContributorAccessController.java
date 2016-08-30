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
package org.ametys.plugins.core.impl.right;

import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.group.GroupIdentity;
import org.ametys.core.right.AccessController;
import org.ametys.core.right.ProfileAssignmentStorageExtensionPoint;
import org.ametys.core.user.UserIdentity;

/**
 * {@link AccessController} for a context objects (strings starting with "/contributor").
 */
public class ContributorAccessController implements AccessController, Component, Serviceable
{
    private static final String __PREFIX_CONTEXT = "/contributor";
    
    /** The extension point for the profile assignment storage */
    protected ProfileAssignmentStorageExtensionPoint _profileAssignmentStorageEP;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _profileAssignmentStorageEP = (ProfileAssignmentStorageExtensionPoint) manager.lookup(ProfileAssignmentStorageExtensionPoint.ROLE);
    }
    
    @Override
    public Map<String, AccessResultContext> getPermissions(UserIdentity user, Set<GroupIdentity> userGroups, Set<String> profileIds, Object object)
    {
        return _profileAssignmentStorageEP.getPermissions(user, userGroups, profileIds, object);
    }
    
    @Override
    public AccessResult getPermissionForAnonymous(Set<String> profileIds, Object object)
    {
        return _profileAssignmentStorageEP.getPermissionForAnonymous(profileIds, object);
    }
    
    @Override
    public AccessResult getPermissionForAnyConnectedUser(Set<String> profileIds, Object object)
    {
        return _profileAssignmentStorageEP.getPermissionForAnonymous(profileIds, object);
    }
    
    @Override
    public Map<String, AccessResult> getPermissionsByProfile(UserIdentity user, Set<GroupIdentity> userGroups, Object object)
    {
        return _profileAssignmentStorageEP.getPermissionsByProfile(user, userGroups, object);
    }
    
    @Override
    public Map<UserIdentity, AccessResult> getPermissionsByUser(Set<String> profileIds, Object object)
    {
        return _profileAssignmentStorageEP.getPermissionsByUser(profileIds, object);
    }
    
    @Override
    public Map<GroupIdentity, AccessResult> getPermissionsByGroup(Set<String> profileIds, Object object)
    {
        return _profileAssignmentStorageEP.getPermissionsByGroup(profileIds, object);
    }
    
    @Override
    public boolean isSupported(Object object)
    {
        return object instanceof String && ((String) object).startsWith(__PREFIX_CONTEXT);
    }
}
