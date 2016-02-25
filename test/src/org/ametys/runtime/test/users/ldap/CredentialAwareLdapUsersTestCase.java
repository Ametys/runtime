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
package org.ametys.runtime.test.users.ldap;

import org.ametys.core.authentication.Credentials;
import org.ametys.core.user.CredentialsAwareUsersManager;
import org.ametys.core.user.ModifiableUsersManager;
import org.ametys.core.user.UsersManager;
import org.ametys.plugins.core.impl.user.ldap.CredentialsAwareLdapUsersManager;
import org.ametys.runtime.test.Init;

/**
 * Tests the LdapUsersManager
 */
public class CredentialAwareLdapUsersTestCase extends LdapUsersTestCase
{
    @Override
    protected void _startApp() throws Exception
    {
        _startApplication("test/environments/runtimes/runtime9.xml", "test/environments/configs/config4.xml", "test/environments/datasources/datasource-mysql.xml", "test/environments/datasources/datasource-ldap.xml", "test/environments/webapp1");
        
        _usersManager = (UsersManager) Init.getPluginServiceManager().lookup(UsersManager.ROLE);
    }
    
    @Override
    public void testType() throws Exception
    {
        // JDBC IMPL
        assertTrue(_usersManager instanceof CredentialsAwareLdapUsersManager);

        // NOT MODIFIABLE
        assertFalse(_usersManager instanceof ModifiableUsersManager);
        
        // NOT CREDENTIAL AWARE
        assertTrue(_usersManager instanceof CredentialsAwareUsersManager);
    }
    
    /**
     * Test that LDAP can authentify incorrectly
     * @throws Exception if an error occurs
     */
    public void testIncorrectAuthentication() throws Exception
    {
        CredentialsAwareUsersManager credentialAwareUSersManager = (CredentialsAwareUsersManager) _usersManager;

        Credentials credentials;
        boolean result;

        credentials = new Credentials("foo", "foo");
        result = credentialAwareUSersManager.checkCredentials(credentials);
        assertFalse(result);

        credentials = new Credentials("user1", "wrongpassword");
        result = credentialAwareUSersManager.checkCredentials(credentials);
        assertFalse(result);
    }

    /**
     * Test that LDAP can authentify correctly
     * @throws Exception if an error occurs
     */
    public void testCorrectAuthentication() throws Exception
    {
        CredentialsAwareUsersManager credentialAwareUSersManager = (CredentialsAwareUsersManager) _usersManager;

        Credentials credentials;
        boolean result;

        credentials = new Credentials("user1", "user1");
        result = credentialAwareUSersManager.checkCredentials(credentials);
        assertTrue(result);
    }
}
