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
package org.ametys.runtime.test.users.jdbc;

import java.io.File;
import java.util.Arrays;

import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.plugins.core.user.jdbc.CredentialsAwareJdbcUsersManager;
import org.ametys.runtime.user.CredentialsAwareUsersManager;
import org.ametys.runtime.user.ModifiableUsersManager;

/**
 * Tests the CredentialAwareJdbcUsersTestCase
 */
public class CredentialAwareJdbcUsersTestCase extends JdbcUsersTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        _resetDB("runtime7.xml", "config1.xml");
    }
    
    @Override
    protected File[] getScripts()
    {
    	return new File[]{new File("main/plugin-core/scripts/mysql/jdbc_users_auth.sql")};
    }

	protected File[] getFilledScripts()
	{
		return new File[] {new File("test/environments/scripts/fillJDBCUsersAuth.sql")};
	}
    
    @Override
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
     * @throws Exception
     */
    public void testIncorrectLogin() throws Exception
    {
        CredentialsAwareUsersManager credentialsAwareUsersManager = (CredentialsAwareUsersManager) _usersManager;

        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "test")));
        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", null)));
        
        // Fill DB
        _setDatabase(Arrays.asList(getFilledScripts()));

        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "test2000")));
        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", null)));
    }
    
    /**
     * Test a correct login
     * @throws Exception
     */
    public void testCorrectLogin() throws Exception
    {
        CredentialsAwareUsersManager credentialsAwareUsersManager = (CredentialsAwareUsersManager) _usersManager;

        // Fill DB
        _setDatabase(Arrays.asList(getFilledScripts()));

        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "test")));
        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test2", "test")));
    }
}
