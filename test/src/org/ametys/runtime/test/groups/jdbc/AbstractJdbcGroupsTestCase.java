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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.group.Group;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.group.GroupListener;
import org.ametys.core.group.InvalidModificationException;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.group.directory.GroupDirectoryFactory;
import org.ametys.core.group.directory.ModifiableGroupDirectory;
import org.ametys.core.user.UserIdentity;
import org.ametys.plugins.core.impl.group.directory.jdbc.JdbcGroupDirectory;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.test.AbstractJDBCTestCase;
import org.ametys.runtime.test.Init;

/**
 * Tests the JdbcGroupDirectory
 */
public abstract class AbstractJdbcGroupsTestCase extends AbstractJDBCTestCase
{
    /** the group directory */
    protected GroupDirectory _groupDirectory;
    
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
     * @param dataSourceFileName The file name in data sources env dir
     * @throws Exception if an error occurs
     */
    protected void _resetDB(String runtimeFilename, String configFileName, String dataSourceFileName) throws Exception
    {
        _startApplication("test/environments/runtimes/" + runtimeFilename, "test/environments/configs/" + configFileName, "test/environments/datasources/" + dataSourceFileName, null, "test/environments/webapp1");
        
        _setDatabase(Arrays.asList(getScripts()));

        _groupDirectory = _createGroupDirectory();
    }
    
    /**
     * Check that the jdbc group manager has the correct type
     * @throws Exception if an error occurs
     */
    public void testType() throws Exception
    {
        // DEFAULT IMPL
        assertTrue(_groupDirectory instanceof JdbcGroupDirectory);

        // MODIFIABLE
        assertTrue(_groupDirectory instanceof ModifiableGroupDirectory);
    }
    
    /**
     * Check when the db is empty
     * @throws Exception if an error occurs
     */
    public void testEmpty() throws Exception
    {
        // GET GROUP
        assertNull(_groupDirectory.getGroup("foo"));
        
        // GET GROUPS
        assertEquals(0, _groupDirectory.getGroups().size());
        
        // GET USER GROUPS
        assertEquals(0, _groupDirectory.getUserGroups("foo", "foo").size());

        // SAX GROUPS
        List<Map<String, Object>> groups = _groupDirectory.groups2JSON(-1, 0, new HashMap<String, String>());

        assertEquals(0, groups.size());

    }

