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
package org.ametys.runtime.test.userpref;

import org.ametys.core.datasource.ConnectionHelper;

/**
 * MySQL-specific user preferences test case.
 */
public class MysqlUserPreferencesTestCase extends AbstractUserPreferencesTestCase
{
    @Override
    protected String _getDBType()
    {
        return ConnectionHelper.DATABASE_MYSQL;
    }
}
