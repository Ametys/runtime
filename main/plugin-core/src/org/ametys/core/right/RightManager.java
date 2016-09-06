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
package org.ametys.core.right;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.core.group.GroupDirectoryDAO;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.group.GroupListener;
import org.ametys.core.group.GroupManager;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.group.directory.ModifiableGroupDirectory;
import org.ametys.core.right.AccessController.AccessResult;
import org.ametys.core.right.AccessController.AccessResultContext;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserListener;
import org.ametys.core.user.UserManager;
import org.ametys.core.user.directory.ModifiableUserDirectory;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;
import org.ametys.runtime.request.RequestListener;
import org.ametys.runtime.request.RequestListenerManager;
import org.ametys.runtime.workspaces.admin.authentication.AdminAuthenticateAction;

/**
 * Abstraction for testing a right associated with a resource and a user from a single source.
 */
public class RightManager extends AbstractLogEnabled implements UserListener, GroupListener, Serviceable, Configurable, Initializable, RequestListener, ThreadSafe, Component, Contextualizable
{
    /** For avalon service manager */
    public static final String ROLE = RightManager.class.getName();
    
    /** The id of the READER profile */
    public static final String READER_PROFILE_ID = "READER";
    
    /** Avalon ServiceManager */
    protected ServiceManager _manager;
    /** Avalon SourceResolver */
    protected SourceResolver _resolver;
    /** The rights' list container */
    protected RightsExtensionPoint _rightsEP;
    /** The extension point for the profile assignement storages */
    protected ProfileAssignmentStorageExtensionPoint _profileAssignmentStorageEP;
    /** The extension point for the Right Context Convertors */
    protected RightContextConvertorExtensionPoint _rightContextConvertorEP;
    /** The extension point for Access Controllers */
    protected AccessControllerExtensionPoint _accessControllerEP;
    /** The user manager */
    protected UserManager _userManager;
    /** The group manager */
    protected GroupManager _groupManager;
    /** The DAO for user populations */
    protected UserPopulationDAO _userPopulationDAO;
    /** The DAO for group directories */
    protected GroupDirectoryDAO _groupDirectoryDAO;
    /** The current user provider */
    protected CurrentUserProvider _currentUserProvider;
    /** The rights DAO */
    protected RightProfilesDAO _profilesDAO;
    
    /**
     * This first cache is for right result on non-null contexts when calling {@link #hasRight(UserIdentity, String, Object)}
     * 
     * { UserIdentity : {[ProfileIds] : { Context : RightResult
     *                             }
     *                  }
     * }
     * 
     * We are caching the set of profiles instead of right id because many rights belong to the exact same profiles
     */
    private final ThreadLocal<Map<UserIdentity, Map<Set<String>, Map<Object, RightResult>>>> _cacheTL = new ThreadLocal<>();
    
    /**
     * This second cache is for right result on null contexts when calling {@link #hasRight(UserIdentity, String, Object)}
     * 
     * { UserIdentity : {[ProfileIds] : RightResult
     *                  }
     * }
     * 
     * We are caching the set of profiles instead of right id because many rights belong to the exact same profiles
     */
    private final ThreadLocal<Map<UserIdentity, Map<Set<String>, RightResult>>> _cache2TL = new ThreadLocal<>();

    private Context _context;
    
    /**
     * Enumeration of all possible values returned by hasRight(user, right, context)
     */
    public enum RightResult
    {
        /**
         * Indicates that a given user has the required right.
         */
        RIGHT_ALLOW,
        
        /**
         * Indicates that a given user does NOT have the required right.
         */
        RIGHT_DENY,
        
        /**
         * Indicates that the system knows nothing about the fact that a given user has a right or not.
         */
        RIGHT_UNKNOWN;
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
        _userManager = (UserManager) manager.lookup(UserManager.ROLE);
        _groupManager = (GroupManager) manager.lookup(GroupManager.ROLE);
        _userPopulationDAO = (UserPopulationDAO) manager.lookup(UserPopulationDAO.ROLE);
        _groupDirectoryDAO = (GroupDirectoryDAO) manager.lookup(GroupDirectoryDAO.ROLE);
        _rightsEP = (RightsExtensionPoint) manager.lookup(RightsExtensionPoint.ROLE);
        _profileAssignmentStorageEP = (ProfileAssignmentStorageExtensionPoint) manager.lookup(ProfileAssignmentStorageExtensionPoint.ROLE);
        _rightContextConvertorEP = (RightContextConvertorExtensionPoint) manager.lookup(RightContextConvertorExtensionPoint.ROLE);
        _accessControllerEP = (AccessControllerExtensionPoint) manager.lookup(AccessControllerExtensionPoint.ROLE);
        _resolver = (SourceResolver) _manager.lookup(SourceResolver.ROLE);
        _currentUserProvider = (CurrentUserProvider) _manager.lookup(CurrentUserProvider.ROLE);
    }
    
