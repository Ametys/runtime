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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.datasource.AbstractDataSourceManager.DataSourceDefinition;
import org.ametys.core.datasource.LDAPDataSourceManager;
import org.ametys.core.util.ldap.ScopeEnumerator;
import org.ametys.runtime.parameter.ParameterChecker;
import org.ametys.runtime.parameter.ParameterCheckerTestFailureException;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Tests the LDAP user directory is not empty
 */
public class LdapUserDirectoryChecker extends AbstractLogEnabled implements ParameterChecker, Serviceable
{
    /** The service manager */
    private ServiceManager _manager;
    
    /** The LDAP data source manager */
    private LDAPDataSourceManager _ldapDataSourceManager;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _manager = manager;
    }
    
    @Override
    public void check(List<String> values) throws ParameterCheckerTestFailureException
    {
        if (_ldapDataSourceManager == null)
        {
            try
            {
                _ldapDataSourceManager = (LDAPDataSourceManager) _manager.lookup(LDAPDataSourceManager.ROLE);
            }
            catch (ServiceException e)
            {
                throw new ParameterCheckerTestFailureException("The test cannot be tested now", e);
            }
        }
        
        String datasourceId = values.get(0);
        String usersRelativeDN = values.get(1);
        String usersObjectFilter = values.get(2);
        int usersSearchScope = ScopeEnumerator.parseScope(values.get(3));
        String usersLoginAttribute = values.get(4);
        String usersFirstnameAttribute = values.get(5);
        if (usersFirstnameAttribute != null && usersFirstnameAttribute.length() == 0)
        {
            usersFirstnameAttribute = null;
        }
        String usersLastnameAttribute = values.get(6);
        String usersEmailAttribute = values.get(7);
        boolean userEmailIsMandatory = "true".equals(values.get(8));
        
        DataSourceDefinition ldapDefinition = _ldapDataSourceManager.getDataSourceDefinition(datasourceId);
        if (ldapDefinition == null)
        {
            throw new ParameterCheckerTestFailureException ("Unable to find the data source definition for the id '" + datasourceId + "'.");
        }
        else
        {
            // Search some users
            LdapContext context = null;
            NamingEnumeration<SearchResult> results = null;
    
            try
            {
                // Connection to the LDAP server
                context = new InitialLdapContext(_getContextEnv(ldapDefinition), null);
    
                // Execute ldap search
                results = context.search(usersRelativeDN, 
                                            usersObjectFilter, 
                                            new Object[0], 
                                            _getSearchConstraint(0, usersFirstnameAttribute, usersLoginAttribute, usersLastnameAttribute, usersEmailAttribute, usersSearchScope));
                
                boolean userFound = false;
                while (results.hasMoreElements() && !userFound)
                {
                    SearchResult result = results.nextElement();
                    Map<String, Object> attrs = _getAttributes(result, usersLoginAttribute, usersFirstnameAttribute, usersLastnameAttribute, usersEmailAttribute, userEmailIsMandatory);
                    if (attrs != null)
                    {
                        // a user was found
                        userFound = true;
                    }
                }
                
                if (!userFound)
                {
                    throw new ParameterCheckerTestFailureException("The LDAP repository does not return any user with the given parameters.");
                }
            }
            catch (IllegalArgumentException | NamingException e)
            {
                throw new ParameterCheckerTestFailureException(e);
            }
            finally
            {
                // Close connections
                try
                {
                    _cleanup(context, results);
                }
                catch (NamingException e)
                {
                    getLogger().error("Cleaning the LDAP connection during test failed.", e);
                    throw new ParameterCheckerTestFailureException(e);
                }
            }
        }
    }
    
    private Hashtable<String, String> _getContextEnv(DataSourceDefinition ldapDefinition)
    {
        Map<String, String> ldapParameters = ldapDefinition.getParameters();
        
        String ldapUrl = ldapParameters.get(LDAPDataSourceManager.PARAM_BASE_URL);
        String ldapBaseDN = ldapParameters.get(LDAPDataSourceManager.PARAM_BASE_DN);
        String ldapAdminRelativeDN = ldapParameters.get(LDAPDataSourceManager.PARAM_ADMIN_DN);
        String ldapAdminPassword = ldapParameters.get(LDAPDataSourceManager.PARAM_ADMIN_PASSWORD);
        String ldapAuthenticationMethod = ldapParameters.get(LDAPDataSourceManager.PARAM_AUTHENTICATION_METHOD);
        boolean ldapUseSSL = "true".equals(ldapParameters.get(LDAPDataSourceManager.PARAM_USE_SSL));
        boolean ldapFollowReferrals = "true".equals(ldapParameters.get(LDAPDataSourceManager.PARAM_FOLLOW_REFERRALS));
        String ldapAliasDerefMode = ldapParameters.get(LDAPDataSourceManager.PARAM_ALIAS_DEREFERENCING);
        
        Hashtable<String, String> env = new Hashtable<>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl + "/" + ldapBaseDN);
        env.put(Context.SECURITY_AUTHENTICATION, ldapAuthenticationMethod);

        if (!ldapAuthenticationMethod.equals("none"))
        {
            env.put(Context.SECURITY_PRINCIPAL, ldapAdminRelativeDN);
            env.put(Context.SECURITY_CREDENTIALS, ldapAdminPassword);
        }

        if (ldapUseSSL)
        {
            // Encrypt the connection to the server with SSL
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        
        // Default is to ignore.
        if (ldapFollowReferrals)
        {
            env.put(Context.REFERRAL, "follow");
        }
        else
        {
            env.put(Context.REFERRAL, "ignore");
        }
        
        env.put("java.naming.ldap.derefAliases", ldapAliasDerefMode);
        
        // Use ldap pool connection
        env.put("com.sun.jndi.ldap.connect.pool", "true");

        return env;
    }
    
    private SearchControls _getSearchConstraint(int maxResults, String usersFirstnameAttribute, String usersLoginAttribute, String usersLastnameAttribute, String usersEmailAttribute, int usersSearchScope)
    {
        // Search parameters
        SearchControls constraints = new SearchControls();
        int attributesCount = 4;
        int index = 0;

        if (usersFirstnameAttribute == null)
        {
            attributesCount--;
        }

        // Position the wanted attributes
        String[] attrs = new String[attributesCount];

        attrs[index++] = usersLoginAttribute;
        if (usersFirstnameAttribute != null)
        {
            attrs[index++] = usersFirstnameAttribute;
        }
        attrs[index++] = usersLastnameAttribute;
        attrs[index++] = usersEmailAttribute;

        constraints.setReturningAttributes(attrs);

        // Choose depth of search
        constraints.setSearchScope(usersSearchScope);
        
        if (maxResults > 0)
        {
            constraints.setCountLimit(maxResults);
        }

        return constraints;
    }
    
    private Map<String, Object> _getAttributes(SearchResult entry, String usersLoginAttribute, String usersFirstnameAttribute, String usersLastnameAttribute, String usersEmailAttribute, boolean userEmailIsMandatory) throws NamingException
    {
        Map<String, Object> result = new HashMap<>();

        // Retrieve the entry attributes
        Attributes attrs = entry.getAttributes();

        // Retrieve the login
        Attribute ldapAttr = attrs.get(usersLoginAttribute);
        if (ldapAttr == null)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Missing login attribute : '{}'", usersLoginAttribute);
            }
            return null;
        }

        result.put(usersLoginAttribute, ldapAttr.get());

        if (usersFirstnameAttribute != null)
        {
            // Retrieve the first name
            ldapAttr = attrs.get(usersFirstnameAttribute);
            if (ldapAttr == null)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("Missing firstname attribute : '{}', for user '{}'.", usersFirstnameAttribute, result.get(usersLoginAttribute));
                }
                return null;
            }

            result.put(usersFirstnameAttribute, ldapAttr.get());
        }

        // Retrieve the last name
        ldapAttr = attrs.get(usersLastnameAttribute);
        if (ldapAttr == null)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Missing lastname attribute : '{}', for user '{}'.", usersLastnameAttribute, result.get(usersLoginAttribute));
            }
            return null;
        }

        result.put(usersLastnameAttribute, ldapAttr.get());

        // Retrieve the email
        ldapAttr = attrs.get(usersEmailAttribute);
        if (ldapAttr == null && userEmailIsMandatory)
        {
            if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Missing email attribute : '{}', for user '{}'.", usersEmailAttribute, result.get(usersLoginAttribute));
            }
            return null;
        }

        if (ldapAttr == null)
        {
            result.put(usersEmailAttribute, "");
        }
        else
        {
            result.put(usersEmailAttribute, ldapAttr.get());
        }

        return result;
    }
    
    private void _cleanup(Context context, NamingEnumeration result) throws NamingException
    {
        if (result != null)
        {
            // Fermer le result
            result.close();
        }
        if (context != null)
        {
            // Fermer la connexion au serveur
            context.close();
        }
    }

}
