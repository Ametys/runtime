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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ametys.core.group.Group;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.user.UserIdentity;
import org.ametys.runtime.test.AbstractRuntimeTestCase;

/**
 * Ldap groups' tests 
 */
public abstract class AbstractLdapGroupsTestCase extends AbstractRuntimeTestCase
{
    /** the user manager */
    protected GroupDirectory _groupDirectory;
    
    /**
     * To avoid alltest failure
     * @throws Exception if an error occurs
     */
    @SuppressWarnings("unchecked")
    public void testFilled() throws Exception
    {
        Group group;
        Set<UserIdentity> users;
        
        // IDENTITIES
        String populationId = "ldap_population";
        UserIdentity user1 = new UserIdentity("user1", populationId);
        UserIdentity user2 = new UserIdentity("user2", populationId);
        UserIdentity user3 = new UserIdentity("user3", populationId);
        UserIdentity user4 = new UserIdentity("user4", populationId);
        UserIdentity user5 = new UserIdentity("user5", populationId);
        UserIdentity user6 = new UserIdentity("user6", populationId);
        UserIdentity user7 = new UserIdentity("user7", populationId);
        UserIdentity user8 = new UserIdentity("user8", populationId);
        
        // GET GROUP
        assertNull(_groupDirectory.getGroup("foo"));
        
        group = _groupDirectory.getGroup("group1");
        assertNotNull(group);
        assertEquals("group1", group.getIdentity().getId());
        assertEquals("Group 1", group.getLabel());
        users = group.getUsers();
        assertEquals(1, users.size());
        assertEquals(user1, users.iterator().next());
        
        group = _groupDirectory.getGroup("group2");
        assertNotNull(group);
        assertEquals("group2", group.getIdentity().getId());
        assertEquals("Group 2", group.getLabel());
        users = group.getUsers();
        assertEquals(4, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
        assertTrue(users.contains(user3));
        assertTrue(users.contains(user4));
        
        group = _groupDirectory.getGroup("group3");
        assertNotNull(group);
        assertEquals("group3", group.getIdentity().getId());
        assertEquals("Group 3", group.getLabel());
        users = group.getUsers();
        assertEquals(4, users.size());
        assertTrue(users.contains(user5));
        assertTrue(users.contains(user6));
        assertTrue(users.contains(user7));
        assertTrue(users.contains(user8));
        
        // GET GROUPS
        Set<Group> groups = _groupDirectory.getGroups();
        GroupIdentity group1 = new GroupIdentity("group1", "groupDirectory");
        GroupIdentity group2 = new GroupIdentity("group2", "groupDirectory");
        
        assertNotNull(groups);
        assertEquals(3, groups.size());        
        assertTrue("group1 not found", groups.contains(new Group(group1, "", null)));
        assertTrue("group2 not found", groups.contains(new Group(group2, "", null)));
        // assertTrue("group3 not found", groups.contains(new Group("group3", "")));
        
        // GET USER GROUPS
        Set<String> groupsIds;
        groupsIds = _groupDirectory.getUserGroups("user1", populationId);
        assertEquals(2, groupsIds.size());
        assertTrue(groupsIds.contains("group1"));
        assertTrue(groupsIds.contains("group2"));
        
        groupsIds = _groupDirectory.getUserGroups("user2", populationId);
        assertEquals(1, groupsIds.size());
        assertTrue(groupsIds.contains("group2"));

        groupsIds = _groupDirectory.getUserGroups("user10", populationId);
        assertEquals(0, groupsIds.size());

        // GROUPS to JSON
        List<Map<String, Object>> jsonGroups = _groupDirectory.groups2JSON(-1, 0, new HashMap<String, String>());
        
        assertEquals(3, jsonGroups.size());
        
        List<Map<String, Object>> filteredJsonGroups;
        Set<UserIdentity> jsonUsers;
        UserIdentity jsonUser;
        filteredJsonGroups = _filterJsonGroupsById(jsonGroups, "group1");
        assertEquals(1, filteredJsonGroups.size());
        assertEquals("Group 1", filteredJsonGroups.iterator().next().get("label"));
        jsonUsers = (Set) filteredJsonGroups.iterator().next().get("users");
        assertEquals(1, jsonUsers.size());
        jsonUser = jsonUsers.iterator().next();
        assertEquals("user1", jsonUser.getLogin());
        assertEquals(populationId, jsonUser.getPopulationId());

        filteredJsonGroups = _filterJsonGroupsById(jsonGroups, "group2");
        assertEquals(1, filteredJsonGroups.size());
        assertEquals("Group 2", filteredJsonGroups.iterator().next().get("label"));
        jsonUsers = (Set) filteredJsonGroups.iterator().next().get("users");
        assertEquals(4, jsonUsers.size());
        assertTrue(jsonUsers.contains(user1));
        assertTrue(jsonUsers.contains(user2));
        assertTrue(jsonUsers.contains(user3));
        assertTrue(jsonUsers.contains(user4));
        
        filteredJsonGroups = _filterJsonGroupsById(jsonGroups, "group3");
        assertEquals(1, filteredJsonGroups.size());
        assertEquals("Group 3", filteredJsonGroups.iterator().next().get("label"));
        jsonUsers = (Set) filteredJsonGroups.iterator().next().get("users");
        assertEquals(4, jsonUsers.size());
        assertTrue(jsonUsers.contains(user5));
        assertTrue(jsonUsers.contains(user6));
        assertTrue(jsonUsers.contains(user7));
        assertTrue(jsonUsers.contains(user8));
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
}
