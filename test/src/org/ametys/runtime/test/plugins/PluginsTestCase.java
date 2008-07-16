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
package org.ametys.runtime.test.plugins;

import java.util.Map;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugin.ExtensionPoint;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.PluginsManager.ActiveFeature;
import org.ametys.runtime.plugin.PluginsManager.InactiveFeature;
import org.ametys.runtime.plugin.PluginsManager.InactivityCause;
import org.ametys.runtime.test.AbstractTestCase;
import org.ametys.runtime.test.Init;

/**
 * Tests the PluginsManager
 */
public class PluginsTestCase extends AbstractTestCase
{
    /**
     * Test that the init class is actually executed
     * @throws Exception
     */
    public void testInit() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime2.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        
        _startCocoon("test/environments/webapp1");
        
        assertTrue(Init.isOk());
    }
    
    /**
     * Tests embedded plugins
     * @throws Exception
     */
    public void testEmbeddedPlugin() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime2.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        
        _startCocoon("test/environments/webapp1");
        
        assertTrue(PluginsManager.getInstance().getEmbeddedPluginsIds().contains("core"));
        assertTrue(PluginsManager.getInstance().getPluginNames().contains("core"));
        assertEquals("resource://org/ametys/runtime/plugins/core", PluginsManager.getInstance().getBaseURI("core"));
    }
    
    /**
     * Tests the concept of plugin location
     * @throws Exception
     */
    public void testLocation() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime10.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        
        _startCocoon("test/environments/webapp1");
        
        assertTrue(PluginsManager.getInstance().getPluginNames().contains("test"));
        assertEquals("location1", PluginsManager.getInstance().getPluginLocation("test"));
    }
    
    /**
     * Tests the declaration of extension points
     * @throws Exception
     */
    public void testExtensionPoint() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime11.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        
        _startCocoon("test/environments/webapp1");
        
        assertTrue(PluginsManager.getInstance().getPluginNames().contains("test"));

        assertNotNull(Config.getInstance());
        
        assertTrue(PluginsManager.getInstance().getSingleExtensionPoints().contains("sep-test"));
                
        assertTrue(Init.getPluginServiceManager().lookup("sep-test") instanceof TestSingleExtensionPoint);
        
        assertTrue(PluginsManager.getInstance().getExtensionPoints().contains("ep-test"));
        
        ExtensionPoint testEP = (ExtensionPoint) Init.getPluginServiceManager().lookup("ep-test");
        assertEquals(1, testEP.getExtensionsIds().size());
        TestExtension ext = (TestExtension) testEP.getExtension("org.ametys.runtime.test.EP");
        assertNotNull(ext);
        assertNull(ext.getContext());
        
        assertTrue(PluginsManager.getInstance().getExtensionPoints().contains("ep-component-test"));
        
        ExtensionPoint testCEP = (ExtensionPoint) Init.getPluginServiceManager().lookup("ep-component-test");
        assertEquals(1, testCEP.getExtensionsIds().size());
        TestExtension ext2 = (TestExtension) testCEP.getExtension("org.ametys.runtime.test.EP");
        assertNotNull(ext2);
        assertNotNull(ext2.getContext());
    }
    
    /**
     * Tests active and inactive feature
     * @throws Exception
     */
    public void testFeatures() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime12.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        
        _startCocoon("test/environments/webapp1");
        
        Map<String, ActiveFeature> activeFeatures = PluginsManager.getInstance().getActiveFeatures();
        Map<String, InactiveFeature> inactiveFeatures = PluginsManager.getInstance().getInactiveFeatures();

        assertTrue(activeFeatures.containsKey("test/test-feature2"));
        assertTrue(activeFeatures.containsKey("test/test-feature3"));
        
        assertFalse(activeFeatures.containsKey("test/test-feature"));
        assertTrue(inactiveFeatures.containsKey("test/test-feature"));
        InactiveFeature feature = inactiveFeatures.get("test/test-feature");
        assertEquals(feature.getCause(), InactivityCause.DEPENDENCY);
        
        assertFalse(activeFeatures.containsKey("test/test-feature4"));
        assertTrue(inactiveFeatures.containsKey("test/test-feature4"));
        feature = inactiveFeatures.get("test/test-feature4");
        assertEquals(feature.getCause(), InactivityCause.SINGLE);
        
        assertFalse(activeFeatures.containsKey("test/test-feature5"));
        assertTrue(inactiveFeatures.containsKey("test/test-feature5"));
        feature = inactiveFeatures.get("test/test-feature5");
        assertEquals(feature.getCause(), InactivityCause.EXCLUDED);
    }
}
