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
package org.ametys.runtime.plugins.core.util.ldap;

import java.util.Hashtable;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.ametys.runtime.config.Config;

/**
 * This abstract class contains all basic for a ldap connection using config parameters
 */
public class AbstractLDAPConnector extends AbstractLogEnabled implements Configurable
{
    // Check filter look
    private static final Pattern __FILTER = Pattern.compile("\\s*\\(.*\\)\\s*");

    /** URL connection to the ldap server. */
    protected String _ldapUrl;
    /** Base DN to the ldap server. */
    protected String _ldapBaseDN;
    /** Distinguished name of the admin used for searching. */
    protected String _ldapAdminRelativeDN;
    /** Password associated with the admin. */
    protected String _ldapAdminPassword;
    /** Authentication method used. */
    protected String _ldapAuthenticationMethod;
    /** Use ssl for connecting to ldap server. */
    protected boolean _ldapUseSSL;
    
    /**
     * Get the filter from configuration key and check it
     * @param configuration The configuration
     * @param filterKey The name of the child in configuration containing the filter config parameter name
     * @return The value of the configured filter
     * @throws ConfigurationException if the filter does not match
     */
    protected String _getFilter(Configuration configuration, String filterKey) throws ConfigurationException
    {
        String filter = _getConfigParameter(configuration, filterKey);
        if (!__FILTER.matcher(filter).matches())
        {
            String message = "Invalid filter '" + filter + "', missing parenthesis";
            throw new ConfigurationException(message, configuration);
        }
        return filter;
    }
    
    /**
     * Get the search scope from configuration key
     * @param configuration The configuration
     * @param searchScopeKey The name of the child in configuration containing the search scop parameter name
     * @return The scope between <code>SearchControls.ONELEVEL_SCOPE</code>, <code>SearchControls.SUBTREE_SCOPE</code> and <code>SearchControls.OBJECT_SCOPE</code>.
     * @throws ConfigurationException
     */
    protected int _getSearchScope(Configuration configuration, String searchScopeKey) throws ConfigurationException
    {
        String usersSearchScope = _getConfigParameter(configuration, searchScopeKey);
        if ("one".equals(usersSearchScope))
        {
            return SearchControls.ONELEVEL_SCOPE;
        }
        else if ("sub".equals(usersSearchScope))
        {
            return SearchControls.SUBTREE_SCOPE;
        }
        else
        {
            return SearchControls.OBJECT_SCOPE;
        }
    }

    public void configure(Configuration configuration) throws ConfigurationException
    {
        _ldapUrl = _getConfigParameter(configuration, "BaseUrl");
        _ldapUseSSL = "true".equals(_getConfigParameter(configuration, "UseSSL"));
        _ldapBaseDN = _getConfigParameter(configuration, "BaseDN");
        
        _ldapAuthenticationMethod = _getConfigParameter(configuration, "AuthenticationMethod");
        if (!_ldapAuthenticationMethod.equals("none"))
        {
            _ldapAdminRelativeDN = _getConfigParameter(configuration, "AdminDN");
            _ldapAdminPassword = _getConfigParameter(configuration, "AdminPassword");
        }

    }
    
    /**
     * Get a config parameter value
     * @param configuration The configuration
     * @param key The child node of configuration containing the config parameter name
     * @return The value (can be null)
     * @throws ConfigurationException if parameter is missing
     */
    protected String _getConfigParameter(Configuration configuration, String key) throws ConfigurationException
    {
        String parameterName = configuration.getChild(key).getValue(null);
        if (parameterName == null)
        {
            String message = "The parameter '" + key + "' is missing";
            getLogger().error(message);
            throw new ConfigurationException(message, configuration);
        }
        
        String valeur = Config.getInstance().getValueAsString(parameterName);
        return valeur;
    }
    
    /**
     * Get the parameters for connecting to the ldap server.
     * 
     * @return Parameters for connecting.
     */
    protected Hashtable<String, String> _getContextEnv()
    {
        Hashtable<String, String> env = new Hashtable<String, String>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, _ldapUrl + "/" + _ldapBaseDN);
        env.put(Context.SECURITY_AUTHENTICATION, _ldapAuthenticationMethod);

        if (!_ldapAuthenticationMethod.equals("none"))
        {
            env.put(Context.SECURITY_PRINCIPAL, _ldapAdminRelativeDN);
            env.put(Context.SECURITY_CREDENTIALS, _ldapAdminPassword);
        }

        if (_ldapUseSSL)
        {
            // Chiffrer la connexion au serveur avec SSL
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }

        // Utiliser le pool de connexion ldap
        env.put("com.sun.jndi.ldap.connect.pool", "true");

        return env;
    }

    /**
     * Clean a connection to an ldap server.
     * 
     * @param context The connection to the database to close.
     * @param result The result to close.
     */
    protected void _cleanup(Context context, NamingEnumeration result)
    {
        if (result != null)
        {
            try
            {
                // Fermer le result
                result.close();
            }
            catch (NamingException e)
            {
                getLogger().error("Error while closing ldap result", e);
            }
        }
        if (context != null)
        {
            try
            {
                // Fermer la connexion au serveur
                context.close();
            }
            catch (NamingException e)
            {
                getLogger().error("Error while closing ldap connection", e);
            }
        }
    }
}
