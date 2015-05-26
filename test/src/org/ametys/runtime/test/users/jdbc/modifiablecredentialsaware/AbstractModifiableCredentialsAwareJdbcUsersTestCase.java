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
package org.ametys.runtime.test.users.jdbc.modifiablecredentialsaware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.ComparisonFailure;

import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.plugins.core.user.jdbc.ModifiableCredentialsAwareJdbcUsersManager;
import org.ametys.runtime.test.users.jdbc.AbstractJDBCUsersManagerTestCase;
import org.ametys.runtime.user.CredentialsAwareUsersManager;
import org.ametys.runtime.user.InvalidModificationException;
import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UserListener;

/**
 * Tests the ModifiableCredentialAwareJdbcUsersTestCase
 */
public abstract class AbstractModifiableCredentialsAwareJdbcUsersTestCase extends AbstractJDBCUsersManagerTestCase
{
    
    /**
     * Test the getting of users on mysql
     * @throws Exception if an error occurs
     */
    public void testType() throws Exception
    {
        // JDBC IMPL
        assertTrue(_usersManager instanceof ModifiableCredentialsAwareJdbcUsersManager);

        // MODIFIABLE
        assertTrue(_usersManager instanceof ModifiableUsersManager);
        
        // CREDENTIAL AWARE
        assertTrue(_usersManager instanceof CredentialsAwareUsersManager);
    }
    
