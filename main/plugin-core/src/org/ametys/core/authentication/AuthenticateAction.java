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
package org.ametys.core.authentication;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;
import org.ametys.core.user.population.PopulationContextHelper;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;
import org.ametys.runtime.authentication.AccessDeniedException;
import org.ametys.runtime.workspace.WorkspaceMatcher;

/**
 * Cocoon action to perform authentication.<br>
 * The {@link CredentialProvider} define the authentication method and retrieves {@link Credentials}.<br>
 * Finally, the Users instance extract the Principal corresponding to the {@link Credentials}.
 */
public class AuthenticateAction extends ServiceableAction implements ThreadSafe, Initializable 
{
    /** The request attribute to allow internal action from an internal request. */
    public static final String REQUEST_ATTRIBUTE_INTERNAL_ALLOWED = "Runtime:InternalAllowedRequest";
    
    /** The session attribute name for storing the identity of the connected user */
    public static final String SESSION_USERIDENTITY = "Runtime:UserIdentity";
    /** The session attribute name for storing the credential provider of the authentication */
    public static final String SESSION_CREDENTIALPROVIDER = "Runtime:CredentialProvider";
    /** The session attribute name for storing the credential provider mode of the authentication: non-blocking=>false, blocking=>true */
    public static final String SESSION_CREDENTIALPROVIDER_MODE = "Runtime:CredentialProviderMode";
    /** The session attribute name for storing the id of the user population */
    public static final String SESSION_USERPOPULATION_ID = "Runtime:UserPopulation";
    
    /** The request attribute name for transmitting the list of credential provider to choose. */
    public static final String REQUEST_ATTRIBUTE_CREDENTIAL_PROVIDER_LIST = "Runtime:RequestListCredentialProvider";
    /** The request attribute name for transmitting the potential list of user populations to the login screen . */
    public static final String REQUEST_ATTRIBUTE_POPULATIONS = "Runtime:RequestPopulations";
    /** The request attribute name for transmitting the potential list of user populations to the login screen . */
    public static final String REQUEST_ATTRIBUTE_INVALID_POPULATION = "Runtime:RequestInvalidPopulation";
    
    /** Name of the user population html field */
    public static final String REQUEST_PARAMETER_POPULATION_NAME = "hiddenPopulation";
    /** Name of the credential provider index html field */
    public static final String REQUEST_PARAMETER_CREDENTIALPROVIDER_INDEX = "CredentialProviderIndex";
    
    /** The request attribute name for indicating that the authentication process has been made. */
    protected static final String __REQUEST_ATTRIBUTE_AUTHENTICATED = "Runtime:RequestAuthenticated";
    /** The url for the different login screens */
    protected static final String __REDIRECT_URL_LOGIN_SCREEN = "cocoon://_plugins/core/login.html";
    /** The url for the logout screen */
    protected static final String __REDIRECT_URL_LOGOUT_SCREEN = "cocoon://_plugins/core/logout.html";

    /** The DAO for user populations */
    protected UserPopulationDAO _userPopulationDAO;
    /** The user manager */
    protected UserManager _userManager;
    /** The helper for the associations population/context */
    protected PopulationContextHelper _populationContextHelper;
    /** The current user provider */
    protected CurrentUserProvider _currentUserProvider;

    @Override
    public void initialize() throws Exception
    {
        _userPopulationDAO = (UserPopulationDAO) manager.lookup(UserPopulationDAO.ROLE);
        _userManager = (UserManager) manager.lookup(UserManager.ROLE);
        _populationContextHelper = (PopulationContextHelper) manager.lookup(PopulationContextHelper.ROLE);
        
        _currentUserProvider = (CurrentUserProvider) manager.lookup(CurrentUserProvider.ROLE);
    }

    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        // Test if user wants to logout
        boolean logout = _handleLogout(redirector, objectModel, source, parameters);
        // Test if this request was already authentified or it the request is marked as an internal one
        boolean internalRequest = _internalRequest(request);
        // Test if the currently connected user is still valid
        boolean userIsValid = _validateCurrentlyConnectedUser(request, redirector);
        
        // We passed the authentication, let's mark it now
        request.setAttribute(__REQUEST_ATTRIBUTE_AUTHENTICATED, "true");
        
        if (logout || internalRequest || userIsValid || redirector.hasRedirected())
        {
            return EMPTY_MAP;
        }

        // Get context and associated populations
        String context = _getContext(parameters);
        List<UserPopulation> userPopulations = _getUserPopulations(request, context);
        request.setAttribute(REQUEST_ATTRIBUTE_POPULATIONS, userPopulations);
        
        // Get possible credential providers
        List<CredentialProvider> availableCredentialProviders = _getCredentialProviders(userPopulations);
        request.setAttribute(REQUEST_ATTRIBUTE_CREDENTIAL_PROVIDER_LIST, availableCredentialProviders);

