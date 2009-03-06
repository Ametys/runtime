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
package org.ametys.runtime.test.rights.basic;

import java.util.Collection;
import java.util.Set;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugins.core.right.BasicRightsManager;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;

/**
 * Tests the BasicRightsManagerTestCase
 */
public class BasicRightsManagerTestCase extends AbstractRuntimeTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime3.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        _startCocoon("test/environments/webapp1");
    }
    
    /**
     * Tests that the <code>BasicRightsManager</code> is the default <code>RightsManager</code><br>
     * Tests the values returned by it
     * @throws Exception
     */
    public void testBasicRightsManager() throws Exception
    {
        RightsManager rightsManager = (RightsManager) Init.getPluginServiceManager().lookup(RightsManager.ROLE);

        // Default impl
        assertTrue(rightsManager instanceof BasicRightsManager);
        
        // GET USER RIGHTS
        Set<String> rightsIds = rightsManager.getUserRights("any", "any");
        assertEquals(4, rightsIds.size());
        assertTrue(rightsIds.contains("Runtime_Rights_User_Handle"));
        assertTrue(rightsIds.contains("Runtime_Rights_Group_Handle"));
        assertTrue(rightsIds.contains("Runtime_Rights_Rights_Profile_Handle"));
        assertTrue(rightsIds.contains("Runtime_Rights_Rights_Handle"));
        
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
