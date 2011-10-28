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

import org.apache.cocoon.transformation.I18nTransformer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.xml.dom.DOMHandler;
import org.apache.excalibur.xml.dom.DOMHandlerFactory;
import org.apache.excalibur.xml.xpath.PrefixResolver;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Node;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugins.core.ui.item.DesktopManager;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;

/**
 * Tests the desktop manager
 */
public class DesktopManagerTestCase extends AbstractRuntimeTestCase
{
    /** The runtime desktop manager */
    protected DesktopManager _desktopManager;
    
    @Override
    protected void setUp() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime3.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        _startCocoon("test/environments/webapp2");
        
        _desktopManager = (DesktopManager) Init.getPluginServiceManager().lookup("DesktopManagerTest");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Test the sax
     * @throws Exception if an error occurs
     */
    public void testSax() throws Exception
    {
        DOMHandlerFactory dom = (DOMHandlerFactory) Init.getPluginServiceManager().lookup(DOMHandlerFactory.ROLE);
        DOMHandler handler;
        XPathProcessor xpath = (XPathProcessor) Init.getPluginServiceManager().lookup(XPathProcessor.ROLE);
        Node testNode;

        // Sax none
        handler = dom.createDOMHandler();
        handler.startDocument();
        XMLUtils.startElement(handler, "categories");
        _desktopManager.toSAX(handler);
        XMLUtils.endElement(handler, "categories");
        handler.endDocument();
        
        PrefixResolver i18nResolver = new PrefixResolver()
        {
            public String prefixToNamespace(String prefix)
            {
                if ("i18n".equals(prefix))
                {
                    return I18nTransformer.I18N_NAMESPACE_URI;
                }
                
                return null;
            }
        };
        
        // Test
        assertEquals(2.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/categories/*)"));
        assertEquals(2.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/categories/category)"));
        
        /**
         * FIRST CATEGORY
         */
        testNode = xpath.selectSingleNode(handler.getDocument(), "/categories/category[1]");
        assertNotNull(testNode);
        assertEquals("ONE", xpath.evaluateAsString(testNode, "@name"));
        assertEquals(1.0, xpath.evaluateAsNumber(testNode, "count(*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(testNode, "count(DesktopItem)"));
        
        Node itemNode1 = xpath.selectSingleNode(testNode, "DesktopItem[1]");
        assertEquals(2.0, xpath.evaluateAsNumber(itemNode1, "count(@*)"));
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode1, "count(*)"));
        // label
        assertNotNull(xpath.evaluateAsString(itemNode1, "action/param[@name='label']/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "action/param[@name='label']/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("label", xpath.evaluateAsString(itemNode1, "action/param[@name='label']/i18n:text/@i18n:key", i18nResolver));
        // description
        assertNotNull(xpath.evaluateAsString(itemNode1, "action/param[@name='default-description']/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "action/param[@name='default-description']/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("description", xpath.evaluateAsString(itemNode1, "action/param[@name='default-description']/i18n:text/@i18n:key", i18nResolver));
        // icons
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", xpath.evaluateAsString(itemNode1, "action/param[@name='icon-small']"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", xpath.evaluateAsString(itemNode1, "action/param[@name='icon-medium']"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", xpath.evaluateAsString(itemNode1, "action/param[@name='icon-large']"));
        // action
        assertEquals("staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "@plugin"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(action/@*)"));
        assertEquals("JavascriptClass", xpath.evaluateAsString(itemNode1, "action/@class"));
        assertEquals(5.0, xpath.evaluateAsNumber(itemNode1, "count(action/*)"));
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode1, "count(action/param[count(*) = 0])"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(scripts/file)"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/js/script.js", xpath.evaluateAsString(itemNode1, "scripts/file"));

        testNode = xpath.selectSingleNode(handler.getDocument(), "/categories/category[1]");
        assertNotNull(testNode);
        assertEquals("ONE", xpath.evaluateAsString(testNode, "@name"));
        assertEquals(1.0, xpath.evaluateAsNumber(testNode, "count(*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(testNode, "count(DesktopItem)"));
        
        /**
         * SECOND CATEGORY
         */
        testNode = xpath.selectSingleNode(handler.getDocument(), "/categories/category[2]");
        assertNotNull(testNode);
        assertEquals("TWO", xpath.evaluateAsString(testNode, "@name"));
        assertEquals(2.0, xpath.evaluateAsNumber(testNode, "count(*)"));
        assertEquals(2.0, xpath.evaluateAsNumber(testNode, "count(DesktopItem)"));
        
        itemNode1 = xpath.selectSingleNode(testNode, "DesktopItem[1]");
        assertEquals(2.0, xpath.evaluateAsNumber(itemNode1, "count(@*)"));
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode1, "count(*)"));
        // label
        assertNotNull(xpath.selectSingleNode(itemNode1, "action/param[@name='label']/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "action/param[@name='label']/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("label", xpath.evaluateAsString(itemNode1, "action/param[@name='label']/i18n:text/@i18n:key", i18nResolver));
        // description
        assertNotNull(xpath.selectSingleNode(itemNode1, "action/param[@name='default-description']/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "action/param[@name='default-description']/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("description", xpath.evaluateAsString(itemNode1, "action/param[@name='default-description']/i18n:text/@i18n:key", i18nResolver));
        // icons
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", xpath.evaluateAsString(itemNode1, "action/param[@name='icon-small']"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", xpath.evaluateAsString(itemNode1, "action/param[@name='icon-medium']"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", xpath.evaluateAsString(itemNode1, "action/param[@name='icon-large']"));
        // action
        assertEquals("staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "@plugin"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(action/@*)"));
        assertEquals("org.ametys.runtime.Link", xpath.evaluateAsString(itemNode1, "action/@class"));
        assertEquals(7.0, xpath.evaluateAsNumber(itemNode1, "count(action/*)"));
        assertEquals(2.0, xpath.evaluateAsNumber(itemNode1, "count(action/param/*)"));
        assertEquals("myurl.html", xpath.evaluateAsString(itemNode1, "action/param[@name='Link']"));
        assertEquals(0.0, xpath.evaluateAsNumber(itemNode1, "count(action/scripts/file)"));

        Node itemNode2 = xpath.selectSingleNode(testNode, "DesktopItem[2]");
        assertEquals(2.0, xpath.evaluateAsNumber(itemNode2, "count(@*)"));
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode2, "count(*)"));
        // label
        assertNotNull(xpath.evaluateAsString(itemNode2, "action/param[@name='label']/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("othercatalogue", xpath.evaluateAsString(itemNode2, "action/param[@name='label']/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("label", xpath.evaluateAsString(itemNode2, "action/param[@name='label']/i18n:text/@i18n:key", i18nResolver));
        // description
        assertNotNull(xpath.evaluateAsString(itemNode2, "action/param[@name='default-description']/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("plugin.otherplugin", xpath.evaluateAsString(itemNode2, "action/param[@name='default-description']/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("description", xpath.evaluateAsString(itemNode2, "action/param[@name='default-description']/i18n:text/@i18n:key", i18nResolver));
        // icons
        assertEquals("/plugins/core/resources/img/icon_small.gif", xpath.evaluateAsString(itemNode2, "action/param[@name='icon-small']"));
        assertEquals("/plugins/core/resources/img/icon_medium.gif", xpath.evaluateAsString(itemNode2, "action/param[@name='icon-medium']"));
        assertEquals("/plugins/core/resources/img/icon_large.gif", xpath.evaluateAsString(itemNode2, "action/param[@name='icon-large']"));
        // action
        assertEquals("staticuiitemfactorytest", xpath.evaluateAsString(itemNode2, "@plugin"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode2, "count(action/@*)"));
        assertEquals("OtherJavascriptClass", xpath.evaluateAsString(itemNode2, "action/@class"));
        assertEquals(5.0, xpath.evaluateAsNumber(itemNode2, "count(action/*)"));
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode2, "count(action/param[count(*) = 0])"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode2, "count(scripts/file)"));
        assertEquals("/plugins/core/resources/js/script.js", xpath.evaluateAsString(itemNode2, "scripts/file"));
    }
}
