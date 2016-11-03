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
package org.ametys.plugins.core.impl.checker;

import java.io.IOException;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.Oid;

import org.ametys.plugins.core.impl.authentication.KerberosCredentialProvider;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * This checks that the parameters are the one of a Kerberos server
 */
public class KerberosChecker extends AbstractLogEnabled implements ParameterChecker, Contextualizable
{
    /** The avalon context */
    protected Context _context;

    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    public void check(List<String> values) throws ParameterCheckerTestFailureException
    {
        String kdc = values.get(0);
        String realm = values.get(1);
        String login = values.get(2);
        String password = values.get(3);
        
        try
        {
            LoginContext loginContext = KerberosCredentialProvider.createLoginContext(kdc, realm, login, password, _context);

            final Subject subject = loginContext.getSubject(); 
            
            GSSManager manager = GSSManager.getInstance();
            
            PrivilegedExceptionAction<GSSCredential> action = new PrivilegedExceptionAction<GSSCredential>() 
            {
                public GSSCredential run() throws GSSException 
                {
                    return manager.createCredential(null, GSSCredential.INDEFINITE_LIFETIME, new Oid("1.3.6.1.5.5.2"), GSSCredential.INITIATE_ONLY);
                } 
            };
            
            GSSCredential gssCredential = Subject.doAs(loginContext.getSubject(), action);
            final GSSContext gssContext = GSSManager.getInstance().createContext(gssCredential);

            // The GSS context initiation has to be performed as a privileged action.
            byte[] serviceTicket = Subject.doAs(subject, new PrivilegedAction<byte[]>() 
            {
                public byte[] run()
                {
                    try
                    {
                        byte[] token = new byte[0];
                        // This is a one pass context initialization.
                        gssContext.requestMutualAuth(false);
                        gssContext.requestCredDeleg(false);
                        return gssContext.initSecContext(token, 0, token.length);
                    }
                    catch (GSSException e)
                    {
                        throw new ParameterCheckerTestFailureException("aaa (" + e.getMessage() + ")", e);
                    }
                }
            });

            System.out.println(serviceTicket);
        }
        catch (IOException | LoginException | ContextException | GSSException | PrivilegedActionException e)
        {
            throw new ParameterCheckerTestFailureException("Unable to connect to the KDC (" + e.getMessage() + ")", e);
        }
    }
}
