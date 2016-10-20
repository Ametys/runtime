/*
 *  Copyright 2009 Anyware Services
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

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.List;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.script.SQLScriptHelper;

/**
 * Abstract test case for jdbc Runtime test cases.
 */
public abstract class AbstractJDBCTestCase extends AbstractRuntimeTestCase
{
    /**
     * Reset the database and insert the scripts. Needs the Init class.
     * @param scripts A list of script to play on db. Scripts should reset tables.
     * @throws Exception if an error occurs
     */
    protected void _setDatabase(List<File> scripts) throws Exception
    {
        Connection connection = null;
        
        try
        {
            connection = ConnectionHelper.getConnection("SQL-test");
            
            for (File script : scripts)
            {
                SQLScriptHelper.runScript(connection, new FileInputStream(script));
            }
        }
        finally 
        {
            ConnectionHelper.cleanup(connection);
        }
    }
}
