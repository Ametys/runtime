/*
 *  Copyright 2016 Anyware Services
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

import org.ametys.runtime.test.administrator.jvmstatus.RequestCountListenerTestCase;
import org.ametys.runtime.test.administrator.jvmstatus.SessionCountListenerTestCase;
import org.ametys.runtime.test.cocoon.XHTMLSerializerTestCase;
import org.ametys.runtime.test.groups.jdbc.AllGroupsTestSuite;
import org.ametys.runtime.test.groups.ldap.GroupDrivenLdapGroupsTestCase;
import org.ametys.runtime.test.groups.ldap.UserDrivenLdapGroupsTestCase;
import org.ametys.runtime.test.minimize.MinimizeTransformerTestCase;
import org.ametys.runtime.test.observers.ObserversTestCase;
import org.ametys.runtime.test.plugins.PluginsTestCase;
import org.ametys.runtime.test.resources.CompiledResourceReaderTestCase;
import org.ametys.runtime.test.rights.basic.BasicRightsManagerTestCase;
import org.ametys.runtime.test.rights.profile.AllProfileBasedRightsManagerTestSuite;
import org.ametys.runtime.test.rights.profile.hierarchical.AllHierarchicalProfileBasedRightsManagerTestSuite;
import org.ametys.runtime.test.ui.StaticUIItemFactoryTestCase;
import org.ametys.runtime.test.userpref.AllUserPreferencesTestSuite;
import org.ametys.runtime.test.users.jdbc.modifiablecredentialsaware.AllModifiableCredentialsAwareJdbcUsersTestSuite;
import org.ametys.runtime.test.users.ldap.CredentialAwareLdapUsersTestCase;
import org.ametys.runtime.test.users.ldap.LdapUsersTestCase;
import org.ametys.runtime.test.users.others.StaticUsersTestCase;
import org.ametys.runtime.test.util.DateConversionTestCase;
import org.ametys.runtime.test.util.I18nTestCase;
import org.ametys.runtime.test.util.JSONTestCase;
import org.ametys.runtime.test.workspaces.WorkspaceGeneratorTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

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
        suite.addTestSuite(SafeModeTestCase.class);
        suite.addTestSuite(AmetysHomeTestCase.class);
        suite.addTestSuite(RuntimeConfigTestCase.class);
        suite.addTestSuite(WorkspacesTestCase.class);
        suite.addTestSuite(ConfigManagerTestCase.class);
        
        suite.addTestSuite(PluginsTestCase.class);
        
        suite.addTestSuite(RequestCountListenerTestCase.class);
        suite.addTestSuite(SessionCountListenerTestCase.class);
        
        suite.addTestSuite(XHTMLSerializerTestCase.class);
        
        suite.addTestSuite(StaticUsersTestCase.class);
        suite.addTest(AllModifiableCredentialsAwareJdbcUsersTestSuite.suite());
        suite.addTestSuite(LdapUsersTestCase.class);
        suite.addTestSuite(CredentialAwareLdapUsersTestCase.class);

        suite.addTest(AllGroupsTestSuite.suite());
        suite.addTestSuite(GroupDrivenLdapGroupsTestCase.class);
        suite.addTestSuite(UserDrivenLdapGroupsTestCase.class);

        suite.addTestSuite(BasicRightsManagerTestCase.class);
        suite.addTest(AllProfileBasedRightsManagerTestSuite.suite());
        suite.addTest(AllHierarchicalProfileBasedRightsManagerTestSuite.suite());

        suite.addTestSuite(CompiledResourceReaderTestCase.class);

        suite.addTest(AllUserPreferencesTestSuite.suite());
        
        suite.addTestSuite(StaticUIItemFactoryTestCase.class);

        suite.addTestSuite(DateConversionTestCase.class);
        suite.addTestSuite(JSONTestCase.class);
        suite.addTestSuite(I18nTestCase.class);
        suite.addTestSuite(MinimizeTransformerTestCase.class);
        suite.addTestSuite(ObserversTestCase.class);

        suite.addTestSuite(WorkspaceGeneratorTestCase.class);
        
        //$JUnit-END$
        return suite;
    }
}
