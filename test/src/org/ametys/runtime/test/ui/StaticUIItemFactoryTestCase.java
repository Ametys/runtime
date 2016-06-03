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

import java.util.Map;

import org.ametys.core.ui.ClientSideElement;
import org.ametys.core.ui.ClientSideElement.Script;
import org.ametys.core.ui.RibbonControlsManager;
import org.ametys.core.ui.StaticClientSideElement;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;

/**
 * Test the static ui item factory
 */
public class StaticUIItemFactoryTestCase extends AbstractRuntimeTestCase
{
    /** The runtime ui item maanger */
    protected RibbonControlsManager _ribbonManager;
    
    @Override
    protected void setUp() throws Exception
    {
        _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");
        _ribbonManager = (RibbonControlsManager) Init.getPluginServiceManager().lookup("ribbonManagerTest");
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
        ClientSideElement itemFactory0 = _ribbonManager.getExtension("unexisting");
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
        ClientSideElement itemFactory1 = _ribbonManager.getExtension("staticuiitemfactorytest.1");
        Script script = itemFactory1.getScripts(null).get(0);
        Map<String, Object> parameters = script.getParameters();
        
        assertNotNull(itemFactory1);
        assertTrue(itemFactory1 instanceof StaticClientSideElement);
        // action
        assertNotNull(script.getScriptClassname());
        assertNotNull(parameters);
        assertEquals(5, parameters.size());
        assertEquals("JavascriptClass", script.getScriptClassname());
        assertNotNull(script.getScriptFiles());
        assertEquals(1, script.getScriptFiles().size());
        assertEquals("/plugins/staticuiitemfactorytest/resources/js/script.js", script.getScriptFiles().iterator().next().getPath());
        // label
        assertNotNull(parameters.get("label"));
        assertTrue(parameters.get("label") instanceof I18nizableText);
        assertTrue(((I18nizableText) parameters.get("label")).isI18n());
        assertEquals("plugin.staticuiitemfactorytest", ((I18nizableText) parameters.get("label")).getCatalogue());
        assertEquals("label", ((I18nizableText) parameters.get("label")).getKey());
        assertNull(((I18nizableText) parameters.get("label")).getParameters());
        // description
        assertNotNull(parameters.get("default-description"));
        assertTrue(parameters.get("default-description") instanceof I18nizableText);
        assertTrue(((I18nizableText) parameters.get("default-description")).isI18n());
        assertEquals("plugin.staticuiitemfactorytest", ((I18nizableText) parameters.get("default-description")).getCatalogue());
        assertEquals("description", ((I18nizableText) parameters.get("default-description")).getKey());
        assertNull(((I18nizableText) parameters.get("default-description")).getParameters());
        // iconset
        assertNotNull(parameters.get("icon-small"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", parameters.get("icon-small").toString());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", parameters.get("icon-medium").toString());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", parameters.get("icon-large").toString());
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
        ClientSideElement itemFactory3 = _ribbonManager.getExtension("staticuiitemfactorytest.3");
        Script script = itemFactory3.getScripts(null).get(0);
        Map<String, Object> parameters = script.getParameters();
        
        assertNotNull(itemFactory3);
        assertTrue(itemFactory3 instanceof StaticClientSideElement);
        // action
        assertNotNull(script.getScriptClassname());
        assertNotNull(parameters);
        assertEquals(7, parameters.size());
        assertEquals("myurl.html", parameters.get("Link").toString());
        assertNotNull(script.getScriptFiles());
        assertEquals(0, script.getScriptFiles().size());
        // label
        assertNotNull(parameters.get("label"));
        assertTrue(parameters.get("label") instanceof I18nizableText);
        assertTrue(((I18nizableText) parameters.get("label")).isI18n());
        assertEquals("plugin.staticuiitemfactorytest", ((I18nizableText) parameters.get("label")).getCatalogue());
        assertEquals("label", ((I18nizableText) parameters.get("label")).getKey());
        assertNull(((I18nizableText) parameters.get("label")).getParameters());
        // description
        assertNotNull(parameters.get("default-description"));
        assertTrue(parameters.get("default-description") instanceof I18nizableText);
        assertTrue(((I18nizableText) parameters.get("default-description")).isI18n());
        assertEquals("plugin.staticuiitemfactorytest", ((I18nizableText) parameters.get("default-description")).getCatalogue());
        assertEquals("description", ((I18nizableText) parameters.get("default-description")).getKey());
        assertNull(((I18nizableText) parameters.get("default-description")).getParameters());
        // iconset
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", parameters.get("icon-small").toString());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", parameters.get("icon-medium").toString());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", parameters.get("icon-large").toString());
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
        ClientSideElement itemFactory4 = _ribbonManager.getExtension("staticuiitemfactorytest.4");
        Script script = itemFactory4.getScripts(null).get(0);
        Map<String, Object> parameters = script.getParameters();
        
        assertNotNull(itemFactory4);
        assertTrue(itemFactory4 instanceof StaticClientSideElement);
        // action
        assertNotNull(script.getScriptClassname());
        assertNotNull(parameters);
        assertEquals(5, parameters.size());
        assertEquals("OtherJavascriptClass", script.getScriptClassname());
        assertNotNull(script.getScriptFiles());
        assertEquals(1, script.getScriptFiles().size());
        assertEquals("/plugins/core/resources/js/script.js", script.getScriptFiles().iterator().next().getPath());
        assertEquals("staticuiitemfactorytest", itemFactory4.getPluginName());
        // label
        assertNotNull(parameters.get("label"));
        assertTrue(parameters.get("label") instanceof I18nizableText);
        assertTrue(((I18nizableText) parameters.get("label")).isI18n());
        assertEquals("othercatalogue", ((I18nizableText) parameters.get("label")).getCatalogue());
        assertEquals("label", ((I18nizableText) parameters.get("label")).getKey());
        assertNull(((I18nizableText) parameters.get("label")).getParameters());
        // description
        assertNotNull(parameters.get("default-description"));
        assertTrue(((I18nizableText) parameters.get("default-description")).isI18n());
        assertEquals("plugin.otherplugin", ((I18nizableText) parameters.get("default-description")).getCatalogue());
        assertEquals("description", ((I18nizableText) parameters.get("default-description")).getKey());
        assertNull(((I18nizableText) parameters.get("default-description")).getParameters());
        // iconset
        assertEquals("/plugins/core/resources/img/icon_small.gif", parameters.get("icon-small").toString());
        assertEquals("/plugins/core/resources/img/icon_medium.gif", parameters.get("icon-medium").toString());
        assertEquals("/plugins/core/resources/img/icon_large.gif", parameters.get("icon-large").toString());
    }  
}
