/*
 *  Copyright 2014 Anyware Services
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
package org.ametys.runtime.authentication;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * Credentials represents the caracteristics of a user needed to log him
 */
public class Credentials extends AbstractLogEnabled
{
    /** The login */
    protected String _login;
    /** The password */
    protected String _passwd;

    /**
     * Create credentials
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
