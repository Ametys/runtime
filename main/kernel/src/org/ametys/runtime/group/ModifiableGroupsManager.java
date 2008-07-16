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

import java.util.List;


/**
 * Abstraction for manipulating users' groups.
 */
public interface ModifiableGroupsManager extends GroupsManager
{
    /**
     * Add a new group of users.
     * @param name The name of the user group to create. Cannot be null;
     * @return The user group created
     * @throws InvalidModificationException if the group id exists yet or
     *         if at least one of the parameter is invalid.
     */
    public Group add(String name) throws InvalidModificationException;

    /**
     * Modify an existing group of users.
     * @param userGroup Informations about the new group. Cannot be null:
     * @throws InvalidModificationException if the group id does not exists yet
     */
    public void update(Group userGroup) throws InvalidModificationException;

    /**
     * Remove a group of users.
     * @param groupID The id of the group. Cannot be null;
     * @throws InvalidModificationException if the group id does not exists.
     */
    public void remove(String groupID) throws InvalidModificationException;
    
    /**
     * Registers an user listener
     * @param listener the user listener
     */
    public void registerListener(GroupListener listener);
    
    /**
     * Removes an user listener
     * @param listener the user listener
     */
    public void removeListener(GroupListener listener);
    
    /**
     * Get the list of user listeners
     * @return The user listeners as a list
     */
    public List getListeners();
}