    /**
     * Returns the DAO for profiles
     * @return The DAO 
     */
    protected RightProfilesDAO _getProfileDAO ()
    {
        try
        {
            if (_profilesDAO == null)
            {
                _profilesDAO = (RightProfilesDAO) _manager.lookup(RightProfilesDAO.ROLE);
            }
            return _profilesDAO;
        }
        catch (ServiceException e)
        {
            throw new RuntimeException("Failed to retrieve the DAO for profiles", e);
        }
    }
    
    @Override
    public void initialize() throws Exception
    {
        for (UserPopulation userPopulation : _userPopulationDAO.getUserPopulations(true))
        {
            for (UserDirectory userDirectory : userPopulation.getUserDirectories())
            {
                if (userDirectory instanceof ModifiableUserDirectory)
                {
                    ModifiableUserDirectory modifiableUserDirectory = (ModifiableUserDirectory) userDirectory;
                    modifiableUserDirectory.registerListener(this);
                }
            }
        }
        
        for (GroupDirectory groupDirectory : _groupDirectoryDAO.getGroupDirectories())
        {
            if (groupDirectory instanceof ModifiableGroupDirectory)
            {
                ModifiableGroupDirectory modifiableGroupDirectory = (ModifiableGroupDirectory) groupDirectory;
                modifiableGroupDirectory.registerListener(this);
            }
        }

        RequestListenerManager rlm = (RequestListenerManager) _manager.lookup(RequestListenerManager.ROLE);
        rlm.registerListener(this);
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        Configuration rightsConfiguration = configuration.getChild("rights");

        String externalFile = rightsConfiguration.getAttribute("config", null);
        if (externalFile != null)
        {
            Source source = null;
            try
            {
                source = _resolver.resolveURI("context://" + externalFile);

                if (source.exists())
                {
                    Configuration externalConfiguration;
                    try (InputStream is = source.getInputStream();)
                    {
                        externalConfiguration = new DefaultConfigurationBuilder().build(is);
                    }

                    configureRights(externalConfiguration);
                }
                else if (getLogger().isInfoEnabled())
                {
                    getLogger().info("The optional external rights file '" + externalFile + "' is missing.");
                }
            }
            catch (Exception e)
            {
                String message = "An error occured while retriving external file '" + externalFile + "'";
                getLogger().error(message, e);
                throw new ConfigurationException(message, configuration, e);
            }
            finally
            {
                if (source != null)
                {
                    _resolver.release(source);
                }
            }
        }
        else
        {
            configureRights(rightsConfiguration);
        }
    }
    
    private void configureRights(Configuration configuration) throws ConfigurationException
    {
        Configuration[] rights = configuration.getChildren("right");
        for (Configuration rightConf : rights)
        {
            String id = rightConf.getAttribute("id", "");

            String label = rightConf.getChild("label").getValue("");
            I18nizableText i18nLabel = new I18nizableText("application", label);

            String description = rightConf.getChild("description").getValue("");
            I18nizableText i18nDescription = new I18nizableText("application", description);

            String category = rightConf.getChild("category").getValue("");
            I18nizableText i18nCategory = new I18nizableText("application", category);

            if (id.length() == 0 || label.length() == 0 || description.length() == 0 || category.length() == 0)
            {
                String message = "Error in " + RightManager.class.getName() + " configuration: attribute 'id' and elements 'label', 'description' and 'category' are mandatory.";
                getLogger().error(message);
                throw new ConfigurationException(message, configuration);
            }

            _rightsEP.addRight(id, i18nLabel, i18nDescription, i18nCategory);
        }
    }
    
    /* --------- */
    /* HAS RIGHT */
    /* --------- */
    
    /**
     * Checks a permission for the current logged user, on a given object (or context).<br>
     * If null, it checks if there is at least one object with this permission
     * @param rightId The name of the right to check. Cannot be null.
     * @param object The object to check the right. Can be null to search on any object.
     * @return {@link RightResult#RIGHT_ALLOW}, {@link RightResult#RIGHT_DENY} or {@link RightResult#RIGHT_UNKNOWN}
     * @throws RightsException if an error occurs.
     */
    public RightResult currentUserHasRight(String rightId, Object object) throws RightsException
    {
        return hasRight(_currentUserProvider.getUser(), rightId, object);
    }
    
