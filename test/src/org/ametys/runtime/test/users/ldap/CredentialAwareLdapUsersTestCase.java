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
package org.ametys.runtime.test.users.ldap;

import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugins.core.user.ldap.CredentialsAwareLdapUsersManager;
import org.ametys.runtime.test.Init;
import org.ametys.runtime.user.CredentialsAwareUsersManager;
import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.UsersManager;

/**
 * Tests the LdapUsersManager
 */
public class CredentialAwareLdapUsersTestCase extends LdapUsersTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime9.xml");
        Config.setFilename("test/environments/configs/config4.xml");
        
        _startCocoon("test/environments/webapp1");

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
     * @throws Exception
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
     * @throws Exception
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