    /**
     * Check when the db is filled
     * @throws Exception if an error occurs
     */
    @SuppressWarnings("unchecked")
    public void testFilled() throws Exception
    {
        Set<UserIdentity> users;
        Group group;
        
        UserIdentity test = new UserIdentity("test", "population");
        UserIdentity test2 = new UserIdentity("test2", "population");
        
        // Fill DB
        _setDatabase(Arrays.asList(getPopulateScripts()));
        
        List<String> groupsIds = _getGroupsIds();
        
        // GET GROUP
        assertNull(_groupDirectory.getGroup("foo"));
        
        group = _groupDirectory.getGroup(groupsIds.get(0));
        assertNotNull(group);
        assertEquals(groupsIds.get(0), group.getIdentity().getId());
        assertEquals("Group 1", group.getLabel());
        users = group.getUsers();
        assertEquals(1, users.size());
        assertEquals(test, users.iterator().next());
        
        group = _groupDirectory.getGroup(groupsIds.get(1));
        assertNotNull(group);
        assertEquals(groupsIds.get(1), group.getIdentity().getId());
        assertEquals("Group 2", group.getLabel());
        users = group.getUsers();
        assertEquals(2, users.size());
        assertTrue(users.contains(test));
        assertTrue(users.contains(test2));
        
        group = _groupDirectory.getGroup(groupsIds.get(2));
        assertNotNull(group);
        assertEquals(groupsIds.get(2), group.getIdentity().getId());
        assertEquals("Group 3", group.getLabel());
        users = group.getUsers();
        assertEquals(1, users.size());
        assertEquals(test2, users.iterator().next());
        
        group = _groupDirectory.getGroup(groupsIds.get(3));
        assertNotNull(group);
        assertEquals(groupsIds.get(3), group.getIdentity().getId());
        assertEquals("Group 4", group.getLabel());
        users = group.getUsers();
        assertEquals(0, users.size());
        
        // GET GROUPS
        Set<Group> groups = _groupDirectory.getGroups();
        assertNotNull(groups);
        assertEquals(4, groups.size());        
        GroupIdentity groupIdentity0 = new GroupIdentity(groupsIds.get(0), "jdbc-groups");
        assertTrue(groups.contains(new Group(groupIdentity0, "", null)));
        GroupIdentity groupIdentity1 = new GroupIdentity(groupsIds.get(1), "jdbc-groups");
        assertTrue(groups.contains(new Group(groupIdentity1, "", null)));
        GroupIdentity groupIdentity2 = new GroupIdentity(groupsIds.get(2), "jdbc-groups");
        assertTrue(groups.contains(new Group(groupIdentity2, "", null)));
        GroupIdentity groupIdentity3 = new GroupIdentity(groupsIds.get(3), "jdbc-groups");
        assertTrue(groups.contains(new Group(groupIdentity3, "", null)));
        
        // GET USER GROUPS
        Set<String> groupsName;
        groupsName = _groupDirectory.getUserGroups("test", "population");
        assertEquals(2, groupsName.size());
        assertTrue(groupsName.contains(groupsIds.get(0)));
        assertTrue(groupsName.contains(groupsIds.get(1)));
        
        groupsName = _groupDirectory.getUserGroups("test2", "population");
        assertEquals(2, groupsName.size());
        assertTrue(groupsName.contains(groupsIds.get(1)));
        assertTrue(groupsName.contains(groupsIds.get(2)));
        
        // GROUPS to JSON
        List<Map<String, Object>> jsonGroups = _groupDirectory.groups2JSON(-1, 0, new HashMap<String, String>());

        assertEquals(4, jsonGroups.size());
        
        List<Map<String, Object>> filteredJsonGroups;
        Set<UserIdentity> jsonUsers;
        UserIdentity jsonUser;
        filteredJsonGroups = _filterJsonGroupsById(jsonGroups, groupsIds.get(0));
        assertEquals(1, filteredJsonGroups.size());
        assertEquals("Group 1", filteredJsonGroups.iterator().next().get("label"));
        jsonUsers = (Set) filteredJsonGroups.iterator().next().get("users");
        assertEquals(1, jsonUsers.size());
        jsonUser = jsonUsers.iterator().next();
        assertEquals("test", jsonUser.getLogin());
        assertEquals("population", jsonUser.getPopulationId());
        
        filteredJsonGroups = _filterJsonGroupsById(jsonGroups, groupsIds.get(1));
        assertEquals(1, filteredJsonGroups.size());
        assertEquals("Group 2", filteredJsonGroups.iterator().next().get("label"));
        jsonUsers = (Set) filteredJsonGroups.iterator().next().get("users");
        assertEquals(2, jsonUsers.size());
        assertTrue(jsonUsers.contains(test));
        assertTrue(jsonUsers.contains(test2));
        
        filteredJsonGroups = _filterJsonGroupsById(jsonGroups, groupsIds.get(2));
        assertEquals(1, filteredJsonGroups.size());
        assertEquals("Group 3", filteredJsonGroups.iterator().next().get("label"));
        jsonUsers = (Set) filteredJsonGroups.iterator().next().get("users");
        assertEquals(1, jsonUsers.size());
        jsonUser = jsonUsers.iterator().next();
        assertEquals("test2", jsonUser.getLogin());
        assertEquals("population", jsonUser.getPopulationId());
        
        filteredJsonGroups = _filterJsonGroupsById(jsonGroups, groupsIds.get(3));
        assertEquals(1, filteredJsonGroups.size());
        assertEquals("Group 4", filteredJsonGroups.iterator().next().get("label"));
        jsonUsers = (Set) filteredJsonGroups.iterator().next().get("users");
        assertEquals(0, jsonUsers.size());
    }
    
