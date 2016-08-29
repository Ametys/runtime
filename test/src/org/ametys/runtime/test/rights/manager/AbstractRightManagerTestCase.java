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
package org.ametys.runtime.test.rights.manager;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ametys.core.group.Group;
import org.ametys.core.group.GroupDirectoryDAO;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.right.Profile;
import org.ametys.core.right.RightManager;
import org.ametys.core.right.RightProfilesDAO;
import org.ametys.core.user.UserIdentity;
import org.ametys.runtime.test.AbstractJDBCTestCase;
import org.ametys.runtime.test.Init;

/**
 * Tests the {@link RightManager}
 */
public abstract class AbstractRightManagerTestCase extends AbstractJDBCTestCase
{
    private RightManager _rightManager;
    private RightProfilesDAO _profilesDAO;
    
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
     * @param sqlDataSourceFileName The file name in config env dir
     * @throws Exception if an error occurs
     */
    protected void _resetDB(String runtimeFilename, String configFileName, String sqlDataSourceFileName) throws Exception
    {
        super.setUp();
        
        _startApplication("test/environments/runtimes/" + runtimeFilename, "test/environments/configs/" + configFileName, "test/environments/datasources/" + sqlDataSourceFileName, null, "test/environments/webapp1");

        _setDatabase(Arrays.asList(getScripts()));

        _rightManager = (RightManager) Init.getPluginServiceManager().lookup(RightManager.ROLE);
        _profilesDAO = (RightProfilesDAO) Init.getPluginServiceManager().lookup(RightProfilesDAO.ROLE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _deleteGroupDirectory();
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Test an empty db
     * @throws Exception if an error occurs
     */
    public void testEmpty() throws Exception
    {
        Set<UserIdentity> users = _rightManager.getAllowedUsers("Runtime_Rights_User_Handle", "foo");
        assertEquals(0, users.size());
        
        assertTrue(_rightManager.getUserRights(new UserIdentity("foo", "population"), "/foo").isEmpty());
        
        List<Profile> profiles = _rightManager.getProfiles();
        assertEquals(0, profiles.size());
    }
    
    /**
     * Test a filled db
     * @throws Exception if an error occurs
     */
    public void testFilled() throws Exception
    {
        Set<UserIdentity> users;
        UserIdentity test = new UserIdentity("test", "population");
        UserIdentity test2 = new UserIdentity("test2", "population");
        
        // Fill DB
        _setDatabase(Arrays.asList(getPopulateScripts()));
        
        _createGroupDirectory();

        users = _rightManager.getAllowedUsers("right1", "/contributor/test");
        assertEquals(1, users.size());
        assertTrue(users.contains(test));
        users = _rightManager.getAllowedUsers("right1", "/contributor/test2");
        assertEquals(0, users.size());
        users = _rightManager.getAllowedUsers("right1", "/contributor/test2/test2");
        assertEquals(1, users.size());
        assertTrue(users.contains(test));
        users = _rightManager.getAllowedUsers("right1", "/contributor/test3");
        assertEquals(1, users.size());
        assertTrue(users.contains(test));
        users = _rightManager.getAllowedUsers("right3", "/contributor/test");
        assertEquals(0, users.size());
        users = _rightManager.getAllowedUsers("right3", "/contributor/test2/test2");
        assertEquals(1, users.size());
        assertTrue(users.contains(test2));
        users = _rightManager.getAllowedUsers("right3", "/contributor/test3");
        assertEquals(1, users.size());
        assertTrue(users.contains(test));
        
        Set<String> rights;
        rights = _rightManager.getUserRights(test, "/contributor/test");
        assertEquals(2, rights.size());
        rights = _rightManager.getUserRights(test, "/contributor/test2");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(test, "/contributor/test2/test2");
        assertEquals(2, rights.size());
        rights = _rightManager.getUserRights(test, "/contributor/test3");
        assertEquals(3, rights.size());
        rights = _rightManager.getUserRights(test2, "/contributor/test");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(test2, "/contributor/test2");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(test2, "/contributor/test2/test2");
        assertEquals(1, rights.size());
        rights = _rightManager.getUserRights(test2, "/contributor/test3");
        assertEquals(0, rights.size());
    }
    
    /**
     * Test profiles methods
     * @throws Exception if an error occurs
     */
    public void testProfiles() throws Exception
    {
        List<Profile> profiles;
        Profile profile;
        List<String> rights;
        
        profile = _rightManager.addProfile("Profile 1");
        profile = _rightManager.getProfile(profile.getId());
        assertEquals("Profile 1", profile.getLabel());
        
        profiles = _rightManager.getProfiles();
        assertEquals(1, profiles.size());
        assertEquals("Profile 1", profiles.iterator().next().getLabel());
        
        _profilesDAO.renameProfile(profile, "Profile 1 renamed");
        _profilesDAO.addRight(profile, "right1");
        _profilesDAO.addRight(profile, "right2");
        
        profile = _rightManager.getProfile(profile.getId());
        assertEquals("Profile 1 renamed", profile.getLabel());
        rights = _profilesDAO.getRights(profile);
        assertEquals(2, rights.size());
        assertTrue(rights.contains("right1"));
        assertTrue(rights.contains("right2"));
        
        _profilesDAO.removeRights(profile);
        profile = _rightManager.getProfile(profile.getId());
        rights = _profilesDAO.getRights(profile);
        assertEquals(0, rights.size());
        
        _rightManager.removeProfile(profile.getId());
        profile = _rightManager.getProfile(profile.getId());
        assertNull(profile);
        
        profiles = _rightManager.getProfiles();
        assertEquals(0, profiles.size());
    }
    
    /**
     * Test rights modifications
     * @throws Exception if an error occurs
     */
    public void testRight() throws Exception
    {
        _setDatabase(Arrays.asList(getPopulateScripts()));
        
        Set<String> rights;
        
        UserIdentity test = new UserIdentity("test", "population");
        
        for (Profile profile : _rightManager.getProfiles())
        {
            _rightManager.removeProfile(profile.getId());
        }
        
        Profile profile1 = _rightManager.addProfile("MyProfil1");
        _profilesDAO.addRight(profile1, "right1");
        _profilesDAO.addRight(profile1, "right2");
        Profile profile2 = _rightManager.addProfile("MyProfil2");
        _profilesDAO.addRight(profile2, "right3");

        // USER
        _rightManager.allowProfileToUser(test, profile1.getId(), "/contributor/test");
        rights = _rightManager.getUserRights(test, "/contributor/test");
        assertEquals(2, rights.size());
        
        _rightManager.allowProfileToUser(test, profile2.getId(), "/contributor/test");
        rights = _rightManager.getUserRights(test, "/contributor/test");
        assertEquals(3, rights.size());
        
        _rightManager.removeAllowedProfileFromUser(test, profile2.getId(), "/contributor/test");
        rights = _rightManager.getUserRights(test, "/contributor/test");
        assertEquals(2, rights.size());
        
        _rightManager.allowProfileToUser(test, profile2.getId(), "/contributor/test2");
        rights = _rightManager.getUserRights(test, "/contributor/test");
        assertEquals(2, rights.size());
        rights = _rightManager.getUserRights(test, "/contributor/test2");
        assertEquals(1, rights.size());

        _rightManager.removeAllowedProfileFromUser(test, profile1.getId(), "/contributor/test");
        _rightManager.removeAllowedProfileFromUser(test, profile2.getId(), "/contributor/test");
        rights = _rightManager.getUserRights(test, "/contributor/test");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(test, "/contributor/test2");
        assertEquals(1, rights.size());

        _rightManager.removeAllowedProfileFromUser(test, profile1.getId(), "/contributor/test2");
        _rightManager.removeAllowedProfileFromUser(test, profile2.getId(), "/contributor/test2");
        rights = _rightManager.getUserRights(test, "/contributor/test1");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(test, "/contributor/test2");
        assertEquals(0, rights.size());

        
        // GROUP
        Group group = _createGroupDirectory().getGroups().iterator().next(); 
        UserIdentity userIdentity = group.getUsers().iterator().next();

        _rightManager.allowProfileToGroup(group.getIdentity(), profile1.getId(), "/contributor/test");
        rights = _rightManager.getUserRights(userIdentity, "/contributor/test");
        assertEquals(2, rights.size());
        
        _rightManager.allowProfileToGroup(group.getIdentity(), profile2.getId(), "/contributor/test");
        rights = _rightManager.getUserRights(userIdentity, "/contributor/test");
        assertEquals(3, rights.size());
        
        _rightManager.removeAllowedProfileFromGroup(group.getIdentity(), profile2.getId(), "/contributor/test");
        rights = _rightManager.getUserRights(userIdentity, "/contributor/test");
        assertEquals(2, rights.size());
        
        _rightManager.allowProfileToGroup(group.getIdentity(), profile2.getId(), "/contributor/test2");
        rights = _rightManager.getUserRights(userIdentity, "/contributor/test");
        assertEquals(2, rights.size());
        rights = _rightManager.getUserRights(userIdentity, "/contributor/test2");
        assertEquals(1, rights.size());

        _rightManager.removeAllowedProfileFromGroup(group.getIdentity(), profile1.getId(), "/contributor/test");
        _rightManager.removeAllowedProfileFromGroup(group.getIdentity(), profile2.getId(), "/contributor/test");
        rights = _rightManager.getUserRights(userIdentity, "/contributor/test");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(userIdentity, "/contributor/test2");
        assertEquals(1, rights.size());

        _rightManager.removeAllowedProfileFromGroup(group.getIdentity(), profile1.getId(), "/contributor/test2");
        _rightManager.removeAllowedProfileFromGroup(group.getIdentity(), profile2.getId(), "/contributor/test2");
        rights = _rightManager.getUserRights(userIdentity, "/contributor/test");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(userIdentity, "/contributor/test2");
        assertEquals(0, rights.size());
    }
    
    private GroupDirectory _createGroupDirectory() throws Exception
    {
        GroupDirectoryDAO groupDirectoryDAO = (GroupDirectoryDAO) Init.getPluginServiceManager().lookup(GroupDirectoryDAO.ROLE);
        
        String modelId = "org.ametys.plugins.core.group.directory.Jdbc";
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put(modelId + "$" + "runtime.groups.jdbc.datasource", "SQL-test");
        parameters.put(modelId + "$" + "runtime.groups.jdbc.list.table", "Groups");
        parameters.put(modelId + "$" + "runtime.groups.jdbc.composition.table", "Groups_Users");
        
        String groupId = groupDirectoryDAO.add("sql_group_directory", "foo", modelId, parameters);
        
        return groupDirectoryDAO.getGroupDirectory(groupId);
    }
    
    private void _deleteGroupDirectory() throws Exception
    {
        ((GroupDirectoryDAO) Init.getPluginServiceManager().lookup(GroupDirectoryDAO.ROLE)).remove("sql_group_directory");
    }
}
