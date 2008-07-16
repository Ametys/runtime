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
package org.ametys.runtime.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ametys.runtime.test.groups.jdbc.JdbcGroupsTestCase;
import org.ametys.runtime.test.groups.ldap.GroupDrivenLdapGroupsTestCase;
import org.ametys.runtime.test.groups.ldap.UserDrivenLdapGroupsTestCase;
import org.ametys.runtime.test.groups.others.EmptyGroupsTestCase;
import org.ametys.runtime.test.plugins.PluginsTestCase;
import org.ametys.runtime.test.rights.basic.BasicRightsManagerTestCase;
import org.ametys.runtime.test.rights.profile.DefaultProfileBasedRightsManagerTestCase;
import org.ametys.runtime.test.rights.profile.HierarchicalProfileBasedRightsManagerTestCase;
import org.ametys.runtime.test.ui.DesktopManagerTestCase;
import org.ametys.runtime.test.ui.MenusManagerTestCase;
import org.ametys.runtime.test.ui.StaticUIItemFactoryTestCase;
import org.ametys.runtime.test.ui.ToolbarsManagerTestCase;
import org.ametys.runtime.test.users.jdbc.CredentialAwareJdbcUsersTestCase;
import org.ametys.runtime.test.users.jdbc.JdbcUsersTestCase;
import org.ametys.runtime.test.users.jdbc.ModifiableCredentialAwareJdbcUsersTestCase;
import org.ametys.runtime.test.users.jdbc.ModifiableJdbcUsersTestCase;
import org.ametys.runtime.test.users.ldap.CredentialAwareLdapUsersTestCase;
import org.ametys.runtime.test.users.ldap.LdapUsersTestCase;
import org.ametys.runtime.test.users.others.StaticUsersTestCase;

/**
 * Test suite grouping all Runtime tests
 */
public final class AllTests
{
    private AllTests()
    {
        // empty constructor
    }
    
    /**
     * Test suite
     * @return the Test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for org.ametys.runtime.test");
        
        //$JUnit-BEGIN$
        suite.addTestSuite(RuntimeConfigTestCase.class);
        suite.addTestSuite(WorkspacesTestCase.class);
        suite.addTestSuite(ConfigManagerTestCase.class);
        
        suite.addTestSuite(PluginsTestCase.class);
        
        suite.addTestSuite(StaticUsersTestCase.class);
        suite.addTestSuite(JdbcUsersTestCase.class);
        suite.addTestSuite(ModifiableJdbcUsersTestCase.class);
        suite.addTestSuite(ModifiableCredentialAwareJdbcUsersTestCase.class);
        suite.addTestSuite(CredentialAwareJdbcUsersTestCase.class);
        suite.addTestSuite(LdapUsersTestCase.class);
        suite.addTestSuite(CredentialAwareLdapUsersTestCase.class);

        suite.addTestSuite(EmptyGroupsTestCase.class);
        suite.addTestSuite(JdbcGroupsTestCase.class);
        suite.addTestSuite(GroupDrivenLdapGroupsTestCase.class);
        suite.addTestSuite(UserDrivenLdapGroupsTestCase.class);

        suite.addTestSuite(BasicRightsManagerTestCase.class);
        suite.addTestSuite(DefaultProfileBasedRightsManagerTestCase.class);
        suite.addTestSuite(HierarchicalProfileBasedRightsManagerTestCase.class);
        
        suite.addTestSuite(StaticUIItemFactoryTestCase.class);
        suite.addTestSuite(DesktopManagerTestCase.class);
        suite.addTestSuite(MenusManagerTestCase.class);
        suite.addTestSuite(ToolbarsManagerTestCase.class);
        //$JUnit-END$
        return suite;
    }
}
