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
package org.ametys.core.group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.group.directory.GroupDirectory;
import org.ametys.core.ui.Callable;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Helper for associating {@link GroupDirectory}(ies) to contexts.
 */
public class GroupDirectoryContextHelper extends AbstractLogEnabled implements Component, Serviceable
{
    /** Avalon Role */
    public static final String ROLE = GroupDirectoryContextHelper.class.getName();
    
    /** The "admin" context */
    public static final String ADMIN_CONTEXT = "/admin";
    
    /** The name of the JDBC table for group directories by context */
    private static final String __GROUP_DIRECTORIES_TABLE = "GroupDirectoriesByContext";

    /** The DAO for {@link GroupDirectory}(ies) */
    private GroupDirectoryDAO _groupDirectoryDAO;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _groupDirectoryDAO = (GroupDirectoryDAO) manager.lookup(GroupDirectoryDAO.ROLE);
    }
    
    /**
     * Get the connection to the database 
     * @return the SQL connection
     */
    protected Connection getSQLConnection ()
    {
        return ConnectionHelper.getInternalSQLDataSourceConnection();
    }
    
    /**
     * Links given group directories to a context.
     * @param context The context
     * @param ids The ids of the group directories to link
     * @return The ids of the linked group directories
     */
    @SuppressWarnings("resource")
    @Callable
    public List<String> link(String context, List<String> ids)
    {
        List<String> result = new ArrayList<>();
        
        Connection connection = null;
        PreparedStatement stmt = null;
        try
        {
            connection = getSQLConnection();
            
            // Remove all the ids affected to this context
            String sql = "DELETE FROM " + __GROUP_DIRECTORIES_TABLE + " WHERE Context=?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, context);
            stmt.executeUpdate();
            getLogger().info("{}\n[{}]", sql, context);
            
            // Set the new ids to this context
            sql = "INSERT INTO " + __GROUP_DIRECTORIES_TABLE + " (Context, GroupDirectory_Id) VALUES(?, ?)";
            stmt = connection.prepareStatement(sql);
            for (String id : ids)
            {
                if (_groupDirectoryDAO.getGroupDirectory(id) != null)
                {
                    stmt.setString(1, context);
                    stmt.setString(2, id);
                    stmt.executeUpdate();
                    
                    getLogger().info("{}\n[{}, {}]", sql, context, id);
                    result.add(id);
                }
                else
                {
                    getLogger().warn("The GroupDirectory with id '{}' does not exist. It will not be linked.", id);
                }
            }            
        }
        catch (SQLException e)
        {
            getLogger().error("Error in sql query", e);
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
            ConnectionHelper.cleanup(stmt);
        }
        
        return result;
    }
    
    /**
     * Gets the group directories linked to the given context
     * @param context The context
     * @return The ids of group directories linked to the context
     */
    @Callable
    public Set<String> getGroupDirectoriesOnContext(String context)
    {
        if (ADMIN_CONTEXT.equals(context))
        {
            // Return all the group directories
            return _groupDirectoryDAO.getGroupDirectoriesIds();
        }
        else
        {
            return  _getDirectorieOnContextFromDatabase(context);
        }
    }
    
    /**
     * Gets the group directories linked to at least one of the given contexts
     * @param contexts The contexts
     * @return The ids of group directories linked to the contexts
     */
    @Callable
    public Set<String> getGroupDirectoriesOnContexts(Set<String> contexts)
    {
        return contexts.stream()
                .map(this::getGroupDirectoriesOnContext)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }
    
    private Set<String> _getDirectorieOnContextFromDatabase(String context)
    {
        Set<String> result = new HashSet<>();
        
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            
            String sql = "SELECT Context, GroupDirectory_Id FROM " + __GROUP_DIRECTORIES_TABLE + " WHERE Context=?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, context);
            rs = stmt.executeQuery();
            getLogger().info("{}\n[{}]", sql, context);
            
            while (rs.next())
            {
                String groupDirectoryId = rs.getString(2);
                if (_groupDirectoryDAO.getGroupDirectory(groupDirectoryId) != null)
                {
                    result.add(groupDirectoryId);
                }
            }
        }
        catch (SQLException e)
        {
            getLogger().error("Error in sql query", e);
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
            ConnectionHelper.cleanup(stmt);
            ConnectionHelper.cleanup(rs);
        }
        
        return result;
    }
}
