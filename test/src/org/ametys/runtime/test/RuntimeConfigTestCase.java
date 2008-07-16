/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.servlet.RuntimeConfig;
import org.apache.commons.codec.binary.Base64;
import org.apache.excalibur.xml.dom.DOMHandler;
import org.apache.excalibur.xml.dom.DOMHandlerFactory;
import org.apache.excalibur.xml.xpath.XPathProcessor;


/**
 * Tests the RuntimeConfig
 */
public class RuntimeConfigTestCase extends AbstractTestCase
{
    /**
     * RuntimeConfig test
     * @throws Exception
     */
    public void testRuntimeConfig0() throws Exception
    {
        try
        {
            _configureRuntime("test/environments/runtimes/runtime0.xml");
            fail("Must not have been validated");
        }
        catch (Exception ex)
        {
            // it is ok
        }
    }
    
    /**
     * RuntimeConfig test
     * @throws Exception
     */
    public void testRuntimeConfig1() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime1.xml");
        
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
        
        assertEquals(config.getApplicationVersion(), "VERSION");
        assertNull(config.getApplicationBuildDate());
    }
    
    /**
     * RuntimeConfig test
     * @throws Exception
     */
    public void testRuntimeConfig2() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime2.xml");
        
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
     * @throws Exception
     */
    public void testVersion() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime2.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        CocoonWrapper cocoon = _startCocoon("test/environments/webapp1");
        
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("authorization", "BASIC " + new String(Base64.encodeBase64("admin:admin".getBytes())));
        
        DOMHandlerFactory dom = (DOMHandlerFactory) Init.getPluginServiceManager().lookup(DOMHandlerFactory.ROLE);
        DOMHandler handler = dom.createDOMHandler();
        
        cocoon.processURI("_admin/homepage-versions.xml", handler, null, null, headers);
        
        XPathProcessor xpath = (XPathProcessor) Init.getPluginServiceManager().lookup(XPathProcessor.ROLE);
        assertEquals("2.0.1", xpath.evaluateAsString(handler.getDocument(), "/Versions/Component[Name='Application']/Version"));
    }
}
