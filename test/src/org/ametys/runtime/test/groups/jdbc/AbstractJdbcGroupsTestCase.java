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
package org.ametys.runtime.test.groups.jdbc;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.excalibur.xml.dom.DOMHandler;
import org.apache.excalibur.xml.dom.DOMHandlerFactory;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Node;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.group.Group;
import org.ametys.core.group.GroupListener;
import org.ametys.core.group.GroupsManager;
import org.ametys.core.group.InvalidModificationException;
import org.ametys.core.group.ModifiableGroupsManager;
import org.ametys.plugins.core.impl.group.jdbc.ModifiableJdbcGroupsManager;
import org.ametys.runtime.test.AbstractJDBCTestCase;
import org.ametys.runtime.test.Init;

/**
 * Tests the JdbcGroupsManager
 */
public abstract class AbstractJdbcGroupsTestCase extends AbstractJDBCTestCase
{
    /** the groups manager */
    protected GroupsManager _groupsManager;
    
    /**
     * Provide the scripts to run before each test invocation.
     * @return the scripts to run.
     */
    protected abstract File[] getScripts();
    
    /**
     * Provide the scripts to run to populate the database.
     * @return the scripts to run.
     */
    protected abstract File[] getPopulateScripts();
    
    /**
     * Reset the db
     * @param runtimeFilename The file name in runtimes env dir
     * @param configFileName The file name in config env dir
     * @throws Exception if an error occurs
     */
    protected void _resetDB(String runtimeFilename, String configFileName) throws Exception
    {
        _startApplication("test/environments/runtimes/" + runtimeFilename, "test/environments/configs/" + configFileName, "test/environments/webapp1");
        
        _setDatabase(Arrays.asList(getScripts()));

        _groupsManager = (GroupsManager) Init.getPluginServiceManager().lookup(GroupsManager.ROLE);
    }
    
    /**
     * Check that the jdbc group manager has the correct type
     * @throws Exception if an error occurs
     */
    public void testType() throws Exception
    {
        // DEFAULT IMPL
        assertTrue(_groupsManager instanceof ModifiableJdbcGroupsManager);

        // MODIFIABLE
        assertTrue(_groupsManager instanceof ModifiableGroupsManager);
    }
    
