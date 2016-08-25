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

import java.util.Collections;
import java.util.HashMap;
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
 * Abstract {@link AccessController} for a hierarchical type of object. 
 * @param <T> The class of a supported object, from which you can retrieve its parent with {@link #_getParent(Object)}
 */
public abstract class AbstractHierarchicalAccessController<T> implements AccessController, Component, Serviceable
{
    /** The extension point for the profile assignement storages */
    protected ProfileAssignmentStorageExtensionPoint _profileAssignmentStorageEP;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _profileAssignmentStorageEP = (ProfileAssignmentStorageExtensionPoint) manager.lookup(ProfileAssignmentStorageExtensionPoint.ROLE);
    }
    
    /**
     * Gets the parent of the object. Must return null when the object is the "root" (where "root" means here the root of the hierarchy of the access controller)
     * @param object The object
     * @return the parent of the object, or null if the object is the "root" of the hierarchy
     */
    protected abstract T _getParent(T object);
    
    @Override
    public Map<String, AccessResultContext> getPermissions(UserIdentity user, Set<GroupIdentity> userGroups, Set<String> profileIds, Object object)
    {
        Map<String, AccessResultContext> result = new HashMap<>();
        
        // TODO this is not the optimized version
        for (String profileId : profileIds)
        {
            result.put(profileId, _getPermission(user, userGroups, profileId, object));
        }
        
        return result;
    }
    
    private AccessResultContext _getPermission(UserIdentity user, Set<GroupIdentity> userGroups, String profileId, Object object)
    {
        AccessResultContext permission = _profileAssignmentStorageEP.getPermissions(user, userGroups, Collections.singleton(profileId), object).get(profileId);
        boolean unknown = AccessResult.UNKNOWN.equals(permission.getResult());
        
        @SuppressWarnings("unchecked")
        T parent = _getParent((T) object);
        if (unknown && parent != null)
        {
            // Unknown and not root, ask its parent
            return _getPermission(user, userGroups, profileId, parent);
        }
        else
        {
            // Return a known permission or unknown as we climbed up to the root object
            return permission;
        }
    }
    
    @Override
    public Map<String, AccessResult> getPermissionsByProfile(UserIdentity user, Set<GroupIdentity> userGroups, Object object)
    {
        // Get permissions on object itself
        Map<String, AccessResult> permissionsByProfile = _profileAssignmentStorageEP.getPermissionsByProfile(user, userGroups, object);

        // Add recursively the permission given by parent context
        @SuppressWarnings("unchecked")
        T parent = _getParent((T) object);
        if (parent != null)
        {
            Map<String, AccessResult> parentResult = getPermissionsByProfile(user, userGroups, parent);
            
            for (String profileId : parentResult.keySet())
            {
                if (!permissionsByProfile.containsKey(profileId) || AccessResult.UNKNOWN.equals(permissionsByProfile.get(profileId)))
                {
                    permissionsByProfile.put(profileId, parentResult.get(profileId));
                }
            }
        }
        
        return permissionsByProfile;
    }
    
    @Override
    public Map<UserIdentity, AccessResult> getPermissionsByUser(Set<String> profileIds, Object object)
    {
        // Get permissions on object itself
        Map<UserIdentity, AccessResult> permissionsByUser = _profileAssignmentStorageEP.getPermissionsByUser(profileIds, object);
        
        // Add recursively the permission given by parent context
        @SuppressWarnings("unchecked")
        T parent = _getParent((T) object);
        if (parent != null)
        {
            Map<UserIdentity, AccessResult> parentResult = getPermissionsByUser(profileIds, parent);
            
            for (UserIdentity user : parentResult.keySet())
            {
                if (!permissionsByUser.containsKey(user) || AccessResult.UNKNOWN.equals(permissionsByUser.get(user)))
                {
                    permissionsByUser.put(user, parentResult.get(user));
                }
            }
        }
        
        return permissionsByUser;
        
    }
    
    @Override
    public Map<GroupIdentity, AccessResult> getPermissionsByGroup(Set<String> profileIds, Object object)
    {
        // Get permissions on object itself
        Map<GroupIdentity, AccessResult> permissionsByGroup = _profileAssignmentStorageEP.getPermissionsByGroup(profileIds, object);
        
        // Add recursively the permission given by parent context
        @SuppressWarnings("unchecked")
        T parent = _getParent((T) object);
        if (parent != null)
        {
            Map<GroupIdentity, AccessResult> parentResult = getPermissionsByGroup(profileIds, parent);
            
            for (GroupIdentity group : parentResult.keySet())
            {
                if (!permissionsByGroup.containsKey(group) || AccessResult.UNKNOWN.equals(permissionsByGroup.get(group)))
                {
                    permissionsByGroup.put(group, parentResult.get(group));
                }
            }
        }
        
        return permissionsByGroup;
    }
}
