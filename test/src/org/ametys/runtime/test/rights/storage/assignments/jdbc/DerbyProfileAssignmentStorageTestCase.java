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

import java.io.File;

/**
 * Derby-specific JDBC ProfileAssignement storage test case.
 */
public class DerbyProfileAssignmentStorageTestCase extends AbstractJdbcProfileAssignmentStorageTestCase
{
    @Override
    protected void setUp() throws Exception
    {
        _startAppAndResetDB("runtime4.xml", "config1.xml", "datasource-derby.xml");
        super.setUp();
    }
    
    @Override
    protected File[] getScripts()
    {
        return new File[] {
            new File("test/environments/scripts/jdbc-derby/dropTables.sql"),
            new File("main/plugin-core/scripts/derby/profile_assignments.sql")
        };
    }
}
