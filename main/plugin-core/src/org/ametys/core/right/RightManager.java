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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.group.GroupDirectoryDAO;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.group.GroupListener;
import org.ametys.core.group.GroupManager;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.group.directory.ModifiableGroupDirectory;
import org.ametys.core.right.AccessController.AccessResult;
import org.ametys.core.right.AccessController.AccessResultContext;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserListener;
import org.ametys.core.user.UserManager;
import org.ametys.core.user.directory.ModifiableUserDirectory;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;
import org.ametys.runtime.request.RequestListener;
import org.ametys.runtime.request.RequestListenerManager;

/**
 * Abstraction for testing a right associated with a resource and a user from a single source.
 */
public class RightManager extends AbstractLogEnabled implements UserListener, GroupListener, Serviceable, Configurable, Initializable, RequestListener, ThreadSafe, Component
{
    /** For avalon service manager */
    public static final String ROLE = RightManager.class.getName();
    
    /** The id of the READER profile */
    public static final String READER_PROFILE_ID = "READER";
    
    private static final String __INITIAL_PROFILE_ID = "TEMPORARY ADMINISTRATOR";
    
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
    /** The id of the data source to use */
    protected String _dataSourceId;
    /** The jdbc table name for profiles' list */
    protected String _tableProfile;
    /** The jdbc table name for profiles' rights */
    protected String _tableProfileRights;
    
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
    
    /**
     * This cache is for storing the set of profiles a right belongs to.
     * 
     * { RightId : {[ProfileIds]}
     */
    private final ThreadLocal<Map<String, Set<String>>> _cacheProfilesTL = new ThreadLocal<>();
    
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
        // FIXME handle safe-mode case
        if (PluginsManager.getInstance().isSafeMode())
        {
            getLogger().warn("Cannot configure the RightManager in safe mode ! Please fix me !");
            return;
        }
        
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

        Configuration dataSourceConf = configuration.getChild("datasource", false);
        if (dataSourceConf == null)
        {
            throw new ConfigurationException("The 'datasource' configuration node must be defined.", dataSourceConf);
        }
        
        String dataSourceConfParam = dataSourceConf.getValue();
        String dataSourceConfType = dataSourceConf.getAttribute("type", "config");
        
        if (StringUtils.equals(dataSourceConfType, "config"))
        {
            _dataSourceId = Config.getInstance().getValueAsString(dataSourceConfParam);
        }
        else // expecting type="id"
        {
            _dataSourceId = dataSourceConfParam;
        }
        
        _tableProfile = configuration.getChild("table-profile").getValue("Rights_Profile");
        _tableProfileRights = configuration.getChild("table-profile-rights").getValue("Rights_ProfileRights");
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
    
    /**
     * Gets the id of the datasource storing the profiles, rights and assignments
     * @return the id of the datasource storing the profiles, rights and assignments
     */
    public String getDataSourceId()
    {
        return _dataSourceId;
    }
    
    /**
     * Gets the table name for storing profiles
     * @return the table name for storing profiles
     */
    public String getTableProfile()
    {
        return _tableProfile;
    }
    
    /**
     * Get the connection to the database 
     * @return the SQL connection
     */
    protected Connection getSQLConnection ()
    {
        return ConnectionHelper.getConnection(_dataSourceId);
    }
    
    
    /* --------- */
    /* HAS RIGHT */
    /* --------- */
    
