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

import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;

/**
 * Check the connection to a LDAP directory
 *
 */
public class LDAPConnectionChecker extends AbstractLogEnabled implements ParameterChecker
{
    @Override
    public void check(List<String> values) throws ParameterCheckerTestFailureException
    {
        Hashtable<String, String> env = new Hashtable<>();
        
        // Get the parameter values
        String baseUrl = values.get(0);
        String authMethod = values.get(1);
        String adminDN = values.get(2);
        String adminPassword = values.get(3);
        String useSSL = values.get(4);
        String followReferrals = values.get(5);
        String baseDN = values.get(6);
        
        // Define the corresponding context
        env.put(Context.PROVIDER_URL, baseUrl);
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, authMethod);
        
        if (authMethod.equals("simple"))
        {
            env.put(Context.SECURITY_PRINCIPAL, adminDN);
            env.put(Context.SECURITY_CREDENTIALS, adminPassword);
        }
        if (useSSL.equals("true"))
        {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        if (followReferrals.equals("true"))
        {
            env.put(Context.REFERRAL, "follow");
        }
        
        DirContext context = null;
        try
        {
            // Try and connect
            context = new InitialDirContext(env);
            
            // Check base DN
            context.search(baseDN, null);
        }
        catch (NamingException e)
        {
            throw new ParameterCheckerTestFailureException(e);
        }
        finally
        {
            // Close environment
            if (context != null)
            {
                try
                {
                    context.close();
                }
                catch (NamingException e)
                {
                    getLogger().error("Closing the LDAP connection during test failed.", e);
                }
            }
        }
    }
}
