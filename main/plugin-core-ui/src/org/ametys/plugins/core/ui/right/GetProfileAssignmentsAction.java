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
package org.ametys.plugins.core.ui.right;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.core.group.Group;
import org.ametys.core.group.GroupDirectoryDAO;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.group.GroupManager;
import org.ametys.core.right.ProfileAssignmentStorageExtensionPoint;
import org.ametys.core.right.RightAssignmentContext;
import org.ametys.core.right.RightAssignmentContextExtensionPoint;
import org.ametys.core.right.RightProfilesDAO;
import org.ametys.core.ui.right.ProfileAssignmentsToolClientSideElement.AccessType;
import org.ametys.core.ui.right.ProfileAssignmentsToolClientSideElement.TargetType;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;
import org.ametys.core.user.population.UserPopulationDAO;

/**
 * Action for generating the grid for profile assignments
 */
public class GetProfileAssignmentsAction extends ServiceableAction
{
    /** The profile assignment storage component */
    protected ProfileAssignmentStorageExtensionPoint _profileAssignmentStorageEP;
    /** The extension point for right assignment contexts */
    protected RightAssignmentContextExtensionPoint _rightAssignmentContextEP;
    /** The profiles DAO */
    protected RightProfilesDAO _profilesDAO;
    /** The DAO for user populations */
    protected UserPopulationDAO _userPopulationDAO;
    /** The user manager */
    protected UserManager _userManager;
    /** The DAO for group directories */
    protected GroupDirectoryDAO _groupDirectoryDAO;
    /** The group manager */
    protected GroupManager _groupManager;

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _profileAssignmentStorageEP = (ProfileAssignmentStorageExtensionPoint) smanager.lookup(ProfileAssignmentStorageExtensionPoint.ROLE);
        _rightAssignmentContextEP = (RightAssignmentContextExtensionPoint) smanager.lookup(RightAssignmentContextExtensionPoint.ROLE);
        _userPopulationDAO = (UserPopulationDAO) smanager.lookup(UserPopulationDAO.ROLE);
        _userManager = (UserManager) smanager.lookup(UserManager.ROLE);
        _groupDirectoryDAO = (GroupDirectoryDAO) smanager.lookup(GroupDirectoryDAO.ROLE);
        _groupManager = (GroupManager) smanager.lookup(GroupManager.ROLE);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (_profilesDAO == null)
        {
            _profilesDAO = (RightProfilesDAO) manager.lookup(RightProfilesDAO.ROLE);
        }
        
        Map<String, Object> result = new HashMap<>();
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        Map jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        String rightAssignmentCtxId = (String) jsParameters.get("rightAssignmentContextId");
        RightAssignmentContext rightCtx = _rightAssignmentContextEP.getExtension(rightAssignmentCtxId);
        
        Object jsContext = jsParameters.get("context");
        Object context = rightCtx.convertJSContext(jsContext);
        
        List<String> profileIds = (List<String>) jsParameters.get("profileIds");
        if (profileIds == null)
        {
            // Get the identifiers of all existing profiles
            profileIds = _profilesDAO.getProfiles().stream().map(profile -> profile.getId()).collect(Collectors.toList());
        }
        
        List<Map<String, Object>> assignments = new ArrayList<>();
        
        assignments.add(_getAssignmentForAnonymous(rightCtx, context, profileIds));
        assignments.add(_getAssignmentForAnyConnectedUser(rightCtx, context, profileIds));
        assignments.addAll(_getAssignmentForUsers(rightCtx, context, profileIds));
        assignments.addAll(_getAssignmentForGroups(rightCtx, context, profileIds));
        
