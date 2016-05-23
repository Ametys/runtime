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
package org.ametys.runtime.test.rights.profile.hierarchical;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.ametys.core.group.Group;
import org.ametys.core.group.GroupDirectoryDAO;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.right.RightsManager;
import org.ametys.core.right.RightsManager.RightResult;
import org.ametys.core.right.profile.Profile;
import org.ametys.core.user.UserIdentity;
import org.ametys.plugins.core.impl.right.profile.DefaultProfileBasedRightsManager;
import org.ametys.plugins.core.impl.right.profile.HierarchicalProfileBasedRightsManager;
import org.ametys.plugins.core.impl.right.profile.ProfileBasedRightsManager;
import org.ametys.runtime.test.AbstractJDBCTestCase;
import org.ametys.runtime.test.Init;

/**
 * Tests the HierarchicalProfileBasedRightsManagerTestCase
 */
public abstract class AbstractHierarchicalProfileBasedRightsManagerTestCase extends AbstractJDBCTestCase
{
    private RightsManager _rightsManager;
    
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

        _rightsManager = (RightsManager) Init.getPluginServiceManager().lookup(RightsManager.ROLE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _deleteGroupDirectory();
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Check the type of the rights manager impl 
     * @throws Exception if an error occurs
     */
    public void testType() throws Exception
    {
        assertTrue(_rightsManager instanceof DefaultProfileBasedRightsManager);
        assertTrue(_rightsManager instanceof ProfileBasedRightsManager);
        assertTrue(_rightsManager instanceof HierarchicalProfileBasedRightsManager);
    }
    
    /**
     * Test an empty db
     * @throws Exception if an error occurs
     */
    public void testEmpty() throws Exception
    {
        ProfileBasedRightsManager profileRightsManager = (ProfileBasedRightsManager) _rightsManager;
        
        Set<UserIdentity> users = profileRightsManager.getGrantedUsers("Runtime_Rights_User_Handle", null);
        assertEquals(0, users.size());
        
        Set<String> rights = profileRightsManager.getUserRights(new UserIdentity("foo", "population"), null);
        assertEquals(0, rights.size());
        
        Set<Profile> profiles = profileRightsManager.getProfiles();
        assertEquals(0, profiles.size());
    }
    
    /**
     * Test a filled db
     * @throws Exception if an error occurs
     */
    public void testFilled() throws Exception
    {
        ProfileBasedRightsManager profileRightsManager = (ProfileBasedRightsManager) _rightsManager;
        
        Set<UserIdentity> users;
        UserIdentity test = new UserIdentity("test", "population");
        UserIdentity test2 = new UserIdentity("test2", "population");
        
        // Fill DB
        _setDatabase(Arrays.asList(getPopulateScripts()));
        
        _createGroupDirectory();

        users = profileRightsManager.getGrantedUsers("right1", null);
        assertEquals(1, users.size());
        assertTrue(users.contains(test));
        users = profileRightsManager.getGrantedUsers("right1", "/test");
        assertEquals(1, users.size());
        assertTrue(users.contains(test));
        users = profileRightsManager.getGrantedUsers("right1", "/test/test");
        assertEquals(1, users.size());
        users = profileRightsManager.getGrantedUsers("right1", "/test2");
        assertEquals(0, users.size());
        users = profileRightsManager.getGrantedUsers("right1", "/test2/test2");
        assertEquals(1, users.size());
        assertTrue(users.contains(test));
        users = profileRightsManager.getGrantedUsers("right1", "/test2/test2/test2");
        assertEquals(1, users.size());
        users = profileRightsManager.getGrantedUsers("right1", "/test3");
        assertEquals(1, users.size());
        assertTrue(users.contains(test));
        users = profileRightsManager.getGrantedUsers("right2", null);
        assertEquals(1, users.size());
        assertTrue(users.contains(test));
        users = profileRightsManager.getGrantedUsers("right3", null);
        assertEquals(2, users.size());
        assertTrue(users.contains(test));
        assertTrue(users.contains(test2));
        users = profileRightsManager.getGrantedUsers("right3", "/test");
        assertEquals(0, users.size());
        users = profileRightsManager.getGrantedUsers("right3", "/test2/test2");
        assertEquals(1, users.size());
        assertTrue(users.contains(test2));
        users = profileRightsManager.getGrantedUsers("right3", "/test3");
        assertEquals(1, users.size());
        assertTrue(users.contains(test));
        
        Set<String> rights;
        rights = profileRightsManager.getUserRights(test, null);
        assertEquals(3, rights.size());
        rights = profileRightsManager.getUserRights(test, "/test");
        assertEquals(2, rights.size());
        rights = profileRightsManager.getUserRights(test, "/test/test");
        assertEquals(2, rights.size());
        rights = profileRightsManager.getUserRights(test, "/test2");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights(test, "/test2/test2");
        assertEquals(2, rights.size());
        rights = profileRightsManager.getUserRights(test, "/test3");
        assertEquals(3, rights.size());
        rights = profileRightsManager.getUserRights(test, "/test3/test3");
        assertEquals(3, rights.size());
        rights = profileRightsManager.getUserRights(test2, null);
        assertEquals(1, rights.size());
        rights = profileRightsManager.getUserRights(test2, "/test");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights(test2, "/test/test");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights(test2, "/test2");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights(test2, "/test2/test2");
        assertEquals(1, rights.size());
        rights = profileRightsManager.getUserRights(test2, "/test3");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights(test2, "/test3/test3");
        assertEquals(0, rights.size());
    }
    
    /**
     * Test profiles methods
     * @throws Exception if an error occurs
     */
    public void testProfiles() throws Exception
    {
        ProfileBasedRightsManager profileRightsManager = (ProfileBasedRightsManager) _rightsManager;

        Set<Profile> profiles;
        Profile profile;
        Set<String> rights;
        
        profile = profileRightsManager.addProfile("Profile 1");
        profile = profileRightsManager.getProfile(profile.getId());
        assertEquals("Profile 1", profile.getName());
        
        profiles = profileRightsManager.getProfiles();
        assertEquals(1, profiles.size());
        assertEquals("Profile 1", profiles.iterator().next().getName());
        
        profile.rename("Profile 1 renamed");
        profile.addRight("right1");
        profile.addRight("right2");
        profile = profileRightsManager.getProfile(profile.getId());
        assertEquals("Profile 1 renamed", profile.getName());
        rights = profile.getRights();
        assertEquals(2, rights.size());
        assertTrue(rights.contains("right1"));
        assertTrue(rights.contains("right2"));
        
        profile.removeRights();
        profile = profileRightsManager.getProfile(profile.getId());
        rights = profile.getRights();
        assertEquals(0, rights.size());
        
        profile.remove();
        profile = profileRightsManager.getProfile(profile.getId());
        assertNull(profile);
        
        profiles = profileRightsManager.getProfiles();
        assertEquals(0, profiles.size());
    }
    
    /**
     * Test rights modifications
     * @throws Exception if an error occurs
     */
    public void testRight() throws Exception
    {
        ProfileBasedRightsManager profileRightsManager = (ProfileBasedRightsManager) _rightsManager;

        _setDatabase(Arrays.asList(getPopulateScripts()));
        
        Set<String> rights;
        RightResult result;
        
        UserIdentity test = new UserIdentity("test", "population");
        
        for (Profile profile : profileRightsManager.getProfiles())
        {
            profile.remove();
        }
        
        Profile profile1 = profileRightsManager.addProfile("MyProfil1");
        profile1.addRight("right1");
        profile1.addRight("right2");
        Profile profile2 = profileRightsManager.addProfile("MyProfil2");
        profile2.addRight("right3");

        // USER
        profileRightsManager.addUserRight(test, "/test", profile1.getId());
        rights = profileRightsManager.getUserRights(test, null);
        assertEquals(2, rights.size());
        
        profileRightsManager.addUserRight(test, "/test", profile2.getId());
        rights = profileRightsManager.getUserRights(test, null);
        assertEquals(3, rights.size());
        
        profileRightsManager.removeUserProfile(test, profile2.getId(), "/test");
        rights = profileRightsManager.getUserRights(test, null);
        assertEquals(2, rights.size());
        
        profileRightsManager.addUserRight(test, "/test2", profile2.getId());
        rights = profileRightsManager.getUserRights(test, null);
        assertEquals(3, rights.size());

        profileRightsManager.removeUserProfiles(test, "/test");
        rights = profileRightsManager.getUserRights(test, null);
        assertEquals(1, rights.size());

        profileRightsManager.removeUserProfiles(test, null);
        rights = profileRightsManager.getUserRights(test, null);
        assertEquals(0, rights.size());

        
        // GROUP
        Group group = _createGroupDirectory().getGroups().iterator().next(); 
        UserIdentity userIdentity = group.getUsers().iterator().next();

        profileRightsManager.addGroupRight(group.getIdentity(), "/test", profile1.getId());
        rights = profileRightsManager.getUserRights(userIdentity, null);
        assertEquals(2, rights.size());
        
        profileRightsManager.addGroupRight(group.getIdentity(), "/test", profile2.getId());
        rights = profileRightsManager.getUserRights(userIdentity, null);
        assertEquals(3, rights.size());
        
        profileRightsManager.removeGroupProfile(group.getIdentity(), profile2.getId(), "/test");
        rights = profileRightsManager.getUserRights(userIdentity, null);
        assertEquals(2, rights.size());
        
        profileRightsManager.addGroupRight(group.getIdentity(), "/test2", profile2.getId());
        rights = profileRightsManager.getUserRights(userIdentity, null);
        assertEquals(3, rights.size());

        profileRightsManager.removeGroupProfiles(group.getIdentity(), "/test");
        rights = profileRightsManager.getUserRights(userIdentity, null);
        assertEquals(1, rights.size());

        profileRightsManager.removeGroupProfiles(group.getIdentity(), null);
        rights = profileRightsManager.getUserRights(userIdentity, null);
        assertEquals(0, rights.size());
        
        
        // Context update and removal.
        Profile profile = profileRightsManager.addProfile("MyProfile");
        profile.addRight("myright");
        Profile profileA = profileRightsManager.addProfile("ProfileA");
        Profile profileB = profileRightsManager.addProfile("ProfileB");
        Profile profileC = profileRightsManager.addProfile("ProfileC");
        Profile profileD = profileRightsManager.addProfile("ProfileD");
        Profile profileE = profileRightsManager.addProfile("ProfileE");
        profileA.addRight("rightA");
        profileB.addRight("rightB");
        profileC.addRight("rightC");
        profileD.addRight("rightD");
        profileE.addRight("rightE");
        
        profileRightsManager.addUserRight(test, "/context", profile.getId());
        profileRightsManager.addUserRight(test, "/context/a", profileA.getId());
        profileRightsManager.addUserRight(test, "/context/a/b", profileB.getId());
        profileRightsManager.addUserRight(test, "/context/a/b/c", profileC.getId());
        profileRightsManager.addUserRight(test, "/context/aaa", profileD.getId());
        profileRightsManager.addUserRight(test, "/context/aaa/bbb", profileE.getId());
        profileRightsManager.addUserRight(test, "/context/ddd", profileD.getId());
        
        rights = profileRightsManager.getUserRights(test, "/context");
        assertEquals(1, rights.size());
        
        profileRightsManager.updateContext("/context", "/newcontext");
        rights = profileRightsManager.getUserRights(test, "/context");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights(test, "/context/a/b/c");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights(test, "/newcontext");
        assertEquals(1, rights.size());
        rights = profileRightsManager.getUserRights(test, "/newcontext/a/b/c");
        assertEquals(4, rights.size());
        
        profileRightsManager.removeUserProfile(test, profile.getId(), "/newcontext");
        
        profileRightsManager.updateContext("/newcontext/a", "/newcontext/d");
        
        rights = profileRightsManager.getUserRights(test, "/newcontext/a");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights(test, "/newcontext/d");
        assertEquals(1, rights.size());
        rights = profileRightsManager.getUserRights(test, "/newcontext/aaa");
        assertEquals(1, rights.size());
        
        result = profileRightsManager.hasRight(test, "rightA", "/newcontext/a");
        assertEquals(RightResult.RIGHT_NOK, result);
        
        result = profileRightsManager.hasRight(test, "rightA", "/newcontext/d");
        assertEquals(RightResult.RIGHT_OK, result);
        
        result = profileRightsManager.hasRight(test, "rightC", "/newcontext/a/b/c");
        assertEquals(RightResult.RIGHT_NOK, result);
        
        result = profileRightsManager.hasRight(test, "rightC", "/newcontext/d/b/c");
        assertEquals(RightResult.RIGHT_OK, result);
        
        result = profileRightsManager.hasRight(test, "rightD", "/newcontext/aaa");
        assertEquals(RightResult.RIGHT_OK, result);
        
        result = profileRightsManager.hasRight(test, "rightE", "/newcontext/aaa/bbb");
        assertEquals(RightResult.RIGHT_OK, result);
        
        profileRightsManager.removeAll("/newcontext/d");
        rights = profileRightsManager.getUserRights(test, "/newcontext/ddd");
        assertEquals(1, rights.size());
        
        profileRightsManager.removeAll("/newcontext");
        rights = profileRightsManager.getUserRights(test, "/newcontext/ddd");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights(test, "/newcontext");
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
