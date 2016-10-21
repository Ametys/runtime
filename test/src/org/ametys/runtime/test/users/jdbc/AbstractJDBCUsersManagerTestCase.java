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
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        _startApplication("test/environments/runtimes/runtime6.xml", "test/environments/configs/config1.xml", null, "test/environments/webapp1");
        
        _userDirectory = _createUserDirectory();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    private UserDirectory _createUserDirectory() throws Exception
    {
        String modelId = "org.ametys.plugins.core.user.directory.Jdbc";
        
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("runtime.users.jdbc.datasource", "SQL-test");
        parameters.put("runtime.users.jdbc.table", "Users");
        
        
        return ((UserDirectoryFactory) Init.getPluginServiceManager().lookup(UserDirectoryFactory.ROLE)).createUserDirectory("foo", modelId, parameters, "foo", null);
    }
    
}
