/*
 *  Copyright 2011 Anyware Services
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

import org.ametys.runtime.test.groups.jdbc.DerbyJdbcGroupsTestCase;
import org.ametys.runtime.test.rights.profile.DerbyProfileBasedRightsManagerTestCase;
import org.ametys.runtime.test.rights.profile.hierarchical.DerbyHierarchicalProfileBasedRightsManagerTestCase;
import org.ametys.runtime.test.userpref.DerbyUserPreferencesTestCase;
import org.ametys.runtime.test.users.jdbc.DerbyJdbcUsersTestCase;
import org.ametys.runtime.test.users.jdbc.credentialsaware.DerbyCredentialsAwareJdbcUsersTestCase;
import org.ametys.runtime.test.users.jdbc.modifiable.DerbyModifiableJdbcUsersTestCase;
import org.ametys.runtime.test.users.jdbc.modifiablecredentialsaware.DerbyModifiableCredentialsAwareJdbcUsersTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite grouping all Runtime tests
 */
public final class AllDerbyTestSuite
{
    private AllDerbyTestSuite()
    {
        // empty constructor
    }
    
    /**
     * Test suite
     * @return the Test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("All Derby-based tests");
        
        //$JUnit-BEGIN$
        suite.addTestSuite(DerbyJdbcUsersTestCase.class);
        suite.addTestSuite(DerbyModifiableJdbcUsersTestCase.class);
        suite.addTestSuite(DerbyCredentialsAwareJdbcUsersTestCase.class);
        suite.addTestSuite(DerbyModifiableCredentialsAwareJdbcUsersTestCase.class);
        
        suite.addTestSuite(DerbyJdbcGroupsTestCase.class);
        
        suite.addTestSuite(DerbyProfileBasedRightsManagerTestCase.class);
        suite.addTestSuite(DerbyHierarchicalProfileBasedRightsManagerTestCase.class);
        
        suite.addTestSuite(DerbyUserPreferencesTestCase.class);
        //$JUnit-END$
        
        return suite;
    }
}
