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
package org.ametys.plugins.core.impl.group.directory.ldap;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang3.StringUtils;

import org.ametys.core.group.Group;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.util.ldap.ScopeEnumerator;

/**
 * Use a ldap server for getting the group of users<br>
 * Groups are constructed using attributes on users.
 */
public class UserDrivenLdapGroupDirectory extends AbstractLdapGroupDirectory
{
    /** Relative DN for users. */
    protected static final String __PARAM_USERS_RELATIVE_DN = "runtime.users.ldap.peopleDN";
    /** Filter for limiting the search. */
    protected static final String __PARAM_USERS_OBJECT_FILTER = "runtime.users.ldap.baseFilter";
    /** The scope used for search. */
    protected static final String __PARAM_USERS_SEARCH_SCOPE = "runtime.users.ldap.scope";
    /** Name of the login attribute. */
    protected static final String __PARAM_USERS_LOGIN_ATTRIBUTE = "runtime.users.ldap.loginAttr";
    /** Name of the member DN attribute. */
    protected static final String __PARAM_GROUPS_MEMBER_ATTRIBUTE = "runtime.groups.ldap.memberof";
    
    private static final GroupComparator _GROUP_COMPARATOR = new GroupComparator();
    
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
    public void init(String groupDirectoryModelId, Map<String, Object> paramValues)
    {
        super.init(groupDirectoryModelId, paramValues);
        
        _usersRelativeDN = (String) paramValues.get(__PARAM_USERS_RELATIVE_DN);
        _usersObjectFilter = (String) paramValues.get(__PARAM_USERS_OBJECT_FILTER);
        _usersSearchScope = ScopeEnumerator.parseScope((String) paramValues.get(__PARAM_USERS_SEARCH_SCOPE));
        _usersLoginAttribute = (String) paramValues.get(__PARAM_USERS_LOGIN_ATTRIBUTE);
        _usersMemberOfAttribute = (String) paramValues.get(__PARAM_GROUPS_MEMBER_ATTRIBUTE);
        
        _groupExtractionPattern = Pattern.compile("^" + _groupsIdAttribute + "=([^,]+),.*");
    }
    
    @Override
    public Group getGroup(String groupID)
    {
        for (Group userGroup : getGroups())
        {
            if (userGroup.getIdentity().getId().equals(groupID))
            {
                return userGroup;
            }
        }

        return null;
    }

    @Override
    public Set<Group> getGroups()
    {
        // Create a set of groups
        Set<Group> groups = new TreeSet<>(_GROUP_COMPARATOR);
        
        Map<String, Group> groupsAssoc = new HashMap<>();

        // Run first search for groups
        Map<String, String> groupsDesc = _search(_pageSize, _groupsRelativeDN, _groupsObjectFilter, _getGroupsSearchConstraint()).stream()
                .map(this::_getGroupDescription)
                .collect(Collectors.toMap(desc -> desc.get("id"), desc -> desc.get("desc")));
        
        // Run second search for users
        _search(_pageSize, _usersRelativeDN, _usersObjectFilter, _getUsersSearchConstraint()).stream()
                .map(this::_getUserInfos)
                .forEach(userInfo -> _addUserToGroups(userInfo, groupsAssoc, groupsDesc));
        groups.addAll(groupsAssoc.values());

        // Return the list of users as a collection of UserGroup, possibly empty
        return groups;
    }
    
