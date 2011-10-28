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
package org.ametys.runtime.test.groups.others;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.group.ModifiableGroupsManager;
import org.ametys.runtime.plugins.core.group.EmptyGroupsManager;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.CocoonWrapper;
import org.ametys.runtime.test.Init;
import org.apache.excalibur.xml.dom.DOMHandler;
import org.apache.excalibur.xml.dom.DOMHandlerFactory;
import org.apache.excalibur.xml.xpath.XPathProcessor;

/**
 * Tests the EmptyGroupsTestCase
 */
public class EmptyGroupsTestCase extends AbstractRuntimeTestCase
{
    /**
     * Check that the empty group manager is the default one and is really empty
     * @throws Exception if an error occurs
     */
    public void testEmpty() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime3.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        CocoonWrapper cocoon = _startCocoon("test/environments/webapp1");
        
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
        
        cocoon.dispose();
    }
}