        // null means the credential providers cannot be determine without knowing population first
        if (availableCredentialProviders == null)
        {
            // Screen "Where Are You From?" with the list of populations to select
            redirector.redirect(false, __REDIRECT_URL_LOGIN_SCREEN);            
            return EMPTY_MAP;
        }
        else if (availableCredentialProviders.size() == 0)
        {
            throw new IllegalStateException("There is no populations credential provider available for context '" + context + "'");
        }
        
        // Get the currently running credential provider
        int runningCredentialProviderIndex = _getCurrentCredentialProviderIndex(request, availableCredentialProviders);
        
        // Let's process non-blocking
        if (!_isCurrentCredentialProviderInBlockingMode(request))
        {
            // if there was no one running, let's start with the first one
            runningCredentialProviderIndex = Math.max(0, runningCredentialProviderIndex); 
            
            for (; runningCredentialProviderIndex < availableCredentialProviders.size(); runningCredentialProviderIndex++)
            {
                CredentialProvider runningCredentialProvider = availableCredentialProviders.get(runningCredentialProviderIndex);
                if (_process(request, false, runningCredentialProvider, redirector, userPopulations))
                {
                    // Whatever the user was correctly authentified or he just required a redirect: let's stop here for the moment
                    return EMPTY_MAP;
                }
            }
            
            // No one matches
            runningCredentialProviderIndex = -1;
        }
        
        // Let's process the current one blocking or the only existing one
        if (runningCredentialProviderIndex >= 0 || availableCredentialProviders.size() == 1)
        {
            CredentialProvider runningCredentialProvider = availableCredentialProviders.get(Math.max(0, runningCredentialProviderIndex));
            if (_process(request, true, runningCredentialProvider, redirector, userPopulations))
            {
                // Whatever the user was correctly authentified or he just required a redirect: let's stop here for the moment
                return EMPTY_MAP;
            }
            
            throw new AccessDeniedException();
        }
        
