/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.plugins.core.impl.user.ldap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
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
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.core.user.User;
import org.ametys.core.user.UsersManager;
import org.ametys.core.util.ldap.AbstractLDAPConnector;

/**
 * Use a ldap server for getting the list of users.
 */
public class LdapUsersManager extends AbstractLDAPConnector implements UsersManager, ThreadSafe, Component
{
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

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);
        
        _usersRelativeDN = _getConfigParameter(configuration, "RelativeDN");
        _usersObjectFilter = _getFilter(configuration, "Filter");
        _usersSearchScope = _getSearchScope(configuration, "SearchScope");
        _usersLoginAttribute = _getConfigParameter(configuration, "Login");
        
        _usersFirstnameAttribute = _getConfigParameter(configuration, "Firstname");
        if (_usersFirstnameAttribute != null && _usersFirstnameAttribute.length() == 0)
        {
            _usersFirstnameAttribute = null;
        }
        
        _usersLastnameAttribute = _getConfigParameter(configuration, "Lastname");
        _usersEmailAttribute = _getConfigParameter(configuration, "Email");
        _userEmailIsMandatory = configuration.getChild("Email").getAttributeAsBoolean("mandatory", false);
        _serverSideSorting = !"false".equals(_getConfigParameter(configuration, "ServerSideSorting"));
    }
    
    @Override
    public Collection<User> getUsers()
    {
        // Créer une liste d'utilisateurs
        List<User> users = new ArrayList<>();
        
        DirContext context = null;
        NamingEnumeration<SearchResult> results = null;

        try
        {
            // Connexion au serveur ldap
            context = new InitialDirContext(_getContextEnv());

            // Effectuer la recherche
            results = context.search(_usersRelativeDN, _usersObjectFilter, _getSearchConstraint(0));

            // Remplir la liste des utilisateurs
            while (results.hasMoreElements())
            {
                Map<String, Object> attributes = _getAttributes(results.nextElement());
                if (attributes != null)
                {
                    // Ajouter un nouveau principal à la liste
                    User user = _createUser (attributes);
                    
                    if (isCacheEnabled())
                    {
                        addObjectInCache(user.getName(), user);
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
            // Fermer les ressources de connexion
            _cleanup(context, results);
        }

        // Retourner la liste des utilisateurs sous forme de collection
        // d'utilisateurs, éventuellement vide
        return users;
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
            // Connexion au serveur ldap
            Hashtable<String, String> contextEnv = _getContextEnv();
            
            // For AD with weird references
            if (!_ldapFollowReferrals)
            {
                contextEnv.put(Context.REFERRAL, "throw");
            }

            context = new InitialDirContext(contextEnv);

            // Créer le filtre de recherche en échappant le login
            String filter = "(&" + _usersObjectFilter + "(" + _usersLoginAttribute + "={0}))";
            Object[] params = new Object[] {login};

            // Effectuer la recherche
            results = context.search(_usersRelativeDN, filter, params, _getSearchConstraint(0));

            // Chercher l'utilisateur voulu
            if (results.hasMore())
            {
                Map<String, Object> attributes = _getAttributes(results.next());
                if (attributes != null)
                {
                    // Ajouter un nouveau principal à la liste
                    principal = _createUser (attributes);
                }
                
                // Test if the enumeration has more results with hasMoreElements to avoid unnecessary logs.
                if (results.hasMoreElements())
                {
                    // Annuler le résultat car plusieurs correspondances pour un login
                    principal = null;
                    getLogger().error("Multiple matches for attribute '" + _usersLoginAttribute + "' and value = '" + login + "'");
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
            // Fermer les ressources de connexion
            _cleanup(context, results);
        }

        // Retourner l'utilisateur trouvé ou null
        return principal;
    }
    
    /**
     * Create a new user from LDAP attributes
     * @param attributes the LDAP attributes
     * @return the user
     */
    protected User _createUser (Map<String, Object> attributes)
    {
        // Récupérer le nom complet
        StringBuffer fullname = new StringBuffer();

        if (_usersFirstnameAttribute != null)
        {
            fullname.append(attributes.get(_usersFirstnameAttribute) + " ");
        }

        fullname.append(attributes.get(_usersLastnameAttribute));

        return new User((String) attributes.get(_usersLoginAttribute), fullname.toString(), (String) attributes.get(_usersEmailAttribute));
    }
    
    @Override
    public Map<String, Object> user2JSON(String login)
    {
        DirContext context = null;
        NamingEnumeration<SearchResult> results = null;

        try
        {
            // Connexion au serveur ldap
            context = new InitialDirContext(_getContextEnv());

            // Créer le filtre de recherche en échappant le login
            String filter = "(&" + _usersObjectFilter + "(" + _usersLoginAttribute + "={0}))";
            Object[] params = new Object[] {login};

            // Effectuer la recherche
            results = context.search(_usersRelativeDN, filter, params, _getSearchConstraint(0));

            // Chercher l'utilisateur voulu
            if (results.hasMoreElements())
            {
                SearchResult  result = results.nextElement();
                Map<String, Object> user = _entry2Json(_getAttributes(result));
                
                if (results.hasMoreElements())
                {
                    // Annuler le résultat car plusieurs correspondances pour un login
                    String errorMessage = "Multiple matches for attribute '" + _usersLoginAttribute + "' and value = '" + login + "'";
                    getLogger().error(errorMessage);
                    throw new IllegalArgumentException(errorMessage);
                }
                
                return user;
            }
            
            return null;
        }
        catch (NamingException e)
        {
            getLogger().error("Error communication with ldap server", e);
            throw new RuntimeException("Error communication with ldap server", e);
        }
        finally
        {
            // Fermer les ressources de connexion
            _cleanup(context, results);
        }
    }
    
    @Override
    @Deprecated
    public void saxUser(String login, ContentHandler handler) throws SAXException
    {
        DirContext context = null;
        NamingEnumeration<SearchResult> results = null;

        try
        {
            // Connexion au serveur ldap
            context = new InitialDirContext(_getContextEnv());

            // Créer le filtre de recherche en échappant le login
            String filter = "(&" + _usersObjectFilter + "(" + _usersLoginAttribute + "={0}))";
            Object[] params = new Object[] {login};

            // Effectuer la recherche
            results = context.search(_usersRelativeDN, filter, params, _getSearchConstraint(0));

            // Chercher l'utilisateur voulu
            if (results.hasMoreElements())
            {
                SearchResult  result = results.nextElement();
                _entryToSAX(handler, _getAttributes(result));
                
                if (results.hasMoreElements())
                {
                    // Annuler le résultat car plusieurs correspondances pour un login
                    String errorMessage = "Multiple matches for attribute '" + _usersLoginAttribute + "' and value = '" + login + "'";
                    getLogger().error(errorMessage);
                    throw new IllegalArgumentException(errorMessage);
                }
            }
        }
        catch (NamingException e)
        {
            getLogger().error("Error communication with ldap server", e);
            throw new RuntimeException("Error communication with ldap server", e);
        }
        finally
        {
            // Fermer les ressources de connexion
            _cleanup(context, results);
        }
    }

    /**
     * Sax the user list.
     * 
     * @param handler The content handler to sax in.
     * @param count The maximum number of users to sax. (-1 to sax all) Warning,
     *            if LDAP contains not valuable objects (with missing mandatory
     *            attributes) they will be ignored but the search scope is still
     *            limited to <code>count</code> : in other words you may
     *            receive less than 'count' results even if the ldap contains
     *            more matching entries
     * @param offset The offset to start with, first is 0.
     * @param parameters Parameters containing a pattern to match :
     *            <ul>
     *            <li>"pattern" =gt; The pattern to match (String) or null to get all the users.</li>
     *            </ul>
     * @throws SAXException If an error occurs while saxing.
     */
    @Override
    @Deprecated
    public void toSAX(ContentHandler handler, int count, int offset, Map parameters) throws SAXException
    {
        String pattern = (String) parameters.get("pattern");
        if (pattern != null && pattern.length() == 0)
        {
            pattern = null;
        }

        XMLUtils.startElement(handler, "users");
        if (count != 0)
        {
            Map<String, Map<String, Object>> entries = new LinkedHashMap<>();
            
            int currentOffset = _toSAXInternal(handler, entries, count, offset >= 0 ? offset : 0, pattern, 0);
            if (count == -1 || currentOffset == -1)
            {
                String total = "" + currentOffset;
                XMLUtils.createElement(handler, "total", total);
            }
        }
        XMLUtils.endElement(handler, "users");
    }
    
    public List<Map<String, Object>> users2JSON(int count, int offset, Map parameters)
    {
        String pattern = (String) parameters.get("pattern");
        if (pattern != null && pattern.length() == 0)
        {
            pattern = null;
        }
        
        if (count != 0)
        {
            Map<String, Map<String, Object>> entries = new LinkedHashMap<>();
            
            return _internalUsers2JSON(entries, count, offset >= 0 ? offset : 0, pattern, 0);
        }
        return new ArrayList<>();
    }
    
    /**
     * Sax LDAP results
     * @param handler The content handler to sax in
     * @param entries where to store entries
     * @param count The count limit (cannot be 0, but can be -1)
     * @param offset The offset for starting search
     * @param pattern The pattern
     * @param results The LDAP seach result
     * @param possibleErrors The number of additionnal results that stand for errors
     * @return the current offset at the end
     * @throws SAXException if an error occured
     * @throws IllegalArgumentException if an error occured
     */
    @Deprecated
    private int _sax(ContentHandler handler, Map<String, Map<String, Object>> entries, int count, int offset, String pattern, NamingEnumeration<SearchResult> results, int possibleErrors) throws SAXException
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
            
            // Passer à l'entrée suivante
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
            return _toSAXInternal(handler, entries, count, offset, pattern, newPossibleErrors);
        }
        else
        {
            for (Map<String, Object> attributes : entries.values())
            {
                _entryToSAX(handler, attributes);
            }
            return entries.size();
        }
    }
    
    private List<Map<String, Object>> _json(Map<String, Map<String, Object>> entries, int count, int offset, String pattern, NamingEnumeration<SearchResult> results, int possibleErrors)
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
            
            // Passer à l'entrée suivante
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
            return _internalUsers2JSON(entries, count, offset, pattern, newPossibleErrors);
        }
        else
        {
            List<Map<String, Object>> users = new ArrayList<>();
            for (Map<String, Object> attributes : entries.values())
            {
                users.add(_entry2Json(attributes));
            }
            return users;
        }
    }

    /**
     * Sax the user list.
     * 
     * @param handler The content handler to sax in.
     * @param entries Where to store entries
     * @param count The maximum number of users to sax. Cannot be 0. Can be -1 to all.
     * @param offset The results to ignore
     * @param pattern The pattern to match.
     * @param possibleErrors This number will be added to count to set the max of the request, but count results will still be returned. The difference stands for errors.
     * @return the final offset
     * @throws SAXException If an error occurs while saxing.
     */
    @Deprecated
    protected int _toSAXInternal(ContentHandler handler, Map<String, Map<String, Object>> entries, int count, int offset, String pattern, int possibleErrors) throws SAXException
    {        
        LdapContext context = null;
        NamingEnumeration<SearchResult> results = null;

        try
        {
            // Connexion au serveur ldap
            context = new InitialLdapContext(_getContextEnv(), null);
            if (_serverSideSorting)
            {
                context.setRequestControls(_getSortControls());
            }

            Map filter = _getPatternFilter(pattern);

            // Effectuer la recherche
            results = context.search(_usersRelativeDN, 
                                    (String) filter.get("filter"), 
                                    (Object[]) filter.get("params"), 
                                    _getSearchConstraint(count == -1 ? 0 : (count + offset + possibleErrors)));

            // Sax results
            return _sax(handler, entries, count, offset, pattern, results, possibleErrors);
        }
        catch (IllegalArgumentException e)
        {
            getLogger().error("Error missing at least one attribute or value", e);
            return -1;
        }
        catch (NamingException e)
        {
            getLogger().error("Error during the communication with ldap server", e);
            return -1;
        }
        finally
        {
            // Fermer les ressources de connexion
            _cleanup(context, results);
        }
    }
    
    /**
     * Get the user list.
     * @param entries Where to store entries
     * @param count The maximum number of users to sax. Cannot be 0. Can be -1 to all.
     * @param offset The results to ignore
     * @param pattern The pattern to match.
     * @param possibleErrors This number will be added to count to set the max of the request, but count results will still be returned. The difference stands for errors.
     * @return the final offset
     * @throws SAXException If an error occurs while saxing.
     */
    protected List<Map<String, Object>> _internalUsers2JSON (Map<String, Map<String, Object>> entries, int count, int offset, String pattern, int possibleErrors)
    {
        LdapContext context = null;
        NamingEnumeration<SearchResult> results = null;

        try
        {
            // Connexion au serveur ldap
            context = new InitialLdapContext(_getContextEnv(), null);
            if (_serverSideSorting)
            {
                context.setRequestControls(_getSortControls());
            }

            Map filter = _getPatternFilter(pattern);

            // Effectuer la recherche
            results = context.search(_usersRelativeDN, 
                                    (String) filter.get("filter"), 
                                    (Object[]) filter.get("params"), 
                                    _getSearchConstraint(count == -1 ? 0 : (count + offset + possibleErrors)));

            // Sax results
            return _json(entries, count, offset, pattern, results, possibleErrors);
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
            // Fermer les ressources de connexion
            _cleanup(context, results);
        }
    }
    
    /**
     * Get the sort control.
     * @return the sort controls. May be empty if a small error occurs
     * @throws SAXException if a fatal error occurs
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
     * 
     * @param pattern The pattern to match.
     * @return The result as a Map containing the filter and the parameters.
     */
    protected Map<String, Object> _getPatternFilter(String pattern)
    {
        Map<String, Object> result = new HashMap<>();

        // Vérifier si l'on a un motif
        if (pattern == null)
        {
            result.put("filter", _usersObjectFilter);
            result.put("params", new Object[0]);
        }
        else
        {
            // Créer le filtre de recherche en échappant les variables
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
     * 
     * @param maxResults The maximum number of items that will be retrieve (0
     *            means all)
     * @return The constraints as a SearchControls.
     */
    protected SearchControls _getSearchConstraint(int maxResults)
    {
        // Paramètres de recherche
        SearchControls constraints = new SearchControls();
        int attributesCount = 4;
        int index = 0;

        if (_usersFirstnameAttribute == null)
        {
            attributesCount--;
        }

        // Positionner les attributs voulus
        String[] attrs = new String[attributesCount];

        attrs[index++] = _usersLoginAttribute;
        if (_usersFirstnameAttribute != null)
        {
            attrs[index++] = _usersFirstnameAttribute;
        }
        attrs[index++] = _usersLastnameAttribute;
        attrs[index++] = _usersEmailAttribute;

        constraints.setReturningAttributes(attrs);

        // Choisir la profondeur de la recherche
        constraints.setSearchScope(_usersSearchScope);
        
        if (maxResults > 0)
        {
            constraints.setCountLimit(maxResults);
        }

        return constraints;
    }
    
    /**
     * Get the JSON representation of a user ldap entry
     * @param attributes The ldap attributes of the entry to sax.
     * @return the JSON representation
     */
    protected Map<String, Object> _entry2Json(Map<String, Object> attributes)
    {
        Map<String, Object> user = new HashMap<>();
        
        if (attributes == null)
        {
            return user;
        }

        user.put("login", attributes.get(_usersLoginAttribute));
        
        if (_usersFirstnameAttribute != null)
        {
            String firstName = (String) attributes.get(_usersFirstnameAttribute);
            user.put("firstName", firstName);
        }

        String lastName = (String) attributes.get(_usersLastnameAttribute);
        user.put("lastName", lastName);

        String email = (String) attributes.get(_usersEmailAttribute);
        user.put("email", email);

        return user;
    }

    /**
     * Sax an ldap entry.
     * 
     * @param handler The content handler to sax in.
     * @param attributes The ldap attributes of the entry to sax.
     * @return true is the entry is correct
     * @throws IllegalArgumentException If a needed attribute is missing in the entry.
     * @throws SAXException If a problem occurs while saxing.
     */
    @Deprecated
    protected boolean _entryToSAX(ContentHandler handler, Map<String, Object> attributes) throws SAXException
    {
        if (attributes == null)
        {
            return false;
        }

        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "login", "login", "CDATA", (String) attributes.get(_usersLoginAttribute));
        XMLUtils.startElement(handler, "user", attr);

        if (_usersFirstnameAttribute != null)
        {
            String firstName = (String) attributes.get(_usersFirstnameAttribute);
            XMLUtils.createElement(handler, "firstname", firstName != null ? firstName : "");
        }

        String lastName = (String) attributes.get(_usersLastnameAttribute);
        XMLUtils.createElement(handler, "lastname", lastName != null ? lastName : "");

        String email = (String) attributes.get(_usersEmailAttribute);
        XMLUtils.createElement(handler, "email", email != null ? email : "");

        handler.endElement("", "user", "user");

        return true;
    }

    /**
     * Get attributes from a ldap entry.
     * 
     * @param entry The ldap entry to get attributes from.
     * @return The attributes in a map.
     * @throws IllegalArgumentException If a needed attribute is missing.
     */
    protected Map<String, Object> _getAttributes(SearchResult entry)
    {
        try
        {
            Map<String, Object> result = new HashMap<>();

            // Récupérer les attributs de l'entrée
            Attributes attrs = entry.getAttributes();

            // Récupérer le login
            Attribute ldapAttr = attrs.get(_usersLoginAttribute);
            if (ldapAttr == null)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("Missing login attribute : '" + _usersLoginAttribute + "'");
                }
                return null;
            }

            result.put(_usersLoginAttribute, ldapAttr.get());

            if (_usersFirstnameAttribute != null)
            {
                // Récupérer le prénom
                ldapAttr = attrs.get(_usersFirstnameAttribute);
                if (ldapAttr == null)
                {
                    if (getLogger().isWarnEnabled())
                    {
                        getLogger().warn("Missing firstname attribute : '" + _usersFirstnameAttribute + "', for user '" + result.get(_usersLoginAttribute) + "'.");
                    }
                    return null;
                }

                result.put(_usersFirstnameAttribute, ldapAttr.get());
            }

            // Récupérer le nom de famille
            ldapAttr = attrs.get(_usersLastnameAttribute);
            if (ldapAttr == null)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("Missing lastname attribute : '" + _usersLastnameAttribute + "', for user '" + result.get(_usersLoginAttribute) + "'.");
                }
                return null;
            }

            result.put(_usersLastnameAttribute, ldapAttr.get());

            // Récupérer l'email
            ldapAttr = attrs.get(_usersEmailAttribute);
            if (ldapAttr == null && _userEmailIsMandatory)
            {
                if (getLogger().isWarnEnabled())
                {
                    getLogger().warn("Missing email attribute : '" + _usersEmailAttribute + "', for user '" + result.get(_usersLoginAttribute) + "'.");
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