    /**
     * Checks a permission for a user, on a given object (or context).<br>
     * If null, it checks if there is at least one object with this permission
     * @param userIdentity The user identity. Cannot be null.
     * @param rightId The name of the right to check. Cannot be null.
     * @param object The object to check the right. Can be null to search on any object.
     * @return {@link RightResult#RIGHT_ALLOW}, {@link RightResult#RIGHT_DENY} or {@link RightResult#RIGHT_UNKNOWN}
     * @throws RightsException if an error occurs.
     */
    public RightResult hasRight(UserIdentity userIdentity, String rightId, Object object) throws RightsException
    {
        if (object instanceof String && StringUtils.equals((String) object, AdminAuthenticateAction.ADMIN_RIGHT_CONTEXT) && StringUtils.equals(userIdentity.getPopulationId(), UserPopulationDAO.ADMIN_POPULATION_ID))
        {
            return RightResult.RIGHT_ALLOW;
        }
        
        getLogger().debug("Try to determine if user '{}' has the right '{}' on the object context {}", userIdentity, rightId, object);
        
        // Retrieve all profiles containing the right rightId
        Set<String> profileIds = _getProfileDAO().getProfilesWithRight(rightId);
        
        RightResult rightResult = _hasRight(userIdentity, profileIds, object);
        
        return rightResult;
    }
    
    private RightResult _hasRight(UserIdentity userIdentity, Set<String> profileIds, Object object)
    {
        if (object == null)
        {
            // Try to retrieve in second cache (the one which manages null context)
            RightResult cacheResult = _hasRightResultInSecondCache(userIdentity, profileIds);
            if (cacheResult != null)
            {
                getLogger().debug("Find entry in cache2 for [{}, {}] => {}", userIdentity, profileIds, cacheResult);
                return cacheResult;
            }
            else
            {
                getLogger().debug("Did not find entry in cache for [{}, {}, {}]", userIdentity, profileIds, object);
            }
        }
        else
        {
            // Try to retrieve in first cache (the one which manages non-null contexts)
            RightResult cacheResult = _hasRightResultInFirstCache(userIdentity, profileIds, object);
            if (cacheResult != null)
            {
                getLogger().debug("Find entry in cache for [{}, {}, {}] => {}", userIdentity, profileIds, object, cacheResult);
                return cacheResult;
            }
            else
            {
                getLogger().debug("Did not find entry in cache for [{}, {}, {}]", userIdentity, profileIds, object);
            }
        }
        
        // Retrieve groups the user belongs to
        Set<GroupIdentity> groups = _getGroups(userIdentity);
        
        // Specific case when object is null, we search if there is at least one object with a permission
        if (object == null)
        {
            boolean hasPermission = _profileAssignmentStorageEP.hasPermission(userIdentity, groups, profileIds);
            RightResult rightResult = hasPermission ? RightResult.RIGHT_ALLOW : RightResult.RIGHT_UNKNOWN;
            
            getLogger().debug("Right result found for [{}, {}] => {}", userIdentity, profileIds, rightResult);
            _putInSecondCache(userIdentity, profileIds, rightResult);
            return rightResult;
        }
        
        // Get the objects to check
        Set<Object> objects = _getConvertedObjects(object);
        
        // Retrieve the set of AccessResult
        Set<AccessResult> accessResults = _getAccessResults(userIdentity, groups, profileIds, objects);
        
        // Compute access
        AccessResult access = _computeAccess(accessResults);
        
        RightResult rightResult = _computeRight(access);
        _putInFirstCache(userIdentity, profileIds, object, rightResult);
        
        return rightResult;
    }
    
    private RightResult _hasRightResultInFirstCache(UserIdentity userIdentity, Set<String> profileIds, Object object)
    {
        Map<UserIdentity, Map<Set<String>, Map<Object, RightResult>>> mapCache = _cacheTL.get();
        if (mapCache == null)
        {
            mapCache = new HashMap<>();
            _cacheTL.set(mapCache);
        }
        
        if (mapCache.containsKey(userIdentity))
        {
            Map<Set<String>, Map<Object, RightResult>> mapRight = mapCache.get(userIdentity);
            if (mapRight.containsKey(profileIds))
            {
                Map<Object, RightResult> mapContext = mapRight.get(profileIds);
                if (mapContext.containsKey(object))
                {
                    return mapContext.get(object);
                }
            }
        }
        return null;
    }
    
