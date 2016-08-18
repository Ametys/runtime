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
        return _getPermissionsByProfile(new HashMap<>(), user, userGroups, object);
    }
    
    private Map<String, AccessResult> _getPermissionsByProfile(Map<String, AccessResult> permissionsByProfile, UserIdentity user, Set<GroupIdentity> userGroups, Object object)
    {
        // Compute the permissions on this object only
        Map<String, AccessResult> currentPermissionsByProfile = _profileAssignmentStorageEP.getPermissionsByProfile(user, userGroups, object);
        
        // Now take account of permissions on the children
        for (String currentAllowedProfile : currentPermissionsByProfile.keySet())
        {
            AccessResult childrenPermission = permissionsByProfile.get(currentAllowedProfile);
            if (childrenPermission == null || childrenPermission.equals(AccessResult.UNKNOWN))
            {
                // We can override the value
                permissionsByProfile.put(currentAllowedProfile, currentPermissionsByProfile.get(currentAllowedProfile));
            }
        }
        
        @SuppressWarnings("unchecked")
        T parent = _getParent((T) object);
        if (parent != null)
        {
            // Not root, pass to its parent
            return _getPermissionsByProfile(permissionsByProfile, user, userGroups, parent);
        }
        else
        {
            // We climbed up to the root object, the result is ready
            return permissionsByProfile;
        }
    }
    
    @Override
    public Map<UserIdentity, AccessResult> getPermissionsByUser(Set<String> profileIds, Object object)
    {
        return _getPermissionsByUser(new HashMap<>(), profileIds, object);
    }
    
    private Map<UserIdentity, AccessResult> _getPermissionsByUser(Map<UserIdentity, AccessResult> permissionsByUser, Set<String> profileIds, Object object)
    {
        // Compute the permissions on this object only
        Map<UserIdentity, AccessResult> currentPermissionsByUser = _profileAssignmentStorageEP.getPermissionsByUser(profileIds, object);
        
        // Now take account of permissions on the children
        for (UserIdentity currentUser : currentPermissionsByUser.keySet())
        {
            AccessResult childrenPermission = permissionsByUser.get(currentUser);
            if (childrenPermission == null || childrenPermission.equals(AccessResult.UNKNOWN))
            {
                // We can override the value
                permissionsByUser.put(currentUser, currentPermissionsByUser.get(currentUser));
            }
        }
        
        @SuppressWarnings("unchecked")
        T parent = _getParent((T) object);
        if (parent != null)
        {
            // Not root, pass to its parent
            return _getPermissionsByUser(permissionsByUser, profileIds, parent);
        }
        else
        {
            // We climbed up to the root object, the result is ready
            return permissionsByUser;
        }
    }
    
    @Override
    public Map<GroupIdentity, AccessResult> getPermissionsByGroup(Set<String> profileIds, Object object)
    {
        return _getPermissionsByGroup(new HashMap<>(), profileIds, object);
    }
    
    private Map<GroupIdentity, AccessResult> _getPermissionsByGroup(Map<GroupIdentity, AccessResult> permissionsByGroup, Set<String> profileIds, Object object)
    {
        // Compute the permissions on this object only
        Map<GroupIdentity, AccessResult> currentPermissionsByGroup = _profileAssignmentStorageEP.getPermissionsByGroup(profileIds, object);
        
        // Now take account of permissions on the children
        for (GroupIdentity currentGroup : currentPermissionsByGroup.keySet())
        {
            AccessResult childrenPermission = permissionsByGroup.get(currentGroup);
            if (childrenPermission == null || childrenPermission.equals(AccessResult.UNKNOWN))
            {
                // We can override the value
                permissionsByGroup.put(currentGroup, currentPermissionsByGroup.get(currentGroup));
            }
        }
        
        @SuppressWarnings("unchecked")
        T parent = _getParent((T) object);
        if (parent != null)
        {
            // Not root, pass to its parent
            return _getPermissionsByGroup(permissionsByGroup, profileIds, parent);
        }
        else
        {
            // We climbed up to the root object, the result is ready
            return permissionsByGroup;
        }
    }
}
