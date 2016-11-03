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

import org.ametys.runtime.test.groups.jdbc.OracleJdbcGroupsTestCase;
import org.ametys.runtime.test.rights.manager.OracleRightManagerTestCase;
import org.ametys.runtime.test.userpref.OracleUserPreferencesTestCase;
import org.ametys.runtime.test.users.jdbc.OracleJdbcUsersTestCase;

/**
 * Test suite grouping all Runtime tests
 */
public final class AllOracleTestSuite
{
    private AllOracleTestSuite()
    {
        // empty constructor
    }
    
    /**
     * Test suite
     * @return the Test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("All Oracle-based tests");
        
        //$JUnit-BEGIN$
        suite.addTestSuite(OracleJdbcUsersTestCase.class);
        
        suite.addTestSuite(OracleJdbcGroupsTestCase.class);
        
        suite.addTestSuite(OracleRightManagerTestCase.class);
        
        suite.addTestSuite(OracleUserPreferencesTestCase.class);
        //$JUnit-END$
        
        return suite;
    }
}
