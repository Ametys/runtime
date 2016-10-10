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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.ametys.core.user.User;
import org.ametys.core.user.directory.ModifiableUserDirectory;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.directory.UserDirectoryFactory;
import org.ametys.plugins.core.impl.user.directory.LdapUserDirectory;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;

/**
 * Tests the LdapUsersManager
 */
public class LdapUsersTestCase extends AbstractRuntimeTestCase
{
    /** the user directory */
    protected UserDirectory _userDirectory;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _startApp();
    }
    
    /**
     * start the application
     * @throws Exception if an error occurs
     */
    protected void _startApp() throws Exception
    {
        _startApplication("test/environments/runtimes/runtime8.xml", "test/environments/configs/config3.xml", "test/environments/datasources/datasource-ldap.xml", "test/environments/webapp1");
        _userDirectory = _createLdapUserDirectory();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Test the getting of users on mysql
     * @throws Exception if an error occurs
     */
    public void testType() throws Exception
    {
        // LDAP IMPL
        assertTrue(_userDirectory instanceof LdapUserDirectory);

        // NOT MODIFIABLE
        assertFalse(_userDirectory instanceof ModifiableUserDirectory);
    }
    
    /**
     * Test a filled db
     * @throws Exception if an error occurs
     */
    public void testFilled() throws Exception
    {
        User user = null;
        Collection<User> users = null;

        // Get unexisting user
        user = _userDirectory.getUser("foo");
        assertNull(user);

        // Get existing user
        User user1 = _userDirectory.getUser("user1");
        assertNotNull(user1);
        assertEquals(user1.getIdentity().getLogin(), "user1");
        assertEquals(user1.getLastName(), "USER1");
        assertEquals(user1.getFirstName(), "User1");
        assertEquals(user1.getFullName(), "User1 USER1");
        assertEquals(user1.getSortableName(), "USER1 User1");
        assertEquals(user1.getEmail(), "user1@ametys.org");
        
        User user10 = _userDirectory.getUser("user10");
        assertNotNull(user10);
        assertEquals(user10.getIdentity().getLogin(), "user10");
        assertEquals(user10.getLastName(), "USER10");
        assertEquals(user10.getFirstName(), "User10");
        assertEquals(user10.getFullName(), "User10 USER10");
        assertEquals(user10.getSortableName(), "USER10 User10");
        assertEquals(user10.getEmail(), "user10@ametys.org");

        // Get users
        users = _userDirectory.getUsers();
        assertEquals(10, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user10));
        
        // Get users with parameters
        users = _userDirectory.getUsers(-1, 0, new HashMap<>());
        assertEquals(10, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user10));
        
        // Get users with pattern "user1"
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("pattern", "user1");
        users = _userDirectory.getUsers(-1, 0, parameters);
        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user10));
        
        // Get users with parameters by part
        Set<String> results = new HashSet<>();
        
        users = _userDirectory.getUsers(4, 0, new HashMap<>());
        assertEquals(4, users.size());
        for (User theUser : users)
        {
            results.add(theUser.getIdentity().getLogin());
        }
        
        users = _userDirectory.getUsers(4, 4, new HashMap<>());
        assertEquals(4, users.size());
        for (User theUser : users)
        {
            results.add(theUser.getIdentity().getLogin());
        }
        
        users = _userDirectory.getUsers(4, 8, new HashMap<>());
        assertEquals(2, users.size());
        for (User theUser : users)
        {
            results.add(theUser.getIdentity().getLogin());
        }
        
        assertEquals(10, results.size());
        assertTrue(results.contains("user1"));
        assertTrue(results.contains("user2"));
        assertTrue(results.contains("user3"));
        assertTrue(results.contains("user4"));
        assertTrue(results.contains("user5"));
        assertTrue(results.contains("user6"));
        assertTrue(results.contains("user7"));
        assertTrue(results.contains("user8"));
        assertTrue(results.contains("user9"));
        assertTrue(results.contains("user10"));
    }
    
    /**
     * Create the user directory
     * @return The user directory
     * @throws Exception If an error occured
     */
    protected UserDirectory _createLdapUserDirectory() throws Exception
    {
        String modelId = "org.ametys.plugins.core.user.directory.Ldap";
        
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("runtime.users.ldap.datasource", "LDAP-test-users");
        
        parameters.put("runtime.users.ldap.peopleDN", "ou=people");
        parameters.put("runtime.users.ldap.baseFilter", "(objectclass=inetOrgPerson)");
        parameters.put("runtime.users.ldap.scope", "sub");
        parameters.put("runtime.users.ldap.loginAttr", "uid");
        parameters.put("runtime.users.ldap.firstnameAttr", "givenName");
        parameters.put("runtime.users.ldap.lastnameAttr", "sn");
        parameters.put("runtime.users.ldap.emailAttr", "mail");
        parameters.put("runtime.users.ldap.emailMandatory", false);
        parameters.put("runtime.users.ldap.serverSideSorting", true);
        
        return ((UserDirectoryFactory) Init.getPluginServiceManager().lookup(UserDirectoryFactory.ROLE)).createUserDirectory(modelId, parameters, "foo", null);
    }
}
