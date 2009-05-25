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
package org.ametys.runtime.plugins.core.user.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.plugins.core.util.ldap.AbstractLDAPConnector;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;

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
    }
    
    public Collection<User> getUsers()
    {
        // Créer une liste d'utilisateurs
        List<User> users = new ArrayList<User>();
        
        DirContext context = null;
        NamingEnumeration results = null;

        try
        {
            // Connexion au serveur ldap
            context = new InitialDirContext(_getContextEnv());

            // Effectuer la recherche
            results = context.search(_usersRelativeDN, _usersObjectFilter, _getSearchConstraint(0));

            // Remplir la liste des utilisateurs
            while (results.hasMoreElements())
            {
                Map<String, Object> attributs = _getAttributes((SearchResult) results.nextElement());
                if (attributs != null)
                {
                    // Récupérer le nom complet
                    StringBuffer fullname = new StringBuffer();

                    if (_usersFirstnameAttribute != null)
                    {
                        fullname.append(attributs.get(_usersFirstnameAttribute) + " ");
                    }

                    fullname.append(attributs.get(_usersLastnameAttribute));

                    // Ajouter un nouveau principal à la liste
                    User user = new User((String) attributs.get(_usersLoginAttribute), fullname.toString(), (String) attributs.get(_usersEmailAttribute));
                    
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
        NamingEnumeration results = null;

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
                Map<String, Object> attributes = _getAttributes((SearchResult) results.nextElement());
                if (attributes != null)
                {
                    // Récupérer le nom complet
                    StringBuffer fullname = new StringBuffer();

                    if (_usersFirstnameAttribute != null)
                    {
                        fullname.append(attributes.get(_usersFirstnameAttribute) + " ");
                    }

                    fullname.append(attributes.get(_usersLastnameAttribute));

                    // Ajouter un nouveau principal à la liste
                    principal = new User((String) attributes.get(_usersLoginAttribute), fullname.toString(), (String) attributes.get(_usersEmailAttribute));
                }
            }

            if (results.hasMoreElements())
            {
                // Annuler le résultat car plusieurs correspondances pour un login
                principal = null;
                getLogger().error("Multiple matches for attribute '" + _usersLoginAttribute + "' and value = '" + login + "'");
            }

            if (isCacheEnabled())
            {
                addObjectInCache(login, principal);
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

        // Retourner l'utilisateur trouvé ou null
        return principal;
    }
    
    public void saxUser(String login, ContentHandler handler) throws SAXException
    {
        DirContext context = null;
        NamingEnumeration results = null;

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
                SearchResult  result = (SearchResult) results.nextElement();
                _entryToSAX(handler, result);
            }

            if (results.hasMoreElements())
            {
                // Annuler le résultat car plusieurs correspondances pour un login
                String errorMessage = "Multiple matches for attribute '" + _usersLoginAttribute + "' and value = '" + login + "'";
                getLogger().error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
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
     *            <li>"pattern" => The pattern to match (String) or null to get
     *            all the users.
     *            </ul>
     * @throws SAXException If an error occurs while saxing.
     */
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
            int currentOffset = _toSAXInternal(handler, count, offset >= 0 ? offset : 0, pattern, 0);
            if (count == -1 || currentOffset == -1)
            {
                String total = "" + currentOffset;
                XMLUtils.createElement(handler, "total", total);
            }
        }
        XMLUtils.endElement(handler, "users");
    }

    /**
     * Sax LDAP results
     * @param handler The content handler to sax in
     * @param count The count limit (cannot be 0, but can be -1)
     * @param offset The offset
     * @param pattern The pattern
     * @param results The LDAP seach result
     * @param knownErrors To know if correct number is send
     * @return the current offset at the end
     * @throws SAXException if an error occured
     * @throws IllegalArgumentException if an error occured
     */
    private int _sax(ContentHandler handler, int count, int offset, String pattern, NamingEnumeration results, int knownErrors) throws SAXException
    {
        int currentOffset = 0;
        int saxed = 0;
        int errors = 0;
        boolean hasMoreElement;
        
        // First loop on the items to ignore (before the offset)
        hasMoreElement = results.hasMoreElements();
        while (count != -1 && currentOffset < offset && hasMoreElement)
        {
            // Passer à l'entrée suivante
            SearchResult result = (SearchResult) results.nextElement();
            if (_getAttributes(result) != null)
            {
                currentOffset++;
            }
            else
            {
                errors++;
            }
            hasMoreElement = results.hasMoreElements();
        }

        // Second loop on the valuable items (between offset and count)
        while (hasMoreElement && (currentOffset < offset + count || count == -1))
        {
            SearchResult result = (SearchResult) results.nextElement();

            // Saxer l'entrée courante
            if (_entryToSAX(handler, result))
            {
                currentOffset++;
                saxed++;
            }
            else
            {
                errors++;
            }
            hasMoreElement = results.hasMoreElements();
        }

        // Errors have to be repaired
        // we only do that when errors equals exactly the numbers of missing results
        // because this means that more items may pottentially exists
        if (count != -1 && (errors != knownErrors) && ((offset + count + knownErrors) == (currentOffset + errors)))
        {
            saxed += _toSAXInternal(handler, count - saxed, offset + saxed, pattern, errors);
        }
        
        return saxed;
    }

    /**
     * Sax the user list.
     * 
     * @param handler The content handler to sax in.
     * @param count The maximum number of users to sax. Cannot be 0. Can be -1 to all.
     * @param offset The offset to start with, first is 0.
     * @param pattern The pattern to match.
     * @param knownErrors the number of errors detected in a previous loop 
     * @return the final offset
     * @throws SAXException If an error occurs while saxing.
     */
    protected int _toSAXInternal(ContentHandler handler, int count, int offset, String pattern, int knownErrors) throws SAXException
    {
        DirContext context = null;
        NamingEnumeration results = null;

        try
        {
            // Connexion au serveur ldap
            context = new InitialDirContext(_getContextEnv());

            Map filter = _getPatternFilter(pattern);

            // Effectuer la recherche
            results = context.search(_usersRelativeDN, 
                                    (String) filter.get("filter"), 
                                    (Object[]) filter.get("params"), 
                                    _getSearchConstraint(count == -1 ? 0 : (count + knownErrors + offset)));

            // Sax results
            return _sax(handler, count, offset, pattern, results, knownErrors);
        }
        catch (IllegalArgumentException e)
        {
            getLogger().error("Error missing at least one attribute or value", e);
            return -1;
        }
        catch (NamingException e)
        {
            getLogger().error("Error durring the communication with ldap server", e);
            return -1;
        }
        finally
        {
            // Fermer les ressources de connexion
            _cleanup(context, results);
        }
    }

    /**
     * Get the filter from a pattern.
     * 
     * @param pattern The pattern to match.
     * @return The result as a Map containing the filter and the parameters.
     */
    protected Map _getPatternFilter(String pattern)
    {
        Map<String, Object> result = new HashMap<String, Object>();

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
     * Sax an ldap entry.
     * 
     * @param handler The content handler to sax in.
     * @param entry The ldap entry to sax.
     * @return true is the entry is correct
     * @throws IllegalArgumentException If a needed attribute is missing in the entry.
     * @throws SAXException If a problem occurs while saxing.
     */
    protected boolean _entryToSAX(ContentHandler handler, SearchResult entry) throws SAXException
    {
        Map<String, Object> attributes = _getAttributes(entry);
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
            Map<String, Object> result = new HashMap<String, Object>();

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
