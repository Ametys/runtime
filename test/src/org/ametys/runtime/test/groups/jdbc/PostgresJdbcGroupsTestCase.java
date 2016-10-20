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
package org.ametys.runtime.test.groups.jdbc;

import java.io.File;

/**
 * Postgres-specific JDBC GroupsManager test case.
 */
public class PostgresJdbcGroupsTestCase extends AbstractJdbcGroupsTestCase
{
    @Override
    protected String _getDataSourceFile()
    {
        return "test/environments/datasources/datasource-postgresql.xml";
    }
    
    @Override
    protected File[] _getStartScripts()
    {
        return new File[] {
            new File("test/environments/scripts/jdbc-postgres/dropTables.sql"),
        };
    }

    @Override
    protected File[] getPopulateScripts()
    {
        return new File[] {new File("test/environments/scripts/jdbc-postgres/fillJDBCUsersAndGroups.sql")};
    }
}
