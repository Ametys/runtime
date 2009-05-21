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
package org.ametys.runtime.test.rights.profile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.group.Group;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.plugins.core.right.profile.DefaultProfileBasedRightsManager;
import org.ametys.runtime.plugins.core.right.profile.HierarchicalProfileBasedRightsManager;
import org.ametys.runtime.plugins.core.right.profile.Profile;
import org.ametys.runtime.plugins.core.right.profile.ProfileBasedRightsManager;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.test.AbstractJDBCTestCase;
import org.ametys.runtime.test.Init;

/**
 * Tests the ProfileBasedRightsManager
 */
public class DefaultProfileBasedRightsManagerTestCase extends AbstractJDBCTestCase
{
    private RightsManager _rightsManager;
    
    /**
     * Reset the db
     * @param runtimeFilename The file name in runtimes env dir
     * @param configFileName The file name in config env dir
     * @throws Exception if an error occurs
     */
    protected void _resetDB(String runtimeFilename, String configFileName) throws Exception
    {
        _configureRuntime("test/environments/runtimes/" + runtimeFilename);
        Config.setFilename("test/environments/configs/" + configFileName);
        super.setUp();
        
        _startCocoon("test/environments/webapp1");

        List<File> scripts = new ArrayList<File>();
        scripts.add(new File("main/plugin-core/scripts/mysql/profile_rights.sql"));
        _setDatabase(scripts);

        _rightsManager = (RightsManager) Init.getPluginServiceManager().lookup(RightsManager.ROLE);
    }
    
    @Override
    protected void setUp() throws Exception
    {
        _resetDB("runtime4.xml", "config1.xml");
    }
    
    /**
     * Check the type of the rights manager impl 
     * @throws Exception if an error occurs
     */
    public void testType() throws Exception
    {
        assertTrue(_rightsManager instanceof DefaultProfileBasedRightsManager);
        assertTrue(_rightsManager instanceof ProfileBasedRightsManager);
        assertFalse(_rightsManager instanceof HierarchicalProfileBasedRightsManager);
    }
    
