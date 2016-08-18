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
package org.ametys.plugins.core.impl.right;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.right.ProfileAssignmentStorage;
import org.ametys.core.right.RightsException;
import org.ametys.core.user.UserIdentity;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Jdbc implementation of {@link ProfileAssignmentStorage} which stores profile assignments in database.
 * This only supports String objects as contexts.
 */
public class JdbcProfileAssignmentStorage extends AbstractLogEnabled implements ProfileAssignmentStorage, Configurable
{
    /** The id of the data source to use */
    protected String _dataSourceId;
    
    /** The jdbc table name for association between profiles and contexts (any connected user will get the given allowed profile on the given context) */
    protected String _tableAllowedProfilesAnyConnected;
    /** The jdbc table name for association between profiles and contexts (any connected user will get the given denied profile on the given context) */
    protected String _tableDeniedProfilesAnyConnected;
    /** The jdbc table name for association between profiles and contexts (any connected user will get the given allowed profile on the given context) */
    protected String _tableAllowedProfilesAnonymous;
    /** The jdbc table name for association between profiles and contexts (any connected user will get the given denied profile on the given context) */
    protected String _tableDeniedProfilesAnonymous;
    
    /** The jdbc table name for association between allowed users, profiles and contexts */
    protected String _tableAllowedUsers;
    /** The jdbc table name for association between denied users, profiles and contexts */
    protected String _tableDeniedUsers;
    /** The jdbc table name for association between allowed groups, profiles and contexts */
    protected String _tableAllowedGroups;
    /** The jdbc table name for association between denied groups, profiles and contexts */
    protected String _tableDeniedGroups;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
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
        
        _tableAllowedProfilesAnyConnected = configuration.getChild("table-allowed-profiles-any-connected").getValue("Rights_AllowedProfilesAnyCon");
        _tableDeniedProfilesAnyConnected = configuration.getChild("table-denied-profiles-any-connected").getValue("Rights_DeniedProfilesAnyCon");
        _tableAllowedProfilesAnonymous = configuration.getChild("table-allowed-profiles-anonymous").getValue("Rights_AllowedProfilesAnonym");
        _tableDeniedProfilesAnonymous = configuration.getChild("table-denied-profiles-anonymous").getValue("Rights_DeniedProfilesAnonym");
        
