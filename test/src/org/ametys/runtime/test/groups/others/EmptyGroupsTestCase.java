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
package org.ametys.runtime.test.groups.others;

import org.apache.excalibur.xml.dom.DOMHandler;
import org.apache.excalibur.xml.dom.DOMHandlerFactory;
import org.apache.excalibur.xml.xpath.XPathProcessor;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.group.ModifiableGroupsManager;
import org.ametys.runtime.plugins.core.group.EmptyGroupsManager;
import org.ametys.runtime.test.AbstractTestCase;
import org.ametys.runtime.test.Init;

/**
 * Tests the EmptyGroupsTestCase
 */
public class EmptyGroupsTestCase extends AbstractTestCase
{
    /**
     * Check that the empty group manager is the default one and is really empty
     * @throws Exception
     */
    public void testEmpty() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime3.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        _startCocoon("test/environments/webapp1");
        
        GroupsManager groupsManager = (GroupsManager) Init.getPluginServiceManager().lookup(GroupsManager.ROLE);
        
        // DEFAULT IMPL
        assertTrue(groupsManager instanceof EmptyGroupsManager);

        // NOT MODIFIABLE
        assertFalse(groupsManager instanceof ModifiableGroupsManager);

        // GET GROUP
        assertNull(groupsManager.getGroup("foo"));
        
        // GET GROUPS
        assertEquals(0, groupsManager.getGroups().size());
        
        // GET USER GROUPS
        assertEquals(0, groupsManager.getUserGroups("foo").size());

        // SAX GROUPS
        DOMHandlerFactory dom = (DOMHandlerFactory) Init.getPluginServiceManager().lookup(DOMHandlerFactory.ROLE);
        DOMHandler handler = dom.createDOMHandler();
        handler.startDocument();
        groupsManager.toSAX(handler, -1, 0, null);
        handler.endDocument();

        XPathProcessor xpath = (XPathProcessor) Init.getPluginServiceManager().lookup(XPathProcessor.ROLE);
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups)"));
        assertEquals(0.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups/*)"));
    }
}
