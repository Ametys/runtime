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

import java.util.HashSet;
import java.util.Set;

import org.ametys.runtime.right.HierarchicalRightsHelper;


/**
 * This right manager looks for right in content and in parent context 
 */
public class HierarchicalProfileBasedRightsManager extends DefaultProfileBasedRightsManager
{
    @Override
    public Set<String> getGrantedUsers(String right, String context)
    {
        if (context == null)
        {
            return super.getGrantedUsers(right, context);
        }
        else
        {
            Set<String> users = new HashSet<String>();
            
            String transiantContext = context;
            
            while (transiantContext != null)
            {
                Set<String> addUsers = super.getGrantedUsers(right, transiantContext);
                users.addAll(addUsers);
                
                transiantContext = HierarchicalRightsHelper.getParentContext(transiantContext);
            }
            
            return users;
        }
    }
    
    @Override
    public Set<String> getUserRights(String login, String context)
    {
        if (context == null)
        {
            return super.getUserRights(login, context);
        }
        else
        {
            Set<String> rights = new HashSet<String>();
            
            String transiantContext = context;
            
            while (transiantContext != null)
            {
                Set<String> addRights = super.getUserRights(login, transiantContext);
                rights.addAll(addRights);
    
                transiantContext = HierarchicalRightsHelper.getParentContext(transiantContext);
            }
            
            return rights;
        }
    }
    
    @Override
    public RightResult hasRight(String userLogin, String right, String context)
    {
        RightResult hasRight = super.hasRight(userLogin, right, context);
        if (hasRight == RightResult.RIGHT_OK)
        {
            return RightResult.RIGHT_OK;
        }
    
        String parentContext = HierarchicalRightsHelper.getParentContext(context);
        if (parentContext == null || parentContext.length() == 0)
        {
            return RightResult.RIGHT_NOK;
        }

        return hasRight(userLogin, right, parentContext);
    }
}
