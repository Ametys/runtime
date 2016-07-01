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
package org.ametys.core.user.directory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ametys.core.authentication.Credentials;
import org.ametys.core.user.User;

/**
 * Abstraction for getting users list and verify the presence of a particular user.
 */
public interface UserDirectory
{
    /**
     * Get the list of all users of one directory.
     * @return list of users as Collection of <code>User</code>s, empty if a problem occurs.
     */
    public Collection<User> getUsers();
    
    /**
     * Get a list of users from a directory given the parameters
     * @param count The limit of users to retrieve
     * @param offset The number of result to ignore before starting to collect users. 
     * @param parameters A map of additional parameters, see implementation.
     * @return The list of retrieved {@link User}
     */
    public List<User> getUsers(int count, int offset, Map<String, Object> parameters);
    
    /**
     * Get a particular user by his login.
     * @param login Login of the user to get. Cannot be null.
     * @return User's information as a <code>User</code> instance or null if the user login does not exist.
     */
    public User getUser(String login);
    
    /**
     * Get the id of the {@link UserDirectoryModel} extension point
     * @return the id of extension point
     */
    public String getUserDirectoryModelId();
    
    /**
     * Get the values of parameters (from user directory model)
     * @return the parameters' values
     */
    public Map<String, Object> getParameterValues();
    
    /**
     * Initialize the user's directory with given parameters' values.
     * @param udModelId The id of user directory extension point
     * @param paramValues The parameters' values
     * @throws Exception If an error occured
     */
    public void init(String udModelId, Map<String, Object> paramValues) throws Exception;
    
    /**
     * Set the value of the id of the population this user directory belong to.
     * @param populationId The id of the population the user directory belongs to.
     */
    public void setPopulationId(String populationId);
    
    /**
     * Get the id of the population this user directory belongs to. 
     * @return The id of the population
     */
    public String getPopulationId();
    
    /**
     * Authenticate a user with its credentials
     * @param credentials the credentials of the user. Cannot be null.
     * @return true if the user is authenticated, false otherwise.
     */
    public boolean checkCredentials(Credentials credentials);  
    
}