        _tableAllowedUsers = configuration.getChild("table-profile-allowed-users").getValue("Rights_AllowedUsers");
        _tableDeniedUsers = configuration.getChild("table-profile-denied-users").getValue("Rights_DeniedUsers");
        _tableAllowedGroups = configuration.getChild("table-profile-allowed-groups").getValue("Rights_AllowedGroups");
        _tableDeniedGroups = configuration.getChild("table-profile-denied-groups").getValue("Rights_DeniedGroups");
    }
    
    /**
     * Get the connection to the database 
     * @return the SQL connection
     */
    protected Connection getSQLConnection ()
    {
        return ConnectionHelper.getConnection(_dataSourceId);
    }
    
    /* -------------- */
    /* HAS PERMISSION */
    /* -------------- */
    
    @Override
    public boolean hasPermission(UserIdentity user, Set<GroupIdentity> userGroups, Set<String> profileIds)
    {
        if (profileIds.isEmpty())
        {
            return false;
        }
        
        // 1.1) Search at least one profile in "denied-profiles" for user, if found return false
        if (_hasDeniedProfile(user, profileIds, null))
        {
            return false;
        }
        
        // 1.2) Search at least one profile in "allowed-profiles" for user, if found return true
        if (_hasAllowedProfile(user, profileIds, null))
        {
            return true;
        }
        
        // 2.1) Search at least one profile in "denied-profiles" for groups, if found return false
        for (GroupIdentity group : userGroups)
        {
            if (_hasDeniedProfile(group, profileIds, null))
            {
                return false;
            }
        }
        
        // 2.2) Search at least one profile in "allowed-profiles" for groups, if found return true
        for (GroupIdentity group : userGroups)
        {
            if (_hasAllowedProfile(group, profileIds, null))
            {
                return true;
            }
        }
        
        // 3.1) Search at least one profile in "denied-any-connected-profiles", if found return false
        if (_hasAnyConnectedDeniedProfile(profileIds, null))
        {
            return false;
        }
            
        // 3.2) Search at least one profile in "allowed-any-connected-profiles", if found return true
        if (_hasAnyConnectedAllowedProfile(profileIds, null))
        {
            return true;
        }
        
        
        // 4.1) Search at least one profile in "denied-any-connected-profiles", if found return false
        if (_hasAnonymousDeniedProfile(profileIds, null))
        {
            return false;
        }
        
        // 4.2) Search at least one profile in "allowed-any-connected-profiles", if found return true
        if (_hasAnonymousAllowedProfile(profileIds, null))
        {
            return true;
        }
        
        // 5) Not found, return false
        return false;
    }
    
    /**
     * Returns true if any context has one of the given profiles as denied for the user
     * @param user The user
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as denied for the user
     */
    protected boolean _hasDeniedProfile(UserIdentity user, Set<String> profileIds, String prefix)
    {
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
            sqlBuilder.append(_tableDeniedUsers);
            sqlBuilder.append(" WHERE Login=? AND UserPopulation_Id=?");
            if (prefix != null)
            {
                sqlBuilder.append(" AND UPPER(Context) LIKE UPPER(?)");
            }
            sqlBuilder.append(" AND (");
            for (int j = 0; j < profileIds.size(); j++)
            {
                sqlBuilder.append(j == 0 ? "Profile_Id = ?" : " OR Profile_Id = ?");
            }
            sqlBuilder.append(")");
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());
            int i;
            if (prefix != null)
            {
                stmt.setString(3, prefix + "%");
                i = 4;
            }
            else
            {
                i = 3;
            }
            for (String profileId : profileIds)
            {
                stmt.setString(i++, profileId);
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}, {}, {}, {}]", sql, user.getLogin(), user.getPopulationId(), prefix + "%", profileIds);
            }
            else
            {
                getLogger().debug("{}\n[{}, {}, {}]", sql, user.getLogin(), user.getPopulationId(), profileIds);
            }
            rs = stmt.executeQuery();
            if (rs.next())
            {
                return true;
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
        
        return false;
    }
    
    /**
     * Returns true if any context has one of the given profiles as allowed for the user
     * @param user The user
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as allowed for the user
     */
    protected boolean _hasAllowedProfile(UserIdentity user, Set<String> profileIds, String prefix)
    {
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {  
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
            sqlBuilder.append(_tableAllowedUsers);
            sqlBuilder.append(" WHERE Login=? AND UserPopulation_Id=?");
            if (prefix != null)
            {
                sqlBuilder.append(" AND UPPER(Context) LIKE UPPER(?)");
            }
            sqlBuilder.append(" AND (");
            for (int j = 0; j < profileIds.size(); j++)
            {
                sqlBuilder.append(j == 0 ? "Profile_Id = ?" : " OR Profile_Id = ?");
            }
            sqlBuilder.append(")");
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());
            int i;
            if (prefix != null)
            {
                stmt.setString(3, prefix + "%");
                i = 4;
            }
            else
            {
                i = 3;
            }
            for (String profileId : profileIds)
            {
                stmt.setString(i++, profileId);
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}, {}, {}, {}]", sql, user.getLogin(), user.getPopulationId(), prefix + "%", profileIds);
            }
            else
            {
                getLogger().debug("{}\n[{}, {}, {}]", sql, user.getLogin(), user.getPopulationId(), profileIds);
            }
            rs = stmt.executeQuery();
            if (rs.next())
            {
                return true;
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
        
        return false;
    }
    
    /**
     * Returns true if any context has one of the given profiles as denied for the group
     * @param group The group
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as denied for the group
     */
    protected boolean _hasDeniedProfile(GroupIdentity group, Set<String> profileIds, String prefix)
    {
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
            sqlBuilder.append(_tableDeniedGroups);
            sqlBuilder.append(" WHERE Group_Id=? AND GroupDirectory_Id=?");
            if (prefix != null)
            {
                sqlBuilder.append(" AND UPPER(Context) LIKE UPPER(?)");
            }
            sqlBuilder.append(" AND (");
            for (int j = 0; j < profileIds.size(); j++)
            {
                sqlBuilder.append(j == 0 ? "Profile_Id = ?" : " OR Profile_Id = ?");
            }
            sqlBuilder.append(")");
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, group.getId());
            stmt.setString(2, group.getDirectoryId());
            int i;
            if (prefix != null)
            {
                stmt.setString(3, prefix + "%");
                i = 4;
            }
            else
            {
                i = 3;
            }
            for (String profileId : profileIds)
            {
                stmt.setString(i++, profileId);
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}, {}, {}, {}]", sql, group.getId(), group.getDirectoryId(), prefix + "%", profileIds);
            }
            else
            {
                getLogger().debug("{}\n[{}, {}, {}]", sql, group.getId(), group.getDirectoryId(), profileIds);
            }
            rs = stmt.executeQuery();
            if (rs.next())
            {
                return true;
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
        
        return false;
    }
    
    /**
     * Returns true if any context has one of the given profiles as allowed for the group
     * @param group The group
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as allowed for the group
     */
    protected boolean _hasAllowedProfile(GroupIdentity group, Set<String> profileIds, String prefix)
    {
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {  
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
            sqlBuilder.append(_tableAllowedGroups);
            sqlBuilder.append(" WHERE Group_Id=? AND GroupDirectory_Id=?");
            if (prefix != null)
            {
                sqlBuilder.append(" AND UPPER(Context) LIKE UPPER(?)");
            }
            sqlBuilder.append(" AND (");
            for (int j = 0; j < profileIds.size(); j++)
            {
                sqlBuilder.append(j == 0 ? "Profile_Id = ?" : " OR Profile_Id = ?");
            }
            sqlBuilder.append(")");
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, group.getId());
            stmt.setString(2, group.getDirectoryId());
            int i;
            if (prefix != null)
            {
                stmt.setString(3, prefix + "%");
                i = 4;
            }
            else
            {
                i = 3;
            }
            for (String profileId : profileIds)
            {
                stmt.setString(i++, profileId);
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}, {}, {}, {}]", sql, group.getId(), group.getDirectoryId(), prefix + "%", profileIds);
            }
            else
            {
                getLogger().debug("{}\n[{}, {}, {}]", sql, group.getId(), group.getDirectoryId(), profileIds);
            }
            rs = stmt.executeQuery();
            if (rs.next())
            {
                return true;
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
        
        return false;
    }
    
    /**
     * Returns true if any context has one of the given profiles as denied for any connected user
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as denied for any connected user
     */
    protected boolean _hasAnyConnectedDeniedProfile(Set<String> profileIds, String prefix)
    {
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null; 
        
        try
        {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
            sqlBuilder.append(_tableDeniedProfilesAnyConnected);
            sqlBuilder.append(" WHERE ");
            if (prefix != null)
            {
                sqlBuilder.append("UPPER(Context) LIKE UPPER(?) AND (");
            }
            
            for (int j = 0; j < profileIds.size(); j++)
            {
                sqlBuilder.append(j == 0 ? "Profile_Id = ?" : " OR Profile_Id = ?");
            }
            if (prefix != null)
            {
                sqlBuilder.append(")");
            }
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            int i;
            if (prefix != null)
            {
                stmt.setString(1, prefix + "%");
                i = 2;
            }
            else
            {
                i = 1;
            }
            for (String profileId : profileIds)
            {
                stmt.setString(i++, profileId);
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}, {}]", sql, prefix + "%", profileIds);
            }
            else
            {
                getLogger().debug("{}\n[{}]", sql, profileIds);
            }
            rs = stmt.executeQuery();
            if (rs.next())
            {
                return true;
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
        
        return false;
    }
    
    /**
     * Returns true if any context has one of the given profiles as allowed for any connected user
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as allowed for any connected user
     */
    protected boolean _hasAnyConnectedAllowedProfile(Set<String> profileIds, String prefix)
    {
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {  
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
            sqlBuilder.append(_tableAllowedProfilesAnyConnected);
            sqlBuilder.append(" WHERE ");
            if (prefix != null)
            {
                sqlBuilder.append("UPPER(Context) LIKE UPPER(?) AND (");
            }
            
            for (int j = 0; j < profileIds.size(); j++)
            {
                sqlBuilder.append(j == 0 ? "Profile_Id = ?" : " OR Profile_Id = ?");
            }
            if (prefix != null)
            {
                sqlBuilder.append(")");
            }
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            int i;
            if (prefix != null)
            {
                stmt.setString(1, prefix + "%");
                i = 2;
            }
            else
            {
                i = 1;
            }
            for (String profileId : profileIds)
            {
                stmt.setString(i++, profileId);
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}, {}]", sql, prefix + "%", profileIds);
            }
            else
            {
                getLogger().debug("{}\n[{}]", sql, profileIds);
            }
            rs = stmt.executeQuery();
            if (rs.next())
            {
                return true;
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
        
        return false;
    }
    
    /**
     * Returns true if any context has one of the given profiles as denied for anonymous
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as denied for anonymous
     */
    protected boolean _hasAnonymousDeniedProfile(Set<String> profileIds, String prefix)
    {
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
            sqlBuilder.append(_tableDeniedProfilesAnonymous);
            sqlBuilder.append(" WHERE ");
            if (prefix != null)
            {
                sqlBuilder.append("UPPER(Context) LIKE UPPER(?) AND (");
            }
            
            for (int j = 0; j < profileIds.size(); j++)
            {
                sqlBuilder.append(j == 0 ? "Profile_Id = ?" : " OR Profile_Id = ?");
            }
            if (prefix != null)
            {
                sqlBuilder.append(")");
            }
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            int i;
            if (prefix != null)
            {
                stmt.setString(1, prefix + "%");
                i = 2;
            }
            else
            {
                i = 1;
            }
            for (String profileId : profileIds)
            {
                stmt.setString(i++, profileId);
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}, {}]", sql, prefix + "%", profileIds);
            }
            else
            {
                getLogger().debug("{}\n[{}]", sql, profileIds);
            }
            rs = stmt.executeQuery();
            if (rs.next())
            {
                return true;
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
        
        return false;
    }
    
    /**
     * Returns true if any context has one of the given profiles as allowed for anonymous
     * @param profileIds The ids of the profiles
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if any context has one of the given profiles as allowed for anonymous
     */
    protected boolean _hasAnonymousAllowedProfile(Set<String> profileIds, String prefix)
    {
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {  
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ");
            sqlBuilder.append(_tableAllowedProfilesAnonymous);
            sqlBuilder.append(" WHERE ");
            if (prefix != null)
            {
                sqlBuilder.append("UPPER(Context) LIKE UPPER(?) AND (");
            }
            
            for (int j = 0; j < profileIds.size(); j++)
            {
                sqlBuilder.append(j == 0 ? "Profile_Id = ?" : " OR Profile_Id = ?");
            }
            if (prefix != null)
            {
                sqlBuilder.append(")");
            }
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            int i;
            if (prefix != null)
            {
                stmt.setString(1, prefix + "%");
                i = 2;
            }
            else
            {
                i = 1;
            }
            for (String profileId : profileIds)
            {
                stmt.setString(i++, profileId);
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}, {}]", sql, prefix + "%", profileIds);
            }
            else
            {
                getLogger().debug("{}\n[{}]", sql, profileIds);
            }
            rs = stmt.executeQuery();
            if (rs.next())
            {
                return true;
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
        
        return false;
    }
    
    
    /* ------------------------------------------------- */
    /* HAS PERMISSION WITH ANY PROFILE AND ON ANY OBJECT */
    /* ------------------------------------------------- */
    
    @Override
    public boolean hasPermission(UserIdentity user, Set<GroupIdentity> userGroups)
    {
        // 1) Search at least one allowed profile for a context which is not in denied for the user
        if (_hasAllowedProfile(user, null))
        {
            return true;
        }
        
        // 2) Search at least one allowed profile for a context which is not in denied for a group
        for (GroupIdentity group : userGroups)
        {
            if (_hasAllowedProfile(group, null))
            {
                return true;
            }
        }
        
        // 3) Search at least one allowed profile for a context which is not in denied for any connected user
        if (_hasAnyConnectedAllowedProfile(null))
        {
            return true;
        }
        
        // 4) Search at least one allowed profile for a context which is not in denied for anonymous
        if (_hasAnonymousAllowedProfile(null))
        {
            return true;
        }
        
        // 5) Not found, return false
        return false;
    }
    
    /**
     * Returns true if the user has at least one allowed profile on any context object (and has not the denied profile on the same object)
     * @param user The user
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if the user has at least one allowed profile on any context object (and has not the denied profile on the same object)
     */
    protected boolean _hasAllowedProfile(UserIdentity user, String prefix)
    {
        Map<String, Set<String>> profilesByContext = new HashMap<>();
        
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            // First get allowed profiles by context
            StringBuilder sqlBuilder = new StringBuilder("SELECT Context, Profile_Id FROM ");
            sqlBuilder.append(_tableAllowedUsers);
            sqlBuilder.append(" WHERE Login=? AND UserPopulation_Id=?");
            if (prefix != null)
            {
                sqlBuilder.append(" AND UPPER(Context) LIKE UPPER(?)");
            }
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());
            if (prefix != null)
            {
                stmt.setString(3, prefix + "%");
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}, {}, {}]", sql, user.getLogin(), user.getPopulationId(), prefix + "%");
            }
            else
            {
                getLogger().debug("{}\n[{}, {}]", sql, user.getLogin(), user.getPopulationId());
            }
            rs = stmt.executeQuery();
            
            while (rs.next())
            {
                String context = rs.getString(1);
                String profileId = rs.getString(2);
                if (profilesByContext.containsKey(context))
                {
                    profilesByContext.get(context).add(profileId);
                }
                else
                {
                    Set<String> profiles = new HashSet<>();
                    profiles.add(profileId);
                    profilesByContext.put(context, profiles);
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
        
        connection = getSQLConnection();
        try
        {
            // Then remove the denied profiles for the same contexts
            StringBuilder sqlBuilder = new StringBuilder("SELECT Context, Profile_Id FROM ");
            sqlBuilder.append(_tableDeniedUsers);
            sqlBuilder.append(" WHERE Login=? AND UserPopulation_Id=?");
            if (prefix != null)
            {
                sqlBuilder.append(" AND UPPER(Context) LIKE UPPER(?)");
            }
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPopulationId());
            if (prefix != null)
            {
                stmt.setString(3, prefix + "%");
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}, {}, {}]", sql, user.getLogin(), user.getPopulationId(), prefix + "%");
            }
            else
            {
                getLogger().debug("{}\n[{}, {}]", sql, user.getLogin(), user.getPopulationId());
            }
            rs = stmt.executeQuery();
            
            while (rs.next())
            {
                String context = rs.getString(1);
                String profileId = rs.getString(2);
                if (profilesByContext.containsKey(context))
                {
                    Set<String> profiles = profilesByContext.get(context);
                    profiles.remove(profileId);
                    if (profiles.isEmpty())
                    {
                        profilesByContext.remove(context);
                    }
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
        
        return !profilesByContext.isEmpty();
    }
    
    /**
     * Returns true if the group has at least one allowed profile on any context object (and has not the denied profile on the same object)
     * @param group The group
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if the group has at least one allowed profile on any context object (and has not the denied profile on the same object)
     */
    protected boolean _hasAllowedProfile(GroupIdentity group, String prefix)
    {
        Map<String, Set<String>> profilesByContext = new HashMap<>();
        
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            // First get allowed profiles by context
            StringBuilder sqlBuilder = new StringBuilder("SELECT Context, Profile_Id FROM ");
            sqlBuilder.append(_tableAllowedGroups);
            sqlBuilder.append(" WHERE Group_Id=? AND GroupDirectory_Id=?");
            if (prefix != null)
            {
                sqlBuilder.append(" AND UPPER(Context) LIKE UPPER(?)");
            }
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, group.getId());
            stmt.setString(2, group.getDirectoryId());
            if (prefix != null)
            {
                stmt.setString(3, prefix + "%");
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}, {}, {}]", sql, group.getId(), group.getDirectoryId(), prefix + "%");
            }
            else
            {
                getLogger().debug("{}\n[{}, {}]", sql, group.getId(), group.getDirectoryId());
            }
            rs = stmt.executeQuery();
            
            while (rs.next())
            {
                String context = rs.getString(1);
                String profileId = rs.getString(2);
                if (profilesByContext.containsKey(context))
                {
                    profilesByContext.get(context).add(profileId);
                }
                else
                {
                    Set<String> profiles = new HashSet<>();
                    profiles.add(profileId);
                    profilesByContext.put(context, profiles);
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
        
        connection = getSQLConnection();
        try
        {
            // Then remove the denied profiles for the same contexts
            StringBuilder sqlBuilder = new StringBuilder("SELECT Context, Profile_Id FROM ");
            sqlBuilder.append(_tableDeniedGroups);
            sqlBuilder.append(" WHERE Group_Id=? AND GroupDirectory_Id=?");
            if (prefix != null)
            {
                sqlBuilder.append(" AND UPPER(Context) LIKE UPPER(?)");
            }
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, group.getId());
            stmt.setString(2, group.getDirectoryId());
            if (prefix != null)
            {
                stmt.setString(3, prefix + "%");
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}, {}, {}]", sql, group.getId(), group.getDirectoryId(), prefix + "%");
            }
            else
            {
                getLogger().debug("{}\n[{}, {}]", sql, group.getId(), group.getDirectoryId());
            }
            rs = stmt.executeQuery();
            
            while (rs.next())
            {
                String context = rs.getString(1);
                String profileId = rs.getString(2);
                if (profilesByContext.containsKey(context))
                {
                    Set<String> profiles = profilesByContext.get(context);
                    profiles.remove(profileId);
                    if (profiles.isEmpty())
                    {
                        profilesByContext.remove(context);
                    }
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
        
        return !profilesByContext.isEmpty();
    }
    
    /**
     * Returns true if there is at least one allowed profile for any connected user on any context object (and has not the denied profile on the same object)
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if there is at least one allowed profile for any connected user on any context object (and has not the denied profile on the same object)
     */
    protected boolean _hasAnyConnectedAllowedProfile(String prefix)
    {
        Map<String, Set<String>> profilesByContext = new HashMap<>();
        
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            // First get allowed profiles by context
            StringBuilder sqlBuilder = new StringBuilder("SELECT Context, Profile_Id FROM ");
            sqlBuilder.append(_tableAllowedProfilesAnyConnected);
            if (prefix != null)
            {
                sqlBuilder.append(" WHERE UPPER(Context) LIKE UPPER(?)");
            }
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            if (prefix != null)
            {
                stmt.setString(1, prefix + "%");
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}]", sql, prefix + "%");
            }
            else
            {
                getLogger().debug("{}]", sql);
            }
            rs = stmt.executeQuery();
            
            while (rs.next())
            {
                String context = rs.getString(1);
                String profileId = rs.getString(2);
                if (profilesByContext.containsKey(context))
                {
                    profilesByContext.get(context).add(profileId);
                }
                else
                {
                    Set<String> profiles = new HashSet<>();
                    profiles.add(profileId);
                    profilesByContext.put(context, profiles);
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
        
        connection = getSQLConnection();
        try
        {
            // Then remove the denied profiles for the same contexts
            StringBuilder sqlBuilder = new StringBuilder("SELECT Context, Profile_Id FROM ");
            sqlBuilder.append(_tableDeniedProfilesAnyConnected);
            if (prefix != null)
            {
                sqlBuilder.append(" WHERE UPPER(Context) LIKE UPPER(?)");
            }
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            if (prefix != null)
            {
                stmt.setString(1, prefix + "%");
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}]", sql, prefix + "%");
            }
            else
            {
                getLogger().debug("{}]", sql);
            }
            rs = stmt.executeQuery();
            
            while (rs.next())
            {
                String context = rs.getString(1);
                String profileId = rs.getString(2);
                if (profilesByContext.containsKey(context))
                {
                    Set<String> profiles = profilesByContext.get(context);
                    profiles.remove(profileId);
                    if (profiles.isEmpty())
                    {
                        profilesByContext.remove(context);
                    }
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
        
        return !profilesByContext.isEmpty();
    }
    
    /**
     * Returns true if there is at least one allowed profile for anonymous on any context object (and has not the denied profile on the same object)
     * @param prefix If not null, add a 'LIKE prefix%' clause to restrict the search over the contexts begining with the prefix
     * @return true if there is at least one allowed profile for anonymous user on any context object (and has not the denied profile on the same object)
     */
    protected boolean _hasAnonymousAllowedProfile(String prefix)
    {
        Map<String, Set<String>> profilesByContext = new HashMap<>();
        
        Connection connection = getSQLConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try
        {
            // First get allowed profiles by context
            StringBuilder sqlBuilder = new StringBuilder("SELECT Context, Profile_Id FROM ");
            sqlBuilder.append(_tableAllowedProfilesAnonymous);
            if (prefix != null)
            {
                sqlBuilder.append(" WHERE UPPER(Context) LIKE UPPER(?)");
            }
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            if (prefix != null)
            {
                stmt.setString(1, prefix + "%");
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}]", sql, prefix + "%");
            }
            else
            {
                getLogger().debug("{}]", sql);
            }
            rs = stmt.executeQuery();
            
            while (rs.next())
            {
                String context = rs.getString(1);
                String profileId = rs.getString(2);
                if (profilesByContext.containsKey(context))
                {
                    profilesByContext.get(context).add(profileId);
                }
                else
                {
                    Set<String> profiles = new HashSet<>();
                    profiles.add(profileId);
                    profilesByContext.put(context, profiles);
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
        
        connection = getSQLConnection();
        try
        {
            // Then remove the denied profiles for the same contexts
            StringBuilder sqlBuilder = new StringBuilder("SELECT Context, Profile_Id FROM ");
            sqlBuilder.append(_tableDeniedProfilesAnonymous);
            if (prefix != null)
            {
                sqlBuilder.append(" WHERE UPPER(Context) LIKE UPPER(?)");
            }
            
            String sql = sqlBuilder.toString();
            stmt = connection.prepareStatement(sql);
            if (prefix != null)
            {
                stmt.setString(1, prefix + "%");
            }
            
            if (prefix != null)
            {
                getLogger().debug("{}\n[{}]", sql, prefix + "%");
            }
            else
            {
                getLogger().debug("{}]", sql);
            }
            rs = stmt.executeQuery();
            
            while (rs.next())
            {
                String context = rs.getString(1);
                String profileId = rs.getString(2);
                if (profilesByContext.containsKey(context))
                {
                    Set<String> profiles = profilesByContext.get(context);
                    profiles.remove(profileId);
                    if (profiles.isEmpty())
                    {
                        profilesByContext.remove(context);
                    }
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
        
        return !profilesByContext.isEmpty();
    }
    
    /* --------------------------------------- */
    /* ALLOWED PROFILES FOR ANY CONNECTED USER */
    /* --------------------------------------- */
    
    @Override
    public Set<String> getAllowedProfilesForAnyConnectedUser(Object object)
    {
        return _getProfiles(_tableAllowedProfilesAnyConnected, object);
    }
    
    @Override
    public boolean isAnyConnectedUserAllowed(Object object, String profileId)
    {
        return getAllowedProfilesForAnyConnectedUser(object).contains(profileId);
    }
    
    @Override
    public void addAllowedProfilesForAnyConnectedUser(Object object, Set<String> profileIds)
    {
        _addProfiles(_tableAllowedProfilesAnyConnected, object, profileIds);
    }
    
    @Override
    public void removeAllowedProfilesForAnyConnectedUser(Object object, Set<String> profileIds)
    {
        _removeProfiles(_tableAllowedProfilesAnyConnected, object, profileIds);
    }
    
    
    /* -------------------------------------- */
    /* DENIED PROFILES FOR ANY CONNECTED USER */
    /* -------------------------------------- */
    
    @Override
    public Set<String> getDeniedProfilesForAnyConnectedUser(Object object)
    {
        return _getProfiles(_tableDeniedProfilesAnyConnected, object);
    }
    
    @Override
    public boolean isAnyConnectedUserDenied(Object object, String profileId)
    {
        return getDeniedProfilesForAnyConnectedUser(object).contains(profileId);
    }
    
    @Override
    public void addDeniedProfilesForAnyConnectedUser(Object object, Set<String> profileIds)
    {
        _addProfiles(_tableDeniedProfilesAnyConnected, object, profileIds);
    }
    
    @Override
    public void removeDeniedProfilesForAnyConnectedUser(Object object, Set<String> profileIds)
    {
        _removeProfiles(_tableDeniedProfilesAnyConnected, object, profileIds);
    }
    
    
    /* ------------------------------ */
    /* ALLOWED PROFILES FOR ANONYMOUS */
    /* ------------------------------ */
    
    @Override
    public Set<String> getAllowedProfilesForAnonymous(Object object)
    {
        return _getProfiles(_tableAllowedProfilesAnonymous, object);
    }
    
    @Override
    public boolean isAnonymousAllowed(Object object, String profileId)
    {
        return getAllowedProfilesForAnonymous(object).contains(profileId);
    }
    
    @Override
    public void addAllowedProfilesForAnonymous(Object object, Set<String> profileIds)
    {
        _addProfiles(_tableAllowedProfilesAnonymous, object, profileIds);
    }
    
    @Override
    public void removeAllowedProfilesForAnonymous(Object object, Set<String> profileIds)
    {
        _removeProfiles(_tableAllowedProfilesAnonymous, object, profileIds);
    }
    
    
    /* ----------------------------- */
    /* DENIED PROFILES FOR ANONYMOUS */
    /* ----------------------------- */
    
    @Override
    public Set<String> getDeniedProfilesForAnonymous(Object object)
    {
        return _getProfiles(_tableDeniedProfilesAnonymous, object);
    }
    
    @Override
    public boolean isAnonymousDenied(Object object, String profileId)
    {
        return getDeniedProfilesForAnonymous(object).contains(profileId);
    }
    
    @Override
    public void addDeniedProfilesForAnonymous(Object object, Set<String> profileIds)
    {
        _addProfiles(_tableDeniedProfilesAnonymous, object, profileIds);
    }
    
    @Override
    public void removeDeniedProfilesForAnonymous(Object object, Set<String> profileIds)
    {
        _removeProfiles(_tableDeniedProfilesAnonymous, object, profileIds);
    }
    
    
    /* --------------------------- */
    /* MANAGEMENT OF ALLOWED USERS */
    /* --------------------------- */
    
    @Override
    public Map<UserIdentity, Set<String>> getAllowedProfilesForUsers(Object object)
    {
        return _getProfilesForUsers(_tableAllowedUsers, object);
    }
    
    @Override
    public Set<UserIdentity> getAllowedUsers(Object object, String profileId)
    {
        return _getUsers(_tableAllowedUsers, object, profileId);
    }
    
    @Override
    public void addAllowedUsers(Set<UserIdentity> users, Object object, String profileId)
    {
        _addUsers(_tableAllowedUsers, users, object, profileId);
    }
    
    @Override
    public void removeAllowedUsers(Set<UserIdentity> users, Object object, String profileId)
    {
        _removeUsers(_tableAllowedUsers, users, object, profileId);
    }
    
    @Override
    public void removeAllowedUsers(Set<UserIdentity> users, Object object)
    {
        _removeUsers(_tableAllowedUsers, users, object);
    }
    
    
    /* ---------------------------- */
    /* MANAGEMENT OF ALLOWED GROUPS */
    /* ---------------------------- */
    
    @Override
    public Map<GroupIdentity, Set<String>> getAllowedProfilesForGroups(Object object)
    {
        return _getGroups(_tableAllowedGroups, object);
    }
    
    @Override
    public Set<GroupIdentity> getAllowedGroups(Object object, String profileId)
    {
        return _getGroups(_tableAllowedGroups, object, profileId);
    }
    
    @Override
    public void addAllowedGroups(Set<GroupIdentity> groups, Object object, String profileId)
    {
        _addGroups(_tableAllowedGroups, groups, object, profileId);
    }
    
    @Override
    public void removeAllowedGroups(Set<GroupIdentity> groups, Object object, String profileId)
    {
        _removeGroups(_tableAllowedGroups, groups, object, profileId);
    }
    
    @Override
    public void removeAllowedGroups(Set<GroupIdentity> groups, Object object)
    {
        _removeGroups(_tableAllowedGroups, groups, object);
    }
    
    
    /* ---------------------------- */
    /* MANAGEMENT OF DENIED USERS */
    /* ---------------------------- */
    
    @Override
    public Map<UserIdentity, Set<String>> getDeniedProfilesForUsers(Object object)
    {
        return _getProfilesForUsers(_tableDeniedUsers, object);
    }
    
    @Override
    public Set<UserIdentity> getDeniedUsers(Object object, String profileId)
    {
        return _getUsers(_tableDeniedUsers, object, profileId);
    }
    
    @Override
    public void addDeniedUsers(Set<UserIdentity> users, Object object, String profileId)
    {
        _addUsers(_tableDeniedUsers, users, object, profileId);
    }
    
    @Override
    public void removeDeniedUsers(Set<UserIdentity> users, Object object, String profileId)
    {
        _removeUsers(_tableDeniedUsers, users, object, profileId);
    }
    
    @Override
    public void removeDeniedUsers(Set<UserIdentity> users, Object object)
    {
        _removeUsers(_tableDeniedUsers, users, object);
    }
    
    
    /* ----------------------------- */
    /* MANAGEMENT OF DENIED GROUPS */
    /* ----------------------------- */
    
    @Override
    public Map<GroupIdentity, Set<String>> getDeniedProfilesForGroups(Object object)
    {
        return _getGroups(_tableDeniedGroups, object);
    }
    
    @Override
    public Set<GroupIdentity> getDeniedGroups(Object object, String profileId)
    {
        return _getGroups(_tableDeniedGroups, object, profileId);
    }
    
    @Override
    public void addDeniedGroups(Set<GroupIdentity> groups, Object object, String profileId)
    {
        _addGroups(_tableDeniedGroups, groups, object, profileId);
    }
    
    @Override
    public void removeDeniedGroups(Set<GroupIdentity> groups, Object object, String profileId)
    {
        _removeGroups(_tableDeniedGroups, groups, object, profileId);
    }
    
    @Override
    public void removeDeniedGroups(Set<GroupIdentity> groups, Object object)
    {
        _removeGroups(_tableDeniedGroups, groups, object);
    }
    
    
    /* --------------- */
    /* PRIVATE METHODS */
    /* --------------- */
    
    private Set<String> _getProfiles(String tableName, Object object)
    {
        String context = (String) object;
        Set<String> profiles = new HashSet<>();
        
        Connection connection = getSQLConnection();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT DISTINCT Profile_Id, Context " + "FROM " + tableName + " WHERE LOWER(Context) = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, context);

            getLogger().debug("{}\n[{}]", sql, context);

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String profileId = rs.getString(1);
                profiles.add(profileId);
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
    
    private Map<UserIdentity, Set<String>> _getProfilesForUsers(String tableName, Object object)
    {
        String context = (String) object;
        Map<UserIdentity, Set<String>> users = new HashMap<>();

        Connection connection = getSQLConnection();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT DISTINCT Login, UserPopulation_Id, Profile_Id " + "FROM " + tableName + " WHERE LOWER(Context) = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, context);

            getLogger().debug("{}\n[{}]", sql, context);

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String login = rs.getString(1);
                String populationId = rs.getString(2);
                UserIdentity userIdentity = new UserIdentity(login, populationId);
                String profileId = rs.getString(3);
                if (users.containsKey(userIdentity))
                {
                    users.get(userIdentity).add(profileId);
                }
                else
                {
                    Set<String> profiles = new HashSet<>();
                    profiles.add(profileId);
                    users.put(userIdentity, profiles);
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
    
    private void _addProfiles(String tableName, Object object, Set<String> profileIds)
    {
        String context = (String) object;
        for (String profileId : profileIds)
        {
            Connection connection = getSQLConnection();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try
            {
                // Check if already exists
                String sql = "SELECT Profile_Id, Context FROM " + tableName + " WHERE Profile_Id=? AND Context=?";
                stmt = connection.prepareStatement(sql);
                stmt.setString(1, profileId);
                stmt.setString(2, context);

                rs = stmt.executeQuery();
                if (rs.next())
                {
                    getLogger().debug("Profile ID {} is already a 'anyConnectedUsersProfile' on context {}", profileId, context);
                    break;
                }

                ConnectionHelper.cleanup(stmt);

                sql = "INSERT INTO " + tableName + " (Profile_Id, Context) VALUES(?, ?)";

                stmt = connection.prepareStatement(sql);

                stmt.setString(1, profileId);
                stmt.setString(2, context);

                getLogger().debug("{}\n[{}, {}]", sql, profileId, context);

                stmt.executeUpdate();
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
    }
    
    private void _removeProfiles(String tableName, Object object, Set<String> profileIds)
    {
        String context = (String) object;
        for (String profileId : profileIds)
        {
            Connection connection = getSQLConnection();
            PreparedStatement stmt = null;

            try
            {
                String sql = "DELETE FROM " + tableName + " WHERE Profile_Id = ? AND LOWER(Context) = ?";

                stmt = connection.prepareStatement(sql);

                stmt.setString(1, profileId);
                stmt.setString(2, context);

                getLogger().debug("{}\n[{}]", sql, context);

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
    }
    
    private Set<UserIdentity> _getUsers(String tableName, Object object, String profileId)
    {
        String context = (String) object;
        Set<UserIdentity> users = new HashSet<>();

        Connection connection = getSQLConnection();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT DISTINCT Login, UserPopulation_Id " + "FROM " + tableName + " WHERE Profile_Id = ? AND LOWER(Context) = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, profileId);
            stmt.setString(2, context);

            getLogger().debug("{}\n[{}, {}]", sql, profileId, context);

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String login = rs.getString(1);
                String populationId = rs.getString(2);
                users.add(new UserIdentity(login, populationId));
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
    
    private void _addUsers(String tableName, Set<UserIdentity> users, Object object, String profileId)
    {
        String context = (String) object;
        for (UserIdentity userIdentity : users)
        {
            Connection connection = getSQLConnection();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try
            {
                // Check if already exists
                String sql = "SELECT Profile_Id FROM " + tableName + " WHERE Profile_Id=? and Login=? AND UserPopulation_Id=? and Context=?";
                stmt = connection.prepareStatement(sql);
                stmt.setString(1, profileId);
                stmt.setString(2, userIdentity.getLogin());
                stmt.setString(3, userIdentity.getPopulationId());
                stmt.setString(4, context);

                rs = stmt.executeQuery();
                if (rs.next())
                {
                    getLogger().debug("Login {} has already profile {} on context {}", userIdentity, profileId, context);
                    break;
                }

                ConnectionHelper.cleanup(stmt);

                sql = "INSERT INTO " + tableName + " (Profile_Id, Login, UserPopulation_Id, Context) VALUES(?, ?, ?, ?)";

                stmt = connection.prepareStatement(sql);

                stmt.setString(1, profileId);
                stmt.setString(2, userIdentity.getLogin());
                stmt.setString(3, userIdentity.getPopulationId());
                stmt.setString(4, context);

                getLogger().debug("{}\n[{}, {}, {}, {}]", sql, profileId, userIdentity.getLogin(), userIdentity.getPopulationId(), context);

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
    }
    
    private void _removeUsers(String tableName, Set<UserIdentity> users, Object object, String profileId)
    {
        String context = (String) object;
        for (UserIdentity userIdentity : users)
        {
            Connection connection = getSQLConnection();
            PreparedStatement stmt = null;
    
            try
            {
                String sql = "DELETE FROM " + tableName + " WHERE Login = ? AND UserPopulation_Id = ? AND Profile_Id = ? AND LOWER(Context) = ? ";
    
                stmt = connection.prepareStatement(sql);
    
                stmt.setString(1, userIdentity.getLogin());
                stmt.setString(2, userIdentity.getPopulationId());
                stmt.setString(3, profileId);
                stmt.setString(4, context);
    
                getLogger().debug("{}\n[{}, {}, {}, {}]", sql, userIdentity.getLogin(), userIdentity.getPopulationId(), profileId, context);
    
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
    }
    
    private void _removeUsers(String tableName, Set<UserIdentity> users, Object object)
    {
        String context = (String) object;
        for (UserIdentity userIdentity : users)
        {
            Connection connection = getSQLConnection();
            PreparedStatement stmt = null;

            try
            {
                String sql = "DELETE FROM " + tableName + " WHERE Login = ? AND UserPopulation_Id = ?";
                if (context != null)
                {
                    sql += " AND LOWER(Context) = ?";
                }

                stmt = connection.prepareStatement(sql);

                stmt.setString(1, userIdentity.getLogin());
                stmt.setString(2, userIdentity.getPopulationId());
                if (context != null)
                {
                    stmt.setString(3, context);
                }

                getLogger().debug("{}\n[{}, {}, {}]", sql, userIdentity.getLogin(), userIdentity.getPopulationId(), context);

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
    }

    private Map<GroupIdentity, Set<String>> _getGroups(String tableName, Object object)
    {
        String context = (String) object;
        Map<GroupIdentity, Set<String>> groups = new HashMap<>();

        Connection connection = getSQLConnection();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT DISTINCT Group_Id, GroupDirectory_Id, Profile_Id " + "FROM " + tableName + " WHERE LOWER(Context) = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, context);

            getLogger().debug("{}\n[{}]", sql, context);

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String groupId = rs.getString(1);
                String directoryId = rs.getString(2);
                GroupIdentity groupIdentity = new GroupIdentity(groupId, directoryId);
                String profileId = rs.getString(3);
                if (groups.containsKey(groupIdentity))
                {
                    groups.get(groupIdentity).add(profileId);
                }
                else
                {
                    Set<String> profiles = new HashSet<>();
                    profiles.add(profileId);
                    groups.put(groupIdentity, profiles);
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

    private Set<GroupIdentity> _getGroups(String tableName, Object object, String profileId)
    {
        String context = (String) object;
        Set<GroupIdentity> groups = new HashSet<>();

        Connection connection = getSQLConnection();

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT DISTINCT Group_Id, GroupDirectory_Id " + "FROM " + tableName + " WHERE Profile_Id = ? AND LOWER(Context) = ? ";

            stmt = connection.prepareStatement(sql);

            stmt.setString(1, profileId);
            stmt.setString(2, context);

            getLogger().debug("{}\n[{}, {}]", sql, profileId, context);

            rs = stmt.executeQuery();

            while (rs.next())
            {
                String groupId = rs.getString(1);
                String groupDirectoryId = rs.getString(2);
                groups.add(new GroupIdentity(groupId, groupDirectoryId));
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
    
    private void _addGroups(String tableName, Set<GroupIdentity> groups, Object object, String profileId)
    {
        String context = (String) object;
        for (GroupIdentity groupIdentity : groups)
        {
            Connection connection = getSQLConnection();
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try
            {
                // Check if already exists
                String sql = "SELECT Profile_Id FROM " + tableName + " WHERE Profile_Id=? and Group_Id=? and GroupDirectory_Id=? and Context=?";
                stmt = connection.prepareStatement(sql);
                stmt.setString(1, profileId);
                stmt.setString(2, groupIdentity.getId());
                stmt.setString(3, groupIdentity.getDirectoryId());
                stmt.setString(4, context);

                rs = stmt.executeQuery();
                if (rs.next())
                {
                    getLogger().debug("Group of id {} has already profile {} on context {}", groupIdentity, profileId, context);
                    break;
                }

                ConnectionHelper.cleanup(stmt);

                sql = "INSERT INTO " + tableName + " (Profile_Id, Group_Id, GroupDirectory_Id, Context) VALUES(?, ?, ?, ?)";

                stmt = connection.prepareStatement(sql);

                stmt.setString(1, profileId);
                stmt.setString(2, groupIdentity.getId());
                stmt.setString(3, groupIdentity.getDirectoryId());
                stmt.setString(4, context);

                getLogger().debug("{}\n[{}, {}, {}, {}]", sql, profileId, groupIdentity.getId(), groupIdentity.getDirectoryId(), context);

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
    }
    
    private void _removeGroups(String tableName, Set<GroupIdentity> groups, Object object, String profileId)
    {
        String context = (String) object;
        for (GroupIdentity groupIdentity : groups)
        {
            Connection connection = getSQLConnection();
            PreparedStatement stmt = null;

            try
            {
                String sql = "DELETE FROM " + tableName + " WHERE Group_Id = ? AND GroupDirectory_Id = ? AND Profile_Id = ? AND LOWER(Context) = ?";

                stmt = connection.prepareStatement(sql);

                stmt.setString(1, groupIdentity.getId());
                stmt.setString(2, groupIdentity.getDirectoryId());
                stmt.setString(3, profileId);
                stmt.setString(4, context);

                getLogger().debug("{}\n[{}, {}, {}, {}]", sql, groupIdentity.getId(), groupIdentity.getDirectoryId(), profileId, context);

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
    }
    
    private void _removeGroups(String tableName, Set<GroupIdentity> groups, Object object)
    {
        String context = (String) object;
        for (GroupIdentity groupIdentity : groups)
        {
            Connection connection = getSQLConnection();
            PreparedStatement stmt = null;

            try
            {
                String sql = "DELETE FROM " + tableName + " WHERE Group_Id = ? AND groupDirectory_Id = ?";

                if (context != null)
                {
                    sql += " AND LOWER(Context) = ?";
                }

                stmt = connection.prepareStatement(sql);

                stmt.setString(1, groupIdentity.getId());
                stmt.setString(2, groupIdentity.getDirectoryId());
                if (context != null)
                {
                    stmt.setString(3, context);
                }

                getLogger().debug("{}\n[{}, {}, {}]", sql, groupIdentity.getId(), groupIdentity.getDirectoryId(), context);

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
    }
    
    
    /* ------ */
    /* REMOVE */
    /* ------ */
    
    @Override
    public void removeProfile(String profileId)
    {
        Connection connection = null;
        PreparedStatement statement = null;
        PreparedStatement statement2 = null;
        PreparedStatement statement3 = null;
        PreparedStatement statement4 = null;
        PreparedStatement statement5 = null;
        PreparedStatement statement6 = null;
        PreparedStatement statement7 = null;
        PreparedStatement statement8 = null;

        try
        {
            connection = getSQLConnection();

            statement = connection.prepareStatement("DELETE FROM " + _tableAllowedUsers + " WHERE Profile_Id = ?");
            statement.setString(1, profileId);
            statement.executeUpdate();

            statement2 = connection.prepareStatement("DELETE FROM " + _tableAllowedGroups + " WHERE Profile_Id = ?");
            statement2.setString(1, profileId);
            statement2.executeUpdate();
            
            statement3 = connection.prepareStatement("DELETE FROM " + _tableDeniedUsers + " WHERE Profile_Id = ?");
            statement3.setString(1, profileId);
            statement3.executeUpdate();
            
            statement4 = connection.prepareStatement("DELETE FROM " + _tableDeniedGroups + " WHERE Profile_Id = ?");
            statement4.setString(1, profileId);
            statement4.executeUpdate();
            
            statement5 = connection.prepareStatement("DELETE FROM " + _tableAllowedProfilesAnyConnected + " WHERE Profile_Id = ?");
            statement5.setString(1, profileId);
            statement5.executeUpdate();
            
            statement6 = connection.prepareStatement("DELETE FROM " + _tableDeniedProfilesAnyConnected + " WHERE Profile_Id = ?");
            statement6.setString(1, profileId);
            statement6.executeUpdate();
            
            statement7 = connection.prepareStatement("DELETE FROM " + _tableAllowedProfilesAnonymous + " WHERE Profile_Id = ?");
            statement7.setString(1, profileId);
            statement7.executeUpdate();
            
            statement8 = connection.prepareStatement("DELETE FROM " + _tableDeniedProfilesAnonymous + " WHERE Profile_Id = ?");
            statement8.setString(1, profileId);
            statement8.executeUpdate();
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
    public void removeUser(UserIdentity user)
    {
        Connection connection = null;
        PreparedStatement statement = null;
        PreparedStatement statement2 = null;

        try
        {
            connection = getSQLConnection();

            statement = connection.prepareStatement("DELETE FROM " + _tableAllowedUsers + " WHERE Login = ? AND UserPopulation_Id = ?");
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getPopulationId());
            statement.executeUpdate();

            statement2 = connection.prepareStatement("DELETE FROM " + _tableDeniedUsers + " WHERE Login = ? AND UserPopulation_Id = ?");
            statement2.setString(1, user.getLogin());
            statement2.setString(2, user.getPopulationId());
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
    }
    
    @Override
    public void removeGroup(GroupIdentity group)
    {
        Connection connection = null;
        PreparedStatement statement = null;
        PreparedStatement statement2 = null;

        try
        {
            connection = getSQLConnection();

            statement = connection.prepareStatement("DELETE FROM " + _tableAllowedGroups + " WHERE Group_Id = ? AND GroupDirectory_Id = ?");
            statement.setString(1, group.getId());
            statement.setString(2, group.getDirectoryId());
            statement.executeUpdate();

            statement2 = connection.prepareStatement("DELETE FROM " + _tableDeniedGroups + " WHERE Group_Id = ? AND GroupDirectory_Id = ?");
            statement2.setString(1, group.getId());
            statement2.setString(2, group.getDirectoryId());
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
    }
    
    /* ------------------------------ */
    /* SUPPORT OF OBJECT AND PRIORITY */
    /* ------------------------------ */

    @Override
    public boolean isSupported(Object object)
    {
        return object instanceof String;
    }

    @Override
    public int getPriority()
    {
        return ProfileAssignmentStorage.MIN_PRIORITY;
    }
}
