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
package org.ametys.core.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.population.PopulationContextHelper;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Component for getting user list and verify the presence of a particular user on a context or for user directory(ies).
 */
public class UserManager extends AbstractLogEnabled implements Component, Serviceable
{
    /** Avalon Role */
    public static final String ROLE = UserManager.class.getName(); 
            
    /** The DAO for User Population */
    protected UserPopulationDAO _userPopulationDAO;
    /** The helper for the associations population/context */
    protected PopulationContextHelper _populationContextHelper;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _userPopulationDAO = (UserPopulationDAO) manager.lookup(UserPopulationDAO.ROLE);
        _populationContextHelper = (PopulationContextHelper) manager.lookup(PopulationContextHelper.ROLE);
    }
    
    /**
     * Get the list of users on some given contexts
     * @param contexts The contexts
     * @return the collection of users
     */
    public Collection<User> getUsersByContext(Set<String> contexts)
    {
        List<UserPopulation> userPopulations = new ArrayList<>();
        Set<String> upIds = _populationContextHelper.getUserPopulationsOnContexts(contexts);
        
        for (String upId : upIds)
        {
            userPopulations.add(_userPopulationDAO.getUserPopulation(upId));
        }
        
        return getUsersByPopulations(userPopulations);
    }
    
    /**
     * Get the users for given users' populations
     * @param userPopulationIds the id of population of users
     * @return the collection of users
     */
    public Collection<User> getUsersByPopulationIds(List<String> userPopulationIds)
    {
        List<User> users = new ArrayList<>();
        for (String id : userPopulationIds)
        {
            UserPopulation userPopulation = _userPopulationDAO.getUserPopulation(id);
            if (userPopulation != null)
            {
                for (User user : getUsers(userPopulation))
                {
                    if (!users.contains(user))
                    {
                        users.add(user);
                    }
                }
            }
        }
        return users;
    }
    
    /**
     * Get the users for given users' populations
     * @param userPopulations the population of users
     * @return the collection of users
     */
    public Collection<User> getUsersByPopulations(List<UserPopulation> userPopulations)
    {
        List<User> users = new ArrayList<>();
        for (UserPopulation userPopulation : userPopulations)
        {
            for (User user : getUsers(userPopulation))
            {
                if (!users.contains(user))
                {
                    users.add(user);
                }
            }
        }
        return users;
    }
    
    /**
     * Gets all the users of a {@link UserPopulation}
     * @param userPopulationId The ID of user population
     * @return list of users as Collection of {@link User}s, empty if a problem occurs.
     */
    public Collection<User> getUsers(String userPopulationId)
    {
        UserPopulation userPopulation = _userPopulationDAO.getUserPopulation(userPopulationId);
        if (userPopulation != null)
        {
            return getUsers(userPopulation);
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }
    
    /**
     * Gets all the users of a {@link UserPopulation}
     * @param userPopulation The user population
     * @return list of users as Collection of {@link User}s, empty if a problem occurs.
     */
    public Collection<User> getUsers(UserPopulation userPopulation)
    {
        List<User> users = new ArrayList<>();
        
        for (UserDirectory ud : userPopulation.getUserDirectories())
        {
            for (User user : ud.getUsers())
            {
                if (!users.contains(user))
                {
                    users.add(user);
                }
            }
        }
        
        return users;
    }
    
    /**
     * Get a list of users given the parameters
     * @param contexts The contexts
     * @param count The limit of users to retrieve
     * @param offset The number of result to ignore before starting to collect users. 
     * @param parameters A map of additional parameters, see implementation.
     * @return The list of retrieved {@link User}
     */
    public List<User> getUsersByContext(Set<String> contexts, int count, int offset, Map<String, Object> parameters)
    {
        List<UserPopulation> userPopulations = new ArrayList<>();
        Set<String> upIds = _populationContextHelper.getUserPopulationsOnContexts(contexts);
        
        for (String upId : upIds)
        {
            userPopulations.add(_userPopulationDAO.getUserPopulation(upId));
        }
        
        return getUsers(userPopulations, count, offset, parameters);
    }
    
    /**
     * Get a list of users given the parameters
     * @param userPopulations the population of users
     * @param count The limit of users to retrieve
     * @param offset The number of result to ignore before starting to collect users. 
     * @param parameters A map of additional parameters, see implementation.
     * @return The list of retrieved {@link User}
     */
    public List<User> getUsers(List<UserPopulation> userPopulations, int count, int offset, Map<String, Object> parameters)
    {
        List<User> users = new ArrayList<>();
        for (UserPopulation userPopulation : userPopulations)
        {
            for (User user : getUsers(userPopulation, -1, 0, parameters))
            {
                if (!users.contains(user))
                {
                    users.add(user);
                }
            }
        }
        
        int boundedCount = count >= 0 ? count : Integer.MAX_VALUE;
        int boundedOffset = offset >= 0 ? offset : 0;
        int toIndex;
        if (boundedOffset + boundedCount >= 0)
        {
            toIndex = Math.min(boundedOffset + boundedCount, users.size());
        }
        else
        {
            // particular case where count was initially negative (to say "no limit") and we set it to Integer.MAX_VALUE
            // so if the offset is strictly positive, the sum overflows
            toIndex = users.size();
        }
        return users.subList(boundedOffset, toIndex);
    }
    
    /**
     * Gets all the users of a {@link UserPopulation}
     * @param userPopulationId The ID of user population
     * @param count The limit of users to retrieve
     * @param offset The number of result to ignore before starting to collect users. 
     * @param parameters A map of additional parameters, see implementation.
     * @return list of users as Collection of {@link User}s, empty if a problem occurs.
     */
    public Collection<User> getUsers(String userPopulationId, int count, int offset, Map<String, Object> parameters)
    {
        UserPopulation userPopulation = _userPopulationDAO.getUserPopulation(userPopulationId);
        if (userPopulation != null)
        {
            return getUsers(userPopulation, count, offset, parameters);
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }
    
    /**
     * Gets all the users of a given {@link UserPopulation} and {@link UserDirectory}
     * @param userPopulationId The ID of user population
     * @param userDirectoryIndex The index of the user directory
     * @param count The limit of users to retrieve
     * @param offset The number of result to ignore before starting to collect users. 
     * @param parameters A map of additional parameters, see implementation.
     * @return list of users as Collection of {@link User}s, empty if a problem occurs.
     */
    public Collection<User> getUsersByDirectory(String userPopulationId, int userDirectoryIndex, int count, int offset, Map<String, Object> parameters)
    {
        UserPopulation userPopulation = _userPopulationDAO.getUserPopulation(userPopulationId);
        
        if (userPopulation == null)
        {
            return Collections.EMPTY_LIST;
        }
        
        UserDirectory ud = userPopulation.getUserDirectories().get(userDirectoryIndex);
        return ud.getUsers(count, offset, parameters);
    }
    
    /**
     * Gets all the users of a {@link UserPopulation}
     * @param userPopulation The users population
     * @param count The limit of users to retrieve
     * @param offset The number of result to ignore before starting to collect users. 
     * @param parameters A map of additional parameters, see implementation.
     * @return list of users as Collection of {@link User}s, empty if a problem occurs.
     */
    public Collection<User> getUsers(UserPopulation userPopulation, int count, int offset, Map<String, Object> parameters)
    {
        List<User> users = new ArrayList<>();
        
        for (UserDirectory ud : userPopulation.getUserDirectories())
        {
            for (User user : ud.getUsers(-1, 0, parameters))
            {
                if (!users.contains(user))
                {
                    users.add(user);
                }
            }
        }
        
        int boundedCount = count >= 0 ? count : Integer.MAX_VALUE;
        int boundedOffset = offset >= 0 ? offset : 0;
        int toIndex;
        if (boundedOffset + boundedCount >= 0)
        {
            toIndex = Math.min(boundedOffset + boundedCount, users.size());
        }
        else
        {
            // particular case where count was initially negative (to say "no limit") and we set it to Integer.MAX_VALUE
            // so if the offset is strictly positive, the sum overflows
            toIndex = users.size();
        }
        return users.subList(boundedOffset, toIndex);
    }
    
    /**
     * Gets all the users of a {@link UserPopulation}
     * @param userPopulation The users population
     * @param userDirectoryIndex The index of the user directory
     * @param count The limit of users to retrieve
     * @param offset The number of result to ignore before starting to collect users. 
     * @param parameters A map of additional parameters, see implementation.
     * @return list of users as Collection of {@link User}s, empty if a problem occurs.
     */
    public Collection<User> getUsersByDirectory(UserPopulation userPopulation, int userDirectoryIndex, int count, int offset, Map<String, Object> parameters)
    {
        UserDirectory ud = userPopulation.getUserDirectories().get(userDirectoryIndex);
        return ud.getUsers(count, offset, parameters);
    }
    
    /**
     * Get a user by his login on some given contexts
     * @param contexts The contexts
     * @param login Login of the user to get. Cannot be null.
     * @return User's information as a {@link User} instance or null if the user login does not exist.
     */
    public User getUserByContext(Set<String> contexts, String login)
    {
        Set<String> upIds = _populationContextHelper.getUserPopulationsOnContexts(contexts);
        for (String upId : upIds)
        {
            UserPopulation userPopulation = _userPopulationDAO.getUserPopulation(upId);
            User user = getUser(userPopulation, login);
            if (user != null)
            {
                return user;
            }
        }
        return null;
    }
    
    /**
     * Get the user from its user identity
     * @param userIdentity The user identity
     * @return The User or null if the user login does not exist.
     */
    public User getUser (UserIdentity userIdentity)
    {
        return getUser(userIdentity.getPopulationId(), userIdentity.getLogin());
    }
    
    /**
     * Get a particular user of the given users population by his login.
     * @param userPopulationId The ID of user population
     * @param login Login of the user to get. Cannot be null.
     * @return User's information as a {@link User} instance or null if the user login does not exist.
     */
    public User getUser(String userPopulationId, String login)
    {
        UserPopulation userPopulation = _userPopulationDAO.getUserPopulation(userPopulationId);
        if (userPopulation != null)
        {
            return getUser(userPopulation, login);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Get a particular user of the given user population and given user directory by his login.
     * @param userPopulationId The ID of user population
     * @param userDirectoryIndex The index of the user directory
     * @param login Login of the user to get. Cannot be null.
     * @return User's information as a {@link User} instance or null if the user login does not exist.
     */
    public User getUserByDirectory(String userPopulationId, int userDirectoryIndex, String login)
    {
        UserPopulation userPopulation = _userPopulationDAO.getUserPopulation(userPopulationId);
        if (userPopulation != null)
        {
            return getUserByDirectory(userPopulation, userDirectoryIndex, login);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Get a particular user of the given user population by his login.
     * @param userPopulation The user population
     * @param login Login of the user to get. Cannot be null.
     * @return User's information as a {@link User} instance or null if the user login does not exist.
     */
    public User getUser(UserPopulation userPopulation, String login)
    {
        for (UserDirectory ud : userPopulation.getUserDirectories())
        {
            User user = ud.getUser(login);
            if (user != null)
            {
                return user;
            }
        }
        return null;
    }
    
    /**
     * Get a particular user of the given user population and given user directory by his login.
     * @param userPopulation The user population
     * @param userDirectoryIndex The index of the user directory
     * @param login Login of the user to get. Cannot be null.
     * @return User's information as a {@link User} instance or null if the user login does not exist.
     */
    public User getUserByDirectory(UserPopulation userPopulation, int userDirectoryIndex, String login)
    {
        UserDirectory ud = userPopulation.getUserDirectories().get(userDirectoryIndex);
        
        User user = ud.getUser(login);
        if (user != null)
        {
            return user;
        }
            
        return null;
    }
    
    /**
     * Get the user directory the given user belongs to
     * @param userPopulationId The id of the user population
     * @param login Login of the user to get. Cannot be null.
     * @return The user directory the user belongs to.
     */
    public UserDirectory getUserDirectory(String userPopulationId, String login)
    {
        UserPopulation userPopulation = _userPopulationDAO.getUserPopulation(userPopulationId);
        if (userPopulation == null)
        {
            return null;
        }
        
        for (UserDirectory ud : userPopulation.getUserDirectories())
        {
            User user = ud.getUser(login);
            if (user != null)
            {
                return ud;
            }
        }
        return null;
    }
}
