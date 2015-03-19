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
package org.ametys.runtime.test.users.jdbc;

import java.io.File;

/**
 * Derby-specific unmodifiable JDBC UsersManager test case.
 */
public class HsqlJdbcUsersTestCase extends AbstractJdbcUnmodifiableUsersTestCase
{

    @Override
    protected void setUp() throws Exception
    {
        _resetDB("runtime4.xml", "config_hsqldb.xml");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
    }
    
    @Override
    protected File[] getScripts()
    {
        // Use non auth script by default
        return new File[] {
            new File("test/environments/scripts/jdbc-hsqldb/dropTables.sql"),
            new File("main/plugin-core/scripts/hsqldb/jdbc_users.sql"),
            new File("main/plugin-core/scripts/hsqldb/jdbc_groups.sql"),
            new File("main/plugin-core/scripts/hsqldb/profile_rights.sql")
        };
    }
    
    @Override
    protected File[] getPopulateScripts()
    {
        return new File[] {new File("test/environments/scripts/jdbc-hsqldb/fillJDBCUsers.sql")};
    }

}