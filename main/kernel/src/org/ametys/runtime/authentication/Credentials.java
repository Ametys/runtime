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
package org.ametys.runtime.authentication;

/**
 * Credentials represents the caracteristics of a user needed to log him
 */
public class Credentials
{
    private String _login;

    private String _passwd;

    /**
     * Create a credentials
     * 
     * @param login Login of the user
     * @param password Password associated to the login
     */
    public Credentials(String login, String password)
    {
        _login = login != null ? login : "";
        _passwd = password != null ? password : "";
    }

    /**
     * Returns the login. Can not be null.
     * @return the login
     */
    public String getLogin()
    {
        return _login;
    }

    /**
     * Returns the password. Can not be null.
     * @return the password
     */
    public String getPassword()
    {
        return _passwd;
    }
}
