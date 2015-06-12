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

import java.util.Map;
import java.util.Set;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Abstraction for getting users' groups.
 */
public interface GroupsManager
{
    /** Avalon Role */
    public static final String ROLE = GroupsManager.class.getName();
    
    /**
     * Returns a particular group.
     * @param groupID The id of the group.
     * @return The group or null if the group does not exist.
     */
    public Group getGroup(String groupID);

    /**
     * Returns all groups.
     * @return The groups as a Set of UserGroup, empty if an error occurs.
     */
    public Set<Group> getGroups();

    /**
     * Get all groups a particular is in.
     * @param login The login of the user.
     * @return The groups as a Set of String (group ID), empty if the login does not match.
     */
    public Set<String> getUserGroups(String login);
    
    /**
     * Sax the user list.
     * @param ch The content handler to sax in.
     * @param count The maximum number of groups to sax. (-1 to sax all)
     * @param offset The offset to start with, first is 0.
     * @param parameters Parameters for saxing user list differently, see implementation.
     * @throws SAXException If an error occurs while saxing.
     */
    public void toSAX(ContentHandler ch, int count, int offset, Map parameters) throws SAXException;
}
