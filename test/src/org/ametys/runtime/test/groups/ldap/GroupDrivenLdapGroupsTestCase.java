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
package org.ametys.runtime.test.groups.ldap;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.group.ModifiableGroupsManager;
import org.ametys.runtime.plugins.core.group.ldap.GroupDrivenLdapGroupsManager;
import org.ametys.runtime.test.Init;

/**
 * Tests the LdapGroupsManager
 */
public class GroupDrivenLdapGroupsTestCase extends AbstractLdapGroupsTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime8.xml");
        Config.setFilename("test/environments/configs/config3.xml");
        
        _startCocoon("test/environments/webapp1");

        _groupsManager = (GroupsManager) Init.getPluginServiceManager().lookup(GroupsManager.ROLE);
    }
    
    /**
     * Test the choosen implementation
     * @throws Exception if an error occurs
     */
    public void testType() throws Exception
    {
        // DEFAULT IMPL
        assertTrue(_groupsManager instanceof GroupDrivenLdapGroupsManager);

        // MODIFIABLE
        assertFalse(_groupsManager instanceof ModifiableGroupsManager);
    }
}
