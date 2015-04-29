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
package org.ametys.runtime.right;

import java.util.HashSet;
import java.util.Set;

import org.ametys.runtime.right.RightsManager.RightResult;


/**
 * Application rights manager with hierarchical contexts.<br>
 * Call the rights manager, first with the full context,
 * and then with the parent context till the root context when
 * the right is unknown.<br>
 * The separator is the '/' character.
 */
public final class HierarchicalRightsHelper
{
    // Denied use of the constructor because of singleton.
    private HierarchicalRightsHelper()
    {
        // empty constructor
    }
    
    /**
     * Check a permission for a user, in a given context and its parents' context.
     * @param rightsManager the RightManager of this application
     * @param userLogin The user's login
     * @param right the name of the right to check.
     * @param context The context to test the right as document URI.
     * @return true if the user is allowed to use this rigth in the context, false otherwise.
     */
    public static boolean hasRight(RightsManager rightsManager, String userLogin, String right, String context)
    {
        boolean rightUnknown = true;
        String currentContext = context;
        
        // Remonter dans les contextes parents tant que le droit est inconnu
        while (rightUnknown && currentContext != null)
        {
            RightResult res = rightsManager.hasRight(userLogin, right, currentContext);
            
            if (res == RightResult.RIGHT_OK)
            {
                return true;
            }
            
            if (res == RightResult.RIGHT_NOK)
            {
                return false;
            }

            // Droit inconnu, remonter au contexte parent
            currentContext = getParentContext(currentContext);
        }
        // Le droit n'est pas connu, ne pas l'autoriser
        return false;
    }

    /**
     * Get the list of users that have a particular right in a particular context (and its parents' context).
     * @param rightsManager the RightManager of this application
     * @param right the name of the right to use.
     * @param context The context to test the right as document URI.
     * @return The set of users with that right as a Set of String (login).
     */
    public static Set<String> getGrantedUsers(RightsManager rightsManager, String right, String context)
    {
        String currentContext = context;
        Set<String> grantedUsers = new HashSet<String>();

        while (currentContext != null)
        {
            Set<String> currentGrantedUsers = rightsManager.getGrantedUsers(right, currentContext);
            
            // Ajouter les utilisateurs autorisés dans le contexte courant
            grantedUsers.addAll(currentGrantedUsers);
            
            // Remonter au contexte parent
            currentContext = getParentContext(currentContext);
        }

        return grantedUsers;
    }
    
    /**
     * Get the list of a user's rights in a particular context (and its parents' context).
     * The user's rights and the rights of the user's groups are returned.
     * @param rightsManager the RightManager of this application
     * @param login the user's login.
     * @param context The context to test the right.<br>May be null, in which case the returned Set contains all granted rights, whatever the context.
     * <br>Wilcards may also be used (eg. "ctx/*") to get all granted rights in the given context and all subcontexts.
     * @return The list of rights as a Set of String (id).
     */
    public static Set<String> getUserRights(RightsManager rightsManager, String login, String context)
    {
        String currentContext = context;
        Set<String> userRights = new HashSet<String>();

        while (currentContext != null)
        {
            Set<String> currentUserRights = rightsManager.getUserRights(login, currentContext);
            
            // Ajouter les utilisateurs autorisés dans le contexte courant
            userRights.addAll(currentUserRights);
            
            // Remonter au contexte parent
            currentContext = getParentContext(currentContext);
        }

        return userRights;
    }
    
    /**
     * Get the parent of a context
     * @param context A context.
     * @return The parent context, or null if not exists.
     */
    public static String getParentContext(String context)
    {
        if (context == null)
        {
            return null;
        }
        
        // Chercher le dernier caractère /
        int index = context.lastIndexOf("/");
        
        if (index < 0)
        {
            // Pas de parent
            return null;
        }
        else if (index == 0)
        {
            return "";
        }
        else
        {
            // Parent existant
            return context.substring(0, index);
        }
    }
}
