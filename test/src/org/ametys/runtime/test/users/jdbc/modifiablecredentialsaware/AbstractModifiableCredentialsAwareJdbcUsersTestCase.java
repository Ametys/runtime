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

import java.util.HashMap;
import java.util.Map;

import org.ametys.core.authentication.Credentials;
import org.ametys.core.user.InvalidModificationException;
import org.ametys.core.user.User;
import org.ametys.core.user.directory.ModifiableUserDirectory;
import org.ametys.plugins.core.impl.user.directory.JdbcUserDirectory;
import org.ametys.runtime.test.users.jdbc.AbstractJDBCUsersManagerTestCase;

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
        assertTrue(_userDirectory instanceof JdbcUserDirectory);

        // MODIFIABLE
        assertTrue(_userDirectory instanceof ModifiableUserDirectory);
    }
    
    /**
     * Test the addition of a new user that should failed
     * @throws Exception if an error occurs
     */
    public void testIncorrectAdd() throws Exception
    {
        ModifiableUserDirectory modifiableUserDirectory = (ModifiableUserDirectory) _userDirectory;
        
        // Incorrect additions
        Map<String, String> userInformation;
        
        try
        {
            userInformation = new HashMap<>();
            modifiableUserDirectory.add(userInformation);
            fail("An empty addition should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
        }

        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "test");
            modifiableUserDirectory.add(userInformation);
            fail("A non complete addition should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
        }

        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "test");
            userInformation.put("lastname", "test");
            userInformation.put("email", "");
            modifiableUserDirectory.add(userInformation);
            fail("A non complete addition should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
        }

        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "test");
            userInformation.put("lastname", "test");
            userInformation.put("password", "test");
            userInformation.put("email", "testthatisnotacorrectemail");
            modifiableUserDirectory.add(userInformation);
            fail("An incorrect addition should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
        }
    }
    
    /**
     * Test the addition of a new user
     * @throws Exception if an error occurs
     */
    public void testCorrectAdd() throws Exception
    {
        ModifiableUserDirectory modifiableUserDirectory = (ModifiableUserDirectory) _userDirectory;
        User user;
        
        // Correct additions
        Map<String, String> userInformation;
        
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Test");
        userInformation.put("lastname", "TEST");
        userInformation.put("email", "");
        userInformation.put("password", "testpassword");
        modifiableUserDirectory.add(userInformation);
        assertTrue(_userDirectory.checkCredentials(new Credentials("test", "testpassword")));
        assertFalse(_userDirectory.checkCredentials(new Credentials("test", "wrongpassword")));
        assertFalse(_userDirectory.checkCredentials(new Credentials("test2", "testpassword")));
        
        userInformation = new HashMap<>();
        userInformation.put("login", "test2");
        userInformation.put("firstname", "Test2");
        userInformation.put("lastname", "TEST2");
        userInformation.put("email", "test2@test.te");
        userInformation.put("password", "testpassword");
        modifiableUserDirectory.add(userInformation);
        assertTrue(_userDirectory.checkCredentials(new Credentials("test", "testpassword")));
        assertFalse(_userDirectory.checkCredentials(new Credentials("test", "wrongpassword")));
        assertTrue(_userDirectory.checkCredentials(new Credentials("test2", "testpassword")));
        
        user = _userDirectory.getUser("test");
        assertNotNull(user);
        assertEquals(user.getIdentity().getLogin(), "test");
        assertEquals(user.getLastName(), "TEST"); 
        assertEquals(user.getFirstName(), "Test"); 
        assertEquals(user.getFullName(), "Test TEST");
        assertEquals(user.getSortableName(), "TEST Test");
        assertEquals(user.getEmail(), "");

        user = _userDirectory.getUser("test2");
        assertNotNull(user);
        assertEquals(user.getIdentity().getLogin(), "test2");
        assertEquals(user.getLastName(), "TEST2"); 
        assertEquals(user.getFirstName(), "Test2"); 
        assertEquals(user.getFullName(), "Test2 TEST2");
        assertEquals(user.getSortableName(), "TEST2 Test2");
        assertEquals(user.getEmail(), "test2@test.te");

        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "Test");
            userInformation.put("lastname", "TEST");
            userInformation.put("email", "");
            userInformation.put("password", "testpassword");
            modifiableUserDirectory.add(userInformation);
            fail("Add should have failed");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior since login already exists
        }
    }
    
    /**
     * Test the update of a user incorrectly
     * @throws Exception if an error occurs
     */
    public void testIncorrectUpdate() throws Exception
    {
        ModifiableUserDirectory modifiableUserDirectory = (ModifiableUserDirectory) _userDirectory;
        
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
            modifiableUserDirectory.update(userInformation);
            fail("Update should have failed");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior since login does not exist
        }
        
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Test");
        userInformation.put("lastname", "TEST");
        userInformation.put("email", "");
        userInformation.put("password", "testpassword");
        modifiableUserDirectory.add(userInformation);
        
        try
        {
            userInformation = new HashMap<>();
            userInformation.put("firstname", "Test");
            userInformation.put("lastname", "TEST");
            userInformation.put("email", "");
            userInformation.put("password", "testpassword");
            modifiableUserDirectory.update(userInformation);
            fail("Update should have failed");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior since no login is given
        }

        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "Test");
            userInformation.put("lastname", "TEST");
            userInformation.put("email", "incorrectemail");
            userInformation.put("password", "testpassword");
            modifiableUserDirectory.update(userInformation);
            fail("Update should have failed");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior since email is incorrect
        }
        
        try
        {
            userInformation = new HashMap<>();
            userInformation.put("login", "test");
            userInformation.put("firstname", "Test");
            userInformation.put("lastname", "TEST");
            userInformation.put("password", "");
            modifiableUserDirectory.update(userInformation);
            fail("Update should have failed");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior since password is empty
        }
        
        assertTrue(_userDirectory.checkCredentials(new Credentials("test", "testpassword")));
        assertFalse(_userDirectory.checkCredentials(new Credentials("test", "wrongpassword")));
        assertFalse(_userDirectory.checkCredentials(new Credentials("test2", "testpassword")));
    }
    
    /**
     * Test the update of a user
     * @throws Exception if an error occurs
     */
    public void testCorrectUpdate() throws Exception
    {
        ModifiableUserDirectory modifiableUserDirectory = (ModifiableUserDirectory) _userDirectory;
        User user;
        
        Map<String, String> userInformation;
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Test");
        userInformation.put("lastname", "TEST");
        userInformation.put("email", "");
        userInformation.put("password", "testpassword");
        modifiableUserDirectory.add(userInformation);

        // Correct modification
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Testmodified");
        userInformation.put("lastname", "TESTMODIFIED");
        userInformation.put("email", "testModified@test.te");
        userInformation.put("password", "testpassword2");
        modifiableUserDirectory.update(userInformation);
        assertFalse(_userDirectory.checkCredentials(new Credentials("test", "testpassword")));
        assertTrue(_userDirectory.checkCredentials(new Credentials("test", "testpassword2")));
        
        user = _userDirectory.getUser("test");
        assertNotNull(user);
        assertEquals(user.getIdentity().getLogin(), "test");
        assertEquals(user.getLastName(), "TESTMODIFIED"); 
        assertEquals(user.getFirstName(), "Testmodified"); 
        assertEquals(user.getFullName(), "Testmodified TESTMODIFIED");
        assertEquals(user.getSortableName(), "TESTMODIFIED Testmodified");
        assertEquals(user.getEmail(), "testModified@test.te");

        // partial modification (no password change)
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Testmodifiedtwice");
        modifiableUserDirectory.update(userInformation);
        assertFalse(_userDirectory.checkCredentials(new Credentials("test", "testpassword")));
        assertTrue(_userDirectory.checkCredentials(new Credentials("test", "testpassword2")));
        
        user = _userDirectory.getUser("test");
        assertNotNull(user);
        assertEquals(user.getIdentity().getLogin(), "test");
        assertEquals(user.getLastName(), "TESTMODIFIED"); 
        assertEquals(user.getFirstName(), "Testmodifiedtwice"); 
        assertEquals(user.getFullName(), "Testmodifiedtwice TESTMODIFIED");
        assertEquals(user.getSortableName(), "TESTMODIFIED Testmodifiedtwice");
        assertEquals(user.getEmail(), "testModified@test.te");
    }
    
    /**
     * Test incorrects remove
     * @throws Exception if an error occurs
     */
    public void testIncorrectRemove() throws Exception
    {
        ModifiableUserDirectory modifiableUserDirectory = (ModifiableUserDirectory) _userDirectory;
        
        try
        {
            modifiableUserDirectory.remove("foo");
            fail("Remove should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
        }

        Map<String, String> userInformation;
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Test");
        userInformation.put("lastname", "TEST");
        userInformation.put("password", "testpassword");
        userInformation.put("email", "");
        modifiableUserDirectory.add(userInformation);

        try
        {
            modifiableUserDirectory.remove("foo");
            fail("Remove should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            User user = _userDirectory.getUser("test");
            assertNotNull(user);
        }

        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Testmodified");
        userInformation.put("lastname", "TESTMODIFIED");
        userInformation.put("email", "testModified@test.te");
        userInformation.put("password", "testpassword2");
        modifiableUserDirectory.update(userInformation);

        try
        {
            modifiableUserDirectory.remove("foo");
            fail("Remove should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            User user = _userDirectory.getUser("test");
            assertNotNull(user);
        }

        assertTrue(_userDirectory.checkCredentials(new Credentials("test", "testpassword2")));
    }
    
    /**
     * Test corrects remove
     * @throws Exception if an error occurs
     */
    public void testCorrectRemove() throws Exception
    {
        ModifiableUserDirectory modifiableUserDirectory = (ModifiableUserDirectory) _userDirectory;
        
        Map<String, String> userInformation;
        userInformation = new HashMap<>();
        userInformation.put("login", "test");
        userInformation.put("firstname", "Test");
        userInformation.put("lastname", "TEST");
        userInformation.put("email", "");
        userInformation.put("password", "testpassword");
        modifiableUserDirectory.add(userInformation);

        userInformation = new HashMap<>();
        userInformation.put("login", "test2");
        userInformation.put("firstname", "Test2");
        userInformation.put("lastname", "TEST2");
        userInformation.put("email", "email@email.ma");
        userInformation.put("password", "test2password");
        modifiableUserDirectory.add(userInformation);

        modifiableUserDirectory.remove("test");
        assertFalse(_userDirectory.checkCredentials(new Credentials("test", "testpassword")));
        assertTrue(_userDirectory.checkCredentials(new Credentials("test2", "test2password")));
        
        User user = _userDirectory.getUser("test2");
        assertNotNull(user);
        assertEquals(user.getIdentity().getLogin(), "test2");
        assertEquals(user.getLastName(), "TEST2"); 
        assertEquals(user.getFirstName(), "Test2"); 
        assertEquals(user.getFullName(), "Test2 TEST2");
        assertEquals(user.getSortableName(), "TEST2 Test2");
        assertEquals(user.getEmail(), "email@email.ma");
    }
}
