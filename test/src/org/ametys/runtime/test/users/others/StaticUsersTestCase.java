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
package org.ametys.runtime.test.users.others;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ametys.core.user.User;
import org.ametys.core.user.directory.ModifiableUserDirectory;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.directory.UserDirectoryFactory;
import org.ametys.plugins.core.impl.user.directory.StaticUserDirectory;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;

/**
 * Tests the DefinedUsersTestCase
 */
public class StaticUsersTestCase extends AbstractRuntimeTestCase
{
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp1");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Tests that the <code>StaticUsersTestCase</code> is the default <code>UserDirectory</code><br/>
     * Tests the values returned by it
     * @throws Exception if an error occurs
     */
    public void testStaticUsers() throws Exception
    {
        UserDirectory userDirectory = _createStaticUserDirectory();
        
        // DEFAULT IMPL
        assertTrue(userDirectory instanceof StaticUserDirectory);
        
        // NOT MODIFIABLE
        assertFalse(userDirectory instanceof ModifiableUserDirectory);
        
        // ONE USER
        User user;
        
        user = userDirectory.getUser("foo");
        assertNull(user);
        
        user = userDirectory.getUser("anonymous");
        assertNotNull(user);
        assertEquals(user.getIdentity().getLogin(), "anonymous");
        assertEquals(user.getLastName(), "Anonymous");
        assertEquals(user.getFirstName(), "user");
        assertEquals(user.getFullName(), "user Anonymous");
        assertEquals(user.getSortableName(), "Anonymous user");
        assertEquals(user.getEmail(), "user@ametys.org");
        
        // ALL USERS
        Collection<User> users = userDirectory.getUsers();
        assertEquals(users.size(), 1);
        assertEquals(users.iterator().next(), user);
        
        // LOGIN
        assertFalse(userDirectory.checkCredentials("foo", null));

        assertTrue(userDirectory.checkCredentials("anonymous", null));
    }
    
    private UserDirectory _createStaticUserDirectory() throws Exception
    {
        String modelId = "org.ametys.plugins.core.user.directory.Static";
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("runtime.users.static.users", "anonymous:Anonymous:user:user@ametys.org");
        return ((UserDirectoryFactory) Init.getPluginServiceManager().lookup(UserDirectoryFactory.ROLE)).createUserDirectory(modelId, parameters, "foo", null);
    }
}