    private List<Map<String, Object>> _filterJsonGroupsById(List<Map<String, Object>> jsonGroups, String id)
    {
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (Map<String, Object> jsonGroup : jsonGroups)
        {
            if (id.equals(jsonGroup.get("id")))
            {
                results.add(jsonGroup);
            }
        }
        
        return results;
    }
 
    private List<String> _getGroupsIds() throws Exception
    {
        List<String> result = new ArrayList<>();

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            connection = ConnectionHelper.getConnection("SQL-test");
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
        ModifiableGroupDirectory groupDirectory = (ModifiableGroupDirectory) _groupDirectory;
        
        MyGroupListener listener1 = new MyGroupListener();
        MyGroupListener listener2 = new MyGroupListener();
        groupDirectory.registerListener(listener1);
        groupDirectory.registerListener(listener2);
        
        // Correct additions
        Group group1 = groupDirectory.add("Group 1");
        _checkListener(listener1, listener2, 1, 0, 0);
        
        Group group2 = groupDirectory.add("Group 2");
        _checkListener(listener1, listener2, 2, 0, 0);

        Group group3 = groupDirectory.add("Group 3");
        _checkListener(listener1, listener2, 3, 0, 0);

        assertNotNull(groupDirectory.getGroup(group1.getIdentity().getId()));
        assertNotNull(groupDirectory.getGroup(group2.getIdentity().getId()));
        assertNotNull(groupDirectory.getGroup(group3.getIdentity().getId()));
        assertNotSame(group1.getIdentity().getId(), group2.getIdentity().getId());
    }
    
