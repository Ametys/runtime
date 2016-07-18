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
package org.ametys.plugins.core.impl.group.directory.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.group.Group;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.group.GroupListener;
import org.ametys.core.group.InvalidModificationException;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.group.directory.GroupDirectoryModel;
import org.ametys.core.group.directory.ModifiableGroupDirectory;
import org.ametys.core.user.UserIdentity;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Standard implementation of {@link GroupDirectory} from the core database.
 */
public class JdbcGroupDirectory extends AbstractLogEnabled implements ModifiableGroupDirectory
{
    /** Name of the parameter holding the datasource id */
    private static final String __DATASOURCE_PARAM_NAME = "runtime.groups.jdbc.datasource";
    /** Name of the parameter holding the SQL table name for storing the groups */
    private static final String __GROUPS_LIST_TABLE_PARAM_NAME = "runtime.groups.jdbc.list.table";
    /** Name of the parameter holding the SQL table name for storing the composition (i.e. users) of the groups */
    private static final String __GROUPS_COMPOSITION_TABLE_PARAM_NAME = "runtime.groups.jdbc.composition.table";
    
    private static final String __GROUPS_LIST_COLUMN_ID = "Id";
    private static final String __GROUPS_LIST_COLUMN_LABEL = "Label";
    private static final String __GROUPS_COMPOSITION_COLUMN_GROUPID = "Group_Id";
    private static final String __GROUPS_COMPOSITION_COLUMN_LOGIN = "Login";
    private static final String __GROUPS_COMPOSITION_COLUMN_POPULATIONID = "UserPopulation_Id";
    
    /** Group listeners */
    protected List<GroupListener> _listeners = new ArrayList<>();
    
    /** The identifier of data source */
    protected String _dataSourceId;
    /** The name of the SQL table storing the groups */
    protected String _groupsListTableName;
    /** The name of the SQL table storing the composition (i.e. users) of the groups*/
    protected String _groupsCompositionTableName;
    
    /** The id */
    protected String _id;
    /** The label */
    protected I18nizableText _label;
    /** The id of the {@link GroupDirectoryModel} */
    private String _groupDirectoryModelId;
    /** The map of the values of the parameters */
    private Map<String, Object> _paramValues;
    
    @Override
    public String getId()
    {
        return _id;
    }
    
    @Override
    public I18nizableText getLabel()
    {
        return _label;
    }
    
    @Override
    public void setId(String id)
    {
        _id = id;
    }
    
    @Override
    public void setLabel(I18nizableText label)
    {
        _label = label;
    }
    
    @Override
    public String getGroupDirectoryModelId ()
    {
        return _groupDirectoryModelId;
    }
    
    @Override
    public Map<String, Object> getParameterValues()
    {
        return _paramValues;
    }
    
    @Override
    public void init(String groupDirectoryModelId, Map<String, Object> paramValues)
    {
        _groupDirectoryModelId = groupDirectoryModelId;
        _paramValues = paramValues;
        
        _groupsListTableName = (String) paramValues.get(__GROUPS_LIST_TABLE_PARAM_NAME);
        _groupsCompositionTableName = (String) paramValues.get(__GROUPS_COMPOSITION_TABLE_PARAM_NAME);
        _dataSourceId = (String) paramValues.get(__DATASOURCE_PARAM_NAME);
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
    public Group getGroup(String groupID)
    {
        Group group = null;

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            connection = getSQLConnection();

            String sql = "SELECT " + __GROUPS_LIST_COLUMN_LABEL + " FROM " + _groupsListTableName + " WHERE " + __GROUPS_LIST_COLUMN_ID + " =  ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(groupID));

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql);
            }
            rs = stmt.executeQuery();

