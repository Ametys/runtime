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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

import org.ametys.core.authentication.Credentials;
import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.user.CredentialsAwareUsersManager;
import org.ametys.core.user.ModifiableUsersManager;
import org.ametys.plugins.core.impl.user.jdbc.CredentialsAwareJdbcUsersManager;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.test.users.jdbc.AbstractJDBCUsersManagerTestCase;

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

        // Test MD5 password
        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "test")));
        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test2", "test")));
        
        if (credentialsAwareUsersManager instanceof ModifiableUsersManager)
        {
            // Test SHA2 password
            _checkSHA2Password();
            
            assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "test")));
        }
    }
    
    /**
     * Check the password was migrated to SHA2
     * @throws Exception if an error occurs
     */
    protected void _checkSHA2Password() throws Exception
    {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            String dataSourceId = Config.getInstance().getValueAsString(ConnectionHelper.CORE_POOL_CONFIG_PARAM);
            connection = ConnectionHelper.getConnection(dataSourceId);
            
            String sql = "SELECT password, salt FROM Users WHERE login = ?";
            
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, "test");
            
            rs = stmt.executeQuery();

            if (rs.next()) 
            {
                String storedPassword = rs.getString("password");
                String salt = rs.getString("salt");
                
                assertTrue(salt != null);
                assertTrue (storedPassword.length() == 128);
            }
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
        }
    }
    
}
