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
     * @return The group or null if the group does not exists.
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