        // Let's display the blocking list
        redirector.redirect(false, __REDIRECT_URL_LOGIN_SCREEN);
        return EMPTY_MAP;
    }
    
    /**
     * Determine the list of credential providers to use
     * @param userPopulations The list of applicable user populations
     * @return the list of applicable credential provider or null if it cannot be determined
     */
    protected List<CredentialProvider> _getCredentialProviders(List<UserPopulation> userPopulations)
    {
        // Is there only one population or all populations have the same credential provider list as the first one?
        if (userPopulations.size() == 1 || userPopulations.stream().map(UserPopulation::getCredentialProviders).distinct().count() == 1)
        {
            return userPopulations.get(0).getCredentialProviders();
        }

        // Cannot determine the list
        return null;
    }
    
    /**
     * Get the available populations for the given context
     * @param context The context
     * @return The non-null list of populations
     */
    protected List<String> _getMaximumUserPopulations(String context)
    {
        return _populationContextHelper.getUserPopulationsOnContext(context);
    }

    /**
     * Get the population for the given context
     * @param request The request
     * @param context The context
     * @return The non-null list of populations
     */
    protected List<UserPopulation> _getUserPopulations(Request request, String context)
    {
        // All user populations
        List<String> availableUserPopulationsIds = _getMaximumUserPopulations(context);
        
        // Get request population choice
        String userPopulationId = request.getParameter(REQUEST_PARAMETER_POPULATION_NAME);
        if (StringUtils.isBlank(userPopulationId))
        {
            // Get memorized population choice
            Session session = request.getSession(false);
            if (session != null)
            {
                userPopulationId = (String) session.getAttribute(SESSION_USERPOPULATION_ID);
                session.setAttribute(SESSION_USERPOPULATION_ID, null);
            }
        }
        
        // A population choice was already made
        if (StringUtils.isNotBlank(userPopulationId))
        {
            if (availableUserPopulationsIds.contains(userPopulationId))
            {
                availableUserPopulationsIds = Collections.singletonList(userPopulationId);
            }
            else
            {
                // Wrong submitted population id
                request.setAttribute(REQUEST_ATTRIBUTE_INVALID_POPULATION, true);
            }
        }
        
        // Convert to user population objects
        List<UserPopulation> availableUserPopulations = availableUserPopulationsIds.stream().map(_userPopulationDAO::getUserPopulation).collect(Collectors.toList());
        if (availableUserPopulations.size() == 0)
        {
            throw new IllegalStateException("There is no populations available for context '" + context + "'");
        }
        
        return availableUserPopulations;
    }

    /**
     * When the process end successfully, save the state
     * @param request The request
     * @param runningBlockingkMode false for non-blocking mode, true for blocking mode
     * @param runningCredentialProvider the Credential provider to test
     * @param userPopulations The list of user populations that apply.
     */
    protected void _saveStateToSession(Request request, CredentialProvider runningCredentialProvider, boolean runningBlockingkMode, List<UserPopulation> userPopulations)
    {
        Session session = request.getSession(true);
        session.setAttribute(SESSION_CREDENTIALPROVIDER, runningCredentialProvider);
        session.setAttribute(SESSION_CREDENTIALPROVIDER_MODE, runningBlockingkMode);
        if (userPopulations.size() == 1)
        {
            session.setAttribute(SESSION_USERPOPULATION_ID, userPopulations.get(0).getId());
        }
        else
        {
            session.setAttribute(SESSION_USERPOPULATION_ID, null);
        }
    }

    /**
     * Try to authenticate with this credential provider in this mode. Delegates to _doProcess
     * @param request The request
     * @param runningBlockingkMode false for non-blocking mode, true for blocking mode
     * @param runningCredentialProvider the Credential provider to test
     * @param redirector The cocoon redirector
     * @param userPopulations The list of possible user populations
     * @return false if we should try with another Credential provider, true otherwise
     * @throws Exception If an error occurred
     */

    protected boolean _process(Request request, boolean runningBlockingkMode, CredentialProvider runningCredentialProvider, Redirector redirector, List<UserPopulation> userPopulations) throws Exception
    {
        boolean existingSession = request.getSession(false) != null;
        _saveStateToSession(request, runningCredentialProvider, runningBlockingkMode, userPopulations);
        if (_doProcess(request, runningBlockingkMode, runningCredentialProvider, redirector, userPopulations))
        {
            return true;
        }
        if (existingSession)
        {
            // A session was created but finally we do not need it
            request.getSession().invalidate();
        }
        return false;
    }
    
    /**
     * Try to authenticate with this credential provider in this mode
     * @param request The request
     * @param runningBlockingkMode false for non-blocking mode, true for blocking mode
     * @param runningCredentialProvider the Credential provider to test
     * @param redirector The cocoon redirector
     * @param userPopulations The list of possible user populations
     * @return false if we should try with another Credential provider, true otherwise
     * @throws Exception If an error occurred
     */

    protected boolean _doProcess(Request request, boolean runningBlockingkMode, CredentialProvider runningCredentialProvider, Redirector redirector, List<UserPopulation> userPopulations) throws Exception
    {
        if (runningCredentialProvider.grantAnonymousRequest(runningBlockingkMode))
        {
            // Anonymous request
            return true;
        }
        
        UserIdentity potentialUserIdentity = runningCredentialProvider.getUserIdentity(runningBlockingkMode, redirector);
        if (redirector.hasRedirected())
        {
            // getCredentials require a redirection, save state and proceed
            return true;
        }
        else if (potentialUserIdentity == null)
        {
            // Let us try another credential provider
            return false;
        }
        
        // Check if user exists
        UserIdentity userIdentity = _getUserIdentity(userPopulations, potentialUserIdentity, redirector, runningBlockingkMode, runningCredentialProvider);
        if (redirector.hasRedirected())
        {
            // getCredentials require a redirection, save state and proceed
            return true;
        }
        else if (userIdentity == null)
        {
            // Let us try another credential provider
            return false;
        }

        // Save user identity
        request.getSession(true).setAttribute(SESSION_USERIDENTITY, userIdentity);
        
        // Authentication succeeded
        runningCredentialProvider.userAllowed(runningBlockingkMode, userIdentity);
            
        return true;
    }

    /**
     * If there is a running credential provider, was it in non-blocking or blocking mode?
     * @param request The request
     * @return false if non-blocking, true if blocking
     */
    protected boolean _isCurrentCredentialProviderInBlockingMode(Request request)
    {
        Session session = request.getSession(false);
        if (session != null)
        {
            Boolean mode = (Boolean) session.getAttribute(SESSION_CREDENTIALPROVIDER_MODE);
            session.setAttribute(SESSION_CREDENTIALPROVIDER_MODE, null);
            if (mode != null)
            {
                return mode.booleanValue();
            }
        }
        return false;
    }

    /**
     * Get the current credential provider index or -1 if there no running provider
     * @param request The request
     * @param availableCredentialProviders The list of available credential provider
     * @return The credential provider index to use in the availablesCredentialProviders list or -1
     */
    protected int _getCurrentCredentialProviderIndex(Request request, List<CredentialProvider> availableCredentialProviders)
    {
        // Is the CP requested?
        String requestedCredentialParameterIndex = request.getParameter(REQUEST_PARAMETER_CREDENTIALPROVIDER_INDEX);
        if (StringUtils.isNotBlank(requestedCredentialParameterIndex))
        {
            int index = Integer.parseInt(requestedCredentialParameterIndex);
            if (index < availableCredentialProviders.size())
            {
                return index;
            }
            else
            {
                return -1;
            }
        }
        
        // Was the CP memorized?
        Session session = request.getSession(false);
        if (session != null)
        {
            CredentialProvider cp = (CredentialProvider) session.getAttribute(SESSION_CREDENTIALPROVIDER);
            session.setAttribute(SESSION_CREDENTIALPROVIDER, null);
            return availableCredentialProviders.indexOf(cp);
        }
        
        // Default value
        return -1;
    }
    
    /**
     * Get the authentication context
     * @param parameters The action parameters
     * @return The context
     * @throws IllegalArgumentException If there is no context set
     */
    protected String _getContext(Parameters parameters)
    {
        String context = parameters.getParameter("context", null);
        if (context == null)
        {
            throw new IllegalArgumentException("The authentication is not parametrized correctly: an authentication context must be specify");
        }
        return context;
    }

    /**
     * Determine if the request is internal and do not need authentication
     * @param request The request
     * @return true to bypass this authentication
     */
    protected boolean _internalRequest(Request request)
    {
        return "true".equals(request.getAttribute(__REQUEST_ATTRIBUTE_AUTHENTICATED)) || request.getAttribute(REQUEST_ATTRIBUTE_INTERNAL_ALLOWED) != null;
    }

    /**
     * This method ensure that there is a currently connected user and that it is still valid
     * @param request The request
     * @param redirector The cocoon redirector
     * @return true if the user is connected and valid
     * @throws Exception if an error occurred
     */
    protected boolean _validateCurrentlyConnectedUser(Request request, Redirector redirector) throws Exception
    {
        Session session = request.getSession(false);
        CredentialProvider runningCredentialProvider = session != null ? (CredentialProvider) session.getAttribute(AuthenticateAction.SESSION_CREDENTIALPROVIDER) : null;
        UserIdentity userCurrentlyConnected = session != null ? (UserIdentity) session.getAttribute(SESSION_USERIDENTITY) : null;
        Boolean runningBlockingkMode = session != null ? (Boolean) session.getAttribute(SESSION_CREDENTIALPROVIDER_MODE) : null;
        
        if (runningCredentialProvider == null || userCurrentlyConnected == null || runningBlockingkMode == null || !runningCredentialProvider.isStillConnected(runningBlockingkMode, userCurrentlyConnected, redirector))
        {
            if (redirector.hasRedirected())
            {
                return true;
            }
            
            // There is an invalid connected user
            if (session != null && userCurrentlyConnected != null)
            {
                session.invalidate();
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * Test if user wants to logout and handle it
     * @param redirector The cocoon redirector
     * @param objectModel The cocoon object model
     * @param source The sitemap source
     * @param parameters The sitemap parameters
     * @return true if the user was logged out
     * @throws Exception if an error occurred
     */
    protected boolean _handleLogout(Redirector redirector, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        if (StringUtils.equals(request.getContextPath() + request.getAttribute(WorkspaceMatcher.WORKSPACE_URI) + "/logout.html", request.getRequestURI()))
        {
            // The user logs out
            _currentUserProvider.logout(redirector);
            if (!redirector.hasRedirected())
            {
                redirector.redirect(false, __REDIRECT_URL_LOGOUT_SCREEN);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Check the authentications of the authentication manager
     * @param userPopulations The list of available matching populations
     * @param redirector The cocoon redirector
     * @param runningBlockingkMode false for non-blocking mode, true for blocking mode
     * @param runningCredentialProvider The Credential provider to test
     * @param potentialUserIdentity A possible user identity. Population can be null. User may not exist either.
     * @return The user population matching credentials or null
     * @throws Exception If an error occurred
     * @throws AccessDeniedException If the user is rejected
     */
    protected UserIdentity _getUserIdentity(List<UserPopulation> userPopulations, UserIdentity potentialUserIdentity, Redirector redirector, boolean runningBlockingkMode, CredentialProvider runningCredentialProvider) throws Exception
    {
        if (potentialUserIdentity.getPopulationId() == null) 
        {
            for (UserPopulation up : userPopulations)
            {
                User user = _userManager.getUser(up, potentialUserIdentity.getLogin()); 
                if (user != null)
                {
                    return user.getIdentity();
                }
            }
        }
        else
        {
            User user = _userManager.getUser(potentialUserIdentity.getPopulationId(), potentialUserIdentity.getLogin()); 
            if (user != null)
            {
                return user.getIdentity();
            }
        }
        
        runningCredentialProvider.userNotAllowed(runningBlockingkMode, redirector);
        
        if (getLogger().isWarnEnabled())
        {
            getLogger().warn("The user '" + potentialUserIdentity + "' was authentified by the credential provider '" + runningCredentialProvider.getCredentialProviderModelId() + "' but it does not match any user of the " + userPopulations.size() + " granted populations.");
        }
        
        return null;
    }
}
