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
package org.ametys.runtime.test.users.jdbc.modifiablecredentialsaware;

import java.io.File;

/**
 * MySQL-specific modifiable credentials aware JDBC UsersManager test case.
 */
public class MysqlModifiableCredentialsAwareJdbcUsersTestCase extends AbstractModifiableCredentialsAwareJdbcUsersTestCase
{

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        _resetDB("runtime6.xml", "config1.xml");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    @Override
    protected File[] getScripts()
    {
        return new File[] {new File("main/plugin-core/scripts/mysql/jdbc_users_auth.sql")};
    }
    
}
