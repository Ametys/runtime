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
package org.ametys.plugins.core.impl.user.directory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.SortControl;

import org.apache.avalon.framework.component.Component;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.core.util.ldap.AbstractLDAPConnector;
import org.ametys.core.util.ldap.ScopeEnumerator;

/**
 * Use an ldap directory for getting the list of users and also authenticating
 * them.<br>
 */
public class LdapUserDirectory extends AbstractLDAPConnector implements UserDirectory, Component
{
    /** Name of the parameter holding the datasource id */
    protected static final String __PARAM_DATASOURCE_ID = "runtime.users.ldap.datasource";
    /** Relative DN for users. */
    protected static final String __PARAM_USERS_RELATIVE_DN = "runtime.users.ldap.peopleDN";
    /** Filter for limiting the search. */
    protected static final String __PARAM_USERS_OBJECT_FILTER = "runtime.users.ldap.baseFilter";
    /** The scope used for search. */
    protected static final String __PARAM_USERS_SEARCH_SCOPE = "runtime.users.ldap.scope";
    /** Name of the login attribute. */
    protected static final String __PARAM_USERS_LOGIN_ATTRIBUTE = "runtime.users.ldap.loginAttr";
    /** Name of the first name attribute. */
    protected static final String __PARAM_USERS_FIRSTNAME_ATTRIBUTE = "runtime.users.ldap.firstnameAttr";
    /** Name of the last name attribute. */
    protected static final String __PARAM_USERS_LASTNAME_ATTRIBUTE = "runtime.users.ldap.lastnameAttr";
    /** Name of the email attribute. */
    protected static final String __PARAM_USERS_EMAIL_ATTRIBUTE = "runtime.users.ldap.emailAttr";
    /** To know if email is a mandatory attribute */
    protected static final String __PARAM_USERS_EMAIL_IS_MANDATORY = "runtime.users.ldap.emailMandatory";
    /** True to sort the results on the server side, false to get the results unsorted. */
    protected static final String __PARAM_SERVER_SIDE_SORTING = "runtime.users.ldap.serverSideSorting";

    /** Relative DN for users. */
    protected String _usersRelativeDN;
    /** Filter for limiting the search. */
    protected String _usersObjectFilter;
    /** The scope used for search. */
    protected int _usersSearchScope;
    /** Name of the login attribute. */
    protected String _usersLoginAttribute;
    /** Name of the first name attribute. */
    protected String _usersFirstnameAttribute;
    /** Name of the last name attribute. */
    protected String _usersLastnameAttribute;
    /** Name of the email attribute. */
    protected String _usersEmailAttribute;
    /** To know if email is a mandatory attribute */
    protected boolean _userEmailIsMandatory;
    /** True to sort the results on the server side, false to get the results unsorted. */
    protected boolean _serverSideSorting;
    
    private String _udModelId;
    private Map<String, Object> _paramValues;
    private String _populationId;
    
    @Override
    public void init(String udModelId, Map<String, Object> paramValues) throws Exception
    {
        _udModelId = udModelId;
        _paramValues = paramValues;
        
        _usersRelativeDN = (String) paramValues.get(__PARAM_USERS_RELATIVE_DN);
        _usersObjectFilter = (String) paramValues.get(__PARAM_USERS_OBJECT_FILTER);
        _usersSearchScope = ScopeEnumerator.parseScope((String) paramValues.get(__PARAM_USERS_SEARCH_SCOPE));
        _usersLoginAttribute = (String) paramValues.get(__PARAM_USERS_LOGIN_ATTRIBUTE);
        
        _usersFirstnameAttribute = (String) paramValues.get(__PARAM_USERS_FIRSTNAME_ATTRIBUTE);
        if (_usersFirstnameAttribute != null && _usersFirstnameAttribute.length() == 0)
        {
            _usersFirstnameAttribute = null;
        }
        
        _usersLastnameAttribute = (String) paramValues.get(__PARAM_USERS_LASTNAME_ATTRIBUTE);
        _usersEmailAttribute = (String) paramValues.get(__PARAM_USERS_EMAIL_ATTRIBUTE);
        _userEmailIsMandatory = (Boolean) paramValues.get(__PARAM_USERS_EMAIL_IS_MANDATORY);
        _serverSideSorting = (Boolean) paramValues.get(__PARAM_SERVER_SIDE_SORTING);
        
        String dataSourceId = (String) paramValues.get(__PARAM_DATASOURCE_ID);
        _delayedInitialize(dataSourceId);
    }
    
