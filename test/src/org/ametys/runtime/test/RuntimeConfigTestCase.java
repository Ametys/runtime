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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.excalibur.xml.dom.DOMHandler;
import org.apache.excalibur.xml.dom.DOMHandlerFactory;
import org.apache.excalibur.xml.xpath.XPathProcessor;

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
    public void testRuntimeConfig0() throws Exception
    {
        try
        {
            _configureRuntime("test/environments/runtimes/runtime0.xml", "test/environments/webapp1");
            fail("Must not have been validated");
        }
        catch (Exception ex)
        {
            // it is ok
        }
    }
    
    /**
     * RuntimeConfig test
     * @throws Exception if an error occurs
     */
    public void testRuntimeConfig1() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime1.xml", "test/environments/webapp1");
        
        RuntimeConfig config = RuntimeConfig.getInstance();
        
        assertEquals(config.getIncompleteConfigRedirectURL(), "cocoon://_admin/public/load-config.html?uri=core/administrator/config/edit.html");
        
        assertEquals(config.getIncompleteConfigAllowedURLs().size(), 4);
        assertTrue(config.getIncompleteConfigAllowedURLs().contains("_admin/public"));
        
        assertEquals(config.getDefaultWorkspace(), "myworkspace");
        
        assertEquals(config.getInitClassName(), null);
        
        assertEquals(config.getPluginsLocations().size(), 1);
        assertTrue(config.getPluginsLocations().contains("plugins/"));
        
        assertEquals(config.getExtensionsPoints().size(), 5);
        assertTrue(config.getExtensionsPoints().containsKey("org.ametys.runtime.right.RightsManager"));
        assertEquals(config.getExtensionsPoints().get("org.ametys.runtime.right.RightsManager"), "org.ametys.runtime.plugins.core.right.DefaultProfileBased");
        
        assertEquals(config.getApplicationVersion(), null);
        assertNull(config.getApplicationBuildDate());
    }
    
    /**
     * RuntimeConfig test
     * @throws Exception if an error occurs
     */
    public void testRuntimeConfig2() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime2.xml", "test/environments/webapp1");
        
        RuntimeConfig config = RuntimeConfig.getInstance();
        
        assertEquals(config.getIncompleteConfigRedirectURL(), "cocoon://_admin/public/load-config.html?uri=core/administrator/config/edit.html");
        
        assertEquals(config.getIncompleteConfigAllowedURLs().size(), 4);
        assertTrue(config.getIncompleteConfigAllowedURLs().contains("_admin/public"));
        
        assertEquals(config.getDefaultWorkspace(), "myworkspace");
        
        assertEquals(config.getInitClassName(), "org.ametys.runtime.test.Init");
        
        assertEquals(config.getPluginsLocations().size(), 2);
        assertTrue(config.getPluginsLocations().contains("plugins/"));
        assertTrue(config.getPluginsLocations().contains("test"));
        
        assertEquals(config.getExcludedFeatures().size(), 1);
        assertTrue(config.getExcludedFeatures().contains("test"));
        
        assertEquals(config.getExcludedWorkspaces().size(), 1);
        assertTrue(config.getExcludedWorkspaces().contains("test"));
        
        assertEquals(config.getExtensionsPoints().size(), 5);
        assertTrue(config.getExtensionsPoints().containsKey("org.ametys.runtime.right.RightsManager"));
        assertEquals(config.getExtensionsPoints().get("org.ametys.runtime.right.RightsManager"), "org.ametys.runtime.plugins.core.right.DefaultProfileBased");
        
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
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime2.xml", "test/environments/configs/config1.xml", "test/environments/webapp1");
        
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("authorization", "BASIC " + new String(Base64.encodeBase64("admin:admin".getBytes())));
        
        DOMHandlerFactory dom = (DOMHandlerFactory) Init.getPluginServiceManager().lookup(DOMHandlerFactory.ROLE);
        DOMHandler handler = dom.createDOMHandler();
        
        cocoon.processURI("_admin/homepage-versions.xml", handler, null, null, headers);
        
        XPathProcessor xpath = (XPathProcessor) Init.getPluginServiceManager().lookup(XPathProcessor.ROLE);
        assertEquals("2.0.1", xpath.evaluateAsString(handler.getDocument(), "/Versions/Component[Name='Application']/Version"));
        
        cocoon.dispose();
    }
}
