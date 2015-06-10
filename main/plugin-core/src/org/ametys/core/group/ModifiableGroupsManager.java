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
