/*
 *  Copyright 2010 Anyware Services
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

import org.ametys.runtime.RuntimeConstants;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;

/**
 * Cocoon action to perform authentication.<br>
 * The {@link CredentialsProvider} define the authentication method and retrieves {@link Credentials}.<br>
 * The {@link Authentication} chain performs actual authentication.<br>
 * Finally, the Users instance extract the Principal corresponding to the {@link Credentials}.
 */
public class AuthenticateAction extends ServiceableAction implements ThreadSafe, Initializable
{
    /** The session attribute name for storing the login of the connected user. */
    public static final String SESSION_USERLOGIN = "Runtime:UserLogin";
    
    /** The request attribute name for indicating that the authentication process has been made. */
    public static final String REQUEST_AUTHENTICATED = "Runtime:RequestAuthenticated";
    
    private CredentialsProvider _credentialsProvider;
    private AuthenticationManager _authManager;
    private UsersManager _usersManager;
    
    public void initialize() throws Exception
    {
        // If the application in not configured yet, do not initialize this action
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
        Request request = ObjectModelHelper.getRequest(objectModel);
        if ("true".equals(request.getAttribute(REQUEST_AUTHENTICATED)))
        {
            // If the authentication has already been processed, don't do it twice.
            return EMPTY_MAP;
        }
        else if (request.getAttribute(RuntimeConstants.INTERNAL_ALLOWED_REQUEST_ATTR) != null)
        {
            // Allow bypassing the authentication for an internal request.
            return EMPTY_MAP;
        }
        
        boolean authenticated = _checkAuth(objectModel, redirector);
        
        if (authenticated)
        {
            // Set the flag indicating the authentication as processed
            request.setAttribute(REQUEST_AUTHENTICATED, "true");
        }
        else
        {
            throw new AccessDeniedException();
        }

        return EMPTY_MAP;
    }

    /**
     * Process the actual authentication.
     * @param objectModel the current objectModel
     * @param redirector the Cocoon redirector
     * @return true if the current user is authenticated, false otherwise
     * @throws Exception if an error occures during the authentication process
     */
    protected boolean _checkAuth(Map objectModel, Redirector redirector) throws Exception
    {
        boolean isValid = _credentialsProvider.validate(redirector);
        
        if (redirector.hasRedirected())
        {
            return true;
        }
        
        if (_credentialsProvider.accept())
        {
            // The request does not need authentication, don't ask for credentials
            return true;
        }
        
        if (isValid)
        {
            // If user already registered, accept it.
            String authenticatedUser = null;
            Request request = ObjectModelHelper.getRequest(objectModel);
            Session session = request.getSession(false);
            
            if (session != null)
            {
                authenticatedUser = (String) session.getAttribute(SESSION_USERLOGIN);
            }

            if (authenticatedUser != null)
            {
                // User already authenticated
                return true;
            }
        }
        
        // Unknown user, looking for his credentials
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

        // Testing all configured Authentications
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

        // The user must be known by the UsersManager
        User user = _usersManager.getUser(credentials.getLogin());
        if (user == null)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The user '" + credentials.getLogin() + "' was authentified and authorized by authentications, but it can not be found by the user manager. It is so refused.");
            }
            
            return false;
        }

        // Authentication succeeded
        _credentialsProvider.allowed(redirector);

        // Then register the user in the HTTP Session
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(true);
        session.setAttribute(SESSION_USERLOGIN, user.getName());
        
        return true;
    }
}