    /**
     * Test an empty db
     * @throws Exception if an error occurs
     */
    public void testEmpty() throws Exception
    {
        ProfileBasedRightsManager profileRightsManager = (ProfileBasedRightsManager) _rightsManager;
        
        Set<String> users = profileRightsManager.getGrantedUsers("Runtime_Rights_User_Handle", null);
        assertEquals(0, users.size());
        
        Set<String> rights = profileRightsManager.getUserRights("foo", null);
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
        
        Set<String> users;
        
        // Fill DB
        List<File> fillscripts = new ArrayList<File>();
        fillscripts.add(new File("test/environments/scripts/fillProfileRights.sql"));
        _setDatabase(fillscripts);

        users = profileRightsManager.getGrantedUsers("right1", null);
        assertEquals(1, users.size());
        assertTrue(users.contains("test"));
        users = profileRightsManager.getGrantedUsers("right1", "/test");
        assertEquals(1, users.size());
        assertTrue(users.contains("test"));
        users = profileRightsManager.getGrantedUsers("right1", "/test/test");
        assertEquals(0, users.size());
        users = profileRightsManager.getGrantedUsers("right1", "/test2");
        assertEquals(0, users.size());
        users = profileRightsManager.getGrantedUsers("right1", "/test2/test2");
        assertEquals(1, users.size());
        assertTrue(users.contains("test"));
        users = profileRightsManager.getGrantedUsers("right1", "/test2/test2/test2");
        assertEquals(0, users.size());
        users = profileRightsManager.getGrantedUsers("right1", "/test3");
        assertEquals(1, users.size());
        assertTrue(users.contains("test"));
        users = profileRightsManager.getGrantedUsers("right2", null);
        assertEquals(1, users.size());
        assertTrue(users.contains("test"));
        users = profileRightsManager.getGrantedUsers("right3", null);
        assertEquals(2, users.size());
        assertTrue(users.contains("test"));
        assertTrue(users.contains("test2"));
        users = profileRightsManager.getGrantedUsers("right3", "/test");
        assertEquals(0, users.size());
        users = profileRightsManager.getGrantedUsers("right3", "/test2/test2");
        assertEquals(1, users.size());
        assertTrue(users.contains("test2"));
        users = profileRightsManager.getGrantedUsers("right3", "/test3");
        assertEquals(1, users.size());
        assertTrue(users.contains("test"));
        
        Set<String> rights;
        rights = profileRightsManager.getUserRights("test", null);
        assertEquals(3, rights.size());
        rights = profileRightsManager.getUserRights("test", "/test");
        assertEquals(2, rights.size());
        rights = profileRightsManager.getUserRights("test", "/test/test");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights("test", "/test2");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights("test", "/test2/test2");
        assertEquals(2, rights.size());
        rights = profileRightsManager.getUserRights("test", "/test3");
        assertEquals(3, rights.size());
        rights = profileRightsManager.getUserRights("test", "/test3/test3");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights("test2", null);
        assertEquals(1, rights.size());
        rights = profileRightsManager.getUserRights("test2", "/test");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights("test2", "/test/test");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights("test2", "/test2");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights("test2", "/test2/test2");
        assertEquals(1, rights.size());
        rights = profileRightsManager.getUserRights("test2", "/test3");
        assertEquals(0, rights.size());
        rights = profileRightsManager.getUserRights("test2", "/test3/test3");
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

        List<File> fillscripts = new ArrayList<File>();
        fillscripts.add(new File("test/environments/scripts/fillProfileRights.sql"));
        _setDatabase(fillscripts);
        
        Set<String> rights;
        
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
        profileRightsManager.addUserRight("test", "/test", profile1.getId());
        rights = profileRightsManager.getUserRights("test", null);
        assertEquals(2, rights.size());
        
        profileRightsManager.addUserRight("test", "/test", profile2.getId());
        rights = profileRightsManager.getUserRights("test", null);
        assertEquals(3, rights.size());
        
        profileRightsManager.removeUserProfile("test", profile2.getId(), "/test");
        rights = profileRightsManager.getUserRights("test", null);
        assertEquals(2, rights.size());
        
        profileRightsManager.addUserRight("test", "/test2", profile2.getId());
        rights = profileRightsManager.getUserRights("test", null);
        assertEquals(3, rights.size());

        profileRightsManager.removeUserProfiles("test", "/test");
        rights = profileRightsManager.getUserRights("test", null);
        assertEquals(1, rights.size());

        profileRightsManager.removeUserProfiles("test", null);
        rights = profileRightsManager.getUserRights("test", null);
        assertEquals(0, rights.size());

        
        // GROUP
        GroupsManager groupsManager = (GroupsManager) Init.getPluginServiceManager().lookup(GroupsManager.ROLE);
        Group group = groupsManager.getGroups().iterator().next();
        String userLogin = group.getUsers().iterator().next();

        profileRightsManager.addGroupRight(group.getId(), "/test", profile1.getId());
        rights = profileRightsManager.getUserRights(userLogin, null);
        assertEquals(2, rights.size());
        
        profileRightsManager.addGroupRight(group.getId(), "/test", profile2.getId());
        rights = profileRightsManager.getUserRights(userLogin, null);
        assertEquals(3, rights.size());
        
        profileRightsManager.removeGroupProfile(group.getId(), profile2.getId(), "/test");
        rights = profileRightsManager.getUserRights(userLogin, null);
        assertEquals(2, rights.size());
        
        profileRightsManager.addGroupRight(group.getId(), "/test2", profile2.getId());
        rights = profileRightsManager.getUserRights(userLogin, null);
        assertEquals(3, rights.size());

        profileRightsManager.removeGroupProfiles(group.getId(), "/test");
        rights = profileRightsManager.getUserRights(userLogin, null);
        assertEquals(1, rights.size());

        profileRightsManager.removeGroupProfiles(group.getId(), null);
        rights = profileRightsManager.getUserRights(userLogin, null);
        assertEquals(0, rights.size());
    }
}
