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

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.service.WrapperServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.environment.commandline.CommandLineContext;

import org.ametys.core.util.LoggerFactory;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.component.PluginsComponentManager;

/**
 * Tests the ConfigManager
 */
public class ConfigManagerTestCase extends AbstractRuntimeTestCase
{
    private DefaultContext _context;
    private CocoonComponentManager _manager;
    
    @Override
    protected void setUp() throws Exception
    {
        CommandLineContext ctx = new CommandLineContext("test/environments/webapp1");
        ctx.enableLogging(LoggerFactory.getLoggerFor("ctx"));
        _context = new DefaultContext();
        _context.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, ctx);
                
        _manager = new CocoonComponentManager();
    }
    
    /**
     * Test the behaviour when the config.xml file is not present
     * @throws Exception if an error occurs
     */
    public void testConfigNotPresent() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime1.xml", "test/environments/webapp1");
        
        Config.setFilename("test/environments/configs/config0.xml"); // does not exist
        
        PluginsComponentManager pluginCM = new PluginsComponentManager(_manager);
        ContainerUtil.contextualize(pluginCM, _context);
        ContainerUtil.enableLogging(pluginCM, LoggerFactory.getLoggerFor("plugins"));
        ContainerUtil.service(pluginCM, new WrapperServiceManager(pluginCM));
        
        assertNull(PluginsManager.getInstance().init(pluginCM, _context, "test/environments/webapp1"));
    }
    
    /**
     * Test that if the datasource is unactivated, the config parameters are not necessary.
     * @throws Exception if an error occurs
     */
    public void testUnactivation() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime3.xml", "test/environments/webapp1");
        Config.setFilename("test/environments/configs/config2.xml");

        PluginsComponentManager pluginCM = new PluginsComponentManager(_manager);
        ContainerUtil.contextualize(pluginCM, _context);
        ContainerUtil.enableLogging(pluginCM, LoggerFactory.getLoggerFor("plugins"));
        ContainerUtil.service(pluginCM, new WrapperServiceManager(pluginCM));
        
        assertNotNull(PluginsManager.getInstance().init(pluginCM, _context, "test/environments/webapp1"));
    }

    /**
     * Test that if the datasource is activated, the config parameters are necessary.
     * @throws Exception if an error occurs
     */
    public void testParameters() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime1.xml", "test/environments/webapp1");
        Config.setFilename("test/environments/configs/config2.xml"); // missing necessary parameters
        
        PluginsComponentManager pluginCM = new PluginsComponentManager(_manager);
        ContainerUtil.contextualize(pluginCM, _context);
        ContainerUtil.enableLogging(pluginCM, LoggerFactory.getLoggerFor("plugins"));
        ContainerUtil.service(pluginCM, new WrapperServiceManager(pluginCM));
        
        assertNull(PluginsManager.getInstance().init(pluginCM, _context, "test/environments/webapp1"));
    }

    /**
     * Test parameters from plugins and features
     * @throws Exception if an error occurs
     */
    public void testConfiguration() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime10.xml", "test/environments/webapp1");
        Config.setFilename("test/environments/configs/config2.xml");
        
        PluginsComponentManager pluginCM = new PluginsComponentManager(_manager);
        ContainerUtil.contextualize(pluginCM, _context);
        ContainerUtil.enableLogging(pluginCM, LoggerFactory.getLoggerFor("plugins"));
        ContainerUtil.service(pluginCM, new WrapperServiceManager(pluginCM));
        
        PluginsManager.getInstance().init(pluginCM, _context, "test/environments/webapp1");
        
        String[] parameters = ConfigManager.getInstance().getParametersIds();
        Collection<String> ids = Arrays.asList(parameters);
        
        assertFalse(ids.contains("param1"));
        assertTrue(ids.contains("param2"));
        assertFalse(ids.contains("param3"));
        assertTrue(ids.contains("param4"));
    }
}
