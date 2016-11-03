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
import org.ametys.core.right.ModifiableProfileAssignmentStorage;
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
        
        if (_profileAssignmentStorage instanceof ModifiableProfileAssignmentStorage)
        {
            ModifiableProfileAssignmentStorage mProfileAssignmentStorage = (ModifiableProfileAssignmentStorage) _profileAssignmentStorage;
         
            // ### USER TEST ###
            // Add another profile as allowed to test1
            mProfileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile3);
            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile1 as allowed to test1, but on another user
            mProfileAssignmentStorage.addAllowedUsers(Collections.singleton(user2), test1, profile1);
            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile1 as allowed to test1
            mProfileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile1);
            assertTrue(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile1 as denied to test1
            mProfileAssignmentStorage.addDeniedUsers(Collections.singleton(user1), test1, profile1);
            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile2 as allowed to test1
            mProfileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile2);
            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // ### GROUPS TEST ###
            // Clean for testing with assignments on groups
            mProfileAssignmentStorage.removeProfile(profile1);
            mProfileAssignmentStorage.removeProfile(profile2);
            
            // Add another profile as allowed to group1
            mProfileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile3);
            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile1 as allowed to test1, but on another group
            mProfileAssignmentStorage.addAllowedGroups(Collections.singleton(group3), test1, profile1);
            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile1 as allowed to test1
            mProfileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile1);
            assertTrue(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile1 as denied to test1
            mProfileAssignmentStorage.addDeniedGroups(Collections.singleton(group2), test1, profile1);
            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile2 as allowed to test1
            mProfileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile2);
            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // ### ANYCONNECTED TEST ###
            // Clean for testing with assignments on anyconnected
            mProfileAssignmentStorage.removeProfile(profile1);
            mProfileAssignmentStorage.removeProfile(profile2);
            
            // Add another profile as allowed
            mProfileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test1, Collections.singleton(profile3));
            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile1 as allowed to test1
            mProfileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test1, Collections.singleton(profile1));
            assertTrue(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile1 as denied to test1
            mProfileAssignmentStorage.addDeniedProfilesForAnyConnectedUser(test1, Collections.singleton(profile1));
            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile2 as allowed to test1
            mProfileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test1, Collections.singleton(profile2));
            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // ### ANONYMOUS TEST ###
            // Clean for testing with assignments on anonymous
            mProfileAssignmentStorage.removeProfile(profile1);
            mProfileAssignmentStorage.removeProfile(profile2);
            
            // Add another profile as allowed
            mProfileAssignmentStorage.addAllowedProfilesForAnonymous(test1, Collections.singleton(profile3));
            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile1 as allowed to test1
            mProfileAssignmentStorage.addAllowedProfilesForAnonymous(test1, Collections.singleton(profile1));
            assertTrue(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles));
            
            // Add profile1 as denied to test1
            mProfileAssignmentStorage.addDeniedProfilesForAnonymous(test1, Collections.singleton(profile1));
//            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles)); // FIXME RUNTIME-2073
            
            // Add profile2 as allowed to test1
            mProfileAssignmentStorage.addAllowedProfilesForAnonymous(test1, Collections.singleton(profile2));
