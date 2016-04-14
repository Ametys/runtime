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
package org.ametys.core.user;

import org.apache.commons.lang3.StringUtils;

import org.ametys.core.user.directory.UserDirectory;

/**
 * Implementation of the principal abstraction to represent an user with a login
 * and eventually a fullname and an email.
 */
public class User implements java.security.Principal
{
    /**
     * The identity of this principal.
     */
    protected UserIdentity _identity;

    /**
     * The last name of this principal.
     */
    protected String _lastName;
    
    /**
     * The first name of this principal.
     */
    protected String _firstName;

    /**
     * The email of this principal.
     */
    protected String _email;

    /**
     * The user directory this user belongs to.
     */
    protected UserDirectory _userDirectory;

    /**
     * Construct a new UserPrincipal, associated with the specified login et population id.
     * 
     * @param login The login of the principal.
     * @param populationId The id of the population
     */
    public User(String login, String populationId)
    {
        this(new UserIdentity(login, populationId));
    }
    
    /**
     * Construct a new UserPrincipal, associated with the specified identity.
     * @param identity The identity of this user. Cannot be null
     */
    public User(UserIdentity identity)
    {
        this(identity, null, null, null, null);
    }

    /**
     * Construct a new UserPrincipal, associated with a last name, a first name,
     * an email and identified by a login.
     * @param identity The identity of this user. Cannot be null
     * @param lastName The last name
     * @param firstName The first name 
     * @param email The email
     * @param userDirectory The user directory the use rbelongs to. Can be null.
     */
    public User(UserIdentity identity, String lastName, String firstName, String email, UserDirectory userDirectory)
    {
        _identity = identity;
        _lastName = StringUtils.defaultString(lastName);
        _firstName = StringUtils.defaultString(firstName);
        _email = StringUtils.defaultString(email);
        _userDirectory = userDirectory;
    }

    /**
     * The identity of the user.
     * 
     * @return The identity. 
     */
    public UserIdentity getIdentity()
    {
        return _identity;
    }
    
    @Override
    public String getName()
    {
        return UserIdentity.userIdentityToString(_identity);
    }
    
    /**
     * The last name of the user
     * @return The last name.
     */
    public String getLastName()
    {
        return _lastName;
    }
    
    /**
     * The first name of the user
     * @return The first name.
     */
    public String getFirstName()
    {
        return _firstName;
    }
    
    /**
     * The email of the user represented by this Principal.
     * 
     * @return The email.
     */
    public String getEmail()
    {
        return _email;
    }
    
    /**
     * The fullname of this user.
     * @return The full name
     */
    public String getFullName()
    {
        return _getFullName(true);
    }
    
    /**
     * The fullname to use to display if sort is needed.
     * Ensure the sort will be on 
     * @return The sortable name
     */
    public String getSortableName()
    {
        return _getFullName(false);
    }
    
    /**
     * The user directory this user belongs to.
     * @return The user directory
     */
    public UserDirectory getUserDirectory()
    {
        return _userDirectory;
    }
    
    /**
     * The full name of the user represented by this Principal.
     * @param firstLast Define the name order if the full name. If true, first name then last name. If false, the contrary.
     * @return The full name.
     */
    protected String _getFullName(boolean firstLast)
    {
        StringBuilder sb = new StringBuilder();
        
        if (firstLast && StringUtils.isNotEmpty(_firstName))
        {
            sb.append(_firstName);
            
            if (StringUtils.isNotEmpty(_lastName))
            {
                sb.append(' ');
            }
        }
        
        if (StringUtils.isNotEmpty(_lastName))
        {
            sb.append(_lastName);
        }
        
        if (!firstLast && StringUtils.isNotEmpty(_firstName))
        {
            if (StringUtils.isNotEmpty(_lastName))
            {
                sb.append(' ');
            }
            
            sb.append(_firstName);
        }
        
        return StringUtils.defaultIfEmpty(sb.toString(), _identity.getLogin());
    }
    
    /**
     * Return a String representation of this object, which exposes only
     * information that should be public.
     * 
     * @return A string representing the user.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Principal[");
        sb.append(_identity.toString());
        sb.append(" : ");
        sb.append(getFullName());
        sb.append(", ");
        sb.append(_email);
        sb.append("]");
        return sb.toString();
    }

    /**
     * Test if two principal are equals.
     * @return true if the given Object represents the same Principal.
     */
    @Override
    public boolean equals(Object another)
    {
        if (another == null || !(another instanceof User))
        {
            return false;
        }
        
        User otherUser = (User) another;
        
        return _identity != null  && _identity.equals(otherUser.getIdentity());
    }
    
    @Override
    public int hashCode()
    {
        return _identity.hashCode();
    }
}
