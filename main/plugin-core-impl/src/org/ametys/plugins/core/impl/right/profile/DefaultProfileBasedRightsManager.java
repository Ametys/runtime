/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.plugins.core.impl.right.profile;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.group.Group;
import org.ametys.core.group.GroupDirectoryDAO;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.group.GroupListener;
import org.ametys.core.group.GroupManager;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.group.directory.ModifiableGroupDirectory;
import org.ametys.core.right.HierarchicalRightsHelper;
import org.ametys.core.right.InitializableRightsManager;
import org.ametys.core.right.RightContextConvertor;
import org.ametys.core.right.RightContextConvertorExtentionPoint;
import org.ametys.core.right.RightsContextPrefixExtensionPoint;
import org.ametys.core.right.RightsException;
import org.ametys.core.right.RightsExtensionPoint;
import org.ametys.core.right.profile.Profile;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserListener;
import org.ametys.core.user.UserManager;
import org.ametys.core.user.directory.ModifiableUserDirectory;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.request.RequestListener;
import org.ametys.runtime.request.RequestListenerManager;


/**
 * Standard implementation of the right manager from the core database that use profile.
 */
public class DefaultProfileBasedRightsManager extends AbstractLogEnabled implements InitializableRightsManager, ProfileBasedRightsManager, UserListener, GroupListener, Serviceable, Configurable, Initializable, RequestListener, ThreadSafe, Component
{
    private static final String __INITIAL_PROFILE_ID = "TEMPORARY ADMINISTRATOR";

    /** Avalon ServiceManager */
    protected ServiceManager _manager;
    
    /** Avalon SourceResolver */
    protected SourceResolver _resolver;
    
    /** The DAO for user populations */
    protected UserPopulationDAO _userPopulationDAO;
    
    /** The DAO for group directories */
    protected GroupDirectoryDAO _groupDirectoryDAO;

    /** The rights' list container */
    protected RightsExtensionPoint _rightsEP;
    /** The rights' context manager */
    protected RightsContextPrefixExtensionPoint _rightsContextPrefixEP;
    /** The rights' alias manager */
    protected RightContextConvertorExtentionPoint _rightContextConvertorExtPt;
    /** The users manager */
    protected UserManager _userManager;

    /** The groups manager */
    protected GroupManager _groupManager;

    /** The id of the data source to use */
    protected String _dataSourceId;
    
    /** The jdbc table name for profiles' list */
    protected String _tableProfile;

    /** The jdbc table name for profiles' rights */
    protected String _tableProfileRights;

    /** The jdbc table name for users' profiles */
    protected String _tableUserRights;

    /** The jdbc table name for groups' profiles */
    protected String _tableGroupRights;

    /* Rights cache
     *  { UserIdentity : { Right : { Context : List(SubContext)
     *                      }
     *            }
     *  }
     *  Si il y a une entrée user,right,context+subcontext on a le droit
     *  La décomposition context+subcontext n'est pas connue : 
     *  Quand on cherche le context /pages/default/default/fr/toto,
     *  il faut cherche le combos suivantes :
     *  "/pages/default/default/fr/toto" + ""
     *  "/pages/default/default/fr" + "toto"
     *  "/pages/default/default" + "fr/toto"
     *  "/pages/default" + "default/fr/toto"
     *  "/pages" + "default/default/fr/toto"
     *  "" + "pages/default/default/fr/toto"
     */
    private final ThreadLocal<Map<UserIdentity, Map<String, Map<String, List<String>>>>> _cacheTL = new ThreadLocal<>();

    /* Identique au précédent si ce n'est qu'il ne traite pas les sous-contextes
     *  { UserIdentity : { Right : { Context : Boolean
     *                      }
     *            }
     *  }
     *  
     *  Il complète le 1er car il permet de faire des requètes sql avec une hierarchie montant.
     *  Le premier cache fait : context/* alors que celui là fait les contextes parents.
     *  On ne peut pas mettre ces infos là dans le 1er cache on ne demande pas /* dans les parents
     *  sinon ça reviendrait à tout lire à chaque fois !
     */
    private final ThreadLocal<Map<UserIdentity, Map<String, Map<String, Boolean>>>> _cache2TL = new ThreadLocal<>();
    
    /*
     * Cache des droits par utilisateur (droits affectés à l'utilisateur uniquement) sur un contexte donné.
     * UserIdentity -> Contexte -> Set<RightId>
     */
    private final ThreadLocal<Map<UserIdentity, Map<String, Set<String>>>> _userRightCache = new ThreadLocal<>();
    