        result.put("assignments", assignments);
        
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        
        return EMPTY_MAP;
    }
    
    private Map<String, Object> _getAssignmentForAnonymous(RightAssignmentContext rightCtx, Object context, List<String> profileIds)
    {
        Map<String, Object> assignment = new HashMap<>();
        
        assignment.put("targetType", TargetType.ANONYMOUS.toString());
        
        for (String profileId : profileIds)
        {
            boolean found = false;
            
            Object currentContext = context;
            while (currentContext != null && !found)
            {
                Set<String> deniedProfiles = _profileAssignmentStorageEP.getDeniedProfilesForAnonymous(currentContext);
                if (deniedProfiles.contains(profileId))
                {
                    assignment.put(profileId, currentContext == context ? AccessType.DENY.toString() : AccessType.INHERITED_DENY.toString());
                    found = true; // stop iteration
                }
                
                Set<String> allowedProfiles = _profileAssignmentStorageEP.getAllowedProfilesForAnonymous(currentContext);
                if (allowedProfiles.contains(profileId))
                {
                    assignment.put(profileId, currentContext == context ? AccessType.ALLOW.toString() : AccessType.INHERITED_ALLOW.toString());
                    found = true; // stop iteration
                }
                
                // Can not determines assignment on current context, up to parent context
                currentContext = rightCtx.getParentContext(currentContext);
            }
        }
        
        return assignment;
    }
    
    private Map<String, Object> _getAssignmentForAnyConnectedUser(RightAssignmentContext rightCtx, Object context, List<String> profileIds)
    {
        Map<String, Object> assignment = new HashMap<>();
        
        assignment.put("targetType", TargetType.ANYCONNECTED_USER.toString());
        
        for (String profileId : profileIds)
        {
            boolean found = false;
            
            Object currentContext = context;
            while (currentContext != null && !found)
            {
                Set<String> deniedProfiles = _profileAssignmentStorageEP.getDeniedProfilesForAnyConnectedUser(currentContext);
                if (deniedProfiles.contains(profileId))
                {
                    assignment.put(profileId, currentContext == context ? AccessType.DENY.toString() : AccessType.INHERITED_DENY.toString());
                    found = true; // stop iteration
                }
                
                Set<String> allowedProfiles = _profileAssignmentStorageEP.getAllowedProfilesForAnyConnectedUser(currentContext);
                if (allowedProfiles.contains(profileId))
                {
                    assignment.put(profileId, currentContext == context ? AccessType.ALLOW.toString() : AccessType.INHERITED_ALLOW.toString());
                    found = true; // stop iteration
                }
                
                // Can not determines assignment on current context, up to parent context
                currentContext = rightCtx.getParentContext(currentContext);
            }
        }
        
        return assignment;
    }
    
    private List<Map<String, Object>> _getAssignmentForUsers(RightAssignmentContext rightCtx, Object context, List<String> profileIds)
    {
        Map<UserIdentity, Map<String, Object>> assignments = new LinkedHashMap<>();
        
        Object currentContext = context;
        while (currentContext != null)
        {
            // Get all user with a denied profile on current context
            Map<UserIdentity, Set<String>> deniedProfilesForUsers = _profileAssignmentStorageEP.getDeniedProfilesForUsers(currentContext);
            for (UserIdentity userIdentity : deniedProfilesForUsers.keySet())
            {
                if (!assignments.containsKey(userIdentity))
                {
                    assignments.put(userIdentity, _user2json(userIdentity));
                }
                
                Map<String, Object> userAssignment = assignments.get(userIdentity);
                
                for (String profileId : deniedProfilesForUsers.get(userIdentity))
                {
                    if (profileIds.contains(profileId) && !userAssignment.containsKey(profileId))
                    {
                        userAssignment.put(profileId, currentContext == context ? AccessType.DENY.toString() : AccessType.INHERITED_DENY.toString());
                    }
                }
            }
            
            // Get all user with a allowed profile on current context
            Map<UserIdentity, Set<String>> allowedProfilesForUsers = _profileAssignmentStorageEP.getAllowedProfilesForUsers(currentContext);
            for (UserIdentity userIdentity : allowedProfilesForUsers.keySet())
            {
                if (!assignments.containsKey(userIdentity))
                {
                    assignments.put(userIdentity, _user2json(userIdentity));
                }
                
                Map<String, Object> userAssignment = assignments.get(userIdentity);
                
                for (String profileId : allowedProfilesForUsers.get(userIdentity))
                {
                    if (profileIds.contains(profileId) && !userAssignment.containsKey(profileId))
                    {
                        userAssignment.put(profileId, currentContext == context ? AccessType.ALLOW.toString() : AccessType.INHERITED_ALLOW.toString());
                    }
                }
            }
            
            // Up to parent context
            currentContext = rightCtx.getParentContext(currentContext);
        }
        
        return new ArrayList<>(assignments.values());
    }
    
    private List<Map<String, Object>> _getAssignmentForGroups(RightAssignmentContext rightCtx, Object context, List<String> profileIds)
    {
        Map<GroupIdentity, Map<String, Object>> assignments = new LinkedHashMap<>();
        
        Object currentContext = context;
        while (currentContext != null)
        {
            // Get all user with a denied profile on current context
            Map<GroupIdentity, Set<String>> deniedProfilesForGroups = _profileAssignmentStorageEP.getDeniedProfilesForGroups(currentContext);
            for (GroupIdentity gpIdentity : deniedProfilesForGroups.keySet())
            {
                if (!assignments.containsKey(gpIdentity))
                {
                    assignments.put(gpIdentity, _group2json(gpIdentity));
                }
                
                Map<String, Object> gpAssignment = assignments.get(gpIdentity);
                
                for (String profileId : deniedProfilesForGroups.get(gpIdentity))
                {
                    if (profileIds.contains(profileId) && !gpAssignment.containsKey(profileId))
                    {
                        gpAssignment.put(profileId, currentContext == context ? AccessType.DENY.toString() : AccessType.INHERITED_DENY.toString());
                    }
                }
            }
            
            // Get all user with a allowed profile on current context
            Map<GroupIdentity, Set<String>> allowedProfilesForGroups = _profileAssignmentStorageEP.getAllowedProfilesForGroups(currentContext);
            for (GroupIdentity gpIdentity : allowedProfilesForGroups.keySet())
            {
                if (!assignments.containsKey(gpIdentity))
                {
                    assignments.put(gpIdentity, _group2json(gpIdentity));
                }
                
                Map<String, Object> gpAssignment = assignments.get(gpIdentity);
                
                for (String profileId : allowedProfilesForGroups.get(gpIdentity))
                {
                    if (profileIds.contains(profileId) && !gpAssignment.containsKey(profileId))
                    {
                        gpAssignment.put(profileId, currentContext == context ? AccessType.ALLOW.toString() : AccessType.INHERITED_ALLOW.toString());
                    }
                }
            }
            
            // Up to parent context
            currentContext = rightCtx.getParentContext(currentContext);
        }
        
        return new ArrayList<>(assignments.values());
    }
    
    private Map<String, Object> _user2json(UserIdentity userIdentity)
    {
        Map<String, Object> assignment = new HashMap<>();
        assignment.put("targetType", TargetType.USER.toString());
        
        String login = userIdentity.getLogin();
        String populationId = userIdentity.getPopulationId();
        assignment.put("login", login);
        assignment.put("population", populationId);
        assignment.put("populationLabel", _userPopulationDAO.getUserPopulation(populationId).getLabel());
        assignment.put("groups", _groupManager.getUserGroups(userIdentity).stream()
                                    .map(this::_userGroup2json)
                                    .collect(Collectors.toList()));
        
        User user = _userManager.getUser(populationId, login);
        assignment.put("userSortableName", user.getSortableName());
        
        return assignment;
    }
   
    
    private Map<String, Object> _group2json(GroupIdentity groupIdentity)
    {
        Map<String, Object> assignment = new HashMap<>();
        assignment.put("targetType", TargetType.GROUP.toString());
        
        String groupId = groupIdentity.getId();
        String directoryId = groupIdentity.getDirectoryId();
        assignment.put("groupId", groupId);
        assignment.put("groupDirectory", directoryId);
        assignment.put("groupDirectoryLabel", _groupDirectoryDAO.getGroupDirectory(directoryId).getLabel());
        
        Group group = _groupManager.getGroup(directoryId, groupId);
        assignment.put("groupLabel", group.getLabel());
        
        return assignment;
    }
    
    private Map<String, Object> _userGroup2json(GroupIdentity group)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("groupId", group.getId());
        result.put("groupDirectory", group.getDirectoryId());
        return result;
    }
}
