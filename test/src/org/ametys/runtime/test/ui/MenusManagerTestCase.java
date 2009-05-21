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
package org.ametys.runtime.test.ui;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;
import org.ametys.runtime.ui.manager.MenusManager;
import org.apache.cocoon.transformation.I18nTransformer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.xml.dom.DOMHandler;
import org.apache.excalibur.xml.dom.DOMHandlerFactory;
import org.apache.excalibur.xml.xpath.PrefixResolver;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Node;

/**
 * Tests the menu manager
 */
public class MenusManagerTestCase extends AbstractRuntimeTestCase
{
    /** The runtime menus manager */
    protected MenusManager _menusManager;
    
    @Override
    protected void setUp() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime3.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        _startCocoon("test/environments/webapp2");
        
        _menusManager = (MenusManager) Init.getPluginServiceManager().lookup("MenusManagerTest");
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
        XMLUtils.startElement(handler, "menus");
        _menusManager.toSAX(handler);
        XMLUtils.endElement(handler, "menus");
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
        assertEquals(3.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/menus/*)"));
        assertEquals(3.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/menus/menu)"));

        /**
         * FIRST MENU
         */
        testNode = xpath.selectSingleNode(handler.getDocument(), "/menus/menu[1]");
        assertNotNull(testNode);
        assertNotNull(xpath.selectSingleNode(testNode, "label/i18n:text", i18nResolver));
        assertEquals("application", xpath.evaluateAsString(testNode, "label/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("MENU1", xpath.evaluateAsString(testNode, "label/i18n:text/@i18n:key", i18nResolver));
        
        assertEquals(6.0, xpath.evaluateAsNumber(testNode, "count(items/*)"));
        assertEquals("UIItem", xpath.evaluateAsString(testNode, "local-name(items/*[1])"));
        assertEquals("separator", xpath.evaluateAsString(testNode, "local-name(items/*[2])"));
        assertEquals("UIItem", xpath.evaluateAsString(testNode, "local-name(items/*[3])"));
        assertEquals("separator", xpath.evaluateAsString(testNode, "local-name(items/*[4])"));
        assertEquals("menu", xpath.evaluateAsString(testNode, "local-name(items/*[5])"));
        assertNotNull(xpath.selectSingleNode(testNode, "items/*[5]/label/i18n:text", i18nResolver));
        assertEquals("application", xpath.evaluateAsString(testNode, "items/*[5]/label/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("SUBMENU1", xpath.evaluateAsString(testNode, "items/*[5]/label/i18n:text/@i18n:key", i18nResolver));
        assertEquals(1.0, xpath.evaluateAsNumber(testNode, "count(items/*[5]/items/*)"));
        assertEquals("separator", xpath.evaluateAsString(testNode, "local-name(items/*[5]/items/*[1])"));
        assertEquals("separator", xpath.evaluateAsString(testNode, "local-name(items/*[6])"));
        
        // entry 1
        Node itemNode1 = xpath.selectSingleNode(testNode, "items/*[1]");
        assertEquals(0.0, xpath.evaluateAsNumber(itemNode1, "count(@*)"));
        assertEquals(5.0, xpath.evaluateAsNumber(itemNode1, "count(*)"));
        // label
        assertNotNull(xpath.selectSingleNode(itemNode1, "Label/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "Label/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("label", xpath.evaluateAsString(itemNode1, "Label/i18n:text/@i18n:key", i18nResolver));
        // description
        assertNotNull(xpath.selectSingleNode(itemNode1, "Description/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "Description/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("description", xpath.evaluateAsString(itemNode1, "Description/i18n:text/@i18n:key", i18nResolver));
        // icons
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode1, "count(Icons/*)"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", xpath.evaluateAsString(itemNode1, "Icons/Small"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", xpath.evaluateAsString(itemNode1, "Icons/Medium"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", xpath.evaluateAsString(itemNode1, "Icons/Large"));
        // action
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/@*)"));
        assertEquals("staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "Action/@plugin"));
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode1, "count(Action/*)"));
        assertEquals("JavascriptClass", xpath.evaluateAsString(itemNode1, "Action/ClassName"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/Parameters[count(*) = 0])"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/Imports/Import)"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/js/script.js", xpath.evaluateAsString(itemNode1, "Action/Imports/Import"));
        // shortcut
        assertEquals("A", xpath.evaluateAsString(itemNode1, "Shortcut"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Shortcut/@*)"));
        assertEquals("SHIFT", xpath.evaluateAsString(itemNode1, "Shortcut/@SHIFT"));
        
        // entry 3
        Node itemNode2 = xpath.selectSingleNode(testNode, "items/*[3]");
        assertEquals(0.0, xpath.evaluateAsNumber(itemNode2, "count(@*)"));
        assertEquals(4.0, xpath.evaluateAsNumber(itemNode2, "count(*)"));
        // label
        assertNotNull(xpath.selectSingleNode(itemNode2, "Label/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode2, "Label/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("label", xpath.evaluateAsString(itemNode2, "Label/i18n:text/@i18n:key", i18nResolver));
        // description
        assertNotNull(xpath.selectSingleNode(itemNode2, "Description/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode2, "Description/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("description", xpath.evaluateAsString(itemNode2, "Description/i18n:text/@i18n:key", i18nResolver));
        // icons
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode2, "count(Icons/*)"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", xpath.evaluateAsString(itemNode2, "Icons/Small"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", xpath.evaluateAsString(itemNode2, "Icons/Medium"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", xpath.evaluateAsString(itemNode2, "Icons/Large"));
        // action
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode2, "count(Action/@*)"));
        assertEquals("staticuiitemfactorytest", xpath.evaluateAsString(itemNode2, "Action/@plugin"));
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode2, "count(Action/*)"));
        assertEquals("Runtime_InteractionActionLibrary_FunctionToClass", xpath.evaluateAsString(itemNode2, "Action/ClassName"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode2, "count(Action/Parameters[count(*) = 1])"));
        assertEquals("JavascriptFunction", xpath.evaluateAsString(itemNode2, "Action/Parameters/FunctionName"));
        assertEquals(2.0, xpath.evaluateAsNumber(itemNode2, "count(Action/Imports/Import)"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode2, "count(Action/Imports[Import = '/plugins/staticuiitemfactorytest/resources/js/script.js'])"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode2, "count(Action/Imports[Import = '/kernel/resources/js/Runtime_InteractionActionLibrary.js'])"));
        // shortcut
        assertEquals(0.0, xpath.evaluateAsNumber(itemNode2, "count(Shortcut)"));

        /**
         * SECOND MENU
         */
        testNode = xpath.selectSingleNode(handler.getDocument(), "/menus/menu[2]");
        assertNotNull(testNode);
        assertNotNull(xpath.selectSingleNode(testNode, "label/i18n:text", i18nResolver));
        assertEquals("application", xpath.evaluateAsString(testNode, "label/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("MENU2", xpath.evaluateAsString(testNode, "label/i18n:text/@i18n:key", i18nResolver));

        assertEquals(9.0, xpath.evaluateAsNumber(testNode, "count(items/*)"));
        assertEquals("UIItem", xpath.evaluateAsString(testNode, "local-name(items/*[1])"));
        assertEquals("separator", xpath.evaluateAsString(testNode, "local-name(items/*[2])"));
        assertEquals("menu", xpath.evaluateAsString(testNode, "local-name(items/*[3])"));
        assertNotNull(xpath.selectSingleNode(testNode, "items/*[3]/label/i18n:text", i18nResolver));
        assertEquals("application", xpath.evaluateAsString(testNode, "items/*[3]/label/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("SUBMENU2", xpath.evaluateAsString(testNode, "items/*[3]/label/i18n:text/@i18n:key", i18nResolver));
        assertEquals(1.0, xpath.evaluateAsNumber(testNode, "count(items/*[3]/items/*)"));
        assertEquals("UIItem", xpath.evaluateAsString(testNode, "local-name(items/*[3]/items/*[1])"));
        assertEquals("separator", xpath.evaluateAsString(testNode, "local-name(items/*[4])"));
        assertEquals("separator", xpath.evaluateAsString(testNode, "local-name(items/*[5])"));
        assertEquals("UIItem", xpath.evaluateAsString(testNode, "local-name(items/*[6])"));
        assertEquals("separator", xpath.evaluateAsString(testNode, "local-name(items/*[7])"));
        assertEquals("UIItem", xpath.evaluateAsString(testNode, "local-name(items/*[8])"));
        assertEquals("separator", xpath.evaluateAsString(testNode, "local-name(items/*[9])"));

        // entry 1
        itemNode1 = xpath.selectSingleNode(testNode, "items/*[1]");
        assertEquals(0.0, xpath.evaluateAsNumber(itemNode1, "count(@*)"));
        assertEquals(4.0, xpath.evaluateAsNumber(itemNode1, "count(*)"));
        // label
        assertNotNull(xpath.selectSingleNode(itemNode1, "Label/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "Label/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("label", xpath.evaluateAsString(itemNode1, "Label/i18n:text/@i18n:key", i18nResolver));
        // description
        assertNotNull(xpath.selectSingleNode(itemNode1, "Description/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "Description/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("description", xpath.evaluateAsString(itemNode1, "Description/i18n:text/@i18n:key", i18nResolver));
        // icons
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode1, "count(Icons/*)"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", xpath.evaluateAsString(itemNode1, "Icons/Small"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", xpath.evaluateAsString(itemNode1, "Icons/Medium"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", xpath.evaluateAsString(itemNode1, "Icons/Large"));
        // action
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/@*)"));
        assertEquals("staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "Action/@plugin"));
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode1, "count(Action/*)"));
        assertEquals("Runtime_InteractionActionLibrary_Link", xpath.evaluateAsString(itemNode1, "Action/ClassName"));
        assertEquals(2.0, xpath.evaluateAsNumber(itemNode1, "count(Action/Parameters/*)"));
        assertEquals("myurl.html", xpath.evaluateAsString(itemNode1, "Action/Parameters/Link"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/Imports/Import)"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/Imports/Import)"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/Imports[Import = '/kernel/resources/js/Runtime_InteractionActionLibrary.js'])"));
        // shortcut
        assertEquals(0.0, xpath.evaluateAsNumber(itemNode1, "count(Shortcut)"));        
        
        // entry 3
        itemNode2 = xpath.selectSingleNode(testNode, "items/*[3]/items/*[1]");
        assertEquals(0.0, xpath.evaluateAsNumber(itemNode2, "count(@*)"));
        assertEquals(4.0, xpath.evaluateAsNumber(itemNode2, "count(*)"));
        // label
        assertNotNull(xpath.selectSingleNode(itemNode2, "Label/i18n:text", i18nResolver));
        assertEquals("othercatalogue", xpath.evaluateAsString(itemNode2, "Label/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("label", xpath.evaluateAsString(itemNode2, "Label/i18n:text/@i18n:key", i18nResolver));
        // description
        assertNotNull(xpath.selectSingleNode(itemNode2, "Description/i18n:text", i18nResolver));
        assertEquals("plugin.otherplugin", xpath.evaluateAsString(itemNode2, "Description/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("description", xpath.evaluateAsString(itemNode2, "Description/i18n:text/@i18n:key", i18nResolver));
        // icons
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode2, "count(Icons/*)"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/../../core/resources/img/icon_small.gif", xpath.evaluateAsString(itemNode2, "Icons/Small"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/../../core/resources/img/icon_medium.gif", xpath.evaluateAsString(itemNode2, "Icons/Medium"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/../../core/resources/img/icon_large.gif", xpath.evaluateAsString(itemNode2, "Icons/Large"));
        // action
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode2, "count(Action/@*)"));
        assertEquals("staticuiitemfactorytest", xpath.evaluateAsString(itemNode2, "Action/@plugin"));
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode2, "count(Action/*)"));
        assertEquals("OtherJavascriptClass", xpath.evaluateAsString(itemNode2, "Action/ClassName"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode2, "count(Action/Parameters[count(*) = 0])"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode2, "count(Action/Imports/Import)"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/../../core/resources/js/script.js", xpath.evaluateAsString(itemNode2, "Action/Imports/Import"));
        // shortcut
        assertEquals(0.0, xpath.evaluateAsNumber(itemNode2, "count(Shortcut)"));        
        
        // entry 5
        itemNode1 = xpath.selectSingleNode(testNode, "items/*[6]");
        assertEquals(0.0, xpath.evaluateAsNumber(itemNode1, "count(@*)"));
        assertEquals(4.0, xpath.evaluateAsNumber(itemNode1, "count(*)"));
        // label
        assertNotNull(xpath.selectSingleNode(itemNode1, "Label/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "Label/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("label", xpath.evaluateAsString(itemNode1, "Label/i18n:text/@i18n:key", i18nResolver));
        // description
        assertNotNull(xpath.selectSingleNode(itemNode1, "Description/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "Description/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("description", xpath.evaluateAsString(itemNode1, "Description/i18n:text/@i18n:key", i18nResolver));
        // icons
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode1, "count(Icons/*)"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", xpath.evaluateAsString(itemNode1, "Icons/Small"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", xpath.evaluateAsString(itemNode1, "Icons/Medium"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", xpath.evaluateAsString(itemNode1, "Icons/Large"));
        // action
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/@*)"));
        assertEquals("staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "Action/@plugin"));
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode1, "count(Action/*)"));
        assertEquals("JavascriptClass", xpath.evaluateAsString(itemNode1, "Action/ClassName"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/Parameters[count(*) = 0])"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/Imports/Import)"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/js/script.js", xpath.evaluateAsString(itemNode1, "Action/Imports/Import"));
        // shortcut
        assertEquals(0.0, xpath.evaluateAsNumber(itemNode2, "count(Shortcut)"));        
        
        itemNode1 = xpath.selectSingleNode(testNode, "items/*[8]");
        assertEquals(0.0, xpath.evaluateAsNumber(itemNode1, "count(@*)"));
        assertEquals(4.0, xpath.evaluateAsNumber(itemNode1, "count(*)"));
        // label
        assertNotNull(xpath.selectSingleNode(itemNode1, "Label/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "Label/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("label", xpath.evaluateAsString(itemNode1, "Label/i18n:text/@i18n:key", i18nResolver));
        // description
        assertNotNull(xpath.selectSingleNode(itemNode1, "Description/i18n:text", i18nResolver));
        assertEquals("plugin.staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "Description/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("description", xpath.evaluateAsString(itemNode1, "Description/i18n:text/@i18n:key", i18nResolver));
        // icons
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode1, "count(Icons/*)"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", xpath.evaluateAsString(itemNode1, "Icons/Small"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", xpath.evaluateAsString(itemNode1, "Icons/Medium"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", xpath.evaluateAsString(itemNode1, "Icons/Large"));
        // action
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/@*)"));
        assertEquals("staticuiitemfactorytest", xpath.evaluateAsString(itemNode1, "Action/@plugin"));
        assertEquals(3.0, xpath.evaluateAsNumber(itemNode1, "count(Action/*)"));
        assertEquals("JavascriptClass", xpath.evaluateAsString(itemNode1, "Action/ClassName"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/Parameters[count(*) = 0])"));
        assertEquals(1.0, xpath.evaluateAsNumber(itemNode1, "count(Action/Imports/Import)"));
        assertEquals("/plugins/staticuiitemfactorytest/resources/js/script.js", xpath.evaluateAsString(itemNode1, "Action/Imports/Import"));
        // shortcut
        assertEquals(0.0, xpath.evaluateAsNumber(itemNode2, "count(Shortcut)"));        
        
        /**
         * THIRD MENU
         */
        testNode = xpath.selectSingleNode(handler.getDocument(), "/menus/menu[3]");
        assertNotNull(testNode);
        assertNotNull(xpath.selectSingleNode(testNode, "label/i18n:text", i18nResolver));
        assertEquals("application", xpath.evaluateAsString(testNode, "label/i18n:text/@i18n:catalogue", i18nResolver));
        assertEquals("MENU3", xpath.evaluateAsString(testNode, "label/i18n:text/@i18n:key", i18nResolver));
        
        assertEquals(1.0, xpath.evaluateAsNumber(testNode, "count(items/*)"));
        assertEquals("separator", xpath.evaluateAsString(testNode, "local-name(items/*[1])"));
    }
}
