/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.runtime.test.rights.storage.assignments;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ametys.core.group.GroupIdentity;
import org.ametys.core.right.ProfileAssignmentStorage;
import org.ametys.core.right.ProfileAssignmentStorageExtensionPoint;
import org.ametys.core.user.UserIdentity;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.CocoonWrapper;
import org.ametys.runtime.test.Init;

/**
 * Common test class for testing the {@link ProfileAssignmentStorage} 
 */
public abstract class AbstractProfileAssignmentStorageTestCase extends AbstractRuntimeTestCase
{
    /** The storage for profile assignments */
    protected ProfileAssignmentStorage _profileAssignmentStorage;
    
    @Override
    protected CocoonWrapper _startApplication(String runtimeFile, String configFile, String ldapDataSourceFile, String contextPath) throws Exception
    {
        CocoonWrapper cocoonWrapper = super._startApplication(runtimeFile, configFile, ldapDataSourceFile, contextPath);
        _initProfileAssignmentStorage();
        return cocoonWrapper;
    }
    
    @Override
    protected CocoonWrapper _startApplication(String runtimeFile, String configFile, String contextPath) throws Exception
    {
        CocoonWrapper cocoonWrapper = super._startApplication(runtimeFile, configFile, contextPath);
        _initProfileAssignmentStorage();
        return cocoonWrapper;
    }
    
    /**
     * Returns the id of the extension for {@link ProfileAssignmentStorage} to use.
     * @return the id of the extension to use.
     */
    protected abstract String _getExtensionId();
    
    /**
     * Initialize {@link #_profileAssignmentStorage}
     * @throws Exception if an error occurs
     */
    private void _initProfileAssignmentStorage() throws Exception
    {
        ProfileAssignmentStorageExtensionPoint profileAssignmentStorageEP = (ProfileAssignmentStorageExtensionPoint) Init.getPluginServiceManager().lookup(ProfileAssignmentStorageExtensionPoint.ROLE);
        _profileAssignmentStorage = profileAssignmentStorageEP.getExtension(_getExtensionId());
        assertNotNull("The #_getExtensionId method should point to a ProfileAssignmentStorage extension", _profileAssignmentStorage);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Gets the typed object 1 for tests
     * @return the typed object 1 for tests
     */
    protected abstract Object _getTest1();
    
    /**
     * Gets the typed object 2 for tests
     * @return the typed object 2 for tests
     */
    protected abstract Object _getTest2();
    
    /**
     * Tests the {@link ProfileAssignmentStorage#hasPermission(UserIdentity, Set, Set)} method
     */
    public void testHasPermission()
    {
        Object test1 = _getTest1();
        
        String profile1 = "1";
        String profile2 = "2";
        String profile3 = "3";
        
        Set<String> userProfiles = Stream.of(profile1, profile2).collect(Collectors.toSet()); // Let's assume for this test that user1 has the profiles profile1 and profile2
        
        UserIdentity user1 = new UserIdentity("user1", "foo");
        UserIdentity user2 = new UserIdentity("user2", "bar");
        
        GroupIdentity group1 = new GroupIdentity("group1", "foofoo");
        GroupIdentity group2 = new GroupIdentity("group2", "barbar");
        GroupIdentity group3 = new GroupIdentity("group3", "barbar");
        Set<GroupIdentity> userGroups = Stream.of(group1, group2).collect(Collectors.toSet()); // Let's assume for this test that user1 belongs to group1 and group2
        
        // test initially empty
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // ### USER TEST ###
        // Add another profile as allowed to test1
        _profileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile3);
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile1 as allowed to test1, but on another user
        _profileAssignmentStorage.addAllowedUsers(Collections.singleton(user2), test1, profile1);
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile1 as allowed to test1
        _profileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile1);
        assertTrue(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile1 as denied to test1
        _profileAssignmentStorage.addDeniedUsers(Collections.singleton(user1), test1, profile1);
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile2 as allowed to test1
        _profileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile2);
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // ### GROUPS TEST ###
        // Clean for testing with assignments on groups
        _profileAssignmentStorage.removeProfile(profile1);
        _profileAssignmentStorage.removeProfile(profile2);
        
