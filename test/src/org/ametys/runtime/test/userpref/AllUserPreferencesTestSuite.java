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
package org.ametys.runtime.test.userpref;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for UserPreferences.
 */
public final class AllUserPreferencesTestSuite extends TestSuite
{
    
    private AllUserPreferencesTestSuite()
    {
        // empty constructor
    }
    
    /**
     * Test suite
     * @return the Test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test UserPreferences with all DBMS");
        
        //$JUnit-BEGIN$
        suite.addTestSuite(MysqlUserPreferencesTestCase.class);
        suite.addTestSuite(PostgresUserPreferencesTestCase.class);
        suite.addTestSuite(OracleUserPreferencesTestCase.class);
        suite.addTestSuite(DerbyUserPreferencesTestCase.class);
        suite.addTestSuite(HsqlUserPreferencesTestCase.class);
        //$JUnit-END$
        
        return suite;
    }
    
}
