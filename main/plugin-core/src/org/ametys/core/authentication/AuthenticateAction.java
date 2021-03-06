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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
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
import org.ametys.plugins.core.impl.authentication.FormCredentialProvider;
import org.ametys.runtime.authentication.AccessDeniedException;
import org.ametys.runtime.authentication.AuthorizationRequiredException;
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
    
    /** The request attribute meaning that the request was not authenticated but granted */
    public static final String REQUEST_ATTRIBUTE_GRANTED = "Runtime:GrantedRequest";
    /** The request attribute name for transmitting the list of user populations */
    public static final String REQUEST_ATTRIBUTE_AVAILABLE_USER_POPULATIONS_LIST = "Runtime:UserPopulationsList";
    /** The request attribute name for transmitting the currently chosen user population */
    public static final String REQUEST_ATTRIBUTE_USER_POPULATION_ID = "Runtime:CurrentUserPopulationId";
    /** The request attribute name for transmitting the login page url */
    public static final String REQUEST_ATTRIBUTE_LOGIN_URL = "Runtime:RequestLoginURL";

    /** Name of the user population HTML field */
    public static final String REQUEST_PARAMETER_POPULATION_NAME = "UserPopulation";
    /** Name of the credential provider index HTML field */
    public static final String REQUEST_PARAMETER_CREDENTIALPROVIDER_INDEX = "CredentialProviderIndex";
    
    /** The request attribute name for transmitting a boolean that tell if there is a list of credential provider to choose */
    protected static final String REQUEST_ATTRIBUTE_CREDENTIAL_PROVIDER_LIST = "Runtime:RequestListCredentialProvider";
    /** The request attribute name for transmitting the index in the list of chosen credential provider */
    protected static final String REQUEST_ATTRIBUTE_CREDENTIAL_PROVIDER_INDEX = "Runtime:RequestCredentialProviderIndex";
    /** The request attribute name to know if user population list should be proposed */
    protected static final String REQUEST_ATTRIBUTE_SHOULD_DISPLAY_USER_POPULATIONS_LIST = "Runtime:UserPopulationsListDisplay";
    /** The request attribute name for transmitting the potential list of user populations to the login screen . */
    protected static final String REQUEST_ATTRIBUTE_INVALID_POPULATION = "Runtime:RequestInvalidPopulation";
    /** The request attribute name for indicating that the authentication process has been made. */
    protected static final String __REQUEST_ATTRIBUTE_AUTHENTICATED = "Runtime:RequestAuthenticated";
    /** The request attribute name for transmitting the list of contexts */
    protected static final String REQUEST_ATTRIBUTE_CONTEXTS = "Runtime:Contexts";

    /** The session attribute name for storing the credential provider index of the authentication (during connection process) */
    protected static final String SESSION_CONNECTING_CREDENTIALPROVIDER_INDEX = "Runtime:ConnectingCredentialProviderIndex";
    /** The session attribute name for storing the last known credential provider index of the authentication (during connection process)*/
    protected static final String SESSION_CONNECTING_CREDENTIALPROVIDER_INDEX_LASTBLOCKINGKNOWN = "Runtime:ConnectingCredentialProviderIndexLastKnown";
    /** The session attribute name for storing the credential provider mode of the authentication: non-blocking=&gt;false, blocking=&gt;true (during connection process) */
    protected static final String SESSION_CONNECTING_CREDENTIALPROVIDER_MODE = "Runtime:ConnectingCredentialProviderMode";
    /** The session attribute name for storing the id of the user population (during connection process) */
    protected static final String SESSION_CONNECTING_USERPOPULATION_ID = "Runtime:ConnectingUserPopulationId";
    
    /** The session attribute name for storing the credential provider of the authentication */
    protected static final String SESSION_CREDENTIALPROVIDER = "Runtime:CredentialProvider";
    /** The session attribute name for storing the credential provider mode of the authentication: non-blocking=&gt;false, blocking=&gt;true */
    protected static final String SESSION_CREDENTIALPROVIDER_MODE = "Runtime:CredentialProviderMode";
    /** The session attribute name for storing the identity of the connected user */
    protected static final String SESSION_USERIDENTITY = "Runtime:UserIdentity";

    /** The DAO for user populations */
    protected UserPopulationDAO _userPopulationDAO;
    /** The user manager */
    protected UserManager _userManager;
    /** The helper for the associations population/context */
    protected PopulationContextHelper _populationContextHelper;
    /** The current user provider */
    protected CurrentUserProvider _currentUserProvider;
    
    /** url requires for authentication */
    protected Collection<Pattern> _acceptedUrlPatterns = Arrays.asList(new Pattern[]{Pattern.compile("^plugins/core/authenticate/[0-9]+$")});

    
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
        
        if (_handleLogout(redirector, objectModel, source, parameters)  // Test if user wants to logout
                || _internalRequest(request)                            // Test if this request was already authenticated or it the request is marked as an internal one
                || _acceptedUrl(request)                                // Test if the url is used for authentication
                || _validateCurrentlyConnectedUser(request, redirector, parameters) // Test if the currently connected user is still valid
                || redirector.hasRedirected())
        {
            // We passed the authentication, let's mark it now
            request.setAttribute(__REQUEST_ATTRIBUTE_AUTHENTICATED, "true");
            return EMPTY_MAP;
        }
        
        // We passed the authentication, let's mark it now
        request.setAttribute(__REQUEST_ATTRIBUTE_AUTHENTICATED, "true");

        // Get population and if possible credential providers
        List<UserPopulation> chosenUserPopulations = new ArrayList<>();
        List<CredentialProvider> credentialProviders = new ArrayList<>();
        if (!_prepareUserPopulationsAndCredentialProviders(request, parameters, redirector, chosenUserPopulations, credentialProviders))
        {
            // Let's display the population screen
            return EMPTY_MAP;
        }
        
        // Get the currently running credential provider
        int runningCredentialProviderIndex = _getCurrentCredentialProviderIndex(request, credentialProviders);
        request.setAttribute(REQUEST_ATTRIBUTE_CREDENTIAL_PROVIDER_INDEX, runningCredentialProviderIndex);
        request.setAttribute(REQUEST_ATTRIBUTE_LOGIN_URL, getLoginURL(request));
        
        // Let's process non-blocking
        if (!_isCurrentCredentialProviderInBlockingMode(request))
        {
            // if there was no one running, let's start with the first one
            runningCredentialProviderIndex = Math.max(0, runningCredentialProviderIndex);
            
            for (; runningCredentialProviderIndex < credentialProviders.size(); runningCredentialProviderIndex++)
            {
                CredentialProvider runningCredentialProvider = credentialProviders.get(runningCredentialProviderIndex);
                if (_process(request, false, runningCredentialProvider, runningCredentialProviderIndex, redirector, chosenUserPopulations))
                {
                    // Whatever the user was correctly authenticated or he just required a redirect: let's stop here for the moment
                    return EMPTY_MAP;
                }
            }
            
            // No one matches
            runningCredentialProviderIndex = -1;
        }
        
        _saveLastKnownBlockingCredentialProvider(request, runningCredentialProviderIndex);
        
        // Let's process the current blocking one or the only existing one
        if (_shouldRunFirstBlockingCredentialProvider(runningCredentialProviderIndex, credentialProviders, request, chosenUserPopulations))
        {
            CredentialProvider runningCredentialProvider = runningCredentialProviderIndex == -1 ? _getFirstBlockingCredentialProvider(credentialProviders) : credentialProviders.get(runningCredentialProviderIndex);
            if (_process(request, true, runningCredentialProvider, runningCredentialProviderIndex, redirector, chosenUserPopulations))
            {
                // Whatever the user was correctly authenticated or he just required a redirect: let's stop here for the moment
                return EMPTY_MAP;
            }
            
            throw new AuthorizationRequiredException();
        }
        
        // At this step we have two kind off requests
        // 1) A secondary request of a blocking cp (such as captcha image...)        
        Integer formerRunningCredentialProviderIndex = (Integer) request.getSession(true).getAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_INDEX_LASTBLOCKINGKNOWN);
        if (formerRunningCredentialProviderIndex != null && credentialProviders.get(formerRunningCredentialProviderIndex).grantAnonymousRequest(true))
        {
            // Anonymous request
            request.setAttribute(REQUEST_ATTRIBUTE_GRANTED, true);
            _saveConnectingStateToSession(request, -1, true);
            return EMPTY_MAP;
        }
        
        // 2) Or a main stream request that should display the list of available blocking cp
        return _displayBlockingList(redirector, request, credentialProviders);
    }

    private void _saveLastKnownBlockingCredentialProvider(Request request, int runningCredentialProviderIndex)
    {
        if (runningCredentialProviderIndex != -1)
        {
            request.getSession(true).setAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_INDEX_LASTBLOCKINGKNOWN, runningCredentialProviderIndex);
        }
    }

    private Map _displayBlockingList(Redirector redirector, Request request, List<CredentialProvider> credentialProviders) throws IOException, ProcessingException, AuthorizationRequiredException
    {
        if (credentialProviders.stream().filter(cp -> cp instanceof BlockingCredentialProvider).findFirst().isPresent())
        {
            _saveConnectingStateToSession(request, -1, true);
            redirector.redirect(false, getLoginURL(request));
            return EMPTY_MAP;
        }
        else
        {
            // No way to login
            throw new AuthorizationRequiredException();
        }
    }
    
    @SuppressWarnings("unchecked")
    private boolean _shouldRunFirstBlockingCredentialProvider(int runningCredentialProviderIndex, List<CredentialProvider> credentialProviders, Request request, List<UserPopulation> chosenUserPopulations)
    {
        return runningCredentialProviderIndex >= 0 // There is a running credential provider 
            || credentialProviders.stream().filter(cp -> cp instanceof BlockingCredentialProvider).count() == 1 // There is a single blocking credential provider AND 
                && (
                        ((List<UserPopulation>) request.getAttribute(REQUEST_ATTRIBUTE_AVAILABLE_USER_POPULATIONS_LIST)).size() == chosenUserPopulations.size() // no population choice screen
                        || _getFirstBlockingCredentialProvider(credentialProviders).requiresNewWindow() // it does not requires a window opening
                );
    }
    
    private BlockingCredentialProvider _getFirstBlockingCredentialProvider(List<CredentialProvider> credentialProviders)
    {
        Optional<CredentialProvider> findFirst = credentialProviders.stream().filter(cp -> cp instanceof BlockingCredentialProvider).findFirst();
        if (findFirst.isPresent())
        {
            return (BlockingCredentialProvider) findFirst.get();
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Fill the list of available users populations and credential providers
     * @param request The request
     * @param parameters The action parameters
     * @param redirector The cocoon redirector
     * @param chosenUserPopulations An empty non-null list to fill with with chosen populations
     * @param credentialProviders An empty non-null list to fill with chosen credential providers
     * @return true, if the population was determined, false if a redirection was required to choose
     * @throws IOException If an error occurred
     * @throws ProcessingException If an error occurred 
     */
    protected boolean _prepareUserPopulationsAndCredentialProviders(Request request, Parameters parameters, Redirector redirector, List<UserPopulation> chosenUserPopulations, List<CredentialProvider> credentialProviders) throws ProcessingException, IOException
    {
        // Get contexts
        List<String> contexts = _getContexts(request, parameters);
        request.setAttribute(REQUEST_ATTRIBUTE_CONTEXTS, contexts);
        
        // All user populations for this context
        List<UserPopulation> availableUserPopulations = _getAvailableUserPopulationsIds(request, contexts).stream().map(_userPopulationDAO::getUserPopulation).collect(Collectors.toList());
        request.setAttribute(REQUEST_ATTRIBUTE_AVAILABLE_USER_POPULATIONS_LIST, availableUserPopulations);
        
        // Chosen population
        String userPopulationId = _getChosenUserPopulationId(request, availableUserPopulations);
        request.setAttribute(REQUEST_ATTRIBUTE_USER_POPULATION_ID, userPopulationId);
        
        chosenUserPopulations.addAll(userPopulationId == null ? availableUserPopulations : Collections.singletonList(_userPopulationDAO.getUserPopulation(userPopulationId)));
        if (chosenUserPopulations.size() == 0)
        {
            throw new IllegalStateException("There is no populations available for contexts '" + StringUtils.join(contexts, "', '") + "'");
        }

        // Get possible credential providers
        boolean availableCredentialProviders = _hasCredentialProviders(chosenUserPopulations);
        request.setAttribute(REQUEST_ATTRIBUTE_CREDENTIAL_PROVIDER_LIST, availableCredentialProviders);

        // null means the credential providers cannot be determine without knowing population first
        if (!availableCredentialProviders)
        {
            request.setAttribute(REQUEST_ATTRIBUTE_SHOULD_DISPLAY_USER_POPULATIONS_LIST, true);
            
            // if we are on this screen after a 'back' button hit, we need to reset connecting information
            _resetConnectingStateToSession(request);
            
            // Screen "Where Are You From?" with the list of populations to select
            if (redirector != null)
            {
                redirector.redirect(false, getLoginURL(request));
            }
            return false;
        }
        else
        {
            credentialProviders.addAll(chosenUserPopulations.get(0).getCredentialProviders());
            if (credentialProviders.size() == 0)
            {
                throw new IllegalStateException("There is no populations credential provider available for contexts '" + StringUtils.join(contexts, "', '") + "'");
            }
            request.setAttribute(REQUEST_ATTRIBUTE_SHOULD_DISPLAY_USER_POPULATIONS_LIST, userPopulationId == null || _hasCredentialProviders(availableUserPopulations) || credentialProviders.size() == 1 && !credentialProviders.stream().filter(cp -> cp instanceof FormCredentialProvider).findAny().isPresent());
            return true;
        }
    }

    /**
     * Get the url for the redirector to display the login screen
     * @param request The request
     * @return The url. Cannot be null or empty
     */
    protected String getLoginURL(Request request)
    {
        return getLoginURLParameters(request, "cocoon://_plugins/core/login.html");
    }
    
    
    /**
     * Get the url for the redirector to display the login screen
     * @param request The request
     * @param baseURL The url to complete with parameters
     * @return The url. Cannot be null or empty
     */
    @SuppressWarnings("unchecked")
    protected String getLoginURLParameters(Request request, String baseURL)
    {
        List<String> parameters = new ArrayList<>();
        
        Boolean invalidPopulationIds = (Boolean) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_INVALID_POPULATION);
        parameters.add("invalidPopulationIds=" + (invalidPopulationIds == Boolean.TRUE ? "true" : "false"));
        
        boolean shouldDisplayUserPopulationsList = (boolean) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_SHOULD_DISPLAY_USER_POPULATIONS_LIST);
        parameters.add("shouldDisplayUserPopulationsList=" + (shouldDisplayUserPopulationsList ? "true" : "false"));
        
        List<UserPopulation> usersPopulations = (List<UserPopulation>) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_AVAILABLE_USER_POPULATIONS_LIST);
        if (usersPopulations != null)
        {
            parameters.add("usersPopulations=" + org.ametys.core.util.StringUtils.encode(StringUtils.join(usersPopulations.stream().map(UserPopulation::getId).collect(Collectors.toList()), ",")));
        }
        
        String chosenPopulationId = (String) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_USER_POPULATION_ID);
        if (chosenPopulationId != null)
        {
            parameters.add("chosenPopulationId=" + org.ametys.core.util.StringUtils.encode(chosenPopulationId));
        }
        
        boolean availableCredentialProviders = (boolean) request.getAttribute(AuthenticateAction.REQUEST_ATTRIBUTE_CREDENTIAL_PROVIDER_LIST);
        parameters.add("availableCredentialProviders=" + (availableCredentialProviders ? "true" : "false"));
        
        Integer credentialProviderIndex = (Integer) request.getAttribute(REQUEST_ATTRIBUTE_CREDENTIAL_PROVIDER_INDEX);
        parameters.add("credentialProviderIndex=" + String.valueOf(credentialProviderIndex != null ? credentialProviderIndex : -1));
        
        List<String> contexts = (List<String>) request.getAttribute(REQUEST_ATTRIBUTE_CONTEXTS);
        parameters.add("contexts=" + org.ametys.core.util.StringUtils.encode(StringUtils.join(contexts, ",")));
        
        return baseURL + (baseURL.contains("?") ? "&" : "?") + StringUtils.join(parameters, "&");
    }
    
    /**
     * Get the url for the redirector to display the logout screen
     * @param request The request
     * @return The url. Cannot be null or empty
     */
    protected String getLogoutURL(Request request)
    {
        return "cocoon://_plugins/core/logout.html";
    }
    
    /**
     * Determine if there is a list of credential providers to use
     * @param userPopulations The list of applicable user populations
     * @return true if credentialproviders can be used
     */
    protected boolean _hasCredentialProviders(List<UserPopulation> userPopulations)
    {
        // Is there only one population or all populations have the same credential provider list as the first one?
        if (userPopulations.size() == 1 || userPopulations.stream().map(UserPopulation::getCredentialProviders).distinct().count() == 1)
        {
            return true;
        }

        // Cannot determine the list
        return false;
    }
    
    /**
     * Get the available populations for the given contexts
     * @param request The request
     * @param contexts The contexts
     * @return The non-null list of populations
     */
    protected List<String> _getAvailableUserPopulationsIds(Request request, List<String> contexts)
    {
        // We return all the populations linked to at least one site
        List<String> populations = new ArrayList<>();
        for (String context : contexts)
        {
            populations.addAll(_populationContextHelper.getUserPopulationsOnContext(context));
        }
        
        return new ArrayList<>(new LinkedHashSet<>(populations));

    }

    /**
     * Get the population for the given context
     * @param request The request
     * @param availableUserPopulations The available users populations
     * @return The chosen population id. Can be null.
     */
    protected String _getChosenUserPopulationId(Request request, List<UserPopulation> availableUserPopulations)
    {
        // Get request population choice
        String userPopulationId = request.getParameter(REQUEST_PARAMETER_POPULATION_NAME);
        if (userPopulationId == null)
        {
            // Get memorized population choice
            Session session = request.getSession(false);
            if (session != null)
            {
                userPopulationId = (String) session.getAttribute(SESSION_CONNECTING_USERPOPULATION_ID);
                session.setAttribute(SESSION_CONNECTING_USERPOPULATION_ID, null);
            }
        }
        
        // A population choice was already made
        if (StringUtils.isNotBlank(userPopulationId))
        {
            final String finalUserPopulationId = userPopulationId;
            if (availableUserPopulations.stream().anyMatch(userPopulation -> userPopulation.getId().equals(finalUserPopulationId)))
            {
                return userPopulationId;
            }
            else
            {
                // Wrong submitted population id
                request.setAttribute(REQUEST_ATTRIBUTE_INVALID_POPULATION, true);
            }
        }
        
        return null;
    }
    
    /**
     * Try to authenticate with this credential provider in this mode. Delegates to _doProcess
     * @param request The request
     * @param runningBlockingkMode false for non-blocking mode, true for blocking mode
     * @param runningCredentialProvider the Credential provider to test
     * @param runningCredentialProviderIndex The index of the currently tested credential provider
     * @param redirector The cocoon redirector
     * @param userPopulations The list of possible user populations
     * @return false if we should try with another Credential provider, true otherwise
     * @throws Exception If an error occurred
     */
    protected boolean _process(Request request, boolean runningBlockingkMode, CredentialProvider runningCredentialProvider, int runningCredentialProviderIndex, Redirector redirector, List<UserPopulation> userPopulations) throws Exception
    {
        boolean existingSession = request.getSession(false) != null;
        _saveConnectingStateToSession(request, runningBlockingkMode ? -1 : runningCredentialProviderIndex, runningBlockingkMode);
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
            request.setAttribute(REQUEST_ATTRIBUTE_GRANTED, true);
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
        _setUserIdentityInSession(request, userIdentity, runningCredentialProvider, runningBlockingkMode);
        
        // Authentication succeeded
        runningCredentialProvider.userAllowed(runningBlockingkMode, userIdentity);
            
        return true;
    }
    
    /**
     * Reset the connecting information in session
     * @param request The request
     */
    protected static void _resetConnectingStateToSession(Request request)
    {
        Session session = request.getSession(false);
        if (session != null)
        {
            session.removeAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_INDEX);
            session.removeAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_MODE);
            session.removeAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_INDEX_LASTBLOCKINGKNOWN);
            session.removeAttribute(SESSION_CONNECTING_USERPOPULATION_ID);
        }
    }
    
    /**
     * When the process end successfully, save the state
     * @param request The request
     * @param runningBlockingkMode false for non-blocking mode, true for blocking mode
     * @param runningCredentialProviderIndex the currently tested credential provider
     */
    protected void _saveConnectingStateToSession(Request request, int runningCredentialProviderIndex, boolean runningBlockingkMode)
    {
        Session session = request.getSession(true);
        session.setAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_INDEX, runningCredentialProviderIndex);
        session.setAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_MODE, runningBlockingkMode);
        session.setAttribute(SESSION_CONNECTING_USERPOPULATION_ID, request.getAttribute(REQUEST_ATTRIBUTE_USER_POPULATION_ID));
    }

    /**
     * Save user identity in request
     * @param request The request
     * @param userIdentity The useridentity to save
     * @param credentialProvider The credential provider used to connect
     * @param blockingMode The mode used for the credential provider
     */
    protected void _setUserIdentityInSession(Request request, UserIdentity userIdentity, CredentialProvider credentialProvider, boolean blockingMode)
    {
        setUserIdentityInSession(request, userIdentity, credentialProvider, blockingMode);
    }
    
    /**
     * Save user identity in request
     * @param request The request
     * @param userIdentity The useridentity to save
     * @param credentialProvider The credential provider used to connect
     * @param blockingMode The mode used for the credential provider
     */
    public static void setUserIdentityInSession(Request request, UserIdentity userIdentity, CredentialProvider credentialProvider, boolean blockingMode)
    {
        Session session = request.getSession(true); 
        _resetConnectingStateToSession(request);
        session.setAttribute(SESSION_USERIDENTITY, userIdentity);
        session.setAttribute(SESSION_CREDENTIALPROVIDER, credentialProvider);
        session.setAttribute(SESSION_CREDENTIALPROVIDER_MODE, blockingMode);
    }
    
    /**
     * Get the user identity of the connected user from the session 
     * @param request The request
     * @return The connected useridentity or null
     */
    protected UserIdentity _getUserIdentityFromSession(Request request)
    {
        return getUserIdentityFromSession(request);
    }
    
    /**
     * Get the user identity of the connected user from the session 
     * @param request The request
     * @return The connected useridentity or null
     */
    public static UserIdentity getUserIdentityFromSession(Request request)
    {
        Session session = request.getSession(false);
        if (session != null)
        {
            return (UserIdentity) session.getAttribute(SESSION_USERIDENTITY);
        }
        return null;
    }
   
    /**
     * Get the credential provider used for the current connection
     * @param request The request 
     * @return The credential provider used or null
     */
    protected CredentialProvider _getCredentialProviderFromSession(Request request)
    {
        return getCredentialProviderFromSession(request);
    }
    
    /**
     * Get the credential provider used for the current connection
     * @param request The request 
     * @return The credential provider used or null
     */
    public static CredentialProvider getCredentialProviderFromSession(Request request)
    {
        Session session = request.getSession(false);
        if (session != null)
        {
            return (CredentialProvider) session.getAttribute(SESSION_CREDENTIALPROVIDER);
        }
        return null;
    }
    
    /**
     * Get the credential provider mode used for the current connection
     * @param request The request 
     * @return The credential provider mode used or null
     */
    protected Boolean _getCredentialProviderModeFromSession(Request request)
    {
        return getCredentialProviderModeFromSession(request);
    }
    
    /**
     * Get the credential provider mode used for the current connection
     * @param request The request 
     * @return The credential provider mode used or null
     */
    public static Boolean getCredentialProviderModeFromSession(Request request)
    {
        Session session = request.getSession(false);
        if (session != null)
        {
            return (Boolean) session.getAttribute(SESSION_CREDENTIALPROVIDER_MODE);
        }
        return null;
    }

    /**
     * If there is a running credential provider, was it in non-blocking or blocking mode?
     * @param request The request
     * @return false if non-blocking, true if blocking
     */
    protected boolean _isCurrentCredentialProviderInBlockingMode(Request request)
    {
        Integer requestedCredentialParameterIndex = _getCurrentCredentialProviderIndexFromParameter(request);
        if (requestedCredentialParameterIndex != null && requestedCredentialParameterIndex != -1)
        {
            return true;
        }
        
        Session session = request.getSession(false);
        if (session != null)
        {
            Boolean mode = (Boolean) session.getAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_MODE);
            session.removeAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_MODE);
            if (mode != null)
            {
                return mode.booleanValue();
            }
        }
        return false;
    }
    
    /**
     * Call this to skip the currently used credential provider and proceed to the next one.
     * Useful for non blocking
     * @param request The request
     */
    public static void skipCurrentCredentialProvider(Request request)
    {
        Session session = request.getSession();
        if (session != null)
        {
            Integer cpIndex = (Integer) session.getAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_INDEX);
            if (cpIndex != null)
            {
                cpIndex++;
                session.setAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_INDEX, cpIndex);
            }
        }
    }

    /**
     * Get the current credential provider index or -1 if there no running provider FROM REQUEST PARAMETER
     * @param request The request
     * @return The credential provider index to use in the availablesCredentialProviders list or -1 or null
     */
    protected Integer _getCurrentCredentialProviderIndexFromParameter(Request request)
    {
        // Is the CP requested?
        String requestedCredentialParameterIndex = request.getParameter(REQUEST_PARAMETER_CREDENTIALPROVIDER_INDEX);
        if (StringUtils.isNotBlank(requestedCredentialParameterIndex))
        {
            int index = Integer.parseInt(requestedCredentialParameterIndex);
            return index;
        }
        return null;
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
        Integer requestedCredentialParameterIndex = _getCurrentCredentialProviderIndexFromParameter(request);
        if (requestedCredentialParameterIndex != null)
        {
            if (requestedCredentialParameterIndex < availableCredentialProviders.size())
            {
                return requestedCredentialParameterIndex;
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
            Integer cpIndex = (Integer) session.getAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_INDEX);
            session.removeAttribute(SESSION_CONNECTING_CREDENTIALPROVIDER_INDEX);
            
            if (cpIndex != null)
            {
                return cpIndex;
            }
        }
        
        // Default value
        return -1;
    }
    
    /**
     * Get the authentication context
     * @param request The request
     * @param parameters The action parameters
     * @return The context
     * @throws IllegalArgumentException If there is no context set
     */
    protected List<String> _getContexts(Request request, Parameters parameters)
    {
        String context = parameters.getParameter("context", null);
        if (context == null)
        {
            throw new IllegalArgumentException("The authentication is not parametrized correctly: an authentication context must be specify");
        }
        return Collections.singletonList(context);
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
     * Determine if the request is one of the authentication process (except the credential providers)
     * @param request The request
     * @return true to bypass this authentication
     */
    protected boolean _acceptedUrl(Request request)
    {
        // URL without server context and leading slash.
        String url = (String) request.getAttribute(WorkspaceMatcher.IN_WORKSPACE_URL);
        for (Pattern pattern : _acceptedUrlPatterns)
        {
            if (pattern.matcher(url).matches())
            {
                // Anonymous request
                request.setAttribute(REQUEST_ATTRIBUTE_GRANTED, true);

                return true;
            }
        }
        
        return false;
    }

    /**
     * This method ensure that there is a currently connected user and that it is still valid
     * @param request The request
     * @param redirector The cocoon redirector
     * @param parameters The action parameters
     * @return true if the user is connected and valid
     * @throws Exception if an error occurred
     */
    protected boolean _validateCurrentlyConnectedUser(Request request, Redirector redirector, Parameters parameters) throws Exception
    {
        Session session = request.getSession(false);
        CredentialProvider runningCredentialProvider = _getCredentialProviderFromSession(request);
        UserIdentity userCurrentlyConnected = _getUserIdentityFromSession(request);
        Boolean runningBlockingkMode = _getCredentialProviderModeFromSession(request);
        
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
        
        // let us make an exception for the user image url since we need it on the 403 page
        if ("plugins/core-ui/current-user/image_64".equals(request.getAttribute(WorkspaceMatcher.IN_WORKSPACE_URL)))
        {
            return true;
        }
        
        // we know this is a valid user, but we need to check if the context is correct
        List<String> contexts = _getContexts(request, parameters);
        // All user populations for this context
        List<String> availableUserPopulationsIds = _getAvailableUserPopulationsIds(request, contexts);

        if (!availableUserPopulationsIds.contains(userCurrentlyConnected.getPopulationId()))
        {
            throw new AccessDeniedException("The user " + userCurrentlyConnected + " cannot be authenticated to the contexts '" + StringUtils.join(contexts, "', '") + "' because its populations is not part of the " + availableUserPopulationsIds.size() + " granted populations.");
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
        if (StringUtils.equals(request.getContextPath() + request.getAttribute(WorkspaceMatcher.WORKSPACE_URI) + "/logout.html", request.getRequestURI())
                || StringUtils.equals("true", parameters.getParameter("logout", "false")))
        {
            // The user logs out
            _currentUserProvider.logout();
            if (!redirector.hasRedirected())
            {
                redirector.redirect(false, getLogoutURL(request));
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
            getLogger().warn("The user '" + potentialUserIdentity + "' was authenticated by the credential provider '" + runningCredentialProvider.getCredentialProviderModelId() + "' but it does not match any user of the " + userPopulations.size() + " granted populations.");
        }
        
        return null;
    }
}