            // Iterate over all the groups
            if (rs.next())
            {
                String label = rs.getString(__GROUPS_LIST_COLUMN_LABEL);
                group = new Group(new GroupIdentity(groupID, getId()), label, this);

                _fillGroup(group, connection);
            }
        }
        catch (NumberFormatException e)
        {
            getLogger().error("Group ID must be an integer.", e);
            return null;
        }
        catch (SQLException e)
        {
            getLogger().error("Error communication with database", e);
            return null;
        }
        finally
        {
            ConnectionHelper.cleanup(rs);       
            ConnectionHelper.cleanup(stmt);       
            ConnectionHelper.cleanup(connection);       
        }

        // Return the found group or null
        return group;
    }

    @Override
    public Set<Group> getGroups()
    {
        Set<Group> groups = new LinkedHashSet<>();

        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try
        {
            connection = getSQLConnection();

            stmt = connection.createStatement();
            String sql = _createGetGroupsClause();

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql);
            }
            rs = stmt.executeQuery(sql);

            // Iterate over all the groups
            while (rs.next())
            {
                String groupID = rs.getString(__GROUPS_LIST_COLUMN_ID);
                String label = rs.getString(__GROUPS_LIST_COLUMN_LABEL);
                Group group = new Group(new GroupIdentity(groupID, getId()), label, this);

                _fillGroup(group, connection);

                // Add current group
                groups.add(group);
            }
        }
        catch (SQLException e)
        {
            getLogger().error("Error communication with database", e);
            return Collections.emptySet();
        }
        finally
        {
            ConnectionHelper.cleanup(rs);       
            ConnectionHelper.cleanup(stmt);       
            ConnectionHelper.cleanup(connection);       
        }

        // Retourner l'ensemble des groupes
        return groups;
    }
    
    /**
     * Get the sql clause that gets all groups
     * @return A non null sql clause (e.g. "select ... from ... where ...")
     */
    protected String _createGetGroupsClause()
    {
        return "SELECT " + __GROUPS_LIST_COLUMN_ID + ", " + __GROUPS_LIST_COLUMN_LABEL + " FROM " + _groupsListTableName + " ORDER BY " + __GROUPS_LIST_COLUMN_LABEL;
    }
    
    /**
     * Fill users set in a group.
     * 
     * @param group The group to fill.
     * @param connection The SQL connection.
     * @throws SQLException If a problem occurs.
     */
    protected void _fillGroup(Group group, Connection connection) throws SQLException
    {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            String sql = "SELECT " + __GROUPS_COMPOSITION_COLUMN_LOGIN + ", " + __GROUPS_COMPOSITION_COLUMN_POPULATIONID + " FROM " + _groupsCompositionTableName + " WHERE " + __GROUPS_COMPOSITION_COLUMN_GROUPID + " = ?";

            stmt = connection.prepareStatement(sql);

            stmt.setInt(1, Integer.parseInt(group.getIdentity().getId()));

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql);
            }
            rs = stmt.executeQuery();

            // Iterate over all the users from current group 
            while (rs.next())
            {
                UserIdentity identity = new UserIdentity(rs.getString(__GROUPS_COMPOSITION_COLUMN_LOGIN), rs.getString(__GROUPS_COMPOSITION_COLUMN_POPULATIONID));
                group.addUser(identity);
            }
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
        }
    }

    @Override
    public Set<String> getUserGroups(String login, String populationId)
    {
        Set<String> groups = new HashSet<>();
        if (login == null)
        {
            return groups;
        }
        
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            connection = getSQLConnection();

            String sql = "SELECT " + __GROUPS_COMPOSITION_COLUMN_GROUPID + " FROM " + _groupsCompositionTableName + " WHERE " + __GROUPS_COMPOSITION_COLUMN_LOGIN + " = ? AND " + __GROUPS_COMPOSITION_COLUMN_POPULATIONID + " = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, login);
            stmt.setString(2, populationId);

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql);
            }
            rs = stmt.executeQuery();

            // Iterate over all the groups
            while (rs.next())
            {
                String groupID = rs.getString(__GROUPS_COMPOSITION_COLUMN_GROUPID);

                // Add the current group
                groups.add(groupID);
            }
        }
        catch (SQLException e)
        {
            getLogger().error("Error communication with database", e);
            return Collections.emptySet();
        }
        finally
        {
            ConnectionHelper.cleanup(rs);       
            ConnectionHelper.cleanup(stmt);       
            ConnectionHelper.cleanup(connection);       
        }

        // Return the groups, potentially empty
        return groups;
    }

    @Override
    public List<Map<String, Object>> groups2JSON(int count, int offset, Map parameters)
    {
        List<Map<String, Object>> groups = new ArrayList<>();
        
        String pattern = (String) parameters.get("pattern");

        Iterator iterator = getGroups().iterator();

        //int totalCount = 0;
        int currentOffset = offset;

        while (currentOffset > 0 && iterator.hasNext())
        {
            Group group = (Group) iterator.next();
            if (StringUtils.isEmpty(pattern) || group.getLabel().toLowerCase().indexOf(pattern.toLowerCase()) != -1)
            {
                currentOffset--;
                //totalCount++;
            }
        }

        int currentCount = count;
        while ((count == -1 || currentCount > 0) && iterator.hasNext())
        {
            Group group = (Group) iterator.next();

            if (StringUtils.isEmpty(pattern) || group.getLabel().toLowerCase().indexOf(pattern.toLowerCase()) != -1)
            {
                groups.add(_group2JSON(group, true));
                currentCount--;
                //totalCount++;
            }
        }

        /*while (iterator.hasNext())
        {
            Group group = (Group) iterator.next();
            
            if (StringUtils.isEmpty(pattern) || group.getLabel().toLowerCase().indexOf(pattern.toLowerCase()) != -1)
            {
                totalCount++;
            }
        }*/
        
        // TODO return totalCount
        return groups;
    }

    @Override
    public Map<String, Object> group2JSON(String id)
    {
        Group group = getGroup(id);
        return _group2JSON(group, false);
    }
    
    /**
     * Get group as JSON object
     * @param group the group
     * @param users true to get users' group
     * @return the group as JSON object
     */
    protected Map<String, Object> _group2JSON (Group group, boolean users)
    {
        Map<String, Object> group2json = new HashMap<>();
        group2json.put("id", group.getIdentity().getId());
        group2json.put("groupDirectory", group.getIdentity().getDirectoryId());
        group2json.put("groupDirectoryLabel", group.getGroupDirectory().getLabel());
        group2json.put("label", group.getLabel());
        if (users)
        {
            group2json.put("users", group.getUsers());
        }
        return group2json;
    }

    @Override
    public Group add(String name) throws InvalidModificationException
    {
        Connection connection = null; 
        PreparedStatement statement = null;
        ResultSet rs = null;

        String id = null;

        try
        {
            connection = getSQLConnection();
            String dbType = ConnectionHelper.getDatabaseType(connection);
            
            if (ConnectionHelper.DATABASE_ORACLE.equals(dbType))
            {
                statement = connection.prepareStatement("SELECT seq_groups.nextval FROM dual");
                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getString(1);
                }
                ConnectionHelper.cleanup(rs);
                ConnectionHelper.cleanup(statement);

                statement = connection.prepareStatement("INSERT INTO " + _groupsListTableName + " (Id, Label) VALUES(?, ?)");
                statement.setString(1, id);
                statement.setString(2, name);
            }
            else
            {
                statement = connection.prepareStatement("INSERT INTO " + _groupsListTableName + " (" + __GROUPS_LIST_COLUMN_LABEL + ") VALUES (?)");
                statement.setString(1, name);
            }

            statement.executeUpdate();

            ConnectionHelper.cleanup(statement);

            //FIXME Write query working with all database
            if (ConnectionHelper.DATABASE_MYSQL.equals(dbType))
            {
                statement = connection.prepareStatement("SELECT " + __GROUPS_LIST_COLUMN_ID + " FROM " + _groupsListTableName + " WHERE " + __GROUPS_LIST_COLUMN_ID + " = last_insert_id()");    
                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getString(__GROUPS_LIST_COLUMN_ID);
                }
                else
                {
                    if (connection.getAutoCommit())
                    {
                        throw new InvalidModificationException("Cannot retrieve inserted group. Group was created but listeners not called : base may be inconsistant");
                    }
                    else
                    {
                        connection.rollback();
                        throw new InvalidModificationException("Cannot retrieve inserted group. Rolling back");
                    }
                }
            }
            else if (ConnectionHelper.DATABASE_DERBY.equals(dbType))
            {
                statement = connection.prepareStatement("VALUES IDENTITY_VAL_LOCAL ()");
                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getString(1);
                }
            }
            else if (ConnectionHelper.DATABASE_HSQLDB.equals(dbType))
            {
                statement = connection.prepareStatement("CALL IDENTITY ()");
                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getString(1);
                }
            }
            else if (ConnectionHelper.DATABASE_POSTGRES.equals(dbType))
            {
                statement = connection.prepareStatement("SELECT currval('groups_id_seq')");
                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getString(1);
                }
            }

            if (id != null)
            {
                for (GroupListener listener : _listeners)
                {
                    listener.groupAdded(new GroupIdentity(id, getId()));
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

        return new Group(new GroupIdentity(id, getId()), name, this);
    }

    @Override
    public void update(Group userGroup) throws InvalidModificationException
    {
        Connection connection = null;
        PreparedStatement statement = null;

        try
        {
            connection = getSQLConnection();

            // Start transaction.
            connection.setAutoCommit(false);

            statement = connection.prepareStatement("UPDATE " + _groupsListTableName + " SET " + __GROUPS_LIST_COLUMN_LABEL + "=? WHERE " + __GROUPS_LIST_COLUMN_ID + " = ?");
            statement.setString(1, userGroup.getLabel());
            statement.setInt(2, Integer.parseInt(userGroup.getIdentity().getId()));

            if (statement.executeUpdate() == 0)
            {
                throw new InvalidModificationException("No group with id '" + userGroup.getIdentity().getId() + "' may be removed");
            }
            ConnectionHelper.cleanup(statement);

            statement = connection.prepareStatement("DELETE FROM " + _groupsCompositionTableName + " WHERE " + __GROUPS_COMPOSITION_COLUMN_GROUPID + " = ?");
            statement.setInt(1, Integer.parseInt(userGroup.getIdentity().getId()));

            statement.executeUpdate();
            ConnectionHelper.cleanup(statement);

            if (!userGroup.getUsers().isEmpty())
            {
                // Tests if the connection supports batch updates.
                boolean supportsBatch = connection.getMetaData().supportsBatchUpdates();

                statement = connection.prepareStatement("INSERT INTO " + _groupsCompositionTableName + " (" + __GROUPS_COMPOSITION_COLUMN_GROUPID + ", " + __GROUPS_COMPOSITION_COLUMN_LOGIN + ", " + __GROUPS_COMPOSITION_COLUMN_POPULATIONID + ") VALUES (?, ?, ?)");
                
                for (UserIdentity identity : userGroup.getUsers())
                {
                    String login = identity.getLogin();
                    String populationId = identity.getPopulationId();
                    statement.setInt(1, Integer.parseInt(userGroup.getIdentity().getId()));
                    statement.setString(2, login);
                    statement.setString(3, populationId);
                    
                    // If batch updates are supported, add to the batch, else execute directly.
                    if (supportsBatch)
                    {
                        statement.addBatch();
                    }
                    else
                    {
                        statement.executeUpdate();
                    }
                }
                
                // If the insert queries were queued in a batch, execute it.
                if (supportsBatch)
                {
                    statement.executeBatch();
                }
            }

            ConnectionHelper.cleanup(statement);

            // Commit transaction.
            connection.commit();

            for (GroupListener listener : _listeners)
            {
                listener.groupUpdated(userGroup.getIdentity());
            }
        }
        catch (NumberFormatException ex)
        {
            throw new InvalidModificationException("No group with id '" + userGroup.getIdentity().getId() + "' may be removed", ex);
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            ConnectionHelper.cleanup(statement);
            ConnectionHelper.cleanup(connection);
        }
    }

    @Override
    public void remove(String groupID) throws InvalidModificationException
    {
        Connection connection = null;
        PreparedStatement statement = null;

        try
        {
            connection = getSQLConnection();

            statement = connection.prepareStatement("DELETE FROM " + _groupsListTableName + " WHERE " + __GROUPS_LIST_COLUMN_ID + " = ?");
            statement.setInt(1, Integer.parseInt(groupID));

            if (statement.executeUpdate() == 0)
            {
                throw new InvalidModificationException("No group with id '" + groupID + "' may be removed");
            }
            ConnectionHelper.cleanup(statement);       

            statement = connection.prepareStatement("DELETE FROM " + _groupsCompositionTableName + " WHERE " + __GROUPS_COMPOSITION_COLUMN_GROUPID + " = ?");
            statement.setInt(1, Integer.parseInt(groupID));

            statement.executeUpdate();

            for (GroupListener listener : _listeners)
            {
                listener.groupRemoved(new GroupIdentity(groupID, getId()));
            }
        }
        catch (NumberFormatException ex)
        {
            throw new InvalidModificationException("No group with id '" + groupID + "' may be removed, the ID must be a number.", ex);
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            ConnectionHelper.cleanup(statement);       
            ConnectionHelper.cleanup(connection);       
        }
    }

    @Override
    public void registerListener(GroupListener listener)
    {
        _listeners.add(listener);
    }

    @Override
    public void removeListener(GroupListener listener)
    {
        _listeners.remove(listener);
    }

    @Override
    public List getListeners()
    {
        return _listeners;
    }

}
