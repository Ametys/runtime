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

import java.util.List;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.test.AbstractTestCase;
import org.ametys.runtime.test.Init;
import org.ametys.runtime.ui.item.Interaction;
import org.ametys.runtime.ui.item.UIItem;
import org.ametys.runtime.ui.item.UIItemFactory;
import org.ametys.runtime.ui.item.UIItemManager;

/**
 * Test the static ui item factory
 */
public class StaticUIItemFactoryTestCase extends AbstractTestCase
{
    /** The runtime ui item maanger */
    protected UIItemManager _uiItemsManager;
    
    @Override
    protected void setUp() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime3.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        _startCocoon("test/environments/webapp2");
        
        _uiItemsManager = (UIItemManager) Init.getPluginServiceManager().lookup(UIItemManager.ROLE);
    }
    
    /**
     * Check that unexisting factory goes well
     * @throws Exception
     */
    public void testUnexisting() throws Exception
    {
        UIItemFactory itemFactory0 = _uiItemsManager.getExtension("unexisting");
        assertNull(itemFactory0);
    }
    
    /**
     * Test the first factory
     * @throws Exception
     */
    public void testFactory1() throws Exception
    {
        /**
         * 1st FACTORY 
         */
        UIItemFactory itemFactory1 = _uiItemsManager.getExtension("staticuiitemfactorytest.1");
        assertNotNull(itemFactory1);
        List<UIItem> items1 = itemFactory1.getUIItems();
        /** SIZE */
        assertEquals(1, items1.size());
        /** ITEM 1 */
        UIItem item11 = items1.get(0);
        assertNotNull(item11);
        assertTrue(item11 instanceof Interaction);
        // type
        Interaction interaction11 = (Interaction) item11;
        // action
        assertNotNull(interaction11.getAction());
        assertNotNull(interaction11.getAction().getParameters());
        assertEquals(0, interaction11.getAction().getParameters().size());
        assertEquals("JavascriptClass", interaction11.getAction().getScriptClassname());
        assertNotNull(interaction11.getAction().getScriptImports());
        assertEquals(1, interaction11.getAction().getScriptImports().size());
        assertEquals("/plugins/staticuiitemfactorytest/resources/js/script.js", interaction11.getAction().getScriptImports().iterator().next());
        // label
        assertNotNull(interaction11.getLabel());
        assertTrue(interaction11.getLabel().isI18n());
        assertEquals("plugin.staticuiitemfactorytest", interaction11.getLabel().getCatalogue());
        assertEquals("label", interaction11.getLabel().getKey());
        assertNull(interaction11.getLabel().getParameters());
        // description
        assertNotNull(interaction11.getDescription());
        assertTrue(interaction11.getDescription().isI18n());
        assertEquals("plugin.staticuiitemfactorytest", interaction11.getDescription().getCatalogue());
        assertEquals("description", interaction11.getDescription().getKey());
        assertNull(interaction11.getDescription().getParameters());
        // iconset
        assertNotNull(interaction11.getIconSet());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", interaction11.getIconSet().getLargeIconPath());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", interaction11.getIconSet().getMediumIconPath());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", interaction11.getIconSet().getSmallIconPath());
        // shortcut
        assertNotNull(interaction11.getShortcut());
        assertFalse(interaction11.getShortcut().hasCtrl());
        assertFalse(interaction11.getShortcut().hasAlt());
        assertTrue(interaction11.getShortcut().hasShift());
        assertEquals("A", interaction11.getShortcut().getKey());
    }
    
    /**
     * Test the second factory
     * @throws Exception
     */
    public void testFactory2() throws Exception
    {
        /**
         * 2nd FACTORY 
         */
        UIItemFactory itemFactory2 = _uiItemsManager.getExtension("staticuiitemfactorytest.2");
        assertNotNull(itemFactory2);
        List<UIItem> items2 = itemFactory2.getUIItems();
        /** SIZE */
        assertEquals(1, items2.size());
        /** ITEM 1 */
        UIItem item21 = items2.get(0);
        assertNotNull(item21);
        assertTrue(item21 instanceof Interaction);
        // type
        Interaction interaction21 = (Interaction) item21;
        // action
        assertNotNull(interaction21.getAction());
        assertNotNull(interaction21.getAction().getParameters());
        assertEquals(1, interaction21.getAction().getParameters().size());
        assertEquals("JavascriptFunction", interaction21.getAction().getParameters().get("FunctionName"));
        assertEquals("Runtime_InteractionActionLibrary_FunctionToClass", interaction21.getAction().getScriptClassname());
        assertNotNull(interaction21.getAction().getScriptImports());
        assertEquals(2, interaction21.getAction().getScriptImports().size());
        assertTrue(interaction21.getAction().getScriptImports().contains("/plugins/staticuiitemfactorytest/resources/js/script.js"));
        assertTrue(interaction21.getAction().getScriptImports().contains("/kernel/resources/js/Runtime_InteractionActionLibrary.js"));
        // label
        assertNotNull(interaction21.getLabel());
        assertTrue(interaction21.getLabel().isI18n());
        assertEquals("plugin.staticuiitemfactorytest", interaction21.getLabel().getCatalogue());
        assertEquals("label", interaction21.getLabel().getKey());
        assertNull(interaction21.getLabel().getParameters());
        // description
        assertNotNull(interaction21.getDescription());
        assertTrue(interaction21.getDescription().isI18n());
        assertEquals("plugin.staticuiitemfactorytest", interaction21.getDescription().getCatalogue());
        assertEquals("description", interaction21.getDescription().getKey());
        assertNull(interaction21.getDescription().getParameters());
        // iconset
        assertNotNull(interaction21.getIconSet());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", interaction21.getIconSet().getLargeIconPath());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", interaction21.getIconSet().getMediumIconPath());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", interaction21.getIconSet().getSmallIconPath());
        // shortcut
        assertNull(interaction21.getShortcut());
    }
    
    /**
     * Test the third factory
     * @throws Exception
     */
    public void testFactory3() throws Exception
    {
        /**
         * 3rd FACTORY 
         */
        UIItemFactory itemFactory3 = _uiItemsManager.getExtension("staticuiitemfactorytest.3");
        assertNotNull(itemFactory3);
        List<UIItem> items3 = itemFactory3.getUIItems();
        /** SIZE */
        assertEquals(1, items3.size());
        /** ITEM 1 */
        UIItem item31 = items3.get(0);
        assertNotNull(item31);
        assertTrue(item31 instanceof Interaction);
        // type
        Interaction interaction31 = (Interaction) item31;
        // action
        assertNotNull(interaction31.getAction());
        assertNotNull(interaction31.getAction().getParameters());
        assertEquals(2, interaction31.getAction().getParameters().size());
        assertEquals("myurl.html", interaction31.getAction().getParameters().get("Link"));
        assertEquals("Runtime_InteractionActionLibrary_Link", interaction31.getAction().getScriptClassname());
        assertNotNull(interaction31.getAction().getScriptImports());
        assertEquals(1, interaction31.getAction().getScriptImports().size());
        assertTrue(interaction31.getAction().getScriptImports().contains("/kernel/resources/js/Runtime_InteractionActionLibrary.js"));
        // label
        assertNotNull(interaction31.getLabel());
        assertTrue(interaction31.getLabel().isI18n());
        assertEquals("plugin.staticuiitemfactorytest", interaction31.getLabel().getCatalogue());
        assertEquals("label", interaction31.getLabel().getKey());
        assertNull(interaction31.getLabel().getParameters());
        // description
        assertNotNull(interaction31.getDescription());
        assertTrue(interaction31.getDescription().isI18n());
        assertEquals("plugin.staticuiitemfactorytest", interaction31.getDescription().getCatalogue());
        assertEquals("description", interaction31.getDescription().getKey());
        assertNull(interaction31.getDescription().getParameters());
        // iconset
        assertNotNull(interaction31.getIconSet());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", interaction31.getIconSet().getLargeIconPath());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", interaction31.getIconSet().getMediumIconPath());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", interaction31.getIconSet().getSmallIconPath());
        // shortcut
        assertNull(interaction31.getShortcut());
    }
    
    /**
     * Test the forth factory
     * @throws Exception
     */
    public void testFactory4() throws Exception
    {
        /**
         * 4th FACTORY 
         */
        UIItemFactory itemFactory4 = _uiItemsManager.getExtension("staticuiitemfactorytest.4");
        assertNotNull(itemFactory4);
        List<UIItem> items4 = itemFactory4.getUIItems();
        /** SIZE */
        assertEquals(1, items4.size());
        /** ITEM 1 */
        UIItem item41 = items4.get(0);
        assertNotNull(item41);
        assertTrue(item41 instanceof Interaction);
        // type
        Interaction interaction41 = (Interaction) item41;
        // action
        assertNotNull(interaction41.getAction());
        assertNotNull(interaction41.getAction().getParameters());
        assertEquals(0, interaction41.getAction().getParameters().size());
        assertEquals("OtherJavascriptClass", interaction41.getAction().getScriptClassname());
        assertNotNull(interaction41.getAction().getScriptImports());
        assertEquals(1, interaction41.getAction().getScriptImports().size());
        assertEquals("/plugins/staticuiitemfactorytest/resources/../../core/resources/js/script.js", interaction41.getAction().getScriptImports().iterator().next());
        assertEquals("staticuiitemfactorytest", interaction41.getAction().getPlugin());
        // label
        assertNotNull(interaction41.getLabel());
        assertTrue(interaction41.getLabel().isI18n());
        assertEquals("othercatalogue", interaction41.getLabel().getCatalogue());
        assertEquals("label", interaction41.getLabel().getKey());
        assertNull(interaction41.getLabel().getParameters());
        // description
        assertNotNull(interaction41.getDescription());
        assertTrue(interaction41.getDescription().isI18n());
        assertEquals("plugin.otherplugin", interaction41.getDescription().getCatalogue());
        assertEquals("description", interaction41.getDescription().getKey());
        assertNull(interaction41.getDescription().getParameters());
        // iconset
        assertNotNull(interaction41.getIconSet());
        assertEquals("/plugins/staticuiitemfactorytest/resources/../../core/resources/img/icon_large.gif", interaction41.getIconSet().getLargeIconPath());
        assertEquals("/plugins/staticuiitemfactorytest/resources/../../core/resources/img/icon_medium.gif", interaction41.getIconSet().getMediumIconPath());
        assertEquals("/plugins/staticuiitemfactorytest/resources/../../core/resources/img/icon_small.gif", interaction41.getIconSet().getSmallIconPath());
        // shortcut
        assertNull(interaction41.getShortcut());
    }  
    
    /**
     * Test the fifth factory
     * @throws Exception
     */
    public void testFactory5() throws Exception
    {
        /**
         * 5th FACTORY 
         */
        UIItemFactory itemFactory5 = _uiItemsManager.getExtension("staticuiitemfactorytest.5");
        assertNotNull(itemFactory5);
        List<UIItem> items5 = itemFactory5.getUIItems();
        /** SIZE */
        assertEquals(5, items5.size());
        /** ITEM 1 */
        UIItem item51 = items5.get(0);
        assertTrue(item51 instanceof UIItem.BarSeparator);
        /** ITEM 2 */
        UIItem item52 = items5.get(1);
        assertNotNull(item52);
        assertTrue(item52 instanceof Interaction);
        // type
        Interaction interaction52 = (Interaction) item52;
        // action
        assertNotNull(interaction52.getAction());
        assertNotNull(interaction52.getAction().getParameters());
        assertEquals(0, interaction52.getAction().getParameters().size());
        assertEquals("JavascriptClass", interaction52.getAction().getScriptClassname());
        assertNotNull(interaction52.getAction().getScriptImports());
        assertEquals(1, interaction52.getAction().getScriptImports().size());
        assertEquals("/plugins/staticuiitemfactorytest/resources/js/script.js", interaction52.getAction().getScriptImports().iterator().next());
        // label
        assertNotNull(interaction52.getLabel());
        assertTrue(interaction52.getLabel().isI18n());
        assertEquals("plugin.staticuiitemfactorytest", interaction52.getLabel().getCatalogue());
        assertEquals("label", interaction52.getLabel().getKey());
        assertNull(interaction52.getLabel().getParameters());
        // description
        assertNotNull(interaction52.getDescription());
        assertTrue(interaction52.getDescription().isI18n());
        assertEquals("plugin.staticuiitemfactorytest", interaction52.getDescription().getCatalogue());
        assertEquals("description", interaction52.getDescription().getKey());
        assertNull(interaction52.getDescription().getParameters());
        // iconset
        assertNotNull(interaction52.getIconSet());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", interaction52.getIconSet().getLargeIconPath());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", interaction52.getIconSet().getMediumIconPath());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", interaction52.getIconSet().getSmallIconPath());
        // shortcut
        assertNull(interaction52.getShortcut());
        /** ITEM 3 */
        UIItem item53 = items5.get(2);
        assertTrue(item53 instanceof UIItem.SpaceSeparator);
        /** ITEM 4 */
        UIItem item54 = items5.get(3);
        assertTrue(item54 instanceof Interaction);
        // type
        Interaction interaction54 = (Interaction) item54;
        // action
        assertNotNull(interaction54.getAction());
        assertNotNull(interaction54.getAction().getParameters());
        assertEquals(0, interaction54.getAction().getParameters().size());
        assertEquals("JavascriptClass", interaction54.getAction().getScriptClassname());
        assertNotNull(interaction54.getAction().getScriptImports());
        assertEquals(1, interaction54.getAction().getScriptImports().size());
        assertEquals("/plugins/staticuiitemfactorytest/resources/js/script.js", interaction54.getAction().getScriptImports().iterator().next());
        // label
        assertNotNull(interaction54.getLabel());
        assertTrue(interaction54.getLabel().isI18n());
        assertEquals("plugin.staticuiitemfactorytest", interaction54.getLabel().getCatalogue());
        assertEquals("label", interaction54.getLabel().getKey());
        assertNull(interaction54.getLabel().getParameters());
        // description
        assertNotNull(interaction54.getDescription());
        assertTrue(interaction54.getDescription().isI18n());
        assertEquals("plugin.staticuiitemfactorytest", interaction54.getDescription().getCatalogue());
        assertEquals("description", interaction54.getDescription().getKey());
        assertNull(interaction54.getDescription().getParameters());
        // iconset
        assertNotNull(interaction54.getIconSet());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_large.gif", interaction54.getIconSet().getLargeIconPath());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_medium.gif", interaction54.getIconSet().getMediumIconPath());
        assertEquals("/plugins/staticuiitemfactorytest/resources/img/icon_small.gif", interaction54.getIconSet().getSmallIconPath());
        // shortcut
        assertNull(interaction54.getShortcut());
        /** ITEM 5 */
        UIItem item55 = items5.get(4);
        assertTrue(item55 instanceof UIItem.BarSeparator);
    }
}
