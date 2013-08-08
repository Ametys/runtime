/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.plugins.core.right.profile;

import java.io.IOException;
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

import org.ametys.runtime.datasource.ConnectionHelper;
import org.ametys.runtime.datasource.ConnectionHelper.DatabaseType;
import org.ametys.runtime.group.Group;
import org.ametys.runtime.group.GroupListener;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.group.ModifiableGroupsManager;
import org.ametys.runtime.plugins.core.right.RightContextConvertor;
import org.ametys.runtime.plugins.core.right.RightContextConvertorExtentionPoint;
import org.ametys.runtime.plugins.core.right.RightsExtensionPoint;
import org.ametys.runtime.request.RequestListener;
import org.ametys.runtime.request.RequestListenerManager;
import org.ametys.runtime.right.HierarchicalRightsHelper;
import org.ametys.runtime.right.InitializableRightsManager;
import org.ametys.runtime.right.RightsContextPrefixExtensionPoint;
import org.ametys.runtime.right.RightsException;
import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UserListener;
import org.ametys.runtime.user.UsersManager;
import org.ametys.runtime.util.I18nizableText;


/**
 * Standard implementation of the right manager from the core database that use profile.
 */
public class DefaultProfileBasedRightsManager extends AbstractLogEnabled implements InitializableRightsManager, ProfileBasedRightsManager, UserListener, GroupListener, Serviceable, Configurable, Initializable, RequestListener, ThreadSafe, Component
{
    private static final String __INITIAL_PROFILE_ID = "TEMPORARY ADMINISTRATOR";
    
    /** Avalon ServiceManager */
    protected ServiceManager _manager;
    
    /** The rights' list container */
    protected RightsExtensionPoint _rightsEP;
    /** The rights' context manager */
    protected RightsContextPrefixExtensionPoint _rightsContextPrefixEP;
    /** The rights' alias manager */
    protected RightContextConvertorExtentionPoint _rightContextConvertorExtPt;
    /** The users manager */
    protected UsersManager _usersManager;
    
    /** The groups manager */
    protected GroupsManager _groupsManager;
    
    /** The jdbc pool name */
    protected String _poolName;
    
    /** The jdbc table name for profiles' list */
    protected String _tableProfile;
    
    /** The jdbc table name for profiles' rights */
    protected String _tableProfileRights;
    
    /** The jdbc table name for users' profiles */
    protected String _tableUserRights;
    
    /** The jdbc table name for groups' profiles */
    protected String _tableGroupRights;
    
