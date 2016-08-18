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
import java.util.Set;

import org.ametys.core.group.GroupIdentity;
import org.ametys.core.user.UserIdentity;

/**
 * This interface is for computing the rights a user has.
 */
public interface AccessController
{
    /**
     * The access result of the method {@link AccessController#getPermissions(UserIdentity, Set, Set, Object)}
     */
    public static enum AccessResult
    {
        /* Note: the order of the values are important ! */
        
        /** If the user is directly denied */
        USER_DENIED,
        /** If the user is directly allowed */
        USER_ALLOWED,
        /** If the user is denied through its groups and not directly */
        GROUP_DENIED,
        /** If the user is allowed through its groups and not directly */
        GROUP_ALLOWED,
        /** If the user is denied because one of the profiles is denied for any connected user */
        ANY_CONNECTED_DENIED,
        /** If the user is allowed because one of the profiles is allowed for any connected user */
        ANY_CONNECTED_ALLOWED,
        /** If the user is denied because one of the profiles is denied for any anonymous user */
        ANONYMOUS_DENIED,
        /** If the user is allowed because one of the profiles is allowed for any anonymous user */
        ANONYMOUS_ALLOWED,
        /** Cannot determine */
        UNKNOWN
    }
    
    /**
     * Object wrapping an {@link AccessResult} and a set of {@link GroupIdentity} if the result is {@link AccessResult#GROUP_ALLOWED} or {@link AccessResult#GROUP_DENIED}
     */
    public class AccessResultContext
    {

        private AccessResult _result;
        private Set<GroupIdentity> _groups;

        /**
         * Constructor
         * @param result the access result
         * @param groups the groups. Can be null
         */
        public AccessResultContext(AccessResult result, Set<GroupIdentity> groups)
        {
            _result = result;
            _groups = groups;
        }
        
        /**
         * Gets the access result
         * @return the access result
         */
        public AccessResult getResult()
        {
            return _result;
        }
        
        /**
         * Gets the groups
         * @return the groups
         */
        public Set<GroupIdentity> getGroups()
        {
            return _groups;
        }
        
        @Override
        public String toString()
        {
            return "{result: " + _result + ", groups: " + _groups + "}";
        }
        
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_groups == null) ? 0 : _groups.hashCode());
            result = prime * result + ((_result == null) ? 0 : _result.hashCode());
            return result;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            AccessResultContext other = (AccessResultContext) obj;
            if (_groups == null)
            {
                if (other._groups != null)
                {
                    return false;
                }
            }
            else if (!_groups.equals(other._groups))
            {
                return false;
            }
            if (_result != other._result)
            {
                return false;
            }
            return true;
        }
    }
    
    /**
     * Gets the kind of access a user has on an object for each profile
     * @param user The user. Cannot be null.
     * @param userGroups The groups the user belongs to
     * @param profileIds The ids of the profiles of the user
     * @param object The context object to check the access
     * @return the kind of access (through an {@link AccessResultContext} object) a user has on an object for each profile
     */
    public Map<String, AccessResultContext> getPermissions(UserIdentity user, Set<GroupIdentity> userGroups, Set<String> profileIds, Object object);
    
    /**
     * Gets the kind of access a user has on an object for all profiles
     * @param user The user. Cannot be null.
     * @param userGroups The groups the user belongs to
     * @param object The context object to check the access
     * @return the kind of access a user has on an object for all profiles
     */
    public Map<String, AccessResult> getPermissionsByProfile(UserIdentity user, Set<GroupIdentity> userGroups, Object object);
    
    /**
     * Gets the permission by user only on an object, according to the given profiles. It does not take account of the groups of the user, etc.
     * @param profileIds The ids of the profiles
     * @param object The object
     * @return the permission by user only on an object, according to the given profiles
     */
    public Map<UserIdentity, AccessResult> getPermissionsByUser(Set<String> profileIds, Object object);
    
    /**
     * Gets the permission by group only on an object, according to the given profiles.
     * @param profileIds The ids of the profiles
     * @param object The object
     * @return the permission by group only on an object, according to the given profiles
     */
    public Map<GroupIdentity, AccessResult> getPermissionsByGroup(Set<String> profileIds, Object object);
    
    /**
     * Returns true if this access controller supports the given object
     * @param object The object to test
     * @return true if this accessc controller supports the given object
     */
    public boolean isSupported(Object object);
}
