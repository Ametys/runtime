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
package org.ametys.plugins.core.impl.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ametys.core.authentication.AuthenticateAction;
import org.ametys.core.authentication.Credentials;
import org.ametys.core.authentication.CredentialsProvider;
import org.ametys.core.authentication.filter.RuntimeFilter;
import org.ametys.runtime.config.Config;

/**
 * This manager gets the credentials given by an authentication CAS filter.
 * <br>
 * The filter must set the 'remote user' header into the request. <br>
 * <br>
 * This manager can not get the password of the connected user: the user is
 * already authentified. This manager should not be associated with a
 * <code>UsersManagerAuthentication</code>
 */
public class CASCredentialsProvider implements CredentialsProvider, Initializable, Configurable, Contextualizable
{
    /** Logger for traces. */
    private static Logger _logger = LoggerFactory.getLogger(CASCredentialsProvider.class);
    
    /** Using gateway feature. */ 
    protected boolean _gateway;
    
    /** List of filter wrappers for a server. */
    private Map<String, List<RuntimeFilter>> _filters;

    /** Cas server URL with context (https://cas-server ou https://cas-server/cas) */
    private String _serverUrl;
    
    /** The avalon context. */
    private Context _context;
    
    /**
     *  A newline-delimited list of acceptable proxy chains.
     *  A proxy chain includes a whitespace-delimited list of valid proxy URLs.
     *  Only one proxy chain needs to match for the login to be successful.
     */
    private String _authorizedProxyChains;
    
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _gateway = configuration.getChild("gateway").getValueAsBoolean(false);
    }

    public void initialize() throws Exception
    {
        _filters = new HashMap<>();
        
        _serverUrl = Config.getInstance().getValueAsString("runtime.authentication.cas.serverUrl");
        
        _authorizedProxyChains = Config.getInstance().getValueAsString("runtime.authentication.cas.authorizedProxyChain");
        if (_authorizedProxyChains == null || _authorizedProxyChains.trim().length() == 0)
        {
            _authorizedProxyChains = "";
        }
    }

    public boolean validate(Redirector redirector) throws Exception
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        Request request = ObjectModelHelper.getRequest(objectModel);
        StringBuffer serverName = new StringBuffer(request.getServerName());
        
        // Construire une uri sans :80 en http et sans :443 en https
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
            parameters.put("gateway", String.valueOf(_gateway));
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
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug("Executing CAS filter chain...");
        }
        
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
            String connectedLogin = session == null ? null : (String) session.getAttribute(AuthenticateAction.SESSION_USERLOGIN);
            valid = (userLogin != null) && userLogin.equals(connectedLogin);
        }
        
        return valid;
    }

    public boolean accept()
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        Request request = ObjectModelHelper.getRequest(objectModel);
        String userLogin = _getLogin(request);
        
        if (_gateway && userLogin == null)
        {
            if (_logger.isDebugEnabled())
            {
                _logger.debug("Gateway CAS : unauthenticated user, letting him through.");
            }
            return true;
        }
        
        return false;
    }

    public Credentials getCredentials(Redirector redirector) throws Exception
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        Request request = ObjectModelHelper.getRequest(objectModel);
        String userLogin = _getLogin(request);

        if (userLogin == null)
        {
            if (!_gateway)
            {
                String errorMessage = "CAS authentication needs a CAS filter to be configured into the WEB-INF/web.xml file. Please see documentation for more details. It is recommanded to use the filter: " + AuthenticationFilter.class.getName();
                _logger.error(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
            else
            {
                return null;
            }
        }
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug("User authenticated by CAS : " + userLogin);
        }
        
        return new Credentials(userLogin, "");
    }

    public void notAllowed(Redirector redirector) throws Exception
    {
        // Nothing to do.
    }

    public void allowed(Redirector redirector)
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
