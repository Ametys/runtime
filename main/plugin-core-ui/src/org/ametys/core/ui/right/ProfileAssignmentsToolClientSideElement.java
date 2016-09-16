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
package org.ametys.core.ui.right;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.core.group.GroupDirectoryDAO;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.group.GroupManager;
import org.ametys.core.right.ProfileAssignmentStorageExtensionPoint;
import org.ametys.core.right.RightAssignmentContext;
import org.ametys.core.right.RightAssignmentContextExtensionPoint;
import org.ametys.core.right.RightManager.RightResult;
import org.ametys.core.right.RightsException;
import org.ametys.core.ui.Callable;
import org.ametys.core.ui.ClientSideElement;
import org.ametys.core.ui.ClientSideElementHelper;
import org.ametys.core.ui.StaticClientSideElement;
import org.ametys.core.user.UserIdentity;

import com.google.common.collect.Sets;

/**
 * {@link ClientSideElement} for the tool displaying the profile assignments
 */
public class ProfileAssignmentsToolClientSideElement extends StaticClientSideElement
{
    /** The profile assignment storage component */
    protected ProfileAssignmentStorageExtensionPoint _profileAssignmentStorageEP;
    /** The extension point for right assignment contexts */
    protected RightAssignmentContextExtensionPoint _rightAssignmentContextEP;
    /** The DAO for group directories */
    protected GroupDirectoryDAO _groupDirectoryDAO;
    /** The group manager */
    protected GroupManager _groupManager;
    
    /**
     * Enumeration of all possible access types
     */
    public enum AccessType
    {
        /**
         * Indicates that the access is allowed
         */
        ALLOW 
        {
            @Override
            public String toString()
            {
                return "allow";
            }
        },
        /**
         * Indicates that the access is denied
         */
        DENY 
        {
            @Override
            public String toString()
            {
                return "deny";
            }
        },
        /**
         * Indicates that the access is allowed by inheritance
         */
        INHERITED_ALLOW 
        {
            @Override
            public String toString()
            {
                return "inherited_allow";
            }
        },
        /**
         * Indicates that the access is denied by inheritance
         */
        INHERITED_DENY 
        {
            @Override
            public String toString()
            {
                return "inherited_deny";
            }
        },
        /**
         * Indicates that the access can not be determined
         */
        UNKNOWN 
        {
            @Override
            public String toString()
            {
                return "unknown";
            }
        }
    }
    
    /**
     * Enumeration of all possible target types
     */
    public enum TargetType 
    {
        /**
         * Indicates that the target is the anonymous user
         */
        ANONYMOUS 
        {
            @Override
            public String toString()
            {
                return "anonymous";
            }
        },
        /**
         * Indicates that the target is the anonymous user
         */
        ANYCONNECTED_USER 
        {
            @Override
            public String toString()
            {
                return "anyconnected_user";
            }
        },
        /**
         * Indicates that the target is a user
         */
        USER 
        {
            @Override
            public String toString()
            {
                return "user";
            }
        },
        /**
         * Indicates that the target is a group
         */
        GROUP 
        {
            @Override
            public String toString()
            {
                return "group";
            }
        }
    }

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _profileAssignmentStorageEP = (ProfileAssignmentStorageExtensionPoint) smanager.lookup(ProfileAssignmentStorageExtensionPoint.ROLE);
        _rightAssignmentContextEP = (RightAssignmentContextExtensionPoint) smanager.lookup(RightAssignmentContextExtensionPoint.ROLE);
        _groupDirectoryDAO = (GroupDirectoryDAO) smanager.lookup(GroupDirectoryDAO.ROLE);
        _groupManager = (GroupManager) smanager.lookup(GroupManager.ROLE);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<Script> getScripts(boolean ignoreRights, Map<String, Object> contextParameters)
    {
        List<Script> scripts = super.getScripts(ignoreRights, contextParameters);
        
        if (scripts.size() > 0)
        {
            Script script = ClientSideElementHelper.cloneScript(scripts.get(0));
            
            Map<String, Object> jsClasses = new HashMap<>();
            script.getParameters().put("classes", jsClasses);
            
            boolean excludePrivate = true;
            Set<String> rightContextIds = _rightAssignmentContextEP.getExtensionsIds();
            if (script.getParameters().containsKey("right-contexts"))
            {
                excludePrivate = false;
                // Restrict the right contexts to the configured right contexts if exist
                Object ctxConfig = ((Map<String, Object>) script.getParameters().get("right-contexts")).get("right-context");
                if (ctxConfig instanceof List)
                {
                    rightContextIds = new HashSet<>((List<String>) ctxConfig);
                }
                else
                {
                    rightContextIds = Sets.newHashSet((String) ctxConfig);
                }
            }
            
            for (String rightContextId: rightContextIds)
            {
                RightAssignmentContext rightAssignmentContext = _rightAssignmentContextEP.getExtension(rightContextId);
                
                if (!excludePrivate || !rightAssignmentContext.isPrivate())
                {
                    List<Script> rightAssignmentContextScripts = rightAssignmentContext.getScripts(ignoreRights, contextParameters);
                    int index = 0;
                    for (Script rightAssignmentContextScript: rightAssignmentContextScripts)
                    {
                        Map<String, Object> classInfo = new HashMap<>();
                        classInfo.put("className", rightAssignmentContextScript.getScriptClassname());
                        classInfo.put("serverId", rightContextId);
                        classInfo.put("parameters", rightAssignmentContextScript.getParameters());
                        jsClasses.put(rightContextId + "-" + index++, classInfo);
                        
                        script.getScriptFiles().addAll(rightAssignmentContextScript.getScriptFiles());
                        script.getCSSFiles().addAll(rightAssignmentContextScript.getCSSFiles());
                    }
                }
                
            }
            
            scripts = new ArrayList<>();
            scripts.add(script);
        }
        
        return scripts;
    }
    