    private void _putInFirstCache(UserIdentity userIdentity, Set<String> profileIds, Object object, RightResult rightResult)
    {
        Map<UserIdentity, Map<Set<String>, Map<Object, RightResult>>> mapCache = _cacheTL.get();
        
        if (mapCache.containsKey(userIdentity))
        {
            Map<Set<String>, Map<Object, RightResult>> mapRight = mapCache.get(userIdentity);
            if (mapRight.containsKey(profileIds))
            {
                Map<Object, RightResult> mapContext = mapRight.get(profileIds);
                mapContext.put(object, rightResult);
            }
            else
            {
                Map<Object, RightResult> mapContext = new HashMap<>();
                mapContext.put(object, rightResult);
                mapRight.put(profileIds, mapContext);
            }
        }
        else
        {
            Map<Set<String>, Map<Object, RightResult>> mapRight = new HashMap<>();
            Map<Object, RightResult> mapContext = new HashMap<>();
            mapContext.put(object, rightResult);
            mapRight.put(profileIds, mapContext);
            mapCache.put(userIdentity, mapRight);
        }
    }
    
    private RightResult _hasRightResultInSecondCache(UserIdentity userIdentity, Set<String> profileIds)
    {
        Map<UserIdentity, Map<Set<String>, RightResult>> mapCache = _cache2TL.get();
        if (mapCache == null)
        {
            mapCache = new HashMap<>();
            _cache2TL.set(mapCache);
        }
        
        if (mapCache.containsKey(userIdentity))
        {
            Map<Set<String>, RightResult> mapRight = mapCache.get(userIdentity);
            if (mapRight.containsKey(profileIds))
            {
                return mapRight.get(profileIds);
            }
        }
        return null;
    }
    
    private void _putInSecondCache(UserIdentity userIdentity, Set<String> profileIds, RightResult rightResult)
    {
        Map<UserIdentity, Map<Set<String>, RightResult>> mapCache = _cache2TL.get();
        
        if (mapCache.containsKey(userIdentity))
        {
            Map<Set<String>, RightResult> mapRight = mapCache.get(userIdentity);
            mapRight.put(profileIds, rightResult);
        }
        else
        {
            Map<Set<String>, RightResult> mapRight = new HashMap<>();
            mapRight.put(profileIds, rightResult);
            mapCache.put(userIdentity, mapRight);
        }
    }
    
    private Set<Object> _getConvertedObjects(Object object)
    {
        Set<Object> objects = _rightContextConvertorEP.getExtensionsIds().stream()
                .map(_rightContextConvertorEP::getExtension)
                .flatMap(convertor -> convertor.convert(object).stream())
                .collect(Collectors.toSet());
        objects.add(object);
        
        return objects;
    }
    
    private Set<AccessResult> _getAccessResults(UserIdentity userIdentity, Set<GroupIdentity> groups, Set<String> profileIds, Set<Object> objects)
    {
        Set<AccessResult> accessResults = new HashSet<>();
        for (Object obj : objects)
        {
            for (String controllerId : _accessControllerEP.getExtensionsIds())
            {
                AccessController accessController = _accessControllerEP.getExtension(controllerId);
                if (accessController.isSupported(obj))
                {
                    // Add all the AccessResult from the AccessResultContexts, ignoring the profile ids they come from
                    accessResults.addAll(accessController.getPermissions(userIdentity, groups, profileIds, obj)
                                                            .values().stream()
                                                            .map(AccessResultContext::getResult)
                                                            .collect(Collectors.toSet()));
                }
                else
                {
                    accessResults.add(AccessResult.UNKNOWN);
                }
            }
        }
        
        return accessResults;
    }
    
    /**
     * Gets the best {@link AccessResult} from a set (as DENIED &gt; ALLOWED and USER &gt; GROUP &gt; ANYCONNECTED &gt; ANONYMOUS)
     * @param accessResults the set of access results
     * @return the best {@link AccessResult}
     */
    private AccessResult _computeAccess(Set<AccessResult> accessResults)
    {
        return accessResults.stream().min(Comparator.naturalOrder()).orElse(AccessResult.UNKNOWN);
    }
    
    private AccessResult _computeAccess(AccessResult ... accessResults)
    {
        return Arrays.stream(accessResults).min(Comparator.naturalOrder()).orElse(AccessResult.UNKNOWN);
    }
    
