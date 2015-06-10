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
package org.ametys.runtime.test.groups.ldap;

import org.ametys.core.group.GroupsManager;
import org.ametys.core.group.ModifiableGroupsManager;
import org.ametys.plugins.core.impl.group.ldap.GroupDrivenLdapGroupsManager;
import org.ametys.runtime.test.Init;

/**
 * Tests the LdapGroupsManager
 */
public class GroupDrivenLdapGroupsTestCase extends AbstractLdapGroupsTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        _startApplication("test/environments/runtimes/runtime8.xml", "test/environments/configs/config3.xml", "test/environments/webapp1");

        _groupsManager = (GroupsManager) Init.getPluginServiceManager().lookup(GroupsManager.ROLE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
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
