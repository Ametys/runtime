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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.core.group.GroupDirectoryDAO;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.group.GroupManager;
import org.ametys.core.right.RightAssignmentContext;
import org.ametys.core.right.RightAssignmentContextExtensionPoint;
import org.ametys.core.right.RightManager.RightResult;
import org.ametys.core.right.RightsException;
import org.ametys.core.ui.Callable;
import org.ametys.core.ui.ClientSideElement;
import org.ametys.core.ui.StaticClientSideElement;
import org.ametys.core.user.UserIdentity;

/**
 * {@link ClientSideElement} for the tool displaying the profile assignments
 */
public class ProfileAssignmentsToolClientSideElement extends StaticClientSideElement
{
    /** Allowed access */
    public static final String ACCESS_TYPE_ALLOW = "allow";
    /** Denied access */
    public static final String ACCESS_TYPE_DENY = "deny";
    /** Allowed access by inheritance */
    public static final String ACCESS_TYPE_INHERITED_ALLOW = "inherited-allow";
    /** Denied access by inheritance */
    public static final String ACCESS_TYPE_INHERITED_DENY = "inherited-deny";
    
    /** The extension point for right assignment contexts */
    protected RightAssignmentContextExtensionPoint _rightAssignmentContextEP;
    /** The DAO for group directories */
    protected GroupDirectoryDAO _groupDirectoryDAO;
    /** The group manager */
    protected GroupManager _groupManager;

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _rightAssignmentContextEP = (RightAssignmentContextExtensionPoint) smanager.lookup(RightAssignmentContextExtensionPoint.ROLE);
        _groupDirectoryDAO = (GroupDirectoryDAO) smanager.lookup(GroupDirectoryDAO.ROLE);
        _groupManager = (GroupManager) smanager.lookup(GroupManager.ROLE);
    }
    
    @Override
    protected Script _configureScript(Configuration configuration) throws ConfigurationException
    {
        Script script = super._configureScript(configuration);
        
        // Scripts of the tool
        List<ScriptFile> scriptsImports = new ArrayList<>(script.getScriptFiles());
        List<ScriptFile> cssImports = new ArrayList<>(script.getCSSFiles());
        
        // Then, for each right assignment context, add its own scripts
        _rightAssignmentContextEP.getExtensionsIds().stream()
            .map(_rightAssignmentContextEP::getExtension)
            .forEach(context -> this._addScripts(context, scriptsImports, cssImports));
        
        return new Script(script.getId(), script.getScriptClassname(), scriptsImports, cssImports, script.getParameters());
    }
    
    private void _addScripts(RightAssignmentContext rightAssignmentContext, List<ScriptFile> scriptsImports, List<ScriptFile> cssImports)
    {
        List<Script> scripts = rightAssignmentContext.getScripts(Collections.EMPTY_MAP);
        
        List<ScriptFile> contextScriptFilesImports = scripts.stream()
                .map(Script::getScriptFiles)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        scriptsImports.addAll(contextScriptFilesImports);
        
        List<ScriptFile> contextCssFilesImports = scripts.stream()
                .map(Script::getCSSFiles)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        cssImports.addAll(contextCssFilesImports);
    }
    
    /**
     * Gets the contexts in JSON format
     * @return the contexts in JSON format
     */
    public List<Map<String, Object>> getContextsAsJson()
    {
        return _rightAssignmentContextEP.getExtensionsIds().stream()
            .map(_rightAssignmentContextEP::getExtension)
            .map(this::_getContextMap)
            .collect(Collectors.toList());
    }
    
    private Map<String, Object> _getContextMap(RightAssignmentContext context)
    {
        Map<String, Object> result = new HashMap<>();
        result.put("value", context.getId());
        List<Script> scripts = context.getScripts(Collections.EMPTY_MAP);
        result.put("displayText", scripts.get(0).getParameters().get("label"));
        return result;
    }
    
    /**
     * Gets the names of the JavaScript class name for each right assignment context
     * @return the names of the JavaScript class name for each right assignment context
     */
    @Callable
    public Map<String, String> getJSClassNames()
    {
        return _rightAssignmentContextEP.getExtensionsIds().stream()
                .collect(Collectors.toMap(Function.identity(), this::_getJSClassName));
    }
    
    private String _getJSClassName(String rightAssignmentContextId)
    {
        List<Script> scripts = _rightAssignmentContextEP.getExtension(rightAssignmentContextId).getScripts(Collections.EMPTY_MAP);
        return scripts.get(0).getScriptClassname();
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
     * @param rightAssignmentId The id of the right assignment context extesnion
     * @param objectContext The JS object context
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
    public void saveChanges(String rightAssignmentId, Object objectContext, List<Map<String, Object>> assignmentsInfo)
    {
        if (_rightManager.hasRight(_currentUserProvider.getUser(), "Runtime_Rights_Rights_Handle", "/contributor") != RightResult.RIGHT_ALLOW)
        {
            throw new RightsException("Insufficient rights to assign profiles");
        }
        
        Object context = _rightAssignmentContextEP.getExtension(rightAssignmentId).convertJSContext(objectContext);
        for (Map<String, Object> assignmentInfo : assignmentsInfo)
        {
            String profileId = (String) assignmentInfo.get("profileId");
            String assignment = (String) assignmentInfo.get("assignment");
            String assignmentType = (String) assignmentInfo.get("assignmentType");
            Map<String, String> identity = (Map<String, String>) assignmentInfo.get("identity");
            _saveChange(context, profileId, assignment, assignmentType, identity);
        }
    }
    
    private void _saveChange(Object context, String profileId, String assignment, String assignmentType, Map<String, String> identity)
    {
        switch (assignmentType)
        {
            case "anonymous":
                if (ACCESS_TYPE_ALLOW.equals(assignment))
                {
                    _rightManager.removeDeniedProfileFromAnonymous(profileId, context);
                    _rightManager.allowProfileToAnonymous(profileId, context);
                }
                else if (ACCESS_TYPE_DENY.equals(assignment))
                {
                    _rightManager.removeAllowedProfileFromAnonymous(profileId, context);
                    _rightManager.denyProfileToAnonymous(profileId, context);
                }
                else
                {
                    _rightManager.removeAllowedProfileFromAnonymous(profileId, context);
                    _rightManager.removeDeniedProfileFromAnonymous(profileId, context);
                }
                break;
                
            case "anyConnectedUser":
                if (ACCESS_TYPE_ALLOW.equals(assignment))
                {
                    _rightManager.removeDeniedProfileFromAnyConnectedUser(profileId, context);
                    _rightManager.allowProfileToAnyConnectedUser(profileId, context);
                }
                else if (ACCESS_TYPE_DENY.equals(assignment))
                {
                    _rightManager.removeAllowedProfileFromAnyConnectedUser(profileId, context);
                    _rightManager.denyProfileToAnyConnectedUser(profileId, context);
                }
                else
                {
                    _rightManager.removeAllowedProfileFromAnyConnectedUser(profileId, context);
                    _rightManager.removeDeniedProfileFromAnyConnectedUser(profileId, context);
                }
                break;
                
            case "user":
                UserIdentity user = new UserIdentity(identity.get("login"), identity.get("population"));
                if (ACCESS_TYPE_ALLOW.equals(assignment))
                {
                    _rightManager.removeDeniedProfileFromUser(user, profileId, context);
                    _rightManager.allowProfileToUser(user, profileId, context);
                }
                else if (ACCESS_TYPE_DENY.equals(assignment))
                {
                    _rightManager.removeAllowedProfileFromUser(user, profileId, context);
                    _rightManager.denyProfileToUser(user, profileId, context);
                }
                else
                {
                    _rightManager.removeAllowedProfileFromUser(user, profileId, context);
                    _rightManager.removeDeniedProfileFromUser(user, profileId, context);
                }
                break;
                
            case "group":
            default:
                GroupIdentity group = new GroupIdentity(identity.get("groupId"), identity.get("groupDirectory"));
                if (ACCESS_TYPE_ALLOW.equals(assignment))
                {
                    _rightManager.removeDeniedProfileFromGroup(group, profileId, context);
                    _rightManager.allowProfileToGroup(group, profileId, context);
                }
                else if (ACCESS_TYPE_DENY.equals(assignment))
                {
                    _rightManager.removeAllowedProfileFromGroup(group, profileId, context);
                    _rightManager.denyProfileToGroup(group, profileId, context);
                }
                else
                {
                    _rightManager.removeAllowedProfileFromGroup(group, profileId, context);
                    _rightManager.removeDeniedProfileFromGroup(group, profileId, context);
                }
                break;
        }
    }
}
