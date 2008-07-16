/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
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
