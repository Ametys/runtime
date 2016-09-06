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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.user.population.PopulationContextHelper;
import org.ametys.core.user.population.UserPopulation;
import org.ametys.core.user.population.UserPopulationDAO;
import org.ametys.plugins.core.impl.authentication.FormCredentialProvider;
import org.ametys.plugins.core.impl.authentication.token.TokenCredentials;
import org.ametys.runtime.authentication.AccessDeniedException;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.workspace.WorkspaceMatcher;

/**
 * Cocoon action to perform authentication.<br>
 * The {@link CredentialProvider} define the authentication method and retrieves {@link Credentials}.<br>
 * The {@link Authentication} chain performs actual authentication.<br>
 * Finally, the Users instance extract the Principal corresponding to the {@link Credentials}.
 */
public class AuthenticateAction extends ServiceableAction implements ThreadSafe, Initializable
{
    /** The session attribute name for storing the identity of the connected user. */
    public static final String SESSION_USERIDENTITY = "Runtime:UserIdentity";
    /** The session attribute name for storing the credential provider of the authentication. */
    public static final String SESSION_CREDENTIALPROVIDER = "Runtime:CredentialProvider";
    
    /** The request attribute name for indicating that the authentication process has been made. */
    public static final String REQUEST_AUTHENTICATED = "Runtime:RequestAuthenticated";
    
    /** The request attribute name for transmitting the list of credential provider to choose. */
    public static final String REQUEST_CHOOSE_CP_LIST = "Runtime:RequestListCredentialProvider";
    /** The request attribute name for transmitting the {@link FormCredentialProvider} to use. */
    public static final String REQUEST_FORM_BASED_CREDENTIAL_PROVIDER = "Runtime:RequestFormBasedCredentialProvider";
    /** The request attribute name for transmitting the index of the form CP under its population */
    public static final String REQUEST_INDEX_FORM_CP = "Runtime:RequestIndexForm";
    /** The request attribute name for transmitting the potential list of user populations to the login screen . */
    public static final String REQUEST_POPULATIONS = "Runtime:RequestPopulations";
    /** The request attribute name for transmitting the potential list of user populations to the login screen . */
    public static final String REQUEST_INVALID_POPULATION = "Runtime:RequestInvalidPopulation";
    /** The request attribute name for indicating if the instance of Ametys is public */
    public static final String REQUEST_AMETYS_PUBLIC = "Runtime:AmetysPublic";
    
    /** Name of the user population html field */
    public static final String SUBMITTED_POPULATION_PARAMETER_NAME = "hiddenPopulation";
    /** Name of the credential provider index html field */
    public static final String SUBMITTED_CP_INDEX_PARAMETER_NAME = "CredentialProviderIndex";
    
    /** Name of the config for the Ametys public */
    protected static final String __CONFIG_AMETYS_PUBLIC = "runtime.ametys.public";
    
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
    /** The value of Ametys Public config */
    protected Boolean _ametysPublic;
    /** The current user provider */
    protected CurrentUserProvider _currentUserProvider;