        // Add another profile as allowed to group1
        _profileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile3);
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile1 as allowed to test1, but on another group
        _profileAssignmentStorage.addAllowedGroups(Collections.singleton(group3), test1, profile1);
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile1 as allowed to test1
        _profileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile1);
        assertTrue(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile1 as denied to test1
        _profileAssignmentStorage.addDeniedGroups(Collections.singleton(group2), test1, profile1);
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile2 as allowed to test1
        _profileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile2);
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // ### ANYCONNECTED TEST ###
        // Clean for testing with assignments on anyconnected
        _profileAssignmentStorage.removeProfile(profile1);
        _profileAssignmentStorage.removeProfile(profile2);
        
        // Add another profile as allowed
        _profileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test1, Collections.singleton(profile3));
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile1 as allowed to test1
        _profileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test1, Collections.singleton(profile1));
        assertTrue(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile1 as denied to test1
        _profileAssignmentStorage.addDeniedProfilesForAnyConnectedUser(test1, Collections.singleton(profile1));
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile2 as allowed to test1
        _profileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test1, Collections.singleton(profile2));
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // ### ANONYMOUS TEST ###
        // Clean for testing with assignments on anonymous
        _profileAssignmentStorage.removeProfile(profile1);
        _profileAssignmentStorage.removeProfile(profile2);
        
        // Add another profile as allowed
        _profileAssignmentStorage.addAllowedProfilesForAnonymous(test1, Collections.singleton(profile3));
        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile1 as allowed to test1
        _profileAssignmentStorage.addAllowedProfilesForAnonymous(test1, Collections.singleton(profile1));
        assertTrue(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
        
        // Add profile1 as denied to test1
        _profileAssignmentStorage.addDeniedProfilesForAnonymous(test1, Collections.singleton(profile1));
//        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles)); // FIXME RUNTIME-2073
        
        // Add profile2 as allowed to test1
        _profileAssignmentStorage.addAllowedProfilesForAnonymous(test1, Collections.singleton(profile2));
//        assertFalse(_profileAssignmentStorage.hasPermission(user1, userGroups, userProfiles)); // FIXME RUNTIME-2073
    }
    
    /**
     * Tests the allowed profiles for any connected user 
     */
    public void testAllowedProfilesForAnyConnectedUser()
    {
        Object test1 = _getTest1();
        Object test2 = _getTest2();
        String profile1 = "1";
        String profile2 = "2";
        String profile3 = "3";
        String profile4 = "4";
        String profile5 = "5";
        
        Set<String> profilesTest1 = Stream.of(profile1, profile2).collect(Collectors.toSet());
        Set<String> profilesTest2 = Stream.of(profile1, profile3, profile4).collect(Collectors.toSet());
        
        // test initially empty
        assertTrue(_profileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test1).isEmpty());
        
        // test add
        _profileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test1, profilesTest1);
        
        _profileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test2, profilesTest2);
        
        assertEquals(profilesTest1, _profileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test1));
        assertEquals(profilesTest2, _profileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test2));
        
        // test remove
        Set<String> profilesToRemove = Stream.of(profile1, profile4, profile5).collect(Collectors.toSet());
        Set<String> remainingProfiles = Collections.singleton(profile3);
        _profileAssignmentStorage.removeAllowedProfilesForAnyConnectedUser(test2, profilesToRemove);
        assertEquals(profilesTest1, _profileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test1));
        assertEquals(remainingProfiles, _profileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test2));
        
        // test add
        Set<String> expectedProfiles = Stream.of(profile1, profile2, profile4).collect(Collectors.toSet());
        _profileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test1, Collections.singleton(profile4));
        assertEquals(expectedProfiles, _profileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test1));
    }
    
    /**
     * Tests the denied profiles for any connected user 
     */
    public void testDeniedProfilesForAnyConnectedUser()
    {
        Object test1 = _getTest1();
        Object test2 = _getTest2();
        String profile1 = "1";
        String profile2 = "2";
        String profile3 = "3";
        String profile4 = "4";
        String profile5 = "5";
        
        Set<String> profilesTest1 = Stream.of(profile1, profile2).collect(Collectors.toSet());
        Set<String> profilesTest2 = Stream.of(profile1, profile3, profile4).collect(Collectors.toSet());
        
        // test initially empty
        assertTrue(_profileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test1).isEmpty());
        
        // test add
        _profileAssignmentStorage.addDeniedProfilesForAnyConnectedUser(test1, profilesTest1);
        
        _profileAssignmentStorage.addDeniedProfilesForAnyConnectedUser(test2, profilesTest2);
        
        assertEquals(profilesTest1, _profileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test1));
        assertEquals(profilesTest2, _profileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test2));
        
        // test remove
        Set<String> profilesToRemove = Stream.of(profile1, profile4, profile5).collect(Collectors.toSet());
        Set<String> remainingProfiles = Collections.singleton(profile3);
        _profileAssignmentStorage.removeDeniedProfilesForAnyConnectedUser(test2, profilesToRemove);
        assertEquals(profilesTest1, _profileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test1));
        assertEquals(remainingProfiles, _profileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test2));
        
        // test add
        Set<String> expectedProfiles = Stream.of(profile1, profile2, profile4).collect(Collectors.toSet());
        _profileAssignmentStorage.addDeniedProfilesForAnyConnectedUser(test1, Collections.singleton(profile4));
        assertEquals(expectedProfiles, _profileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test1));
    }
    
    /**
     * Tests the allowed profiles for an anonymous user
     */
    public void testAllowedProfilesForAnonymous()
    {
        Object test1 = _getTest1();
        Object test2 = _getTest2();
        String profile1 = "1";
        String profile2 = "2";
        String profile3 = "3";
        String profile4 = "4";
        String profile5 = "5";
        
        Set<String> profilesTest1 = Stream.of(profile1, profile2).collect(Collectors.toSet());
        Set<String> profilesTest2 = Stream.of(profile1, profile3, profile4).collect(Collectors.toSet());
        
        // test initially empty
        assertTrue(_profileAssignmentStorage.getAllowedProfilesForAnonymous(test1).isEmpty());
        
        // test add
        _profileAssignmentStorage.addAllowedProfilesForAnonymous(test1, profilesTest1);
        
        _profileAssignmentStorage.addAllowedProfilesForAnonymous(test2, profilesTest2);
        
        assertEquals(profilesTest1, _profileAssignmentStorage.getAllowedProfilesForAnonymous(test1));
        assertEquals(profilesTest2, _profileAssignmentStorage.getAllowedProfilesForAnonymous(test2));
        
        // test remove
        Set<String> profilesToRemove = Stream.of(profile1, profile4, profile5).collect(Collectors.toSet());
        Set<String> remainingProfiles = Collections.singleton(profile3);
        _profileAssignmentStorage.removeAllowedProfilesForAnonymous(test2, profilesToRemove);
        assertEquals(profilesTest1, _profileAssignmentStorage.getAllowedProfilesForAnonymous(test1));
        assertEquals(remainingProfiles, _profileAssignmentStorage.getAllowedProfilesForAnonymous(test2));
        
        // test add
        Set<String> expectedProfiles = Stream.of(profile1, profile2, profile4).collect(Collectors.toSet());
        _profileAssignmentStorage.addAllowedProfilesForAnonymous(test1, Collections.singleton(profile4));
        assertEquals(expectedProfiles, _profileAssignmentStorage.getAllowedProfilesForAnonymous(test1));
    }
    
    /**
     * Tests the denied profiles for an anonymous user
     */
    public void testDeniedProfilesForAnonymous()
    {
        Object test1 = _getTest1();
        Object test2 = _getTest2();
        String profile1 = "1";
        String profile2 = "2";
        String profile3 = "3";
        String profile4 = "4";
        String profile5 = "5";
        
        Set<String> profilesTest1 = Stream.of(profile1, profile2).collect(Collectors.toSet());
        Set<String> profilesTest2 = Stream.of(profile1, profile3, profile4).collect(Collectors.toSet());
        
        // test initially empty
        assertTrue(_profileAssignmentStorage.getDeniedProfilesForAnonymous(test1).isEmpty());
        
        // test add
        _profileAssignmentStorage.addDeniedProfilesForAnonymous(test1, profilesTest1);
        
        _profileAssignmentStorage.addDeniedProfilesForAnonymous(test2, profilesTest2);
        
        assertEquals(profilesTest1, _profileAssignmentStorage.getDeniedProfilesForAnonymous(test1));
        assertEquals(profilesTest2, _profileAssignmentStorage.getDeniedProfilesForAnonymous(test2));
        
        // test remove
        Set<String> profilesToRemove = Stream.of(profile1, profile4, profile5).collect(Collectors.toSet());
        Set<String> remainingProfiles = Collections.singleton(profile3);
        _profileAssignmentStorage.removeDeniedProfilesForAnonymous(test2, profilesToRemove);
        assertEquals(profilesTest1, _profileAssignmentStorage.getDeniedProfilesForAnonymous(test1));
        assertEquals(remainingProfiles, _profileAssignmentStorage.getDeniedProfilesForAnonymous(test2));
        
        // test add
        Set<String> expectedProfiles = Stream.of(profile1, profile2, profile4).collect(Collectors.toSet());
        _profileAssignmentStorage.addDeniedProfilesForAnonymous(test1, Collections.singleton(profile4));
        assertEquals(expectedProfiles, _profileAssignmentStorage.getDeniedProfilesForAnonymous(test1));
    }
    
    /**
     * Tests the storage of allowed users
     */
    public void testAllowedUsers()
    {
        Object test1 = _getTest1();
        Object test2 = _getTest2();
        String profile1 = "1";
        String profile2 = "2";
        
        Set<String> bothProfiles = Stream.of(profile1, profile2).collect(Collectors.toSet());
        
        UserIdentity user1 = new UserIdentity("user1", "foo");
        UserIdentity user2 = new UserIdentity("user2", "bar");
        UserIdentity user3 = new UserIdentity("user3", "bar");
        
        Set<UserIdentity> bothUsers = Stream.of(user1, user2).collect(Collectors.toSet());
        Set<UserIdentity> threeUsers = Stream.of(user1, user2, user3).collect(Collectors.toSet());
        
        Map<UserIdentity, Set<String>> expectedUsers = new HashMap<>();
        
        // test initially empty
        assertTrue(_profileAssignmentStorage.getAllowedProfilesForUsers(test1).isEmpty());
        assertTrue(_profileAssignmentStorage.getAllowedUsers(test1, profile1).isEmpty());
        
        // test add
        _profileAssignmentStorage.addAllowedUsers(bothUsers, test1, profile1);
        _profileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile2);
        expectedUsers.put(user1, bothProfiles);
        expectedUsers.put(user2, Collections.singleton(profile1));
        assertEquals(expectedUsers, _profileAssignmentStorage.getAllowedProfilesForUsers(test1));
        assertEquals(Collections.EMPTY_MAP, _profileAssignmentStorage.getAllowedProfilesForUsers(test2));
        assertEquals(bothUsers, _profileAssignmentStorage.getAllowedUsers(test1, profile1));
        assertEquals(Collections.singleton(user1), _profileAssignmentStorage.getAllowedUsers(test1, profile2));
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getAllowedUsers(test2, profile1));
        
        // test removing non-existing associations
        _profileAssignmentStorage.removeAllowedUsers(Collections.singleton(user3), test1, profile1);
        assertEquals(bothUsers, _profileAssignmentStorage.getAllowedUsers(test1, profile1));
        _profileAssignmentStorage.removeAllowedUsers(Collections.singleton(user1), test2, profile1);
        assertEquals(Collections.EMPTY_MAP, _profileAssignmentStorage.getAllowedProfilesForUsers(test2));
        _profileAssignmentStorage.removeAllowedUsers(Collections.singleton(user2), test1, profile2);
        assertEquals(Collections.singleton(user1), _profileAssignmentStorage.getAllowedUsers(test1, profile2));
        
        // test remove profile
        _profileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test2, profile1); //for testing the deletion is not made on another object
        _profileAssignmentStorage.removeAllowedUsers(Collections.singleton(user1), test1, profile1);
        assertEquals(Collections.singleton(user2), _profileAssignmentStorage.getAllowedUsers(test1, profile1));
        assertEquals(Collections.singleton(user1), _profileAssignmentStorage.getAllowedUsers(test1, profile2)); //but still have the profile2
        assertEquals(Collections.singleton(user1), _profileAssignmentStorage.getAllowedUsers(test2, profile1)); //but still it on another object
        _profileAssignmentStorage.removeAllowedUsers(Collections.singleton(user2), test1, profile1);
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getAllowedUsers(test1, profile1));
        
        // test remove all profiles
        _profileAssignmentStorage.addAllowedUsers(threeUsers, test1, profile1); // populate
        _profileAssignmentStorage.addAllowedUsers(bothUsers, test1, profile2); // populate
        _profileAssignmentStorage.removeAllowedUsers(bothUsers, test1); // this is what we want to test
        expectedUsers.clear();
        expectedUsers.put(user3, Collections.singleton(profile1));
        assertEquals(expectedUsers, _profileAssignmentStorage.getAllowedProfilesForUsers(test1));
        expectedUsers.clear();
        expectedUsers.put(user1, Collections.singleton(profile1));
        assertEquals(expectedUsers, _profileAssignmentStorage.getAllowedProfilesForUsers(test2));
    }
    
    /**
     * Tests the storage of denied users
     */
    public void testDeniedUsers()
    {
        Object test1 = _getTest1();
        Object test2 = _getTest2();
        String profile1 = "1";
        String profile2 = "2";
        
        Set<String> bothProfiles = Stream.of(profile1, profile2).collect(Collectors.toSet());
        
        UserIdentity user1 = new UserIdentity("user1", "foo");
        UserIdentity user2 = new UserIdentity("user2", "bar");
        UserIdentity user3 = new UserIdentity("user3", "bar");
        
        Set<UserIdentity> bothUsers = Stream.of(user1, user2).collect(Collectors.toSet());
        Set<UserIdentity> threeUsers = Stream.of(user1, user2, user3).collect(Collectors.toSet());
        
        Map<UserIdentity, Set<String>> expectedUsers = new HashMap<>();
        
        // test initially empty
        assertTrue(_profileAssignmentStorage.getDeniedProfilesForUsers(test1).isEmpty());
        assertTrue(_profileAssignmentStorage.getDeniedUsers(test1, profile1).isEmpty());
        
        // test add
        _profileAssignmentStorage.addDeniedUsers(bothUsers, test1, profile1);
        _profileAssignmentStorage.addDeniedUsers(Collections.singleton(user1), test1, profile2);
        expectedUsers.put(user1, bothProfiles);
        expectedUsers.put(user2, Collections.singleton(profile1));
        assertEquals(expectedUsers, _profileAssignmentStorage.getDeniedProfilesForUsers(test1));
        assertEquals(Collections.EMPTY_MAP, _profileAssignmentStorage.getDeniedProfilesForUsers(test2));
        assertEquals(bothUsers, _profileAssignmentStorage.getDeniedUsers(test1, profile1));
        assertEquals(Collections.singleton(user1), _profileAssignmentStorage.getDeniedUsers(test1, profile2));
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getDeniedUsers(test2, profile1));
        
        // test removing non-existing associations
        _profileAssignmentStorage.removeDeniedUsers(Collections.singleton(user3), test1, profile1);
        assertEquals(bothUsers, _profileAssignmentStorage.getDeniedUsers(test1, profile1));
        _profileAssignmentStorage.removeDeniedUsers(Collections.singleton(user1), test2, profile1);
        assertEquals(Collections.EMPTY_MAP, _profileAssignmentStorage.getDeniedProfilesForUsers(test2));
        _profileAssignmentStorage.removeDeniedUsers(Collections.singleton(user2), test1, profile2);
        assertEquals(Collections.singleton(user1), _profileAssignmentStorage.getDeniedUsers(test1, profile2));
        
        // test remove profile
        _profileAssignmentStorage.addDeniedUsers(Collections.singleton(user1), test2, profile1); //for testing the deletion is not made on another object
        _profileAssignmentStorage.removeDeniedUsers(Collections.singleton(user1), test1, profile1);
        assertEquals(Collections.singleton(user2), _profileAssignmentStorage.getDeniedUsers(test1, profile1));
        assertEquals(Collections.singleton(user1), _profileAssignmentStorage.getDeniedUsers(test1, profile2)); //but still have the profile2
        assertEquals(Collections.singleton(user1), _profileAssignmentStorage.getDeniedUsers(test2, profile1)); //but still it on another object
        _profileAssignmentStorage.removeDeniedUsers(Collections.singleton(user2), test1, profile1);
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getDeniedUsers(test1, profile1));
        
        // test remove all profiles
        _profileAssignmentStorage.addDeniedUsers(threeUsers, test1, profile1); // populate
        _profileAssignmentStorage.addDeniedUsers(bothUsers, test1, profile2); // populate
        _profileAssignmentStorage.removeDeniedUsers(bothUsers, test1); // this is what we want to test
        expectedUsers.clear();
        expectedUsers.put(user3, Collections.singleton(profile1));
        assertEquals(expectedUsers, _profileAssignmentStorage.getDeniedProfilesForUsers(test1));
        expectedUsers.clear();
        expectedUsers.put(user1, Collections.singleton(profile1));
        assertEquals(expectedUsers, _profileAssignmentStorage.getDeniedProfilesForUsers(test2));
    }
    
    /**
     * Tests the storage of allowed groups
     */
    public void testAllowedGroups()
    {
        Object test1 = _getTest1();
        Object test2 = _getTest2();
        String profile1 = "1";
        String profile2 = "2";
        
        Set<String> bothProfiles = Stream.of(profile1, profile2).collect(Collectors.toSet());
        
        GroupIdentity group1 = new GroupIdentity("group1", "foo");
        GroupIdentity group2 = new GroupIdentity("group2", "bar");
        GroupIdentity group3 = new GroupIdentity("group3", "bar");
        
        Set<GroupIdentity> bothGroups = Stream.of(group1, group2).collect(Collectors.toSet());
        Set<GroupIdentity> threeGroups = Stream.of(group1, group2, group3).collect(Collectors.toSet());
        
        // test initially empty
        assertTrue(_profileAssignmentStorage.getAllowedProfilesForGroups(test1).isEmpty());
        assertTrue(_profileAssignmentStorage.getAllowedGroups(test1, profile1).isEmpty());
        
        // test add
        _profileAssignmentStorage.addAllowedGroups(bothGroups, test1, profile1);
        _profileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile2);
        Map<GroupIdentity, Set<String>> expectedGroups = new HashMap<>();
        expectedGroups.put(group1, bothProfiles);
        expectedGroups.put(group2, Collections.singleton(profile1));
        assertEquals(expectedGroups, _profileAssignmentStorage.getAllowedProfilesForGroups(test1));
        assertEquals(Collections.EMPTY_MAP, _profileAssignmentStorage.getAllowedProfilesForGroups(test2));
        assertEquals(bothGroups, _profileAssignmentStorage.getAllowedGroups(test1, profile1));
        assertEquals(Collections.singleton(group1), _profileAssignmentStorage.getAllowedGroups(test1, profile2));
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getAllowedGroups(test2, profile1));
        
        // test removing non-existing associations
        _profileAssignmentStorage.removeAllowedGroups(Collections.singleton(group3), test1, profile1);
        assertEquals(bothGroups, _profileAssignmentStorage.getAllowedGroups(test1, profile1));
        _profileAssignmentStorage.removeAllowedGroups(Collections.singleton(group1), test2, profile1);
        assertEquals(Collections.EMPTY_MAP, _profileAssignmentStorage.getAllowedProfilesForGroups(test2));
        _profileAssignmentStorage.removeAllowedGroups(Collections.singleton(group2), test1, profile2);
        assertEquals(Collections.singleton(group1), _profileAssignmentStorage.getAllowedGroups(test1, profile2));
        
        // test remove profile
        _profileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test2, profile1); //for testing the deletion is not made on another object
        _profileAssignmentStorage.removeAllowedGroups(Collections.singleton(group1), test1, profile1);
        assertEquals(Collections.singleton(group2), _profileAssignmentStorage.getAllowedGroups(test1, profile1));
        assertEquals(Collections.singleton(group1), _profileAssignmentStorage.getAllowedGroups(test1, profile2)); //but still have the profile2
        assertEquals(Collections.singleton(group1), _profileAssignmentStorage.getAllowedGroups(test2, profile1)); //but still it on another object
        _profileAssignmentStorage.removeAllowedGroups(Collections.singleton(group2), test1, profile1);
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getAllowedGroups(test1, profile1));
        
        // test remove all profiles
        _profileAssignmentStorage.addAllowedGroups(threeGroups, test1, profile1); // populate
        _profileAssignmentStorage.addAllowedGroups(bothGroups, test1, profile2); // populate
        _profileAssignmentStorage.removeAllowedGroups(bothGroups, test1); // this is what we want to test
        expectedGroups.clear();
        expectedGroups.put(group3, Collections.singleton(profile1));
        assertEquals(expectedGroups, _profileAssignmentStorage.getAllowedProfilesForGroups(test1));
        expectedGroups.clear();
        expectedGroups.put(group1, Collections.singleton(profile1));
        assertEquals(expectedGroups, _profileAssignmentStorage.getAllowedProfilesForGroups(test2));
    }
    
    /**
     * Tests the storage of denied groups
     */
    public void testDeniedGroups()
    {
        Object test1 = _getTest1();
        Object test2 = _getTest2();
        String profile1 = "1";
        String profile2 = "2";
        
        Set<String> bothProfiles = Stream.of(profile1, profile2).collect(Collectors.toSet());
        
        GroupIdentity group1 = new GroupIdentity("group1", "foo");
        GroupIdentity group2 = new GroupIdentity("group2", "bar");
        GroupIdentity group3 = new GroupIdentity("group3", "bar");
        
        Set<GroupIdentity> bothGroups = Stream.of(group1, group2).collect(Collectors.toSet());
        Set<GroupIdentity> threeGroups = Stream.of(group1, group2, group3).collect(Collectors.toSet());
        
        // test initially empty
        assertTrue(_profileAssignmentStorage.getDeniedProfilesForGroups(test1).isEmpty());
        assertTrue(_profileAssignmentStorage.getDeniedGroups(test1, profile1).isEmpty());
        
        // test add
        _profileAssignmentStorage.addDeniedGroups(bothGroups, test1, profile1);
        _profileAssignmentStorage.addDeniedGroups(Collections.singleton(group1), test1, profile2);
        Map<GroupIdentity, Set<String>> expectedGroups = new HashMap<>();
        expectedGroups.put(group1, bothProfiles);
        expectedGroups.put(group2, Collections.singleton(profile1));
        assertEquals(expectedGroups, _profileAssignmentStorage.getDeniedProfilesForGroups(test1));
        assertEquals(Collections.EMPTY_MAP, _profileAssignmentStorage.getDeniedProfilesForGroups(test2));
        assertEquals(bothGroups, _profileAssignmentStorage.getDeniedGroups(test1, profile1));
        assertEquals(Collections.singleton(group1), _profileAssignmentStorage.getDeniedGroups(test1, profile2));
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getDeniedGroups(test2, profile1));
        
        // test removing non-existing associations
        _profileAssignmentStorage.removeDeniedGroups(Collections.singleton(group3), test1, profile1);
        assertEquals(bothGroups, _profileAssignmentStorage.getDeniedGroups(test1, profile1));
        _profileAssignmentStorage.removeDeniedGroups(Collections.singleton(group1), test2, profile1);
        assertEquals(Collections.EMPTY_MAP, _profileAssignmentStorage.getDeniedProfilesForGroups(test2));
        _profileAssignmentStorage.removeDeniedGroups(Collections.singleton(group2), test1, profile2);
        assertEquals(Collections.singleton(group1), _profileAssignmentStorage.getDeniedGroups(test1, profile2));
        
        // test remove profile
        _profileAssignmentStorage.addDeniedGroups(Collections.singleton(group1), test2, profile1); //for testing the deletion is not made on another object
        _profileAssignmentStorage.removeDeniedGroups(Collections.singleton(group1), test1, profile1);
        assertEquals(Collections.singleton(group2), _profileAssignmentStorage.getDeniedGroups(test1, profile1));
        assertEquals(Collections.singleton(group1), _profileAssignmentStorage.getDeniedGroups(test1, profile2)); //but still have the profile2
        assertEquals(Collections.singleton(group1), _profileAssignmentStorage.getDeniedGroups(test2, profile1)); //but still it on another object
        _profileAssignmentStorage.removeDeniedGroups(Collections.singleton(group2), test1, profile1);
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getDeniedGroups(test1, profile1));
        
        // test remove all profiles
        _profileAssignmentStorage.addDeniedGroups(threeGroups, test1, profile1); // populate
        _profileAssignmentStorage.addDeniedGroups(bothGroups, test1, profile2); // populate
        _profileAssignmentStorage.removeDeniedGroups(bothGroups, test1); // this is what we want to test
        expectedGroups.clear();
        expectedGroups.put(group3, Collections.singleton(profile1));
        assertEquals(expectedGroups, _profileAssignmentStorage.getDeniedProfilesForGroups(test1));
        expectedGroups.clear();
        expectedGroups.put(group1, Collections.singleton(profile1));
        assertEquals(expectedGroups, _profileAssignmentStorage.getDeniedProfilesForGroups(test2));
    }
    
    /**
     * Tests the removing of a profile
     */
    public void testRemoveProfile()
    {
        Object test1 = _getTest1();
        String profile1 = "1";
        String profile2 = "2";
        Set<String> bothProfiles = Stream.of(profile1, profile2).collect(Collectors.toSet());
        
        UserIdentity user1 = new UserIdentity("user1", "foo");
        
        GroupIdentity group1 = new GroupIdentity("group1", "foo");
        
        // populate
        _profileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile1);
        _profileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile2);
        
        _profileAssignmentStorage.addDeniedUsers(Collections.singleton(user1), test1, profile1);
        _profileAssignmentStorage.addDeniedUsers(Collections.singleton(user1), test1, profile2);
        
        _profileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile1);
        _profileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile2);
        
        _profileAssignmentStorage.addDeniedGroups(Collections.singleton(group1), test1, profile1);
        _profileAssignmentStorage.addDeniedGroups(Collections.singleton(group1), test1, profile2);
        
        _profileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test1, bothProfiles);
        _profileAssignmentStorage.addDeniedProfilesForAnyConnectedUser(test1, bothProfiles);
        
        _profileAssignmentStorage.addAllowedProfilesForAnonymous(test1, bothProfiles);
        _profileAssignmentStorage.addDeniedProfilesForAnonymous(test1, bothProfiles);
        
        // test removing
        _profileAssignmentStorage.removeProfile(profile1);
        
        Map<UserIdentity, Set<String>> expectedUsers = new HashMap<>();
        Map<GroupIdentity, Set<String>> expectedGroups = new HashMap<>();
        
        expectedUsers.put(user1, Collections.singleton(profile2));
        assertEquals(expectedUsers, _profileAssignmentStorage.getAllowedProfilesForUsers(test1));
        assertEquals(expectedUsers, _profileAssignmentStorage.getDeniedProfilesForUsers(test1));
        
        expectedGroups.put(group1, Collections.singleton(profile2));
        assertEquals(expectedGroups, _profileAssignmentStorage.getAllowedProfilesForGroups(test1));
        assertEquals(expectedGroups, _profileAssignmentStorage.getDeniedProfilesForGroups(test1));
        
        assertEquals(Collections.singleton(profile2), _profileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test1));
        assertEquals(Collections.singleton(profile2), _profileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test1));
        assertEquals(Collections.singleton(profile2), _profileAssignmentStorage.getAllowedProfilesForAnonymous(test1));
        assertEquals(Collections.singleton(profile2), _profileAssignmentStorage.getDeniedProfilesForAnonymous(test1));
    }
    
    /**
     * Tests the removing of a user
     */
    public void testRemoveUser()
    {
        Object test1 = _getTest1();
        Object test2 = _getTest2();
        String profile1 = "1";
        String profile2 = "2";
        
        UserIdentity user1 = new UserIdentity("user1", "foo");
        UserIdentity user2 = new UserIdentity("user2", "bar");
        
        // populate
        _profileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile1);
        _profileAssignmentStorage.addAllowedUsers(Collections.singleton(user2), test1, profile2);
        _profileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test2, profile1);
        
        _profileAssignmentStorage.addDeniedUsers(Collections.singleton(user2), test1, profile1);
        _profileAssignmentStorage.addDeniedUsers(Collections.singleton(user1), test1, profile2);
        _profileAssignmentStorage.addDeniedUsers(Collections.singleton(user2), test2, profile1);
        
        // test removing
        _profileAssignmentStorage.removeUser(user1);
        
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getAllowedUsers(test1, profile1));
        assertEquals(Collections.singleton(user2), _profileAssignmentStorage.getAllowedUsers(test1, profile2));
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getAllowedUsers(test2, profile1));
        
        assertEquals(Collections.singleton(user2), _profileAssignmentStorage.getDeniedUsers(test1, profile1));
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getDeniedUsers(test1, profile2));
        assertEquals(Collections.singleton(user2), _profileAssignmentStorage.getDeniedUsers(test2, profile1));
    }
    
    /**
     * Tests the removing of a group
     */
    public void testRemoveGroup()
    {
        Object test1 = _getTest1();
        Object test2 = _getTest2();
        String profile1 = "1";
        String profile2 = "2";
        
        GroupIdentity group1 = new GroupIdentity("group1", "foo");
        GroupIdentity group2 = new GroupIdentity("group2", "bar");
        
        // populate
        _profileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile1);
        _profileAssignmentStorage.addAllowedGroups(Collections.singleton(group2), test1, profile2);
        _profileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test2, profile1);
        
        _profileAssignmentStorage.addDeniedGroups(Collections.singleton(group2), test1, profile1);
        _profileAssignmentStorage.addDeniedGroups(Collections.singleton(group1), test1, profile2);
        _profileAssignmentStorage.addDeniedGroups(Collections.singleton(group2), test2, profile1);
        
        // test removing
        _profileAssignmentStorage.removeGroup(group1);
        
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getAllowedGroups(test1, profile1));
        assertEquals(Collections.singleton(group2), _profileAssignmentStorage.getAllowedGroups(test1, profile2));
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getAllowedGroups(test2, profile1));
        
        assertEquals(Collections.singleton(group2), _profileAssignmentStorage.getDeniedGroups(test1, profile1));
        assertEquals(Collections.EMPTY_SET, _profileAssignmentStorage.getDeniedGroups(test1, profile2));
        assertEquals(Collections.singleton(group2), _profileAssignmentStorage.getDeniedGroups(test2, profile1));
    }
}
