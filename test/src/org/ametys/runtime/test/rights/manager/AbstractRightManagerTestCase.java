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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.io.IOUtils;

import org.ametys.core.group.Group;
import org.ametys.core.group.GroupDirectoryDAO;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.right.Profile;
import org.ametys.core.right.ProfileAssignmentStorageExtensionPoint;
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
    private ProfileAssignmentStorageExtensionPoint _profileAssignmentStorageEP;
    private RightProfilesDAO _profilesDAO;
    
    /**
     * Provide the scripts to run to populate the database.
     * @return the scripts to run.
     */
    protected File[] getPopulateScripts()
    {
        return new File[] {new File("test/environments/scripts/jdbc/" + _getDBType() + "/fillProfileRights.sql")};
    }
    
    
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
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        _startApplication("test/environments/runtimes/runtime5.xml", "test/environments/configs/config1.xml", null, "test/environments/webapp4");
        
        _rightManager = (RightManager) Init.getPluginServiceManager().lookup(RightManager.ROLE);
        _profileAssignmentStorageEP = (ProfileAssignmentStorageExtensionPoint) Init.getPluginServiceManager().lookup(ProfileAssignmentStorageExtensionPoint.ROLE);
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
        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        Environment env = (Environment) environmentInformation.get("environment");
        Request request = (Request) env.getObjectModel().get(ObjectModelHelper.REQUEST_OBJECT);
        request.setAttribute("populationContexts", new ArrayList<String>());
        
        Set<UserIdentity> users = _rightManager.getAllowedUsers("Runtime_Rights_User_Handle", "foo").resolveAllowedUsers(true);
        assertEquals(0, users.size());
        
        assertTrue(_rightManager.getUserRights(new UserIdentity("foo", "population"), "/foo").isEmpty());
        
        List<Profile> profiles = _profilesDAO.getProfiles();
        assertEquals(1, profiles.size()); // There is the READER profile only
        
        _cocoon._leaveEnvironment(environmentInformation);
    }
    
    /**
     * Test a filled db
     * @throws Exception if an error occurs
     */
    public void testFilled() throws Exception
    {
        Map<String, Object> environmentInformation = _cocoon._enterEnvironment();
        
        Environment env = (Environment) environmentInformation.get("environment");
        Request request = (Request) env.getObjectModel().get(ObjectModelHelper.REQUEST_OBJECT);
        request.setAttribute("populationContexts", new ArrayList<String>());
        
        Set<UserIdentity> users;
        UserIdentity test = new UserIdentity("test", "population");
        UserIdentity test2 = new UserIdentity("test2", "population");
        
        // Fill DB
        _setDatabase(Arrays.asList(getPopulateScripts()));
        
        _createGroupDirectory();

        users = _rightManager.getAllowedUsers("right1", "/test").resolveAllowedUsers(true);
        assertEquals(1, users.size());
        assertTrue(users.contains(test));
        users = _rightManager.getAllowedUsers("right1", "/test2").resolveAllowedUsers(true);
        assertEquals(0, users.size());
        users = _rightManager.getAllowedUsers("right3", "/test").resolveAllowedUsers(true);
        assertEquals(0, users.size());
        
        Set<String> rights;
        rights = _rightManager.getUserRights(test, "/test");
        assertEquals(2, rights.size());
        rights = _rightManager.getUserRights(test, "/test2");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(test2, "/test");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(test2, "/test2");
        assertEquals(0, rights.size());
        
        _cocoon._leaveEnvironment(environmentInformation);
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
        
        profile = _profilesDAO.addProfile("Profile 1");
        profile = _profilesDAO.getProfile(profile.getId());
        assertEquals("Profile 1", profile.getLabel());
        
        profiles = _profilesDAO.getProfiles();
        assertEquals(2, profiles.size()); // The added profile "Profile 1" and READER
        for (Profile currentProfile : profiles)
        {
            if (!RightManager.READER_PROFILE_ID.equals(currentProfile.getId()))
            {
                assertEquals("Profile 1", currentProfile.getLabel());
            }
        }
        
        _profilesDAO.renameProfile(profile, "Profile 1 renamed");
        _profilesDAO.addRight(profile, "right1");
        _profilesDAO.addRight(profile, "right2");
        
        profile = _profilesDAO.getProfile(profile.getId());
        assertEquals("Profile 1 renamed", profile.getLabel());
        rights = _profilesDAO.getRights(profile);
        assertEquals(2, rights.size());
        assertTrue(rights.contains("right1"));
        assertTrue(rights.contains("right2"));
        
        _profilesDAO.removeRights(profile);
        profile = _profilesDAO.getProfile(profile.getId());
        rights = _profilesDAO.getRights(profile);
        assertEquals(0, rights.size());
        
        _profilesDAO.deleteProfile(profile);
        profile = _profilesDAO.getProfile(profile.getId());
        assertNull(profile);
        
        profiles = _profilesDAO.getProfiles();
        assertEquals(1, profiles.size()); // Only READER profile
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
        
        for (Profile profile : _profilesDAO.getProfiles())
        {
            if (!RightManager.READER_PROFILE_ID.equals(profile.getId()))
            {
                _profilesDAO.deleteProfile(profile);
            }
        }
        
        Profile profile1 = _profilesDAO.addProfile("MyProfil1");
        _profilesDAO.addRight(profile1, "right1");
        _profilesDAO.addRight(profile1, "right2");
        Profile profile2 = _profilesDAO.addProfile("MyProfil2");
        _profilesDAO.addRight(profile2, "right3");

        // USER
        _profileAssignmentStorageEP.allowProfileToUser(test, profile1.getId(), "/test");
        rights = _rightManager.getUserRights(test, "/test");
        assertEquals(2, rights.size());
        
        _profileAssignmentStorageEP.allowProfileToUser(test, profile2.getId(), "/test");
        rights = _rightManager.getUserRights(test, "/test");
        assertEquals(3, rights.size());
        
        _profileAssignmentStorageEP.removeAllowedProfileFromUser(test, profile2.getId(), "/test");
        rights = _rightManager.getUserRights(test, "/test");
        assertEquals(2, rights.size());
        
        _profileAssignmentStorageEP.allowProfileToUser(test, profile2.getId(), "/test2");
        rights = _rightManager.getUserRights(test, "/test");
        assertEquals(2, rights.size());
        rights = _rightManager.getUserRights(test, "/test2");
        assertEquals(1, rights.size());

        _profileAssignmentStorageEP.removeAllowedProfileFromUser(test, profile1.getId(), "/test");
        _profileAssignmentStorageEP.removeAllowedProfileFromUser(test, profile2.getId(), "/test");
        rights = _rightManager.getUserRights(test, "/test");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(test, "/test2");
        assertEquals(1, rights.size());

        _profileAssignmentStorageEP.removeAllowedProfileFromUser(test, profile1.getId(), "/test2");
        _profileAssignmentStorageEP.removeAllowedProfileFromUser(test, profile2.getId(), "/test2");
        rights = _rightManager.getUserRights(test, "/test");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(test, "/test2");
        assertEquals(0, rights.size());

        
        // GROUP
        Group group = _createGroupDirectory().getGroups().iterator().next(); 
        UserIdentity userIdentity = group.getUsers().iterator().next();

        _profileAssignmentStorageEP.allowProfileToGroup(group.getIdentity(), profile1.getId(), "/test");
        rights = _rightManager.getUserRights(userIdentity, "/test");
        assertEquals(2, rights.size());
        
        _profileAssignmentStorageEP.allowProfileToGroup(group.getIdentity(), profile2.getId(), "/test");
        rights = _rightManager.getUserRights(userIdentity, "/test");
        assertEquals(3, rights.size());
        
        _profileAssignmentStorageEP.removeAllowedProfileFromGroup(group.getIdentity(), profile2.getId(), "/test");
        rights = _rightManager.getUserRights(userIdentity, "/test");
        assertEquals(2, rights.size());
        
        _profileAssignmentStorageEP.allowProfileToGroup(group.getIdentity(), profile2.getId(), "/test2");
        rights = _rightManager.getUserRights(userIdentity, "/test");
        assertEquals(2, rights.size());
        rights = _rightManager.getUserRights(userIdentity, "/test2");
        assertEquals(1, rights.size());

        _profileAssignmentStorageEP.removeAllowedProfileFromGroup(group.getIdentity(), profile1.getId(), "/test");
        _profileAssignmentStorageEP.removeAllowedProfileFromGroup(group.getIdentity(), profile2.getId(), "/test");
        rights = _rightManager.getUserRights(userIdentity, "/test");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(userIdentity, "/test2");
        assertEquals(1, rights.size());

        _profileAssignmentStorageEP.removeAllowedProfileFromGroup(group.getIdentity(), profile1.getId(), "/test2");
        _profileAssignmentStorageEP.removeAllowedProfileFromGroup(group.getIdentity(), profile2.getId(), "/test2");
        rights = _rightManager.getUserRights(userIdentity, "/test");
        assertEquals(0, rights.size());
        rights = _rightManager.getUserRights(userIdentity, "/test2");
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