    private RightResult _computeRight(AccessResult access)
    {
        switch (access)
        {
            case USER_DENIED:
            case GROUP_DENIED:
            case ANY_CONNECTED_DENIED:
            case ANONYMOUS_DENIED:
                return RightResult.RIGHT_DENY;
            case USER_ALLOWED:
            case GROUP_ALLOWED:
            case ANY_CONNECTED_ALLOWED:
            case ANONYMOUS_ALLOWED:
                return RightResult.RIGHT_ALLOW;
            case UNKNOWN:
            default:
                return RightResult.RIGHT_UNKNOWN;
        }
    }
    
    
    /* ----------- */
    /* HAS PROFILE */
    /* ----------- */
    
    /**
     * Gets the right result for anonymous with given profile on given object context
     * @param profileId The id of the profile
     * @param object The object to check
     * @return the right result for anonymous with given profile on given object context
     */
    public RightResult hasAnonymousProfile(String profileId, Object object)
    {
        Set<Object> objects = _getConvertedObjects(object);
        
        Set<String> profileIds = new HashSet<>();
        profileIds.add(profileId);
        
        Set<AccessResult> results = new HashSet<>();
        
        for (Object obj : objects)
        {
            for (String controllerId : _accessControllerEP.getExtensionsIds())
            {
                AccessController accessController = _accessControllerEP.getExtension(controllerId);
                if (accessController.isSupported(obj))
                {
                    AccessResult result = accessController.getPermissionForAnonymous(profileIds, obj);
                    if (result.equals(AccessResult.ANONYMOUS_DENIED))
                    {
                        // Stop iteration, one object returns a denied permission
                        return RightResult.RIGHT_DENY;
                    }
                    
                    results.add(result);
                }
            }
        }
        
        return _computeRight(_computeAccess(results));
    }
    
    /**
     * Gets the right result for any connected user with given profile on given object context
     * @param profileId The id of the profile
     * @param object The object to check
     * @return the right result for any connected user with given profile on given object context
     */
    public RightResult hasAnyConnectedUserProfile(String profileId, Object object)
    {
        Set<Object> objects = _getConvertedObjects(object);
        
        Set<String> profileIds = new HashSet<>();
        profileIds.add(profileId);
        
        Set<AccessResult> results = new HashSet<>();
        
        for (Object obj : objects)
        {
            for (String controllerId : _accessControllerEP.getExtensionsIds())
            {
                AccessController accessController = _accessControllerEP.getExtension(controllerId);
                if (accessController.isSupported(obj))
                {
                    AccessResult result = accessController.getPermissionForAnyConnectedUser(profileIds, obj);
                    if (result.equals(AccessResult.ANONYMOUS_DENIED))
                    {
                        // Stop iteration, one object returns a denied permission
                        return RightResult.RIGHT_DENY;
                    }
                    
                    results.add(result);
                }
            }
        }
        
        return _computeRight(_computeAccess(results));
    }
    
    
    /* ------------------------- */
    /* METHODS ON READER PROFILE */
    /* ------------------------- */
    
    /**
     * Returns true if the current user has READ access on the given object
     * @param object The object to check the right. Can be null to search on any object.
     * @return true if the given user has READ access on the given object
     */
    public boolean currentUserHasReaderRight(Object object)
    {
        return hasReadAccess(_currentUserProvider.getUser(), object);
    }

    /**
     * Returns true if the given user has READ access on the given object
     * @param userIdentity The user identity. Cannot be null.
     * @param object The object to check the right. Can be null to search on any object.
     * @return true if the given user has READ access on the given object
     */
    public boolean hasReadAccess(UserIdentity userIdentity, Object object)
    {
        if (object instanceof String && StringUtils.equals((String) object, AdminAuthenticateAction.ADMIN_RIGHT_CONTEXT) && StringUtils.equals(userIdentity.getPopulationId(), UserPopulationDAO.ADMIN_POPULATION_ID))
        {
            return true;
        }
        
        return _hasRight(userIdentity, Collections.singleton(READER_PROFILE_ID), object) == RightResult.RIGHT_ALLOW;
    }
    
    /**
     * Returns true if the object is restricted, i.e. an anonymous user has not READ access (is denied) on the object
     * @param object The object to check
     * @return true if the object is restricted, i.e. an anonymous user has not READ access (is denied) on the object
     */
    public boolean isReadingRestricted(Object object)
    {
        /** FIXME Can not work from FO side until RUNTIME-2075 is not fixed **/ 
        // return hasAnonymousProfile(READER_PROFILE_ID, object) != RightResult.RIGHT_ALLOW;
        return false;
    }
    
    /**
     * Returns true if any connected user has READ access allowed on the object
     * @param object The object to check
     * @return true if any connected user has READ access allowed on the object
     */
    public boolean hasAnyConnectedReadAccess(Object object)
    {
        return hasAnyConnectedUserProfile(READER_PROFILE_ID, object) == RightResult.RIGHT_ALLOW;
    }
    
