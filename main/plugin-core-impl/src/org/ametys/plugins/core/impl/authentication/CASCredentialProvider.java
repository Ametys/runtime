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
package org.ametys.plugins.core.impl.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;

import org.ametys.core.authentication.AbstractCredentialProvider;
import org.ametys.core.authentication.AuthenticateAction;
import org.ametys.core.authentication.BlockingCredentialProvider;
import org.ametys.core.authentication.Credentials;
import org.ametys.core.authentication.NonBlockingCredentialProvider;
import org.ametys.core.authentication.filter.RuntimeFilter;
import org.ametys.core.user.UserIdentity;

/**
 * This manager gets the credentials given by an authentication CAS filter.
 * <br>
 * The filter must set the 'remote user' header into the request. <br>
 * <br>
 * This manager can not get the password of the connected user: the user is
 * already authentified. This manager should not be associated with a
 * <code>UsersManagerAuthentication</code>
 */
public class CASCredentialProvider extends AbstractCredentialProvider implements NonBlockingCredentialProvider, BlockingCredentialProvider, Initializable, Contextualizable
{
    /** Parameter name for server url  */
    private static final String __PARAM_SERVER_URL = "runtime.authentication.cas.serverUrl";
    
    /** Parameter name for authorized proxy chains */
    private static final String __PARAM_AUTHORIZED_PROXY_CHAINS = "runtime.authentication.cas.authorizedProxyChain";
    
    /** Cas server URL with context (https://cas-server ou https://cas-server/cas) */
    protected String _serverUrl;
    
    /** List of filter wrappers for a server. */
    private Map<String, List<RuntimeFilter>> _filters;

    private Context _context;

    /**
     * Authorized proxy chains, which is
     *  a newline-delimited list of acceptable proxy chains.
     *  A proxy chain includes a whitespace-delimited list of valid proxy URLs.
     *  Only one proxy chain needs to match for the login to be successful.
     */
    private String _authorizedProxyChains;
    
    @Override
    public void initialize() throws Exception
    {
        _filters = new HashMap<>();
    }
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void init(String cpModelId, Map<String, Object> paramValues)
    {
        super.init(cpModelId, paramValues);
        _serverUrl = (String) paramValues.get(__PARAM_SERVER_URL);
        _authorizedProxyChains = (String) paramValues.get(__PARAM_AUTHORIZED_PROXY_CHAINS);
    }

    @Override
    public boolean validateBlocking(Redirector redirector) throws Exception
    {
        return _validate(redirector, false);
    }
    
    @Override
    public boolean validateNonBlocking(Redirector redirector) throws Exception
    {
        return _validate(redirector, true);
    }
    
    private boolean _validate(Redirector redirector, boolean gateway) throws Exception
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        Request request = ObjectModelHelper.getRequest(objectModel);
        StringBuffer serverName = new StringBuffer(request.getServerName());
        
        // Build an URI without :80 (http) and without :443 (https)
        if (request.isSecure())
        {
            if (request.getServerPort() != 443)
            {
                serverName.append(":");
                serverName.append(request.getServerPort());
            }
        }
        else
        {
            if (request.getServerPort() != 80)
            {
                serverName.append(":");
                serverName.append(request.getServerPort());
            }
        }
        
        String name = serverName.toString();
        