    private Map<String, String> _getGroupDescription(SearchResult entry)
    {
        Map<String, String> result = new HashMap<>();
        
        // Retrieve the attributes of the entry
        Attributes attrs = entry.getAttributes();
        
        try
        {
            // Retrieve the identifier of a group
            Attribute groupIDAttr = attrs.get(_groupsIdAttribute);
            if (groupIDAttr == null)
            {
                throw new IllegalArgumentException("Missing group id attribute : \"" + _groupsIdAttribute + "\"");
            }
            String groupId = (String) groupIDAttr.get();
            
            // Retrieve the description of groups
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
    
    /**
     * Add the user to the groups he belongs to.
     * @param userInfos The user membership informations.
     * @param groupsAssoc The group Map, indexed by group ID.
     * @param groupsDesc The group descriptions Map.
     */
    protected void _addUserToGroups(UserInfos userInfos, Map<String, Group> groupsAssoc, Map<String, String> groupsDesc)
    {
        String login = userInfos.getLogin();
        
        // Create or update the groups
        for (String groupID : userInfos.getGroups())
        {
            if (groupsAssoc.containsKey(groupID))
            {
                // Add current user to the group
                UserIdentity identity = new UserIdentity(login, _associatedPopulationId);
                groupsAssoc.get(groupID).addUser(identity);
            }
            else
            {
                if (groupsDesc.containsKey(groupID))
                {
                    String description = groupsDesc.get(groupID);
                    
                    // Create a new group
                    Group userGroup = new Group(new GroupIdentity(groupID, getId()), description != null ? description : groupID, this);
                    UserIdentity identity = new UserIdentity(login, _associatedPopulationId);
                    userGroup.addUser(identity);
                    // Add the group to the map
                    groupsAssoc.put(groupID, userGroup);
                }
            }
        }
    }

    @Override
    public Set<String> getUserGroups(String login, String populationId)
    {
        if (!StringUtils.equals(populationId, _associatedPopulationId))
        {
            return Collections.emptySet();
        }
        
        // Cache hit, return the results. 
        if (isCacheEnabled())
        {
            @SuppressWarnings("unchecked")
            Set<String> userGroups = (Set<String>) getObjectFromCache(login);
            if (userGroups != null)
            {
                return userGroups;
            }
        }
        
        Set<String> groups = new HashSet<>();
        
        DirContext context = null;
        NamingEnumeration results = null;
        
        try
        {
            // Connect to ldap server
            context = new InitialDirContext(_getContextEnv());
            
            // Create search filter
            StringBuffer filter = new StringBuffer("(&");
            filter.append(_usersObjectFilter);
            filter.append("(");
            filter.append(_usersLoginAttribute);
            filter.append("={0}))");

            // Run search
            results = context.search(_usersRelativeDN, filter.toString(), new Object[] {login}, _getUsersSearchConstraint());
            
            // Fill the set of groups
            while (results.hasMoreElements())
            {
                // Retrieve found group
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
            // Close connection resources
            _cleanup(context, results);
        }

        // Return the groups, possibly empty
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
        Set<String> groups = new HashSet<>();
        
        // Retrieve the entry attributes
        Attributes attrs = entry.getAttributes();
        
        try
        {
            // Retrieve the identifier of the groups
            Attribute groupIDsAttr = attrs.get(_usersMemberOfAttribute);
            if (groupIDsAttr != null)
            {
                // Retrieve the members of the group
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
        // Retrieve the entry attributes
        Attributes attrs = entry.getAttributes();
        
        try
        {
            // Retrieve the identifier of a group
            Attribute loginAttr = attrs.get(_usersLoginAttribute);
            if (loginAttr == null)
            {
                throw new IllegalArgumentException("Missing login id attribute : \"" + _usersLoginAttribute + "\"");
            }
            
            infos = new UserInfos((String) loginAttr.get());
            
            // Retrieve the identifiers of groups
            Attribute groupIDsAttr = attrs.get(_usersMemberOfAttribute);
            if (groupIDsAttr != null)
            {
                // Retrieve the members of the group
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
            throw new IllegalArgumentException("Missing at least one value for an attribute in a ldap entry", e);
        }
        
        return infos;
    }
    
    /**
     * Get constraints for a search on groups.
     * @return The constraints as a SearchControls.
     */
    protected SearchControls _getGroupsSearchConstraint()
    {
        // Search parameters
        SearchControls constraints = new SearchControls();
        
        // Only one attribute to retrieve
        constraints.setReturningAttributes(new String [] {_groupsIdAttribute, _groupsDescriptionAttribute});
        // Choose depth of search
        constraints.setSearchScope(_groupsSearchScope);
        return constraints;
    }
    
    /**
     * Get constraints for a search on users.
     * @return The constraints as a SearchControls.
     */
    protected SearchControls _getUsersSearchConstraint()
    {
        // Search parameters
        SearchControls constraints = new SearchControls();
        
        // Only one attribute to retrieve
        constraints.setReturningAttributes(new String [] {_usersLoginAttribute, _usersMemberOfAttribute});
        // Choose depth of search
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
        private Set<String> _groups = new HashSet<>();
        
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
    
    /**
     * Group comparator.
     */
    private static class GroupComparator implements Comparator<Group>
    {
        /**
         * Constructor.
         */
        public GroupComparator()
        {
            // Nothing to do.
        }
        
        @Override
        public int compare(Group g1, Group g2) 
        {
            if (g1.getIdentity().getId().equals(g2.getIdentity().getId()))
            {
                return 0;
            }
            
            int compareTo = g1.getLabel().toLowerCase().compareTo(g2.getLabel().toLowerCase());
            if (compareTo == 0)
            {
                return g1.getIdentity().getId().compareTo(g2.getIdentity().getId());
            }
            return compareTo;
        }
    }

}
