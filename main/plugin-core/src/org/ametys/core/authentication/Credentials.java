/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.core.authentication;

/**
 * Credentials represents the characteristics of a user needed to log him
 */
public class Credentials
{
    /** The login */
    protected String _login;

    /**
     * Create credentials
     * @param login Login of the user
     */
    public Credentials(String login)
    {
        _login = login != null ? login : "";
    }

    /**
     * Returns the login. Can not be null.
     * @return the login
     */
    public String getLogin()
    {
        return _login;
    }
}