        List<RuntimeFilter> runtimeFilters = _filters.get(name);
        if (runtimeFilters == null)
        {
            // Create the filter chain.
            runtimeFilters = new ArrayList<>();
            _filters.put(name, runtimeFilters);
            
            ServletContext servletContext = (ServletContext) objectModel.get(HttpEnvironment.HTTP_SERVLET_CONTEXT);
            Map<String, String> parameters = new HashMap<>();
            
            // Authentication filter.
            parameters.put("casServerLoginUrl", _serverUrl + "/login");
            parameters.put("serverName", name);
            parameters.put("gateway", String.valueOf(gateway));
            RuntimeFilter runtimeFilter = new RuntimeFilter(new AuthenticationFilter());
            runtimeFilter.init(parameters, servletContext);
            runtimeFilters.add(runtimeFilter);
            
            // Ticket validation filter.
            parameters.clear();
            parameters.put("casServerUrlPrefix", _serverUrl);
            parameters.put("serverName", name);
            parameters.put("allowedProxyChains", _authorizedProxyChains);
            runtimeFilter = new RuntimeFilter(new Cas20ProxyReceivingTicketValidationFilter());
            runtimeFilter.init(parameters, servletContext);
            runtimeFilters.add(runtimeFilter);
            
            // Ticket validation filter.
            parameters.clear();
            runtimeFilter = new RuntimeFilter(new HttpServletRequestWrapperFilter());
            runtimeFilter.init(parameters, servletContext);
            runtimeFilters.add(runtimeFilter);
        }
        
        getLogger().debug("Executing CAS filter chain...");
        
        // Execute the filter chain.
        for (RuntimeFilter filter : runtimeFilters)
        {
            filter.doFilter(objectModel, redirector);
        }
        
        boolean valid = true;
        
        // If a redirect was sent, the getSession call won't work.
        if (!redirector.hasRedirected())
        {
            Session session = request.getSession(false);
            String userLogin = _getLogin(request);
            String connectedLogin = session == null || session.getAttribute(AuthenticateAction.SESSION_USERIDENTITY) == null ? null : ((UserIdentity) session.getAttribute(AuthenticateAction.SESSION_USERIDENTITY)).getLogin();
            valid = (userLogin != null) && userLogin.equals(connectedLogin);
        }
        
        return valid;
    }

    @Override
    public boolean acceptBlocking()
    {       
        return false;
    }
    
    @Override
    public boolean acceptNonBlocking()
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        Request request = ObjectModelHelper.getRequest(objectModel);
        String userLogin = _getLogin(request);
        
        if (userLogin == null)
        {
            getLogger().debug("Gateway CAS : unauthenticated user, letting him through.");
            return true;
        }
        
        return false;
    }

    @Override
    public Credentials getCredentialsBlocking(Redirector redirector) throws Exception
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        Request request = ObjectModelHelper.getRequest(objectModel);
        String userLogin = _getLogin(request);

        if (userLogin == null)
        {
            String errorMessage = "CAS authentication needs a CAS filter to be configured into the WEB-INF/web.xml file. Please see documentation for more details. It is recommanded to use the filter: {}";
            getLogger().error(errorMessage, AuthenticationFilter.class.getName());
            throw new IllegalStateException(errorMessage);
        }
        
        getLogger().debug("User authenticated by CAS : {}", userLogin);
        
        return new Credentials(userLogin, "");
    }
    
    @Override
    public Credentials getCredentialsNonBlocking(Redirector redirector) throws Exception
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        Request request = ObjectModelHelper.getRequest(objectModel);
        String userLogin = _getLogin(request);

        if (userLogin == null)
        {
            return null;
        }
        
        getLogger().debug("User authenticated by Gateway CAS : {}", userLogin);
        
        return new Credentials(userLogin, "");
    }

    @Override
    public void notAllowedBlocking(Redirector redirector) throws Exception
    {
        // Nothing to do.
    }
    
    @Override
    public void notAllowedNonBlocking(Redirector redirector) throws Exception
    {
        // Nothing to do.
    }

    @Override
    public void allowedBlocking(Redirector redirector)
    {
        // Empty method, nothing more to do.
    }
    
    @Override
    public void allowedNonBlocking(Redirector redirector)
    {
        // Empty method, nothing more to do.
    }
    
    /**
     * Get the connected user login from the request or session.
     * @param request the request object.
     * @return the connected user login or null.
     */
    protected String _getLogin(Request request)
    {
        String userLogin = null;
        
        Session session = request.getSession(false);
        
        final Assertion assertion = (Assertion) (session == null ? request.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION));
        
        if (assertion != null)
        {
            userLogin = assertion.getPrincipal().getName();
        }
        return userLogin;
    }

}
