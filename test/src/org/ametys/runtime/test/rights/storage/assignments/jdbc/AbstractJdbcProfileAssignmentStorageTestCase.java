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
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.right.ProfileAssignmentStorage;
import org.ametys.core.script.ScriptRunner;
import org.ametys.plugins.core.impl.right.JdbcProfileAssignmentStorage;
import org.ametys.runtime.test.rights.storage.assignments.AbstractProfileAssignmentStorageTestCase;

/**
 * Common test class for testing the {@link JdbcProfileAssignmentStorage} 
 */
public abstract class AbstractJdbcProfileAssignmentStorageTestCase extends AbstractProfileAssignmentStorageTestCase
{
    /**
     * Provide the scripts to run before each test invocation.
     * @return the scripts to run.
     */
    protected abstract File[] getScripts();
    
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
    
    /**
     * Reset the db
     * @param runtimeFilename The file name in runtimes env dir
     * @param configFileName The file name in config env dir
     * @param sqlDataSourceFileName The file name in config env dir
     * @throws Exception if an error occurs
     */
    protected void _startAppAndResetDB(String runtimeFilename, String configFileName, String sqlDataSourceFileName) throws Exception
    {
        _startApplication("test/environments/runtimes/" + runtimeFilename, "test/environments/configs/" + configFileName, "test/environments/datasources/" + sqlDataSourceFileName, null, "test/environments/webapp4");

        _setDatabase(Arrays.asList(getScripts()));
    }
    
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
                ScriptRunner.runScript(connection, new FileInputStream(script));
            }
        }
        finally 
        {
            ConnectionHelper.cleanup(connection);
        }
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
