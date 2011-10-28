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
package org.ametys.runtime.test.users.jdbc.credentialsaware;

import java.io.File;
import java.util.Arrays;

import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.plugins.core.user.jdbc.CredentialsAwareJdbcUsersManager;
import org.ametys.runtime.test.users.jdbc.AbstractJDBCUsersManagerTestCase;
import org.ametys.runtime.user.CredentialsAwareUsersManager;
import org.ametys.runtime.user.ModifiableUsersManager;

/**
 * Tests the CredentialAwareJdbcUsersTestCase
 */
public abstract class AbstractCredentialsAwareJdbcUsersTestCase extends AbstractJDBCUsersManagerTestCase
{
    
    /**
     * Provide the scripts to run for populating database.
     * @return the scripts to run.
     */
    protected abstract File[] getPopulateScripts();
    
    /**
     * Test the getting of users on mysql
     * @throws Exception if an error occurs
     */
    public void testType() throws Exception
    {
        // JDBC IMPL
        assertTrue(_usersManager instanceof CredentialsAwareJdbcUsersManager);

        // MODIFIABLE
        assertFalse(_usersManager instanceof ModifiableUsersManager);
        
        // CREDENTIAL AWARE
        assertTrue(_usersManager instanceof CredentialsAwareUsersManager);
    }
    
    /**
     * Test an incorrect credential
     * @throws Exception if an error occurs
     */
    public void testIncorrectLogin() throws Exception
    {
        CredentialsAwareUsersManager credentialsAwareUsersManager = (CredentialsAwareUsersManager) _usersManager;

        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "test")));
        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", null)));
        
        // Fill DB
        _setDatabase(Arrays.asList(getPopulateScripts()));

        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "test2000")));
        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", null)));
    }
    
    /**
     * Test a correct login
     * @throws Exception if an error occurs
     */
    public void testCorrectLogin() throws Exception
    {
        CredentialsAwareUsersManager credentialsAwareUsersManager = (CredentialsAwareUsersManager) _usersManager;

        // Fill DB
        _setDatabase(Arrays.asList(getPopulateScripts()));

        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "test")));
        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test2", "test")));
    }
    
}
