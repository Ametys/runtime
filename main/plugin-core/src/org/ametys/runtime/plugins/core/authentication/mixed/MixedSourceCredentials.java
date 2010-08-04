/*
 *  Copyright 2010 Anyware Services
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
package org.ametys.runtime.plugins.core.authentication.mixed;

import org.ametys.runtime.authentication.Credentials;

/**
 * Credentials coming from more than one source.
 * If the credentials come from a SSO (like CAS), they are already authenticated.
 */
public class MixedSourceCredentials extends Credentials
{
    /** Is the user already authenticated by the CredentialsProvider ? */
    protected boolean _authenticated;
    
    /**
     * Create a mixed source credentials.
     * @param login Login of the user
     * @param password Password associated to the login
     */
    public MixedSourceCredentials(String login, String password)
    {
        this(login, password, false);
    }
    
    /**
     * Create a mixed source credentials from a standard Credentials object.
     * @param credentials the standard Credentials object.
     * @param authenticated true if the user is already authenticated by the CredentialsProvider (SSO).
     */
    public MixedSourceCredentials(Credentials credentials, boolean authenticated)
    {
        this(credentials.getLogin(), credentials.getPassword(), authenticated);
    }
    
    /**
     * Create a mixed source credentials.
     * @param login Login of the user
     * @param password Password associated to the login
     * @param authenticated true if the user is already authenticated by the CredentialsProvider (SSO).
     */
    public MixedSourceCredentials(String login, String password, boolean authenticated)
    {
        super(login, password);
        _authenticated = authenticated;
    }
    
    /**
     * Test if the user is already authenticated by the CredentialsProvider ?
     * @return true if the user is already authenticated by the CredentialsProvider, false otherwise.
     */
    public boolean isAuthenticated()
    {
        return _authenticated;
    }
}