    /**
     * Get the users with a READ access on given object
     * @param object The object
     * @return The representation of allowed users 
     */
    public AllowedUsers getUsersWithReadAccess(Object object)
    {
        Set<String> profileIds = new HashSet<>();
        profileIds.add(READER_PROFILE_ID);
        return getAllowedUsers(profileIds, object);
    }
    
    /* ----------------- */
    /* GET ALLOWED USERS */
    /* ----------------- */
    
    /**
     * Get the list of users that have a particular right in a particular context.
     * @param rightId The name of the right to check. Cannot be null.
     * @param object The object to check the right. Cannot be null.
     * @return The list of users allowed with that right as a Set of String (user identities).
     * @throws RightsException if an error occurs.
     */
    public AllowedUsers getAllowedUsers(String rightId, Object object)
    {
        // Retrieve all profiles containing the right rightId
        Set<String> profileIds = _getProfileDAO().getProfilesWithRight(rightId);
        return getAllowedUsers(profileIds, object);
    }
    
    /**
     * Get the list of users that have a particular right in a particular context.
     * @param profileIds The id of profiles to check.
     * @param object The object to check the right. Cannot be null.
     * @return The list of users allowed with that right as a Set of String (user identities).
     */
    public AllowedUsers getAllowedUsers(Set<String> profileIds, Object object)
    {
        // Get the objects to check
        Set<Object> objects = _getConvertedObjects(object);
        
        // For each object, retrieve the allowed and denied users/groups
        Boolean isAnyConnectedAllowed = null; // unknown
        Set<UserIdentity> allAllowedUsers = new HashSet<>();
        Set<UserIdentity> allDeniedUsers = new HashSet<>();
        Set<GroupIdentity> allAllowedGroups = new HashSet<>();
        Set<GroupIdentity> allDeniedGroups = new HashSet<>();
        
        for (Object obj : objects)
        {
            if (obj instanceof String && StringUtils.equals((String) obj, AdminAuthenticateAction.ADMIN_RIGHT_CONTEXT))
            {
                allAllowedUsers.addAll(_userManager.getUsers(UserPopulationDAO.ADMIN_POPULATION_ID).stream().map(user -> user.getIdentity()).collect(Collectors.toSet()));
            }
            
            for (String controllerId : _accessControllerEP.getExtensionsIds())
            {
                AccessController accessController = _accessControllerEP.getExtension(controllerId);
                if (accessController.isSupported(obj))
                {
                    if (accessController.getPermissionForAnonymous(profileIds, obj) == AccessResult.ANONYMOUS_ALLOWED)
                    {
                        // Any anonymous user is allowed
                        return new AllowedUsers(true, false, null, null, null, null, _userManager, _groupManager, null);
                    }
                    
                    AccessResult permissionForAnyConnectedUser = accessController.getPermissionForAnyConnectedUser(profileIds, obj);
                    if (permissionForAnyConnectedUser == AccessResult.ANY_CONNECTED_DENIED)
                    {
                        // For having any connected user allowed, you need to not have the denied access for one object
                        isAnyConnectedAllowed = Boolean.FALSE;
                    }
                    else if (isAnyConnectedAllowed == null && permissionForAnyConnectedUser == AccessResult.ANY_CONNECTED_ALLOWED)
                    {
                        isAnyConnectedAllowed = Boolean.TRUE;
                    }
                    
                    Map<UserIdentity, AccessResult> permissionsByUser = accessController.getPermissionsByUser(profileIds, obj);
                    
                    Set<UserIdentity> allowedUsersOnObj = permissionsByUser.entrySet().stream()
                            .filter(entry -> AccessResult.USER_ALLOWED.equals(entry.getValue()))
                            .map(Entry::getKey)
                            .collect(Collectors.toSet());
                    allAllowedUsers.addAll(allowedUsersOnObj);
                    
                    Set<UserIdentity> deniedUsersOnObj = permissionsByUser.entrySet().stream()
                            .filter(entry -> AccessResult.USER_DENIED.equals(entry.getValue()))
                            .map(Entry::getKey)
                            .collect(Collectors.toSet());
                    allDeniedUsers.addAll(deniedUsersOnObj);
                    
                    
                    Map<GroupIdentity, AccessResult> permissionsByGroup = accessController.getPermissionsByGroup(profileIds, obj);
                    
                    Set<GroupIdentity> allowedGroupsOnObj = permissionsByGroup.entrySet().stream()
                            .filter(entry -> AccessResult.GROUP_ALLOWED.equals(entry.getValue()))
                            .map(Entry::getKey)
                            .collect(Collectors.toSet());
                    allAllowedGroups.addAll(allowedGroupsOnObj);
                    
                    Set<GroupIdentity> deniedGroupsOnObj = permissionsByGroup.entrySet().stream()
                            .filter(entry -> AccessResult.GROUP_DENIED.equals(entry.getValue()))
                            .map(Entry::getKey)
                            .collect(Collectors.toSet());
                    allDeniedGroups.addAll(deniedGroupsOnObj);
                }
            }
        }
        
        Request request = ContextHelper.getRequest(_context);
        @SuppressWarnings("unchecked")
        List<String> populationContexts = (List<String>) request.getAttribute("populationContexts");
        
        // Then, return the AllowedUsers object
        return new AllowedUsers(false, isAnyConnectedAllowed != null && isAnyConnectedAllowed.booleanValue(), allAllowedUsers, allDeniedUsers, allAllowedGroups, allDeniedGroups, _userManager, _groupManager, new HashSet<>(populationContexts));
    }
    
