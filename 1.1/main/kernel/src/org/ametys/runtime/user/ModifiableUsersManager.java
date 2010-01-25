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

import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Abstraction for getting users list and verify the presence of a particular
 * user and finally modifying this list.
 */
public interface ModifiableUsersManager extends UsersManager
{
    /**
     * Add a new user to the list.
     * @param userInformation Informations about the user, see implementation. Cannot be null.
     * @throws InvalidModificationException if the login exists yet or
     *         if at least one of the parameter is invalid. 
     */
    public void add(Map<String, String> userInformation) throws InvalidModificationException;

    /**
     * Modify informations about an user of the list.
     * @param userInformation New informations about the user, see implementation. Cannot be null.
     * @throws InvalidModificationException if the login does not match
     *         in the list or if at least one of the parameter is invalid. 
     */
    public void update(Map<String, String> userInformation) throws InvalidModificationException;

    /**
     * Remove an user from the list.
     * @param login The user's login. Cannot be null.
     * @throws InvalidModificationException if the user cannot be removed
     */
    public void remove(String login) throws InvalidModificationException;
    
    /**
     * SAX the edition model (depending on implementation)
     * @param handler The sax handler
     * @throws SAXException if an error occured
     */
    public void saxModel(ContentHandler handler) throws SAXException;
    
    /**
     * Registers an user listener
     * @param listener the user listener
     */
    public void registerListener(UserListener listener);
    
    /**
     * Removes an user listener
     * @param listener the user listener
     */
    public void removeListener(UserListener listener);
}