    @Override
    public void initialize() throws Exception
    {
        _ametysPublic = Config.getInstance() != null ? Config.getInstance().getValueAsBoolean(__CONFIG_AMETYS_PUBLIC) : false;
        
        _userPopulationDAO = (UserPopulationDAO) manager.lookup(UserPopulationDAO.ROLE);
        _userManager = (UserManager) manager.lookup(UserManager.ROLE);
        _populationContextHelper = (PopulationContextHelper) manager.lookup(PopulationContextHelper.ROLE);
        
        _currentUserProvider = (CurrentUserProvider) manager.lookup(CurrentUserProvider.ROLE);
    }

    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
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
        }
        // If the authentication has already been processed, don't do it twice.
        // Allow bypassing the authentication for an internal request.
        else if (!"true".equals(request.getAttribute(REQUEST_AUTHENTICATED)) && request.getAttribute(Authentication.INTERNAL_ALLOWED_REQUEST_ATTR) == null)
        {
            String context = parameters.getParameter("context");
            if (context == null)
            {
                throw new AccessDeniedException();
            }
            
            boolean authenticated = _doAuthenticate(redirector, request, context);
            
            if (authenticated)
            {
                // Set the flag indicating the authentication as processed
                request.setAttribute(REQUEST_AUTHENTICATED, "true");
            }
            else
            {
                throw new AccessDeniedException();
            }
        }
        
        return EMPTY_MAP;
    }
    
    /**
     * Do the process of authentication.
     * @param redirector The redirector
     * @param request The request
     * @param context The context
     * @return True to stop the action or if the user is authenticated
     * @throws Exception If an error occurs.
     */
    protected boolean _doAuthenticate(Redirector redirector, Request request, String context) throws Exception
    {
        // If the process has already been done (then there is a session and a registered user), the user is already authenticated
        Session session = request.getSession(false);
        boolean authenticated = session != null && session.getAttribute(SESSION_USERIDENTITY) != null;
        if (authenticated)
        {
            return true;
        }
        
        List<CredentialProvider> candidatesCredentialProviders = new ArrayList<>();
        List<UserPopulation> candidatesUserPopulations = new ArrayList<>();
        
        // First step will determine the possible UPs and CPs
        boolean stop = _determineCandidatesCredentialProviders(redirector, request, context, candidatesUserPopulations, candidatesCredentialProviders);
        if (!stop)
        {
            // Second step will determine the exact CP to use for authenticating the user and execute it
            stop = _determineAndExecuteExactCP(redirector, request, candidatesUserPopulations, candidatesCredentialProviders);
        }
        
        return stop;
    }
    
    /**
     * First step of the authentication process.
     * Determines what are the candidates credential providers, from a context.
     * @param redirector The redirector
     * @param request The request
     * @param context The context
     * @param userPopulations The candidates user populations
     * @param credentialProviders The candidates credential providers
     * @return true if the user is authenticated
     * @throws AccessDeniedException If the access to the user is denied.
     * @throws ProcessingException If an error occurs during processing the request.
     * @throws IOException If an I/O error occurs.
     */
    private boolean _determineCandidatesCredentialProviders(Redirector redirector, Request request, String context, List<UserPopulation> userPopulations, List<CredentialProvider> credentialProviders) throws AccessDeniedException, ProcessingException, IOException
    {
        Set<String> upIds = _getUserPopulationsOnContext(context);
        for (String upId : upIds)
        {
            userPopulations.add(_userPopulationDAO.getUserPopulation(upId));
        }
        
        String submittedPopulationId = request.getParameter(SUBMITTED_POPULATION_PARAMETER_NAME);
        
        // Did the user already select his population ?
        if (submittedPopulationId != null)
        {
            UserPopulation userPopulation = _userPopulationDAO.getUserPopulation(submittedPopulationId);
            if (userPopulation != null && userPopulations.contains(userPopulation))
            {
                // The UP is now known, only keep this one
                userPopulations.removeIf(up -> !userPopulation.equals(up));
                
                // The CPs are not known, there are multiple candidates
                credentialProviders.addAll(userPopulation.getCredentialProviders());
                return false;
            }
            else
            {
                // Wrong submitted population id
                request.setAttribute(REQUEST_INVALID_POPULATION, "true");
            }
        }
        
        if (userPopulations.size() == 0)
        {
            throw new AccessDeniedException();
        }
        else if (userPopulations.size() == 1 || userPopulations.stream().map(UserPopulation::getCredentialProviders).distinct().count() == 1)
        {
            // Number of UP = 1 => The UP is known and the CPs are not known, there are multiple candidates
            // or
            // The populations have the same CP => The UP is not known, there are multiple candidates and the CPs are the ones of the first population (equal to the ones of the second population, etc.)
            
            credentialProviders.addAll(userPopulations.get(0).getCredentialProviders());
            return false;
        }
        else
        {
            _askUserHisPopulation(redirector, request, _ametysPublic, userPopulations);
            return true;
        }
    }
    
    /**
     * Gets the user populations linked to the given context
     * @param context The context
     * @return The ids of populations linked to the context
     */
    protected Set<String> _getUserPopulationsOnContext(String context)
    {
        return _populationContextHelper.getUserPopulationsOnContext(context);
    }
    
    /**
     * Redirect to a page which asks the user what is his population
     * @param redirector The redirector
     * @param request The request
     * @param ametysPublic True if the instance of Ametys is public.
     * @param candidatesUserPopulations The possible user populations 
     * in a combobox (otherwise, the user will have to type the user population in a textarea field)
     * @throws ProcessingException If an error occurs during processing the request.
     * @throws IOException If an I/O error occurs.
     */
    private void _askUserHisPopulation(Redirector redirector, Request request, Boolean ametysPublic, List<UserPopulation> candidatesUserPopulations) throws ProcessingException, IOException
    {
        request.setAttribute(REQUEST_AMETYS_PUBLIC, ametysPublic);
        if (ametysPublic)
        {
            request.setAttribute(REQUEST_POPULATIONS, candidatesUserPopulations);
        }
        else
        {
            request.setAttribute(REQUEST_POPULATIONS, Collections.EMPTY_LIST);
        }
        
        // Screen WAYF (Where Are You From ?) with the list of populations to select
        redirector.redirect(false, __REDIRECT_URL_LOGIN_SCREEN);
    }

    /**
     * Second step of the authentication process.
     * Determines what is the exact credential provider to use for authenticating the user.
     * @param redirector The redirector
     * @param request The request
     * @param userPopulations The candidates user populations
     * @param credentialProviders The candidates credential providers
     * @return True if the user is authenticated or to stop the action
     * @throws Exception If an error occurs.
     */
    private boolean _determineAndExecuteExactCP(Redirector redirector, Request request, List<UserPopulation> userPopulations, List<CredentialProvider> credentialProviders) throws Exception
    {
        String submittedIndex = request.getParameter(SUBMITTED_CP_INDEX_PARAMETER_NAME);
        boolean wasSubmitted = false;
        
        // Did the user already select the Credential Provider he wants to use ?
        if (submittedIndex != null)
        {
            // The user submitted the index among the blocking ones only, not among all candidates !
            wasSubmitted = true;
        }
        
        // Execute the non-blocking CPs
        List<CredentialProvider> blockingCP = new ArrayList<>();
        Iterator<CredentialProvider> it = credentialProviders.iterator();
        while (it.hasNext())
        {
            CredentialProvider currentCp = it.next();
            if (currentCp instanceof BlockingCredentialProvider)
            {
                blockingCP.add(currentCp);
            }
            
            if (!wasSubmitted && currentCp instanceof NonBlockingCredentialProvider)
            {
                if (_checkAuth(request, redirector, currentCp, false, userPopulations))
                {
                    // The execution of the CredentialProvider succeeded, the user is authenticated.
                    return true;
                }
            }
        }
        
        if (wasSubmitted)
        {
            BlockingCredentialProvider submittedCP = (BlockingCredentialProvider) blockingCP.get(Integer.parseInt(submittedIndex));
            return _execute(redirector, request, blockingCP, submittedCP, userPopulations);
        }
        
        // Each NonBlockingCredentialProvider did not succeed => the remaining candidates are only blocking CPs
        
        // Is there only one CP left ?
        if (blockingCP.size() == 1)
        {
            BlockingCredentialProvider cp = (BlockingCredentialProvider) blockingCP.get(0);
            return _execute(redirector, request, blockingCP, cp, userPopulations);
        }
        else if (blockingCP.stream().filter(cp -> cp instanceof FormCredentialProvider).count() == 1) // there is one and only one Form
        {
            FormCredentialProvider formBasedCp = (FormCredentialProvider) blockingCP.stream().filter(cp -> cp instanceof FormCredentialProvider).findAny().get();
            
            // This is a particular case when we have at least two Credential Providers whose
            // one and only one is a Form
            // In this case, we wish we display the authentication form with a link towards the other available CPs (ex: Google, Facebook...) for each one.
            int indexForm = blockingCP.indexOf(formBasedCp);
            blockingCP.remove(formBasedCp); // we do not want a link to connect with the form !
            _checkFormAuth(request, redirector, formBasedCp, indexForm, blockingCP, userPopulations);
            return true;
        }
        else
        {
            request.setAttribute(REQUEST_CHOOSE_CP_LIST, blockingCP);
            if (userPopulations.size() == 1)
            {
                request.setAttribute(REQUEST_POPULATIONS, userPopulations);
            }
            
            // Screen with the list of CP to select
            redirector.redirect(false, __REDIRECT_URL_LOGIN_SCREEN);
            return true;
        }
    }
    
    /**
     * Once the exact CP to use is determined, execute it.
     * @param redirector The redirector
     * @param request The request
     * @param candidatesCredentialProviders The list of all candidates credential providers
     * @param credentialProvider The credential provider to execute
     * @param candidatesUserPopulations The candidates user populations
     * @return True if the user is authenticated or to stop the action
     * @throws Exception If an error occurs.
     */
    private boolean _execute(Redirector redirector, Request request, List<CredentialProvider> candidatesCredentialProviders, BlockingCredentialProvider credentialProvider, List<UserPopulation> candidatesUserPopulations) throws Exception
    {
        if (credentialProvider instanceof FormCredentialProvider)
        {
            int indexForm = candidatesCredentialProviders.indexOf(credentialProvider);
            return _checkFormAuth(request, redirector, (FormCredentialProvider) credentialProvider, indexForm, Collections.EMPTY_LIST, candidatesUserPopulations);
        }
        else
        {
            // If true is returned, the execution of the CredentialProvider succeeded, and the user is authenticated.
            return _checkAuth(request, redirector, credentialProvider, true, candidatesUserPopulations);
        }
    }
    
    /**
     * Checks the authentication of a {@link FormCredentialProvider}
     * @param request The request
     * @param redirector The redirector
     * @param formCP The form Credential Provider
     * @param indexForm The index of the FormCredentialProvider under its parent population
     * @param linkedCP The {@link CredentialProvider}s we want to eventually use for authenticate.
     * @param candidatesUserPopulations The possible user populations
     * @return True to stop the action or if the user is authenticated
     * @throws Exception If an error occurs.
     */
    private boolean _checkFormAuth(Request request, Redirector redirector, FormCredentialProvider formCP, int indexForm, List<CredentialProvider> linkedCP, List<UserPopulation> candidatesUserPopulations) throws Exception
    {
        request.setAttribute(REQUEST_FORM_BASED_CREDENTIAL_PROVIDER, formCP);
        request.setAttribute(REQUEST_POPULATIONS, candidatesUserPopulations);            
        request.setAttribute(REQUEST_CHOOSE_CP_LIST, linkedCP);
        request.setAttribute(REQUEST_INDEX_FORM_CP, indexForm);
        request.setAttribute(REQUEST_AMETYS_PUBLIC, _ametysPublic);
        
        if ("true".equals(request.getAttribute(REQUEST_INVALID_POPULATION)))
        {
            // do not let the user authenticate if we asked his population and he gives an invalid one
            return _checkAuth(request, redirector, formCP, true, Collections.EMPTY_LIST);
        }
        else
        {
            return _checkAuth(request, redirector, formCP, true, candidatesUserPopulations);
        }
    }

    /**
     * Process the actual authentication.
     * @param request The request
     * @param redirector the Cocoon redirector
     * @param credentialProvider The credential provider to execute
     * @param blocking true to process the Credential Provider as a {@link BlockingCredentialProvider}, false to process it as a {@link NonBlockingCredentialProvider}
     * @param userPopulations The user populations the user might come from.
     * @return true if the current user is authenticated, false otherwise
     * @throws Exception if an error occurs during the authentication process
     */
    protected boolean _checkAuth(Request request, Redirector redirector, CredentialProvider credentialProvider, boolean blocking, List<UserPopulation> userPopulations) throws Exception
    {
        boolean isValid = _validate(credentialProvider, blocking, redirector);
        
        if (redirector.hasRedirected())
        {
            return true;
        }
        
        boolean accept = _accept(credentialProvider, blocking);
        if (accept)
        {
            // The request does not need authentication, don't ask for credentials
            return true;
        }
        
        if (isValid)
        {
            // If user already registered, accept it.
            String authenticatedUser = null;
            Session session = request.getSession(false);
            
            if (session != null)
            {
                authenticatedUser = (String) session.getAttribute(SESSION_USERIDENTITY);
            }

            if (authenticatedUser != null)
            {
                // User already authenticated
                return true;
            }
        }
        
        // Unknown user, looking for his credentials
        Credentials credentials = _getCredentials(credentialProvider, blocking, redirector);
        if (redirector.hasRedirected())
        {
            return true;
        }
        
        if (credentials == null)
        {
            _notAllowed(credentialProvider, blocking, redirector);
            if (redirector.hasRedirected())
            {
                return true;
            }
            
            return false;
        }

        UserPopulation userPopulation = _determinePopulation(userPopulations, credentials);
        if (userPopulation == null)
        {
            _notAllowed(credentialProvider, blocking, redirector);
            if (redirector.hasRedirected())
            {
                return true;
            }
            return false;
        }

        /* FIXME
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
        */

        // The user must be known by the UserManager
        User user = _userManager.getUser(userPopulation, credentials.getLogin());
        if (user == null)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("The user '" + credentials.getLogin() + "' was authentified and authorized by authentications, but it can not be found by the user manager. Access to the application is therefore denied.");
            }
            
            return false;
        }

        // Authentication succeeded
        _allowed(credentialProvider, blocking, redirector);

        // Then register the user in the HTTP Session
        Session session = request.getSession(true);
        UserIdentity identity = user.getIdentity();
        session.setAttribute(SESSION_USERIDENTITY, identity);
        session.setAttribute(SESSION_CREDENTIALPROVIDER, credentialProvider);
        
        return true;
    }
    
    private UserPopulation _determinePopulation(List<UserPopulation> userPopulations, Credentials credentials)
    {
        for (UserPopulation up : userPopulations)
        {
            if (_userManager.getUser(up, credentials.getLogin()) != null && _login(credentials, up))
            {
                return up;
            }
        }
        
        return null;
    }
    
    /**
     * Check if a User can log in. This method is not in charge to check the
     * presence and to get the user in the base user.
     * 
     * @param credentials Contains user information (id, password and realm)
     * @param userPopulation The user population where to check
     * @return true if the user can login
     */
    private boolean _login(Credentials credentials, UserPopulation userPopulation)
    {
        // If the credentials come from a SSO (like CAS), they are already authenticated : grant access.
        if (credentials instanceof TokenCredentials)
        {
            return ((TokenCredentials) credentials).checkToken();
        }
        
        // Do authenticate encrypting password
        for (UserDirectory userDirectory : userPopulation.getUserDirectories())
        {
            if (userDirectory.checkCredentials(credentials))
            {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean _validate(CredentialProvider credentialProvider, boolean blocking, Redirector redirector) throws Exception
    {
        return blocking ? ((BlockingCredentialProvider) credentialProvider).validateBlocking(redirector) : ((NonBlockingCredentialProvider) credentialProvider).validateNonBlocking(redirector);
    }
    
    private boolean _accept(CredentialProvider credentialProvider, boolean blocking)
    {
        return blocking ? ((BlockingCredentialProvider) credentialProvider).acceptBlocking() : ((NonBlockingCredentialProvider) credentialProvider).acceptNonBlocking();
    }
    
    private Credentials _getCredentials(CredentialProvider credentialProvider, boolean blocking, Redirector redirector) throws Exception
    {
        return blocking ? ((BlockingCredentialProvider) credentialProvider).getCredentialsBlocking(redirector) : ((NonBlockingCredentialProvider) credentialProvider).getCredentialsNonBlocking(redirector);
    }
    
    private void _notAllowed(CredentialProvider credentialProvider, boolean blocking, Redirector redirector) throws Exception
    {
        if (blocking)
        {
            ((BlockingCredentialProvider) credentialProvider).notAllowedBlocking(redirector);
        }
        else
        {
            ((NonBlockingCredentialProvider) credentialProvider).notAllowedNonBlocking(redirector);
        }
    }
    
    private void _allowed(CredentialProvider credentialProvider, boolean blocking, Redirector redirector)
    {
        if (blocking)
        {
            ((BlockingCredentialProvider) credentialProvider).allowedBlocking(redirector);
        }
        else
        {
            ((NonBlockingCredentialProvider) credentialProvider).allowedNonBlocking(redirector);
        }
    }
}
