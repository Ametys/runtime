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
package org.ametys.core.util.ldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.datasource.AbstractDataSourceManager.DataSourceDefinition;
import org.ametys.core.datasource.LDAPDataSourceManager;
import org.ametys.core.datasource.UnknownDataSourceException;
import org.ametys.core.util.CachingComponent;
import org.ametys.runtime.config.Config;

/**
 * This abstract class contains all basic for a ldap connection using config parameters
 */
public abstract class AbstractLDAPConnector extends CachingComponent<Object> implements Serviceable
{
    /** The default LDAP search page size */
    protected static final int __DEFAULT_PAGE_SIZE = 1000;
    
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
    /** Enable following referrals. */
    protected boolean _ldapFollowReferrals;
    /** Alias dereferencing mode. */
    protected String _ldapAliasDerefMode;
    
    /** Indicates if the LDAP server supports paging feature. */
    protected boolean _pagingSupported;

    /** The LDAP data source manager */
    private LDAPDataSourceManager _ldapDataSourceManager;
    
    /**
     * Call this method with the datasource id to initialize this component
     * @param dataSourceId The id of the datasource
     * @throws Exception If an error occurs.
     */
    protected void _delayedInitialize(String dataSourceId) throws Exception
    {
        DataSourceDefinition ldapDefinition = _ldapDataSourceManager.getDataSourceDefinition(dataSourceId);
        if (ldapDefinition != null)
        {
            Map<String, String> ldapParameters = ldapDefinition.getParameters();
            
            _ldapUrl = ldapParameters.get(LDAPDataSourceManager.PARAM_BASE_URL);
            _ldapBaseDN = ldapParameters.get(LDAPDataSourceManager.PARAM_BASE_DN);
            _ldapAdminRelativeDN = ldapParameters.get(LDAPDataSourceManager.PARAM_ADMIN_DN);
            _ldapAdminPassword = ldapParameters.get(LDAPDataSourceManager.PARAM_ADMIN_PASSWORD);
            _ldapAuthenticationMethod = ldapParameters.get(LDAPDataSourceManager.PARAM_AUTHENTICATION_METHOD);
            _ldapUseSSL = "true".equals(ldapParameters.get(LDAPDataSourceManager.PARAM_USE_SSL));
            _ldapFollowReferrals = "true".equals(ldapParameters.get(LDAPDataSourceManager.PARAM_FOLLOW_REFERRALS));
            _ldapAliasDerefMode = ldapParameters.get(LDAPDataSourceManager.PARAM_ALIAS_DEREFERENCING);
            
            _pagingSupported = _testPagingSupported();
        }
        else
        {
            throw new UnknownDataSourceException("The data source of id '" + dataSourceId + "' is still referenced but no longer exists.");
        }
    }
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _ldapDataSourceManager = (LDAPDataSourceManager) serviceManager.lookup(LDAPDataSourceManager.ROLE); 
    }
    
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
     * @throws ConfigurationException if a configuration problem occurs
     */
    protected int _getSearchScope(Configuration configuration, String searchScopeKey) throws ConfigurationException
    {
        String usersSearchScope = _getConfigParameter(configuration, searchScopeKey);
        
        try
        {
            return ScopeEnumerator.parseScope(usersSearchScope);
        }
        catch (IllegalArgumentException e)
        {
            throw new ConfigurationException("Unable to parse scope", e);
        }
    }

    /**
     * Test if paging is supported by the underlying directory server.
     * @return true if the server supports paging.
     */
    public boolean isPagingSupported()
    {
        return _pagingSupported;
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
        Hashtable<String, String> env = new Hashtable<>();

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
            // Encrypt the connection to the server with SSL
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        
        // Default is to ignore.
        if (_ldapFollowReferrals)
        {
            env.put(Context.REFERRAL, "follow");
        }
        else
        {
            env.put(Context.REFERRAL, "ignore");
        }
        
        env.put("java.naming.ldap.derefAliases", _ldapAliasDerefMode);
        
        // Use ldap pool connection
        env.put("com.sun.jndi.ldap.connect.pool", "true");

        return env;
    }
    
    /**
     * Get the parameters for connecting to the ldap server, root DN.
     * @return Parameters for connecting.
     */
    protected Hashtable<String, String> _getRootContextEnv()
    {
        Hashtable<String, String> env = _getContextEnv();
        
        env.put(Context.PROVIDER_URL, _ldapUrl);
        
        return env;
    }
    
    /**
     * Test if paging is supported by the underlying directory server.
     * @return true if the server supports paging.
     */
    protected boolean _testPagingSupported()
    {
        boolean supported = false;
        
        LdapContext context = null;
        NamingEnumeration<SearchResult> results = null;
        
        try
        {
            // Connection to LDAP server
            context = new InitialLdapContext(_getRootContextEnv(), null);
            
            SearchControls controls = new SearchControls();
            controls.setReturningAttributes(new String[]{"supportedControl"});
            controls.setSearchScope(SearchControls.OBJECT_SCOPE);
            
            // Search for the rootDSE object.
            results = context.search("", "(objectClass=*)", controls);
            
            while (results.hasMore() && !supported)
            {
                SearchResult entry = results.next();
                NamingEnumeration<?> attrs = entry.getAttributes().getAll();
                while (attrs.hasMore() && !supported)
                {
                    Attribute attr = (Attribute) attrs.next();
                    NamingEnumeration<?> vals = attr.getAll();
                    while (vals.hasMore() && !supported)
                    {
                        String value = (String) vals.next();
                        if (PagedResultsControl.OID.equals(value))
                        {
                            supported = true;
                        }
                    }
                }
            }
        }
        catch (NamingException e)
        {
            getLogger().warn("Error while testing the LDAP server for paging feature, assuming false.", e);
        }
        finally
        {
            // Close resources of connection
            _cleanup(context, results);
        }
        
        return supported;
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

    /**
     * Executes a LDAP search
     * @param pageSize The number of entries in a page
     * @param name  the name of the context or object to search
     * @param filter the filter expression to use for the search
     * @param searchControls the search controls that control the search.
     * @return The results of the LDAP search
     */
    protected List<SearchResult> _search(int pageSize, String name, String filter, SearchControls searchControls)
    {
        getLogger().error("Je passe dans ma nouvelle méthode");
        List<SearchResult> allResults = new ArrayList<>();
        
        LdapContext context = null;
        NamingEnumeration<SearchResult> tmpResults = null;
        
        try
        {
            // Connect to the LDAP server.
            context = new InitialLdapContext(_getContextEnv(), null);
            byte[] cookie = null;
            
            if (isPagingSupported())
            {
                try
                {
                    context.setRequestControls(new Control[]{new PagedResultsControl(pageSize, Control.NONCRITICAL) });
                }
                catch (IOException ioe)
                {
                    getLogger().error("Error setting the PagedResultsControl in the LDAP context.", ioe);
                }
            }
            do
            {
                // Perform the search
                tmpResults = context.search(name, filter, searchControls);
                
                // Iterate over a batch of search results
                while (tmpResults != null && tmpResults.hasMoreElements())
                {
                    // Retrieve current entry
                    allResults.add(tmpResults.nextElement());
                }
                
                // Examine the paged results control response
                Control[] controls = context.getResponseControls();
                if (controls != null)
                {
                    for (int i = 0; i < controls.length; i++)
                    {
                        if (controls[i] instanceof PagedResultsResponseControl)
                        {
                            PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
                            cookie = prrc.getCookie();
                        }
                    }
                }
                
                // Re-activate paged results
                if (isPagingSupported())
                {
                    try
                    {
                        context.setRequestControls(new Control[]{new PagedResultsControl(pageSize, cookie, Control.NONCRITICAL)});
                    }
                    catch (IOException ioe)
                    {
                        getLogger().error("Error setting the PagedResultsControl in the LDAP context.", ioe);
                    }
                }
                
            }
            while (cookie != null);
        }
        catch (NamingException e)
        {
            getLogger().error("Error communication with ldap server", e);
        }
        finally
        {
            // Close connection resources
            _cleanup(context, tmpResults);
        }
        
        return allResults;
    }
}