    /**
     * Check when the db is empty
     * @throws Exception if an error occurs
     */
    public void testEmpty() throws Exception
    {
        // GET GROUP
        assertNull(_groupsManager.getGroup("foo"));
        
        // GET GROUPS
        assertEquals(0, _groupsManager.getGroups().size());
        
        // GET USER GROUPS
        assertEquals(0, _groupsManager.getUserGroups("foo").size());

        // SAX GROUPS
        DOMHandlerFactory dom = (DOMHandlerFactory) Init.getPluginServiceManager().lookup(DOMHandlerFactory.ROLE);
        DOMHandler handler = dom.createDOMHandler();
        handler.startDocument();
        _groupsManager.toSAX(handler, -1, 0, new HashMap<String, String>());
        handler.endDocument();

        XPathProcessor xpath = (XPathProcessor) Init.getPluginServiceManager().lookup(XPathProcessor.ROLE);
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups/*)"));   // 1.0 for total tag

    }

    /**
     * Check when the db is filled
     * @throws Exception if an error occurs
     */
    public void testFilled() throws Exception
    {
        Set<String> users;
        Group group;
        
        // Fill DB
        _setDatabase(Arrays.asList(getPopulateScripts()));
        
        List<String> groupsIds = _getGroupsIds();
        
        // GET GROUP
        assertNull(_groupsManager.getGroup("foo"));
        
        group = _groupsManager.getGroup(groupsIds.get(0));
        assertNotNull(group);
        assertEquals(groupsIds.get(0), group.getId());
        assertEquals("Group 1", group.getLabel());
        users = group.getUsers();
        assertEquals(1, users.size());
        assertEquals("test", users.iterator().next());
        
        group = _groupsManager.getGroup(groupsIds.get(1));
        assertNotNull(group);
        assertEquals(groupsIds.get(1), group.getId());
        assertEquals("Group 2", group.getLabel());
        users = group.getUsers();
        assertEquals(2, users.size());
        assertTrue(users.contains("test"));
        assertTrue(users.contains("test2"));
        
        group = _groupsManager.getGroup(groupsIds.get(2));
        assertNotNull(group);
        assertEquals(groupsIds.get(2), group.getId());
        assertEquals("Group 3", group.getLabel());
        users = group.getUsers();
        assertEquals(1, users.size());
        assertEquals("test2", users.iterator().next());
        
        group = _groupsManager.getGroup(groupsIds.get(3));
        assertNotNull(group);
        assertEquals(groupsIds.get(3), group.getId());
        assertEquals("Group 4", group.getLabel());
        users = group.getUsers();
        assertEquals(0, users.size());
        
        // GET GROUPS
        Set<Group> groups = _groupsManager.getGroups();
        assertNotNull(groups);
        assertEquals(4, groups.size());        
        assertTrue(groups.contains(new Group(groupsIds.get(0), "")));
        assertTrue(groups.contains(new Group(groupsIds.get(1), "")));
        assertTrue(groups.contains(new Group(groupsIds.get(2), "")));
        assertTrue(groups.contains(new Group(groupsIds.get(3), "")));
        
        // GET USER GROUPS
        Set<String> groupsName;
        groupsName = _groupsManager.getUserGroups("test");
        assertEquals(2, groupsName.size());
        assertTrue(groupsName.contains(groupsIds.get(0)));
        assertTrue(groupsName.contains(groupsIds.get(1)));
        
        groupsName = _groupsManager.getUserGroups("test2");
        assertEquals(2, groupsName.size());
        assertTrue(groupsName.contains(groupsIds.get(1)));
        assertTrue(groupsName.contains(groupsIds.get(2)));
        
        // SAX GROUPS
        DOMHandlerFactory dom = (DOMHandlerFactory) Init.getPluginServiceManager().lookup(DOMHandlerFactory.ROLE);
        DOMHandler handler = dom.createDOMHandler();
        handler.startDocument();
        _groupsManager.toSAX(handler, -1, 0, new HashMap<String, String>());
        handler.endDocument();

        XPathProcessor xpath = (XPathProcessor) Init.getPluginServiceManager().lookup(XPathProcessor.ROLE);
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups)"));
        assertEquals(5.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups/*)")); // +1.0 for total tag
        assertEquals(4.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups/group)"));
        assertEquals(4.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups/group[count(@*) = 1])"));
        assertEquals(4.0, xpath.evaluateAsNumber(handler.getDocument(), "count(/groups/group[count(label) = 1 and count(users) = 1])"));
        
        Node groupNode;
        groupNode = xpath.selectSingleNode(handler.getDocument(), "/groups/group[@id='" + groupsIds.get(0) + "']");
        assertEquals("Group 1", xpath.evaluateAsString(groupNode, "label"));
        assertEquals(1.0, xpath.evaluateAsNumber(groupNode, "count(users/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(groupNode, "count(users/user)"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'test']"));
        
        groupNode = xpath.selectSingleNode(handler.getDocument(), "/groups/group[@id='" + groupsIds.get(1) + "']");
        assertEquals("Group 2", xpath.evaluateAsString(groupNode, "label"));
        assertEquals(2.0, xpath.evaluateAsNumber(groupNode, "count(users/*)"));
        assertEquals(2.0, xpath.evaluateAsNumber(groupNode, "count(users/user)"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'test']"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'test2']"));
        
        groupNode = xpath.selectSingleNode(handler.getDocument(), "/groups/group[@id='" + groupsIds.get(2) + "']");
        assertEquals("Group 3", xpath.evaluateAsString(groupNode, "label"));
        assertEquals(1.0, xpath.evaluateAsNumber(groupNode, "count(users/*)"));
        assertEquals(1.0, xpath.evaluateAsNumber(groupNode, "count(users/user)"));
        assertTrue(xpath.evaluateAsBoolean(groupNode, "users/user[text() = 'test2']"));

        groupNode = xpath.selectSingleNode(handler.getDocument(), "/groups/group[@id='" + groupsIds.get(3) + "']");
        assertEquals("Group 4", xpath.evaluateAsString(groupNode, "label"));
        assertEquals(0.0, xpath.evaluateAsNumber(groupNode, "count(users/*)"));
    }
 
    private List<String> _getGroupsIds() throws Exception
    {
        List<String> result = new ArrayList<>();

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(ConnectionHelper.CORE_POOL_NAME);
            stmt = connection.prepareStatement("SELECT Id FROM Groups order by Id");
            
            rs = stmt.executeQuery();
            while (rs.next())
            {
                result.add(rs.getString("Id"));
            }
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
        
        return result;
    }

    /**
     * Test the addition of a new user
     * @throws Exception if an error occurs
     */
    public void testCorrectAdd() throws Exception
    {
        ModifiableGroupsManager groupsManager = (ModifiableGroupsManager) _groupsManager;
        
        MyGroupListener listener1 = new MyGroupListener();
        MyGroupListener listener2 = new MyGroupListener();
        groupsManager.registerListener(listener1);
        groupsManager.registerListener(listener2);
        
        // Correct additions
        Group group1 = groupsManager.add("Group 1");
        _checkListener(listener1, listener2, 1, 0, 0);
        
        Group group2 = groupsManager.add("Group 2");
        _checkListener(listener1, listener2, 2, 0, 0);

        Group group3 = groupsManager.add("Group 3");
        _checkListener(listener1, listener2, 3, 0, 0);

        assertNotNull(groupsManager.getGroup(group1.getId()));
        assertNotNull(groupsManager.getGroup(group2.getId()));
        assertNotNull(groupsManager.getGroup(group3.getId()));
        assertNotSame(group1.getId(), group2.getId());
    }
    
    /**
     * Test the update of a user incorrectly
     * @throws Exception if an error occurs
     */
    public void testIncorrectUpdate() throws Exception
    {
        ModifiableGroupsManager groupsManager = (ModifiableGroupsManager) _groupsManager;
        
        MyGroupListener listener1 = new MyGroupListener();
        MyGroupListener listener2 = new MyGroupListener();
        groupsManager.registerListener(listener1);
        groupsManager.registerListener(listener2);
        
        // Incorrect modification
        try
        {
            Group foo = new Group("foo", "Foo");
            foo.addUser("user1");
            
            groupsManager.update(foo);
            fail("Update should have failed");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior since login does not exist
            _checkListener(listener1, listener2, 0, 0, 0);
        }
    }
    
    /**
     * Test the update of a user
     * @throws Exception if an error occurs
     */
    public void testCorrectUpdate() throws Exception
    {
        ModifiableGroupsManager groupsManager = (ModifiableGroupsManager) _groupsManager;
        
        MyGroupListener listener1 = new MyGroupListener();
        MyGroupListener listener2 = new MyGroupListener();
        groupsManager.registerListener(listener1);
        groupsManager.registerListener(listener2);
        
        Group group1 = groupsManager.add("Group 1");
        _checkListener(listener1, listener2, 1, 0, 0);
        
        group1.addUser("test");
        groupsManager.update(group1);
        _checkListener(listener1, listener2, 1, 1, 0);
        assertEquals(1, groupsManager.getGroup(group1.getId()).getUsers().size());

        group1.addUser("test2");
        groupsManager.update(group1);
        _checkListener(listener1, listener2, 1, 2, 0);
        assertEquals(2, groupsManager.getGroup(group1.getId()).getUsers().size());
        
        group1.addUser("test");
        groupsManager.update(group1);
        _checkListener(listener1, listener2, 1, 3, 0);
        assertEquals(2, groupsManager.getGroup(group1.getId()).getUsers().size());
        
        group1.getUsers().remove("test");
        groupsManager.update(group1);
        _checkListener(listener1, listener2, 1, 4, 0);
        assertEquals(1, groupsManager.getGroup(group1.getId()).getUsers().size());
    }
    
    /**
     * Test incorrects remove
     * @throws Exception if an error occurs
     */
    public void testIncorrectRemove() throws Exception
    {
        ModifiableGroupsManager groupsManager = (ModifiableGroupsManager) _groupsManager;

        MyGroupListener listener1 = new MyGroupListener();
        MyGroupListener listener2 = new MyGroupListener();
        groupsManager.registerListener(listener1);
        groupsManager.registerListener(listener2);

        try
        {
            groupsManager.remove("foo");
            fail("Remove should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            _checkListener(listener1, listener2, 0, 0, 0);
        }

        Group group1 = groupsManager.add("Group 1");
        _checkListener(listener1, listener2, 1, 0, 0);

        try
        {
            groupsManager.remove("foo");
            fail("Remove should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            _checkListener(listener1, listener2, 1, 0, 0);
            Group group = groupsManager.getGroup(group1.getId());
            assertNotNull(group);
        }
    }
    
    /**
     * Test corrects remove
     * @throws Exception if an error occurs
     */
    public void testCorrectRemove() throws Exception
    {
        ModifiableGroupsManager groupsManager = (ModifiableGroupsManager) _groupsManager;

        MyGroupListener listener1 = new MyGroupListener();
        MyGroupListener listener2 = new MyGroupListener();
        groupsManager.registerListener(listener1);
        groupsManager.registerListener(listener2);

        Group group1 = groupsManager.add("Group 1");
        _checkListener(listener1, listener2, 1, 0, 0);
        group1.addUser("test");
        groupsManager.update(group1);
        _checkListener(listener1, listener2, 1, 1, 0);

        groupsManager.remove(group1.getId());
        _checkListener(listener1, listener2, 1, 1, 1);
        
        assertNull(groupsManager.getGroup(group1.getId()));
    }
    
    private void _checkListener(MyGroupListener listener1, MyGroupListener listener2, int added, int updated, int removed)
    {
        assertEquals(added, listener1.getAddedGroups().size());
        assertEquals(updated, listener1.getUpdatedGroups().size());
        assertEquals(removed, listener1.getRemovedGroups().size());

        assertEquals(added, listener2.getAddedGroups().size());
        assertEquals(updated, listener2.getUpdatedGroups().size());
        assertEquals(removed, listener2.getRemovedGroups().size());
    }
    
    /**
     * Group listener
     */
    public class MyGroupListener implements GroupListener
    {
        private List<String> _addedGroups = new ArrayList<>();
        private List<String> _removedGroups = new ArrayList<>();
        private List<String> _updatedGroups = new ArrayList<>();

        /**
         * Returns the added groups'list
         * @return the added groups'list
         */
        public List<String> getAddedGroups()
        {
            return _addedGroups;
        }

        /**
         * Returns the removed groups'list
         * @return the removed groups'list
         */
        public List<String> getRemovedGroups()
        {
            return _removedGroups;
        }
        
        /**
         * Returns the updated groups'list
         * @return the updated groups'list
         */
        public List<String> getUpdatedGroups()
        {
            return _updatedGroups;
        }
        
        public void groupAdded(String id)
        {
            if (id == null)
            {
                throw new RuntimeException("Listener detected a null group addition");
            }
            _addedGroups.add(id);
        }

        public void groupRemoved(String id)
        {
            if (id == null)
            {
                throw new RuntimeException("Listener detected a null group removal");
            }
            _removedGroups.add(id);
        }

        public void groupUpdated(String id)
        {
            if (id == null)
            {
                throw new RuntimeException("Listener detected a null group update");
            }
            _updatedGroups.add(id);
        }
        
    }

}
