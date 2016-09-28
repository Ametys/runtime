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
package org.ametys.core.user.population;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.ObservationConstants;
import org.ametys.core.datasource.ConnectionHelper;
import org.ametys.core.observation.Event;
import org.ametys.core.observation.ObservationManager;
import org.ametys.core.ui.Callable;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Helper for associating {@link UserPopulation}s to contexts.
 */
public class PopulationContextHelper extends AbstractLogEnabled implements Component, Serviceable
{
    /** Avalon Role */
    public static final String ROLE = PopulationContextHelper.class.getName();
 
    /** The name of request attribute holding the current population contexts */
    public static final String POPULATION_CONTEXTS_REQUEST_ATTR = "populationContexts";
    
    /** The "admin" context */
    public static final String ADMIN_CONTEXT = "/admin";
    
    /** The name of the JDBC table for user populations by context */
    private static final String __USER_POPULATIONS_TABLE = "UserPopulationsByContext";

    /** The DAO for {@link UserPopulation}s */
    private UserPopulationDAO _userPopulationDAO;

    private ObservationManager _observationManager;

    private CurrentUserProvider _currentUserProvider;

    private ServiceManager _manager;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
    }
    
    private ObservationManager getObservationManager()
    {
        if (_observationManager == null)
        {
            try
            {
                _observationManager = (ObservationManager) _manager.lookup(ObservationManager.ROLE);
            }
            catch (ServiceException e)
            {
                throw new RuntimeException(e);
            }
        }
        return _observationManager;
    }
    
    private UserPopulationDAO getUserPopulationDAO()
    {
        if (_userPopulationDAO == null)
        {
            try
            {
                _userPopulationDAO = (UserPopulationDAO) _manager.lookup(UserPopulationDAO.ROLE);
            }
            catch (ServiceException e)
            {
                throw new RuntimeException(e);
            }
        }
        return _userPopulationDAO;
    }
    
    private CurrentUserProvider getCurrentUserProvider()
    {
        if (_currentUserProvider == null)
        {
            try
            {
                _currentUserProvider = (CurrentUserProvider) _manager.lookup(CurrentUserProvider.ROLE);
            }
            catch (ServiceException e)
            {
                throw new RuntimeException(e);
            }
        }
        return _currentUserProvider;
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
     * Links given populations to a context.
     * @param context The context
     * @param ids The ids of the populations to link
     * @return The ids of the changed user populations (the ones unlinked and the ones linked)
     */
    @SuppressWarnings("resource")
    @Callable
    public List<String> link(String context, List<String> ids)
    {
        List<String> result = getUserPopulationsOnContext(context); // Get the already linked ids
        
        Connection connection = null;
        PreparedStatement stmt = null;
        try
        {
            connection = getSQLConnection();
            
            // Remove all the ids affected to this context
            String sql = "DELETE FROM " + __USER_POPULATIONS_TABLE + " WHERE Context=?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, context);
            stmt.executeUpdate();
            getLogger().info("{}\n[{}]", sql, context);
            
            // Set the new ids to this context
            sql = "INSERT INTO " + __USER_POPULATIONS_TABLE + " (Context, Ordering, UserPopulation_Id) VALUES(?, ?, ?)";
            stmt = connection.prepareStatement(sql);
            for (int index = 0; index < ids.size(); index++)
            {
                String id = ids.get(index);
                if (getUserPopulationDAO().getUserPopulation(id) != null)
                {
                    stmt.setString(1, context);
                    stmt.setInt(2, index);
                    stmt.setString(3, id);
                    stmt.executeUpdate();
                    
                    getLogger().info("{}\n[{}, {}, {}]", sql, context, index, id);
                    if (result.contains(id))
                    {
                        result.remove(id); // the population was already linked, so its status didn't changed
                    }
                    else
                    {
                        result.add(id);
                    }
                }
                else
                {
                    getLogger().warn("The user population with id '{}' does not exist. It will not be linked.", id);
                }
            }
        }
        catch (SQLException e)
        {
            getLogger().error("SQL error while linking user populations to a context", e);
        }
        finally
        {
            ConnectionHelper.cleanup(connection);
            ConnectionHelper.cleanup(stmt);
        }
        
        Map<String, Object> eventParams = new HashMap<>();
        eventParams.put(ObservationConstants.ARGS_USERPOPULATION_IDS, ids);
        eventParams.put(ObservationConstants.ARGS_USERPOPULATION_CONTEXT, context);
        getObservationManager().notify(new Event(ObservationConstants.EVENT_USERPOPULATIONS_ASSIGNMENT, getCurrentUserProvider().getUser(), eventParams));
        
        return result;
    }
    
    /**
     * Gets the populations linked to the given context (need the population to be enabled)
     * @param context The context
     * @return The ids of populations linked to the context
     */
    @Callable
    public List<String> getUserPopulationsOnContext(String context)
    {
        if (ADMIN_CONTEXT.equals(context))
        {
            // Return all the enabled populations
            return getUserPopulationDAO().getEnabledUserPopulations(true).stream().map(UserPopulation::getId).collect(Collectors.toList());
        }
        else
        {
            return  _getPopulationsOnContextFromDatabase(context);
        }
    }
    
    /**
     * Gets the populations linked to at least one of the given contexts (need the population to be enabled)
     * @param contexts The contexts
     * @return The ids of populations linked to the contexts
     */
    @Callable
    public List<String> getUserPopulationsOnContexts(Set<String> contexts)
    {
        return contexts.stream()
                .map(this::getUserPopulationsOnContext)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
    
    private List<String> _getPopulationsOnContextFromDatabase(String context)
    {
        List<String> result = new ArrayList<>();
        
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            
            String sql = "SELECT Context, Ordering, UserPopulation_Id FROM " + __USER_POPULATIONS_TABLE + " WHERE Context=? ORDER BY Ordering";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, context);
            rs = stmt.executeQuery();
            getLogger().info("{}\n[{}]", sql, context);
            
            while (rs.next())
            {
                String userPopulationId = rs.getString(3);
                if (getUserPopulationDAO().getUserPopulation(userPopulationId) == null)
                {
                    getLogger().warn("The population of id '{}' is linked to a context, but does not exist anymore.", userPopulationId);
                }
                else if (!getUserPopulationDAO().getUserPopulation(userPopulationId).isEnabled())
                {
                    getLogger().warn("The population of id '{}' is linked to a context but disabled. It will not be returned.", userPopulationId);
                }
                else
                {
                    result.add(userPopulationId);
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
    
    /**
     * Returns true if the user population is linked to a context.
     * @param upId The id of the user population
     * @return True if the user population is currently linked to a context.
     */
    public boolean isLinked(String upId)
    {
        boolean result = false;
        
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            connection = getSQLConnection();
            
            String sql = "SELECT UserPopulation_Id FROM " + __USER_POPULATIONS_TABLE + " WHERE UserPopulation_Id=?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, upId);
            rs = stmt.executeQuery();
            getLogger().info("{}\n[{}]", sql, upId);
            
            result = rs.next();
        }
        catch (SQLException e)
        {
            getLogger().error("SQL error while checking if the population is linked to a context", e);
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