//            assertFalse(mProfileAssignmentStorage.hasPermission(user1, userGroups, userProfiles)); // FIXME RUNTIME-2073
        }
        
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
        
        if (_profileAssignmentStorage instanceof ModifiableProfileAssignmentStorage)
        {
            ModifiableProfileAssignmentStorage mProfileAssignmentStorage = (ModifiableProfileAssignmentStorage) _profileAssignmentStorage;
            
            // test add
            mProfileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test1, profilesTest1);
            
            mProfileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test2, profilesTest2);
            
            assertEquals(profilesTest1, mProfileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test1));
            assertEquals(profilesTest2, mProfileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test2));
            
            // test remove
            Set<String> profilesToRemove = Stream.of(profile1, profile4, profile5).collect(Collectors.toSet());
            Set<String> remainingProfiles = Collections.singleton(profile3);
            mProfileAssignmentStorage.removeAllowedProfilesForAnyConnectedUser(test2, profilesToRemove);
            assertEquals(profilesTest1, mProfileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test1));
            assertEquals(remainingProfiles, mProfileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test2));
            
            // test add
            Set<String> expectedProfiles = Stream.of(profile1, profile2, profile4).collect(Collectors.toSet());
            mProfileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test1, Collections.singleton(profile4));
            assertEquals(expectedProfiles, mProfileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test1));
        }
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
        
        if (_profileAssignmentStorage instanceof ModifiableProfileAssignmentStorage)
        {
            ModifiableProfileAssignmentStorage mProfileAssignmentStorage = (ModifiableProfileAssignmentStorage) _profileAssignmentStorage;
            
            // test add
            mProfileAssignmentStorage.addDeniedProfilesForAnyConnectedUser(test1, profilesTest1);
            
            mProfileAssignmentStorage.addDeniedProfilesForAnyConnectedUser(test2, profilesTest2);
            
            assertEquals(profilesTest1, mProfileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test1));
            assertEquals(profilesTest2, mProfileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test2));
            
            // test remove
            Set<String> profilesToRemove = Stream.of(profile1, profile4, profile5).collect(Collectors.toSet());
            Set<String> remainingProfiles = Collections.singleton(profile3);
            mProfileAssignmentStorage.removeDeniedProfilesForAnyConnectedUser(test2, profilesToRemove);
            assertEquals(profilesTest1, mProfileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test1));
            assertEquals(remainingProfiles, mProfileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test2));
            
            // test add
            Set<String> expectedProfiles = Stream.of(profile1, profile2, profile4).collect(Collectors.toSet());
            mProfileAssignmentStorage.addDeniedProfilesForAnyConnectedUser(test1, Collections.singleton(profile4));
            assertEquals(expectedProfiles, mProfileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test1));
        }
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
        
        if (_profileAssignmentStorage instanceof ModifiableProfileAssignmentStorage)
        {
            ModifiableProfileAssignmentStorage mProfileAssignmentStorage = (ModifiableProfileAssignmentStorage) _profileAssignmentStorage;
            
            // test add
            mProfileAssignmentStorage.addAllowedProfilesForAnonymous(test1, profilesTest1);
            
            mProfileAssignmentStorage.addAllowedProfilesForAnonymous(test2, profilesTest2);
            
            assertEquals(profilesTest1, mProfileAssignmentStorage.getAllowedProfilesForAnonymous(test1));
            assertEquals(profilesTest2, mProfileAssignmentStorage.getAllowedProfilesForAnonymous(test2));
            
            // test remove
            Set<String> profilesToRemove = Stream.of(profile1, profile4, profile5).collect(Collectors.toSet());
            Set<String> remainingProfiles = Collections.singleton(profile3);
            mProfileAssignmentStorage.removeAllowedProfilesForAnonymous(test2, profilesToRemove);
            assertEquals(profilesTest1, mProfileAssignmentStorage.getAllowedProfilesForAnonymous(test1));
            assertEquals(remainingProfiles, mProfileAssignmentStorage.getAllowedProfilesForAnonymous(test2));
            
            // test add
            Set<String> expectedProfiles = Stream.of(profile1, profile2, profile4).collect(Collectors.toSet());
            mProfileAssignmentStorage.addAllowedProfilesForAnonymous(test1, Collections.singleton(profile4));
            assertEquals(expectedProfiles, mProfileAssignmentStorage.getAllowedProfilesForAnonymous(test1));
        }
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
        
        if (_profileAssignmentStorage instanceof ModifiableProfileAssignmentStorage)
        {
            ModifiableProfileAssignmentStorage mProfileAssignmentStorage = (ModifiableProfileAssignmentStorage) _profileAssignmentStorage;
            
            // test add
            mProfileAssignmentStorage.addDeniedProfilesForAnonymous(test1, profilesTest1);
            
            mProfileAssignmentStorage.addDeniedProfilesForAnonymous(test2, profilesTest2);
            
            assertEquals(profilesTest1, mProfileAssignmentStorage.getDeniedProfilesForAnonymous(test1));
            assertEquals(profilesTest2, mProfileAssignmentStorage.getDeniedProfilesForAnonymous(test2));
            
            // test remove
            Set<String> profilesToRemove = Stream.of(profile1, profile4, profile5).collect(Collectors.toSet());
            Set<String> remainingProfiles = Collections.singleton(profile3);
            mProfileAssignmentStorage.removeDeniedProfilesForAnonymous(test2, profilesToRemove);
            assertEquals(profilesTest1, mProfileAssignmentStorage.getDeniedProfilesForAnonymous(test1));
            assertEquals(remainingProfiles, mProfileAssignmentStorage.getDeniedProfilesForAnonymous(test2));
            
            // test add
            Set<String> expectedProfiles = Stream.of(profile1, profile2, profile4).collect(Collectors.toSet());
            mProfileAssignmentStorage.addDeniedProfilesForAnonymous(test1, Collections.singleton(profile4));
            assertEquals(expectedProfiles, mProfileAssignmentStorage.getDeniedProfilesForAnonymous(test1));
        }
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
        
        if (_profileAssignmentStorage instanceof ModifiableProfileAssignmentStorage)
        {
            ModifiableProfileAssignmentStorage mProfileAssignmentStorage = (ModifiableProfileAssignmentStorage) _profileAssignmentStorage;
            
            // test add
            mProfileAssignmentStorage.addAllowedUsers(bothUsers, test1, profile1);
            mProfileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile2);
            expectedUsers.put(user1, bothProfiles);
            expectedUsers.put(user2, Collections.singleton(profile1));
            assertEquals(expectedUsers, mProfileAssignmentStorage.getAllowedProfilesForUsers(test1));
            assertEquals(Collections.EMPTY_MAP, mProfileAssignmentStorage.getAllowedProfilesForUsers(test2));
            assertEquals(bothUsers, mProfileAssignmentStorage.getAllowedUsers(test1, profile1));
            assertEquals(Collections.singleton(user1), mProfileAssignmentStorage.getAllowedUsers(test1, profile2));
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getAllowedUsers(test2, profile1));
            
            // test removing non-existing associations
            mProfileAssignmentStorage.removeAllowedUsers(Collections.singleton(user3), test1, profile1);
            assertEquals(bothUsers, mProfileAssignmentStorage.getAllowedUsers(test1, profile1));
            mProfileAssignmentStorage.removeAllowedUsers(Collections.singleton(user1), test2, profile1);
            assertEquals(Collections.EMPTY_MAP, mProfileAssignmentStorage.getAllowedProfilesForUsers(test2));
            mProfileAssignmentStorage.removeAllowedUsers(Collections.singleton(user2), test1, profile2);
            assertEquals(Collections.singleton(user1), mProfileAssignmentStorage.getAllowedUsers(test1, profile2));
            
            // test remove profile
            mProfileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test2, profile1); //for testing the deletion is not made on another object
            mProfileAssignmentStorage.removeAllowedUsers(Collections.singleton(user1), test1, profile1);
            assertEquals(Collections.singleton(user2), mProfileAssignmentStorage.getAllowedUsers(test1, profile1));
            assertEquals(Collections.singleton(user1), mProfileAssignmentStorage.getAllowedUsers(test1, profile2)); //but still have the profile2
            assertEquals(Collections.singleton(user1), mProfileAssignmentStorage.getAllowedUsers(test2, profile1)); //but still it on another object
            mProfileAssignmentStorage.removeAllowedUsers(Collections.singleton(user2), test1, profile1);
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getAllowedUsers(test1, profile1));
            
            // test remove all profiles
            mProfileAssignmentStorage.addAllowedUsers(threeUsers, test1, profile1); // populate
            mProfileAssignmentStorage.addAllowedUsers(bothUsers, test1, profile2); // populate
            mProfileAssignmentStorage.removeAllowedUsers(bothUsers, test1); // this is what we want to test
            expectedUsers.clear();
            expectedUsers.put(user3, Collections.singleton(profile1));
            assertEquals(expectedUsers, mProfileAssignmentStorage.getAllowedProfilesForUsers(test1));
            expectedUsers.clear();
            expectedUsers.put(user1, Collections.singleton(profile1));
            assertEquals(expectedUsers, mProfileAssignmentStorage.getAllowedProfilesForUsers(test2));
        }
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
        
        if (_profileAssignmentStorage instanceof ModifiableProfileAssignmentStorage)
        {
            ModifiableProfileAssignmentStorage mProfileAssignmentStorage = (ModifiableProfileAssignmentStorage) _profileAssignmentStorage;
            
            // test add
            mProfileAssignmentStorage.addDeniedUsers(bothUsers, test1, profile1);
            mProfileAssignmentStorage.addDeniedUsers(Collections.singleton(user1), test1, profile2);
            expectedUsers.put(user1, bothProfiles);
            expectedUsers.put(user2, Collections.singleton(profile1));
            assertEquals(expectedUsers, mProfileAssignmentStorage.getDeniedProfilesForUsers(test1));
            assertEquals(Collections.EMPTY_MAP, mProfileAssignmentStorage.getDeniedProfilesForUsers(test2));
            assertEquals(bothUsers, mProfileAssignmentStorage.getDeniedUsers(test1, profile1));
            assertEquals(Collections.singleton(user1), mProfileAssignmentStorage.getDeniedUsers(test1, profile2));
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getDeniedUsers(test2, profile1));
            
            // test removing non-existing associations
            mProfileAssignmentStorage.removeDeniedUsers(Collections.singleton(user3), test1, profile1);
            assertEquals(bothUsers, mProfileAssignmentStorage.getDeniedUsers(test1, profile1));
            mProfileAssignmentStorage.removeDeniedUsers(Collections.singleton(user1), test2, profile1);
            assertEquals(Collections.EMPTY_MAP, mProfileAssignmentStorage.getDeniedProfilesForUsers(test2));
            mProfileAssignmentStorage.removeDeniedUsers(Collections.singleton(user2), test1, profile2);
            assertEquals(Collections.singleton(user1), mProfileAssignmentStorage.getDeniedUsers(test1, profile2));
            
            // test remove profile
            mProfileAssignmentStorage.addDeniedUsers(Collections.singleton(user1), test2, profile1); //for testing the deletion is not made on another object
            mProfileAssignmentStorage.removeDeniedUsers(Collections.singleton(user1), test1, profile1);
            assertEquals(Collections.singleton(user2), mProfileAssignmentStorage.getDeniedUsers(test1, profile1));
            assertEquals(Collections.singleton(user1), mProfileAssignmentStorage.getDeniedUsers(test1, profile2)); //but still have the profile2
            assertEquals(Collections.singleton(user1), mProfileAssignmentStorage.getDeniedUsers(test2, profile1)); //but still it on another object
            mProfileAssignmentStorage.removeDeniedUsers(Collections.singleton(user2), test1, profile1);
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getDeniedUsers(test1, profile1));
            
            // test remove all profiles
            mProfileAssignmentStorage.addDeniedUsers(threeUsers, test1, profile1); // populate
            mProfileAssignmentStorage.addDeniedUsers(bothUsers, test1, profile2); // populate
            mProfileAssignmentStorage.removeDeniedUsers(bothUsers, test1); // this is what we want to test
            expectedUsers.clear();
            expectedUsers.put(user3, Collections.singleton(profile1));
            assertEquals(expectedUsers, mProfileAssignmentStorage.getDeniedProfilesForUsers(test1));
            expectedUsers.clear();
            expectedUsers.put(user1, Collections.singleton(profile1));
            assertEquals(expectedUsers, mProfileAssignmentStorage.getDeniedProfilesForUsers(test2));
        }
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
        
        if (_profileAssignmentStorage instanceof ModifiableProfileAssignmentStorage)
        {
            ModifiableProfileAssignmentStorage mProfileAssignmentStorage = (ModifiableProfileAssignmentStorage) _profileAssignmentStorage;
            
            // test add
            mProfileAssignmentStorage.addAllowedGroups(bothGroups, test1, profile1);
            mProfileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile2);
            Map<GroupIdentity, Set<String>> expectedGroups = new HashMap<>();
            expectedGroups.put(group1, bothProfiles);
            expectedGroups.put(group2, Collections.singleton(profile1));
            assertEquals(expectedGroups, mProfileAssignmentStorage.getAllowedProfilesForGroups(test1));
            assertEquals(Collections.EMPTY_MAP, mProfileAssignmentStorage.getAllowedProfilesForGroups(test2));
            assertEquals(bothGroups, mProfileAssignmentStorage.getAllowedGroups(test1, profile1));
            assertEquals(Collections.singleton(group1), mProfileAssignmentStorage.getAllowedGroups(test1, profile2));
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getAllowedGroups(test2, profile1));
            
            // test removing non-existing associations
            mProfileAssignmentStorage.removeAllowedGroups(Collections.singleton(group3), test1, profile1);
            assertEquals(bothGroups, mProfileAssignmentStorage.getAllowedGroups(test1, profile1));
            mProfileAssignmentStorage.removeAllowedGroups(Collections.singleton(group1), test2, profile1);
            assertEquals(Collections.EMPTY_MAP, mProfileAssignmentStorage.getAllowedProfilesForGroups(test2));
            mProfileAssignmentStorage.removeAllowedGroups(Collections.singleton(group2), test1, profile2);
            assertEquals(Collections.singleton(group1), mProfileAssignmentStorage.getAllowedGroups(test1, profile2));
            
            // test remove profile
            mProfileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test2, profile1); //for testing the deletion is not made on another object
            mProfileAssignmentStorage.removeAllowedGroups(Collections.singleton(group1), test1, profile1);
            assertEquals(Collections.singleton(group2), mProfileAssignmentStorage.getAllowedGroups(test1, profile1));
            assertEquals(Collections.singleton(group1), mProfileAssignmentStorage.getAllowedGroups(test1, profile2)); //but still have the profile2
            assertEquals(Collections.singleton(group1), mProfileAssignmentStorage.getAllowedGroups(test2, profile1)); //but still it on another object
            mProfileAssignmentStorage.removeAllowedGroups(Collections.singleton(group2), test1, profile1);
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getAllowedGroups(test1, profile1));
            
            // test remove all profiles
            mProfileAssignmentStorage.addAllowedGroups(threeGroups, test1, profile1); // populate
            mProfileAssignmentStorage.addAllowedGroups(bothGroups, test1, profile2); // populate
            mProfileAssignmentStorage.removeAllowedGroups(bothGroups, test1); // this is what we want to test
            expectedGroups.clear();
            expectedGroups.put(group3, Collections.singleton(profile1));
            assertEquals(expectedGroups, mProfileAssignmentStorage.getAllowedProfilesForGroups(test1));
            expectedGroups.clear();
            expectedGroups.put(group1, Collections.singleton(profile1));
            assertEquals(expectedGroups, mProfileAssignmentStorage.getAllowedProfilesForGroups(test2));
        }
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
        
        if (_profileAssignmentStorage instanceof ModifiableProfileAssignmentStorage)
        {
            ModifiableProfileAssignmentStorage mProfileAssignmentStorage = (ModifiableProfileAssignmentStorage) _profileAssignmentStorage;
          
            // test add
            mProfileAssignmentStorage.addDeniedGroups(bothGroups, test1, profile1);
            mProfileAssignmentStorage.addDeniedGroups(Collections.singleton(group1), test1, profile2);
            Map<GroupIdentity, Set<String>> expectedGroups = new HashMap<>();
            expectedGroups.put(group1, bothProfiles);
            expectedGroups.put(group2, Collections.singleton(profile1));
            assertEquals(expectedGroups, mProfileAssignmentStorage.getDeniedProfilesForGroups(test1));
            assertEquals(Collections.EMPTY_MAP, mProfileAssignmentStorage.getDeniedProfilesForGroups(test2));
            assertEquals(bothGroups, mProfileAssignmentStorage.getDeniedGroups(test1, profile1));
            assertEquals(Collections.singleton(group1), mProfileAssignmentStorage.getDeniedGroups(test1, profile2));
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getDeniedGroups(test2, profile1));
            
            // test removing non-existing associations
            mProfileAssignmentStorage.removeDeniedGroups(Collections.singleton(group3), test1, profile1);
            assertEquals(bothGroups, mProfileAssignmentStorage.getDeniedGroups(test1, profile1));
            mProfileAssignmentStorage.removeDeniedGroups(Collections.singleton(group1), test2, profile1);
            assertEquals(Collections.EMPTY_MAP, mProfileAssignmentStorage.getDeniedProfilesForGroups(test2));
            mProfileAssignmentStorage.removeDeniedGroups(Collections.singleton(group2), test1, profile2);
            assertEquals(Collections.singleton(group1), mProfileAssignmentStorage.getDeniedGroups(test1, profile2));
            
            // test remove profile
            mProfileAssignmentStorage.addDeniedGroups(Collections.singleton(group1), test2, profile1); //for testing the deletion is not made on another object
            mProfileAssignmentStorage.removeDeniedGroups(Collections.singleton(group1), test1, profile1);
            assertEquals(Collections.singleton(group2), mProfileAssignmentStorage.getDeniedGroups(test1, profile1));
            assertEquals(Collections.singleton(group1), mProfileAssignmentStorage.getDeniedGroups(test1, profile2)); //but still have the profile2
            assertEquals(Collections.singleton(group1), mProfileAssignmentStorage.getDeniedGroups(test2, profile1)); //but still it on another object
            mProfileAssignmentStorage.removeDeniedGroups(Collections.singleton(group2), test1, profile1);
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getDeniedGroups(test1, profile1));
            
            // test remove all profiles
            mProfileAssignmentStorage.addDeniedGroups(threeGroups, test1, profile1); // populate
            mProfileAssignmentStorage.addDeniedGroups(bothGroups, test1, profile2); // populate
            mProfileAssignmentStorage.removeDeniedGroups(bothGroups, test1); // this is what we want to test
            expectedGroups.clear();
            expectedGroups.put(group3, Collections.singleton(profile1));
            assertEquals(expectedGroups, mProfileAssignmentStorage.getDeniedProfilesForGroups(test1));
            expectedGroups.clear();
            expectedGroups.put(group1, Collections.singleton(profile1));
            assertEquals(expectedGroups, mProfileAssignmentStorage.getDeniedProfilesForGroups(test2));
        }
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
        
        if (_profileAssignmentStorage instanceof ModifiableProfileAssignmentStorage)
        {
            ModifiableProfileAssignmentStorage mProfileAssignmentStorage = (ModifiableProfileAssignmentStorage) _profileAssignmentStorage;
          
            // populate
            mProfileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile1);
            mProfileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile2);
            
            mProfileAssignmentStorage.addDeniedUsers(Collections.singleton(user1), test1, profile1);
            mProfileAssignmentStorage.addDeniedUsers(Collections.singleton(user1), test1, profile2);
            
            mProfileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile1);
            mProfileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile2);
            
            mProfileAssignmentStorage.addDeniedGroups(Collections.singleton(group1), test1, profile1);
            mProfileAssignmentStorage.addDeniedGroups(Collections.singleton(group1), test1, profile2);
            
            mProfileAssignmentStorage.addAllowedProfilesForAnyConnectedUser(test1, bothProfiles);
            mProfileAssignmentStorage.addDeniedProfilesForAnyConnectedUser(test1, bothProfiles);
            
            mProfileAssignmentStorage.addAllowedProfilesForAnonymous(test1, bothProfiles);
            mProfileAssignmentStorage.addDeniedProfilesForAnonymous(test1, bothProfiles);
            
            // test removing
            mProfileAssignmentStorage.removeProfile(profile1);
            
            Map<UserIdentity, Set<String>> expectedUsers = new HashMap<>();
            Map<GroupIdentity, Set<String>> expectedGroups = new HashMap<>();
            
            expectedUsers.put(user1, Collections.singleton(profile2));
            assertEquals(expectedUsers, mProfileAssignmentStorage.getAllowedProfilesForUsers(test1));
            assertEquals(expectedUsers, mProfileAssignmentStorage.getDeniedProfilesForUsers(test1));
            
            expectedGroups.put(group1, Collections.singleton(profile2));
            assertEquals(expectedGroups, mProfileAssignmentStorage.getAllowedProfilesForGroups(test1));
            assertEquals(expectedGroups, mProfileAssignmentStorage.getDeniedProfilesForGroups(test1));
            
            assertEquals(Collections.singleton(profile2), mProfileAssignmentStorage.getAllowedProfilesForAnyConnectedUser(test1));
            assertEquals(Collections.singleton(profile2), mProfileAssignmentStorage.getDeniedProfilesForAnyConnectedUser(test1));
            assertEquals(Collections.singleton(profile2), mProfileAssignmentStorage.getAllowedProfilesForAnonymous(test1));
            assertEquals(Collections.singleton(profile2), mProfileAssignmentStorage.getDeniedProfilesForAnonymous(test1));
        }
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
        
        if (_profileAssignmentStorage instanceof ModifiableProfileAssignmentStorage)
        {
            ModifiableProfileAssignmentStorage mProfileAssignmentStorage = (ModifiableProfileAssignmentStorage) _profileAssignmentStorage;
          
            // populate
            mProfileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test1, profile1);
            mProfileAssignmentStorage.addAllowedUsers(Collections.singleton(user2), test1, profile2);
            mProfileAssignmentStorage.addAllowedUsers(Collections.singleton(user1), test2, profile1);
            
            mProfileAssignmentStorage.addDeniedUsers(Collections.singleton(user2), test1, profile1);
            mProfileAssignmentStorage.addDeniedUsers(Collections.singleton(user1), test1, profile2);
            mProfileAssignmentStorage.addDeniedUsers(Collections.singleton(user2), test2, profile1);
            
            // test removing
            mProfileAssignmentStorage.removeUser(user1);
            
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getAllowedUsers(test1, profile1));
            assertEquals(Collections.singleton(user2), mProfileAssignmentStorage.getAllowedUsers(test1, profile2));
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getAllowedUsers(test2, profile1));
            
            assertEquals(Collections.singleton(user2), mProfileAssignmentStorage.getDeniedUsers(test1, profile1));
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getDeniedUsers(test1, profile2));
            assertEquals(Collections.singleton(user2), mProfileAssignmentStorage.getDeniedUsers(test2, profile1));
        }
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
        
        if (_profileAssignmentStorage instanceof ModifiableProfileAssignmentStorage)
        {
            ModifiableProfileAssignmentStorage mProfileAssignmentStorage = (ModifiableProfileAssignmentStorage) _profileAssignmentStorage;
          
            // populate
            mProfileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test1, profile1);
            mProfileAssignmentStorage.addAllowedGroups(Collections.singleton(group2), test1, profile2);
            mProfileAssignmentStorage.addAllowedGroups(Collections.singleton(group1), test2, profile1);
            
            mProfileAssignmentStorage.addDeniedGroups(Collections.singleton(group2), test1, profile1);
            mProfileAssignmentStorage.addDeniedGroups(Collections.singleton(group1), test1, profile2);
            mProfileAssignmentStorage.addDeniedGroups(Collections.singleton(group2), test2, profile1);
            
            // test removing
            mProfileAssignmentStorage.removeGroup(group1);
            
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getAllowedGroups(test1, profile1));
            assertEquals(Collections.singleton(group2), mProfileAssignmentStorage.getAllowedGroups(test1, profile2));
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getAllowedGroups(test2, profile1));
            
            assertEquals(Collections.singleton(group2), mProfileAssignmentStorage.getDeniedGroups(test1, profile1));
            assertEquals(Collections.EMPTY_SET, mProfileAssignmentStorage.getDeniedGroups(test1, profile2));
            assertEquals(Collections.singleton(group2), mProfileAssignmentStorage.getDeniedGroups(test2, profile1));
        }
    }
}
