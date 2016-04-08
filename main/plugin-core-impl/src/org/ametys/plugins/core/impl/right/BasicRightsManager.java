/*
 *  Copyright 2012 Anyware Services
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.ametys.core.right.RightsException;
import org.ametys.core.right.RightsExtensionPoint;
import org.ametys.core.right.RightsManager;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;

/**
 * This rights manager always answers OK. 
 */
public class BasicRightsManager implements RightsManager, Serviceable, ThreadSafe, Component
{
    private UserManager _userManager;
    private UserPopulationDAO _userPopulationDAO;
    private RightsExtensionPoint _rightsExtensionPoint;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _userManager = (UserManager) manager.lookup(UserManager.ROLE);
        _userPopulationDAO = (UserPopulationDAO) manager.lookup(UserPopulationDAO.ROLE);
        _rightsExtensionPoint = (RightsExtensionPoint) manager.lookup(RightsExtensionPoint.ROLE);
    }
    
    @Override
    public Set<UserIdentity> getGrantedUsers(String right, String context) throws RightsException
    {
        Set<UserIdentity> result = new HashSet<>();
        
        // We want to return all the users, iterate over the populations
        for (UserPopulation userPopulation : _userPopulationDAO.getUserPopulations(true))
        {
            Collection<User> users = _userManager.getUsers(userPopulation);
            for (User user : users)
            {
                result.add(user.getIdentity());
            }
        }
        
        return result;
    }
    
    @Override
    public Set<UserIdentity> getGrantedUsers(String context) throws RightsException
    {
        Set<UserIdentity> result = new HashSet<>();

        // We want to return all the users, iterate over the populations
        for (UserPopulation userPopulation : _userPopulationDAO.getUserPopulations(true))
        {
            Collection<User> users = _userManager.getUsers(userPopulation);
            for (User user : users)
            {
                result.add(user.getIdentity());
            }
        }

        return result;
    }
    
    @Override
    public Set<String> getUserRights(UserIdentity user, String context)
    {
        return _rightsExtensionPoint.getExtensionsIds();
    }
    
    @Override
    public Map<String, Set<String>> getUserRights(UserIdentity user) throws RightsException
    {
        Map<String, Set<String>> rights = new HashMap<>();
        rights.put("/", _rightsExtensionPoint.getExtensionsIds());
        return rights;
    }
    
    @Override
    public RightResult hasRight(UserIdentity user, String right, String context) throws RightsException
    {
        return RightsManager.RightResult.RIGHT_OK;
    }

}
