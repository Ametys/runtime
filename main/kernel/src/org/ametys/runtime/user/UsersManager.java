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
package org.ametys.runtime.user;

import java.util.Collection;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Abstraction for getting users list and verify the presence of a particular user.
 */
public interface UsersManager
{
    /** Avalon Role */
    public static final String ROLE = UsersManager.class.getName();
    
    /**
     * Get the list of all users.
     * @return list of users as Collection of <code>User</code>s, empty if a problem occurs.
     */
    public Collection<User> getUsers();

    /**
     * Get a particular user by his login.
     * @param login Login of the user to get. Cannot be null.
     * @return User's information as a <code>User</code> instance or null if the user login does not exists.
     */
    public User getUser(String login);
    
    /**
     * Sax the user list.
     * @param handler The content handler to sax in.
     * @param count The maximum number of users to sax. (-1 to sax all)
     * @param offset The offset to start with, first is 0.
     * @param parameters Parameters for saxing user list differently, see implementation.
     * @throws SAXException If an error occurs while saxing.
     */
    public void toSAX(ContentHandler handler, int count, int offset, Map parameters) throws SAXException;
}
