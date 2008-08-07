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
package org.ametys.runtime.authentication;

import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UserHelper;
import org.ametys.runtime.user.UsersManager;


/**
 * Cocoon action to perform authentication. <br>
 * The CredentialsProvider define the authentication method and retrieves Credentials. <br>
 * The Authentication performs actual authentication. <br>
 * Finally, the Users instance extract the Principal corresponding to the Credentials.
 */
public class AuthenticateAction extends ServiceableAction implements ThreadSafe, Initializable
{
    private CredentialsProvider _credentialsProvider;
    private AuthenticationManager _authManager;
    private UsersManager _usersManager;
    
    public void initialize() throws Exception
    {
        // Si l'appli n'a pas démarrée, il ne faut pas démarrer cette action non plus
        if (Config.getInstance() == null)
        {
            return;
        }
        
        _credentialsProvider = (CredentialsProvider) manager.lookup(CredentialsProvider.ROLE);
        _authManager = (AuthenticationManager) manager.lookup(AuthenticationManager.ROLE);
        _usersManager = (UsersManager) manager.lookup(UsersManager.ROLE);
    }

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (!_checkAuth(objectModel, redirector))
        {
            throw new AccessDeniedException();
        }

        return EMPTY_MAP;
    }

    private boolean _checkAuth(Map objectModel, Redirector redirector) throws Exception
    {
        boolean isValid = _credentialsProvider.validate(redirector);
        
        if (redirector.hasRedirected())
        {
            return true;
        }
        
        if (_credentialsProvider.accept())
        {
            // Laisser passer la requête
            // Ne pas demander de credentials
            return true;
        }
        
        if (isValid)
        {
            // If user already registered, accept it.
            String registeredUP = UserHelper.getCurrentUser(objectModel);
            if (registeredUP != null)
            {
                // User already authenticated
                return true;
            }
        }
        
        // Utilisateur inconnu
        // On recherche ses identifiants
        Credentials credentials = _credentialsProvider.getCredentials(redirector);
        if (redirector.hasRedirected())
        {
            return true;
        }
        if (credentials == null)
        {
            _credentialsProvider.notAllowed(redirector);
            if (redirector.hasRedirected())
            {
                return true;
            }
            return false;
        }

        // On teste ses authentification
        for (String authId : _authManager.getExtensionsIds())
        {
            Authentication authentication = _authManager.getExtension(authId);
            if (!authentication.login(credentials))
            {
                _credentialsProvider.notAllowed(redirector);
                if (redirector.hasRedirected())
                {
                    return true;
                }
                return false;
            }
        }

        // Il faut enfin que l'utilisateur soit connu par la base utilisateur.
        User user = _usersManager.getUser(credentials.getLogin());
        if (user == null)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The user '" + credentials.getLogin() + "' was authentified and authorized by authentications, but it can not be found by the user manager. It is so refused.");
            }
            return false;
        }

        // L'authentification a réussi
        // On laisse une dernière fois la main au CredentialsProvider
        _credentialsProvider.allowed(redirector);

        // Et on enregistre l'utilisateur dans la session
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(true);
        session.setAttribute(UserHelper.SESSION_USERLOGIN, user.getName());
        
        return true;
    }
}