    /**
     * Test the update of a user incorrectly
     * @throws Exception if an error occurs
     */
    public void testIncorrectUpdate() throws Exception
    {
        ModifiableGroupDirectory groupDirectory = (ModifiableGroupDirectory) _groupDirectory;
        
        MyGroupListener listener1 = new MyGroupListener();
        MyGroupListener listener2 = new MyGroupListener();
        groupDirectory.registerListener(listener1);
        groupDirectory.registerListener(listener2);
        
        // Incorrect modification
        try
        {
            GroupIdentity groupIdentity = new GroupIdentity("foo", "groupDirectory");
            Group foo = new Group(groupIdentity, "Foo", null);
            foo.addUser(new UserIdentity("user1", "population"));
            
            groupDirectory.update(foo);
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
        ModifiableGroupDirectory groupDirectory = (ModifiableGroupDirectory) _groupDirectory;
        
        MyGroupListener listener1 = new MyGroupListener();
        MyGroupListener listener2 = new MyGroupListener();
        groupDirectory.registerListener(listener1);
        groupDirectory.registerListener(listener2);
        
        Group group1 = groupDirectory.add("Group 1");
        _checkListener(listener1, listener2, 1, 0, 0);
        
        group1.addUser(new UserIdentity("test", "population"));
        groupDirectory.update(group1);
        _checkListener(listener1, listener2, 1, 1, 0);
        assertEquals(1, groupDirectory.getGroup(group1.getIdentity().getId()).getUsers().size());

        group1.addUser(new UserIdentity("test2", "population"));
        groupDirectory.update(group1);
        _checkListener(listener1, listener2, 1, 2, 0);
        assertEquals(2, groupDirectory.getGroup(group1.getIdentity().getId()).getUsers().size());
        
        group1.addUser(new UserIdentity("test", "population"));
        groupDirectory.update(group1);
        _checkListener(listener1, listener2, 1, 3, 0);
        assertEquals(2, groupDirectory.getGroup(group1.getIdentity().getId()).getUsers().size());
        
        group1.getUsers().remove(new UserIdentity("test", "population"));
        groupDirectory.update(group1);
        _checkListener(listener1, listener2, 1, 4, 0);
        assertEquals(1, groupDirectory.getGroup(group1.getIdentity().getId()).getUsers().size());
    }
    
    /**
     * Test incorrects remove
     * @throws Exception if an error occurs
     */
    public void testIncorrectRemove() throws Exception
    {
        ModifiableGroupDirectory groupDirectory = (ModifiableGroupDirectory) _groupDirectory;

        MyGroupListener listener1 = new MyGroupListener();
        MyGroupListener listener2 = new MyGroupListener();
        groupDirectory.registerListener(listener1);
        groupDirectory.registerListener(listener2);

        try
        {
            groupDirectory.remove("foo");
            fail("Remove should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            _checkListener(listener1, listener2, 0, 0, 0);
        }

        Group group1 = groupDirectory.add("Group 1");
        _checkListener(listener1, listener2, 1, 0, 0);

        try
        {
            groupDirectory.remove("foo");
            fail("Remove should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
            _checkListener(listener1, listener2, 1, 0, 0);
            Group group = groupDirectory.getGroup(group1.getIdentity().getId());
            assertNotNull(group);
        }
    }
    
    /**
     * Test corrects remove
     * @throws Exception if an error occurs
     */
    public void testCorrectRemove() throws Exception
    {
        ModifiableGroupDirectory groupDirectory = (ModifiableGroupDirectory) _groupDirectory;

        MyGroupListener listener1 = new MyGroupListener();
        MyGroupListener listener2 = new MyGroupListener();
        groupDirectory.registerListener(listener1);
        groupDirectory.registerListener(listener2);

        Group group1 = groupDirectory.add("Group 1");
        _checkListener(listener1, listener2, 1, 0, 0);
        group1.addUser(new UserIdentity("test", "population"));
        groupDirectory.update(group1);
        _checkListener(listener1, listener2, 1, 1, 0);

        groupDirectory.remove(group1.getIdentity().getId());
        _checkListener(listener1, listener2, 1, 1, 1);
        
        assertNull(groupDirectory.getGroup(group1.getIdentity().getId()));
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
    
    private GroupDirectory _createGroupDirectory() throws Exception
    {
        String modelId = "org.ametys.plugins.core.group.directory.Jdbc";
        
        Map<String, Object> parameters = new LinkedHashMap<>();
        //parameters.put("runtime.groups.jdbc.datasource", "SQL-ametys-internal");
        parameters.put("runtime.groups.jdbc.datasource", "SQL-test");
        parameters.put("runtime.groups.jdbc.list.table", "Groups");
        parameters.put("runtime.groups.jdbc.composition.table", "Groups_Users");
        
        
        return ((GroupDirectoryFactory) Init.getPluginServiceManager().lookup(GroupDirectoryFactory.ROLE)).createGroupDirectory("jdbc-groups", new I18nizableText("JDBC"), modelId, parameters);
    }
    
    /**
     * Group listener
     */
    public class MyGroupListener implements GroupListener
    {
        private List<GroupIdentity> _addedGroups = new ArrayList<>();
        private List<GroupIdentity> _removedGroups = new ArrayList<>();
        private List<GroupIdentity> _updatedGroups = new ArrayList<>();

        /**
         * Returns the added groups'list
         * @return the added groups'list
         */
        public List<GroupIdentity> getAddedGroups()
        {
            return _addedGroups;
        }

        /**
         * Returns the removed groups'list
         * @return the removed groups'list
         */
        public List<GroupIdentity> getRemovedGroups()
        {
            return _removedGroups;
        }
        
        /**
         * Returns the updated groups'list
         * @return the updated groups'list
         */
        public List<GroupIdentity> getUpdatedGroups()
        {
            return _updatedGroups;
        }
        
        public void groupAdded(GroupIdentity group)
        {
            if (group == null)
            {
                throw new RuntimeException("Listener detected a null group addition");
            }
            _addedGroups.add(group);
        }

        public void groupRemoved(GroupIdentity group)
        {
            if (group == null)
            {
                throw new RuntimeException("Listener detected a null group removal");
            }
            _removedGroups.add(group);
        }

        public void groupUpdated(GroupIdentity group)
        {
            if (group == null)
            {
                throw new RuntimeException("Listener detected a null group update");
            }
            _updatedGroups.add(group);
        }
        
    }

}