    @Override
    public void setPopulationId(String populationId)
    {
        _populationId = populationId;
    }
    
    @Override
    public String getPopulationId()
    {
        return _populationId;
    }
    
    @Override
    public Map<String, Object> getParameterValues()
    {
        return _paramValues;
    }
    
    @Override
    public String getUserDirectoryModelId()
    {
        return _udModelId;
    }
    
    @Override
    public Collection<User> getUsers()
    {
        // Create a users list
        List<User> users = new ArrayList<>();
        
        DirContext context = null;
        NamingEnumeration<SearchResult> results = null;

        try
        {
            // Connection to the LDAP server
            context = new InitialDirContext(_getContextEnv());

            // Execute ldap search
            results = context.search(_usersRelativeDN, _usersObjectFilter, _getSearchConstraint(0));

            // Get users from Ldap
            while (results.hasMoreElements())
            {
                Map<String, Object> attributes = _getAttributes(results.nextElement());
                if (attributes != null)
                {
                    // Create user
                    User user = _createUser (attributes);
                    
                    if (isCacheEnabled())
                    {
                        addObjectInCache(user.getIdentity().getLogin(), user);
                    }
                    
                    users.add(user);
                }
            }
        }
        catch (IllegalArgumentException e)
        {
            getLogger().error("Error missing at least one attribute or attribute value", e);
        }
        catch (NamingException e)
        {
            getLogger().error("Error communication with ldap server", e);
        }
        finally
        {
            // Close connections
            _cleanup(context, results);
        }

        // Return the users list as a users collection (may be empty)
        return users;
    }

    @Override
    public List<User> getUsers(int count, int offset, Map<String, Object> parameters)
    {
        String pattern = (String) parameters.get("pattern");
        if (StringUtils.isEmpty(pattern))
        {
            pattern = null;
        }
        
        if (count != 0)
        {
            Map<String, Map<String, Object>> entries = new LinkedHashMap<>();
            return _internalGetUsers(entries, count, offset >= 0 ? offset : 0, pattern, 0);
        }
        return new ArrayList<>();
    }

    @Override
    public User getUser(String login)
    {
        if (isCacheEnabled())
        {
            User user = (User) getObjectFromCache(login);
            if (user != null)
            {
                return user;
            }
        }
        
        User principal = null;

        DirContext context = null;
        NamingEnumeration<SearchResult> results = null;

        try
        {
            // Connection to the LDAP server
            Hashtable<String, String> contextEnv = _getContextEnv();
            
            // For AD with weird references
            if (!_ldapFollowReferrals)
            {
                contextEnv.put(javax.naming.Context.REFERRAL, "throw");
            }

            context = new InitialDirContext(contextEnv);

            // Escape the login and create the search filter
            String filter = "(&" + _usersObjectFilter + "(" + _usersLoginAttribute + "={0}))";
            Object[] params = new Object[] {login};

            // Execute ldap search
            results = context.search(_usersRelativeDN, filter, params, _getSearchConstraint(0));

            // Search the user
            if (results.hasMore())
            {
                Map<String, Object> attributes = _getAttributes(results.next());
                if (attributes != null)
                {
                    // Add a new user to the list
                    principal = _createUser (attributes);
                }
                
                // Test if the enumeration has more results with hasMoreElements to avoid unnecessary logs.
                if (results.hasMoreElements())
                {
                    // Cancel the result because there are several matches for one login
                    principal = null;
                    getLogger().error("Multiple matches for attribute '{}' and value = '{}'", _usersLoginAttribute, login);
                }
            }

            if (isCacheEnabled())
            {
                addObjectInCache(login, principal);
            }
        }
        catch (IllegalArgumentException e)
        {
            getLogger().error("Error missing at least one attribute or attribute value for login '" + login + "'", e);
        }
        catch (PartialResultException e)
        {
            if (_ldapFollowReferrals)
            {
                getLogger().debug("Error communicating with ldap server retrieving user with login '" + login + "'", e);
            }
            else
            {
                getLogger().error("Error communicating with ldap server retrieving user with login '" + login + "'", e);
            }
        }
        catch (NamingException e)
        {
            getLogger().error("Error communicating with ldap server retrieving user with login '" + login + "'", e);
        }

        finally
        {
            // Close connections
            _cleanup(context, results);
        }

        // Return the users or null
        return principal;
    }