    /* Rights cache
     *  { Login : { Right : { Context : List(SubContext)
     *                      }
     *            }
     *  }
     *  Si il y a une entrée login,right,context+subcontext on a le droit
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
    private ThreadLocal<Map<String, Map<String, Map<String, List<String>>>>> _cacheTL = new ThreadLocal<Map<String, Map<String, Map<String, List<String>>>>>();

    /* Identique au précédent si ce n'est qu'il ne traite pas les sous-contextes
     *  { Login : { Right : { Context : Boolean
     *                      }
     *            }
     *  }
     *  
     *  Il complète le 1er car il permet de faire des requètes sql avec une hierarchie montant.
     *  Le premier cache fait : context/* alors que celui là fait les contextes parents.
     *  On ne peut pas mettre ces infos là dans le 1er cache on ne demande pas /* dans les parents
     *  sinon ça reviendrait à tout lire à chaque fois !
     */
    private ThreadLocal<Map<String, Map<String, Map<String, Boolean>>>> _cache2TL = new ThreadLocal<Map<String, Map<String, Map<String, Boolean>>>>();

    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
        _rightsEP = (RightsExtensionPoint) _manager.lookup(RightsExtensionPoint.ROLE);
        _rightsContextPrefixEP = (RightsContextPrefixExtensionPoint) manager.lookup(RightsContextPrefixExtensionPoint.ROLE);
        _usersManager = (UsersManager) _manager.lookup(UsersManager.ROLE);
        _groupsManager = (GroupsManager) _manager.lookup(GroupsManager.ROLE);
        _rightContextConvertorExtPt = (RightContextConvertorExtentionPoint) _manager.lookup(RightContextConvertorExtentionPoint.ROLE);
    }
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        Configuration rightsConfiguration = configuration.getChild("rights");
        
        String externalFile = rightsConfiguration.getAttribute("config", null);
        if (externalFile != null)
        {
            SourceResolver resolver = null;
            Source source = null;
            InputStream is = null;
            try
            {
                resolver = (SourceResolver) _manager.lookup(SourceResolver.ROLE);
                
                source = resolver.resolveURI("context://" + externalFile);
                
                if (source.exists())
                {
                    is = source.getInputStream();
                    
                    Configuration externalConfiguration = new DefaultConfigurationBuilder().build(is);
                    
                    is.close();
                    is = null;
                    
                    configureRights(externalConfiguration);
                }
                else if (getLogger().isInfoEnabled())
                {
                    getLogger().info("The optionnal external rights file '" + externalFile + "' is missing.");
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
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                        getLogger().error("An error occured while closing source '" + externalFile + "'", e);
                    }
                }
                if (resolver != null)
                {
                    if (source != null)
                    {
                        resolver.release(source);
                    }
                    _manager.release(resolver);
                }
            }
        }
        else
        {
            configureRights(rightsConfiguration);
        }
        
        String poolName = configuration.getChild("pool").getValue("");
        if (poolName.length() == 0)
        {
            String message = "The 'pool' mandatory element is missing or empty";
            getLogger().error(message);
            throw new ConfigurationException(message, configuration);
        }
        
        _poolName = poolName;
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
                String message = "Error in " + DefaultProfileBasedRightsManager.class.getName() + " configuration : attribute 'id' and elements 'label', 'description' and 'category' are mandatory.";
                getLogger().error(message);
                throw new ConfigurationException(message, configuration);
            }

            _rightsEP.addRight(id, i18nLabel, i18nDescription, i18nCategory);
        }
    }
    
    public void requestStarted(HttpServletRequest req)
    {
        // empty
    }

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
    
    public void initialize() throws Exception
    {
        if (_usersManager instanceof ModifiableUsersManager)
        {
            ModifiableUsersManager mbu = (ModifiableUsersManager) _usersManager;
            mbu.registerListener(this);
        }

        if (_groupsManager instanceof ModifiableGroupsManager)
        {
            ModifiableGroupsManager dgm = (ModifiableGroupsManager) _groupsManager;
            dgm.registerListener(this);
        }
        
        RequestListenerManager rlm = (RequestListenerManager) _manager.lookup(RequestListenerManager.ROLE);
        rlm.registerListener(this);
    }
    
    @Override
    public Set<String> getGrantedUsers(String right, String context) throws RightsException
    {
        try
        {
            Set<String> users = new HashSet<String>();
            
            Set<String> convertedContexts = getAliasContext(context);
            for (String convertContext : convertedContexts)
            {
                Set<String> addUsers = internalGetGrantedUsers(right, convertContext);
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
     * @return The list of users granted with that right as a Set of String (login).
     * @throws SQLException if an error occurs retrieving the rights from the database.
     */
    protected Set<String> internalGetGrantedUsers(String right, String context) throws SQLException
    {
        String lcContext = getFullContext (context);

        Set<String> logins = new HashSet<String>();
        
        logins.addAll(getGrantedUsersOnly(right, lcContext));
        logins.addAll(getGrantedGroupsOnly(right, lcContext));
        
        return logins;
    }

    public Set<String> getUserRights(String login, String context) throws RightsException
    {
        try
        {
            Set<String> rights = new HashSet<String>();
            
            if (login == null)
            {
                return rights;
            }
            
            Set<String> convertedContexts = getAliasContext(context);
            for (String convertContext : convertedContexts)
            {
                Set<String> addUsers = internalGetUserRights(login, convertContext);
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
     * @param login the user's login. Cannot be null.
     * @param context The context to test the right
     * @return The list of rights as a Set of String (id).
     * @throws SQLException if an error occurs executing the queries.
     */
    protected Set<String> internalGetUserRights(String login, String context) throws SQLException
    {
        String lcContext = getFullContext (context);

        Set<String> rights = new HashSet<String>();

        rights.addAll(getUsersOnlyRights(login, lcContext));
        rights.addAll(getGroupsOnlyRights(login, lcContext));

        return rights;
    }
    
    @Override
    public Map<String, Set<String>> getUserRights(String login) throws RightsException
    {
        if (login == null)
        {
            return new HashMap<String, Set<String>>();
        }
        
        try
        {
            Map<String, Set<String>> rights = getUsersOnlyRights(login);
            Map<String, Set<String>> groupRights = getGroupsOnlyRights(login);
            
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
    public void grantAllPrivileges(String login, String context, String profileName)
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
        addUserRight(login, context, adminProfile.getId());
        
    }
    
    public void grantAllPrivileges(String login, String context)
    {
        grantAllPrivileges(login, context, __INITIAL_PROFILE_ID);
    }

    @Override
    public RightResult hasRight(String userLogin, String right, String context)
    {
        if (userLogin == null)
        {
            return RightResult.RIGHT_NOK;
        }
        
        Set<String> convertedContexts = getAliasContext(context);
        for (String convertContext : convertedContexts)
        {
            RightResult hasRight = internalHasRight(userLogin, right, convertContext);
            if (hasRight == RightResult.RIGHT_OK)
            {
                return RightResult.RIGHT_OK;
            }
        }
        
        return RightResult.RIGHT_NOK;
    }
    
    /**
     * Check a permission for a user, in a given context.<br>
     * @param userLogin The user's login. Cannot be null.
     * @param right the name of the right to check. Cannot be null.
     * @param context the right context
     * @return RIGHT_OK, RIGHT_NOK or RIGHT_UNKNOWN
     * @throws RightsException if an error occurs.
     */
    protected RightResult internalHasRight(String userLogin, String right, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        try
        {
            return hasUserRightInCache(userLogin, right, lcContext, "");
        }
        catch (RightOnContextNotInCacheException e)
        {
            getLogger().info("No find entry in cache for [" + userLogin + ", " + right + ", " + lcContext + "]");
        }

        try
        {
            return hasUserRightInCache2(userLogin, right, lcContext);
        }
        catch (RightOnContextNotInCacheException e)
        {
            getLogger().info("No find entry in cache2 for [" + userLogin + ", " + right + ", " + lcContext + "]");
        }

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            RightResult userResult = lcContext != null ? hasUserOnlyRight(userLogin, right, lcContext) : hasUserOnlyRight(userLogin, right);
            if (userResult == RightResult.RIGHT_OK)
            {
                // Droit en tant qu'utilisateur
                return RightResult.RIGHT_OK;
            }

            // Puis parmi les droits affectés aux groupes auxquels
            // appartient l'utilisateur
            RightResult groupResult = lcContext != null ? hasGroupOnlyRight(userLogin, right, lcContext) : hasGroupOnlyRight(userLogin, right);
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
        Set<String> convertedContexts = new HashSet<String>();
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
     * @param login the login of the concerned user
     * @param context the context
     * @return a Set containing all profiles for a given user and a context
     * @throws RightsException if an error occurs.
     */
    public Set<String> getProfilesByUser(String login, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Set<String> profiles = new HashSet<String>();
        
        if (login == null)
        {
            return new HashSet<String>();
        }
        
        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT UR.Profile_Id " + "FROM " + _tableUserRights + " UR WHERE UR.Login = ? AND LOWER(UR.Context) = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, login);
            stmt.setString(2, lcContext);

            // Logger la requête
            getLogger().info(sql + "\n[" + login + ", " + lcContext + "]");

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

        Set<User> users = new HashSet<User>();

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT DISTINCT Login " + "FROM " + _tableUserRights + " WHERE Profile_Id = ? AND LOWER(Context) = ?";
            
            stmt = connection.prepareStatement(sql);

            stmt.setInt(1, Integer.parseInt(profileID));
            stmt.setString(2, lcContext);

            // Logger la requête
            getLogger().info(sql + "\n[" + profileID + ", " + lcContext + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String login = rs.getString(1);
                User user = _usersManager.getUser(login);
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

        Set<Group> groups = new HashSet<Group>();

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT DISTINCT Group_Id " + "FROM " + _tableGroupRights + " WHERE Profile_Id = ? AND LOWER(Context) = ? ";
            
            stmt = connection.prepareStatement(sql);

            stmt.setInt(1, Integer.parseInt(profileID));
            stmt.setString(2, lcContext);

            // Logger la requête
            getLogger().info(sql + "\n[" + profileID + ", " + lcContext + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String groupId = rs.getString(1);
                Group group = _groupsManager.getGroup(groupId);
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
     * Returns a Set containing all contexts for an user and a profile
     * 
     * @param login The user's login
     * @param profileID The profile
     * @return a Set containing all contexts for an user and a profile
     * @throws RightsException if an error occurs.
     */
    public Set<String> getContextByUserAndProfile(String login, String profileID) throws RightsException
    {
        Set<String> contexts = new HashSet<String>();

        if (login == null)
        {
            return contexts;
        }
        
        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT UR.Context " + "FROM " + _tableUserRights + " UR WHERE UR.Login = ? AND UR.Profile_Id = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, login);
            stmt.setInt(2, Integer.parseInt(profileID));

            // Logger la requête
            getLogger().info(sql + "\n[" + login + ", " + profileID + "]");

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
     * @param login the login of the concerned user
     * @return a Map as (Profile Id, set of context)
     * @throws RightsException if an error occurs.
     */
    public Map<String, Set<String>> getProfilesAndContextByUser (String login) throws RightsException
    {
        try
        {
            if (login == null)
            {
                return new HashMap<String, Set<String>>();
            }
            
            Map<String, Set<String>> userProfiles = getUsersOnlyProfiles(login);
            Map<String, Set<String>> groupProfiles = getGroupsOnlyProfiles(login);
            
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
     * @param groupId the group
     * @param context the context
     * @return a Set containing all profiles for a group and a context
     * @throws RightsException if an error occurs.
     */
    public Set<String> getProfilesByGroup(String groupId, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Set<String> profiles = new HashSet<String>();

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT GR.Profile_Id " + "FROM " + _tableGroupRights + " GR WHERE GR.Group_Id = ? AND LOWER(GR.Context) = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, groupId);
            stmt.setString(2, lcContext);

            // Logger la requête
            getLogger().info(sql + "\n[" + groupId + ", " + lcContext + "]");

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
     * @param groupID The group ID
     * @param profileID The profile
     * @return a Set containing all contexts for a user and a profile
     * @throws RightsException if an error occurs.
     */
    public Set<String> getContextByGroupAndProfile(String groupID, String profileID) throws RightsException
    {
        Set<String> contexts = new HashSet<String>();

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT GR.Context " + "FROM " + _tableGroupRights + " GR WHERE GR.Group_Id = ? AND GR.Profile_Id = ?";

            stmt = connection.prepareStatement(sql);
            stmt.setString(1, groupID);
            stmt.setInt(2, Integer.parseInt(profileID));

            // Logger la requête
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(sql + "\n[" + groupID + ", " + profileID + "]");
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

    public void addUserRight(String login, String context, String profileId) throws RightsException
    {
        String lcContext = getFullContext (context);

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            // Check if already exists
            String sql = "SELECT Profile_Id FROM " + _tableUserRights + " WHERE Profile_Id=? and Login=? and Context=?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(profileId));
            stmt.setString(2, login);
            stmt.setString(3, lcContext);
            
            rs = stmt.executeQuery();
            if (rs.next())
            {
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("Login " + login + " has already profile " + profileId + " on context " +  lcContext + "");
                }
                return;
            }
            
            ConnectionHelper.cleanup(stmt);
            
            sql = "INSERT INTO " + _tableUserRights + " (Profile_Id, Login, Context) VALUES(?, ?, ?)";

            stmt = connection.prepareStatement(sql);

            stmt.setInt(1, Integer.parseInt(profileId));
            stmt.setString(2, login);
            stmt.setString(3, lcContext);

            // Logger la requête
            getLogger().info(sql + "\n[" + profileId + ", " + login + ", " + lcContext + "]");

            stmt.executeUpdate();
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

    public void addGroupRight(String groupId, String context, String profileId) throws RightsException
    {
        String lcContext = getFullContext (context);
        
        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            // Check if already exists
            String sql = "SELECT Profile_Id FROM " + _tableGroupRights + " WHERE Profile_Id=? and Group_Id=? and Context=?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(profileId));
            stmt.setString(2, groupId);
            stmt.setString(3, lcContext);
            
            rs = stmt.executeQuery();
            if (rs.next())
            {
                if (getLogger().isDebugEnabled())
                {
                    getLogger().debug("Group of id " + groupId + " has already profile " + profileId + " on context " +  lcContext + "");
                }
                return;
            }

            ConnectionHelper.cleanup(stmt);
            
            sql = "INSERT INTO " + _tableGroupRights + " (Profile_Id, Group_Id, Context) VALUES(?, ?, ?)";

            stmt = connection.prepareStatement(sql);

            stmt.setInt(1, Integer.parseInt(profileId));
            stmt.setString(2, groupId);
            stmt.setString(3, lcContext);

            // Logger la requête
            getLogger().info(sql + "\n[" + profileId + ", " + groupId + ", " + lcContext + "]");

            stmt.executeUpdate();
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

    public void removeUserProfile(String login, String profileId, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "DELETE FROM " + _tableUserRights + " WHERE Login = ? AND Profile_Id = ? AND LOWER(Context) = ? ";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, login);
            stmt.setInt(2, Integer.parseInt(profileId));
            stmt.setString(3, lcContext);

            // Logger la requête
            getLogger().info(sql + "\n[" + login + ", " + profileId + ", " + lcContext + "]");

            stmt.executeUpdate();
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

    public void removeUserProfiles(String login, String context) throws RightsException
    {
        String lcContext = getFullContext (context);
        
        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "DELETE FROM " + _tableUserRights + " WHERE Login = ?";
            if (lcContext != null)
            {
                sql += " AND LOWER(Context) = ?";
            }

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, login);
            if (lcContext != null)
            {
                stmt.setString(2, lcContext);
            }

            // Logger la requête
            getLogger().info(sql + "\n[" + login + ", " + lcContext + "]");

            stmt.executeUpdate();
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
        
        Connection connection = ConnectionHelper.getConnection(_poolName);
        
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
        
        Connection connection = ConnectionHelper.getConnection(_poolName);
        
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
    
    public void removeGroupProfile(String groupId, String profileId, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;

        try
        {
            String sql = "DELETE FROM " + _tableGroupRights + " WHERE Group_Id = ? AND Profile_Id = ? AND LOWER(Context) = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, groupId);
            stmt.setInt(2, Integer.parseInt(profileId));
            stmt.setString(3, lcContext);

            // Logger la requête
            getLogger().info(sql + "\n[" + groupId + ", " + profileId + ", " + lcContext + "]");

            stmt.executeUpdate();
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

    public void removeGroupProfiles(String groupId, String context) throws RightsException
    {
        String lcContext = getFullContext (context);

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "DELETE FROM " + _tableGroupRights + " WHERE Group_Id = ?";

            if (lcContext != null)
            {
                sql += " AND LOWER(Context) = ?";
            }

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, groupId);
            if (lcContext != null)
            {
                stmt.setString(2, lcContext);
            }

            // Logger la requête
            getLogger().info(sql + "\n[" + groupId + ", " + lcContext + "]");

            stmt.executeUpdate();
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

        Set<User> users = new HashSet<User>();

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT DISTINCT Login " + "FROM " + _tableUserRights + " WHERE LOWER(Context) = ? ";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, lcContext);

            // Logger la requête
            getLogger().info(sql + "\n[" + lcContext + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String login = rs.getString(1);
                User principal = _usersManager.getUser(login);
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

        Set<Group> groups = new HashSet<Group>();

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT DISTINCT Group_Id " + "FROM " + _tableGroupRights + " WHERE LOWER(Context) = ? ";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, lcContext);

            // Logger la requête
            getLogger().info(sql + "\n[" + lcContext + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String groupId = rs.getString(1);
                Group group = _groupsManager.getGroup(groupId);
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
            String sql = "UPDATE " + _tableGroupRights + " SET Context = ? WHERE LOWER(Context) = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, fullNewContext);
            stmt.setString(2, fullOldContext);
            
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(sql + "\n[" + fullNewContext + ", " + fullOldContext + "]");
            }
            stmt.executeUpdate();
            
            ConnectionHelper.cleanup(stmt);
            
            sql = "UPDATE " + _tableUserRights + " SET Context = ? WHERE LOWER(Context) = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, fullNewContext);
            stmt.setString(2, fullOldContext);
            
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(sql + "\n[" + fullNewContext + ", " + fullOldContext + "]");
            }
            stmt.executeUpdate();
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
            getLogger().info(sql + "\n[" + fullContext + "]");
            stmt.executeUpdate();
            
            ConnectionHelper.cleanup(stmt);
            
            sql = "DELETE FROM " + _tableUserRights + " WHERE LOWER(Context) = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, fullContext);
            getLogger().info(sql + "\n[" + fullContext + "]");
            stmt.executeUpdate();
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
     * @param login the login of the user
     * @param right the right to verify
     * 
     * @return RIGHT_OK, RIGHT_NOK or RIGHT_UNKNOWN
     * 
     * @throws SQLException in case of connection error with the databse
     */
    private RightResult hasUserOnlyRight(String login, String right) throws SQLException
    {
        Connection connection = ConnectionHelper.getConnection(_poolName);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT UR.Login " + "FROM " + _tableProfileRights + " PR, " + _tableUserRights + " UR WHERE PR.Right_Id = ? " + "AND UR.Profile_Id = PR.Profile_Id " + "AND UR.Login = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, right);
            stmt.setString(2, login);

            // Logger la requête
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(sql + "\n[" + right + ", " + login + "]");
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
        sql.append("AND UR.Profile_Id = PR.Profile_Id AND UR.Login = ? ");
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
     * @param login the login of the user
     * @param right the right to verify
     * @param context the context
     * 
     * @return RIGHT_OK or RIGHT_UNKNOWN
     * 
     * @throws SQLException in case of connection error with the databse
     */
    private RightResult hasUserOnlyRight(String login, String right, String context) throws SQLException
    {
        Connection connection = ConnectionHelper.getConnection(_poolName);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = buildSQLStatementForUser(context);
            stmt = connection.prepareStatement(sql);

            stmt.setString(1, right);
            stmt.setString(2, login);
            stmt.setString(3, context);
            stmt.setString(4, context + "/%");
            
            int i = 0;
            String currentContext = HierarchicalRightsHelper.getParentContext(context);
            while (currentContext != null)
            {
                stmt.setString (5 + i, currentContext);
                currentContext = HierarchicalRightsHelper.getParentContext(currentContext);
                i++;
            }

            // Logger la requête
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(sql + "\n[" + right + ", " + login + (context != null ? "," + context : "") + "]");
            }

            rs = stmt.executeQuery();

            Map<String, List<String>> mapContext = getCacheContext(login, right);
            List<String> contextList = mapContext.get(context);
            if (contextList == null)
            {
                contextList = new ArrayList<String>();
                mapContext.put(context, contextList);
            }

            Map<String, Boolean> mapContext2 = _prepareCache2(login, right, context);
            
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
    
    private Map<String, Boolean> _prepareCache2(String login, String right, String context)
    {
        Map<String, Boolean> mapContext2 = getCache2Context(login, right);
        
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

    private Set<String> getGrantedUsersOnly(String right, String context) throws SQLException
    {
        Set<String> logins = new HashSet<String>();
        
        Connection connection = ConnectionHelper.getConnection(_poolName);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT DISTINCT UR.Login " + "FROM " + _tableProfileRights + " PR, " + _tableUserRights + " UR WHERE UR.Profile_Id = PR.Profile_Id " + "AND PR.Right_Id = ? ";

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
            getLogger().info(sql + "\n[" + right + (context != null ? "," + context : "") + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                logins.add(rs.getString(1));
            }

            return logins;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    private Set<String> getUsersOnlyRights(String login, String context) throws SQLException
    {
        Set<String> rights = new HashSet<String>();
        
        if (login == null)
        {
            return rights;
        }
        
        Connection connection = ConnectionHelper.getConnection(_poolName);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux utilisateurs
            StringBuffer sql = new StringBuffer();

            sql.append("SELECT DISTINCT PR.Right_id ");
            sql.append("FROM " + _tableProfileRights  + " PR, " + _tableUserRights + " UR ");
            sql.append("WHERE UR.Profile_Id = PR.Profile_Id ");
            sql.append("AND UR.Login = ? ");
            
            if (context != null)
            {
                sql.append("AND LOWER(UR.Context) ");
                sql.append(_getCondition(context));
                sql.append(" ?");
            }

            stmt = connection.prepareStatement(sql.toString());

            stmt.setString(1, login);

            if (context != null)
            {
                // LIKE query
                stmt.setString(2, context.replace('*', '%'));
            }

            // Logger la requête
            getLogger().info(sql + "\n[" + login + (context != null ? "," + context : "") + "]");

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
    
    private Map<String, Set<String>> getUsersOnlyRights(String login) throws SQLException
    {
        Map<String, Set<String>> rights = new HashMap<String, Set<String>>();

        Connection connection = ConnectionHelper.getConnection(_poolName);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux utilisateurs
            StringBuffer sql = new StringBuffer();

            sql.append("SELECT DISTINCT PR.Right_id, UR.Context ");
            sql.append("FROM " + _tableProfileRights  + " PR, " + _tableUserRights + " UR ");
            sql.append("WHERE UR.Profile_Id = PR.Profile_Id ");
            sql.append("AND UR.Login = ? ");
            
            stmt = connection.prepareStatement(sql.toString());
            stmt.setString(1, login);

            // Logger la requête
            getLogger().info(sql + "\n[" + login + "]");

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
    
    private Map<String, Set<String>> getUsersOnlyProfiles(String login) throws SQLException
    {
        Map<String, Set<String>> profiles = new HashMap<String, Set<String>>();
        
        Connection connection = ConnectionHelper.getConnection(_poolName);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux utilisateurs
            StringBuffer sql = new StringBuffer();
                
            sql.append("SELECT DISTINCT Profile_Id, Context ");
            sql.append("FROM " + _tableUserRights  + " ");
            sql.append("WHERE Login = ?");
            
            stmt = connection.prepareStatement(sql.toString());
            stmt.setString(1, login);

            // Logger la requête
            getLogger().info(sql + "\n[" + login + "]");

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
     * @param login the login of the user
     * @param right the right to verify
     * 
     * @return RIGHT_OK, RIGHT_NOK or RIGHT_UNKNOWN
     * 
     * @throws SQLException in case of connection error with the databse
     */
    private RightResult hasGroupOnlyRight(String login, String right) throws SQLException
    {
        Connection connection = ConnectionHelper.getConnection(_poolName);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            Set<String> groupsId = getGroupId(login);
            String groupSql = createGroupIdRequest(groupsId);

            if (groupSql != null)
            {
                String sql = "SELECT GR.Group_Id " + "FROM " + _tableProfileRights + " PR, " + _tableGroupRights + " GR WHERE PR.Right_Id = ? " + "AND GR.Profile_Id = PR.Profile_Id " + "AND (" + groupSql + ")";

                stmt = connection.prepareStatement(sql);

                int i = 1;
                stmt.setString(i++, right);
                fillStatement(stmt, null, groupsId, i);

                // Logger la requête
                if (getLogger().isInfoEnabled())
                {
                    getLogger().info(sql + "\n[" + right + ", " + login + "]");
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
     * @param login the login of the user
     * @param right the right to verify
     * @param context the context
     * @return RIGHT_OK or RIGHT_UNKNOWN
     * @throws SQLException in case of connection error with the databse
     */
    private RightResult hasGroupOnlyRight(String login, String right, String context) throws SQLException
    {
        Connection connection = ConnectionHelper.getConnection(_poolName);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Récupère la liste des groupes du user
            Set<String> groupsId = getGroupId(login);
            String groupSql = createGroupIdRequest(groupsId);

            if (groupSql != null)
            { // Construit le bout de sql pour tous les groupes
                String sql = buildSQLStatementForGroup(context, groupSql);

                stmt = connection.prepareStatement(sql);

                int i = 1;
                stmt.setString(i++, right);
                stmt.setString(i++, context);
                stmt.setString(i++, context + "/%");
                fillStatement(stmt, context, groupsId, i);

                // Logger la requête
                if (getLogger().isInfoEnabled())
                {
                    getLogger().info(sql + "\n[" + right + ", " + (context != null ? "," + context : "") + "]");
                }

                Map<String, List<String>> mapContext = getCacheContext(login, right);
                List<String> contextList = mapContext.get(context);
                if (contextList == null)
                {
                    contextList = new ArrayList<String>();
                    mapContext.put(context, contextList);
                }

                Map<String, Boolean> mapContext2 = _prepareCache2(login, right, context);
                
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

    private Set<String> getGrantedGroupsOnly(String right, String context) throws SQLException
    {
        Set<String> logins = new HashSet<String>();

        Connection connection = ConnectionHelper.getConnection(_poolName);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Puis parmi les droits affectés aux groups auxquels appartient
            // l'utilisateur
            String sql = "SELECT DISTINCT GR.Group_Id " + "FROM " + _tableProfileRights + " PR, " + _tableGroupRights + " GR WHERE GR.Profile_Id = PR.Profile_Id " + "AND PR.Right_Id = ? ";

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
            getLogger().info(sql + "\n[" + right + (context != null ? "," + context : "") + "]");

            // boucle sur les group id retrouvés
            rs = stmt.executeQuery();
            while (rs.next())
            {
                String groupId = rs.getString(1);
                
                Group group = _groupsManager.getGroup(groupId);
                if (group != null)
                {
                    for (String login : group.getUsers())
                    {
                        logins.add(login);
                    }
                }
                else if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("The group '" + groupId + "' is referenced in profile tables, but cannot be retrieve by GroupsManager. The database may be inconsistant.");
                }
            }

            return logins;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    private Set<String> getGroupsOnlyRights(String login, String context) throws SQLException
    {
        Set<String> rights = new HashSet<String>();

        Connection connection = ConnectionHelper.getConnection(_poolName);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Récupère la liste des groupes du user
            Set<String> userSGroup = _groupsManager.getUserGroups(login);
            if (userSGroup != null && userSGroup.size() != 0)
            {
                // Construit le bout de sql pour tous les groupes
                StringBuffer groupSql = new StringBuffer();

                for (int i = 0; i < userSGroup.size(); i++)
                {
                    if (i != 0)
                    {
                        groupSql.append(" OR ");
                    }
                    
                    groupSql.append("GR.Group_Id = ?");
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

                Iterator groupSqlIterator = userSGroup.iterator();
                int i = 1;

                if (context != null)
                {
                    // LIKE query
                    stmt.setString(i, context.replace('*', '%'));
                    i++;
                }

                while (groupSqlIterator.hasNext())
                {
                    String groupId = (String) groupSqlIterator.next();
                    stmt.setString(i, groupId);
                    i++;
                }

                // Logger la requête
                getLogger().info(sql + "\n[" + login + (context == null ? "" : "," + context) + "]");

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
    
    private Map<String, Set<String>> getGroupsOnlyRights(String login) throws SQLException
    {
        Map<String, Set<String>> rights = new HashMap<String, Set<String>>();

        Connection connection = ConnectionHelper.getConnection(_poolName);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Récupère la liste des groupes du user
            Set<String> userSGroup = _groupsManager.getUserGroups(login);
            if (userSGroup != null && userSGroup.size() != 0)
            {
                // Construit le bout de sql pour tous les groupes
                StringBuffer groupSql = new StringBuffer();

                for (int i = 0; i < userSGroup.size(); i++)
                {
                    if (i != 0)
                    {
                        groupSql.append(" OR ");
                    }
                    
                    groupSql.append("GR.Group_Id = ?");
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

                Iterator groupSqlIterator = userSGroup.iterator();
                int i = 1;

                while (groupSqlIterator.hasNext())
                {
                    String groupId = (String) groupSqlIterator.next();
                    stmt.setString(i, groupId);
                    i++;
                }

                // Logger la requête
                getLogger().info(sql + "\n[" + login + "]");

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
    
    private Map<String, Set<String>> getGroupsOnlyProfiles(String login) throws SQLException
    {
        Map<String, Set<String>> profiles = new HashMap<String, Set<String>>();

        Connection connection = ConnectionHelper.getConnection(_poolName);
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // Récupère la liste des groupes du user
            Set<String> userSGroup = _groupsManager.getUserGroups(login);
            if (userSGroup != null && userSGroup.size() != 0)
            {
                // Construit le bout de sql pour tous les groupes
                StringBuffer groupSql = new StringBuffer();

                for (int i = 0; i < userSGroup.size(); i++)
                {
                    if (i != 0)
                    {
                        groupSql.append(" OR ");
                    }
                    
                    groupSql.append("GR.Group_Id = ?");
                }

                // Puis parmi les profils affectés aux groupes auxquels appartient l'utilisateur
                StringBuffer sql = new StringBuffer();
                
                sql.append("SELECT DISTINCT GR.Profile_Id, GR.Context ");
                sql.append("FROM Rights_GroupRights GR ");
                sql.append("WHERE ");
                sql.append(groupSql);

                stmt = connection.prepareStatement(sql.toString());

                Iterator groupSqlIterator = userSGroup.iterator();
                int i = 1;

                while (groupSqlIterator.hasNext())
                {
                    String groupId = (String) groupSqlIterator.next();
                    stmt.setString(i, groupId);
                    i++;
                }

                // Logger la requête
                getLogger().info(sql + "\n[" + login + "]");

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
     * @param login the login of the user
     * @param right the right to verify
     * @param rootContext the context beginning
     * @param endContext the context ending
     * 
     * @return RIGHT_OK or RIGHT_UNKNOWN
     * @throws RightOnContextNotInCacheException if the right on this context is
     *             not in cache
     */
    private RightResult hasUserRightInCache(String login, String right, String rootContext, String endContext) throws RightOnContextNotInCacheException
    {
        Map<String, Map<String, Map<String, List<String>>>> mapCache = _cacheTL.get();

        if (mapCache == null)
        {
            mapCache = new HashMap<String, Map<String, Map<String, List<String>>>>();
            _cacheTL.set(mapCache);
        }

        if (mapCache.containsKey(login) && rootContext != null)
        {
            Map<String, Map<String, List<String>>> mapRight = mapCache.get(login);
            if (mapRight.containsKey(right))
            {
                Map<String, List<String>> mapContext = mapRight.get(right);
                if (mapContext.containsKey(rootContext))
                {
                    // Liste des sous contexte
                    List<String> contextList = mapContext.get(rootContext);
                    if (contextList != null && contextList.contains(endContext))
                    {
                        getLogger().info("Find in cache the right " + right + "for user " + login + "on context [" + rootContext + ", " + endContext + "]");
                        return RightResult.RIGHT_OK;
                    }
                    else
                    {
                        getLogger().info("In cache, user " + login + " has not the right " + right + " on context [" + rootContext + ", " + endContext + "]");
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
                    return hasUserRightInCache(login, right, newRootContext, newEndContext);
                }
            }
        }
        throw new RightOnContextNotInCacheException();
    }
    
    private RightResult hasUserRightInCache2(String login, String right, String context) throws RightOnContextNotInCacheException
    {
        Map<String, Map<String, Map<String, Boolean>>> mapCache = _cache2TL.get();

        if (mapCache == null)
        {
            mapCache = new HashMap<String, Map<String, Map<String, Boolean>>>();
            _cache2TL.set(mapCache);
        }

        if (mapCache.containsKey(login) && context != null)
        {
            Map<String, Map<String, Boolean>> mapRight = mapCache.get(login);
            if (mapRight.containsKey(right))
            {
                Map<String, Boolean> mapContext = mapRight.get(right);
                if (mapContext.containsKey(context))
                {
                    Boolean hasRight = mapContext.get(context);
                    if (hasRight.booleanValue())
                    {
                        getLogger().info("Find in cache2 the right " + right + "for user " + login + "on context [" + context + "]");
                        return RightResult.RIGHT_OK;
                    }
                    else
                    {
                        getLogger().info("In cache2, user " + login + " has not the right " + right + " on context [" + context + "]");
                        return RightResult.RIGHT_UNKNOWN;
                    }
                }
            }
        }
        throw new RightOnContextNotInCacheException();
    }

    private Set<String> getGroupId(String login)
    {
        // Récupère la liste des groupes du user
        Set<String> userSGroup = _groupsManager.getUserGroups(login);
        if (userSGroup == null || userSGroup.size() == 0)
        {
            return null;
        }
        else
        {
            return userSGroup;
        }
    }

    private String createGroupIdRequest(Set userSGroup)
    {
        if (userSGroup == null || userSGroup.size() == 0)
        {
            return null;
        }

        // Construit le bout de sql pour tous les groupes
        StringBuffer groupSql = new StringBuffer();

        for (int i = 0; i < userSGroup.size(); i++)
        {
            if (i != 0)
            {
                groupSql.append(" OR ");
            }
            groupSql.append("GR.Group_Id = ?");
        }

        return "(" + groupSql.toString() + ")";
    }

    private void fillStatement(PreparedStatement stmt, String context, Set<String> userSGroup, int startIndex) throws SQLException
    {
        int i = startIndex;

        String currentContext = HierarchicalRightsHelper.getParentContext(context);
        while (currentContext != null)
        {
            stmt.setString (i++, currentContext);
            currentContext = HierarchicalRightsHelper.getParentContext(currentContext);
        }

        for (String groupId : userSGroup)
        {
            stmt.setString(i++, groupId);
        }

    }

    private Map<String, List<String>> getCacheContext(String login, String right)
    {
        Map<String, Map<String, Map<String, List<String>>>> mapCache = _cacheTL.get();

        Map<String, Map<String, List<String>>> mapRight = mapCache.get(login);
        if (mapRight == null)
        {
            mapRight = new HashMap<String, Map<String, List<String>>>();
            mapCache.put(login, mapRight);
        }

        Map<String, List<String>> mapContext = mapRight.get(right);
        if (mapContext == null)
        {
            mapContext = new HashMap<String, List<String>>();
            mapRight.put(right, mapContext);
        }

        return mapContext;
    }

    private Map<String, Boolean> getCache2Context(String login, String right)
    {
        Map<String, Map<String, Map<String, Boolean>>> mapCache = _cache2TL.get();

        Map<String, Map<String, Boolean>> mapRight = mapCache.get(login);
        if (mapRight == null)
        {
            mapRight = new HashMap<String, Map<String, Boolean>>();
            mapCache.put(login, mapRight);
        }

        Map<String, Boolean> mapContext = mapRight.get(right);
        if (mapContext == null)
        {
            mapContext = new HashMap<String, Boolean>();
            mapRight.put(right, mapContext);
        }

        return mapContext;
    }
    
    /**
    * Get the list of declared contexts on which an user has the given right
    * @param login The user's login. Cannot be null.
    * @param rightId the id of the right to check. Cannot be null.
    * @param initialContext the initial context to filter the results. Cannot be null.
    * @return The Set containing the contexts
    * @throws RightsException if an error occurs.
    */
    protected Set<String> getDeclaredContexts(String login, String rightId, String initialContext)
    {
        HashSet<String> contexts = new HashSet<String>();
        
        RightResult result = hasRight(login, rightId, initialContext);
        if (result == RightResult.RIGHT_OK)
        {
            contexts.add(initialContext);
        }
        
        String prefix = _rightsContextPrefixEP.getContextPrefix() + "/" + (StringUtils.isNotEmpty(initialContext) ? initialContext + "/" : "");
        // The cache is necessarily filled has we first call 'hasRight' method
        Map<String, List<String>> cacheContext = getCacheContext(login, rightId);
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
    
    public Profile getProfile(String id)
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
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
    
    public Profile addProfile(String name, String context) throws RightsException
    {
        Integer id = null;
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            DatabaseType dbType = ConnectionHelper.getDatabaseType(connection);
            
            if (dbType == DatabaseType.DATABASE_ORACLE || dbType == DatabaseType.DATABASE_POSTGRES)
            {
                if (DatabaseType.DATABASE_ORACLE.equals(dbType))
                {
                    statement = connection.prepareStatement("SELECT seq_rights_profile.nextval FROM dual");
                }
                else // if (DatabaseType.DATABASE_POSTGRES.equals(dbType))
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
            
            //FIXME Write query working with all database
            if (dbType == DatabaseType.DATABASE_MYSQL)
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
            else if (dbType == DatabaseType.DATABASE_DERBY)
            {
                statement = connection.prepareStatement("VALUES IDENTITY_VAL_LOCAL ()");
                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getInt(1);
                }
            }
            else if (dbType == DatabaseType.DATABASE_HSQLDB)
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
        Set<Profile> profiles = new HashSet<Profile>();
        
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
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
    
    public Set<Profile> getAllProfiles()
    {
        Set<Profile> profiles = new HashSet<Profile>();
        
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
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
    
    public void userAdded(String login)
    {
        // Nothing
    }

    public void userUpdated(String login)
    {
        // Nothing
    }

    public void userRemoved(String login)
    {
        removeUserProfiles(login, null);
    }

    public void groupAdded(String groupID)
    {
        // Nothing
    }

    public void groupUpdated(String groupID)
    {
        // Nothing
    }

    public void groupRemoved(String groupID)
    {
        removeGroupProfiles(groupID, null);
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
        private String _id;
        private String _name;
        private String _context;
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
        
        public String getId()
        {
            return _id;
        }
        
        public String getName()
        {
            return _name;
        }
        
        @Override
        public String getContext()
        {
            return _context;
        }
        
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
        
        public void rename(String newName)
        {
            Connection connection = ConnectionHelper.getConnection(_poolName);
            
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
        
        public Set<String> getRights()
        {
            Set<String> rights = new HashSet<String>();
            
            Connection connection = ConnectionHelper.getConnection(_poolName);
            
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
        
        public void remove()
        {
            Connection connection = null;
            PreparedStatement statement = null;
            PreparedStatement statement2 = null;
            PreparedStatement statement3 = null;
            PreparedStatement statement4 = null;
            
            try
            {
                connection = ConnectionHelper.getConnection(_poolName);
                
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
            _currentConnection = ConnectionHelper.getConnection(_poolName);
            
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
                return ConnectionHelper.getConnection(_poolName);
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
