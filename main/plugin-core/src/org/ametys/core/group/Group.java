/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.core.group;

import java.util.HashSet;
import java.util.Set;

import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.user.UserIdentity;

/**
 * A user group is a set of {@link UserIdentity} , representing a group of users.<br>
 * A group contains any number of users, and a single user may belong to any number of groups.
 */
public class Group
{
    private Set<UserIdentity> _users;
    private GroupIdentity _identity;
    private String _label;
    private GroupDirectory _groupDirectory;
    
    /**
     * Constructor.
     * @param identity the identity of this group
     * @param label the label of this group
     * @param groupDirectory the group directory this group belongs to
     */
    public Group(GroupIdentity identity, String label, GroupDirectory groupDirectory)
    {
        _identity = identity;
        _label = label;
        _groupDirectory = groupDirectory;
        _users = new HashSet<>();
    }
    
    /**
     * Returns the identity of this group
     * @return the identity of this group
     */
    public GroupIdentity getIdentity()
    {
        return _identity;
    }
    
    /**
     * Returns the label of this group
     * @return the label of this group
     */
    public String getLabel()
    {
        return _label;
    }
    
    /**
     * Returns the group directory this group belongs to
     * @return the group directory this group belongs to
     */
    public GroupDirectory getGroupDirectory()
    {
        return _groupDirectory;
    }
    
    /**
     * Set the label of this group
     * @param label The new label of the group
     */
    public void setLabel(String label)
    {
        _label = label;
    }
    
    /**
     * Adds a user to this group
     * @param user The user to add
     */
    public void addUser(UserIdentity user)
    {
        _users.add(user);
    }
    
    /**
     * Removes a user to this group
     * @param user The user to add
     */
    public void removeUser(UserIdentity user)
    {
        _users.remove(user);
    }
    
    /**
     * Returns all users of this group.
     * @return Users as a Set of {@link UserIdentity}
     */
    public Set<UserIdentity> getUsers()
    {
        return _users;
    }
    
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer("UserGroup[");
        sb.append(_identity);
        sb.append(" (");
        sb.append(_label);
        sb.append(") => ");
        sb.append(_users.toString());
        sb.append("]");
        return sb.toString();
    }    
    
    @Override
    public boolean equals(Object another)
    {
        if (another == null || !(another instanceof Group))
        {
            return false;
        }
        
        Group otherGroup = (Group) another;
        
        return _identity != null && _identity.equals(otherGroup.getIdentity());
    }
    
    @Override
    public int hashCode()
    {
        return _identity.hashCode();
    }
}
