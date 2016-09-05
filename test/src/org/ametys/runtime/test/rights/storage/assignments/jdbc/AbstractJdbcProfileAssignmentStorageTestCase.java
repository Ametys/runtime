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
package org.ametys.runtime.test.rights.storage.assignments.jdbc;

import org.ametys.core.right.ProfileAssignmentStorage;
import org.ametys.plugins.core.impl.right.JdbcProfileAssignmentStorage;
import org.ametys.runtime.test.rights.storage.assignments.AbstractProfileAssignmentStorageTestCase;

/**
 * Common test class for testing the {@link JdbcProfileAssignmentStorage} 
 */
public abstract class AbstractJdbcProfileAssignmentStorageTestCase extends AbstractProfileAssignmentStorageTestCase
{
    @Override
    protected String _getExtensionId()
    {
        return "profile.assignment.test.JdbcProfileAssignmentStorage1";
    }
    
    @Override
    protected Object _getTest1()
    {
        return "test1";
    }
    
    @Override
    protected Object _getTest2()
    {
        return "test2";
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        _startApplication("test/environments/runtimes/runtime4.xml", "test/environments/configs/config1.xml", null, "test/environments/webapp4");
    }
    
    /**
     * Tests the support of String
     */
    public void testSupport()
    {
        assertFalse(_profileAssignmentStorage.isSupported(""));
        assertFalse(_profileAssignmentStorage.isSupported("foo"));
        assertTrue(_profileAssignmentStorage.isSupported("/test"));
        assertFalse(_profileAssignmentStorage.isSupported(new Object()));
    }
    
    /**
     * Tests the priority is min
     */
    public void testPriority()
    {
        assertEquals(ProfileAssignmentStorage.MIN_PRIORITY, _profileAssignmentStorage.getPriority());
    }
}
