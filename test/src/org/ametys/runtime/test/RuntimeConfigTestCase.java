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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.excalibur.xml.xpath.XPathProcessor;

import org.ametys.core.util.AmetysXSLTHelper;
import org.ametys.runtime.servlet.RuntimeConfig;

/**
 * Tests the RuntimeConfig
 */
public class RuntimeConfigTestCase extends AbstractRuntimeTestCase
{
    /**
     * RuntimeConfig test
     * @throws Exception if an error occurs
     */
    public void testRuntimeConfigFail() throws Exception
    {
        // The file runtime00_syntax-error.xml is not valid against the schema
        _configureRuntime("test/environments/runtimes/runtime00.xml", "test/environments/webapp1");
        assertTrue("Must not have been validated", RuntimeConfig.getInstance().isSafeMode());
    }
    
    /**
     * RuntimeConfig test
     * @throws Exception if an error occurs
     */
    public void testRuntimeConfigOk() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime01.xml", "test/environments/webapp1");
        
        RuntimeConfig config = RuntimeConfig.getInstance();
        
        assertEquals(config.getDefaultWorkspace(), "myworkspace");
        
        assertEquals(config.getInitClassName(), "org.ametys.runtime.test.Init");
        
        assertEquals(2, config.getPluginsLocations().size());
        assertTrue(config.getPluginsLocations().contains("plugins/"));
        assertTrue(config.getPluginsLocations().contains("test"));
        
        assertEquals(2, config.getExcludedFeatures().size()); // FIXME RUNTIME-2062
        assertTrue(config.getExcludedFeatures().contains("test"));
        
        assertEquals(1, config.getExcludedWorkspaces().size());
        assertTrue(config.getExcludedWorkspaces().contains("test"));
        
        assertEquals(2, config.getComponents().size());
        
        assertEquals(config.getApplicationVersion(), "2.0.1");
        Date date = config.getApplicationBuildDate();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        
        assertEquals(calendar.get(Calendar.MINUTE), 15);
        assertEquals(calendar.get(Calendar.MONTH), 0);
    }
    
    /**
     * Tests the VersionGenerator and VersionHandler process
     * @throws Exception if an error occurs
     */
    public void testVersion() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp1");
        
        XPathProcessor xpath = (XPathProcessor) Init.getPluginServiceManager().lookup(XPathProcessor.ROLE);
        assertEquals("2.0.1", xpath.evaluateAsString(AmetysXSLTHelper.versions(), "Component[Name='Application']/Version"));
        
        cocoon.dispose();
    }
}