    @Override
    public boolean checkCredentials(String login, String password)
    {
        boolean authenticated = false;

        // Check password is not empty
        if (StringUtils.isNotEmpty(password))
        {
            // Retrieve user DN
            String userDN = getUserDN(login);
            if (userDN != null)
            {
                DirContext context = null;

                // Retrieve connection parameters
                Hashtable<String, String> env = _getContextEnv();

                // Edit DN and password for authentication
                env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "simple");
                env.put(javax.naming.Context.SECURITY_PRINCIPAL, userDN);
                env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);

                try
                {
                    // Connection and authentication to LDAP server
                    context = new InitialDirContext(env);
                    // Authentication succeeded
                    authenticated = true;
                }
                catch (AuthenticationException e)
                {
                    if (getLogger().isInfoEnabled())
                    {
                        getLogger().info("Authentication failed", e);
                    }
                }
                catch (NamingException e)
                {
                    // Error
                    getLogger().error("Error communication with ldap server", e);
                }
                finally
                {
                    // Close connections
                    _cleanup(context, null);
                }
            }
        }
        else if (getLogger().isDebugEnabled())
        {
            getLogger().debug("LDAP Authentication failed since no password (or an empty one) was given");
        }

        // If an error happened, do not authenticate the user
        return authenticated;
    }
    
    /**
     * Get the distinguished name of an user by his login.
     * @param login Login of the user.
     * @return The dn of the user, or null if there is no match or if multiple
     *         matches.
     */
    protected String getUserDN(String login)
    {
        String userDN = null;
        DirContext context = null;
        NamingEnumeration<SearchResult> results = null;

        try
        {
            // Connection to the LDAP server
            context = new InitialDirContext(_getContextEnv());

            // Create search filter
            String filter = "(&" + _usersObjectFilter + "(" + _usersLoginAttribute + "={0}))";
            Object[] params = new Object[] {login};

            SearchControls constraints = new SearchControls();
            // Choose depth of parameterized search
            constraints.setSearchScope(_usersSearchScope);
            // Do not ask attributes, we only want the DN
            constraints.setReturningAttributes(new String[] {});

            // Execute ldap search
            results = context.search(_usersRelativeDN, filter, params, constraints);

            // Fill users list
            if (results.hasMore())
            {
                SearchResult result = results.next();

                // Retrieve the DN
                userDN = result.getName();
                if (result.isRelative())
                {
                    // Retrieve the absolute DN 
                    NameParser parser = context.getNameParser("");
                    Name topDN = parser.parse(context.getNameInNamespace());
                    topDN.addAll(parser.parse(_usersRelativeDN));
                    topDN.addAll(parser.parse(userDN));
                    userDN = topDN.toString();
                }
                
                if (results.hasMoreElements())
                {
                    // Cancel the result because there are several matches for one login
                    userDN = null;
                    getLogger().error("Multiple matches for attribute \"{}\" and value = \"{}\"", _usersLoginAttribute, login);
                }
            }
        }
        catch (NamingException e)
        {
            getLogger().error("Error communicating with ldap server retrieving user with login '" + login + "'", e);
        }

        finally
        {
            // Close connections
            _cleanup(context, results);
        }
        return userDN;
    }
    
    /**
     * Create a new user from LDAP attributes
     * @param attributes the LDAP attributes
     * @return the user
     */
    protected User _createUser (Map<String, Object> attributes)
    {
        String login = (String) attributes.get(_usersLoginAttribute);
        String lastName = (String) attributes.get(_usersLastnameAttribute);
        String firstName = _usersFirstnameAttribute != null ? (String) attributes.get(_usersFirstnameAttribute) : null;
        String email = (String) attributes.get(_usersEmailAttribute);

        return new User(new UserIdentity(login, _populationId), lastName, firstName, email, this);
    }
    
    /**
     * Get the user list.
     * @param entries Where to store entries
     * @param count The maximum number of users to sax. Cannot be 0. Can be -1 to all.
     * @param offset The results to ignore
     * @param pattern The pattern to match.
     * @param possibleErrors This number will be added to count to set the max of the request, but count results will still be returned. The difference stands for errors.
     * @return the final offset
     */
    protected List<User> _internalGetUsers(Map<String, Map<String, Object>> entries, int count, int offset, String pattern, int possibleErrors)
    {
        LdapContext context = null;
        NamingEnumeration<SearchResult> results = null;

        try
        {
            // Connection to the LDAP server
            context = new InitialLdapContext(_getContextEnv(), null);
            if (_serverSideSorting)
            {
                context.setRequestControls(_getSortControls());
            }

            Map filter = _getPatternFilter(pattern);

            // Execute ldap search
            results = context.search(_usersRelativeDN, 
                                    (String) filter.get("filter"), 
                                    (Object[]) filter.get("params"), 
                                    _getSearchConstraint(count == -1 ? 0 : (count + offset + possibleErrors)));

            // Sax results
            return _users(entries, count, offset, pattern, results, possibleErrors);
        }
        catch (IllegalArgumentException e)
        {
            getLogger().error("Error missing at least one attribute or value", e);
            return new ArrayList<>();
        }
        catch (NamingException e)
        {
            getLogger().error("Error during the communication with ldap server", e);
            return new ArrayList<>();
        }
        finally
        {
            // Close connections
            _cleanup(context, results);
        }
    }
    
    private List<User> _users(Map<String, Map<String, Object>> entries, int count, int offset, String pattern, NamingEnumeration<SearchResult> results, int possibleErrors)
    {
        int nbResults = 0;
        
        boolean hasMoreElement = results.hasMoreElements();
        
        // First loop on the items to ignore (before the offset)
        while (nbResults < offset && hasMoreElement)
        {
            nbResults++;
            
            // FIXME we should check that this element has really attributes to count it as an real offset
            results.nextElement();

            hasMoreElement = results.hasMoreElements();
        }
        
        // Second loop to work
        while ((count == -1 || entries.size() < count) && hasMoreElement)
        {
            nbResults++;
            
            // Next element
            SearchResult result = results.nextElement();
            Map<String, Object> attrs = _getAttributes(result);
            if (attrs != null)
            {
                entries.put((String) attrs.get(_usersLoginAttribute), attrs);
            }

            hasMoreElement = results.hasMoreElements();
        }


        // If we have less results than expected
        // can be due to errors (null attributes)
        // can be due to max results is less than wanted results
        if (entries.size() < count && nbResults == count + offset + possibleErrors)
        {
            double nbErrors = count + possibleErrors - entries.size();
            double askedResultsSize = possibleErrors + count;
            int newPossibleErrors = Math.max(possibleErrors + count - entries.size(), (int) Math.ceil((nbErrors / askedResultsSize + 1) * nbErrors));
            return _internalGetUsers(entries, count, offset, pattern, newPossibleErrors);
        }
        else
        {
            List<User> users = new ArrayList<>();
            for (Map<String, Object> attributes : entries.values())
            {
                users.add(_entry2User(attributes));
            }
            return users;
        }
    }
    
    /**
     * Get the sort control.
     * @return the sort controls. May be empty if a small error occurs
     */
    protected Control[] _getSortControls()
    {
        try
        {
            SortControl sortControl = new SortControl(new String[] {_usersLastnameAttribute, _usersFirstnameAttribute}, Control.NONCRITICAL);
            return new Control[] {sortControl};
        }
        catch (IOException e)
        {
            getLogger().warn("Cannot sort request on LDAP", e);
            return new Control[0];
        }
    }

    /**
     * Get the filter from a pattern.
     * @param pattern The pattern to match.
     * @return The result as a Map containing the filter and the parameters.
     */
    protected Map<String, Object> _getPatternFilter(String pattern)
    {
        Map<String, Object> result = new HashMap<>();

        // Check if pattern
        if (pattern == null)
        {
            result.put("filter", _usersObjectFilter);
            result.put("params", new Object[0]);
        }
        else
        {
            // Create search filter escaping variables
            StringBuffer filter = new StringBuffer("(&" + _usersObjectFilter + "(|(");
            Object[] params = null;

            if (_usersFirstnameAttribute == null)
            {
                filter.append(_usersLoginAttribute);
                filter.append("=*{0}*)(");
                filter.append(_usersLastnameAttribute);
                filter.append("=*{1}*)(");
                filter.append(_usersEmailAttribute);
                filter.append("=*{2}*)))");
                params = new Object[] {pattern, pattern, pattern};
            }
            else
            {
                filter.append(_usersLoginAttribute);
                filter.append("=*{0}*)(");
                filter.append(_usersFirstnameAttribute);
                filter.append("=*{1}*)(");
                filter.append(_usersLastnameAttribute);
                filter.append("=*{2}*)(");
                filter.append(_usersEmailAttribute);
                filter.append("=*{3}*)))");
                params = new Object[] {pattern, pattern, pattern, pattern};
            }

            result.put("filter", filter.toString());
            result.put("params", params);
        }
        return result;
    }
    
    /**
     * Get constraints for a search.
     * @param maxResults The maximum number of items that will be retrieve (0
     *            means all)
     * @return The constraints as a SearchControls.
     */
    protected SearchControls _getSearchConstraint(int maxResults)
    {
        // Search parameters
        SearchControls constraints = new SearchControls();
        int attributesCount = 4;
        int index = 0;

        if (_usersFirstnameAttribute == null)
        {
            attributesCount--;
        }

        // Position the wanted attributes
        String[] attrs = new String[attributesCount];

        attrs[index++] = _usersLoginAttribute;
        if (_usersFirstnameAttribute != null)
        {
            attrs[index++] = _usersFirstnameAttribute;
        }
        attrs[index++] = _usersLastnameAttribute;
        attrs[index++] = _usersEmailAttribute;

        constraints.setReturningAttributes(attrs);

        // Choose depth of search
        constraints.setSearchScope(_usersSearchScope);
        
        if (maxResults > 0)
        {
            constraints.setCountLimit(maxResults);
        }

        return constraints;
    }
    
    /**
     * Get the User corresponding to an user ldap entry
     * @param attributes The ldap attributes of the entry to sax.
     * @return the JSON representation
     */
    protected User _entry2User(Map<String, Object> attributes)
    {
        if (attributes == null)
        {
            return null;
        }
        
        String login = (String) attributes.get(_usersLoginAttribute);
        String lastName = (String) attributes.get(_usersLastnameAttribute);
        
        String firstName = null;
        if (_usersFirstnameAttribute != null)
        {
            firstName = (String) attributes.get(_usersFirstnameAttribute);
        }
        
        String email = (String) attributes.get(_usersEmailAttribute);

        return new User(new UserIdentity(login, _populationId), lastName, firstName, email, this);
    }
    
    /**
     * Get attributes from a ldap entry.
     * @param entry The ldap entry to get attributes from.
     * @return The attributes in a map.
     * @throws IllegalArgumentException If a needed attribute is missing.
     */
    protected Map<String, Object> _getAttributes(SearchResult entry)
    {
        try
        {
            Map<String, Object> result = new HashMap<>();

            // Retrieve the entry attributes
            Attributes attrs = entry.getAttributes();

            // Retrieve the login
            Attribute ldapAttr = attrs.get(_usersLoginAttribute);
            if (ldapAttr == null)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("Missing login attribute : '{}'", _usersLoginAttribute);
                }
                return null;
            }

            result.put(_usersLoginAttribute, ldapAttr.get());

            if (_usersFirstnameAttribute != null)
            {
                // Retrieve the first name
                ldapAttr = attrs.get(_usersFirstnameAttribute);
                if (ldapAttr == null)
                {
                    if (getLogger().isWarnEnabled())
                    {
                        getLogger().warn("Missing firstname attribute : '{}', for user '{}'.", _usersFirstnameAttribute, result.get(_usersLoginAttribute));
                    }
                    return null;
                }

                result.put(_usersFirstnameAttribute, ldapAttr.get());
            }

            // Retrieve the last name
            ldapAttr = attrs.get(_usersLastnameAttribute);
            if (ldapAttr == null)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("Missing lastname attribute : '{}', for user '{}'.", _usersLastnameAttribute, result.get(_usersLoginAttribute));
                }
                return null;
            }

            result.put(_usersLastnameAttribute, ldapAttr.get());

            // Retrieve the email
            ldapAttr = attrs.get(_usersEmailAttribute);
            if (ldapAttr == null && _userEmailIsMandatory)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("Missing email attribute : '{}', for user '{}'.", _usersEmailAttribute, result.get(_usersLoginAttribute));
                }
                return null;
            }

            if (ldapAttr == null)
            {
                result.put(_usersEmailAttribute, "");
            }
            else
            {
                result.put(_usersEmailAttribute, ldapAttr.get());
            }

            return result;
        }
        catch (NamingException e)
        {
            throw new IllegalArgumentException("Missing at least one value for an attribute in an ldap entry", e);
        }
    }

}
