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
package org.ametys.runtime.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ametys.runtime.test.administrator.jvmstatus.RequestCountListenerTestCase;
import org.ametys.runtime.test.administrator.jvmstatus.SessionCountListenerTestCase;
import org.ametys.runtime.test.cocoon.XHTMLSerializerTestCase;
import org.ametys.runtime.test.groups.jdbc.JdbcGroupsTestCase;
import org.ametys.runtime.test.groups.ldap.GroupDrivenLdapGroupsTestCase;
import org.ametys.runtime.test.groups.ldap.UserDrivenLdapGroupsTestCase;
import org.ametys.runtime.test.groups.others.EmptyGroupsTestCase;
import org.ametys.runtime.test.plugins.PluginsTestCase;
import org.ametys.runtime.test.rights.basic.BasicRightsManagerTestCase;
import org.ametys.runtime.test.rights.profile.DefaultProfileBasedRightsManagerTestCase;
import org.ametys.runtime.test.rights.profile.HierarchicalProfileBasedRightsManagerTestCase;
import org.ametys.runtime.test.ui.DesktopManagerTestCase;
import org.ametys.runtime.test.ui.StaticUIItemFactoryTestCase;
import org.ametys.runtime.test.users.jdbc.CredentialAwareJdbcUsersTestCase;
import org.ametys.runtime.test.users.jdbc.JdbcUsersTestCase;
import org.ametys.runtime.test.users.jdbc.ModifiableCredentialAwareJdbcUsersTestCase;
import org.ametys.runtime.test.users.jdbc.ModifiableJdbcUsersTestCase;
import org.ametys.runtime.test.users.ldap.CredentialAwareLdapUsersTestCase;
import org.ametys.runtime.test.users.ldap.LdapUsersTestCase;
import org.ametys.runtime.test.users.others.StaticUsersTestCase;
import org.ametys.runtime.test.util.DateConversionTestCase;

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
        
        suite.addTestSuite(RequestCountListenerTestCase.class);
        suite.addTestSuite(SessionCountListenerTestCase.class);
        
        suite.addTestSuite(XHTMLSerializerTestCase.class);
        
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

        suite.addTestSuite(DateConversionTestCase.class);
        //$JUnit-END$
        return suite;
    }
}
