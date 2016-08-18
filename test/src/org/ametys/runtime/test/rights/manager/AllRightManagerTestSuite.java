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
package org.ametys.runtime.test.rights.manager;

import org.ametys.core.right.RightManager;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for {@link RightManager} test.
 */
public final class AllRightManagerTestSuite extends TestSuite
{
    
    private AllRightManagerTestSuite()
    {
        // empty constructor
    }
    
    /**
     * Test suite
     * @return the Test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test RightManager with all DBMS");
        
        //$JUnit-BEGIN$
        suite.addTestSuite(MysqlRightManagerTestCase.class);
        suite.addTestSuite(PostgresRightManagerTestCase.class);
        suite.addTestSuite(OracleRightManagerTestCase.class);
        suite.addTestSuite(DerbyRightManagerTestCase.class);
        suite.addTestSuite(HsqlRightManagerTestCase.class);
        //$JUnit-END$
        
        return suite;
    }
    
}
