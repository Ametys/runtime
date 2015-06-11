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
package org.ametys.runtime.test.rights.basic;

import java.util.Collection;
import java.util.Set;

import org.ametys.core.right.RightsManager;
import org.ametys.core.user.User;
import org.ametys.core.user.UsersManager;
import org.ametys.plugins.core.impl.right.BasicRightsManager;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;

/**
 * Tests the BasicRightsManagerTestCase
 */
public class BasicRightsManagerTestCase extends AbstractRuntimeTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        _startApplication("test/environments/runtimes/runtime12.xml", "test/environments/configs/config1.xml", "test/environments/webapp1");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Tests that the <code>BasicRightsManager</code> is the default <code>RightsManager</code><br>
     * Tests the values returned by it
     * @throws Exception if an error occurs
     */
    public void testBasicRightsManager() throws Exception
    {
        RightsManager rightsManager = (RightsManager) Init.getPluginServiceManager().lookup(RightsManager.ROLE);

        // Default impl
        assertTrue(rightsManager instanceof BasicRightsManager);
        
        // GET USER RIGHTS
        Set<String> rightsIds = rightsManager.getUserRights("any", "any");
        // one right from a test feature, and two from the plugin core
        assertEquals(3, rightsIds.size());
        assertTrue(rightsIds.contains("Test_Right"));
        
        // GET GRANTED USERS
        Set<String> usersIds = rightsManager.getGrantedUsers("any", "any");

        UsersManager usersManager = (UsersManager) Init.getPluginServiceManager().lookup(UsersManager.ROLE);
        Collection<User> users = usersManager.getUsers();
        for (User user : users)
        {
            assertTrue(usersIds.contains(user.getName()));
        }
        
        assertEquals(usersIds.size(), users.size());
    }
}
