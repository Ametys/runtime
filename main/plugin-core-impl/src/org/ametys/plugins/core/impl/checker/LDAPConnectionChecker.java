/*
 *  Copyright 2014 Anyware Services
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
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.ametys.core.parameter.checker.ParameterCheckerTestFailureException;
import org.ametys.runtime.parameter.ParameterChecker;

/**
 * Checks if the connection to the LDAP is possible and then verifies the DN. 
 */
public class LDAPConnectionChecker extends AbstractLogEnabled implements ParameterChecker, Configurable
{
    private String _paramBaseDN;
    private String _paramFollowReferrals;
    private String _paramBaseUrl;
    private String _paramAuthMethod;
    private String _paramAdminDN;
    private String _paramAdminPasswd;
    private String _paramUseSSL;

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        Configuration[] config = configuration.getChild("linked-params").getChildren();
        if (config.length != 7)
        {
            throw new ConfigurationException("The LDAPConnectionChecker should have 7 linked params in the right order: baseUrl, useSSL, baseDN, authMethod, adminDN, adminPassws, followReferrals");
        }
        
        int i = 0;
        _paramBaseUrl = config[i++].getAttribute("id");
        _paramUseSSL = config[i++].getAttribute("id");
        _paramBaseDN = config[i++].getAttribute("id");
        _paramAuthMethod = config[i++].getAttribute("id");
        _paramAdminDN = config[i++].getAttribute("id");
        _paramAdminPasswd = config[i++].getAttribute("id");
        _paramFollowReferrals = config[i++].getAttribute("id");
    }
    
    @Override
    public void check(Map<String, String> configurationParameters) throws ParameterCheckerTestFailureException
    {
        Hashtable<String, String> env = new Hashtable<>();
        
        // Get the parameter values
        String baseUrl = configurationParameters.get(_paramBaseUrl);
        String authMethod = configurationParameters.get(_paramAuthMethod);
        String adminDN = configurationParameters.get(_paramAdminDN);
        String adminPassword = configurationParameters.get(_paramAdminPasswd);
        String useSSL = configurationParameters.get(_paramUseSSL);
        String followReferrals = configurationParameters.get(_paramFollowReferrals);
        String baseDN = configurationParameters.get(_paramBaseDN);
        
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