    /**
     * Test the addition of a new user that should failed
     * @throws Exception if an error occurs
     */
    public void testIncorrectAdd() throws Exception
    {
        ModifiableUsersManager modifiableUsersManager = (ModifiableUsersManager) _usersManager;
        
        MyUserListener listener1 = new MyUserListener();
        MyUserListener listener2 = new MyUserListener();
        modifiableUsersManager.registerListener(listener1);
        modifiableUsersManager.registerListener(listener2);
        
        // Incorrect additions
        Map<String, String> userInformation;
        
        try
        {
            userInformation = new HashMap<>();
            modifiableUsersManager.add(userInformation);
            fail("An empty addition should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            _checkListener(listener1, listener2, 0, 0, 0);
        }

        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "test");
            modifiableUsersManager.add(userInformation);
            fail("A non complete addition should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            _checkListener(listener1, listener2, 0, 0, 0);
        }

        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "test");
            userInformation.put("lastname", "test");
            userInformation.put("email", "");
            modifiableUsersManager.add(userInformation);
            fail("A non complete addition should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            _checkListener(listener1, listener2, 0, 0, 0);
        }

        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "test");
            userInformation.put("lastname", "test");
            userInformation.put("password", "test");
            userInformation.put("email", "testthatisnotacorrectemail");
            modifiableUsersManager.add(userInformation);
            fail("An incorrect addition should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            _checkListener(listener1, listener2, 0, 0, 0);
        }
    }
    
    /**
     * Test the addition of a new user
     * @throws Exception if an error occurs
     */
    public void testCorrectAdd() throws Exception
    {
        ModifiableUsersManager modifiableUsersManager = (ModifiableUsersManager) _usersManager;
        CredentialsAwareUsersManager credentialsAwareUsersManager = (CredentialsAwareUsersManager) _usersManager;
        User user;
        
        MyUserListener listener1 = new MyUserListener();
        MyUserListener listener2 = new MyUserListener();
        modifiableUsersManager.registerListener(listener1);
        modifiableUsersManager.registerListener(listener2);
        
        // Correct additions
        Map<String, String> userInformation;
        
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Test");
        userInformation.put("lastname", "TEST");
        userInformation.put("email", "");
        userInformation.put("password", "testpassword");
        modifiableUsersManager.add(userInformation);
        _checkListener(listener1, listener2, 1, 0, 0);
        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "testpassword")));
        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "wrongpassword")));
        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test2", "testpassword")));
        
        userInformation = new HashMap<>();
        userInformation.put("login", "test2");
        userInformation.put("firstname", "Test2");
        userInformation.put("lastname", "TEST2");
        userInformation.put("email", "test2@test.te");
        userInformation.put("password", "testpassword");
        modifiableUsersManager.add(userInformation);
        _checkListener(listener1, listener2, 2, 0, 0);
        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "testpassword")));
        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "wrongpassword")));
        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test2", "testpassword")));
        
        user = _usersManager.getUser("test");
        assertNotNull(user);
        assertEquals(user.getName(), "test");
        assertEquals(user.getFullName(), "Test TEST");
        if (user.getEmail() != null && !user.getEmail().equals(""))
        {
            throw new ComparisonFailure(null, "null or ''", user.getEmail());
        }

        user = _usersManager.getUser("test2");
        assertNotNull(user);
        assertEquals(user.getName(), "test2");
        assertEquals(user.getFullName(), "Test2 TEST2");
        assertEquals(user.getEmail(), "test2@test.te");

        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "Test");
            userInformation.put("lastname", "TEST");
            userInformation.put("email", "");
            userInformation.put("password", "testpassword");
            modifiableUsersManager.add(userInformation);
            fail("Add should have failed");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior since login already exists
            _checkListener(listener1, listener2, 2, 0, 0);
        }
    }
    
    /**
     * Test the update of a user incorrectly
     * @throws Exception if an error occurs
     */
    public void testIncorrectUpdate() throws Exception
    {
        ModifiableUsersManager modifiableUsersManager = (ModifiableUsersManager) _usersManager;
        CredentialsAwareUsersManager credentialsAwareUsersManager = (CredentialsAwareUsersManager) _usersManager;
        
        MyUserListener listener1 = new MyUserListener();
        MyUserListener listener2 = new MyUserListener();
        modifiableUsersManager.registerListener(listener1);
        modifiableUsersManager.registerListener(listener2);
        
        // Incorrect modification
        Map<String, String> userInformation;
        
        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "Test");
            userInformation.put("lastname", "TEST");
            userInformation.put("email", "");
            userInformation.put("password", "testpassword");
            modifiableUsersManager.update(userInformation);
            fail("Update should have failed");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior since login does not exists
            _checkListener(listener1, listener2, 0, 0, 0);
        }
        
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Test");
        userInformation.put("lastname", "TEST");
        userInformation.put("email", "");
        userInformation.put("password", "testpassword");
        modifiableUsersManager.add(userInformation);
        _checkListener(listener1, listener2, 1, 0, 0);
        
        try
        {
            userInformation = new HashMap<>();
            userInformation.put("firstname", "Test");
            userInformation.put("lastname", "TEST");
            userInformation.put("email", "");
            userInformation.put("password", "testpassword");
            modifiableUsersManager.update(userInformation);
            fail("Update should have failed");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior since no login is given
            _checkListener(listener1, listener2, 1, 0, 0);
        }

        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "Test");
            userInformation.put("lastname", "TEST");
            userInformation.put("email", "incorrectemail");
            userInformation.put("password", "testpassword");
            modifiableUsersManager.update(userInformation);
            fail("Update should have failed");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior since email is incorrect
            _checkListener(listener1, listener2, 1, 0, 0);
        }
        
        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "Test");
            userInformation.put("lastname", "TEST");
            userInformation.put("password", "");
            modifiableUsersManager.update(userInformation);
            fail("Update should have failed");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior since password is empty
            _checkListener(listener1, listener2, 1, 0, 0);
        }
        
        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "testpassword")));
        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "wrongpassword")));
        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test2", "testpassword")));
    }
    
    /**
     * Test the update of a user
     * @throws Exception if an error occurs
     */
    public void testCorrectUpdate() throws Exception
    {
        ModifiableUsersManager modifiableUsersManager = (ModifiableUsersManager) _usersManager;
        CredentialsAwareUsersManager credentialsAwareUsersManager = (CredentialsAwareUsersManager) _usersManager;
        User user;
        
        MyUserListener listener1 = new MyUserListener();
        MyUserListener listener2 = new MyUserListener();
        modifiableUsersManager.registerListener(listener1);
        modifiableUsersManager.registerListener(listener2);
        
        Map<String, String> userInformation;
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Test");
        userInformation.put("lastname", "TEST");
        userInformation.put("email", "");
        userInformation.put("password", "testpassword");
        modifiableUsersManager.add(userInformation);
        _checkListener(listener1, listener2, 1, 0, 0);

        // Correct modification
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Testmodified");
        userInformation.put("lastname", "TESTMODIFIED");
        userInformation.put("email", "testModified@test.te");
        userInformation.put("password", "testpassword2");
        modifiableUsersManager.update(userInformation);
        _checkListener(listener1, listener2, 1, 1, 0);
        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "testpassword")));
        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "testpassword2")));
        
        user = _usersManager.getUser("test");
        assertNotNull(user);
        assertEquals(user.getName(), "test");
        assertEquals(user.getFullName(), "Testmodified TESTMODIFIED");
        assertEquals(user.getEmail(), "testModified@test.te");

        // partial modification (no password change)
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Testmodifiedtwice");
        modifiableUsersManager.update(userInformation);
        _checkListener(listener1, listener2, 1, 2, 0);
        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "testpassword")));
        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "testpassword2")));
        
        user = _usersManager.getUser("test");
        assertNotNull(user);
        assertEquals(user.getName(), "test");
        assertEquals(user.getFullName(), "Testmodifiedtwice TESTMODIFIED");
        assertEquals(user.getEmail(), "testModified@test.te");
    }
    
    /**
     * Test incorrects remove
     * @throws Exception if an error occurs
     */
    public void testIncorrectRemove() throws Exception
    {
        ModifiableUsersManager modifiableUsersManager = (ModifiableUsersManager) _usersManager;
        CredentialsAwareUsersManager credentialsAwareUsersManager = (CredentialsAwareUsersManager) _usersManager;
        
        MyUserListener listener1 = new MyUserListener();
        MyUserListener listener2 = new MyUserListener();
        modifiableUsersManager.registerListener(listener1);
        modifiableUsersManager.registerListener(listener2);

        try
        {
            modifiableUsersManager.remove("foo");
            fail("Remove should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            _checkListener(listener1, listener2, 0, 0, 0);
        }

        Map<String, String> userInformation;
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Test");
        userInformation.put("lastname", "TEST");
        userInformation.put("password", "testpassword");
        userInformation.put("email", "");
        modifiableUsersManager.add(userInformation);
        _checkListener(listener1, listener2, 1, 0, 0);

        try
        {
            modifiableUsersManager.remove("foo");
            fail("Remove should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            _checkListener(listener1, listener2, 1, 0, 0);
            User user = _usersManager.getUser("test");
            assertNotNull(user);
        }

        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Testmodified");
        userInformation.put("lastname", "TESTMODIFIED");
        userInformation.put("email", "testModified@test.te");
        userInformation.put("password", "testpassword2");
        modifiableUsersManager.update(userInformation);
        _checkListener(listener1, listener2, 1, 1, 0);

        try
        {
            modifiableUsersManager.remove("foo");
            fail("Remove should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            _checkListener(listener1, listener2, 1, 1, 0);
            User user = _usersManager.getUser("test");
            assertNotNull(user);
        }

        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "testpassword2")));
    }
    
    /**
     * Test corrects remove
     * @throws Exception if an error occurs
     */
    public void testCorrectRemove() throws Exception
    {
        ModifiableUsersManager modifiableUsersManager = (ModifiableUsersManager) _usersManager;
        CredentialsAwareUsersManager credentialsAwareUsersManager = (CredentialsAwareUsersManager) _usersManager;
        
        MyUserListener listener1 = new MyUserListener();
        MyUserListener listener2 = new MyUserListener();
        modifiableUsersManager.registerListener(listener1);
        modifiableUsersManager.registerListener(listener2);

        Map<String, String> userInformation;
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Test");
        userInformation.put("lastname", "TEST");
        userInformation.put("email", "");
        userInformation.put("password", "testpassword");
        modifiableUsersManager.add(userInformation);
        _checkListener(listener1, listener2, 1, 0, 0);

        userInformation = new HashMap<>();
        userInformation.put("login", "test2");
        userInformation.put("firstname", "Test2");
        userInformation.put("lastname", "TEST2");
        userInformation.put("email", "email@email.ma");
        userInformation.put("password", "test2password");
        modifiableUsersManager.add(userInformation);
        _checkListener(listener1, listener2, 2, 0, 0);

        modifiableUsersManager.remove("test");
        _checkListener(listener1, listener2, 2, 0, 1);
        assertFalse(credentialsAwareUsersManager.checkCredentials(new Credentials("test", "testpassword")));
        assertTrue(credentialsAwareUsersManager.checkCredentials(new Credentials("test2", "test2password")));
        
        User user = _usersManager.getUser("test2");
        assertNotNull(user);
        assertEquals(user.getName(), "test2");
        assertEquals(user.getFullName(), "Test2 TEST2");
        assertEquals(user.getEmail(), "email@email.ma");
    }
    
    private void _checkListener(MyUserListener listener1, MyUserListener listener2, int added, int updated, int removed)
    {
        assertEquals(added, listener1.getAddedUsers().size());
        assertEquals(updated, listener1.getUpdatedUsers().size());
        assertEquals(removed, listener1.getRemovedUsers().size());

        assertEquals(added, listener2.getAddedUsers().size());
        assertEquals(updated, listener2.getUpdatedUsers().size());
        assertEquals(removed, listener2.getRemovedUsers().size());
    }
    
    /**
     * User listener
     */
    public class MyUserListener implements UserListener
    {
        private List<String> _addedUsers = new ArrayList<>();
        private List<String> _removedUsers = new ArrayList<>();
        private List<String> _updatedUsers = new ArrayList<>();

        /**
         * Returns the added users'list
         * @return the added users'list
         */
        public List<String> getAddedUsers()
        {
            return _addedUsers;
        }

        /**
         * Returns the removed users'list
         * @return the removed users'list
         */
        public List<String> getRemovedUsers()
        {
            return _removedUsers;
        }
        
        /**
         * Returns the updated users'list
         * @return the updated users'list
         */
        public List<String> getUpdatedUsers()
        {
            return _updatedUsers;
        }
        
        public void userAdded(String login)
        {
            if (login == null)
            {
                throw new RuntimeException("Listener detected a null login addition");
            }
            _addedUsers.add(login);
        }

        public void userRemoved(String login)
        {
            if (login == null)
            {
                throw new RuntimeException("Listener detected a null login removal");
            }
            _removedUsers.add(login);
        }

        public void userUpdated(String login)
        {
            if (login == null)
            {
                throw new RuntimeException("Listener detected a null login update");
            }
            _updatedUsers.add(login);
        }
        
    }
}
