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
package org.ametys.runtime.test.groups.ldap;

import java.util.HashMap;
import java.util.Set;

import org.ametys.runtime.group.Group;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;
import org.apache.excalibur.xml.dom.DOMHandler;
import org.apache.excalibur.xml.dom.DOMHandlerFactory;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Node;

/**
 * Ldap groups' tests 
 */
public abstract class AbstractLdapGroupsTestCase extends AbstractRuntimeTestCase
{
    /** the user manager */
    protected GroupsManager _groupsManager;
    
    /**
     * To avoid alltest failure
     * @throws Exception if an error occurs
     */
    public void testFilled() throws Exception
    {
        Group group;
        Set<String> users;
        
        // GET GROUP
        assertNull(_groupsManager.getGroup("foo"));
        
        group = _groupsManager.getGroup("group1");
        assertNotNull(group);
        assertEquals("group1", group.getId());
        assertEquals("Group 1", group.getLabel());
        users = group.getUsers();
        assertEquals(1, users.size());
        assertEquals("user1", users.iterator().next());
        
        group = _groupsManager.getGroup("group2");
        assertNotNull(group);
        assertEquals("group2", group.getId());
        assertEquals("Group 2", group.getLabel());
        users = group.getUsers();
        assertEquals(4, users.size());
        assertTrue(users.contains("user1"));
        assertTrue(users.contains("user2"));
        assertTrue(users.contains("user3"));
        assertTrue(users.contains("user4"));
        
        group = _groupsManager.getGroup("group3");
        assertNotNull(group);
        assertEquals("group3", group.getId());
        assertEquals("Group 3", group.getLabel());
        users = group.getUsers();
        assertEquals(4, users.size());
        assertTrue(users.contains("user5"));
        assertTrue(users.contains("user6"));
        assertTrue(users.contains("user7"));
        assertTrue(users.contains("user8"));
        
        // GET GROUPS
        Set<Group> groups = _groupsManager.getGroups();
        assertNotNull(groups);
        assertEquals(3, groups.size());        
        assertTrue("group1 not found", groups.contains(new Group("group1", "")));
        assertTrue("group2 not found", groups.contains(new Group("group2", "")));
        // assertTrue("group3 not found", groups.contains(new Group("group3", "")));
        
        // GET USER GROUPS
        Set<String> groupsName;
        groupsName = _groupsManager.getUserGroups("user1");
        assertEquals(2, groupsName.size());
        assertTrue(groupsName.contains("group1"));
        assertTrue(groupsName.contains("group2"));
        
        groupsName = _groupsManager.getUserGroups("user2");
        assertEquals(1, groupsName.size());
        assertTrue(groupsName.contains("group2"));

        groupsName = _groupsManager.getUserGroups("user10");
        assertEquals(0, groupsName.size());

        // SAX GROUPS
        DOMHandlerFactory dom = (DOMHandlerFactory) Init.getPluginServiceManager().lookup(DOMHandlerFactory.ROLE);
        DOMHandler handler = dom.createDOMHandler();
        handler.startDocument();
        _groupsManager.toSAX(handler, -1, 0, new HashMap<String, String>());
        handler.endDocument();

        XPathProcessor xpath = (XPathProcessor) Init.getPluginServiceManager().lookup(XPathProcessor.ROLE);
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups)"));
        assertEquals(4.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups/*)")); // +1.0 for total property
        assertEquals(3.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups/group)"));
        assertEquals(3.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups/group[count(@*) = 1])"));
        assertEquals(3.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups/group[count(label) = 1 and count(users) = 1])"));
        
        Node groupNode;
        groupNode = xpath.selectSingleNode(handler.getDocument(), "/groups/group[@id='group1']");
        assertEquals("Group 1", xpath.evaluateAsString(groupNode, "label"));
        assertEquals(1.0, xpath.evaluateAsNumber(groupNode, "count(users/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(groupNode, "count(users/user)"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'user1']"));
        
        groupNode = xpath.selectSingleNode(handler.getDocument(), "/groups/group[@id='group2']");
        assertEquals("Group 2", xpath.evaluateAsString(groupNode, "label"));
        assertEquals(4.0, xpath.evaluateAsNumber(groupNode, "count(users/*)"));
        assertEquals(4.0, xpath.evaluateAsNumber(groupNode, "count(users/user)"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'user1']"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'user2']"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'user3']"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'user4']"));
        
        groupNode = xpath.selectSingleNode(handler.getDocument(), "/groups/group[@id='group3']");
        assertEquals("Group 3", xpath.evaluateAsString(groupNode, "label"));
        assertEquals(4.0, xpath.evaluateAsNumber(groupNode, "count(users/*)"));
        assertEquals(4.0, xpath.evaluateAsNumber(groupNode, "count(users/user)"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'user5']"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'user6']"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'user7']"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'user8']"));
    }
}
