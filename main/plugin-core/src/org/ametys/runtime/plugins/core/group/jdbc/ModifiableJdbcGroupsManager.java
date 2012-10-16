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
package org.ametys.runtime.plugins.core.group.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.datasource.ConnectionHelper;
import org.ametys.runtime.datasource.ConnectionHelper.DatabaseType;
import org.ametys.runtime.group.Group;
import org.ametys.runtime.group.GroupListener;
import org.ametys.runtime.group.InvalidModificationException;
import org.ametys.runtime.group.ModifiableGroupsManager;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.UserListener;
import org.ametys.runtime.user.UsersManager;


/**
 * Standard implementation of the group manager from the core database.
 */
public class ModifiableJdbcGroupsManager extends AbstractLogEnabled implements ModifiableGroupsManager, Configurable, UserListener, Initializable, Serviceable, Component
{
    /** The users manager */
    protected UsersManager _users;
    
    /** Group listeners */
    protected List<GroupListener> _listeners = new ArrayList<GroupListener>();
    
    /** The name of the jdbc pool to use */
    protected String _poolName;
    /** The name of the jdbc table containing the list of groups */
    protected String _groupsListTable;
    /** The name of the jdbc column in <code>_groupsListTable</code> containing the unique identifier of a group */
    protected String _groupsListColId;
    /** The name of the jdbc column in <code>_groupsListTable</code> containing the label of a group */
    protected String _groupsListColLabel;
    /** The name of the jdbc table containing the association user and groups */
    protected String _groupsCompositionTable;
    /** The name of the jdbc column in <code>_groupsCompositionTable</code> containing the identifier of the group */
    protected String _groupsCompositionColGroup;
    /** The name of the jdbc column in <code>_groupsCompositionTable</code> containing the identifier of the user */
    protected String _groupsCompositionColUser;
    
    public List<GroupListener> getListeners()
    {
        return _listeners;
    }

    public void configure(Configuration configuration) throws ConfigurationException
    {
        _poolName = configuration.getChild("pool").getValue();
        
        Configuration listConfiguration = configuration.getChild("list");
        _groupsListTable = listConfiguration.getChild("table").getValue("Groups");
        _groupsListColId = listConfiguration.getChild("id").getValue("Id");
        _groupsListColLabel = listConfiguration.getChild("label").getValue("Label");
        
        Configuration compositionConfiguration = configuration.getChild("composition");
        _groupsCompositionTable = compositionConfiguration.getChild("table").getValue("Groups_Users");
        _groupsCompositionColGroup = compositionConfiguration.getChild("group").getValue("Group_Id");
        _groupsCompositionColUser = compositionConfiguration.getChild("user").getValue("Login");
    }

    public void registerListener(GroupListener listener)
    {
        _listeners.add(listener);
    }

    public void removeListener(GroupListener listener)
    {
        _listeners.remove(listener);
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _users = (UsersManager) manager.lookup(UsersManager.ROLE);
    }

    public void initialize() throws Exception
    {
        PluginsManager pm = PluginsManager.getInstance();
        if (pm != null)
        {
            if (_users instanceof ModifiableUsersManager)
            {
                ModifiableUsersManager mbu = (ModifiableUsersManager) _users;
                mbu.registerListener(this);
            }
        }
    }

