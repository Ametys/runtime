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
import java.io.FileInputStream;
import java.io.InputStream;
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

import org.apache.commons.io.IOUtils;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.group.Group;
import org.ametys.core.group.GroupIdentity;
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
    
    @Override
    protected String[] _getStartScripts() throws Exception
    {
        String[] scripts = new String[2];
        
        try (InputStream is = new FileInputStream("main/plugin-core/scripts/" + _getDBType() + "/jdbc_users.template.sql"))
        {
            scripts[0] = IOUtils.toString(is, "UTF-8");
            scripts[0] = scripts[0].replaceAll("%TABLENAME%", "Users");
        }
        
        try (InputStream is = new FileInputStream("main/plugin-core/scripts/" + _getDBType() + "/jdbc_groups.template.sql"))
        {
            scripts[1] = IOUtils.toString(is, "UTF-8");
            scripts[1] = scripts[1].replaceAll("%TABLENAME%", "Groups");
            scripts[1] = scripts[1].replaceAll("%TABLENAME_COMPOSITION%", "Groups_Users");
        }
        
        return scripts;
    }
    
    /**
     * Provide the scripts to run to populate the database.
     * @return the scripts to run.
     */
    protected File[] getPopulateScripts()
    {
        return new File[] {new File("test/environments/scripts/jdbc/" + _getDBType() + "/fillJDBCUsersAndGroups.sql")};
    }
    
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        _startApplication("test/environments/runtimes/runtime4.xml", "test/environments/configs/config1.xml", null, "test/environments/webapp1");
        _groupDirectory = _createGroupDirectory();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
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
        
        // Correct additions
        Group group1 = groupDirectory.add("Group 1");
        Group group2 = groupDirectory.add("Group 2");
        Group group3 = groupDirectory.add("Group 3");

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
        }
    }
    
    /**
     * Test the update of a user
     * @throws Exception if an error occurs
     */
    public void testCorrectUpdate() throws Exception
    {
        ModifiableGroupDirectory groupDirectory = (ModifiableGroupDirectory) _groupDirectory;
        
        Group group1 = groupDirectory.add("Group 1");
        
        group1.addUser(new UserIdentity("test", "population"));
        groupDirectory.update(group1);
        assertEquals(1, groupDirectory.getGroup(group1.getIdentity().getId()).getUsers().size());

        group1.addUser(new UserIdentity("test2", "population"));
        groupDirectory.update(group1);
        assertEquals(2, groupDirectory.getGroup(group1.getIdentity().getId()).getUsers().size());
        
        group1.addUser(new UserIdentity("test", "population"));
        groupDirectory.update(group1);
        assertEquals(2, groupDirectory.getGroup(group1.getIdentity().getId()).getUsers().size());
        
        group1.getUsers().remove(new UserIdentity("test", "population"));
        groupDirectory.update(group1);
        assertEquals(1, groupDirectory.getGroup(group1.getIdentity().getId()).getUsers().size());
    }
    
    /**
     * Test incorrects remove
     * @throws Exception if an error occurs
     */
    public void testIncorrectRemove() throws Exception
    {
        ModifiableGroupDirectory groupDirectory = (ModifiableGroupDirectory) _groupDirectory;

        try
        {
            groupDirectory.remove("foo");
            fail("Remove should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
        }

        Group group1 = groupDirectory.add("Group 1");

        try
        {
            groupDirectory.remove("foo");
            fail("Remove should fail");
        }
        catch (InvalidModificationException e)
        {
            // normal behavior
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

        Group group1 = groupDirectory.add("Group 1");
        group1.addUser(new UserIdentity("test", "population"));
        groupDirectory.update(group1);

        groupDirectory.remove(group1.getIdentity().getId());
        
        assertNull(groupDirectory.getGroup(group1.getIdentity().getId()));
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
}
