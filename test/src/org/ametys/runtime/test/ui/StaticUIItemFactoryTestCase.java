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
package org.ametys.runtime.test.ui;

import org.ametys.runtime.plugins.core.ui.item.DesktopManager;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;
import org.ametys.runtime.ui.ClientSideElement;
import org.ametys.runtime.ui.StaticClientSideElement;
import org.ametys.runtime.util.I18nizableText;

/**
 * Test the static ui item factory
 */
public class StaticUIItemFactoryTestCase extends AbstractRuntimeTestCase
{
    /** The runtime ui item maanger */
    protected DesktopManager _desktopManager;
    
    @Override
    protected void setUp() throws Exception
    {
        _startApplication("test/environments/runtimes/runtime3.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");
        
        _desktopManager = (DesktopManager) Init.getPluginServiceManager().lookup("DesktopManagerTest");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Check that unexisting factory goes well
     * @throws Exception if an error occurs
     */
    public void testUnexisting() throws Exception
    {
        ClientSideElement itemFactory0 = _desktopManager.getExtension("unexisting");
        assertNull(itemFactory0);
    }
    
    /**
     * Test the first factory
     * @throws Exception if an error occurs
     */
    public void testFactory1() throws Exception
    {
        /**
         * 1st FACTORY 
         */
        ClientSideElement itemFactory1 = _desktopManager.getExtension("staticuiitemfactorytest.1");
        assertNotNull(itemFactory1);
        assertTrue(itemFactory1 instanceof StaticClientSideElement);
        // action
        assertNotNull(itemFactory1.getScript(null).getScriptClassname());
        assertNotNull(itemFactory1.getParameters(null));
        assertEquals(5, itemFactory1.getParameters(null).size());
        assertEquals("JavascriptClass", itemFactory1.getScript(null).getScriptClassname());
        assertNotNull(itemFactory1.getScript(null).getScriptFiles());
        assertEquals(1, itemFactory1.getScript(null).getScriptFiles().size());
        assertEquals("/plugins/staticuiitemfactorytest/resources/js/script.js", itemFactory1.getScript(null).getScriptFiles().iterator().next());
        // label
        assertNotNull(itemFactory1.getParameters(null).get("label"));
        assertTrue(itemFactory1.getParameters(null).get("label") instanceof I18nizableText);
        assertTrue(((I18nizableText) itemFactory1.getParameters(null).get("label")).isI18n());
        assertEquals("plugin.staticuiitemfactorytest", ((I18nizableText) itemFactory1.getParameters(null).get("label")).getCatalogue());
        assertEquals("label", ((I18nizableText) itemFactory1.getParameters(null).get("label")).getKey());
        assertNull(((I18nizableText) itemFactory1.getParameters(null).get("label")).getParameters());
        // description
        assertNotNull(itemFactory1.getParameters(null).get("default-description"));
        assertTrue(itemFactory1.getParameters(null).get("default-description") instanceof I18nizableText);
        assertTrue(((I18nizableText) itemFactory1.getParameters(null).get("default-description")).isI18n());
        assertEquals("plugin.staticuiitemfactorytest", ((I18nizableText) itemFactory1.getParameters(null).get("default-description")).getCatalogue());
        assertEquals("description", ((I18nizableText) itemFactory1.getParameters(null).get("default-description")).getKey());
        assertNull(((I18nizableText) itemFactory1.getParameters(null).get("default-description")).getParameters());
        // iconset
        assertNotNull(itemFactory1.getParameters(null).get("icon-small"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", itemFactory1.getParameters(null).get("icon-small").toString());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", itemFactory1.getParameters(null).get("icon-medium").toString());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", itemFactory1.getParameters(null).get("icon-large").toString());
    }
    
    /**
     * Test the third factory
     * @throws Exception if an error occurs
     */
    public void testFactory3() throws Exception
    {
        /**
         * 3rd FACTORY 
         */
        ClientSideElement itemFactory3 = _desktopManager.getExtension("staticuiitemfactorytest.3");
        assertNotNull(itemFactory3);
        assertTrue(itemFactory3 instanceof StaticClientSideElement);
        // action
        assertNotNull(itemFactory3.getScript(null).getScriptClassname());
        assertNotNull(itemFactory3.getParameters(null));
        assertEquals(7, itemFactory3.getParameters(null).size());
        assertEquals("myurl.html", itemFactory3.getParameters(null).get("Link").toString());
        assertNotNull(itemFactory3.getScript(null).getScriptFiles());
        assertEquals(0, itemFactory3.getScript(null).getScriptFiles().size());
        // label
        assertNotNull(itemFactory3.getParameters(null).get("label"));
        assertTrue(itemFactory3.getParameters(null).get("label") instanceof I18nizableText);
        assertTrue(((I18nizableText) itemFactory3.getParameters(null).get("label")).isI18n());
        assertEquals("plugin.staticuiitemfactorytest", ((I18nizableText) itemFactory3.getParameters(null).get("label")).getCatalogue());
        assertEquals("label", ((I18nizableText) itemFactory3.getParameters(null).get("label")).getKey());
        assertNull(((I18nizableText) itemFactory3.getParameters(null).get("label")).getParameters());
        // description
        assertNotNull(itemFactory3.getParameters(null).get("default-description"));
        assertTrue(itemFactory3.getParameters(null).get("default-description") instanceof I18nizableText);
        assertTrue(((I18nizableText) itemFactory3.getParameters(null).get("default-description")).isI18n());
        assertEquals("plugin.staticuiitemfactorytest", ((I18nizableText) itemFactory3.getParameters(null).get("default-description")).getCatalogue());
        assertEquals("description", ((I18nizableText) itemFactory3.getParameters(null).get("default-description")).getKey());
        assertNull(((I18nizableText) itemFactory3.getParameters(null).get("default-description")).getParameters());
        // iconset
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", itemFactory3.getParameters(null).get("icon-small").toString());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", itemFactory3.getParameters(null).get("icon-medium").toString());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", itemFactory3.getParameters(null).get("icon-large").toString());
    }
    
    /**
     * Test the forth factory
     * @throws Exception if an error occurs
     */
    public void testFactory4() throws Exception
    {
        /**
         * 4th FACTORY 
         */
        ClientSideElement itemFactory4 = _desktopManager.getExtension("staticuiitemfactorytest.4");
        assertNotNull(itemFactory4);
        assertTrue(itemFactory4 instanceof StaticClientSideElement);
        // action
        assertNotNull(itemFactory4.getScript(null).getScriptClassname());
        assertNotNull(itemFactory4.getParameters(null));
        assertEquals(5, itemFactory4.getParameters(null).size());
        assertEquals("OtherJavascriptClass", itemFactory4.getScript(null).getScriptClassname());
        assertNotNull(itemFactory4.getScript(null).getScriptFiles());
        assertEquals(1, itemFactory4.getScript(null).getScriptFiles().size());
        assertEquals("/plugins/core/resources/js/script.js", itemFactory4.getScript(null).getScriptFiles().iterator().next());
        assertEquals("staticuiitemfactorytest", itemFactory4.getPluginName());
        // label
        assertNotNull(itemFactory4.getParameters(null).get("label"));
        assertTrue(itemFactory4.getParameters(null).get("label") instanceof I18nizableText);
        assertTrue(((I18nizableText) itemFactory4.getParameters(null).get("label")).isI18n());
        assertEquals("othercatalogue", ((I18nizableText) itemFactory4.getParameters(null).get("label")).getCatalogue());
        assertEquals("label", ((I18nizableText) itemFactory4.getParameters(null).get("label")).getKey());
        assertNull(((I18nizableText) itemFactory4.getParameters(null).get("label")).getParameters());
        // description
        assertNotNull(itemFactory4.getParameters(null).get("default-description"));
        assertTrue(((I18nizableText) itemFactory4.getParameters(null).get("default-description")).isI18n());
        assertEquals("plugin.otherplugin", ((I18nizableText) itemFactory4.getParameters(null).get("default-description")).getCatalogue());
        assertEquals("description", ((I18nizableText) itemFactory4.getParameters(null).get("default-description")).getKey());
        assertNull(((I18nizableText) itemFactory4.getParameters(null).get("default-description")).getParameters());
        // iconset
        assertEquals("/plugins/core/resources/img/icon_small.gif", itemFactory4.getParameters(null).get("icon-small").toString());
        assertEquals("/plugins/core/resources/img/icon_medium.gif", itemFactory4.getParameters(null).get("icon-medium").toString());
        assertEquals("/plugins/core/resources/img/icon_large.gif", itemFactory4.getParameters(null).get("icon-large").toString());
    }  
}