    /* --------------- */
    /* GET USER RIGHTS */
    /* --------------- */
    
    /**
     * Get the list of rights a user is allowed, on a particular object.
     * @param userIdentity the user identity. Cannot be null.
     * @param object The object to check the right. Cannot be null.
     * @return The list of rights as a Set of String (id).
     * @throws RightsException if an error occurs.
     */
    public Set<String> getUserRights(UserIdentity userIdentity, Object object) throws RightsException
    {
        // Get the objects to check
        Set<Object> objects = _getConvertedObjects(object);
        
        // An admin population user in admin context have all rights
        if (objects.contains(AdminAuthenticateAction.ADMIN_RIGHT_CONTEXT) && StringUtils.equals(userIdentity.getPopulationId(), UserPopulationDAO.ADMIN_POPULATION_ID))
        {
            return _rightsEP.getExtensionsIds();
        }
        
        // Retrieve groups the user belongs to
        Set<GroupIdentity> groups = _getGroups(userIdentity);
        
        // Gets the access by profiles
        Map<String, AccessResult> accessResults = _getAccessResultByProfile(userIdentity, groups, objects);
        // Convert AccessResult to RightResult
        Map<String, RightResult> rightsResults = accessResults.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey(), entry -> _computeRight(entry.getValue())));
        
        Set<String> allowedProfiles = _getAllowedProfiles(rightsResults);
        Set<String> deniedProfiles = _getDeniedProfiles(rightsResults);
        
        Set<String> rights = new HashSet<>();
        // Iterate over allowed profiles and add all their rights
        for (String profileId : allowedProfiles)
        {
            Profile profile = getProfile(profileId);
            rights.addAll(_getProfileDAO().getRights(profile));
        }
        
        // Then iterate over denied profiles and remove all their rights
        for (String profileId : deniedProfiles)
        {
            Profile profile = getProfile(profileId);
            rights.removeAll(_getProfileDAO().getRights(profile));
        }
        
        return rights;
    }
    
    private Map<String, AccessResult> _getAccessResultByProfile(UserIdentity userIdentity, Set<GroupIdentity> groups, Set<Object> objects)
    {
        Map<String, AccessResult> result = new HashMap<>();
        
        for (Object obj : objects)
        {
            for (String controllerId : _accessControllerEP.getExtensionsIds())
            {
                AccessController accessController = _accessControllerEP.getExtension(controllerId);
                if (accessController.isSupported(obj))
                {
                    // Update the result map
                    Map<String, AccessResult> permissionsByProfile = accessController.getPermissionsByProfile(userIdentity, groups, obj);
                    for (String profileId : permissionsByProfile.keySet())
                    {
                        if (!result.containsKey(profileId))
                        {
                            result.put(profileId, permissionsByProfile.get(profileId));
                        }
                        else
                        {
                            result.put(profileId, _computeAccess(result.get(profileId), permissionsByProfile.get(profileId)));
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    private Set<String> _getAllowedProfiles(Map<String, RightResult> rightResultByProfile)
    {
        return rightResultByProfile.entrySet().stream()
                .filter(entry -> RightResult.RIGHT_ALLOW.equals(entry.getValue()))
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }
    
    private Set<String> _getDeniedProfiles(Map<String, RightResult> rightResultByProfile)
    {
        return rightResultByProfile.entrySet().stream()
                .filter(entry -> RightResult.RIGHT_DENY.equals(entry.getValue()))
                .map(Entry::getKey)
                .collect(Collectors.toSet());
    }
    
    /* ------------------- */
    /* PROFILES MANAGEMENT */
    /* ------------------- */
    
    /**
     * Add a new Profile to null context. An id will be generated
     * @param name the name of the new Profile
     * @return the newly created Profile
     * @throws RightsException if an error occurs.
     */
    public Profile addProfile(String name) throws RightsException
    {
        return addProfile(name, null);
    }
    
    /**
     * Add a new Profile. An id will be generated
     * @param name the name of the new Profile
     * @param context the context. Can be null.
     * @return the newly created Profile
     * @throws RightsException if an error occurs.
     */
    public Profile addProfile(String name, String context) throws RightsException
    {
        String id = UUID.randomUUID().toString();

        return addProfile(id, name, context);
    }
    
    /**
     * Add a new Profile
     * @param id the id of the profile
     * @param name the name of the new Profile
     * @param context the context. Can be null.
     * @return the newly created Profile
     * @throws RightsException if an error occurs.
     */
    public Profile addProfile(String id, String name, String context) throws RightsException
    {
        if (getProfile(id) != null)
        {
            throw new RightsException(String.format("The profile of id %s already exists. Thus the profile cannot be added.", id));
        }
        
        Profile profile = new Profile(id, name, context);
        _getProfileDAO().addProfile(profile);
        return profile;
    }
    
    /**
     * Returns the Profile with the given Id
     * @param id the id oif the wanted Profile
     * @return the Profile with the given Id
     * @throws RightsException if an error occurs.
     */
    public Profile getProfile(String id) throws RightsException
    {
        return _getProfileDAO().getProfile(id);
    }

    /**
     * Returns all known profiles
     * @return all known profiles
     * @throws RightsException if an error occurs.
     */
    public List<Profile> getAllProfiles() throws RightsException
    {
        return _getProfileDAO().getProfiles();
    }
    
    /**
     * Returns profiles with no context
     * @return profiles with no context
     * @throws RightsException if an error occurs.
     */
    public List<Profile> getProfiles() throws RightsException
    {
        return getProfiles(null);
    }
    
    /**
     * Returns profiles of a given context
     * @param context The context. Can be null. If null, the profiles with no context are returned.
     * @return profiles of a given context 
     * @throws RightsException if an error occurs.
     */
    public List<Profile> getProfiles(String context) throws RightsException
    {
        return _getProfileDAO().getProfiles(context);
    }
    
    /**
     * Remove the given profile from database
     * @param id The id of the profile
     */
    public void removeProfile(String id)
    {
        if (READER_PROFILE_ID.equals(id))
        {
            throw new RightsException("You cannot remove the system profile 'READER'");
        }
        
        _getProfileDAO().deleteProfile(id);
        
        // Removes this profile in the profile assignment storages
        _profileAssignmentStorageEP.getExtensionsIds().stream()
            .map(_profileAssignmentStorageEP::getExtension)
            .forEach(pas -> pas.removeProfile(id));
    }
    
    @Override
    public void userRemoved(UserIdentity user)
    {
        _profileAssignmentStorageEP.getExtensionsIds().stream()
            .map(_profileAssignmentStorageEP::getExtension)
            .forEach(pas -> pas.removeUser(user));
    }

    @Override
    public void userAdded(UserIdentity user)
    {
        // Nothing
    }

    @Override
    public void userUpdated(UserIdentity user)
    {
        // Nothing
    }

    @Override
    public void groupRemoved(GroupIdentity group)
    {
        _profileAssignmentStorageEP.getExtensionsIds().stream()
            .map(_profileAssignmentStorageEP::getExtension)
            .forEach(pas -> pas.removeGroup(group));
    }

    @Override
    public void groupAdded(GroupIdentity group)
    {
        // Nothing
    }

    @Override
    public void groupUpdated(GroupIdentity group)
    {
        // Nothing
    }

    @Override
    public void requestStarted(HttpServletRequest req)
    {
        // empty
    }

    @Override
    public void requestEnded(HttpServletRequest req)
    {
        if (_cacheTL.get() != null)
        {
            _cacheTL.set(null);
        }
        if (_cache2TL.get() != null)
        {
            _cache2TL.set(null);
        }
    }
    
    private Set<GroupIdentity> _getGroups(UserIdentity user)
    {
        if (user == null)
        {
            return Collections.EMPTY_SET;
        }
        else
        {
            Set<GroupIdentity> userGroups = _groupManager.getUserGroups(user.getLogin(), user.getPopulationId());
            return userGroups;
        }
    }
}
