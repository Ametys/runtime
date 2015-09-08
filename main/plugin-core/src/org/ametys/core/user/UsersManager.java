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
package org.ametys.core.user;

import java.util.Collection;
import java.util.List;
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
     * @return User's information as a <code>User</code> instance or null if the user login does not exist.
     */
    public User getUser(String login);
    
    /**
     * Get the JSON representation of a User.
     * @param login the login of the user. Cannot be null.
     * @return User's information as a JSON object or null if the user login does not exist.
     */
    public Map<String, Object> user2JSON(String login);
    
    /**
     * Sax a particular user
     * @param login the login of the user. Cannot be null.
     * @param handler The content handler to sax in.
     * @throws SAXException If an error occurs while saxing.
     */
    @Deprecated
    public void saxUser(String login, ContentHandler handler) throws SAXException;
    
    /**
     * Sax the user list.
     * @param handler The content handler to sax in.
     * @param count The maximum number of users to sax. (-1 to sax all)
     * @param offset The offset to start with, first is 0.
     * @param parameters Parameters for saxing user list differently, see implementation.
     * @throws SAXException If an error occurs while saxing.
     */
    @Deprecated
    public void toSAX(ContentHandler handler, int count, int offset, Map parameters) throws SAXException;
    
    /**
     * Get the user list.
     * @param count The maximum number of users to sax. (-1 to sax all)
     * @param offset The offset to start with, first is 0.
     * @param parameters Parameters for saxing user list differently, see implementation.
     * @return The users' information
     */
    public List<Map<String, Object>> users2JSON (int count, int offset, Map parameters);
}
