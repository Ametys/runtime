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

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

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
}
