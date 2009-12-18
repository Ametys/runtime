/*
 *  Copyright 2009 Anyware Services
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

/**
 * Listener for group
 */
public interface GroupListener
{
    /**
     * When a group is removed
     * @param groupID the group id
     */
    public void groupRemoved(String groupID);
    
    /**
     * When a group is added
     * @param groupID the group id
     */
    public void groupAdded(String groupID);
    
    /**
     * When a group is updated
     * @param groupID the group id
     */
    public void groupUpdated(String groupID);
}
