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

import java.util.Map;
import java.util.StringTokenizer;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.commons.codec.binary.Base64;

import org.ametys.core.authentication.AbstractCredentialProvider;
import org.ametys.core.authentication.BlockingCredentialProvider;
import org.ametys.core.authentication.Credentials;
import org.ametys.runtime.authentication.AuthorizationRequiredException;

/**
 * Basic http authentication based on AuthenticationManager.
 */
public class BasicCredentialProvider extends AbstractCredentialProvider implements BlockingCredentialProvider, Contextualizable
{
    /** Name of the parameter holding the authentication realm */
    protected static final String __PARAM_REALM = "runtime.authentication.basic.realm";
    
    private static final String BASIC_AUTHENTICATION_KEY = "BASIC ";

    /** The realm */
    protected String _realm;
    
    private Context _context;

    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void init(String cpModelId, Map<String, Object> paramValues)
    {
        super.init(cpModelId, paramValues);
        _realm = (String) paramValues.get(__PARAM_REALM);
    }
    
    @Override
    public boolean validateBlocking(Redirector redirector) throws Exception
    {
        // this manager is always valid
        return true;
    }
    
    @Override
    public boolean acceptBlocking()
    {
        // this implementation does not have any particular request
        // to take into account
        return false;
    }

    @Override
    public Credentials getCredentialsBlocking(Redirector redirector) throws Exception
    {
        Map objectModel = ContextHelper.getObjectModel(_context);
        
        // Check authentication header
        String auth = ObjectModelHelper.getRequest(objectModel).getHeader("Authorization");

        if (auth == null)
        {
            // no auth
            return null;
        }

        if (!auth.toUpperCase().startsWith(BASIC_AUTHENTICATION_KEY))
        {
            // we only do BASIC
            return null;
        }

        // Get encoded user and password, comes after "BASIC "
        String userpassEncoded = auth.substring(BASIC_AUTHENTICATION_KEY.length());

        // Decode it, using any base 64 decoder
        String userpassDecoded = new String(Base64.decodeBase64(userpassEncoded.getBytes()));

        // Login and password are separated with a :
        StringTokenizer stk = new StringTokenizer(userpassDecoded, ":");
        String log = stk.hasMoreTokens() ? stk.nextToken() : "";
        String pass = stk.hasMoreTokens() ? stk.nextToken() : "";

        return new Credentials(log, pass);
    }

    @Override
    public void notAllowedBlocking(Redirector redirector) throws Exception
    {
        throw new AuthorizationRequiredException(_realm);
    }

    @Override
    public void allowedBlocking(Redirector redirector)
    {
        // empty method, nothing more to do
    }

}
