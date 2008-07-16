/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
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
