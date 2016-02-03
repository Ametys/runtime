package org.ametys.plugins.core.impl.checker;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.ametys.core.datasource.LDAPDataSourceManager;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;

/**
 * Check the connection to a LDAP directory
 *
 */
public class LDAPConnectionChecker extends AbstractLogEnabled implements ParameterChecker
{
    @Override
    public void check(Map<String, String> parameters) throws ParameterCheckerTestFailureException
    {
        Hashtable<String, String> env = new Hashtable<>();
        
        // Get the parameter values
        String baseUrl = parameters.get(LDAPDataSourceManager.PARAM_BASE_URL);
        String authMethod = parameters.get(LDAPDataSourceManager.PARAM_AUTHENTICATION_METHOD);
        String adminDN = parameters.get(LDAPDataSourceManager.PARAM_ADMIN_DN);
        String adminPassword = parameters.get(LDAPDataSourceManager.PARAM_ADMIN_PASSWORD);
        String useSSL = parameters.get(LDAPDataSourceManager.PARAM_USE_SSL);
        String followReferrals = parameters.get(LDAPDataSourceManager.PARAM_FOLLOW_REFERRALS);
        String baseDN = parameters.get(LDAPDataSourceManager.PARAM_BASE_DN);
        
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
