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
package org.ametys.runtime.plugins.core.authentication.mixed;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;

import org.ametys.runtime.authentication.AuthenticateAction;
import org.ametys.runtime.authentication.BasicCredentialsProvider;
import org.ametys.runtime.authentication.Credentials;
import org.ametys.runtime.plugins.core.authentication.CASCredentialsProvider;
import org.ametys.runtime.util.LoggerFactory;

/**
 * CAS credentials provider which uses Basic as fallback :
 * - First, the user is authenticated with CAS. If he's known by CAS (already logged elsewhere), ok.
 * - If he's not already authenticated by CAS, try to authenticate him with a Basic scheme.
 * 
 * Do not use UsersManagerAuthentication with this CredentialsProvider. A special Authentication
 * exists for this purpose, {@link MixedSourceUsersManagerAuthentication}, which tries to
 * authenticate only users with HTTP Basic credentials.
 */
public class CASBasicCredentialsProvider extends CASCredentialsProvider
{
    /** The logger. */
    protected static Logger _logger = LoggerFactory.getLoggerFor(CASBasicCredentialsProvider.class);
    
    /** The avalon context. */
    protected Context _context;
    
    /** The fallback credentials provider. */
    protected BasicCredentialsProvider _fallbackCredentialsProvider;
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        super.contextualize(context);
        _context = context;
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);
        
        // Force gateway mode.
        _gateway = true;
    }
    
    @Override
    public void initialize() throws Exception
    {
        super.initialize();
        
        // Create and initialize the basic credentials provider.
        _fallbackCredentialsProvider = new BasicCredentialsProvider();
        _fallbackCredentialsProvider.contextualize(_context);
        _fallbackCredentialsProvider.initialize();
    }
    
    @Override
    public boolean validate(Redirector redirector) throws Exception
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        Request request = ObjectModelHelper.getRequest(objectModel);
        
        String authenticatedUser = null;
        Session session = request.getSession(false);
        if (session != null)
        {
            authenticatedUser = (String) session.getAttribute(AuthenticateAction.SESSION_USERLOGIN);
        }
        
        boolean valid = _fallbackCredentialsProvider.validate(redirector);
        
        if (authenticatedUser == null)
        {
            valid = valid && super.validate(redirector);
        }
        
        return valid;
    }
    
    @Override
    public boolean accept()
    {
        // Never accept a user : if the user isn't logged into CAS, don't let him pass (even gatewayed) and log him with Basic.
        return false;
    }
    
    @Override
    public Credentials getCredentials(Redirector redirector) throws Exception
    {
        MixedSourceCredentials credentials = null;
        
        Credentials sourceCredentials = super.getCredentials(redirector);
        
        if (sourceCredentials == null)
        {
            sourceCredentials = _fallbackCredentialsProvider.getCredentials(redirector);
            
            if (sourceCredentials != null)
            {
                credentials = new MixedSourceCredentials(sourceCredentials, false);
            }
        }
        else
        {
            credentials = new MixedSourceCredentials(sourceCredentials, true);
        }
        
        return credentials;
    }
    
    @Override
    public void notAllowed(Redirector redirector) throws Exception
    {
        super.notAllowed(redirector);
        if (!redirector.hasRedirected())
        {
            _fallbackCredentialsProvider.notAllowed(redirector);
        }
    }
    
    @Override
    public void allowed(Redirector redirector)
    {
        super.allowed(redirector);
        if (!redirector.hasRedirected())
        {
            _fallbackCredentialsProvider.allowed(redirector);
        }
    }
    
}
