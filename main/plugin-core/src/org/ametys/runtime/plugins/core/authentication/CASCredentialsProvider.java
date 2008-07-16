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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;

import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.authentication.CredentialsProvider;
import org.ametys.runtime.authentication.filter.RuntimeFilter;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.user.UserHelper;
import org.ametys.runtime.util.LoggerFactory;

import edu.yale.its.tp.cas.client.filter.CASFilter;

/**
 * This manager gets the credentials given by an authentification CAS filter.
 * <br>
 * The filter must set the 'remote user' header into the request. <br>
 * <br>
 * This manager can not get the password of the connected user: the user is
 * already authentified. This manager should not be associated with an
 * <code>AuthenticableBaseUser</code>
 */
public class CASCredentialsProvider implements CredentialsProvider, Initializable, Configurable, Contextualizable
{
    // Logger for traces
    private static Logger _logger = LoggerFactory.getLoggerFor(CASCredentialsProvider.class);

    private Map<String, RuntimeFilter> _filters;

    // Cas server URL with context (https://cas-server ou https://cas-server/cas)
    private String _serverUrl;
    
    //Using gateway feature. 
    private boolean _gateway;
    
    // The avalon context
    private Context _context;
    
    // A whitespace-delimited list of valid proxy URLs.
    // Only one URL needs to match for the login to be successful. 
    private String _authorizedProxy;
    
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
        _filters = new HashMap<String, RuntimeFilter>();
        
        _serverUrl = Config.getInstance().getValueAsString("runtime.authentication.cas.serverUrl");
        
        _authorizedProxy = Config.getInstance().getValueAsString("runtime.authentication.cas.authorizedProxy");
        if (_authorizedProxy == null || _authorizedProxy.trim().length() == 0)
        {
            _authorizedProxy = "";
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
        
        RuntimeFilter runtimeFilter = _filters.get(serverName);
        if (runtimeFilter == null)
        {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("edu.yale.its.tp.cas.client.filter.loginUrl", _serverUrl + "/login");
            parameters.put("edu.yale.its.tp.cas.client.filter.validateUrl", _serverUrl + "/proxyValidate");
            parameters.put("edu.yale.its.tp.cas.client.filter.serverName", serverName.toString());
            parameters.put("edu.yale.its.tp.cas.client.filter.authorizedProxy", _authorizedProxy);
            parameters.put("edu.yale.its.tp.cas.client.filter.gateway", String.valueOf(_gateway));

            runtimeFilter = new RuntimeFilter(new CASFilter());
            runtimeFilter.init(parameters, objectModel);
            _filters.put(serverName.toString(), runtimeFilter);
        }

        runtimeFilter.doFilter(objectModel, redirector);

        String userLoginObject = (String) request.getSession().getAttribute(CASFilter.CAS_FILTER_USER);
        String connectedLogin = UserHelper.getCurrentUser(objectModel);
        return (userLoginObject != null) && userLoginObject.equals(connectedLogin);
    }

    public boolean accept()
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        Request request = ObjectModelHelper.getRequest(objectModel);
        Object userLoginObject = request.getSession().getAttribute(CASFilter.CAS_FILTER_USER);

        if (_gateway && userLoginObject == null)
        {
            return true;
        }
        
        return false;
    }

    public Credentials getCredentials(Redirector redirector) throws Exception
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        Object userLoginObject = ObjectModelHelper.getRequest(objectModel).getSession().getAttribute(CASFilter.CAS_FILTER_USER);

        if (userLoginObject == null)
        {
            if (!_gateway)
            {
                String errorMessage = "CAS authentication needs a CAS filter to be configured into the WEB-INF/web.xml file. Please see documentation for more details. It is recommendanded to use the filter: " + CASFilter.class.getName();
                _logger.error(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
            else
            {
                return null;
            }
        }

        String userLogin = userLoginObject.toString();

        return new Credentials(userLogin, "");
    }

    public void notAllowed(Redirector redirector) throws Exception
    {
        // nothing to do
    }

    public void allowed(Redirector redirector)
    {
        // empty method, nothing more to do
    }
}
