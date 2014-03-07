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
package org.ametys.runtime.group;

import java.util.HashSet;
import java.util.Set;

/**
 * A user group is a set of logins, representing a group of users.<br>
 * A group contains any number of users, and a single user may belong to any number of groups.
 */
public class Group
{
    private Set<String> _users;
    private String _id;
    private String _label;
    
    /**
     * Constructor.
     * @param id the unique id of this profile
     * @param label the label of this group
     */
    public Group(String id, String label)
    {
        _id = id;
        _label = label;
        _users = new HashSet<String>();
    }
    
    /**
     * Returns the unique Id of this group
     * @return the unique Id of this group
     */
    public String getId()
    {
        return _id;
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
     * Set the label of this group
     * @param label The new label of the group
     */
    public void setLabel(String label)
    {
        _label = label;
    }
    
    /**
     * Adds an user to this group
     * @param login the login of the user to add to this group
     */
    public void addUser(String login)
    {
        _users.add(login);
    }
    
    /**
     * Removes an user to this group
     * @param login the login of the user to remove to this group
     */
    public void removeUser(String login)
    {
        _users.remove(login);
    }
    
    /**
     * Returns all users of this group.
     * @return Users as a Set of String (login).
     */
    public Set<String> getUsers()
    {
        return _users;
    }
    
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer("UserGroup[");
        sb.append(_id);
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
        
        return _id != null  || _id.equals(otherGroup.getId());
    }
    
    @Override
    public int hashCode()
    {
        return _id.hashCode();
    }
}
