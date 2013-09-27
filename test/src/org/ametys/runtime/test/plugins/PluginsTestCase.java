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
package org.ametys.runtime.test.plugins;

import java.util.Map;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugin.ExtensionPoint;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.PluginsManager.ActiveFeature;
import org.ametys.runtime.plugin.PluginsManager.InactiveFeature;
import org.ametys.runtime.plugin.PluginsManager.InactivityCause;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.CocoonWrapper;
import org.ametys.runtime.test.Init;

/**
 * Tests the PluginsManager
 */
public class PluginsTestCase extends AbstractRuntimeTestCase
{
    /**
     * Test that the init class is actually executed
     * @throws Exception if an error occurs
     */
    public void testInit() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime2.xml", "test/environments/configs/config1.xml", "test/environments/webapp1");
        
        assertTrue(Init.isOk());
        
        cocoon.dispose();
    }
        
    /**
     * Tests the concept of plugin location
     * @throws Exception if an error occurs
     */
    public void testLocation() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime10.xml", "test/environments/configs/config1.xml", "test/environments/webapp1");
        
        assertTrue(PluginsManager.getInstance().getPluginNames().contains("test"));
        assertEquals("location1", PluginsManager.getInstance().getPluginLocation("test").getParentFile().getName());
        
        cocoon.dispose();
    }
    
    /**
     * Tests the declaration of extension points
     * @throws Exception if an error occurs
     */
    public void testExtensionPoint() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime11.xml", "test/environments/configs/config1.xml", "test/environments/webapp1");
        
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
        
        cocoon.dispose();
    }
    
    /**
     * Tests active and inactive feature
     * @throws Exception if an error occurs
     */
    public void testFeatures() throws Exception
    {
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime12.xml", "test/environments/configs/config1.xml", "test/environments/webapp1");
        
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
        
        cocoon.dispose();
    }
}