    /**
     * Checks a permission for a user, on a given object (or context).<br>
     * If null, it checks i fthere is at least one object with this permission
     * @param userIdentity The user identity. Cannot be null.
     * @param rightId The name of the right to check. Cannot be null.
     * @param object The object to check the right. Can be null to search on any object.
     * @return {@link RightResult#RIGHT_ALLOW}, {@link RightResult#RIGHT_DENY} or {@link RightResult#RIGHT_UNKNOWN}
     * @throws RightsException if an error occurs.
     */
    public RightResult hasRight(UserIdentity userIdentity, String rightId, Object object) throws RightsException
    {
        getLogger().debug("RightManager is asked if user '{}' has the right {} on the object '{}'", userIdentity, rightId, object);
        
        // Retrieve all profiles containing the right rightId
        Set<String> profileIds = _getProfiles(rightId);
        
        RightResult rightResult = _hasRight(userIdentity, profileIds, object);
        
        _putInFirstCache(userIdentity, profileIds, object, rightResult);
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
                getLogger().debug("RightManager is responding {}", cacheResult);
                return cacheResult;
            }
            else
            {
                getLogger().debug("Did not find entry in cache for [{}, '{}', '{}']", userIdentity, profileIds, object);
            }
        }
        else
        {
            // Try to retrieve in first cache (the one which manages non-null contexts)
            RightResult cacheResult = _hasRightResultInFirstCache(userIdentity, profileIds, object);
            if (cacheResult != null)
            {
                getLogger().debug("RightManager is responding {}", cacheResult);
                return cacheResult;
            }
            else
            {
                getLogger().debug("Did not find entry in cache for [{}, '{}', '{}']", userIdentity, profileIds, object);
            }
        }
        
        // Retrieve groups the user belongs to
        Set<GroupIdentity> groups = _getGroups(userIdentity);
        
        // Specific case when object is null, we search if there is at least one object with a permission
        if (object == null)
        {
            boolean hasPermission = _profileAssignmentStorageEP.hasPermission(userIdentity, groups, profileIds);
            RightResult rightResult = hasPermission ? RightResult.RIGHT_ALLOW : RightResult.RIGHT_UNKNOWN;
            
            getLogger().debug("RightManager is responding {}", rightResult);
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
        getLogger().debug("RightManager is responding {} ({})", rightResult, access);
        
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
                    getLogger().debug("Find in cache the set of profiles '{}' for user {} on context '{}'", profileIds, userIdentity, object);
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
                getLogger().debug("Find in cache the set of profiles '{}' for user {} on null context", profileIds, userIdentity);
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
    
    private Set<String> _getProfiles(String rightId)
    {
        Map<String, Set<String>> mapCache = _cacheProfilesTL.get();
        
        if (mapCache == null)
        {
            // Build the cache with only one SQL query
            mapCache = new HashMap<>();
            
            Connection connection = getSQLConnection();
            PreparedStatement stmt = null;
            ResultSet rs = null;
    
            try
            {
                String sql = "SELECT Profile_Id, Right_Id FROM " + _tableProfileRights;
    
                stmt = connection.prepareStatement(sql);
    
                getLogger().debug("{}", sql);
    
                rs = stmt.executeQuery();
                
                while (rs.next())
                {
                    String currentProfileId = rs.getString(1);
                    String currentRightId = rs.getString(2);
                    if (mapCache.containsKey(currentRightId))
                    {
                        mapCache.get(currentRightId).add(currentProfileId);
                    }
                    else
                    {
                        Set<String> profiles = new HashSet<>();
                        profiles.add(currentProfileId);
                        mapCache.put(currentRightId, profiles);
                    }
                }
            }
            catch (SQLException e)
            {
                getLogger().error("Error communicating with database", e);
                throw new RightsException("Error communicating with database", e);
            }
            finally
            {
                ConnectionHelper.cleanup(rs);
                ConnectionHelper.cleanup(stmt);
                ConnectionHelper.cleanup(connection);
            }
        }
        
        if (mapCache.containsKey(rightId))
        {
            return mapCache.get(rightId);
        }
        else
        {
            return Collections.EMPTY_SET;
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
        boolean isAnonymousAllowed = _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.isAnonymousAllowed(object, profileId))
                .orElse(false);
        return isAnonymousAllowed ? RightResult.RIGHT_ALLOW : RightResult.RIGHT_DENY;
    }
    
    /**
     * Gets the right result for any connected user with given profile on given object context
     * @param profileId The id of the profile
     * @param object The object to check
     * @return the right result for any connected user with given profile on given object context
     */
    public RightResult hasAnyConnectedUserProfile(String profileId, Object object)
    {
        boolean isAnonymousAllowed = _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.isAnyConnectedUserAllowed(object, profileId))
                .orElse(false);
        return isAnonymousAllowed ? RightResult.RIGHT_ALLOW : RightResult.RIGHT_DENY;
    }
    
    
    /* ------------------------- */
    /* METHODS ON READER PROFILE */
    /* ------------------------- */
    
    /**
     * Returns true if the given user has READ access on the given object
     * @param userIdentity The user identity. Cannot be null.
     * @param object The object to check the right. Can be null to search on any object.
     * @return true if the given user has READ access on the given object
     */
    public boolean hasReaderRight(UserIdentity userIdentity, Object object)
    {
        return _hasRight(userIdentity, Collections.singleton(READER_PROFILE_ID), object) == RightResult.RIGHT_ALLOW;
    }
    
    /**
     * Returns true if the object is restricted, i.e. an anonymous user has not READ access (is denied) on the object
     * @param object The object to check
     * @return true if the object is restricted, i.e. an anonymous user has not READ access (is denied) on the object
     */
    public boolean isRestricted(Object object)
    {
        return hasAnonymousProfile(READER_PROFILE_ID, object) == RightResult.RIGHT_DENY;
    }
    
    /**
     * Returns true if any connected user has READ access allowed on the object
     * @param object The object to check
     * @return true if any connected user has READ access allowed on the object
     */
    public boolean isAnyConnectedAllowed(Object object)
    {
        return hasAnyConnectedUserProfile(READER_PROFILE_ID, object) == RightResult.RIGHT_ALLOW;
    }
    
    /**
     * Gets the users that have READ access allowed
     * @param object The object to check
     * @return the users that have READ access allowed
     */
    public Set<UserIdentity> getReaderAllowedUsers(Object object)
    {
        return getOnlyAllowedUsers(object, READER_PROFILE_ID);
    }
    
    /**
     * Gets the users that have READ access denied
     * @param object The object to check
     * @return the users that have READ access denied
     */
    public Set<UserIdentity> getReaderDeniedUsers(Object object)
    {
        return getOnlyDeniedUsers(object, READER_PROFILE_ID);
    }
    
    /**
     * Gets the groups that have READ access allowed
     * @param object The object to check
     * @return the groups that have READ access allowed
     */
    public Set<GroupIdentity> getReaderAllowedGroups(Object object)
    {
        return getOnlyAllowedGroups(object, READER_PROFILE_ID);
    }
    
    /**
     * Gets the groups that have READ access denied
     * @param object The object to check
     * @return the groups that have READ access denied
     */
    public Set<GroupIdentity> getReaderDeniedGroups(Object object)
    {
        return getOnlyDeniedGroups(object, READER_PROFILE_ID);
    }
    
    
    /* --------- */
    /* ANONYMOUS */
    /* --------- */
    
    /**
     * Gets the allowed profiles an anonymous user has on the given object
     * @param context The object context
     * @return the allowed profiles any connected user has on the given object
     */
    public Set<String> getAllowedProfilesForAnonymous(Object context)
    {
        return _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getAllowedProfilesForAnonymous(context))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the denied profiles an anonymous user has on the given object
     * @param context The object context
     * @return the denied profiles any connected user has on the given object
     */
    public Set<String> getDeniedProfilesForAnonymous(Object context)
    {
        return _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getDeniedProfilesForAnonymous(context))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Adds allowed profile an anonymous user has on the given object
     * @param profileId The profile to add
     * @param context The object context
     */
    public void allowProfileToAnonymous(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addAllowedProfilesForAnonymous(context, Collections.singleton(profileId)));
    }
    
    /**
     * Adds denied profile an anonymous user has on the given object
     * @param profileId The profile to add
     * @param context The object context
     */
    public void denyProfileToAnonymous(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addDeniedProfilesForAnonymous(context, Collections.singleton(profileId)));
    }
    
    /**
     * Removes allowed profile an anonymous user has on the given object
     * @param profileId The profile to remove
     * @param context The object context
     */
    public void removeAllowedProfileFromAnonymous(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeAllowedProfilesForAnonymous(context, Collections.singleton(profileId)));
    }
    
    /**
     * Removes denied profile an anonymous user has on the given object
     * @param context The object context
     * @param profileId The profile to remove
     */
    public void removeDeniedProfileFromAnonymous(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeDeniedProfilesForAnonymous(context, Collections.singleton(profileId)));
    }
    
    
    /* ------------------ */
    /* ANY CONNECTED USER */
    /* ------------------ */
    
    /**
     * Gets the allowed profiles any connected user has on the given object
     * @param context The object context
     * @return the allowed profiles any connected user has on the given object
     */
    public Set<String> getAllowedProfilesForAnyConnectedUser(Object context)
    {
        return _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getAllowedProfilesForAnyConnectedUser(context))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the denied profiles any connected user has on the given object
     * @param context The object context
     * @return the denied profiles any connected user has on the given object
     */
    public Set<String> getDeniedProfilesForAnyConnectedUser(Object context)
    {
        return _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getDeniedProfilesForAnyConnectedUser(context))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Adds allowed profile any connected user has on the given object
     * @param context The object context
     * @param profileId The profile to add
     */
    public void allowProfileToAnyConnectedUser(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addAllowedProfilesForAnyConnectedUser(context, Collections.singleton(profileId)));
    }
    
    /**
     * Adds denied profile any connected user has on the given object
     * @param profileId The profile to add
     * @param context The object context
     */
    public void denyProfileToAnyConnectedUser(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addDeniedProfilesForAnyConnectedUser(context, Collections.singleton(profileId)));
    }
    
    /**
     * Removes allowed profile any connected user has on the given object
     * @param profileId The profile to remove
     * @param context The object context
     */
    public void removeAllowedProfileFromAnyConnectedUser(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeAllowedProfilesForAnyConnectedUser(context, Collections.singleton(profileId)));
    }
    
    /**
     * Removes denied profile any connected user has on the given object
     * @param context The object context
     * @param profileId The profile to remove
     */
    public void removeDeniedProfileFromAnyConnectedUser(String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeDeniedProfilesForAnyConnectedUser(context, Collections.singleton(profileId)));
    }
    
    
    /* ----- */
    /* USERS */
    /* ----- */
    
    /**
     * Gets the users that have allowed profiles assigned on the given object
     * @param context The object context to test 
     * @return The map of allowed users (keys) with their assigned profiles (values)
     */
    public Map<UserIdentity, Set<String>> getAllowedProfilesForUsers(Object context)
    {
        return _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getAllowedProfilesForUsers(context))
                .orElse(Collections.EMPTY_MAP);
    }
    
    /**
     * Gets the users that have the given profile as allowed on the given object (assignment only)
     * @param object The object context to check
     * @param profileId The id of the profile
     * @return the users that have the given profile as allowed on the given object (assignment only)
     */
    public Set<UserIdentity> getOnlyAllowedUsers(Object object, String profileId)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedUsers(object, profileId))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the users that have denied profiles assigned on the given object
     * @param context The object context to test 
     * @return The map of denied users (keys) with their assigned profiles (values)
     */
    public Map<UserIdentity, Set<String>> getDeniedProfilesForUsers(Object context)
    {
        return _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getDeniedProfilesForUsers(context))
                .orElse(Collections.EMPTY_MAP);
    }
    
    /**
     * Gets the users that have the given profile as denied on the given object (assignment only)
     * @param object The object context to check
     * @param profileId The id of the profile
     * @return the users that have the given profile as denied on the given object (assignment only)
     */
    public Set<UserIdentity> getOnlyDeniedUsers(Object object, String profileId)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedUsers(object, profileId))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Allows a user to a profile on a given object
     * @param user The user to add
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void allowProfileToUser(UserIdentity user, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addAllowedUsers(Collections.singleton(user), context, profileId));
    }
    
    /**
     * Denies a user to a profile on a given object
     * @param user The user to add
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void denyProfileToUser(UserIdentity user, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addDeniedUsers(Collections.singleton(user), context, profileId));
    }
    
    /**
     * Removes the association between a user and an allowed profile on a given object
     * @param user The user to remove
     * @param context The object context
     * @param profileId The id of the profile
     */
    public void removeAllowedProfileFromUser(UserIdentity user, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeAllowedUsers(Collections.singleton(user), context, profileId));
    }
    
    /**
     * Removes the association between a user and a denied profile on a given object
     * @param user The user to remove
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void removeDeniedProfileFromUser(UserIdentity user, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeDeniedUsers(Collections.singleton(user), context, profileId));
    }
    
    
    /* ------ */
    /* GROUPS */
    /* ------ */
    
    /**
     * Gets the groups that have allowed profiles assigned on the given object
     * @param context The object context to test
     * @return The map of allowed groups (keys) with their assigned profiles (values)
     */
    public Map<GroupIdentity, Set<String>> getAllowedProfilesForGroups(Object context)
    {
        return _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getAllowedProfilesForGroups(context))
                .orElse(Collections.EMPTY_MAP);
    }
    
    /**
     * Gets the groups that have the given profile as allowed on the given object (assignment only)
     * @param object The object context to check
     * @param profileId The id of the profile
     * @return the groups that have the given profile as allowed on the given object (assignment only)
     */
    public Set<GroupIdentity> getOnlyAllowedGroups(Object object, String profileId)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getAllowedGroups(object, profileId))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Gets the groups that have denied profiles assigned on the given object
     * @param context The object context to test 
     * @return The map of denied groups (keys) with their assigned profiles (values)
     */
    public Map<GroupIdentity, Set<String>> getDeniedProfilesForGroups(Object context)
    {
        return _getFirstProfileAssignmentStorage(context)
                .map(pas -> pas.getDeniedProfilesForGroups(context))
                .orElse(Collections.EMPTY_MAP);
    }
    
    /**
     * Gets the groups that have the given profile as denied on the given object (assignment only)
     * @param object The object context to check
     * @param profileId The id of the profile
     * @return the groups that have the given profile as denied on the given object (assignment only)
     */
    public Set<GroupIdentity> getOnlyDeniedGroups(Object object, String profileId)
    {
        return _getFirstProfileAssignmentStorage(object)
                .map(pas -> pas.getDeniedGroups(object, profileId))
                .orElse(Collections.EMPTY_SET);
    }
    
    /**
     * Allows a group to a profile on a given object
     * @param group The group to add
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void allowProfileToGroup(GroupIdentity group, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addAllowedGroups(Collections.singleton(group), context, profileId));
    }
    
    /**
     * Denies a group to a profile on a given object
     * @param group The group to add
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void denyProfileToGroup(GroupIdentity group, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.addDeniedGroups(Collections.singleton(group), context, profileId));
    }
    
    /**
     * Removes the association between a group and an allowed profile on a given object
     * @param group The group to remove
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void removeAllowedProfileFromGroup(GroupIdentity group, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeAllowedGroups(Collections.singleton(group), context, profileId));
    }
    
    /**
     * Removes the association between a group and a denied profile on a given object
     * @param group The group to remove
     * @param profileId The id of the profile
     * @param context The object context
     */
    public void removeDeniedProfileFromGroup(GroupIdentity group, String profileId, Object context)
    {
        _getFirstProfileAssignmentStorage(context)
                .ifPresent(pas -> pas.removeDeniedGroups(Collections.singleton(group), context, profileId));
    }
    
    
    private Optional<ProfileAssignmentStorage> _getFirstProfileAssignmentStorage(Object object)
    {
        return _profileAssignmentStorageEP.getExtensionsIds().stream()
                .map(_profileAssignmentStorageEP::getExtension)
                .filter(pas -> pas.isSupported(object))
                .sorted(Comparator.comparing(ProfileAssignmentStorage::getPriority))
                .findFirst();
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
    public Set<UserIdentity> getAllowedUsers(String rightId, Object object) throws RightsException
    {
        // FIXME not enough, we have to take account of allowed/denied profiles on anonymous/anyconnected
        
        // Retrieve all profiles containing the right rightId
        Set<String> profileIds = _getProfiles(rightId);
        
        // Get the objects to check
        Set<Object> objects = _getConvertedObjects(object);
        
        // For each object, retrieve the allowed and denied users/groups
        Set<UserIdentity> allAllowedUsers = new HashSet<>();
        Set<UserIdentity> allDeniedUsers = new HashSet<>();
        Set<GroupIdentity> allAllowedGroups = new HashSet<>();
        Set<GroupIdentity> allDeniedGroups = new HashSet<>();
        
        for (Object obj : objects)
        {
            for (String controllerId : _accessControllerEP.getExtensionsIds())
            {
                AccessController accessController = _accessControllerEP.getExtension(controllerId);
                if (accessController.isSupported(obj))
                {
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
        
        // Retrieve the users from the allowed groups and add them all
        for (GroupIdentity allowedGroup : allAllowedGroups)
        {
            Set<UserIdentity> groupUsers = _groupManager.getGroup(allowedGroup).getUsers();
            allAllowedUsers.addAll(groupUsers);
        }
        
        // Remove the users from the denied users
        allAllowedUsers.removeAll(allDeniedUsers);
        
        // Remove the users from the denied groups
        for (GroupIdentity deniedGroup : allDeniedGroups)
        {
            Set<UserIdentity> groupUsers = _groupManager.getGroup(deniedGroup).getUsers();
            allAllowedUsers.removeAll(groupUsers);
        }
        
        return allAllowedUsers;
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
        // Retrieve groups the user belongs to
        Set<GroupIdentity> groups = _getGroups(userIdentity);
        
        // Get the objects to check
        Set<Object> objects = _getConvertedObjects(object);
        
        // Gets the allowed profiles
        Map<String, RightResult> rightResultByProfile = _getRightResultByProfile(userIdentity, groups, objects);
        Set<String> allowedProfiles = _getAllowedProfiles(rightResultByProfile);
        Set<String> deniedProfiles = _getDeniedProfiles(rightResultByProfile);
        
        
        Set<String> rights = new HashSet<>();
        // Iterate over allowed profiles and add all their rights
        for (String profileId : allowedProfiles)
        {
            Profile profile = getProfile(profileId);
            rights.addAll(profile.getRights());
        }
        
        // Then iterate over denied profiles and remove all their rights
        for (String profileId : deniedProfiles)
        {
            Profile profile = getProfile(profileId);
            rights.removeAll(profile.getRights());
        }
        
        return rights;
    }
    
    private Map<String, RightResult> _getRightResultByProfile(UserIdentity userIdentity, Set<GroupIdentity> groups, Set<Object> objects)
    {
        Map<String, RightResult> rightResultByProfile = new HashMap<>();
        for (Object obj : objects)
        {
            for (String controllerId : _accessControllerEP.getExtensionsIds())
            {
                AccessController accessController = _accessControllerEP.getExtension(controllerId);
                if (accessController.isSupported(obj))
                {
                    // Update the result map
                    Map<String, AccessResult> newValues = accessController.getPermissionsByProfile(userIdentity, groups, obj);
                    _updateRightResultMap(rightResultByProfile, newValues);
                }
            }
        }
        
        return rightResultByProfile;
    }
    
    private void _updateRightResultMap(Map<String, RightResult> originalMap, Map<String, AccessResult> newValues)
    {
        for (String profileId : newValues.keySet())
        {
            RightResult rightResult = _computeRight(newValues.get(profileId));
            // Update only if key profileId not present or it is a 'RIGHT_DENY' 
            if (!originalMap.containsKey(profileId) || RightResult.RIGHT_DENY.equals(rightResult))
            {
                originalMap.put(profileId, rightResult);
            }
        }
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
    
    
    /* ------------------------------------------------- */
    /* METHOD FOR CHECKING A USER HAS AT LEAST ONE RIGHT */
    /* ------------------------------------------------- */
    
    /**
     * Checks if the given user has at least one right on any object
     * @param userIdentity The user
     * @return true if the given user has at least one right on any object
     */
    public boolean hasAtLeastOneRight(UserIdentity userIdentity)
    {
        // Retrieve groups the user belongs to
        Set<GroupIdentity> groups = _getGroups(userIdentity);
        
        return _profileAssignmentStorageEP.hasOneAllowedProfile(userIdentity, groups);
    }
    
    
    /* ------------------- */
    /* PROFILES MANAGEMENT */
    /* ------------------- */
    
    /**
     * Add a new Profile
     * @param name the name of the new Profile
     * @return the newly created Profile
     * @throws RightsException if an error occurs.
     */
    public Profile addProfile(String name) throws RightsException
    {
        return addProfile(name, null);
    }
    
    /**
     * Add a new Profile
     * @param name the name of the new Profile
     * @param context the context. Can be null.
     * @return the newly created Profile
     * @throws RightsException if an error occurs.
     */
    public Profile addProfile(String name, String context) throws RightsException
    {
        String id = UUID.randomUUID().toString();

        return _addProfile(id, name, context);
    }
    
    private Profile _addProfile(String id, String name, String context) throws RightsException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try
        {
            connection = getSQLConnection();
            
            statement = connection.prepareStatement("INSERT INTO " + _tableProfile + " (Id, Label, Context) VALUES(?, ?, ?)");
            statement.setString(1, id);
            statement.setString(2, name);
            statement.setString(3, context);
            
            statement.executeUpdate();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(statement);
            ConnectionHelper.cleanup(connection);
        }
        
        return new Profile(id, name, context, _dataSourceId, _tableProfile, _tableProfileRights);
    }
    
    /**
     * Returns the Profile with the given Id
     * @param id the id oif the wanted Profile
     * @return the Profile with the given Id
     * @throws RightsException if an error occurs.
     */
    public Profile getProfile(String id) throws RightsException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try
        {
            connection = getSQLConnection();
            statement = connection.prepareStatement("SELECT Label, Context FROM " + _tableProfile + " WHERE Id = ?");
            statement.setString(1, id);

            rs = statement.executeQuery();

            if (rs.next())
            {
                String label = rs.getString("Label");
                String context = rs.getString("Context");
                return new Profile(id, label, context, _dataSourceId, _tableProfile, _tableProfileRights);
            }

            return null;
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(statement);
            ConnectionHelper.cleanup(connection);
        }
    }

    /**
     * Returns all known profiles
     * @return all known profiles
     * @throws RightsException if an error occurs.
     */
    public Set<Profile> getAllProfiles() throws RightsException
    {
        Set<Profile> profiles = new HashSet<>();

        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;

        try
        {
            connection = getSQLConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT Id, Label, Context FROM " + _tableProfile);

            while (rs.next())
            {
                String id = rs.getString("Id");
                String label = rs.getString("Label");
                String context = rs.getString("Context");
                profiles.add(new Profile(id, label, context, _dataSourceId, _tableProfile, _tableProfileRights));
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }

        return profiles;
    }
    
    /**
     * Returns profiles with no context
     * @return profiles with no context
     * @throws RightsException if an error occurs.
     */
    public Set<Profile> getProfiles() throws RightsException
    {
        return getProfiles(null);
    }
    
    /**
     * Returns profiles of a given context
     * @param context The context. Can be null. If null, the profiles with no context are returned.
     * @return profiles of a given context 
     * @throws RightsException if an error occurs.
     */
    public Set<Profile> getProfiles(String context) throws RightsException
    {
        Set<Profile> profiles = new HashSet<>();

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            connection = getSQLConnection();
            if (context == null)
            {
                stmt = connection.prepareStatement("SELECT Id, Label FROM " + _tableProfile + " WHERE Context is null");
            }
            else
            {
                stmt = connection.prepareStatement("SELECT Id, Label FROM " + _tableProfile + " WHERE Context=?");
                stmt.setString(1, context);
            }

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String id = rs.getString("Id");
                String label = rs.getString("Label");
                profiles.add(new Profile(id, label, context, _dataSourceId, _tableProfile, _tableProfileRights));
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }

        return profiles;
    }
    
    /**
     * Remove the given profile from database
     * @param id The id of the profile
     */
    public void removeProfile(String id)
    {
        // Remove the storing of the profile and the rights linked to it
        Connection connection = null;
        PreparedStatement statement = null;
        PreparedStatement statement2 = null;

        try
        {
            connection = getSQLConnection();

            statement = connection.prepareStatement("DELETE FROM " + _tableProfile + " WHERE Id = ?");
            statement.setString(1, id);
            statement.executeUpdate();

            statement2 = connection.prepareStatement("DELETE FROM " + _tableProfileRights + " WHERE Profile_Id = ?");
            statement2.setString(1, id);
            statement2.executeUpdate();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            ConnectionHelper.cleanup(statement);
            ConnectionHelper.cleanup(statement2);
            ConnectionHelper.cleanup(connection);
        }
        
        // Removes this profile in the profile assignment storages
        _profileAssignmentStorageEP.getExtensionsIds().stream()
            .map(_profileAssignmentStorageEP::getExtension)
            .forEach(pas -> pas.removeProfile(id));
    }

    /**
     * This method has to ensure that the user identified by its login will have all power by assigning a profile containing all rights.
     * @param user The user that will obtain all privilege on the right manager.
     * @param context The context of the right (cannot be null)
     * @param profileName The name of the profile to affect
     * @return The assigned profile id
     * @throws RightsException if an error occurs.
     */
    public String grantAllPrivileges(UserIdentity user, String context, String profileName) throws RightsException
    {
        // Create or get the temporary admin profile
        Profile adminProfile = null;
        for (Profile profile : getProfiles())
        {
            if (profileName.equals(profile.getId()))
            {
                adminProfile = profile;
                break;
            }
        }
        if (adminProfile == null)
        {
            adminProfile = _addProfile(profileName, profileName, null);
        }

        // Set all rights
        Collection currentRights = adminProfile.getRights();

        adminProfile.startUpdate();

        for (Object rightId : _rightsEP.getExtensionsIds())
        {
            if (!currentRights.contains(rightId))
            {
                adminProfile.addRight((String) rightId);
            }
        }

        adminProfile.endUpdate();

        // Assign the profile
        allowProfileToUser(user, adminProfile.getId(), context);

        return adminProfile.getId();
    }
    
    /**
     * This method has to ensure that the user identified by its login will have all power.
     * @param user The user that will obtain all privilege on the right manager.
     * @param context The context of the right (cannot be null)
     * @return The assigned profile id
     * @throws RightsException if an error occurs.
     */
    public String grantAllPrivileges(UserIdentity user, String context) throws RightsException
    {
        return grantAllPrivileges(user, context, __INITIAL_PROFILE_ID);
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
        if (_cacheProfilesTL.get() != null)
        {
            _cacheProfilesTL.set(null);
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
