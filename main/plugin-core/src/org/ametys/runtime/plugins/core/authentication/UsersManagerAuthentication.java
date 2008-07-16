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
package org.ametys.runtime.plugins.core.authentication;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.runtime.authentication.Authentication;
import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.user.CredentialsAwareUsersManager;
import org.ametys.runtime.user.UsersManager;


/**
 * Authenticate http request with the current base users extension (which must
 * implements AuthenticatingBaseUsers).
 */
public class UsersManagerAuthentication extends AbstractLogEnabled implements Authentication, Serviceable
{
    private UsersManager _users;
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _users = (UsersManager) manager.lookup(UsersManager.ROLE);
    }
    
    /**
     * Check if a User can log in. Returns the User identified by this login or null if none.
     * @param credentials Contains user information with an unencrypted password
     * @return true if the user is authenticated, false otherwise.
     */
    public boolean login(Credentials credentials)
    {
        // Vérifier qu'il sait authentifier
        if (_users instanceof CredentialsAwareUsersManager)
        {
            CredentialsAwareUsersManager auth = (CredentialsAwareUsersManager) _users;

            // Effectuer l'authentification en cryptant le mot de passe
            return auth.checkCredentials(credentials);
        }
        
        getLogger().error("UsersManager cannot authenticate");
        
        // Classe d'authentification invalide, interdir l'accès
        return false;
    }
}
