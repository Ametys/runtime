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
import org.ametys.core.user.directory.ModifiableUserDirectory;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.plugins.core.impl.user.directory.LdapUserDirectory;

/**
 * Tests the LdapUserDirectory
 */
public class CredentialAwareLdapUsersTestCase extends LdapUsersTestCase
{
    @Override
    protected void _startApp() throws Exception
    {
        _startApplication("test/environments/runtimes/runtime9.xml", "test/environments/configs/config4.xml", "test/environments/datasources/datasource-mysql.xml", "test/environments/datasources/datasource-ldap.xml", "test/environments/webapp1");
        
        _userDirectory = _createLdapUserDirectory();
    }
    
    @Override
    public void testType() throws Exception
    {
        // LDAP IMPL
        assertTrue(_userDirectory instanceof LdapUserDirectory);

        // NOT MODIFIABLE
        assertFalse(_userDirectory instanceof ModifiableUserDirectory);
        
        // CREDENTIAL AWARE
        assertTrue(_userDirectory instanceof UserDirectory);
    }
    
    /**
     * Test that LDAP can authentify incorrectly
     * @throws Exception if an error occurs
     */
    public void testIncorrectAuthentication() throws Exception
    {
        UserDirectory credentialAwareUserDirectory = (UserDirectory) _userDirectory;

        Credentials credentials;
        boolean result;

        credentials = new Credentials("foo", "foo");
        result = credentialAwareUserDirectory.checkCredentials(credentials);
        assertFalse(result);

        credentials = new Credentials("user1", "wrongpassword");
        result = credentialAwareUserDirectory.checkCredentials(credentials);
        assertFalse(result);
    }

    /**
     * Test that LDAP can authentify correctly
     * @throws Exception if an error occurs
     */
    public void testCorrectAuthentication() throws Exception
    {
        UserDirectory credentialAwareUserDirectory = (UserDirectory) _userDirectory;

        Credentials credentials;
        boolean result;

        credentials = new Credentials("user1", "user1");
        result = credentialAwareUserDirectory.checkCredentials(credentials);
        assertTrue(result);
    }
}