    public Group getGroup(String groupID)
    {
        Group group = null;

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            
            String sql = "SELECT " + _groupsListColLabel + " FROM " + _groupsListTable + " WHERE " + _groupsListColId + " =  ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(groupID));
            
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql);
            }
            rs = stmt.executeQuery();

            // Parcourir tous les groupes
            if (rs.next())
            {
                String label = rs.getString(_groupsListColLabel);
                group = new Group(groupID, label);

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

        // Retourner le groupe trouvé ou null
        return group;
    }
    
    /**
     * Get the sql clause that gets all groups
     * @return A non null sql clause (e.g. "select ... from ... where ...")
     */
    protected String _createGetGroupsClause()
    {
        return "SELECT " + _groupsListColId + ", " + _groupsListColLabel + " FROM " + _groupsListTable;
    }

    public Set<Group> getGroups()
    {
        Set<Group> groups = new HashSet<Group>();

        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;

        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            
            stmt = connection.createStatement();
            String sql = _createGetGroupsClause();

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql);
            }
            rs = stmt.executeQuery(sql);

            // Parcourir tous les groupes
            while (rs.next())
            {
                String groupID = rs.getString(_groupsListColId);
                String label = rs.getString(_groupsListColLabel);
                Group group = new Group(groupID, label);

                _fillGroup(group, connection);

                // Ajouter le groupe courant
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

    public Set<String> getUserGroups(String login)
    {
        Set<String> groups = new HashSet<String>();

        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            
            String sql = "SELECT " + _groupsCompositionColGroup + " FROM " + _groupsCompositionTable + " WHERE " + _groupsCompositionColUser + " = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, login);

            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql);
            }
            rs = stmt.executeQuery();

            // Parcourir tous les groupes
            while (rs.next())
            {
                String groupID = rs.getString(_groupsCompositionColGroup);

                // Ajouter le groupe courant
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

        // Retourner l'ensemble des groupes, éventuellement vide
        return groups;
    }

    /**
     * Removes all groups associated with a user
     * 
     * @param login the login of the user
     */
    public void removeUserGroups(String login)
    {
        Connection connection = null;
        PreparedStatement stmt = null;

        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            
            String sql = "DELETE FROM " + _groupsCompositionTable + " WHERE Login = ?";

            stmt = connection.prepareStatement(sql);
            stmt.setString(1, login);

            // Logger la requête
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(sql + "\n[" + login + "]");
            }

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
        removeUserGroups(login);
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
            String sql = "SELECT " + _groupsCompositionColUser + " FROM " + _groupsCompositionTable + " WHERE " + _groupsCompositionColGroup + " = ?";
            
            stmt = connection.prepareStatement(sql);
            
            stmt.setInt(1, Integer.parseInt(group.getId()));
            
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(sql);
            }
            rs = stmt.executeQuery();

            // Parcourir tous les utilisateurs du groupe courant
            while (rs.next())
            {
                group.addUser(rs.getString(_groupsCompositionColUser));
            }
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt);
        }
    }

    public Group add(String name) throws InvalidModificationException
    {
        Connection connection = null; 
        PreparedStatement statement = null;
        ResultSet rs = null;
        
        String id = null;

        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            
            if (DatabaseType.DATABASE_ORACLE.equals(ConnectionHelper.getDatabaseType(connection)))
            {
                statement = connection.prepareStatement("SELECT seq_groups.nextval FROM dual");
                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getString(1);
                }
                ConnectionHelper.cleanup(rs);
                ConnectionHelper.cleanup(statement);
                
                statement = connection.prepareStatement("INSERT INTO " + _groupsListTable + " (Id, Label) VALUES(?, ?)");
                statement.setString(1, id);
                statement.setString(2, name);
            }
            else
            {
                statement = connection.prepareStatement("INSERT INTO " + _groupsListTable + " (" + _groupsListColLabel + ") VALUES (?)");
                statement.setString(1, name);
            }
            
            statement.executeUpdate();
            
            ConnectionHelper.cleanup(statement);
            
            //FIXME Write query working with all database
            if (DatabaseType.DATABASE_MYSQL.equals(ConnectionHelper.getDatabaseType(connection)))
            {
                statement = connection.prepareStatement("SELECT " + _groupsListColId + " FROM " + _groupsListTable + " WHERE " + _groupsListColId + " = last_insert_id()");    
                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getString(_groupsListColId);
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
            else if (DatabaseType.DATABASE_DERBY.equals(ConnectionHelper.getDatabaseType(connection)))
            {
                statement = connection.prepareStatement("VALUES IDENTITY_VAL_LOCAL ()");
                rs = statement.executeQuery();
                if (rs.next())
                {
                    id = rs.getString(1);
                }
            }
            else if (DatabaseType.DATABASE_POSTGRES.equals(ConnectionHelper.getDatabaseType(connection)))
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
                    listener.groupAdded(id);
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

        return new Group(id, name);
    }

    public void remove(String groupID) throws InvalidModificationException
    {
        Connection connection = null;
        PreparedStatement statement = null;

        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            
            statement = connection.prepareStatement("DELETE FROM " + _groupsListTable + " WHERE " + _groupsListColId + " = ?");
            statement.setInt(1, Integer.parseInt(groupID));
            
            if (statement.executeUpdate() == 0)
            {
                throw new InvalidModificationException("No group with id '" + groupID + "' may be removed");
            }
            ConnectionHelper.cleanup(statement);       

            statement = connection.prepareStatement("DELETE FROM " + _groupsCompositionTable + " WHERE " + _groupsCompositionColGroup + " = ?");
            statement.setInt(1, Integer.parseInt(groupID));
            
            statement.executeUpdate();

            for (GroupListener listener : _listeners)
            {
                listener.groupRemoved(groupID);
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

    public void update(Group userGroup) throws InvalidModificationException
    {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try
        {
            connection = ConnectionHelper.getConnection(_poolName);
            
            // Start transaction.
            connection.setAutoCommit(false);
            
            statement = connection.prepareStatement("UPDATE " + _groupsListTable + " SET " + _groupsListColLabel + "=? WHERE " + _groupsListColId + " = ?");
            statement.setString(1, userGroup.getLabel());
            statement.setInt(2, Integer.parseInt(userGroup.getId()));
            
            if (statement.executeUpdate() == 0)
            {
                throw new InvalidModificationException("No group with id '" + userGroup.getId() + "' may be removed");
            }
            ConnectionHelper.cleanup(statement);

            statement = connection.prepareStatement("DELETE FROM " + _groupsCompositionTable + " WHERE " + _groupsCompositionColGroup + " = ?");
            statement.setInt(1, Integer.parseInt(userGroup.getId()));
            
            statement.executeUpdate();
            ConnectionHelper.cleanup(statement);

            Iterator userIt = userGroup.getUsers().iterator();
            while (userIt.hasNext())
            {
                String login = (String) userIt.next();

                statement = connection.prepareStatement("INSERT INTO " + _groupsCompositionTable + " (" + _groupsCompositionColGroup + ", " + _groupsCompositionColUser + ") VALUES (?, ?)");
                statement.setInt(1, Integer.parseInt(userGroup.getId()));
                statement.setString(2, login);
                
                statement.executeUpdate();
                ConnectionHelper.cleanup(statement);
            }
            
            // Commit transaction.
            connection.commit();
            
            for (GroupListener listener : _listeners)
            {
                listener.groupUpdated(userGroup.getId());
            }
        }
        catch (NumberFormatException ex)
        {
            throw new InvalidModificationException("No group with id '" + userGroup.getId() + "' may be removed", ex);
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
    
    public void toSAX(ContentHandler ch, int count, int offset, Map parameters) throws SAXException
    {
        XMLUtils.startElement(ch, "groups");
        
        String pattern = (String) parameters.get("pattern");
        
        Iterator iterator = getGroups().iterator();
        
        int currentOffset = offset;
        // Parcourir les groupes
        while (currentOffset > 0 && iterator.hasNext())
        {
            Group group = (Group) iterator.next();
            if (pattern == null || pattern.length() == 0 || group.getLabel().toLowerCase().indexOf(pattern.toLowerCase()) != -1)
            {
                currentOffset--;
            }
        }
        
        int currentCount = count;
        // Parcourir les groupes
        while ((count == -1 || currentCount > 0) && iterator.hasNext())
        {
            Group group = (Group) iterator.next();
            
            if (pattern == null || pattern.length() == 0 || group.getLabel().toLowerCase().indexOf(pattern.toLowerCase()) != -1)
            {
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute("", "id", "id", "CDATA", group.getId());
                XMLUtils.startElement(ch, "group", attr);
                
                XMLUtils.createElement(ch, "label", group.getLabel());
    
                XMLUtils.startElement(ch, "users");
    
                // Parcourir les utilisateurs du groupe courant
                for (String login : group.getUsers())
                {
                    XMLUtils.createElement(ch, "user", login);
                }
    
                XMLUtils.endElement(ch, "users");
                XMLUtils.endElement(ch, "group");
                
                currentCount--;
            }
        }
        
        XMLUtils.endElement(ch, "groups");
    }
}
