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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ametys.runtime.test.groups.jdbc.MysqlJdbcGroupsTestCase;
import org.ametys.runtime.test.rights.profile.MysqlProfileBasedRightsManagerTestCase;
import org.ametys.runtime.test.rights.profile.hierarchical.MysqlHierarchicalProfileBasedRightsManagerTestCase;
import org.ametys.runtime.test.users.jdbc.MysqlJdbcUsersTestCase;
import org.ametys.runtime.test.users.jdbc.credentialsaware.MysqlCredentialsAwareJdbcUsersTestCase;
import org.ametys.runtime.test.users.jdbc.modifiable.MysqlModifiableJdbcUsersTestCase;
import org.ametys.runtime.test.users.jdbc.modifiablecredentialsaware.MysqlModifiableCredentialsAwareJdbcUsersTestCase;

/**
 * Test suite grouping all Mysql-based tests
 */
public final class AllMysqlTestSuite
{
    private AllMysqlTestSuite()
    {
        // empty constructor
    }
    
    /**
     * Test suite
     * @return the Test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("All MySQL tests");
        
        //$JUnit-BEGIN$
        suite.addTestSuite(MysqlJdbcUsersTestCase.class);
        suite.addTestSuite(MysqlModifiableJdbcUsersTestCase.class);
        suite.addTestSuite(MysqlCredentialsAwareJdbcUsersTestCase.class);
        suite.addTestSuite(MysqlModifiableCredentialsAwareJdbcUsersTestCase.class);
        
        suite.addTestSuite(MysqlJdbcGroupsTestCase.class);
        
        suite.addTestSuite(MysqlProfileBasedRightsManagerTestCase.class);
        suite.addTestSuite(MysqlHierarchicalProfileBasedRightsManagerTestCase.class);
        //$JUnit-END$
        
        return suite;
    }
}
