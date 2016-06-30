/*
 *  Copyright 2009 Anyware Services
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
package org.ametys.runtime.plugins.core.group.ldap;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import org.ametys.runtime.group.Group;


/**
 * Use a ldap server for getting the group of users.<br>
 * Groups are branch.
 */
public class GroupDrivenLdapGroupsManager extends AbstractLDAPGroupsManager implements Component
{
    /** Pattern to retrieve user's login from DN */
    protected Pattern _loginExtractionPattern;
    
    /** The attribut which contains the member DN */
    protected String _groupsMemberAttribute;
    /** Relative DN for users. */
    protected String _usersRelativeDN;
    /** The users's id in the DN of a user. */
    protected String _usersLoginAttribute;

    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);

        _usersRelativeDN = _getConfigParameter(configuration, "UsersRelativeDN");
        _usersLoginAttribute = _getConfigParameter(configuration, "Login");
        _groupsMemberAttribute = _getConfigParameter(configuration, "Member");
        
        _loginExtractionPattern = Pattern.compile("^" + _usersLoginAttribute + "=([^,]+)(,.*)?");
    }
    
    public Group getGroup(String groupID)
    {
        Group group = null;

        DirContext context = null;
        NamingEnumeration results = null;
        
        try
        {
            // Connexion au serveur ldap
            context = new InitialDirContext(_getContextEnv());
            
            // Créer le filtre de recherche
            StringBuffer filter = new StringBuffer("(&");
            filter.append(_groupsObjectFilter);
            filter.append("(");
            filter.append(_groupsIdAttribute);
            filter.append("={0}))");

            // Effectuer la recherche
            results = context.search(_groupsRelativeDN, filter.toString(),
                                     new Object[] {groupID}, _getSearchConstraint());
            
            // Vérifier si un groupe corresond
            if (results.hasMoreElements())
            {
                // Récupérer le groupe trouvé
                group = _getUserGroup((SearchResult) results.nextElement());
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

        // Retourner le groupe ou null
        return group;
    }

    public Set<Group> getGroups()
    {
        // Créer un ensemble de groupes
        Set<Group> groups = new TreeSet<Group>(new Comparator<Group>()
        {
            public int compare(Group g1, Group g2) 
            {
                if (g1.getId().equals(g2.getId()))
                {
                    return 0;
                }
                
                // Case insensitive sort
                int compareTo = g1.getLabel().toLowerCase().compareTo(g2.getLabel().toLowerCase());
                if (compareTo == 0)
                {
                    return g1.getId().compareTo(g2.getId());
                }
                return compareTo;
            }
        });

        DirContext context = null;
        NamingEnumeration results = null;
        
        try
        {
            // Connexion au serveur ldap
            context = new InitialDirContext(_getContextEnv());

            // Effectuer la recherche
            results = context.search(_groupsRelativeDN, _groupsObjectFilter, _getSearchConstraint());
            while (results.hasMoreElements())
            {
                // Ajouter un nouveau groupe à l'ensemble
                groups.add(_getUserGroup((SearchResult) results.nextElement()));
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
        // d'UserGroup, éventuellement vide
        return groups;
    }

    public Set<String> getUserGroups(String login)
    {
        Set<String> groups = new HashSet<String>();

        DirContext context = null;
        NamingEnumeration results = null;
        
        try
        {
            // Connexion au serveur ldap
            context = new InitialDirContext(_getContextEnv());
            
            // Créer le filtre de recherche
            StringBuffer filter = new StringBuffer("(&");
            filter.append(_groupsObjectFilter);
            filter.append("(|(");
            filter.append(_groupsMemberAttribute);
            filter.append("=" + _usersLoginAttribute + "={0},");
            filter.append(_usersRelativeDN + (_usersRelativeDN.length() > 0 && _ldapBaseDN.length() > 0 ? "," : "") + _ldapBaseDN);
            filter.append(")(");
            filter.append(_groupsMemberAttribute);
            filter.append("=" + _usersLoginAttribute + "={0}");
            filter.append(")))");

            // Effectuer la recherche
            results = context.search(_groupsRelativeDN, filter.toString(),
                                     new Object[] {login}, _getSearchConstraint());
            
            // Remplir l'ensemble des groupes
            while (results.hasMoreElements())
            {
                // Récupérer le groupe trouvé
                groups.add(_getGroupID((SearchResult) results.nextElement()));
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

        // Retourner les groupes, éventuellement vide
        return groups;
    }
    
    /**
     * Get a group id from attributes of a ldap entry.
     * @param entry The ldap entry to get attributes from.
     * @return The group id as a String.
     * @throws IllegalArgumentException If a needed attribute is missing.
     */
    protected String _getGroupID(SearchResult entry)
    {
        // Récupérer les attributs de l'entrée
        Attributes attrs = entry.getAttributes();
        
        try
        {
            // Récupérer l'identifiant d'un groupe
            Attribute groupIDAttr = attrs.get(_groupsIdAttribute);
            if (groupIDAttr == null)
            {
                throw new IllegalArgumentException("Missing group id attribute : \"" + _groupsIdAttribute + "\"");
            }
            
            return (String) groupIDAttr.get();
        }
        catch (NamingException e)
        {
            throw new IllegalArgumentException("Missing at least one value for an attribute in an ldap entry", e);
        }
    }

    /**
     * Get an UserGroup from attributes of a ldap entry.
     * @param entry The ldap entry to get attributes from.
     * @return The group as an UserGroup.
     * @throws IllegalArgumentException If a needed attribute is missing.
     */
    protected Group _getUserGroup(SearchResult entry)
    {
        Group group = null;
        // Récupérer les attributs de l'entrée
        Attributes attrs = entry.getAttributes();
        
        try
        {
            // Récupérer l'identifiant d'un groupe
            Attribute groupIDAttr = attrs.get(_groupsIdAttribute);
            if (groupIDAttr == null)
            {
                throw new IllegalArgumentException("Missing group id attribute : \"" + _groupsIdAttribute + "\"");
            }
            String groupID = (String) groupIDAttr.get();
            
            // Récupérer la description d'un groupe
            Attribute groupDESCAttr = attrs.get(_groupsDescriptionAttribute);
            if (groupDESCAttr == null)
            {
                throw new IllegalArgumentException("Missing group description attribute : \"" + _groupsDescriptionAttribute + "\"");
            }
            String groupDesc = (String) groupDESCAttr.get();

            group = new Group(groupID, groupDesc);
            
            // Récupérer l'identifiant d'un groupe
            Attribute membersAttr = attrs.get(_groupsMemberAttribute);
            if (membersAttr != null)
            {
                // Récupérer les membres du groupe
                NamingEnumeration members = membersAttr.getAll();
                while (members.hasMore())
                {
                    String userDN = (String) members.next();
                    
                    // Récuperer le login
                    Matcher matcher = _loginExtractionPattern.matcher(userDN);
                    if (matcher.matches())
                    {
                        // Ajouter le login de l'utilisateur courant
                        group.addUser(matcher.group(1));
                    }
                    else
                    {
                        if (getLogger().isWarnEnabled())
                        {
                            getLogger().warn("Unable to get the uid from the LDAP RDN entry : " + userDN);
                        }
                    }
                }
                
                members.close();
            }
        }
        catch (NamingException e)
        {
            throw new IllegalArgumentException("Missing at least one value for an attribute in an ldap entry", e);
        }
        
        return group;
    }
    
    /**
     * Get constraints for a search.
     * @return The constraints as a SearchControls.
     */
    protected SearchControls _getSearchConstraint()
    {
        // Paramètres de recherche
        SearchControls constraints = new SearchControls();
        
        // Un seul attribut à récupérer
        constraints.setReturningAttributes(new String [] {_groupsIdAttribute, _groupsDescriptionAttribute, _groupsMemberAttribute});
        // Choisir la profondeur de la recherche
        constraints.setSearchScope(_groupsSearchScope);
        return constraints;
    }
}
