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
package org.ametys.runtime.test.rights;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ametys.runtime.test.rights.access.controller.AllContributorAccessControllerTestSuite;
import org.ametys.runtime.test.rights.manager.AllRightManagerTestSuite;
import org.ametys.runtime.test.rights.storage.assignments.jdbc.AllJdbcProfileAssignmentStorageTestSuite;

/**
 * Test suite for all features of the right system.
 */
public final class AllRightsTestSuite extends TestSuite
{
    private AllRightsTestSuite()
    {
        // empty constructor
    }
    
    /**
     * Test suite
     * @return the Test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test all features of the right system");
        
        //$JUnit-BEGIN$
        suite.addTest(AllJdbcProfileAssignmentStorageTestSuite.suite());
        suite.addTest(AllContributorAccessControllerTestSuite.suite());
        suite.addTest(AllRightManagerTestSuite.suite());
        //$JUnit-END$
        
        return suite;
    }
}
