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
import org.ametys.core.right.RightAssignmentContextExtensionPoint;
import org.ametys.core.right.RightManager;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;
import org.ametys.core.user.population.UserPopulationDAO;

/**
 * Action for generating the grid for profile assignments
 */
public class GetProfileAssignmentsAction extends ServiceableAction
{
    /** The extension point for right assignment contexts */
    protected RightAssignmentContextExtensionPoint _rightAssignmentContextEP;
    /** The right manager */
    protected RightManager _rightManager;
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
        _rightAssignmentContextEP = (RightAssignmentContextExtensionPoint) smanager.lookup(RightAssignmentContextExtensionPoint.ROLE);
        _rightManager = (RightManager) smanager.lookup(RightManager.ROLE);
        _userPopulationDAO = (UserPopulationDAO) smanager.lookup(UserPopulationDAO.ROLE);
        _userManager = (UserManager) smanager.lookup(UserManager.ROLE);
        _groupDirectoryDAO = (GroupDirectoryDAO) smanager.lookup(GroupDirectoryDAO.ROLE);
        _groupManager = (GroupManager) smanager.lookup(GroupManager.ROLE);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map<String, Object> result = new HashMap<>();
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        Map jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        String rightAssignmentContextId = (String) jsParameters.get("rightAssignmentContextId");
        
        List<Object> jsParentContexts = (List<Object>) jsParameters.get("parentContexts"); // order is important, the first item is the direct parent, etc.
        Object jsContext = jsParameters.get("context");
        
        List<Map<String, Object>> assignments = new ArrayList<>();
        
