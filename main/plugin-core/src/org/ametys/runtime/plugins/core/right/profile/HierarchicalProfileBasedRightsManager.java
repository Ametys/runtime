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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.ametys.runtime.datasource.ConnectionHelper;
import org.ametys.runtime.right.HierarchicalRightsHelper;
import org.ametys.runtime.right.HierarchicalRightsManager;
import org.ametys.runtime.right.RightsException;


/**
 * This right manager looks for right in content and in parent context 
 */
public class HierarchicalProfileBasedRightsManager extends DefaultProfileBasedRightsManager implements HierarchicalRightsManager
{
    @Override
    public RightResult hasRightOnContextPrefix(String userLogin, String right, String contextPrefix) throws RightsException
    {
        if (userLogin == null)
        {
            return RightResult.RIGHT_NOK;
        }
        
        if (getDeclaredContexts(userLogin, right, contextPrefix).isEmpty())
        {
            return RightResult.RIGHT_NOK;
        }
        return RightResult.RIGHT_OK;
    }
    
    @Override
    public Set<String> getGrantedUsers(String context) throws RightsException
    {
        try
        {
            if (context == null)
            {
                return super.getGrantedUsers(context);
            }
            else
            {
                Set<String> users = new HashSet<String>();
    
                Set<String> convertedContexts = getAliasContext(context);
                for (String convertContext : convertedContexts)
                {
                    String transiantContext = convertContext;
                    
                    while (transiantContext != null)
                    {
                        Set<String> addUsers = internalGetGrantedUsers(transiantContext);
                        users.addAll(addUsers);
                        
                        transiantContext = HierarchicalRightsHelper.getParentContext(transiantContext);
                    }
                }
                
                return users;
            }
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
    }
    
    @Override
    public Set<String> getGrantedUsers(String right, String context) throws RightsException
    {
        try
        {
            if (context == null)
            {
                return super.getGrantedUsers(right, context);
            }
            else
            {
                Set<String> users = new HashSet<String>();
    
                Set<String> convertedContexts = getAliasContext(context);
                for (String convertContext : convertedContexts)
                {
                    String transiantContext = convertContext;
                    
                    while (transiantContext != null)
                    {
                        Set<String> addUsers = internalGetGrantedUsers(right, transiantContext);
                        users.addAll(addUsers);
                        
                        transiantContext = HierarchicalRightsHelper.getParentContext(transiantContext);
                    }
                }
                
                return users;
            }
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
    }
    
    @Override
    public Set<String> getUserRights(String login, String context) throws RightsException
    {
        try
        {
            if (login == null)
            {
                return new HashSet<String>();
            }
            else if (context == null)
            {
                return super.getUserRights(login, context);
            }
            else
            {
                Set<String> rights = new HashSet<String>();
                
                Set<String> convertedContexts = getAliasContext(context);
                for (String convertContext : convertedContexts)
                {
                    String transiantContext = convertContext;
                    
                    while (transiantContext != null)
                    {
                        Set<String> addRights = internalGetUserRights(login, transiantContext);
                        rights.addAll(addRights);
            
                        transiantContext = HierarchicalRightsHelper.getParentContext(transiantContext);
                    }
                }
                
                return rights;
            }
        }
        catch (SQLException ex)
        {
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
    }
    
    @Override
    public RightResult hasRight(String userLogin, String right, String context) throws RightsException
    {
        if (userLogin == null)
        {
            return RightResult.RIGHT_NOK;
        }
        
        Set<String> convertedContexts = getAliasContext(context);
        for (String convertContext : convertedContexts)
        {
            String transiantContext = convertContext;
            
            while (transiantContext != null) // && transiantContext.length() != 0)
            {
                RightResult hasRight = internalHasRight(userLogin, right, transiantContext);
                
                if (hasRight == RightResult.RIGHT_OK)
                {
                    return RightResult.RIGHT_OK;
                }
                
                transiantContext = HierarchicalRightsHelper.getParentContext(transiantContext);
            }
        }
        
        return RightResult.RIGHT_NOK;
    }
    
    @Override
    public void updateContext(String oldContext, String newContext) throws RightsException
    {
        String lcOldContext = getFullContext(oldContext);
        String lcNewContext = getFullContext(newContext);
        
        Connection connection = ConnectionHelper.getConnection(_poolName);
        
        try
        {
            // Start a transaction.
            connection.setAutoCommit(false);
            
            // Update the exact context.
            updateContext(lcOldContext, lcNewContext, connection);
            
            // Update the children contexts.
            updateGroupChildrenContexts(lcOldContext, lcNewContext, connection);
            updateUserChildrenContexts(lcOldContext, lcNewContext, connection);
            
            // Commit.
            connection.commit();
        }
        catch (SQLException ex)
        {
            try
            {
                connection.rollback();
            }
            catch (SQLException e)
            {
                // Ignore.
            }
            
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
            // Start a transaction.
            connection.setAutoCommit(false);
            
            // Remove the exact context.
            removeAll(lcContext, connection);
            
            // Remove the children contexts.
            removeAllChildren(lcContext, connection);
            
            // Commit.
            connection.commit();
        }
        catch (SQLException ex)
        {
            try
            {
                connection.rollback();
            }
            catch (SQLException e)
            {
                // Ignore.
            }
            
            getLogger().error("Error in sql query", ex);
            throw new RightsException("Error in sql query", ex);
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
        }
    }
    
    /**
     * Modify children contexts on the user table.
     * @param oldPrefix the old context prefix.
     * @param newPrefix the new context prefix.
     * @param connection the database connection.
     * @throws SQLException if an error occurs.
     */
    protected void updateUserChildrenContexts(String oldPrefix, String newPrefix, Connection connection) throws SQLException
    {
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet rs = null;
        
        try
        {
            // Select all the assignments under the given context.
            String sql = "SELECT * FROM " + _tableUserRights + " WHERE LOWER(Context) LIKE ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, oldPrefix + "/%");
            
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(sql + "\n[" + oldPrefix + "/%]");
            }
            rs = stmt.executeQuery();
            
            // Clear the user right cache.
            _clearUserRightCache();
            
            // Modify the context on each entry. 
            while (rs.next())
            {
                int profileId = rs.getInt("Profile_Id");
                String login = rs.getString("Login");
                String context = rs.getString("Context");
                String newContext = newPrefix + StringUtils.removeStart(context, oldPrefix);
                
                sql = "UPDATE " + _tableUserRights + " SET Context = ? WHERE Context = ? AND Profile_Id = ? AND Login = ?";
                stmt2 = connection.prepareStatement(sql);
                stmt2.setString(1, newContext);
                stmt2.setString(2, context);
                stmt2.setInt(3, profileId);
                stmt2.setString(4, login);
                
                stmt2.executeUpdate();
                
                ConnectionHelper.cleanup(stmt2);
            }
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt2);
            ConnectionHelper.cleanup(stmt);
        }
    }
    
    /**
     * Modify children contexts on the group table.
     * @param oldPrefix the old context prefix.
     * @param newPrefix the new context prefix.
     * @param connection the database connection.
     * @throws SQLException if an error occurs.
     */
    protected void updateGroupChildrenContexts(String oldPrefix, String newPrefix, Connection connection) throws SQLException
    {
        PreparedStatement stmt = null;
        PreparedStatement stmt2 = null;
        ResultSet rs = null;
        
        try
        {
            // Select all the assignments under the given context.
            String sql = "SELECT * FROM " + _tableGroupRights + " WHERE LOWER(Context) LIKE ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, oldPrefix + "/%");
            
            if (getLogger().isInfoEnabled())
            {
                getLogger().info(sql + "\n[" + oldPrefix + "/%]");
            }
            rs = stmt.executeQuery();
            
            // Clear the group right cache.
            _clearGroupRightCache();
            
            // Modify the context on each entry. 
            while (rs.next())
            {
                int profileId = rs.getInt("Profile_Id");
                String group = rs.getString("Group_Id");
                String context = rs.getString("Context");
                String newContext = newPrefix + StringUtils.removeStart(context, oldPrefix);
                
                sql = "UPDATE " + _tableGroupRights + " SET Context = ? WHERE Context = ? AND Profile_Id = ? AND Group_Id = ?";
                stmt2 = connection.prepareStatement(sql);
                stmt2.setString(1, newContext);
                stmt2.setString(2, context);
                stmt2.setInt(3, profileId);
                stmt2.setString(4, group);
                
                stmt2.executeUpdate();
                
                ConnectionHelper.cleanup(stmt2);
            }
        }
        finally
        {
            ConnectionHelper.cleanup(rs);
            ConnectionHelper.cleanup(stmt2);
            ConnectionHelper.cleanup(stmt);
        }
    }
    
    /**
     * Modify children contexts on the group table.
     * @param contextPrefix the context prefix.
     * @param connection the database connection.
     * @throws SQLException if an error occurs.
     */
    protected void removeAllChildren(String contextPrefix, Connection connection) throws SQLException
    {
        PreparedStatement stmt = null;
        
        try
        {
            String sql = "DELETE FROM " + _tableGroupRights + " WHERE LOWER(Context) LIKE ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, contextPrefix + "/%");
            getLogger().info(sql + "\n[" + contextPrefix + "/%]");
            stmt.executeUpdate();
            
            _clearGroupRightCache();
            
            ConnectionHelper.cleanup(stmt);
            
            sql = "DELETE FROM " + _tableUserRights + " WHERE LOWER(Context) LIKE ?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, contextPrefix + "/%");
            getLogger().info(sql + "\n[" + contextPrefix + "/%]");
            stmt.executeUpdate();
            
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
    
}
