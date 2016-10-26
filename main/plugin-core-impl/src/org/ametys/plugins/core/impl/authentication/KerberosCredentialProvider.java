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

import java.io.File;
import java.io.IOException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import org.ametys.core.authentication.AbstractCredentialProvider;
import org.ametys.core.authentication.NonBlockingCredentialProvider;
import org.ametys.core.user.UserIdentity;
import org.ametys.runtime.authentication.AuthorizationRequiredException;
import org.ametys.runtime.util.AmetysHomeHelper;

/**
 * Kerberos http authentication.
 */
public class KerberosCredentialProvider extends AbstractCredentialProvider implements NonBlockingCredentialProvider, Contextualizable
{
    /** Name of the parameter holding the authentication server kdc adress */
    protected static final String __PARAM_KDC = "runtime.authentication.kerberos.kdc";
    /** Name of the parameter holding the authentication server realm */
    protected static final String __PARAM_REALM = "runtime.authentication.kerberos.realm";
    /** Name of the parameter holding the ametys login */
    protected static final String __PARAM_LOGIN = "runtime.authentication.kerberos.login";
    /** Name of the parameter holding the ametys password */
    protected static final String __PARAM_PASSWORD = "runtime.authentication.kerberos.password";
    
    /** Name of the login config file */
    protected static final String __LOGIN_CONF_FILE = "jaas.conf";
    
    private Context _context;
    private GSSCredential _gssCredential;

    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void init(String id, String cpModelId, Map<String, Object> paramValues, String label)
    {
        super.init(id, cpModelId, paramValues, label);

        String kdc = (String) paramValues.get(__PARAM_KDC);
        String realm = (String) paramValues.get(__PARAM_REALM);
        String login = (String) paramValues.get(__PARAM_LOGIN);
        String password = (String) paramValues.get(__PARAM_PASSWORD);
        
        try
        {
            System.setProperty("java.security.krb5.kdc", kdc);
            System.setProperty("java.security.krb5.realm", realm);
            org.apache.cocoon.environment.Context context = (org.apache.cocoon.environment.Context) _context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
            if (System.getProperty("java.security.auth.login.config") == null)
            {
                String jaasConfLocation = context.getRealPath("/WEB-INF/param/" + __LOGIN_CONF_FILE);
                if (!new File(jaasConfLocation).exists())
                {
                    jaasConfLocation = AmetysHomeHelper.getAmetysHomeTmp() + File.pathSeparator + __LOGIN_CONF_FILE;
                    FileUtils.write(new File(jaasConfLocation), "kerberos {\ncom.sun.security.auth.module.Krb5LoginModule required\nstoreKey=true\nisInitiator=false;\n};");
                }
                System.setProperty("java.security.auth.login.config", jaasConfLocation);
            }
            
            LoginContext loginContext = new LoginContext("kerberos", new CallbackHandler()
            {
                public void handle(final Callback[] callbacks)
                {
                    for (Callback callback : callbacks)
                    {
                        if (callback instanceof NameCallback)
                        {
                            ((NameCallback) callback).setName(login);
                        }
                        else if (callback instanceof PasswordCallback)
                        {
                            ((PasswordCallback) callback).setPassword(password.toCharArray());
                        }
                        else
                        {
                            throw new RuntimeException("Invalid callback received during KerberosCredentialProvider initialization");
                        }
                    }
                }
            });
            
            loginContext.login();
            
            GSSManager manager = GSSManager.getInstance();
            
            PrivilegedExceptionAction<GSSCredential> action = new PrivilegedExceptionAction<GSSCredential>() 
            {
                public GSSCredential run() throws GSSException 
                {
                    return manager.createCredential(null, GSSCredential.INDEFINITE_LIFETIME, new Oid("1.3.6.1.5.5.2"), GSSCredential.ACCEPT_ONLY);
                } 
            };
            
            _gssCredential = Subject.doAs(loginContext.getSubject(), action);
        }
        catch (IOException | LoginException | PrivilegedActionException | ContextException e)
        {
            throw new RuntimeException("Unable to initialize the KerberosCredentialProvider", e);
        }
    }
    
    @Override
    public boolean nonBlockingIsStillConnected(UserIdentity userIdentity, Redirector redirector) throws Exception
    {
        // this manager is always valid
        return true;
    }
    
    @Override
    public boolean nonBlockingGrantAnonymousRequest()
    {
        // this implementation does not have any particular request
        // to take into account
        return false;
    }

    @Override
    public UserIdentity nonBlockingGetUserIdentity(Redirector redirector) throws Exception
    {
        Request request = ContextHelper.getRequest(_context);
        
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Negotiate "))
        {
            String negotiateToken = authorization.substring("Negotiate ".length());
            byte[] token = Base64.decodeBase64(negotiateToken);
            GSSContext gssContext = GSSManager.getInstance().createContext(_gssCredential);
            byte[] kdcTokenAnswer = gssContext.acceptSecContext(token, 0, token.length);
            String tokenAnswer = kdcTokenAnswer != null ? Base64.encodeBase64String(kdcTokenAnswer) : null;
            if (!gssContext.isEstablished())
            {
                // Handshake is not over, send new token
                throw new AuthorizationRequiredException(true, tokenAnswer);
            }
            
            if (tokenAnswer != null)
            {
                Response response = ContextHelper.getResponse(_context);
                response.setHeader("WWW-Authenticate", "Negotiate " + tokenAnswer);
            }
            
            GSSName gssSrcName = gssContext.getSrcName();
            if (gssSrcName == null)
            {
                throw new AuthorizationRequiredException(true, null);
            }
            
            String login = gssSrcName.toString();
            // gssSrcName should be <login>@<realm>
            if (login.indexOf('@') > 0)
            {
                login = login.substring(0, login.indexOf('@'));
            }
            
            return new UserIdentity(login, null);
        }
        else
        {
            throw new AuthorizationRequiredException(true, null);
        }
    }

    @Override
    public void nonBlockingUserNotAllowed(Redirector redirector) throws Exception
    {
        // Nothing
    }
    
    @Override
    public void nonBlockingUserAllowed(UserIdentity userIdentity)
    {
        // Nothing
    }

}
