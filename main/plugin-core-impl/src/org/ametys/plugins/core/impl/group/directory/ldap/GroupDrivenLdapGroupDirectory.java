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

import org.ametys.core.group.Group;
import org.ametys.core.group.GroupIdentity;
import org.ametys.core.user.UserIdentity;

/**
 * Use a ldap server for getting the group of users.<br>
 * Groups are branch.
 */
public class GroupDrivenLdapGroupDirectory extends AbstractLdapGroupDirectory
{
    /** Relative DN for users. */
    protected static final String __PARAM_USERS_RELATIVE_DN = "runtime.users.ldap.peopleDN";
    /** Name of the login attribute. */
    protected static final String __PARAM_USERS_LOGIN_ATTRIBUTE = "runtime.users.ldap.loginAttr";
    /** Name of the member DN attribute. */
    protected static final String __PARAM_GROUPS_MEMBER_ATTRIBUTE = "runtime.groups.ldap.member";
    
    /** Pattern to retrieve user's login from DN */
    protected Pattern _loginExtractionPattern;
    
    /** The attribute which contains the member DN */
    protected String _groupsMemberAttribute;
    /** Relative DN for users. */
    protected String _usersRelativeDN;
    /** The users's id in the DN of a user. */
    protected String _usersLoginAttribute;
    
    @Override
    public void init(String groupDirectoryModelId, Map<String, Object> paramValues)
    {
        super.init(groupDirectoryModelId, paramValues);
        
        _usersRelativeDN = (String) paramValues.get(__PARAM_USERS_RELATIVE_DN);
        _usersLoginAttribute = (String) paramValues.get(__PARAM_USERS_LOGIN_ATTRIBUTE);
        _groupsMemberAttribute = (String) paramValues.get(__PARAM_GROUPS_MEMBER_ATTRIBUTE);
        
        _loginExtractionPattern = Pattern.compile("^(?:" + _usersLoginAttribute + "=)?([^,]+)(,.*)?");
    }
    
    @Override
    public Group getGroup(String groupID)
    {
        Group group = null;

        DirContext context = null;
        NamingEnumeration results = null;
        
        try
        {
            // Connect to ldap server
            context = new InitialDirContext(_getContextEnv());
            
            // Create search filter
            StringBuffer filter = new StringBuffer("(&");
            filter.append(_groupsObjectFilter);
            filter.append("(");
            filter.append(_groupsIdAttribute);
            filter.append("={0}))");

            // Run search
            results = context.search(_groupsRelativeDN, filter.toString(),
                                     new Object[] {groupID}, _getSearchConstraint());
            
            // Check if a group matches
            if (results.hasMoreElements())
            {
                // Retrieve the found group
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
            // Close connection resources
            _cleanup(context, results);
        }

        // Return group or null
        return group;
    }

    @Override
    public Set<Group> getGroups()
    {
        // Create set of group
        Set<Group> groups = new TreeSet<>(new Comparator<Group>()
        {
            public int compare(Group g1, Group g2) 
            {
                if (g1.getIdentity().getId().equals(g2.getIdentity().getId()))
                {
                    return 0;
                }
                
                // Case insensitive sort
                int compareTo = g1.getLabel().toLowerCase().compareTo(g2.getLabel().toLowerCase());
                if (compareTo == 0)
                {
                    return g1.getIdentity().getId().compareTo(g2.getIdentity().getId());
                }
                return compareTo;
            }
        });

        try
        {
            for (SearchResult searchResult : _search(_pageSize, _groupsRelativeDN, _groupsObjectFilter, _getSearchConstraint()))
            {
                // Add a new group to the set
                groups.add(_getUserGroup(searchResult));
            }
        }
        catch (IllegalArgumentException e)
        {
            getLogger().error("Error missing at least one attribute or attribute value", e);
        }

        // Return the list of users as a collection of UserGroup, possibly empty
        return groups;
    }

    @Override
    public Set<String> getUserGroups(String login, String populationId)
    {
        if (!populationId.equals(_associatedPopulationId))
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
            filter.append(_groupsObjectFilter);
            filter.append("(|(");
            filter.append(_groupsMemberAttribute);
            filter.append("=" + _usersLoginAttribute + "={0},");
            filter.append(_usersRelativeDN + (_usersRelativeDN.length() > 0 && _ldapBaseDN.length() > 0 ? "," : "") + _ldapBaseDN);
            filter.append(")(");
            filter.append(_groupsMemberAttribute);
            filter.append("=" + _usersLoginAttribute + "={0}");
            filter.append(")(");
            filter.append(_groupsMemberAttribute);
            filter.append("={0}");
            filter.append(")))");
            
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Searching groups of user '" + login + "' with base DN '" + _groupsRelativeDN + "': '" + filter.toString() + "'.");
            }
            
            // Run search
            results = context.search(_groupsRelativeDN, filter.toString(),
                                     new Object[] {login}, _getSearchConstraint());
            
            int groupCount = 0;
            
            // Fill the set of groups
            while (results.hasMoreElements())
            {
                // Retrieve the found group
                groups.add(_getGroupID((SearchResult) results.nextElement()));
                
                groupCount++;
            }
            
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug(groupCount + " groups found for user '" + login + "'");
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

        // Return the groups, posssibly empty
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
            String groupID = (String) groupIDAttr.get();
            
            // Retrieve the description of a group
            Attribute groupDESCAttr = attrs.get(_groupsDescriptionAttribute);
            if (groupDESCAttr == null)
            {
                throw new IllegalArgumentException("Missing group description attribute : \"" + _groupsDescriptionAttribute + "\"");
            }
            String groupDesc = (String) groupDESCAttr.get();

            group = new Group(new GroupIdentity(groupID, getId()), groupDesc, this);
            
            // Retrieve the identifier of a group
            Attribute membersAttr = attrs.get(_groupsMemberAttribute);
            if (membersAttr != null)
            {
                // Retrieve the members of the group
                NamingEnumeration members = membersAttr.getAll();
                while (members.hasMore())
                {
                    String userDN = (String) members.next();
                    
                    // Retrieve the login
                    Matcher matcher = _loginExtractionPattern.matcher(userDN);
                    if (matcher.matches())
                    {
                        // Add the curent user
                        UserIdentity identity = new UserIdentity(matcher.group(1), _associatedPopulationId);
                        group.addUser(identity);
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
        // Search parameters
        SearchControls constraints = new SearchControls();
        
        // Only one attribute to retrieve
        constraints.setReturningAttributes(new String [] {_groupsIdAttribute, _groupsDescriptionAttribute, _groupsMemberAttribute});
        // Choose the depth of search
        constraints.setSearchScope(_groupsSearchScope);
        return constraints;
    }
}