    /*
     * Cache des droits par utilisateur (droits issus des groupes uniquement) sur un contexte donné.
     * UserIdentity -> Contexte -> Set<RightId>
     */
    private final ThreadLocal<Map<UserIdentity, Map<String, Set<String>>>> _groupRightCache = new ThreadLocal<>();

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
        _rightsEP = (RightsExtensionPoint) _manager.lookup(RightsExtensionPoint.ROLE);
        _rightsContextPrefixEP = (RightsContextPrefixExtensionPoint) manager.lookup(RightsContextPrefixExtensionPoint.ROLE);
        _userManager = (UserManager) _manager.lookup(UserManager.ROLE);
        _groupManager = (GroupManager) _manager.lookup(GroupManager.ROLE);
        _rightContextConvertorExtPt = (RightContextConvertorExtentionPoint) _manager.lookup(RightContextConvertorExtentionPoint.ROLE);
        _resolver = (SourceResolver) _manager.lookup(SourceResolver.ROLE);
        _userPopulationDAO = (UserPopulationDAO) _manager.lookup(UserPopulationDAO.ROLE);
        _groupDirectoryDAO = (GroupDirectoryDAO) _manager.lookup(GroupDirectoryDAO.ROLE);
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
        _tableUserRights = configuration.getChild("table-profile-user").getValue("Rights_UserRights");
        _tableGroupRights = configuration.getChild("table-profile-group").getValue("Rights_GroupRights");
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
                String message = "Error in " + DefaultProfileBasedRightsManager.class.getName() + " configuration: attribute 'id' and elements 'label', 'description' and 'category' are mandatory.";
                getLogger().error(message);
                throw new ConfigurationException(message, configuration);
            }

            _rightsEP.addRight(id, i18nLabel, i18nDescription, i18nCategory);
        }
    }
    
    /**
     * Get the connection to the database 
     * @return the SQL connection
     */
    protected Connection getSQLConnection ()
    {
        return ConnectionHelper.getConnection(_dataSourceId);
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
        if (_userRightCache.get() != null)
        {
            _userRightCache.set(null);
        }
        if (_groupRightCache.get() != null)
        {
            _groupRightCache.set(null);
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
    public Set<UserIdentity> getGrantedUsers(String right, String context) throws RightsException
    {
        try
        {
            Set<UserIdentity> users = new HashSet<>();

            Set<String> convertedContexts = getAliasContext(context);
            for (String convertContext : convertedContexts)
            {
                Set<UserIdentity> addUsers = internalGetGrantedUsers(right, convertContext);
                users.addAll(addUsers);
            }

            return users;
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
    }
    
    /**
     * Get the list of users that have the given right on the given context.
     * @param right The name of the right to use. Cannot be null.
     * @param context The context to test the right.<br>May be null, in which case the returned Set contains all granted users, whatever the context.
     * @return The list of users granted with that right as a Set of {@link UserIdentity}.
     * @throws SQLException if an error occurs retrieving the rights from the database.
     */
    protected Set<UserIdentity> internalGetGrantedUsers(String right, String context) throws SQLException
    {
        String lcContext = getFullContext (context);

        Set<UserIdentity> logins = new HashSet<>();

        logins.addAll(getGrantedUsersOnly(right, lcContext));
        logins.addAll(getGrantedGroupsOnly(right, lcContext));

        return logins;
    }

    @Override
    public Set<UserIdentity> getGrantedUsers(String context) throws RightsException
    {
        try
        {
            Set<UserIdentity> users = new HashSet<>();
            
            Set<String> convertedContexts = getAliasContext(context);
            for (String convertContext : convertedContexts)
            {
                Set<UserIdentity> addUsers = internalGetGrantedUsers(convertContext);
                users.addAll(addUsers);
            }
            
            return users;
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
    }
    
    /**
     * Get the list of users that have at least one right on the given context.
     * @param context The context to test the right.<br>May be null, in which case the returned Set contains all granted users, whatever the context.
     * @return The list of users granted that have a least one right as a Set of {@link UserIdentity}.
     * @throws SQLException if an error occurs retrieving the rights from the database.
     */
    protected Set<UserIdentity> internalGetGrantedUsers(String context) throws SQLException
    {
        String lcContext = getFullContext (context);

        Set<UserIdentity> users = new HashSet<>();
        
        users.addAll(getGrantedUsersOnly(lcContext));
        users.addAll(getGrantedGroupsOnly(lcContext));
        
        return users;
    }
    
    @Override
    public Set<String> getUserRights(UserIdentity user, String context) throws RightsException
    {
        try
        {
            Set<String> rights = new HashSet<>();

            if (user == null)
            {
                return rights;
            }

            Set<String> convertedContexts = getAliasContext(context);
            for (String convertContext : convertedContexts)
            {
                Set<String> addUsers = internalGetUserRights(user, convertContext);
                rights.addAll(addUsers);
            }

            return rights;
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
    }

    /**
     * Get the list of a user's rights in a particular context.
     * @param user the user. Cannot be null.
     * @param context The context to test the right
     * @return The list of rights as a Set of String (id).
     * @throws SQLException if an error occurs executing the queries.
     */
    protected Set<String> internalGetUserRights(UserIdentity user, String context) throws SQLException
    {
        String lcContext = getFullContext (context);

        Set<String> rights = new HashSet<>();

        rights.addAll(getUsersOnlyRights(user, lcContext));
        rights.addAll(getGroupsOnlyRights(user, lcContext));

        return rights;
    }

    @Override
    public Map<String, Set<String>> getUserRights(UserIdentity user) throws RightsException
    {
        if (user == null)
        {
            return new HashMap<>();
        }

        try
        {
            Map<String, Set<String>> rights = getUsersOnlyRights(user);
            Map<String, Set<String>> groupRights = getGroupsOnlyRights(user);

            for (String context : groupRights.keySet())
            {
                if (!rights.containsKey(context))
                {
                    rights.put(context, new HashSet<String>());
                }
                rights.get(context).addAll(groupRights.get(context));
            }

            return rights;
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
    }

    @Override
    public String grantAllPrivileges(UserIdentity user, String context, String profileName)
    {
        // Create or get the temporary admin profile
        Profile adminProfile = null;
        for (Profile profile : getProfiles())
        {
            if (profileName.equals(profile.getName()))
            {
                adminProfile = profile;
                break;
            }
        }
        if (adminProfile == null)
        {
            adminProfile = addProfile(profileName);
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
        addUserRight(user, context, adminProfile.getId());

        return adminProfile.getId();
    }

    @Override
    public String grantAllPrivileges(UserIdentity user, String context)
    {
        return grantAllPrivileges(user, context, __INITIAL_PROFILE_ID);
    }

    @Override
    public RightResult hasRight(UserIdentity user, String right, String context)
    {
        if (user == null)
        {
            return RightResult.RIGHT_NOK;
        }

        Set<String> convertedContexts = getAliasContext(context);
        for (String convertContext : convertedContexts)
        {
            RightResult hasRight = internalHasRight(user, right, convertContext);
            if (hasRight == RightResult.RIGHT_OK)
            {
                return RightResult.RIGHT_OK;
            }
        }

        return RightResult.RIGHT_NOK;
    }

    /**
     * Check a permission for a user, in a given context.<br>
     * @param user The user. Cannot be null.
     * @param right the name of the right to check. Cannot be null.
     * @param context the right context
     * @return RIGHT_OK, RIGHT_NOK or RIGHT_UNKNOWN
     * @throws RightsException if an error occurs.
     */
    protected RightResult internalHasRight(UserIdentity user, String right, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        try
        {
            return hasUserRightInCache(user, right, lcContext, "");
        }
        catch (RightOnContextNotInCacheException e)
        {
            getLogger().debug("No find entry in cache for [" + user + ", " + right + ", " + lcContext + "]");
        }

        try
        {
            return hasUserRightInCache2(user, right, lcContext);
        }
        catch (RightOnContextNotInCacheException e)
        {
            getLogger().debug("No find entry in cache2 for [" + user + ", " + right + ", " + lcContext + "]");
        }

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            RightResult userResult = lcContext != null ? hasUserOnlyRight(user, right, lcContext) : hasUserOnlyRight(user, right);
            if (userResult == RightResult.RIGHT_OK)
            {
                // Droit en tant qu'utilisateur
                return RightResult.RIGHT_OK;
            }

            // Puis parmi les droits affectés aux groupes auxquels
            // appartient l'utilisateur
            RightResult groupResult = lcContext != null ? hasGroupOnlyRight(user, right, lcContext) : hasGroupOnlyRight(user, right);
            if (groupResult == RightResult.RIGHT_OK)
            {
                // Droit en tant que groupe d'utilisateurs
                return RightResult.RIGHT_OK;
            }

            return RightResult.RIGHT_UNKNOWN;
        }
        catch (SQLException se)
        {
            getLogger().error("Error communicating with database", se);
            throw new RightsException("Error communicating with database", se);
        }
    }

    /* METHODES AJOUTEES PAR LE RIGHT MANAGER */

    /**
     * Get a Set of alias for the given context
     * @param initialContext The initial context
     * @return alias for the given context
     */
    protected Set<String> getAliasContext (String initialContext)
    {
        Set<String> convertedContexts = new HashSet<>();
        convertedContexts.add(initialContext);

        Set<String> ids = _rightContextConvertorExtPt.getExtensionsIds();
        for (String id : ids)
        {
            RightContextConvertor convertor = _rightContextConvertorExtPt.getExtension(id);
            convertedContexts.addAll(convertor.convertContext(initialContext));
        }

        return convertedContexts;
    }

    /**
     * Returns a Set containing all profiles for a given user and a context
     * 
     * @param user the concerned user
     * @param context the context
     * @return a Set containing all profiles for a given user and a context
     * @throws RightsException if an error occurs.
     */
    public Set<String> getProfilesByUser(UserIdentity user, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Set<String> profiles = new HashSet<>();

        if (user == null)
        {
            return new HashSet<>();
        }

        Connection connection = getSQLConnection();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT UR.Profile_Id " + "FROM " + _tableUserRights + " UR WHERE UR.Login = ? AND UR.UserPopulation_Id = ? AND LOWER(UR.Context) = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());
            stmt.setString(3, lcContext);

            // Logger la requête
            getLogger().debug(sql + "\n[" + user.getLogin() + ", " + user.getPopulationId() + ", " + lcContext + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String profile = rs.getString(1);
                profiles.add(profile);
            }
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
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
     * Returns a Set containing all users for a context and a profile
     * 
     * @param profileID The profile id
     * @param context The context
     * @return a Set containing all users for a context and a profile
     * @throws RightsException if an error occurs.
     */
    public Set<User> getUsersByContextAndProfile(String profileID, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Set<User> users = new HashSet<>();

        Connection connection = getSQLConnection();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT DISTINCT Login, UserPopulation_Id " + "FROM " + _tableUserRights + " WHERE Profile_Id = ? AND LOWER(Context) = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setInt(1, Integer.parseInt(profileID));
            stmt.setString(2, lcContext);

            // Logger la requête
            getLogger().debug(sql + "\n[" + profileID + ", " + lcContext + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String login = rs.getString(1);
                String populationId = rs.getString(2);
                User user = _userManager.getUser(populationId, login);
                if (user != null)
                {
                    users.add(user);
                }
            }
        }
        catch (NumberFormatException ex)
        {
            getLogger().error("Profile ID must be an integer.", ex);
            throw new RightsException("Profile ID must be an integer.", ex);
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }

        return users;
    }

    /**
     * Returns a Set containing groups explicitly linked with the given Context and profile
     * 
     * @param profileID The profile id
     * @param context the context
     * @return a Set containing groups explicitly linked with the given Context and profile
     * @throws RightsException if an error occurs.
     */
    public Set<Group> getGroupsByContextAndProfile(String profileID, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Set<Group> groups = new HashSet<>();

        Connection connection = getSQLConnection();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT DISTINCT Group_Id, GroupDirectory_Id " + "FROM " + _tableGroupRights + " WHERE Profile_Id = ? AND LOWER(Context) = ? ";

            stmt = connection.prepareStatement(sql);

            stmt.setInt(1, Integer.parseInt(profileID));
            stmt.setString(2, lcContext);

            // Logger la requête
            getLogger().debug(sql + "\n[" + profileID + ", " + lcContext + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String groupId = rs.getString(1);
                String groupDirectoryId = rs.getString(2);
                Group group = _groupManager.getGroup(groupDirectoryId, groupId);
                if (group != null)
                {
                    groups.add(group);
                }
            }
        }
        catch (NumberFormatException ex)
        {
            getLogger().error("Profile ID must be an integer.", ex);
            throw new RightsException("Profile ID must be an integer.", ex);
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }

        return groups;
    }

    /**
     * Returns a Set containing all contexts for a user and a profile
     * 
     * @param user The user
     * @param profileID The profile
     * @return a Set containing all contexts for a user and a profile
     * @throws RightsException if an error occurs.
     */
    public Set<String> getContextByUserAndProfile(UserIdentity user, String profileID) throws RightsException
    {
        Set<String> contexts = new HashSet<>();

        if (user == null)
        {
            return contexts;
        }

        Connection connection = getSQLConnection();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT UR.Context " + "FROM " + _tableUserRights + " UR WHERE UR.Login = ? AND UR.UserPopulation_Id = ? AND UR.Profile_Id = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());
            stmt.setInt(3, Integer.parseInt(profileID));

            // Logger la requête
            getLogger().debug(sql + "\n[" + user.getLogin() + ", " + user.getPopulationId() + ", " + profileID + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                contexts.add(rs.getString(1));
            }
        }
        catch (NumberFormatException ex)
        {
            getLogger().error("Profile ID must be an integer.", ex);
            throw new RightsException("Profile ID must be an integer.", ex);
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }

        return contexts;
    }


    /**
     * Returns a Map containing all profiles and context for a given user
     * 
     * @param user the concerned user
     * @return a Map as (Profile Id, set of context)
     * @throws RightsException if an error occurs.
     */
    public Map<String, Set<String>> getProfilesAndContextByUser (UserIdentity user) throws RightsException
    {
        try
        {
            if (user == null)
            {
                return new HashMap<>();
            }

            Map<String, Set<String>> userProfiles = getUsersOnlyProfiles(user);
            Map<String, Set<String>> groupProfiles = getGroupsOnlyProfiles(user);

            for (String context : groupProfiles.keySet())
            {
                if (!userProfiles.containsKey(context))
                {
                    userProfiles.put(context, new HashSet<String>());
                }
                userProfiles.get(context).addAll(groupProfiles.get(context));
            }

            return userProfiles;
        }

        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
    }

    /**
     * Returns a Set containing all profiles for a group and a context
     * 
     * @param group the group
     * @param context the context
     * @return a Set containing all profiles for a group and a context
     * @throws RightsException if an error occurs.
     */
    public Set<String> getProfilesByGroup(GroupIdentity group, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Set<String> profiles = new HashSet<>();

        Connection connection = getSQLConnection();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT GR.Profile_Id " + "FROM " + _tableGroupRights + " GR WHERE GR.Group_Id = ? AND GR.GroupDirectory_Id = ? AND LOWER(GR.Context) = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, group.getId());
            stmt.setString(2, group.getDirectoryId());
            stmt.setString(3, lcContext);

            // Logger la requête
            getLogger().debug(sql + "\n[" + group.getId() + ", " + group.getDirectoryId() + ", " + lcContext + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String profile = rs.getString(1);
                profiles.add(profile);
            }
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
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
     * Returns a Set containing all contexts for a user and a profile
     * 
     * @param group The group
     * @param profileID The profile
     * @return a Set containing all contexts for a user and a profile
     * @throws RightsException if an error occurs.
     */
    public Set<String> getContextByGroupAndProfile(GroupIdentity group, String profileID) throws RightsException
    {
        Set<String> contexts = new HashSet<>();

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT GR.Context " + "FROM " + _tableGroupRights + " GR WHERE GR.Group_Id = ? AND GR.GroupDirectory_Id = ? AND GR.Profile_Id = ?";

            stmt = connection.prepareStatement(sql);
            stmt.setString(1, group.getId());
            stmt.setString(2, group.getDirectoryId());
            stmt.setInt(3, Integer.parseInt(profileID));

            // Logger la requête
            if (getLogger().isInfoEnabled())
            {
                getLogger().debug(sql + "\n[" + group.getId() + ", " + group.getDirectoryId() + ", " + profileID + "]");
            }

            rs = stmt.executeQuery();

            while (rs.next())
            {
                contexts.add(rs.getString(1));
            }
        }
        catch (NumberFormatException ex)
        {
            getLogger().error("Profile ID must be an integer.", ex);
            throw new RightsException("Profile ID must be an integer.", ex);
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }

        return contexts;
    }

    @Override
    public void addUserRight(UserIdentity user, String context, String profileId) throws RightsException
    {
        String lcContext = getFullContext (context);

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            // Check if already exists
            String sql = "SELECT Profile_Id FROM " + _tableUserRights + " WHERE Profile_Id=? and Login=? AND UserPopulation_Id=? and Context=?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(profileId));
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getPopulationId());
            stmt.setString(4, lcContext);

            rs = stmt.executeQuery();
            if (rs.next())
            {
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("Login " + user + " has already profile " + profileId + " on context " +  lcContext + "");
                }
                return;
            }

            ConnectionHelper.cleanup(stmt);

            sql = "INSERT INTO " + _tableUserRights + " (Profile_Id, Login, UserPopulation_Id, Context) VALUES(?, ?, ?, ?)";

            stmt = connection.prepareStatement(sql);

            stmt.setInt(1, Integer.parseInt(profileId));
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getPopulationId());
            stmt.setString(4, lcContext);

            // Logger la requête
            getLogger().debug(sql + "\n[" + profileId + ", " + user.getLogin() + ", " + user.getPopulationId() + ", " + lcContext + "]");

            stmt.executeUpdate();
            
            // Query OK: clear the user right cache.
            _clearUserRightCache();
        }
        catch (NumberFormatException ex)
        {
            getLogger().error("Profile ID must be an integer.", ex);
            throw new RightsException("Profile ID must be an integer.", ex);
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    @Override
    public void addGroupRight(GroupIdentity group, String context, String profileId) throws RightsException
    {
        String lcContext = getFullContext (context);

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            // Check if already exists
            String sql = "SELECT Profile_Id FROM " + _tableGroupRights + " WHERE Profile_Id=? and Group_Id=? and GroupDirectory_Id=? and Context=?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(profileId));
            stmt.setString(2, group.getId());
            stmt.setString(3, group.getDirectoryId());
            stmt.setString(4, lcContext);

            rs = stmt.executeQuery();
            if (rs.next())
            {
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("Group of id " + group + " has already profile " + profileId + " on context " +  lcContext + "");
                }
                return;
            }

            ConnectionHelper.cleanup(stmt);

            sql = "INSERT INTO " + _tableGroupRights + " (Profile_Id, Group_Id, GroupDirectory_Id, Context) VALUES(?, ?, ?, ?)";

            stmt = connection.prepareStatement(sql);

            stmt.setInt(1, Integer.parseInt(profileId));
            stmt.setString(2, group.getId());
            stmt.setString(3, group.getDirectoryId());
            stmt.setString(4, lcContext);

            // Logger la requête
            getLogger().debug(sql + "\n[" + profileId + ", " + group.getId() + ", " + group.getDirectoryId() + ", " + lcContext + "]");

            stmt.executeUpdate();
            
            // Query OK: clear the group right cache.
            _clearGroupRightCache();
        }
        catch (NumberFormatException ex)
        {
            getLogger().error("Profile ID must be an integer.", ex);
            throw new RightsException("Profile ID must be an integer.", ex);
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    @Override
    public void removeUserProfile(UserIdentity user, String profileId, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "DELETE FROM " + _tableUserRights + " WHERE Login = ? AND UserPopulation_Id = ? AND Profile_Id = ? AND LOWER(Context) = ? ";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());
            stmt.setInt(3, Integer.parseInt(profileId));
            stmt.setString(4, lcContext);

            // Logger la requête
            getLogger().debug(sql + "\n[" + user.getLogin() + ", " + user.getPopulationId() + ", " + profileId + ", " + lcContext + "]");

            stmt.executeUpdate();
            
            // Query OK: clear the user right cache.
            _clearUserRightCache();
        }
        catch (NumberFormatException ex)
        {
            getLogger().error("Profile ID must be an integer.", ex);
            throw new RightsException("Profile ID must be an integer.", ex);
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    @Override
    public void removeUserProfiles(UserIdentity user, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "DELETE FROM " + _tableUserRights + " WHERE Login = ? AND UserPopulation_Id = ?";
            if (lcContext != null)
            {
                sql += " AND LOWER(Context) = ?";
            }

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());
            if (lcContext != null)
            {
                stmt.setString(3, lcContext);
            }

            // Logger la requête
            getLogger().debug(sql + "\n[" + user.getLogin() + ", " + user.getPopulationId() + ", " + lcContext + "]");

            stmt.executeUpdate();
            
            // Query OK: clear the user right cache.
            _clearUserRightCache();
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    @Override
    public void updateContext(String oldContext, String newContext) throws RightsException
    {
        String lcOldContext = getFullContext(oldContext);
        String lcNewContext = getFullContext(newContext);

        Connection connection = getSQLConnection();

        try
        {
            updateContext(lcOldContext, lcNewContext, connection);
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
        }
    }

    @Override
    public void removeAll(String context) throws RightsException
    {
        String lcContext = getFullContext(context);

        Connection connection = getSQLConnection();

        try
        {
            removeAll(lcContext, connection);
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
        }
    }

    @Override
    public void removeGroupProfile(GroupIdentity group, String profileId, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;

        try
        {
            String sql = "DELETE FROM " + _tableGroupRights + " WHERE Group_Id = ? AND GroupDirectory_Id = ? AND Profile_Id = ? AND LOWER(Context) = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, group.getId());
            stmt.setString(2, group.getDirectoryId());
            stmt.setInt(3, Integer.parseInt(profileId));
            stmt.setString(4, lcContext);

            // Logger la requête
            getLogger().debug(sql + "\n[" + group.getId() + ", " + group.getDirectoryId() + ", " + profileId + ", " + lcContext + "]");

            stmt.executeUpdate();
            
            // Query OK: clear the group right cache.
            _clearGroupRightCache();
        }
        catch (NumberFormatException ex)
        {
            getLogger().error("Profile ID must be an integer.", ex);
            throw new RightsException("Profile ID must be an integer.", ex);
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    @Override
    public void removeGroupProfiles(GroupIdentity group, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "DELETE FROM " + _tableGroupRights + " WHERE Group_Id = ? AND groupDirectory_Id = ?";

            if (lcContext != null)
            {
                sql += " AND LOWER(Context) = ?";
            }

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, group.getId());
            stmt.setString(2, group.getDirectoryId());
            if (lcContext != null)
            {
                stmt.setString(3, lcContext);
            }

            // Logger la requête
            getLogger().debug(sql + "\n[" + group.getId() + ", " + group.getDirectoryId() + ", " + lcContext + "]");

            stmt.executeUpdate();
            
            // Query OK: clear the group right cache.
            _clearGroupRightCache();
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    /**
     * Returns a Set containing users explicitly linked with the given Context
     * @param context the context
     * @return a Set containing users explicitly linked with the given Context
     * @throws RightsException if an error occurs.
     */
    public Set<User> getUsersByContext(String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Set<User> users = new HashSet<>();

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT DISTINCT Login, UserPopulation_Id " + "FROM " + _tableUserRights + " WHERE LOWER(Context) = ? ";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, lcContext);

            // Logger la requête
            getLogger().debug(sql + "\n[" + lcContext + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String login = rs.getString(1);
                String populationId = rs.getString(2);
                User principal = _userManager.getUser(populationId, login);
                if (principal != null)
                {
                    users.add(principal);
                }
            }
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }

        return users;
    }

    /**
     * Returns a Set containing groups explicitly linked with the given Context
     * 
     * @param context the context
     * @return a Set containing groups explicitly linked with the given Context
     * @throws RightsException if an error occurs.
     */
    public Set<Group> getGroupsByContext(String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Set<Group> groups = new HashSet<>();

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT DISTINCT Group_Id, GroupDirectory_Id " + "FROM " + _tableGroupRights + " WHERE LOWER(Context) = ? ";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, lcContext);

            // Logger la requête
            getLogger().debug(sql + "\n[" + lcContext + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String groupId = rs.getString(1);
                String groupDirectoryId = rs.getString(2);
                Group group = _groupManager.getGroup(groupDirectoryId, groupId);
                if (group != null)
                {
                    groups.add(group);
                }
            }
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }

        return groups;
    }

    /**
     * Modify a context for all users and groups with any profile.
     * @param fullOldContext the context to modify, full version.
     * @param fullNewContext the new context, full version.
     * @param connection a connection on the database.
     * @throws SQLException if an error occurs.
     */
    protected void updateContext(String fullOldContext, String fullNewContext, Connection connection) throws SQLException
    {
        PreparedStatement stmt = null;

        try
        {
            // Update the context on the group entries.
            String sql = "UPDATE " + _tableGroupRights + " SET Context = ? WHERE LOWER(Context) = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, fullNewContext);
            stmt.setString(2, fullOldContext);

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql + "\n[" + fullNewContext + ", " + fullOldContext + "]");
            }
            stmt.executeUpdate();
            
            // Clear the group right cache.
            _clearGroupRightCache();

            ConnectionHelper.cleanup(stmt);

            // Update the context on the user entries.
            sql = "UPDATE " + _tableUserRights + " SET Context = ? WHERE LOWER(Context) = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, fullNewContext);
            stmt.setString(2, fullOldContext);

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql + "\n[" + fullNewContext + ", " + fullOldContext + "]");
            }
            stmt.executeUpdate();
            
            // Clear the user right cache.
            _clearUserRightCache();
        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
        }
    }

    /**
     * Remove all the assignments for a given right context.
     * @param fullContext the full context to remove.
     * @param connection the database connection.
     * @throws SQLException if an error occurs.
     */
    protected void removeAll(String fullContext, Connection connection) throws SQLException
    {
        PreparedStatement stmt = null;

        try
        {
            String sql = "DELETE FROM " + _tableGroupRights + " WHERE LOWER(Context) = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, fullContext);
            getLogger().debug(sql + "\n[" + fullContext + "]");
            stmt.executeUpdate();

            ConnectionHelper.cleanup(stmt);
            
            // Clear the group right cache.
            _clearGroupRightCache();

            sql = "DELETE FROM " + _tableUserRights + " WHERE LOWER(Context) = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, fullContext);
            getLogger().debug(sql + "\n[" + fullContext + "]");
            stmt.executeUpdate();
            
            // Clear the user right cache.
            _clearUserRightCache();
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
        }
    }

    /* -------------------------------------- */
    /* METHODES PRIVEES POUR LES UTILISATEURS */
    /* -------------------------------------- */

    /**
     * Search if the user has the specified right. This function search in the
     * rights assigned directly to the user.
     * 
     * @param user the identity of the user
     * @param right the right to verify
     * 
     * @return RIGHT_OK, RIGHT_NOK or RIGHT_UNKNOWN
     * 
     * @throws SQLException in case of connection error with the database
     */
    private RightResult hasUserOnlyRight(UserIdentity user, String right) throws SQLException
    {
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT UR.Login " + "FROM " + _tableProfileRights + " PR, " + _tableUserRights + " UR WHERE PR.Right_Id = ? " + "AND UR.Profile_Id = PR.Profile_Id " + "AND UR.Login = ? AND UR.UserPopulation_Id = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, right);
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getPopulationId());

            // Logger la requête
            if (getLogger().isInfoEnabled())
            {
                getLogger().debug(sql + "\n[" + right + ", " + user.getLogin() + ", " + user.getPopulationId() + "]");
            }

            rs = stmt.executeQuery();

            if (rs.next())
            {
                return RightResult.RIGHT_OK;
            }

            return RightResult.RIGHT_UNKNOWN;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    private String buildSQLStatementForUser(String context)
    {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT LOWER(UR.Context) ");
        sql.append("FROM " + _tableProfileRights + " PR, " + _tableUserRights + " UR ");
        sql.append("WHERE PR.Right_Id = ? ");
        sql.append("AND UR.Profile_Id = PR.Profile_Id AND UR.Login = ? AND UR.UserPopulation_Id = ?");
        sql.append("AND (LOWER(UR.Context) = ? OR LOWER(UR.Context) LIKE ? ");

        String currentContext = HierarchicalRightsHelper.getParentContext(context);
        while (currentContext != null)
        {
            sql.append("OR UR.Context = ? ");
            currentContext = HierarchicalRightsHelper.getParentContext(currentContext);
        }

        sql.append(")");

        return sql.toString();
    }

    /**
     * Search if the user has the specified right in the given context and the
     * other subcontext where the user has this right. This function search in
     * the rights assigned directly to the user. The result is stored in cache.
     * 
     * @param user the identity of the user
     * @param right the right to verify
     * @param context the context
     * 
     * @return RIGHT_OK or RIGHT_UNKNOWN
     * 
     * @throws SQLException in case of connection error with the databse
     */
    private RightResult hasUserOnlyRight(UserIdentity user, String right, String context) throws SQLException
    {
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = buildSQLStatementForUser(context);
            stmt = connection.prepareStatement(sql);

            stmt.setString(1, right);
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getPopulationId());
            stmt.setString(4, context);
            stmt.setString(5, context + "/%");

            int i = 0;
            String currentContext = HierarchicalRightsHelper.getParentContext(context);
            while (currentContext != null)
            {
                stmt.setString (6 + i, currentContext);
                currentContext = HierarchicalRightsHelper.getParentContext(currentContext);
                i++;
            }

            // Logger la requête
            if (getLogger().isInfoEnabled())
            {
                getLogger().debug(sql + "\n[" + right + ", " + user.getLogin() + ", " + user.getPopulationId() + (context != null ? "," + context : "") + "]");
            }

            rs = stmt.executeQuery();

            Map<String, List<String>> mapContext = getCacheContext(user, right);
            List<String> contextList = mapContext.get(context);
            if (contextList == null)
            {
                contextList = new ArrayList<>();
                mapContext.put(context, contextList);
            }

            Map<String, Boolean> mapContext2 = _prepareCache2(user, right, context);

            boolean hasRight = false;
            while (rs.next())
            {
                String strContext = rs.getString(1);
                if (context == null || strContext == null || strContext.equals(context))
                {
                    hasRight = true;
                    contextList.add("");
                }
                else if (strContext.length() < context.length())
                {
                    mapContext2.put(strContext, Boolean.TRUE);
                }
                else
                {
                    contextList.add(strContext.substring(context.length() + 1));
                }
            }

            if (hasRight)
            {
                return RightResult.RIGHT_OK;
            }
            else
            {
                return RightResult.RIGHT_UNKNOWN;
            }
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    private Map<String, Boolean> _prepareCache2(UserIdentity user, String right, String context)
    {
        Map<String, Boolean> mapContext2 = getCache2Context(user, right);

        String currentContext = HierarchicalRightsHelper.getParentContext(context);
        while (currentContext != null)
        {
            if (mapContext2.get(currentContext) == null)
            {
                mapContext2.put(currentContext, Boolean.FALSE);
            }

            currentContext = HierarchicalRightsHelper.getParentContext(currentContext);
        }
        return mapContext2;
    }

    private Set<UserIdentity> getGrantedUsersOnly(String right, String context) throws SQLException
    {
        Set<UserIdentity> users = new HashSet<>();

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT DISTINCT UR.Login, UR.UserPopulation_Id " + "FROM " + _tableProfileRights + " PR, " + _tableUserRights + " UR WHERE UR.Profile_Id = PR.Profile_Id " + "AND PR.Right_Id = ? ";

            if (context != null)
            {
                sql += " AND LOWER(UR.Context) = ?";
            }

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, right);

            if (context != null)
            {
                stmt.setString(2, context);
            }

            // Logger la requête
            getLogger().debug(sql + "\n[" + right + (context != null ? "," + context : "") + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                UserIdentity user = new UserIdentity(rs.getString(1), rs.getString(2));
                users.add(user);
            }

            return users;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }
    
    private Set<UserIdentity> getGrantedUsersOnly(String context) throws SQLException
    {
        Set<UserIdentity> users = new HashSet<>();
        
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            StringBuilder sql = new StringBuilder("SELECT DISTINCT UR.Login, UR.UserPopulation_Id " + "FROM " + _tableProfileRights + " PR, " + _tableUserRights + " UR WHERE UR.Profile_Id = PR.Profile_Id ");

            if (context != null)
            {
                sql.append("AND LOWER(UR.Context) ");
                sql.append(_getCondition(context));
                sql.append(" ?");
            }

            stmt = connection.prepareStatement(sql.toString());

            if (context != null)
            {
                // LIKE query
                stmt.setString(1, context.replace('*', '%'));
            }

            // Logger la requête
            getLogger().debug(sql.toString() + "\n[" + (context != null ? "," + context : "") + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                UserIdentity user = new UserIdentity(rs.getString(1), rs.getString(2));
                users.add(user);
            }

            return users;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }
    
    private Set<String> getUsersOnlyRights(UserIdentity user, String context) throws SQLException
    {
        Set<String> rights = new HashSet<>();
        
        if (user == null)
        {
            return rights;
        }
        
        rights = _getUserRightsInCache(user, context);
        if (rights == null)
        {
            rights = _getUsersOnlyRightsFromDb(user, context);
            
            _setUserRightsInCache(user, context, rights);
        }
        
        return rights;
    }

    private Set<String> _getUsersOnlyRightsFromDb(UserIdentity user, String context) throws SQLException
    {
        Set<String> rights = new HashSet<>();

        if (user == null)
        {
            return rights;
        }

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux utilisateurs
            StringBuffer sql = new StringBuffer();

            sql.append("SELECT DISTINCT PR.Right_id ");
            sql.append("FROM " + _tableProfileRights  + " PR, " + _tableUserRights + " UR ");
            sql.append("WHERE UR.Profile_Id = PR.Profile_Id ");
            sql.append("AND UR.Login = ? AND UR.UserPopulation_Id = ?");

            if (context != null)
            {
                sql.append("AND LOWER(UR.Context) ");
                sql.append(_getCondition(context));
                sql.append(" ?");
            }

            stmt = connection.prepareStatement(sql.toString());

            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());

            if (context != null)
            {
                // LIKE query
                stmt.setString(3, context.replace('*', '%'));
            }

            // Logger la requête
            getLogger().debug(sql + "\n[" + user.getLogin() + "," + user.getPopulationId() + (context != null ? "," + context : "") + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                rights.add(rs.getString(1));
            }

            return rights;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    private Map<String, Set<String>> getUsersOnlyRights(UserIdentity user) throws SQLException
    {
        Map<String, Set<String>> rights = new HashMap<>();
        
        if (user == null)
        {
            return rights;
        }
        
        rights = _getUserRightsInCache(user);
        if (rights == null)
        {
            rights = _getUsersOnlyRightsFromDb(user);
            
            _setUserRightsInCache(user, rights);
        }
        
        return rights;
    }
    
    private Map<String, Set<String>> _getUsersOnlyRightsFromDb(UserIdentity user) throws SQLException
    {
        Map<String, Set<String>> rights = new HashMap<>();
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux utilisateurs
            StringBuffer sql = new StringBuffer();

            sql.append("SELECT DISTINCT PR.Right_id, UR.Context ");
            sql.append("FROM " + _tableProfileRights  + " PR, " + _tableUserRights + " UR ");
            sql.append("WHERE UR.Profile_Id = PR.Profile_Id ");
            sql.append("AND UR.Login = ? AND UR.UserPopulation_Id = ?");

            stmt = connection.prepareStatement(sql.toString());
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());

            // Logger la requête
            getLogger().debug(sql + "\n[" + user.getLogin() + ", " + user.getPopulationId() + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String rightId = rs.getString(1);
                String context = rs.getString(2);

                if (!rights.containsKey(context))
                {
                    rights.put(context, new HashSet<String>());
                }
                rights.get(context).add(rightId);
            }

            return rights;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    private Map<String, Set<String>> getUsersOnlyProfiles(UserIdentity user) throws SQLException
    {
        Map<String, Set<String>> profiles = new HashMap<>();

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux utilisateurs
            StringBuffer sql = new StringBuffer();

            sql.append("SELECT DISTINCT Profile_Id, Context ");
            sql.append("FROM " + _tableUserRights  + " ");
            sql.append("WHERE Login = ? AND UserPopulation_Id = ?");

            stmt = connection.prepareStatement(sql.toString());
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());

            // Logger la requête
            getLogger().debug(sql + "\n[" + user.getLogin() + ", " + user.getPopulationId() + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String profileId = rs.getString(1);
                String context = rs.getString(2);

                if (!profiles.containsKey(profileId))
                {
                    profiles.put(profileId, new HashSet<String>());
                }
                profiles.get(profileId).add(context);
            }

            return profiles;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    /* ----------------------------- */
    /* METHODES INTERNES DES GROUPES */
    /* ----------------------------- */

    /**
     * Search if the user has the specified right. This function search in the
     * rights assigned to its owning groups.
     * 
     * @param user the identity of the user
     * @param right the right to verify
     * 
     * @return RIGHT_OK, RIGHT_NOK or RIGHT_UNKNOWN
     * 
     * @throws SQLException in case of connection error with the databse
     */
    private RightResult hasGroupOnlyRight(UserIdentity user, String right) throws SQLException
    {
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            Set<GroupIdentity> groups = getGroupId(user);
            String groupSql = createGroupIdRequest(groups);

            if (groupSql != null)
            {
                String sql = "SELECT GR.Group_Id " + "FROM " + _tableProfileRights + " PR, " + _tableGroupRights + " GR WHERE PR.Right_Id = ? " + "AND GR.Profile_Id = PR.Profile_Id " + "AND (" + groupSql + ")";

                stmt = connection.prepareStatement(sql);

                int i = 1;
                stmt.setString(i++, right);
                fillStatement(stmt, null, groups, i);

                // Logger la requête
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug(sql + "\n[" + right + ", " + user + "]");
                }

                rs = stmt.executeQuery();
                if (rs.next())
                {
                    return RightResult.RIGHT_OK;
                }
            }

            return RightResult.RIGHT_UNKNOWN;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    private String buildSQLStatementForGroup(String context, String groupSql)
    {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT GR.Context ");
        sql.append("FROM " + _tableProfileRights + " PR, " + _tableGroupRights + " GR ");
        sql.append("WHERE PR.Right_Id = ? ");
        sql.append("AND GR.Profile_Id = PR.Profile_Id ");
        sql.append("AND (LOWER(GR.Context) = ? OR LOWER(GR.Context) LIKE ? ");

        String currentContext = HierarchicalRightsHelper.getParentContext(context);
        while (currentContext != null)
        {
            sql.append("OR LOWER(GR.Context) = ? ");
            currentContext = HierarchicalRightsHelper.getParentContext(currentContext);
        }

        sql.append(") AND (");
        sql.append(groupSql);
        sql.append(")");

        return sql.toString();
    }

    /**
     * Search if the user has the specified right in the given context and the
     * other subcontext where the user has this right. This function search in
     * the rights assigned to its owning groups. The result is stored in cache.
     * @param user the identity of the user
     * @param right the right to verify
     * @param context the context
     * @return RIGHT_OK or RIGHT_UNKNOWN
     * @throws SQLException in case of connection error with the databse
     */
    private RightResult hasGroupOnlyRight(UserIdentity user, String right, String context) throws SQLException
    {
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Récupère la liste des groupes du user
            Set<GroupIdentity> groups = getGroupId(user);
            String groupSql = createGroupIdRequest(groups);

            if (groupSql != null)
            { // Construit le bout de sql pour tous les groupes
                String sql = buildSQLStatementForGroup(context, groupSql);

                stmt = connection.prepareStatement(sql);

                int i = 1;
                stmt.setString(i++, right);
                stmt.setString(i++, context);
                stmt.setString(i++, context + "/%");
                fillStatement(stmt, context, groups, i);

                // Logger la requête
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug(sql + "\n[" + right + ", " + (context != null ? "," + context : "") + "]");
                }

                Map<String, List<String>> mapContext = getCacheContext(user, right);
                List<String> contextList = mapContext.get(context);
                if (contextList == null)
                {
                    contextList = new ArrayList<>();
                    mapContext.put(context, contextList);
                }

                Map<String, Boolean> mapContext2 = _prepareCache2(user, right, context);

                rs = stmt.executeQuery();
                boolean hasRight = false;
                while (rs.next())
                {
                    String strContext = rs.getString(1);
                    if (context == null || strContext == null || strContext.equals(context))
                    {
                        hasRight = true;
                        contextList.add("");
                    }
                    else if (strContext.length() < context.length())
                    {
                        mapContext2.put(strContext, Boolean.TRUE);
                    }
                    else
                    {
                        contextList.add(strContext.substring(context.length() + 1));
                    }
                }

                if (hasRight)
                {
                    return RightResult.RIGHT_OK;
                }
                else
                {
                    return RightResult.RIGHT_UNKNOWN;
                }
            }

            return RightResult.RIGHT_UNKNOWN;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    private Set<UserIdentity> getGrantedGroupsOnly(String right, String context) throws SQLException
    {
        Set<UserIdentity> users = new HashSet<>();

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Puis parmi les droits affectés aux groups auxquels appartient
            // l'utilisateur
            String sql = "SELECT DISTINCT GR.Group_Id, GR.GroupDirectory_Id " + "FROM " + _tableProfileRights + " PR, " + _tableGroupRights + " GR WHERE GR.Profile_Id = PR.Profile_Id " + "AND PR.Right_Id = ? ";

            if (context != null)
            {
                sql += " AND LOWER(GR.Context) = ?";
            }

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, right);

            if (context != null)
            {
                stmt.setString(2, context);
            }

            // Logger la requête
            getLogger().debug(sql + "\n[" + right + (context != null ? "," + context : "") + "]");

            // boucle sur les group id retrouvés
            rs = stmt.executeQuery();
            while (rs.next())
            {
                String groupId = rs.getString(1);
                String groupDirectoryId = rs.getString(2);

                Group group = _groupManager.getGroup(groupDirectoryId, groupId);
                if (group != null)
                {
                    users.addAll(group.getUsers());
                }
                else if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("The group ('" + groupId + "', '" + groupDirectoryId + "') is referenced in profile tables, but cannot be retrieve by GroupsManager. The database may be inconsistant.");
                }
            }

            return users;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }
    
    private Set<UserIdentity> getGrantedGroupsOnly(String context) throws SQLException
    {
        Set<UserIdentity> users = new HashSet<>();

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Puis parmi les droits affectés aux groups auxquels appartient
            // l'utilisateur
            StringBuilder sql = new StringBuilder("SELECT DISTINCT GR.Group_Id, GR.GroupDirectory_Id " + "FROM " + _tableProfileRights + " PR, " + _tableGroupRights + " GR WHERE GR.Profile_Id = PR.Profile_Id ");

            if (context != null)
            {
                sql.append("AND LOWER(GR.Context) ");
                sql.append(_getCondition(context));
                sql.append(" ?");
            }

            stmt = connection.prepareStatement(sql.toString());

            if (context != null)
            {
                // LIKE query
                stmt.setString(1, context.replace('*', '%'));
            }

            // Logger la requête
            getLogger().debug(sql.toString() + "\n[" + (context != null ? "," + context : "") + "]");

            // boucle sur les group id retrouvés
            rs = stmt.executeQuery();
            while (rs.next())
            {
                String groupId = rs.getString(1);
                String groupDirectoryId = rs.getString(2);
                
                Group group = _groupManager.getGroup(groupDirectoryId, groupId);
                if (group != null)
                {
                    users.addAll(group.getUsers());
                }
                else if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("The group ('" + groupId + "', '" + groupDirectoryId + "') is referenced in profile tables, but cannot be retrieve by GroupsManager. The database may be inconsistant.");
                }
            }

            return users;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    private Set<String> getGroupsOnlyRights(UserIdentity user, String context) throws SQLException
    {
        Set<String> rights = new HashSet<>();
        
        if (user == null)
        {
            return rights;
        }
        
        rights = _getGroupRightsInCache(user, context);
        if (rights == null)
        {
            rights = _getGroupsOnlyRightsFromDb(user, context);
            
            _setGroupRightsInCache(user, context, rights);
        }
        
        return rights;
    }
    
    private Set<String> _getGroupsOnlyRightsFromDb(UserIdentity user, String context) throws SQLException
    {
        Set<String> rights = new HashSet<>();
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Récupère la liste des groupes du user
            Set<GroupIdentity> userGroups = _groupManager.getUserGroups(user.getLogin(), user.getPopulationId());
            if (userGroups != null && userGroups.size() != 0)
            {
                // Construit le bout de sql pour tous les groupes
                StringBuffer groupSql = new StringBuffer();

                for (int i = 0; i < userGroups.size(); i++)
                {
                    if (i != 0)
                    {
                        groupSql.append(" OR ");
                    }

                    groupSql.append("(GR.Group_Id = ? AND GR.GroupDirectory_Id = ?)");
                }

                // Puis parmi les droits affectés aux groupes auxquels appartient l'utilisateur
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT DISTINCT PR.Right_Id ");
                sql.append("FROM " + _tableProfileRights + " PR, " + _tableGroupRights + " GR ");
                sql.append("WHERE GR.Profile_Id = PR.Profile_Id ");

                if (context != null)
                {
                    sql.append("AND LOWER(GR.Context) ");
                    sql.append(_getCondition(context));
                    sql.append(" ? ");
                }

                sql.append("AND (");
                sql.append(groupSql);
                sql.append(")");

                stmt = connection.prepareStatement(sql.toString());

                Iterator<GroupIdentity>groupSqlIterator = userGroups.iterator();
                int i = 1;

                if (context != null)
                {
                    // LIKE query
                    stmt.setString(i, context.replace('*', '%'));
                    i++;
                }

                while (groupSqlIterator.hasNext())
                {
                    GroupIdentity group = groupSqlIterator.next();
                    stmt.setString(i++, group.getId());
                    stmt.setString(i++, group.getDirectoryId());
                }

                // Logger la requête
                getLogger().debug(sql + "\n[" + (context == null ? "" : context) + "]");

                rs = stmt.executeQuery();

                while (rs.next())
                {
                    rights.add(rs.getString(1));
                }
            }

            return rights;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    private Map<String, Set<String>> getGroupsOnlyRights(UserIdentity user) throws SQLException
    {
        Map<String, Set<String>> rights = new HashMap<>();
        
        if (user == null)
        {
            return rights;
        }
        
        rights = _getGroupRightsInCache(user);
        if (rights == null)
        {
            rights = _getGroupsOnlyRightsFromDb(user);
            
            _setGroupRightsInCache(user, rights);
        }
        
        return rights;
    }
    
    private Map<String, Set<String>> _getGroupsOnlyRightsFromDb(UserIdentity user) throws SQLException
    {
        Map<String, Set<String>> rights = new HashMap<>();
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Récupère la liste des groupes du user
            Set<GroupIdentity> users = _groupManager.getUserGroups(user.getLogin(), user.getPopulationId());
            if (users != null && users.size() != 0)
            {
                // Construit le bout de sql pour tous les groupes
                StringBuffer groupSql = new StringBuffer();

                for (int i = 0; i < users.size(); i++)
                {
                    if (i != 0)
                    {
                        groupSql.append(" OR ");
                    }

                    groupSql.append("(GR.Group_Id = ? AND GR.GroupDirectory_Id = ?)");
                }

                // Puis parmi les droits affectés aux groupes auxquels appartient l'utilisateur
                StringBuffer sql = new StringBuffer();
                sql.append("SELECT DISTINCT PR.Right_Id, GR.Context ");
                sql.append("FROM " + _tableProfileRights + " PR, " + _tableGroupRights + " GR ");
                sql.append("WHERE GR.Profile_Id = PR.Profile_Id ");

                sql.append("AND (");
                sql.append(groupSql);
                sql.append(")");

                stmt = connection.prepareStatement(sql.toString());

                Iterator<GroupIdentity> groupSqlIterator = users.iterator();
                int i = 1;

                while (groupSqlIterator.hasNext())
                {
                    GroupIdentity group = groupSqlIterator.next();
                    stmt.setString(i++, group.getId());
                    stmt.setString(i++, group.getDirectoryId());
                }

                // Logger la requête
                getLogger().debug(sql.toString());

                rs = stmt.executeQuery();

                while (rs.next())
                {
                    String rightId = rs.getString(1);
                    String context = rs.getString(2);

                    if (!rights.containsKey(context))
                    {
                        rights.put(context, new HashSet<String>());
                    }
                    rights.get(context).add(rightId);
                }
            }

            return rights;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    private Map<String, Set<String>> getGroupsOnlyProfiles(UserIdentity user) throws SQLException
    {
        Map<String, Set<String>> profiles = new HashMap<>();

        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Récupère la liste des groupes du user
            Set<GroupIdentity> userGroups = _groupManager.getUserGroups(user.getLogin(), user.getPopulationId());
            if (userGroups != null && userGroups.size() != 0)
            {
                // Construit le bout de sql pour tous les groupes
                StringBuffer groupSql = new StringBuffer();

                for (int i = 0; i < userGroups.size(); i++)
                {
                    if (i != 0)
                    {
                        groupSql.append(" OR ");
                    }

                    groupSql.append("(GR.Group_Id = ? AND GR.GroupDirectory_Id)");
                }

                // Puis parmi les profils affectés aux groupes auxquels appartient l'utilisateur
                StringBuffer sql = new StringBuffer();

                sql.append("SELECT DISTINCT GR.Profile_Id, GR.Context ");
                sql.append("FROM Rights_GroupRights GR ");
                sql.append("WHERE ");
                sql.append(groupSql);

                stmt = connection.prepareStatement(sql.toString());

                Iterator<GroupIdentity> groupSqlIterator = userGroups.iterator();
                int i = 1;

                while (groupSqlIterator.hasNext())
                {
                    GroupIdentity group = groupSqlIterator.next();
                    stmt.setString(i++, group.getId());
                    stmt.setString(i++, group.getDirectoryId());
                }

                // Logger la requête
                getLogger().debug(sql.toString());

                rs = stmt.executeQuery();

                while (rs.next())
                {
                    String profileId = rs.getString(1);
                    String context = rs.getString(2);

                    if (!profiles.containsKey(profileId))
                    {
                        profiles.put(profileId, new HashSet<String>());
                    }
                    profiles.get(profileId).add(context);
                }
            }

            return profiles;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    private String _getCondition(String context)
    {
        if (context.indexOf("*") >= 0)
        {
            return "LIKE";
        }
        else
        {
            return "=";
        }
    }

    /**
     * Search if the user has the specified right in the given context. This
     * function search in the rights assigned directly to the user.
     * 
     * @param user the identity of the user
     * @param right the right to verify
     * @param rootContext the context beginning
     * @param endContext the context ending
     * 
     * @return RIGHT_OK or RIGHT_UNKNOWN
     * @throws RightOnContextNotInCacheException if the right on this context is
     *             not in cache
     */
    private RightResult hasUserRightInCache(UserIdentity user, String right, String rootContext, String endContext) throws RightOnContextNotInCacheException
    {
        Map<UserIdentity, Map<String, Map<String, List<String>>>> mapCache = _cacheTL.get();

        if (mapCache == null)
        {
            mapCache = new HashMap<>();
            _cacheTL.set(mapCache);
        }

        if (mapCache.containsKey(user) && rootContext != null)
        {
            Map<String, Map<String, List<String>>> mapRight = mapCache.get(user);
            if (mapRight.containsKey(right))
            {
                Map<String, List<String>> mapContext = mapRight.get(right);
                if (mapContext.containsKey(rootContext))
                {
                    // Liste des sous contexte
                    List<String> contextList = mapContext.get(rootContext);
                    if (contextList != null && contextList.contains(endContext))
                    {
                        getLogger().debug("Find in cache the right " + right + "for user " + user + " on context [" + rootContext + ", " + endContext + "]");
                        return RightResult.RIGHT_OK;
                    }
                    else
                    {
                        getLogger().debug("In cache, user " + user + " has not the right " + user + " on context [" + rootContext + ", " + endContext + "]");
                        return RightResult.RIGHT_UNKNOWN;
                    }
                }
                else if (!rootContext.equals(""))
                {
                    // le cache ne contient pas le context 'rootContext' pour cet utilisateur et ce droit
                    // on remonte d'un cran et on voit si le cache contient ce nouveau context racine
                    String newRootContext = rootContext.substring(0, rootContext.lastIndexOf("/"));
                    String newEndContext = rootContext.substring(rootContext.lastIndexOf("/") + 1);
                    newEndContext = endContext.equals("") ? newEndContext : newEndContext + "/" + endContext;
                    return hasUserRightInCache(user, right, newRootContext, newEndContext);
                }
            }
        }
        throw new RightOnContextNotInCacheException();
    }

    private RightResult hasUserRightInCache2(UserIdentity user, String right, String context) throws RightOnContextNotInCacheException
    {
        Map<UserIdentity, Map<String, Map<String, Boolean>>> mapCache = _cache2TL.get();

        if (mapCache == null)
        {
            mapCache = new HashMap<>();
            _cache2TL.set(mapCache);
        }

        if (mapCache.containsKey(user) && context != null)
        {
            Map<String, Map<String, Boolean>> mapRight = mapCache.get(user);
            if (mapRight.containsKey(right))
            {
                Map<String, Boolean> mapContext = mapRight.get(right);
                if (mapContext.containsKey(context))
                {
                    Boolean hasRight = mapContext.get(context);
                    if (hasRight.booleanValue())
                    {
                        getLogger().debug("Find in cache2 the right " + right + "for user " + user + " on context [" + context + "]");
                        return RightResult.RIGHT_OK;
                    }
                    else
                    {
                        getLogger().debug("In cache2, user " + user + " has not the right " + right + " on context [" + context + "]");
                        return RightResult.RIGHT_UNKNOWN;
                    }
                }
            }
        }
        throw new RightOnContextNotInCacheException();
    }

    private Set<GroupIdentity> getGroupId(UserIdentity user)
    {
        // Récupère la liste des groupes du user
        Set<GroupIdentity> userGroups = _groupManager.getUserGroups(user.getLogin(), user.getPopulationId());
        if (userGroups == null || userGroups.size() == 0)
        {
            return null;
        }
        else
        {
            return userGroups;
        }
    }

    private String createGroupIdRequest(Set<GroupIdentity> userGroups)
    {
        if (userGroups == null || userGroups.size() == 0)
        {
            return null;
        }

        // Construit le bout de sql pour tous les groupes
        StringBuffer groupSql = new StringBuffer();

        for (int i = 0; i < userGroups.size(); i++)
        {
            if (i != 0)
            {
                groupSql.append(" OR ");
            }
            groupSql.append("(GR.Group_Id = ? AND GR.GroupDirectory_Id = ?)");
        }

        return "(" + groupSql.toString() + ")";
    }

    private void fillStatement(PreparedStatement stmt, String context, Set<GroupIdentity> userGroups, int startIndex) throws SQLException
    {
        int i = startIndex;

        String currentContext = HierarchicalRightsHelper.getParentContext(context);
        while (currentContext != null)
        {
            stmt.setString (i++, currentContext);
            currentContext = HierarchicalRightsHelper.getParentContext(currentContext);
        }

        for (GroupIdentity group : userGroups)
        {
            stmt.setString(i++, group.getId());
            stmt.setString(i++, group.getDirectoryId());
        }

    }

    private Map<String, List<String>> getCacheContext(UserIdentity user, String right)
    {
        Map<UserIdentity, Map<String, Map<String, List<String>>>> mapCache = _cacheTL.get();

        Map<String, Map<String, List<String>>> mapRight = mapCache.get(user);
        if (mapRight == null)
        {
            mapRight = new HashMap<>();
            mapCache.put(user, mapRight);
        }

        Map<String, List<String>> mapContext = mapRight.get(right);
        if (mapContext == null)
        {
            mapContext = new HashMap<>();
            mapRight.put(right, mapContext);
        }

        return mapContext;
    }

    private Map<String, Boolean> getCache2Context(UserIdentity user, String right)
    {
        Map<UserIdentity, Map<String, Map<String, Boolean>>> mapCache = _cache2TL.get();

        Map<String, Map<String, Boolean>> mapRight = mapCache.get(user);
        if (mapRight == null)
        {
            mapRight = new HashMap<>();
            mapCache.put(user, mapRight);
        }

        Map<String, Boolean> mapContext = mapRight.get(right);
        if (mapContext == null)
        {
            mapContext = new HashMap<>();
            mapRight.put(right, mapContext);
        }

        return mapContext;
    }
    
    private Set<String> _getUserRightsInCache(UserIdentity user, String context)
    {
        Set<String> rights = _getRightsInCache(_userRightCache, user, context);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("User rights cache " + (rights == null ? "miss" : "hit") + " for login '" + user + "' in context '" + context + "'.");
        }
        
        return rights;
    }
    
    private void _setUserRightsInCache(UserIdentity user, String context, Set<String> rights)
    {
        _setRightsInCache(_userRightCache, user, context, rights);
    }
    
    private Map<String, Set<String>> _getUserRightsInCache(UserIdentity user)
    {
        Map<String, Set<String>> rights = _getRightsInCache(_userRightCache, user);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("User rights cache " + (rights == null ? "miss" : "hit") + " for login '" + user + "'.");
        }
        
        return rights;
    }
    
    private void _setUserRightsInCache(UserIdentity user, Map<String, Set<String>> rights)
    {
        _setRightsInCache(_userRightCache, user, rights);
    }
    
    private Set<String> _getGroupRightsInCache(UserIdentity user, String context)
    {
        Set<String> rights = _getRightsInCache(_groupRightCache, user, context);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Group rights cache " + (rights == null ? "miss" : "hit") + " for login '" + user + "' in context '" + context + "'.");
        }
        
        return rights;
    }
    
    private void _setGroupRightsInCache(UserIdentity user, String context, Set<String> rights)
    {
        _setRightsInCache(_groupRightCache, user, context, rights);
    }
    
    private Map<String, Set<String>> _getGroupRightsInCache(UserIdentity user)
    {
        Map<String, Set<String>> rights = _getRightsInCache(_groupRightCache, user);
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Group rights cache " + (rights == null ? "miss" : "hit") + " for login '" + user + "'.");
        }
        
        return rights;
    }
    
    private void _setGroupRightsInCache(UserIdentity user, Map<String, Set<String>> rights)
    {
        _setRightsInCache(_groupRightCache, user, rights);
    }
    
    /**
     * Clear the user right cache.
     */
    protected void _clearUserRightCache()
    {
        Map<UserIdentity, Map<String, Set<String>>> cache = _userRightCache.get();
        if (cache != null)
        {
            cache.clear();
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Cleared user rights cache.");
        }
    }
    
    /**
     * Clear the group right cache.
     */
    protected void _clearGroupRightCache()
    {
        Map<UserIdentity, Map<String, Set<String>>> cache = _groupRightCache.get();
        if (cache != null)
        {
            cache.clear();
        }
        
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Cleared group rights cache.");
        }
    }
    
    private Set<String> _getRightsInCache(ThreadLocal<Map<UserIdentity, Map<String, Set<String>>>> cacheTL, UserIdentity user, String context)
    {
        Map<UserIdentity, Map<String, Set<String>>> cache = cacheTL.get();
        
        if (cache == null)
        {
            cache = new HashMap<>();
            cacheTL.set(cache);
        }
        
        if (cache.containsKey(user))
        {
            return cache.get(user).get(context);
        }
        
        return null;
    }
    
    private void _setRightsInCache(ThreadLocal<Map<UserIdentity, Map<String, Set<String>>>> cacheTL, UserIdentity user, String context, Set<String> rights)
    {
        Map<UserIdentity, Map<String, Set<String>>> cache = cacheTL.get();

        if (cache == null)
        {
            cache = new HashMap<>();
            cacheTL.set(cache);
        }
        
        Map<String, Set<String>> userCache;
        if (cache.containsKey(user))
        {
            userCache = cache.get(user);
        }
        else
        {
            userCache = new HashMap<>();
            cache.put(user, userCache);
        }
        
        userCache.put(context, rights);
    }
    
    private Map<String, Set<String>> _getRightsInCache(ThreadLocal<Map<UserIdentity, Map<String, Set<String>>>> cacheTL, UserIdentity user)
    {
        Map<UserIdentity, Map<String, Set<String>>> cache = cacheTL.get();
        
        if (cache == null)
        {
            cache = new HashMap<>();
            cacheTL.set(cache);
        }
        
        return cache.get(user);
    }
    
    private void _setRightsInCache(ThreadLocal<Map<UserIdentity, Map<String, Set<String>>>> cacheTL, UserIdentity user, Map<String, Set<String>> rights)
    {
        Map<UserIdentity, Map<String, Set<String>>> cache = cacheTL.get();
        
        if (cache == null)
        {
            cache = new HashMap<>();
            cacheTL.set(cache);
        }
        
        cache.put(user, rights);
    }
    
    /**
     * Get the list of declared contexts on which an user has the given right
     * @param user The user. Cannot be null.
     * @param rightId the id of the right to check. Cannot be null.
     * @param initialContext the initial context to filter the results. Cannot be null.
     * @return The Set containing the contexts
     * @throws RightsException if an error occurs.
     */
    protected Set<String> getDeclaredContexts(UserIdentity user, String rightId, String initialContext)
    {
        HashSet<String> contexts = new HashSet<>();

        RightResult result = hasRight(user, rightId, initialContext);
        if (result == RightResult.RIGHT_OK)
        {
            contexts.add(initialContext);
        }

        String prefix = _rightsContextPrefixEP.getContextPrefix() + "/" + (StringUtils.isNotEmpty(initialContext) ? initialContext + "/" : "");
        // The cache is necessarily filled has we first call 'hasRight' method
        Map<String, List<String>> cacheContext = getCacheContext(user, rightId);
        for (String rootCtx : cacheContext.keySet())
        {
            List<String> subContexts = cacheContext.get(rootCtx);
            for (String subCtx : subContexts)
            {
                String ctx = rootCtx + "/" + subCtx;
                if (ctx.startsWith(prefix))
                {
                    contexts.add(ctx);
                }

            }
        }

        return contexts;
    }

    @Override
    public Profile getProfile(String id)
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try
        {
            connection = getSQLConnection();
            statement = connection.prepareStatement("SELECT Label, Context FROM " + _tableProfile + " WHERE Id = ?");
            statement.setInt(1, Integer.parseInt(id));

            rs = statement.executeQuery();

            if (rs.next())
            {
                String label = rs.getString("Label");
                String context = rs.getString("Context");
                return new DefaultProfile(id, label, context);
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

    @Override
    public Profile addProfile(String name)
    {
        return addProfile(name, null);
    }

    @Override
    public Profile addProfile(String name, String context) throws RightsException
    {
        Integer id = null;

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;

        try
        {
            connection = getSQLConnection();
            String dbType = ConnectionHelper.getDatabaseType(connection);

            if (ConnectionHelper.DATABASE_ORACLE.equals(dbType) || ConnectionHelper.DATABASE_POSTGRES.equals(dbType))
            {
                if (ConnectionHelper.DATABASE_ORACLE.equals(dbType))
                {
                    statement = connection.prepareStatement("SELECT seq_rights_profile.nextval FROM dual");
                }
                else
                {
                    statement = connection.prepareStatement("SELECT nextval('seq_rights_profile')");
                }

                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getInt(1);
                }
                else
                {
                    throw new RightsException("Error generating a new profile ID, the profile was not created");
                }

                ConnectionHelper.cleanup(rs);
                ConnectionHelper.cleanup(statement);

                statement = connection.prepareStatement("INSERT INTO " + _tableProfile + " (Id, Label, Context) VALUES(?, ?, ?)");
                statement.setInt(1, id);
                statement.setString(2, name);
                statement.setString(3, context);
            }
            else
            {
                statement = connection.prepareStatement("INSERT INTO " + _tableProfile + " (Label, Context) VALUES(?, ?)");
                statement.setString(1, name);
                statement.setString(2, context);
            }

            statement.executeUpdate();
            ConnectionHelper.cleanup(statement);

            // FIXME Write query working with all database
            if (ConnectionHelper.DATABASE_MYSQL.equals(dbType))
            {
                statement = connection.prepareStatement("SELECT Id FROM " + _tableProfile + " WHERE Id = last_insert_id()");
                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getInt("Id");
                }
                else
                {
                    if (connection.getAutoCommit())
                    {
                        throw new RuntimeException("Cannot retrieve inserted profile. Profile was created but method did not return it. Application may be inconsistent.");
                    }
                    else
                    {
                        connection.rollback();
                        throw new RuntimeException("Cannot retrieve inserted profile. Rolling back");
                    }
                }
            }
            else if (ConnectionHelper.DATABASE_DERBY.equals(dbType))
            {
                statement = connection.prepareStatement("VALUES IDENTITY_VAL_LOCAL ()");
                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getInt(1);
                }
            }
            else if (ConnectionHelper.DATABASE_HSQLDB.equals(dbType))
            {
                statement = connection.prepareStatement("CALL IDENTITY ()");
                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getInt(1);
                }
            }
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

        if (id == null)
        {
            throw new RightsException("Error generating a new profile ID, the profile was not created");
        }

        return new DefaultProfile(Integer.toString(id), name, context);
    }

    @Override
    public Set<Profile> getProfiles()
    {
        return getProfiles(null);
    }

    @Override
    public Set<Profile> getProfiles(String context)
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
                profiles.add(new DefaultProfile(id, label, context));
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

    @Override
    public Set<Profile> getAllProfiles()
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
                profiles.add(new DefaultProfile(id, label, context));
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
     * Get the full context (ie. with the prefix)
     * @param context The context
     * @return The full context
     */
    protected String getFullContext (String context)
    {
        String contextPrefix = _rightsContextPrefixEP.getContextPrefix();
        return context != null ? (contextPrefix + context).toLowerCase() : null;
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
    public void userRemoved(UserIdentity user)
    {
        removeUserProfiles(user, null);
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
    public void groupRemoved(GroupIdentity group)
    {
        removeGroupProfiles(group, null);
    }

    /**
     * Exception if right is not in cache
     */
    protected class RightOnContextNotInCacheException extends Exception
    {
        /**
         * Default constructor.
         */
        public RightOnContextNotInCacheException()
        {
            // Nothing to do
        }

        /**
         * Constructor with a message.
         * 
         * @param message The message.
         */
        public RightOnContextNotInCacheException(String message)
        {
            super(message);
        }

        /**
         * Constructor with a cause.
         * 
         * @param cause The cause.
         */
        public RightOnContextNotInCacheException(Exception cause)
        {
            super(cause);
        }

        /**
         * Constructor with a message and a cause.
         * 
         * @param message The message.
         * @param cause The cause.
         */
        public RightOnContextNotInCacheException(String message, Exception cause)
        {
            super(message, cause);
        }
    }

    /**
     * A profile is a set of Rights.
     */
    public class DefaultProfile implements Profile
    {
        private final String _id;
        private final String _name;
        private final String _context;
        private Connection _currentConnection;
        private boolean _supportsBatch;
        private PreparedStatement _batchStatement;

        /**
         * Constructor.
         * @param id the unique id of this profile
         * @param name the name of this profile
         */
        public DefaultProfile(String id, String name)
        {
            this(id, name, null);
        }

        /**
         * Constructor.
         * @param id the unique id of this profile
         * @param name the name of this profile
         * @param context the context
         */
        public DefaultProfile(String id, String name, String context)
        {
            _id = id;
            _name = name;
            _context = context;
            _currentConnection = null;
        }

        @Override
        public String getId()
        {
            return _id;
        }

        @Override
        public String getName()
        {
            return _name;
        }

        @Override
        public String getContext()
        {
            return _context;
        }

        @Override
        public void addRight(String rightId)
        {
            Connection connection = getConnection();

            try
            {
                PreparedStatement statement = getAddStatement(connection);
                statement.setInt(1, Integer.parseInt(_id));
                statement.setString(2, rightId);

                if (isUpdating() && _supportsBatch)
                {
                    statement.addBatch();
                }
                else
                {
                    statement.executeUpdate();
                }
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
            finally
            {
                if (!isUpdating())
                {
                    ConnectionHelper.cleanup(connection);
                }
            }
        }

        @Override
        public void rename(String newName)
        {
            Connection connection = getSQLConnection();

            try
            {
                PreparedStatement statement = connection.prepareStatement("UPDATE " + _tableProfile + " SET label = ? WHERE Id = ?");
                statement.setString(1, newName);
                statement.setInt(2, Integer.parseInt(_id));
                statement.executeUpdate();
            }
            catch (NumberFormatException ex)
            {
                throw new RuntimeException(ex);
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
            finally
            {
                ConnectionHelper.cleanup(connection);
            }
        }

        @Override
        public Set<String> getRights()
        {
            Set<String> rights = new HashSet<>();

            Connection connection = getSQLConnection();

            try
            {
                PreparedStatement statement = connection.prepareStatement("SELECT Right_Id FROM " + _tableProfileRights + " WHERE profile_Id = ?");
                statement.setInt(1, Integer.parseInt(_id));
                ResultSet rs = statement.executeQuery();

                while (rs.next())
                {
                    String id = rs.getString("Right_Id");
                    rights.add(id);
                }
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
            finally
            {
                ConnectionHelper.cleanup(connection);
            }

            return rights;
        }

        @Override
        public void removeRights()
        {
            Connection connection = getConnection();

            try
            {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM " + _tableProfileRights + " WHERE Profile_Id = ?");
                statement.setInt(1, Integer.parseInt(_id));
                statement.executeUpdate();
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
            finally
            {
                if (!isUpdating())
                {
                    ConnectionHelper.cleanup(connection);
                }
            }
        }

        @Override
        public void remove()
        {
            Connection connection = null;
            PreparedStatement statement = null;
            PreparedStatement statement2 = null;
            PreparedStatement statement3 = null;
            PreparedStatement statement4 = null;

            try
            {
                connection = getSQLConnection();

                statement = connection.prepareStatement("DELETE FROM " + _tableProfile + " WHERE Id = ?");
                statement.setInt(1, Integer.parseInt(_id));
                statement.executeUpdate();

                statement2 = connection.prepareStatement("DELETE FROM " + _tableProfileRights + " WHERE Profile_Id = ?");
                statement2.setInt(1, Integer.parseInt(_id));
                statement2.executeUpdate();

                statement3 = connection.prepareStatement("DELETE FROM " + _tableUserRights + " WHERE Profile_Id = ?");
                statement3.setInt(1, Integer.parseInt(_id));
                statement3.executeUpdate();

                statement4 = connection.prepareStatement("DELETE FROM " + _tableGroupRights + " WHERE Profile_Id = ?");
                statement4.setInt(1, Integer.parseInt(_id));
                statement4.executeUpdate();
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
            finally
            {
                ConnectionHelper.cleanup(statement);
                ConnectionHelper.cleanup(statement2);
                ConnectionHelper.cleanup(statement3);
                ConnectionHelper.cleanup(statement4);
                ConnectionHelper.cleanup(connection);
            }

        }

        @Override
        public void startUpdate()
        {
            _currentConnection = getSQLConnection();

            try
            {
                _supportsBatch = _currentConnection.getMetaData().supportsBatchUpdates();
                _currentConnection.setAutoCommit(false);
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void endUpdate()
        {
            try
            {
                if (isUpdating() && _supportsBatch && _batchStatement != null)
                {
                    _batchStatement.executeBatch();
                }
                _supportsBatch = false;
                _currentConnection.commit();
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
            finally
            {
                ConnectionHelper.cleanup(_batchStatement);
                _batchStatement = null;

                ConnectionHelper.cleanup(_currentConnection);
                _currentConnection = null;
            }
        }

        /**
         * Get the current connection or create a new one.
         * @return the current connection if in "update" mode, a new one otherwise.
         */
        protected Connection getConnection()
        {
            if (isUpdating())
            {
                return _currentConnection;
            }
            else
            {
                return getSQLConnection();
            }
        }

        /**
         * Get a prepared statement to add a profile in the DBMS.
         * @param connection the connection.
         * @return a prepared statement.
         */
        protected PreparedStatement getAddStatement(Connection connection)
        {
            try
            {
                String query = "INSERT INTO " + _tableProfileRights + " (Profile_Id, Right_Id) VALUES(?, ?)";

                if (isUpdating() && _supportsBatch)
                {
                    if (_batchStatement == null)
                    {
                        _batchStatement = connection.prepareStatement(query);
                    }

                    return _batchStatement;
                }
                else
                {
                    return connection.prepareStatement(query);
                }
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
        }

        /**
         * Test if we are in "update" mode.
         * @return true if we are "update" mode, false otherwise.
         */
        protected boolean isUpdating()
        {
            boolean updating = false;

            try
            {
                updating = _currentConnection != null && !_currentConnection.isClosed();
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }

            return updating;
        }

        @Override
        @Deprecated
        public void toSAX(ContentHandler handler) throws SAXException
        {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "id", "id", "CDATA", _id);
            XMLUtils.startElement(handler, "profile", atts);

            XMLUtils.createElement(handler, "label", _name);

            String context = getContext();
            if (context != null)
            {
                XMLUtils.createElement(handler, "context", context);
            }

            handler.startElement("", "rights", "rights", new AttributesImpl());

            for (String right : getRights())
            {
                AttributesImpl attsRight = new AttributesImpl();
                attsRight.addAttribute("", "id", "id", "CDATA", right);
                XMLUtils.createElement(handler, "right", attsRight);
            }

            XMLUtils.endElement(handler, "rights");
            XMLUtils.endElement(handler, "profile");
        }
        
        @Override
        public Map<String, Object> toJSON()
        {
            Map<String, Object> profile = new HashMap<>();
            
            profile.put("id", _id);
            profile.put("label", _name);
            profile.put("context", getContext());
            profile.put("rights", getRights());
            
            return profile;
        }

        @Override
        public boolean equals(Object another)
        {
            if (another == null || !(another instanceof Profile))
            {
                return false;
            }

            Profile otherProfile = (Profile) another;

            return _id != null  || _id.equals(otherProfile.getId());
        }

        @Override
        public int hashCode()
        {
            return _id.hashCode();
        }
    }
}