        Object context = _rightAssignmentContextEP.getExtension(rightAssignmentContextId).convertJSContext(jsContext);
        List<Object> parentContexts = jsParentContexts.stream()
                .map(parentContext -> _rightAssignmentContextEP.getExtension(rightAssignmentContextId).convertJSContext(parentContext))
                .collect(Collectors.toList());
        assignments.add(_getAnonymousAssignmentWithInheritance(context, parentContexts));
        assignments.add(_getAnyConnectedAssignmentWithInheritance(context, parentContexts));
        assignments.addAll(_getUserAssignmentsWithInheritance(context, parentContexts));
        assignments.addAll(_getGroupAssignmentsWithInheritance(context, parentContexts));
        result.put("assignments", assignments);
        
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        
        return EMPTY_MAP;
    }
    
    private Map<String, Object> _getAnonymousAssignmentWithInheritance(Object context, List<Object> parentContexts)
    {
        Map<String, Object> assignment = new HashMap<>();
        
        assignment.put("assignmentType", "1-anonymous");
        
        // First ask on context object
        _getAnonymousAssignment(assignment, context, "localAllow", "localDeny");
        
        // Then ask on parent contexts
        for (Object parentContext : parentContexts)
        {
            _getAnonymousAssignment(assignment, parentContext, "inheritAllow", "inheritDeny");
        }
        
        return assignment;
    }
    
    private void _getAnonymousAssignment(Map<String, Object> assignment, Object context, String allow, String deny)
    {
        Set<String> deniedProfilesForAnonymous = _rightManager.getDeniedProfilesForAnonymous(context);
        for (String profileId : deniedProfilesForAnonymous)
        {
            if (!assignment.containsKey(profileId) && _rightManager.getProfile(profileId) != null)
            {
                assignment.put(profileId, deny);
            }
        }
        
        Set<String> allowedProfilesForAnonymous = _rightManager.getAllowedProfilesForAnonymous(context);
        for (String profileId : allowedProfilesForAnonymous)
        {
            if (!assignment.containsKey(profileId) && _rightManager.getProfile(profileId) != null)
            {
                assignment.put(profileId, allow);
            }
        }
        
    }
    
    private Map<String, Object> _getAnyConnectedAssignmentWithInheritance(Object context, List<Object> parentContexts)
    {
        Map<String, Object> assignment = new HashMap<>();
        
        assignment.put("assignmentType", "2-anyconnected");
        
        // First ask on context object
        _getAnyConnectedAssignment(assignment, context, "localAllow", "localDeny");
        
        // Then ask on parent contexts
        for (Object parentContext : parentContexts)
        {
            _getAnyConnectedAssignment(assignment, parentContext, "inheritAllow", "inheritDeny");
        }
        
        return assignment;
    }
    
    private void _getAnyConnectedAssignment(Map<String, Object> assignment, Object context, String allow, String deny)
    {
        Set<String> deniedProfilesForAnyConnectedUser = _rightManager.getDeniedProfilesForAnyConnectedUser(context);
        for (String profileId : deniedProfilesForAnyConnectedUser)
        {
            if (!assignment.containsKey(profileId) && _rightManager.getProfile(profileId) != null)
            {
                assignment.put(profileId, deny);
            }
        }
        
        Set<String> allowedProfilesForAnyConnectedUser = _rightManager.getAllowedProfilesForAnyConnectedUser(context);
        for (String profileId : allowedProfilesForAnyConnectedUser)
        {
            if (!assignment.containsKey(profileId) && _rightManager.getProfile(profileId) != null)
            {
                assignment.put(profileId, allow);
            }
        }
    }
    
    private List<Map<String, Object>> _getUserAssignmentsWithInheritance(Object context, List<Object> parentContexts)
    {
        Map<UserIdentity, Map<String, Object>> assignments = new LinkedHashMap<>();
        
        // First ask on context object
        _getUserAssignments(assignments, context, "localAllow", "localDeny");
        
        // Then ask on parent contexts
        for (Object parentContext : parentContexts)
        {
            _getUserAssignments(assignments, parentContext, "inheritAllow", "inheritDeny");
        }
        
        return new ArrayList<>(assignments.values());
    }
    
    private void _getUserAssignments(Map<UserIdentity, Map<String, Object>> assignments, Object context, String allow, String deny)
    {
        Map<UserIdentity, Set<String>> deniedProfilesForUsers = _rightManager.getDeniedProfilesForUsers(context);
        for (UserIdentity userIdentity : deniedProfilesForUsers.keySet())
        {
            Map<String, Object> assignment;
            if (assignments.containsKey(userIdentity))
            {
                assignment = assignments.get(userIdentity);
            }
            else
            {
                assignment = _getUserInfo(userIdentity);
            }
            
            for (String profileId : deniedProfilesForUsers.get(userIdentity))
            {
                if (!assignment.containsKey(profileId) && _rightManager.getProfile(profileId) != null)
                {
                    assignment.put(profileId, deny);
                }
            }
            
            assignments.put(userIdentity, assignment);
        }
        
        Map<UserIdentity, Set<String>> allowedProfilesForUsers = _rightManager.getAllowedProfilesForUsers(context);
        for (UserIdentity userIdentity : allowedProfilesForUsers.keySet())
        {
            Map<String, Object> assignment;
            if (assignments.containsKey(userIdentity))
            {
                assignment = assignments.get(userIdentity);
            }
            else
            {
                assignment = _getUserInfo(userIdentity);
            }
            
            for (String profileId : allowedProfilesForUsers.get(userIdentity))
            {
                if (!assignment.containsKey(profileId) && _rightManager.getProfile(profileId) != null)
                {
                    assignment.put(profileId, allow);
                }
            }
            
            assignments.put(userIdentity, assignment);
        }
    }
    
    private Map<String, Object> _getUserInfo(UserIdentity userIdentity)
    {
        Map<String, Object> assignment = new HashMap<>();
        assignment.put("assignmentType", "3-users");
        
        String login = userIdentity.getLogin();
        String populationId = userIdentity.getPopulationId();
        assignment.put("login", login);
        assignment.put("population", populationId);
        assignment.put("populationLabel", _userPopulationDAO.getUserPopulation(populationId).getLabel());
        assignment.put("groups", _groupManager.getUserGroups(userIdentity).stream()
                                    .map(this::_groupToJson)
                                    .collect(Collectors.toList()));
        
        User user = _userManager.getUser(populationId, login);
        assignment.put("userSortableName", user.getSortableName());
        
        return assignment;
    }
    
    private List<Map<String, Object>> _getGroupAssignmentsWithInheritance(Object context, List<Object> parentContexts)
    {
        Map<GroupIdentity, Map<String, Object>> assignments = new LinkedHashMap<>();
        
        // First ask on context object
        _getGroupAssignments(assignments, context, "localAllow", "localDeny");
        
        // Then ask on parent contexts
        for (Object parentContext : parentContexts)
        {
            _getGroupAssignments(assignments, parentContext, "inheritAllow", "inheritDeny");
        }
        
        return new ArrayList<>(assignments.values());
    }
    
    private void _getGroupAssignments(Map<GroupIdentity, Map<String, Object>> assignments, Object context, String allow, String deny)
    {
        Map<GroupIdentity, Set<String>> allowedProfilesForGroups = _rightManager.getAllowedProfilesForGroups(context);
        
        for (GroupIdentity groupIdentity : allowedProfilesForGroups.keySet())
        {
            Map<String, Object> assignment;
            if (assignments.containsKey(groupIdentity))
            {
                assignment = assignments.get(groupIdentity);
            }
            else
            {
                assignment = _getGroupInfo(groupIdentity);
            }
            
            for (String profileId : allowedProfilesForGroups.get(groupIdentity))
            {
                if (!assignment.containsKey(profileId) && _rightManager.getProfile(profileId) != null)
                {
                    assignment.put(profileId, allow);
                }
            }
            
            assignments.put(groupIdentity, assignment);
        }
        
        Map<GroupIdentity, Set<String>> deniedProfilesForGroups = _rightManager.getDeniedProfilesForGroups(context);
        for (GroupIdentity groupIdentity : deniedProfilesForGroups.keySet())
        {
            Map<String, Object> assignment;
            if (assignments.containsKey(groupIdentity))
            {
                assignment = assignments.get(groupIdentity);
            }
            else
            {
                assignment = _getGroupInfo(groupIdentity);
            }
            
            for (String profileId : deniedProfilesForGroups.get(groupIdentity))
            {
                if (!assignment.containsKey(profileId) && _rightManager.getProfile(profileId) != null)
                {
                    assignment.put(profileId, deny);
                }
            }
            
            assignments.put(groupIdentity, assignment);
        }
    }
    
    private Map<String, Object> _getGroupInfo(GroupIdentity groupIdentity)
    {
        Map<String, Object> assignment = new HashMap<>();
        assignment.put("assignmentType", "4-groups");
        
        String groupId = groupIdentity.getId();
        String directoryId = groupIdentity.getDirectoryId();
        assignment.put("groupId", groupId);
        assignment.put("groupDirectory", directoryId);
        assignment.put("groupDirectoryLabel", _groupDirectoryDAO.getGroupDirectory(directoryId).getLabel());
        
        Group group = _groupManager.getGroup(directoryId, groupId);
        assignment.put("groupLabel", group.getLabel());
        
        return assignment;
    }
    
    private Map<String, Object> _groupToJson(GroupIdentity group)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("groupId", group.getId());
        result.put("groupDirectory", group.getDirectoryId());
        return result;
    }
}
