/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
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
import org.ametys.runtime.plugins.core.right.RightsExtensionPoint;
import org.ametys.runtime.request.RequestListener;
import org.ametys.runtime.request.RequestListenerManager;
import org.ametys.runtime.right.HierarchicalRightsHelper;
import org.ametys.runtime.right.InitializableRightsManager;
import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UserListener;
import org.ametys.runtime.user.UsersManager;


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
        _usersManager = (UsersManager) _manager.lookup(UsersManager.ROLE);
        _groupsManager = (GroupsManager) _manager.lookup(GroupsManager.ROLE);
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
            String description = rightConf.getChild("description").getValue("");
            String category = rightConf.getChild("category").getValue("");
            
            if (id.length() == 0 || label.length() == 0 || description.length() == 0 || category.length() == 0)
            {
                String message = "Error in " + DefaultProfileBasedRightsManager.class.getName() + " configuration : attribute 'id' and elements 'label', 'description' and 'category' are mandatory.";
                getLogger().error(message);
                throw new ConfigurationException(message, configuration);
            }

            _rightsEP.addRight(id, label, description, category, "application");
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

    public Set<String> getGrantedUsers(String right, String context)
    {
        Set<String> logins = new HashSet<String>();

        try
        {
            logins.addAll(getGrantedUsersOnly(right, context));
            logins.addAll(getGrantedGroupsOnly(right, context));
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);

        }

        return logins;
    }

    public Set<String> getUserRights(String login, String context)
    {
        Set<String> rights = new HashSet<String>();

        try
        {
            rights.addAll(getUsersOnlyRights(login, context));
            rights.addAll(getGroupsOnlyRights(login, context));
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
        }

        return rights;
    }

    public void grantAllPrivileges(String login, String context)
    {
        // On crée un profile
        /** FIXME ne pas recréer si existe déjà */
        Profile adminProfile = addProfile(__INITIAL_PROFILE_ID);

        // On lui met tous les droits
        Collection currentRights = adminProfile.getRights();
        for (Object rightId : _rightsEP.getExtensionsIds())
        {
            if (!currentRights.contains(rightId))
            {
                adminProfile.addRight((String) rightId);
            }
        }
        
        // On affecte le profil
        addUserRight(login, context, adminProfile.getId());
    }

    public RightResult hasRight(String userLogin, String right, String context)
    {
        try
        {
            return hasUserRightInCache(userLogin, right, context, "");
        }
        catch (RightOnContextNotInCacheException e)
        {
            getLogger().info("No find entry in cache for [" + userLogin + ", " + right + ", " + context + "]");
        }

        try
        {
            return hasUserRightInCache2(userLogin, right, context);
        }
        catch (RightOnContextNotInCacheException e)
        {
            getLogger().info("No find entry in cache2 for [" + userLogin + ", " + right + ", " + context + "]");
        }

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            RightResult userResult = context != null ? hasUserOnlyRight(userLogin, right, context) : hasUserOnlyRight(userLogin, right);
            if (userResult == RightResult.RIGHT_OK)
            {
                // Droit en tant qu'utilisateur
                return RightResult.RIGHT_OK;
            }

            // Puis parmi les droits affectés aux groupes auxquels
            // appartient l'utilisateur
            RightResult groupResult = context != null ? hasGroupOnlyRight(userLogin, right, context) : hasGroupOnlyRight(userLogin, right);
            if (groupResult == RightResult.RIGHT_OK)
            {
                // Droit en tant que groupe d'utilisateurs
                return RightResult.RIGHT_OK;
            }

            return RightResult.RIGHT_UNKNOWN;
        }
        catch (SQLException se)
        {
            getLogger().error("Error communication with database", se);
            // Refuser par sécurité
            return RightResult.RIGHT_NOK;
        }
    }

    /* METHODES AJOUTEES PAR LE RIGHT MANAGER */

    /**
     * Returns a Set containing all profiles for a given user and a context
     * 
     * @param login the login of the concerned user
     * @param context the context
     * @return a Set containing all profiles for a given user and a context
     */
    public Set<String> getProfilesByUser(String login, String context)
    {
        Set<String> profiles = new HashSet<String>();

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT UR.Profile_Id " + "FROM " + _tableUserRights + " UR WHERE UR.Login = ? AND UR.Context = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, login);
            stmt.setString(2, context);

            // Logger la requête
            getLogger().info(sql + "\n[" + login + ", " + context + "]");

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
     * Returns a Set containing all contexts for an user and a profile
     * 
     * @param login The user's login
     * @param profileID The profile
     * @return a Set containing all contexts for an user and a profile
     */
    public Set<String> getContextByUserAndProfile(String login, String profileID)
    {
        Set<String> contexts = new HashSet<String>();

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT UR.Context " + "FROM " + _tableUserRights + " UR WHERE UR.Login = ? AND UR.Profile_Id = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, login);
            stmt.setString(2, profileID);

            // Logger la requête
            getLogger().info(sql + "\n[" + login + ", " + profileID + "]");

            rs = stmt.executeQuery();

            while (rs.next())
            {
                contexts.add(rs.getString(1));
            }
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);

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
     * Returns a Set containing all profiles for a group and a context
     * 
     * @param groupId the group
     * @param context the context
     * @return a Set containing all profiles for a group and a context
     */
    public Set<String> getProfilesByGroup(String groupId, String context)
    {
        Set<String> profiles = new HashSet<String>();

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT GR.Profile_Id " + "FROM " + _tableGroupRights + " GR WHERE GR.Group_Id = ? AND GR.Context = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, groupId);
            stmt.setString(2, context);

            // Logger la requête
            getLogger().info(sql + "\n[" + groupId + ", " + context + "]");

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
     */
    public Set<String> getContextByGroupAndProfile(String groupID, String profileID)
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
            stmt.setString(2, profileID);

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
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);

        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }

        return contexts;
    }

    public void addUserRight(String login, String context, String profileId)
    {
        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "INSERT INTO " + _tableUserRights + " (Profile_Id, Login, Context) VALUES(?, ?, ?)";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, profileId);
            stmt.setString(2, login);
            stmt.setString(3, context);

            // Logger la requête
            getLogger().info(sql + "\n[" + profileId + ", " + login + ", " + context + "]");

            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);

        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    public void addGroupRight(String groupId, String context, String profileId)
    {
        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "INSERT INTO " + _tableGroupRights + " (Profile_Id, Group_Id, Context) VALUES(?, ?, ?)";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, profileId);
            stmt.setString(2, groupId);
            stmt.setString(3, context);

            // Logger la requête
            getLogger().info(sql + "\n[" + profileId + ", " + groupId + ", " + context + "]");

            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);

        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    public void removeUserProfile(String login, String profile, String context)
    {
        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "DELETE FROM " + _tableUserRights + " WHERE Login = ? AND Profile_Id = ? AND Context = ? ";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, login);
            stmt.setString(2, profile);
            stmt.setString(3, context);

            // Logger la requête
            getLogger().info(sql + "\n[" + login + ", " + profile + ", " + context + "]");

            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);

        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    public void removeUserProfiles(String login, String context)
    {
        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "DELETE FROM " + _tableUserRights + " WHERE Login = ?";
            if (context != null)
            {
                sql += " AND Context = ?;";
            }

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, login);
            if (context != null)
            {
                stmt.setString(2, context);
            }

            // Logger la requête
            getLogger().info(sql + "\n[" + login + ", " + context + "]");

            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);

        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

  
    public void removeAll(String context)
    {
        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;

        try
        {
            String sql;

            sql = "DELETE FROM " + _tableGroupRights + " WHERE Context = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, context);
            getLogger().info(sql + "\n[" + context + "]");
            stmt.executeUpdate();

            ConnectionHelper.cleanup(stmt);

            sql = "DELETE FROM " + _tableUserRights + " WHERE Context = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, context);
            getLogger().info(sql + "\n[" + context + "]");
            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);

        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }

    }

    public void removeGroupProfile(String groupId, String profile, String context)
    {
        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;

        try
        {
            String sql = "DELETE FROM " + _tableGroupRights + " WHERE Group_Id = ? AND Profile_Id = ? AND Context = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, groupId);
            stmt.setString(2, profile);
            stmt.setString(3, context);

            // Logger la requête
            getLogger().info(sql + "\n[" + groupId + ", " + profile + ", " + context + "]");

            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);

        }
        finally
        {
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }
    }

    public void removeGroupProfiles(String groupId, String context)
    {
        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "DELETE FROM " + _tableGroupRights + " WHERE Group_Id = ?";

            if (context != null)
            {
                sql += " AND Context = ?";
            }

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, groupId);
            if (context != null)
            {
                stmt.setString(2, context);
            }

            // Logger la requête
            getLogger().info(sql + "\n[" + groupId + ", " + context + "]");

            stmt.executeUpdate();
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);

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
     */
    public Set<User> getUsersByContext(String context)
    {
        Set<User> users = new HashSet<User>();

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT DISTINCT Login " + "FROM " + _tableUserRights + " WHERE Context = ? ";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, context);

            // Logger la requête
            getLogger().info(sql + "\n[" + context + "]");

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
     */
    public Set<Group> getGroupsByContext(String context)
    {
        Set<Group> groups = new HashSet<Group>();

        Connection connection = ConnectionHelper.getConnection(_poolName);

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            // On regarde d'abord parmi les droits affectés directement aux
            // utilisateurs
            String sql = "SELECT DISTINCT Group_Id " + "FROM " + _tableGroupRights + " WHERE Context = ? ";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, context);

            // Logger la requête
            getLogger().info(sql + "\n[" + context + "]");

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
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(connection);
        }

        return groups;
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
        sql.append("SELECT UR.Context ");
        sql.append("FROM " + _tableProfileRights + " PR, " + _tableUserRights + " UR ");
        sql.append("WHERE PR.Right_Id = ? ");
        sql.append("AND UR.Profile_Id = PR.Profile_Id AND UR.Login = ? ");
        sql.append("AND (UR.Context = ? OR UR.Context LIKE ? ");
        
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
                sql += " AND UR.Context = ?";
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
                sql.append("AND UR.Context ");
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
        sql.append("AND (GR.Context = ? OR GR.Context LIKE ? ");
        
        String currentContext = HierarchicalRightsHelper.getParentContext(context);
        while (currentContext != null)
        {
            sql.append("OR GR.Context = ? ");
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
                sql += " AND GR.Context = ?";
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
                    sql.append("AND GR.Context ");
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
    
    public Profile getProfile(String id)
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            statement = connection.prepareStatement("SELECT Label FROM " + _tableProfile + " WHERE Id = ?");
            statement.setInt(1, Integer.parseInt(id));
            
            rs = statement.executeQuery();
            
            if (rs.next())
            {
                String label = rs.getString("Label");
                return new DefaultProfile(id, label);
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
    
    public Profile addProfile(String name)
    {
        String id;
        
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            
            statement = connection.prepareStatement("INSERT INTO " + _tableProfile + " (Label) VALUES(?)");
            statement.setString(1, name);
            statement.executeUpdate();
            ConnectionHelper.cleanup(statement);
            
            if (DatabaseType.DATABASE_POSTGRES.equals(ConnectionHelper.getDatabaseType(connection)))
            {
                statement = connection.prepareStatement("SELECT Id FROM " + _tableProfile + " WHERE Id = (SELECT MAX(Id) from " + _tableProfile + ")");
            }
            else
            {
                statement = connection.prepareStatement("SELECT Id FROM " + _tableProfile + " WHERE Id = last_insert_id()");
            }
            
            rs = statement.executeQuery();
            if (rs.next())
            {
                id = rs.getString("Id");
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
        
        return new DefaultProfile(id, name);
    }
    
    public Set<Profile> getProfiles()
    {
        Set<Profile> profiles = new HashSet<Profile>();
        
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT Id, Label FROM " + _tableProfile);
            
            while (rs.next())
            {
                String id = rs.getString("Id");
                String label = rs.getString("Label");
                profiles.add(new DefaultProfile(id, label));
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
        
        /**
         * Constructor.
         * @param id the unique id of this profile
         * @param name the name of this profile
         */
        public DefaultProfile(String id, String name)
        {
            _id = id;
            _name = name;
        }
        
        public String getId()
        {
            return _id;
        }
        
        public String getName()
        {
            return _name;
        }
        
        public void addRight(String rightId)
        {
            Connection connection = ConnectionHelper.getConnection(_poolName);
            
            try
            {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO " + _tableProfileRights + " (Profile_Id, Right_Id) VALUES(?, ?)");
                statement.setString(1, _id);
                statement.setString(2, rightId);
                statement.executeUpdate();
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
            finally
            {
                try
                {
                    if (connection != null)
                    {
                        if (!connection.getAutoCommit())
                        {
                            connection.commit();
                        }
                        
                        connection.close();
                    }
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
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
                statement.setString(2, _id);
                statement.executeUpdate();
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
            finally
            {
                try
                {
                    if (connection != null)
                    {
                        if (!connection.getAutoCommit())
                        {
                            connection.commit();
                        }

                        connection.close();
                    }
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        
        public Set<String> getRights()
        {
            Set<String> rights = new HashSet<String>();
            
            Connection connection = ConnectionHelper.getConnection(_poolName);
            
            try
            {
                PreparedStatement statement = connection.prepareStatement("SELECT Right_Id FROM " + _tableProfileRights + " WHERE profile_Id = ?");
                statement.setString(1, _id);
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
                try
                {
                    if (connection != null)
                    {
                        if (!connection.getAutoCommit())
                        {
                            connection.commit();
                        }

                        connection.close();
                    }
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
                }
            }
            
            return rights;
        }
        
        public void removeRights()
        {
            Connection connection = ConnectionHelper.getConnection(_poolName);
            
            try
            {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM " + _tableProfileRights + " WHERE Profile_Id = ?");
                statement.setString(1, _id);
                statement.executeUpdate();
            }
            catch (SQLException ex)
            {
                throw new RuntimeException(ex);
            }
            finally
            {
                try
                {
                    if (connection != null)
                    {
                        if (!connection.getAutoCommit())
                        {
                            connection.commit();
                        }

                        connection.close();
                    }
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
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
                statement.setString(1, _id);
                statement.executeUpdate();
                
                statement2 = connection.prepareStatement("DELETE FROM " + _tableProfileRights + " WHERE Profile_Id = ?");
                statement2.setString(1, _id);
                statement2.executeUpdate();

                statement3 = connection.prepareStatement("DELETE FROM " + _tableUserRights + " WHERE Profile_Id = ?");
                statement3.setString(1, _id);
                statement3.executeUpdate();

                statement4 = connection.prepareStatement("DELETE FROM " + _tableGroupRights + " WHERE Profile_Id = ?");
                statement4.setString(1, _id);
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
        
        public void toSAX(ContentHandler handler) throws SAXException
        {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "id", "id", "CDATA", _id);
            XMLUtils.startElement(handler, "profile", atts);

            XMLUtils.createElement(handler, "label", _name);
                    
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
