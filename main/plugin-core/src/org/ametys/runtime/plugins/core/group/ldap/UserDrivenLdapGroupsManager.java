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
package org.ametys.runtime.plugins.core.group.ldap;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
 * Use a ldap server for getting the group of users<br/>
 * Groups are constructed using attributes on users.
 */
public class UserDrivenLdapGroupsManager extends AbstractLDAPGroupsManager implements Component
{
    /** The attribut which contains the groups of a user */
    protected String _usersMemberOfAttribute;
    /** Relative DN for users. */
    protected String _usersRelativeDN;
    /** Filter for limiting the search. */
    protected String _usersObjectFilter;
    /** The scope used for search. */
    protected int _usersSearchScope;
    /** Name of the login attribute. */
    protected String _usersLoginAttribute;
    
    private Pattern _groupExtractionPattern;
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);
        
        _usersRelativeDN = _getConfigParameter(configuration, "UsersRelativeDN");
        _usersObjectFilter = _getFilter(configuration, "UsersFilter");
        _usersSearchScope = _getSearchScope(configuration, "UsersSearchScope");
        _usersLoginAttribute = _getConfigParameter(configuration, "UsersLogin");
        _usersMemberOfAttribute = _getConfigParameter(configuration, "MemberOf");
        
        _groupExtractionPattern = Pattern.compile("^" + _groupsIdAttribute + "=([^,]+),.*");
    }
    
    public Group getGroup(String groupID)
    {
        for (Group userGroup : getGroups())
        {
            if (userGroup.getId().equals(groupID))
            {
                return userGroup;
            }
        }

        return null;
    }

    public Set<Group> getGroups()
    {
        // Créer un ensemble de groupes
        Set<Group> groups = new TreeSet<Group>(new Comparator<Group>()
        {
            public int compare(Group g1, Group g2) 
            {
                return g1.getLabel().compareTo(g2.getLabel());
            }
        });
        
        Map<String, Group> groupsAssoc = new HashMap<String, Group>();

        DirContext context = null;
        NamingEnumeration results = null;
        
        Map<String, String> groupsDesc = new HashMap<String, String>();
        try
        {
            // Connexion au serveur ldap
            context = new InitialDirContext(_getContextEnv());

            // Effectuer la recherche
            results = context.search(_groupsRelativeDN, _groupsObjectFilter, _getGroupsSearchConstraint());
            while (results.hasMoreElements())
            {
                Map<String, String> groupdesc = _getGroupDescription((SearchResult) results.nextElement());
                groupsDesc.put(groupdesc.get("id"), groupdesc.get("desc"));
            }
            
            // Cleanup the first results.
            _cleanup(null, results);
            
            // Connexion au serveur ldap
            context = new InitialDirContext(_getContextEnv());
            
            // Effectuer la recherche
            results = context.search(_usersRelativeDN, _usersObjectFilter, _getUsersSearchConstraint());
            while (results.hasMoreElements())
            {
                // Récupérer l'entrée courante
                try
                {
                    UserInfos userInfos = _getUserInfos((SearchResult) results.nextElement());
                    String login = userInfos.getLogin();
                    
                    // Créer ou bien mettre à jour les groupes
                    for (String groupID : userInfos.getGroups())
                    {
                        if (groupsAssoc.containsKey(groupID))
                        {
                            // Ajouter l'utilisateur courant au groupe
                            groupsAssoc.get(groupID).addUser(login);
                        }
                        else
                        {
                            String description = groupsDesc.get(groupID);
                            
                            // Créer un nouveau groupe
                            Group userGroup = new Group(groupID, description != null ? description : groupID);
                            userGroup.addUser(login);
                            // L'ajouter à la map
                            groupsAssoc.put(groupID, userGroup);
                        }
                    }
                }
                catch (IllegalArgumentException e)
                {
                    getLogger().warn("Error missing at least one attribute or attribute value", e);
                }
            }
            
            // Convertir la map en ensemble
            groups.addAll(groupsAssoc.values());
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
    
    private Map<String, String> _getGroupDescription(SearchResult entry)
    {
        Map<String, String> result = new HashMap<String, String>();
        
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
            String groupId = (String) groupIDAttr.get();
            
            // Récupérer la description d'un groupe
            Attribute groupDESCAttr = attrs.get(_groupsDescriptionAttribute);
            if (groupDESCAttr == null)
            {
                throw new IllegalArgumentException("Missing group description attribute : \"" + _groupsDescriptionAttribute + "\"");
            }
            String groupDesc = (String) groupDESCAttr.get();
            
            result.put("id", groupId);
            result.put("desc", groupDesc);

            return result;
        }
        catch (NamingException e)
        {
            throw new IllegalArgumentException("Missing at least one value for an attribute in an ldap entry", e);
        }
    }
    
    public Set<String> getUserGroups(String login)
    {
        // On cache hit, return the results. 
        if (isCacheEnabled())
        {
            Set<String> userGroups = (Set<String>) getObjectFromCache(login);
            if (userGroups != null)
            {
                return userGroups;
            }
        }
        
        Set<String> groups = new HashSet<String>();
        
        DirContext context = null;
        NamingEnumeration results = null;
        
        try
        {
            // Connexion au serveur ldap
            context = new InitialDirContext(_getContextEnv());
            
            // Créer le filtre de recherche
            StringBuffer filter = new StringBuffer("(&");
            filter.append(_usersObjectFilter);
            filter.append("(");
            filter.append(_usersLoginAttribute);
            filter.append("={0}))");

            // Effectuer la recherche
            results = context.search(_usersRelativeDN, filter.toString(), new Object[] {login}, _getUsersSearchConstraint());
            
            // Remplir l'ensemble des groupes
            while (results.hasMoreElements())
            {
                // Récupérer le groupe trouvé
                groups.addAll(_getGroupID((SearchResult) results.nextElement()));
            }
            
            // Cache the results.
            if (isCacheEnabled())
            {
                addObjectInCache(login, groups);
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
     * @return The group ids as a Set of String.
     * @throws IllegalArgumentException If a needed attribute is missing.
     */
    protected Set<String> _getGroupID(SearchResult entry)
    {
        Set<String> groups = new HashSet<String>();
        
        // Récupérer les attributs de l'entrée
        Attributes attrs = entry.getAttributes();
        
        try
        {
            // Récupérer les identifiants des groupes
            Attribute groupIDsAttr = attrs.get(_usersMemberOfAttribute);
            if (groupIDsAttr != null)
            {
                // Récupérer les membres du groupe
                NamingEnumeration members = groupIDsAttr.getAll();
                while (members.hasMore())
                {
                    String groupDN = (String) members.next();
                    Matcher matcher = _groupExtractionPattern.matcher(groupDN);
                    if (matcher.matches())
                    {
                        groups.add(matcher.group(1));
                    }
                    else
                    {
                        if (getLogger().isWarnEnabled())
                        {
                            getLogger().warn("Unable to get the uid from the LDAP RDN entry : " + groupDN);
                        }
    
                        groups.add(groupDN);
                    }
                }
                
                members.close();
            }
            
            return groups;
        }
        catch (NamingException e)
        {
            throw new IllegalArgumentException("Missing at least one value for an attribute in an ldap entry", e);
        }
    }

    /**
     * Get an UserInfos from attributes of a ldap entry.
     * @param entry The ldap entry to get attributes from.
     * @return Informations about the use as an UserInfos.
     * @throws IllegalArgumentException If a needed attribute is missing.
     */
    protected UserInfos _getUserInfos(SearchResult entry)
    {
        UserInfos infos = null;
        // Récupérer les attributs de l'entrée
        Attributes attrs = entry.getAttributes();
        
        try
        {
            // Récupérer l'identifiant d'un groupe
            Attribute loginAttr = attrs.get(_usersLoginAttribute);
            if (loginAttr == null)
            {
                throw new IllegalArgumentException("Missing login id attribute : \"" + _usersLoginAttribute + "\"");
            }
            
            infos = new UserInfos((String) loginAttr.get());
            
            // Récupérer les identifiants des groupes
            Attribute groupIDsAttr = attrs.get(_usersMemberOfAttribute);
            if (groupIDsAttr != null)
            {
                // Récupérer les membres du groupe
                NamingEnumeration members = groupIDsAttr.getAll();
                while (members.hasMore())
                {
                    String groupDN = (String) members.next();
                    
                    Matcher matcher = _groupExtractionPattern.matcher(groupDN);
                    if (matcher.matches())
                    {
                        infos.addGroup(matcher.group(1));
                    }
                    else
                    {
                        if (getLogger().isWarnEnabled())
                        {
                            getLogger().warn("Unable to get the uid from the LDAP RDN entry : " + groupDN);
                        }
                        infos.addGroup(groupDN);
                    }
                }
                members.close();
            }
        }
        catch (NamingException e)
        {
            throw new IllegalArgumentException("Missing at least one value for an attribute in an ldap entry", e);
        }
        
        return infos;
    }

    /**
     * Get constraints for a search on groups.
     * @return The constraints as a SearchControls.
     */
    protected SearchControls _getGroupsSearchConstraint()
    {
        // Paramètres de recherche
        SearchControls constraints = new SearchControls();
        
        // Un seul attribut à récupérer
        constraints.setReturningAttributes(new String [] {_groupsIdAttribute, _groupsDescriptionAttribute});
        // Choisir la profondeur de la recherche
        constraints.setSearchScope(_groupsSearchScope);
        return constraints;
    }
    

    /**
     * Get constraints for a search on users.
     * @return The constraints as a SearchControls.
     */
    protected SearchControls _getUsersSearchConstraint()
    {
        // Paramètres de recherche
        SearchControls constraints = new SearchControls();
        
        // Un seul attribut à récupérer
        constraints.setReturningAttributes(new String [] {_usersLoginAttribute, _usersMemberOfAttribute});
        // Choisir la profondeur de la recherche
        constraints.setSearchScope(_usersSearchScope);
        return constraints;
    }

    /**
     * Class for representing informations about an user
     * from an LDAP entry.
     */
    private class UserInfos
    {
        private String _login;
        private Set<String> _groups = new HashSet<String>();
        
        /**
         * Allocate an UserInfos.
         * @param login The login of the user.
         */
        public UserInfos(String login)
        {
            _login = login;
        }
        
        /**
         * Get the login of the user.
         * @return The login.
         */
        public String getLogin()
        {
            return _login;
        }
        
        /**
         * Get the groups associated with the user.
         * @return The group as a Set of String.
         */
        public Set<String> getGroups()
        {
            return _groups;
        }
        
        /**
         * Add a group.
         * @param group The group.
         */
        public void addGroup(String group)
        {
            _groups.add(group);
        }
    }
}