    /**
     * Gets the groups of a user as JSON
     * @param login The login of the user
     * @param population The population of the user
     * @return the groups of a user as JSON
     */
    @Callable
    public List<Map<String, Object>> getUserGroups(String login, String population)
    {
        return _groupManager.getUserGroups(login, population).stream()
            .map(this::_groupToJson)
            .collect(Collectors.toList());
    }
    
    private Map<String, Object> _groupToJson(GroupIdentity groupIdentity)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("groupId", groupIdentity.getId());
        result.put("groupDirectory", groupIdentity.getDirectoryId());
        return result;
    }
    
    /**
     * Save some changes made client-side.
     * @param rightAssignmentCtxId The id of the right assignment context
     * @param jsContext The JS object context
     * @param assignmentsInfo The list of all the changes to make. Each map in the list must contain the following keys:
     * <ol>
     * <li><b>profileId</b> for the id of the profile (as a string)</li>
     * <li><b>assignment</b> for the kind of assignment (can be ACCESS_TYPE_ALLOW, ACCESS_TYPE_DENY...)</li>
     * <li><b>assignmentType</b> expects one of these four strings: "user", "group", "anonymous", "anyConnectedUser"</li>
     * <li><b>identity</b> Can be null if assignmentType is "anonymous" or "anyConnectedUser". If "user", must be a map with the keys "login" and "population". If "group", must be a map with the keys "groupId" and "groupDirectory"</li>
     * </ol>
     */
    @SuppressWarnings("unchecked")
    @Callable
    public void saveChanges(String rightAssignmentCtxId, Object jsContext, List<Map<String, Object>> assignmentsInfo)
    {
        if (_rightManager.hasRight(_currentUserProvider.getUser(), "Runtime_Rights_Rights_Handle", "/contributor") != RightResult.RIGHT_ALLOW)
        {
            throw new RightsException("The user '" + _currentUserProvider.getUser().getLogin() + "' try to assign profile without sufficient rights");
        }
        
        Object context = _rightAssignmentContextEP.getExtension(rightAssignmentCtxId).convertJSContext(jsContext);
        for (Map<String, Object> assignmentInfo : assignmentsInfo)
        {
            String profileId = (String) assignmentInfo.get("profileId");
            String assignment = (String) assignmentInfo.get("assignment");
            String targetType = (String) assignmentInfo.get("targetType");
            Map<String, String> identity = (Map<String, String>) assignmentInfo.get("identity");
            _saveChange(context, profileId, assignment, targetType, identity);
        }
    }
    
    /**
     * Get the first permission given by inheritance for a object context and profiles
     * @param rightAssignmentCtxId The id of the right assignment context
     * @param jsContext The JS object context
     * @param profileIds The list of profiles
     * @param targetType The type of target : anonymous, any connected users, a user or a group
     * @param identity The identity of the target. Can be null if the target is anonymous or any connected users
     * @return The first access type given by inheritance for each profile
     */
    @Callable
    public Map<String, String> getInheritedAssignments (String rightAssignmentCtxId, Object jsContext, List<String> profileIds, String targetType, Map<String, String> identity)
    {
        Map<String, String> assignments = new HashMap<>();
        
        for (String profileId : profileIds)
        {
            assignments.put(profileId, getInheritedAssignment(rightAssignmentCtxId, jsContext, profileId, targetType, identity));
        }
        return assignments;
    }
    
    /**
     * Get the first permission given by inheritance for a object context and a specific profile
     * @param rightAssignmentCtxId The id of the right assignment context
     * @param jsContext The JS object context
     * @param profileId The id of profile 
     * @param targetType The type of target : anonymous, any connected users, a user or a group
     * @param identity The identity of the target. Can be null if the target is anonymous or any connected users
     * @return The first access type given by inheritance
     */
    @Callable
    public String getInheritedAssignment (String rightAssignmentCtxId, Object jsContext, String profileId, String targetType, Map<String, String> identity)
    {
        RightAssignmentContext rightCtx = _rightAssignmentContextEP.getExtension(rightAssignmentCtxId);
        Object context = rightCtx.convertJSContext(jsContext);
        
        switch (TargetType.valueOf(targetType.toUpperCase()))
        {
            case ANONYMOUS:
                return _getInheritedAssignmentForAnonymous(rightCtx, context, profileId);
            case ANYCONNECTED_USER:
                return _getInheritedAssignmentForAnyconnected(rightCtx, context, profileId);
            case USER:
                UserIdentity user = new UserIdentity(identity.get("login"), identity.get("population"));
                return _getInheritedAssignmentForUser(rightCtx, context, profileId, user);
            case GROUP:
                GroupIdentity group = new GroupIdentity(identity.get("groupId"), identity.get("groupDirectory"));
                return _getInheritedAssignmentForGroup(rightCtx, context, profileId, group);
            default:
                return AccessType.UNKNOWN.toString();
        }
    }
    
    private String _getInheritedAssignmentForAnonymous (RightAssignmentContext extension, Object context, String profileId)
    {
        Object parentContext = extension.getParentContext(context);
        while (parentContext != null)
        {
            Set<String> deniedProfiles = _profileAssignmentStorageEP.getDeniedProfilesForAnonymous(parentContext);
            if (deniedProfiles.contains(profileId))
            {
                return AccessType.INHERITED_DENY.toString();
            }
            
            Set<String> allowedProfiles = _profileAssignmentStorageEP.getAllowedProfilesForAnonymous(parentContext);
            if (allowedProfiles.contains(profileId))
            {
                return AccessType.INHERITED_ALLOW.toString();
            }
            
            parentContext = extension.getParentContext(parentContext);
        }
        
        return AccessType.UNKNOWN.toString();
    }
    
    private String _getInheritedAssignmentForAnyconnected (RightAssignmentContext extension, Object context, String profileId)
    {
        Object parentContext = extension.getParentContext(context);
        while (parentContext != null)
        {
            Set<String> deniedProfiles = _profileAssignmentStorageEP.getDeniedProfilesForAnyConnectedUser(parentContext);
            if (deniedProfiles.contains(profileId))
            {
                return AccessType.INHERITED_DENY.toString();
            }
            
            Set<String> allowedProfiles = _profileAssignmentStorageEP.getAllowedProfilesForAnyConnectedUser(parentContext);
            if (allowedProfiles.contains(profileId))
            {
                return AccessType.INHERITED_ALLOW.toString();
            }
            
            parentContext = extension.getParentContext(parentContext);
        }
        
        return AccessType.UNKNOWN.toString();
    }
    
    private String _getInheritedAssignmentForUser (RightAssignmentContext extension, Object context, String profileId, UserIdentity user)
    {
        Object parentContext = extension.getParentContext(context);
        while (parentContext != null)
        {
            // FIXME Optimization _profileAssignmentStorageEP.getDeniedProfilesForUser(parentContext, user)
            Map<UserIdentity, Set<String>> deniedProfiles = _profileAssignmentStorageEP.getDeniedProfilesForUsers(parentContext);
            if (deniedProfiles.containsKey(user) && deniedProfiles.get(user).contains(profileId))
            {
                return AccessType.INHERITED_DENY.toString();
            }
            
            Map<UserIdentity, Set<String>> allowedProfiles = _profileAssignmentStorageEP.getAllowedProfilesForUsers(parentContext);
            if (allowedProfiles.containsKey(user) && allowedProfiles.get(user).contains(profileId))
            {
                return AccessType.INHERITED_ALLOW.toString();
            }
            
            parentContext = extension.getParentContext(parentContext);
        }
        
        return AccessType.UNKNOWN.toString();
    }
    
    private String _getInheritedAssignmentForGroup (RightAssignmentContext extension, Object context, String profileId, GroupIdentity group)
    {
        Object parentContext = extension.getParentContext(context);
        while (parentContext != null)
        {
            // FIXME Optimization _profileAssignmentStorageEP.getDeniedProfilesForUser(parentContext, user)
            Map<GroupIdentity, Set<String>> deniedProfiles = _profileAssignmentStorageEP.getDeniedProfilesForGroups(parentContext);
            if (deniedProfiles.containsKey(group) && deniedProfiles.get(group).contains(profileId))
            {
                return AccessType.INHERITED_DENY.toString();
            }
            
            Map<GroupIdentity, Set<String>> allowedProfiles = _profileAssignmentStorageEP.getAllowedProfilesForGroups(parentContext);
            if (allowedProfiles.containsKey(group) && allowedProfiles.get(group).contains(profileId))
            {
                return AccessType.INHERITED_ALLOW.toString();
            }
            
            parentContext = extension.getParentContext(parentContext);
        }
        
        return AccessType.UNKNOWN.toString();
    }
    
    private void _saveChange(Object context, String profileId, String assignment, String targetType, Map<String, String> identity)
    {
        AccessType accessType = assignment != null ? AccessType.valueOf(assignment.toUpperCase()) : AccessType.UNKNOWN;
        switch (TargetType.valueOf(targetType.toUpperCase()))
        {
            case ANONYMOUS:
                switch (accessType)
                {
                    case ALLOW:
                        _profileAssignmentStorageEP.removeDeniedProfileFromAnonymous(profileId, context);
                        _profileAssignmentStorageEP.allowProfileToAnonymous(profileId, context);
                        break;
                    case DENY:
                        _profileAssignmentStorageEP.removeAllowedProfileFromAnonymous(profileId, context);
                        _profileAssignmentStorageEP.denyProfileToAnonymous(profileId, context);
                        break;
                    default:
                        _profileAssignmentStorageEP.removeAllowedProfileFromAnonymous(profileId, context);
                        _profileAssignmentStorageEP.removeDeniedProfileFromAnonymous(profileId, context);
                        break;
                }
                break;
                
            case ANYCONNECTED_USER:
                switch (accessType)
                {
                    case ALLOW:
                        _profileAssignmentStorageEP.removeDeniedProfileFromAnyConnectedUser(profileId, context);
                        _profileAssignmentStorageEP.allowProfileToAnyConnectedUser(profileId, context);
                        break;
                    case DENY:
                        _profileAssignmentStorageEP.removeAllowedProfileFromAnyConnectedUser(profileId, context);
                        _profileAssignmentStorageEP.denyProfileToAnyConnectedUser(profileId, context);
                        break;
                    default:
                        _profileAssignmentStorageEP.removeAllowedProfileFromAnyConnectedUser(profileId, context);
                        _profileAssignmentStorageEP.removeDeniedProfileFromAnyConnectedUser(profileId, context);
                        break;
                }
                break;
                
            case USER:
                UserIdentity user = new UserIdentity(identity.get("login"), identity.get("population"));
                switch (accessType)
                {
                    case ALLOW:
                        _profileAssignmentStorageEP.removeDeniedProfileFromUser(user, profileId, context);
                        _profileAssignmentStorageEP.allowProfileToUser(user, profileId, context);
                        break;
                    case DENY:    
                        _profileAssignmentStorageEP.removeAllowedProfileFromUser(user, profileId, context);
                        _profileAssignmentStorageEP.denyProfileToUser(user, profileId, context);
                        break;
                    default:
                        _profileAssignmentStorageEP.removeAllowedProfileFromUser(user, profileId, context);
                        _profileAssignmentStorageEP.removeDeniedProfileFromUser(user, profileId, context);
                        break;
                }
                break;
                
            case GROUP:
                GroupIdentity group = new GroupIdentity(identity.get("groupId"), identity.get("groupDirectory"));
                switch (accessType)
                {
                    case ALLOW:
                        _profileAssignmentStorageEP.removeDeniedProfileFromGroup(group, profileId, context);
                        _profileAssignmentStorageEP.allowProfileToGroup(group, profileId, context);
                        break;
                    case DENY:  
                        _profileAssignmentStorageEP.removeAllowedProfileFromGroup(group, profileId, context);
                        _profileAssignmentStorageEP.denyProfileToGroup(group, profileId, context);
                        break;
                    default:
                        _profileAssignmentStorageEP.removeAllowedProfileFromGroup(group, profileId, context);
                        _profileAssignmentStorageEP.removeDeniedProfileFromGroup(group, profileId, context);
                        break;
                }
                break;
            default:
                break;
        }
    }
    
    
}
