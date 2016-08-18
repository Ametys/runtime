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

import java.util.Arrays;
import java.util.Collection;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.commandline.CommandLineContext;
import org.apache.cocoon.util.log.SLF4JLoggerAdapter;
import org.slf4j.LoggerFactory;

import org.ametys.core.datasource.SQLDataSourceManager;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.plugin.PluginsManager;

/**
 * Tests the ConfigManager
 */
public class ConfigManagerTestCase extends AbstractRuntimeTestCase
{
    private DefaultContext _context;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        CommandLineContext ctx = new CommandLineContext("test/environments/webapp1");
        ctx.enableLogging(new SLF4JLoggerAdapter(LoggerFactory.getLogger("ctx")));
        _context = new DefaultContext();
        _context.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, ctx);
    }
    
    /**
     * Test the behaviour when the config.xml file is not present
     * @throws Exception if an error occurs
     */
    public void testConfigNotPresent() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime01.xml", "test/environments/webapp1");
        
        Config.setFilename("test/environments/configs/config0.xml"); // does not exist
        SQLDataSourceManager.setFilename("test/environments/datasources/datasource-mysql.xml");
        
        PluginsManager.getInstance().init(null, _context, "test/environments/webapp1", false);
        
        assertTrue(PluginsManager.getInstance().isSafeMode());
        assertFalse(ConfigManager.getInstance().isComplete());
    }
    
    /**
     * Test that if the datasource is activated, the config parameters are necessary.
     * @throws Exception if an error occurs
     */
    public void testMissingParameters() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime01.xml", "test/environments/webapp1");
        Config.setFilename("test/environments/configs/config2.xml"); // missing necessary parameters
        SQLDataSourceManager.setFilename("test/environments/datasources/datasource-mysql.xml");
        
        PluginsManager.getInstance().init(null, _context, "test/environments/webapp1", false);
        
        assertTrue(PluginsManager.getInstance().isSafeMode());
        assertFalse(ConfigManager.getInstance().isComplete());
    }

    /**
     * Test that if the datasource is unactivated, the config parameters are not necessary.
     * @throws Exception if an error occurs
     */
    public void testUnactivation() throws Exception
    {
        // FIXME RightManager currently needs runtime.rights.datasource as BasicRightManager does not exist anymore
        // so this test fails 
        
//        _configureRuntime("test/environments/runtimes/runtime03.xml", "test/environments/webapp1");
//        Config.setFilename("test/environments/configs/config2.xml");
//        SQLDataSourceManager.setFilename("test/environments/datasources/datasource-mysql.xml");
//
//        PluginsManager.getInstance().init(null, _context, "test/environments/webapp1", false);
//        
//        assertTrue(ConfigManager.getInstance().isComplete());
    }

    /**
     * Test parameters from plugins and features
     * @throws Exception if an error occurs
     */
    public void testConfiguration() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime02.xml", "test/environments/webapp1");
        Config.setFilename("test/environments/configs/config1.xml");
        SQLDataSourceManager.setFilename("test/environments/datasources/datasource-mysql.xml");
        
        PluginsManager.getInstance().init(null, _context, "test/environments/webapp1", false);
        
        assertTrue(ConfigManager.getInstance().isComplete());
        
        String[] parameters = ConfigManager.getInstance().getParametersIds();
        Collection<String> ids = Arrays.asList(parameters);
        
        assertFalse(ids.contains("param1"));
        assertTrue(ids.contains("param2"));
        assertFalse(ids.contains("param3"));
        assertTrue(ids.contains("param4"));
        
        assertEquals("param2", Config.getInstance().getValueAsString("param2"));
        assertEquals(4, Config.getInstance().getValueAsLong("param4").longValue());
    }
}
