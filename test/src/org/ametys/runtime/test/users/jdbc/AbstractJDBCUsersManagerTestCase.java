/*
 *  Copyright 2009 Anyware Services_configureRuntime
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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.directory.UserDirectoryFactory;
import org.ametys.runtime.test.AbstractJDBCTestCase;
import org.ametys.runtime.test.Init;

/**
 * Reset the jdbc user db and load the user manager 
 */
public abstract class AbstractJDBCUsersManagerTestCase extends AbstractJDBCTestCase
{
    /** the user manager */
    protected UserDirectory _userDirectory;
    
    /**
     * Reset the db
     * @param runtimeFilename The file name in runtimes env dir
     * @param configFileName The file name in config env dir
     * @param sqlDataSourceFileName the path of the data sources configuration file
     * @throws Exception if an error occurs
     */
    protected void _resetDB(String runtimeFilename, String configFileName, String sqlDataSourceFileName) throws Exception
    {
        _startApplication("test/environments/runtimes/" + runtimeFilename, "test/environments/configs/" + configFileName, "test/environments/datasources/" + sqlDataSourceFileName, null, "test/environments/webapp1");

        _setDatabase(Arrays.asList(getScripts()));
        
        _userDirectory = _createUserDirectory();
    }

    /**
     * Provide the scripts to run before each test invocation.
     * @return the scripts to run.
     */
    protected abstract File[] getScripts();
    
    private UserDirectory _createUserDirectory() throws Exception
    {
        String modelId = "org.ametys.plugins.core.user.directory.Jdbc";
        
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("runtime.users.jdbc.datasource", "SQL-test");
        parameters.put("runtime.users.jdbc.table", "Users");
        
        
        return ((UserDirectoryFactory) Init.getPluginServiceManager().lookup(UserDirectoryFactory.ROLE)).createUserDirectory(modelId, parameters, "foo");
    }
    
}
