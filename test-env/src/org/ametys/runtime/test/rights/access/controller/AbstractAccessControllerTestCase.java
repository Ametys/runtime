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
package org.ametys.runtime.test.rights.access.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import org.ametys.core.group.GroupIdentity;
import org.ametys.core.right.AccessController;
import org.ametys.core.right.AccessController.AccessResult;
import org.ametys.core.right.AccessController.AccessResultContext;
import org.ametys.core.right.AccessControllerExtensionPoint;
import org.ametys.core.right.ProfileAssignmentStorageExtensionPoint;
import org.ametys.core.right.RightManager;
import org.ametys.core.user.UserIdentity;
import org.ametys.runtime.test.AbstractRuntimeTestCase;
import org.ametys.runtime.test.Init;

/**
 * Common test class for testing the {@link AccessController}
 */
public abstract class AbstractAccessControllerTestCase extends AbstractRuntimeTestCase
{
    /** The access controller to test */
    protected AccessController _accessController;
    
    /** The extension point for profile assignment storage */
    protected ProfileAssignmentStorageExtensionPoint _profileAssignmentStorageEP;

    /** The right manager */
    protected RightManager _rightManager;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        _initComponents();
        LogManager.getLoggerRepository().getLogger(AccessControllerExtensionPoint.class.getName()).setLevel(Level.DEBUG);
    }
    
    /**
     * Returns the id of the extension for {@link AccessController} to use.
     * @return the id of the extension to use.
     */
    protected abstract String _getExtensionId();
    
    private void _initComponents() throws Exception
    {
        AccessControllerExtensionPoint accessControllerEP = (AccessControllerExtensionPoint) Init.getPluginServiceManager().lookup(AccessControllerExtensionPoint.ROLE);
        _accessController = accessControllerEP.getExtension(_getExtensionId());
        assertNotNull("The #_getExtensionId method should point to an AccessController extension", _accessController);
        _profileAssignmentStorageEP = (ProfileAssignmentStorageExtensionPoint) Init.getPluginServiceManager().lookup(ProfileAssignmentStorageExtensionPoint.ROLE);
        _rightManager = (RightManager) Init.getPluginServiceManager().lookup(RightManager.ROLE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        _cocoon.dispose();
        super.tearDown();
    }
    
    /**
     * Gets the typed test object 1
     * @return the typed test object 1
     */
    protected abstract Object _getTest1();
    
    /**
     * Gets the typed test object 2
     * @return the typed test object 2
     */
    protected abstract Object _getTest2();
    
    /**
     * Tests responding UNKNOWN when no assignments set
     */
    public void testEmpty()
    {
        Object test = _getTest1();
        
        String profile1 = "1";
        String profile2 = "2";
        Set<String> bothProfiles = Stream.of(profile1, profile2).collect(Collectors.toSet());
        
        UserIdentity user1 = new UserIdentity("user1", "population1");
        
        GroupIdentity group1 = new GroupIdentity("group1", "directory1");
        GroupIdentity group2 = new GroupIdentity("group2", "directory2");
        Set<GroupIdentity> bothGroups = Stream.of(group1, group2).collect(Collectors.toSet());
        
        // initially nothing is set
        assertEquals(Collections.EMPTY_MAP, _accessController.getPermissions(user1, Collections.EMPTY_SET, Collections.EMPTY_SET, null));
        assertEquals(Collections.EMPTY_MAP, _accessController.getPermissions(user1, Collections.EMPTY_SET, Collections.EMPTY_SET, test));

        assertEquals(Stream.of(profile1, profile2).collect(Collectors.toMap(Function.identity(), p -> new AccessResultContext(AccessResult.UNKNOWN, null))), 
                     _accessController.getPermissions(user1, bothGroups, bothProfiles, test));
    }
    
    /**
     * Tests getPermissions method
     */
    public void testGetPermissions()
    {
        Object test1 = _getTest1();
        Object test2 = _getTest2();
        
        String profile1 = "1";
        String profile2 = "2";
        String profile3 = "3";
        String profile4 = "4";
        String profile5 = "5";
        String profile6 = "6";
        String profile7 = "7";
        
        UserIdentity user1 = new UserIdentity("user1", "population1");
//        UserIdentity user2 = new UserIdentity("user2", "population2");
//        Set<UserIdentity> bothUsers = Stream.of(user1, user2).collect(Collectors.toSet());
        
        GroupIdentity group1 = new GroupIdentity("group1", "directory1");
        GroupIdentity group2 = new GroupIdentity("group2", "directory23");
        GroupIdentity group3 = new GroupIdentity("group3", "directory23");
        
        /*
         * TODO here the excluded all are not set, maybe test that ?
         * Set the profiles on test1 object as
         * any connected user has granted profile 1
         * user1 has granted profile 2 and excluded profile 3
         * group1 has granted profile 4 and excluded profile 5
         * group2 has granted profile 6 and excluded profile 7
         * group3 has granted profile 5 and excluded profile 4
         */
        _rightManager.allowProfileToAnyConnectedUser(profile1, test1);
        _rightManager.allowProfileToUser(user1, profile2, test1);
        _rightManager.denyProfileToUser(user1, profile3, test1);
        
        _rightManager.allowProfileToGroup(group1, profile4, test1);
        _rightManager.denyProfileToGroup(group1, profile5, test1);
        
        _rightManager.allowProfileToGroup(group2, profile6, test1);
        _rightManager.denyProfileToGroup(group2, profile7, test1);
        
        _rightManager.allowProfileToGroup(group3, profile5, test1);
        _rightManager.denyProfileToGroup(group3, profile4, test1);
        
        // test
        // TODO faire les tests sur qui prend la main si le même profile est allowed et denied par exemple (mix user/group, etc.)
        assertEquals(_expectedSingletonMap(profile1, AccessResult.ANY_CONNECTED_ALLOWED, null), 
                     _accessController.getPermissions(user1, Collections.EMPTY_SET, Collections.singleton(profile1), test1));
        assertEquals(_expectedSingletonMap(profile1, AccessResult.UNKNOWN, null), 
                     _accessController.getPermissions(user1, Collections.EMPTY_SET, Collections.singleton(profile1), test2));
        assertEquals(_expectedSingletonMap(profile4, AccessResult.UNKNOWN, null), 
                     _accessController.getPermissions(user1, Collections.EMPTY_SET, Collections.singleton(profile4), test1));
        
        assertEquals(_expectedSingletonMap(profile2, AccessResult.USER_ALLOWED, null), 
                     _accessController.getPermissions(user1, Collections.EMPTY_SET, Collections.singleton(profile2), test1));
        assertEquals(_expectedSingletonMap(profile3, AccessResult.USER_DENIED, null), 
                     _accessController.getPermissions(user1, Collections.EMPTY_SET, Collections.singleton(profile3), test1));
//        Set<String> profiles = new HashSet<>();
//        profiles.add(profile1);
//        profiles.add(profile3);
//        assertEquals(AccessResult.USER_NOK, _accessController.isAllowed(user1, Collections.EMPTY_SET, profiles, test1));
//        profiles.clear();
//        profiles.add(profile2);
//        profiles.add(profile3);
//        assertEquals(AccessResult.USER_OK, _accessController.isAllowed(user1, Collections.EMPTY_SET, profiles, test1));
        
        assertEquals(_expectedSingletonMap(profile4, AccessResult.GROUP_ALLOWED, group1), 
                     _accessController.getPermissions(user1, Collections.singleton(group1), Collections.singleton(profile4), test1));
//        profiles.clear();
//        profiles.add(profile1);
//        profiles.add(profile4);
//        assertEquals(AccessResult.GROUP_OK, _accessController.isAllowed(user1, Collections.singleton(group1), profiles, test1));
//        profiles.clear();
//        profiles.add(profile2);
//        profiles.add(profile4);
//        assertEquals(AccessResult.GROUP_OK, _accessController.isAllowed(user1, Collections.singleton(group1), profiles, test1));
        
        assertEquals(_expectedSingletonMap(profile5, AccessResult.GROUP_DENIED, group1), 
                     _accessController.getPermissions(user1, Collections.singleton(group1), Collections.singleton(profile5), test1));
        
        assertEquals(_expectedSingletonMap(profile6, AccessResult.GROUP_ALLOWED, group2), 
                     _accessController.getPermissions(user1, Collections.singleton(group2), Collections.singleton(profile6), test1));
        assertEquals(_expectedSingletonMap(profile7, AccessResult.GROUP_DENIED, group2), 
                     _accessController.getPermissions(user1, Collections.singleton(group2), Collections.singleton(profile7), test1));
        
        assertEquals(_expectedSingletonMap(profile5, AccessResult.GROUP_ALLOWED, group3), 
                     _accessController.getPermissions(user1, Collections.singleton(group3), Collections.singleton(profile5), test1));
        assertEquals(_expectedSingletonMap(profile4, AccessResult.GROUP_DENIED, group3), 
                     _accessController.getPermissions(user1, Collections.singleton(group3), Collections.singleton(profile4), test1));
    }
    
    private Map<String, AccessResultContext> _expectedSingletonMap(String profile, AccessResult result, GroupIdentity group)
    {
        if (group == null)
        {
            // returns a map {profile: AccessResultContext(result, null)}
            return Stream.of(profile).collect(Collectors.toMap(Function.identity(), p -> new AccessResultContext(result, null)));
        }
        else
        {
            // returns a map {profile: AccessResultContext(result, [group])}
            return Stream.of(profile).collect(Collectors.toMap(Function.identity(), p -> new AccessResultContext(result, Collections.singleton(group))));
        }
    }
    
    /**
     * Tests getPermissionsByProfile method
     */
    public void testGetPermissionsByProfile()
    {
        Object test1 = _getTest1();
//        Object test2 = _getTest2();
        
        String profile1 = "1";
        String profile2 = "2";
        String profile3 = "3";
        String profile4 = "4";
//        String profile5 = "5";
//        String profile6 = "6";
//        String profile7 = "7";
        
        UserIdentity user1 = new UserIdentity("user1", "population1");
        
        GroupIdentity group1 = new GroupIdentity("group1", "directory1");
//        GroupIdentity group2 = new GroupIdentity("group2", "directory23");
//        GroupIdentity group3 = new GroupIdentity("group3", "directory23");
        
        // test initially empty
        assertEquals(Collections.EMPTY_MAP,
                     _accessController.getPermissionsByProfile(user1, Collections.singleton(group1), test1));
        // test 1
        _rightManager.allowProfileToAnonymous(profile1, test1);
        _rightManager.allowProfileToAnyConnectedUser(profile2, test1);
        _rightManager.allowProfileToGroup(group1, profile3, test1);
        _rightManager.allowProfileToUser(user1, profile4, test1);
        
        Map<String, AccessResult> expectedMap = new HashMap<>();
        expectedMap.put(profile1, AccessResult.ANONYMOUS_ALLOWED);
        expectedMap.put(profile2, AccessResult.ANY_CONNECTED_ALLOWED);
        expectedMap.put(profile3, AccessResult.GROUP_ALLOWED);
        expectedMap.put(profile4, AccessResult.USER_ALLOWED);
        
        assertEquals(expectedMap,
                _accessController.getPermissionsByProfile(user1, Collections.singleton(group1), test1));
        
        // test 2
        _rightManager.denyProfileToAnonymous(profile1, test1);
        expectedMap.put(profile1, AccessResult.ANONYMOUS_DENIED);
        
        assertEquals(expectedMap,
                _accessController.getPermissionsByProfile(user1, Collections.singleton(group1), test1));
        
        // test 3
        _rightManager.denyProfileToAnyConnectedUser(profile2, test1);
        expectedMap.put(profile2, AccessResult.ANY_CONNECTED_DENIED);
        
        assertEquals(expectedMap,
                _accessController.getPermissionsByProfile(user1, Collections.singleton(group1), test1));
        
        // test 4
        _rightManager.denyProfileToGroup(group1, profile3, test1);
        expectedMap.put(profile3, AccessResult.GROUP_DENIED);
        
        assertEquals(expectedMap,
                _accessController.getPermissionsByProfile(user1, Collections.singleton(group1), test1));
        
        // test 5
        _rightManager.denyProfileToUser(user1, profile4, test1);
        expectedMap.put(profile4, AccessResult.USER_DENIED);
        
        assertEquals(expectedMap,
                _accessController.getPermissionsByProfile(user1, Collections.singleton(group1), test1));
        
        // Let's restart from scratch
        _rightManager.removeProfile(profile1);
        _rightManager.removeProfile(profile2);
        _rightManager.removeProfile(profile3);
        _rightManager.removeProfile(profile4);
        expectedMap.clear();
        
        // test 1
        _rightManager.denyProfileToAnonymous(profile1, test1);
        expectedMap.put(profile1, AccessResult.ANONYMOUS_DENIED);
        
        assertEquals(expectedMap,
                _accessController.getPermissionsByProfile(user1, Collections.singleton(group1), test1));
        
        // test 2
        _rightManager.allowProfileToAnonymous(profile1, test1);
        expectedMap.put(profile1, AccessResult.ANY_CONNECTED_ALLOWED);
        
        // test 3
        _rightManager.allowProfileToGroup(group1, profile1, test1);
        expectedMap.put(profile1, AccessResult.GROUP_ALLOWED);
        
        // test 4
        _rightManager.allowProfileToUser(user1, profile1, test1);
        expectedMap.put(profile1, AccessResult.USER_ALLOWED);
    }
}
