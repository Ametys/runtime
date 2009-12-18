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
package org.ametys.runtime.user;

/**
 * Implementation of the principal abstraction to represent an user with a login
 * and eventually a fullname and an email.
 */
public class User implements java.security.Principal
{
    /**
     * The login of this principal.
     */
    protected String _login;

    /**
     * The full name of this principal.
     */
    protected String _fullName;

    /**
     * The first name of this principal.
     */
    protected String _email;

    /**
     * Construct a new UserPrincipal, associated with the specified login.
     * 
     * @param login The login of the principal.
     */
    public User(String login)
    {
        this(login, null, null);
    }

    /**
     * Construct a new UserPrincipal, associated with a last name, a first name,
     * an email and identified by a login.
     * 
     * @param login The login of this principal.
     * @param fullName The full name of this principal.
     * @param email The email of this principal.
     */
    public User(String login, String fullName, String email)
    {
        _login = login;
        _fullName = fullName;
        _email = email;
    }

    /**
     * The login of the user represented by this Principal.
     * 
     * @return The login.
     */
    public String getName()
    {
        return _login;
    }

    /**
     * The full name of the user represented by this Principal.
     * 
     * @return The full name.
     */
    public String getFullName()
    {
        return _fullName;
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
     * Return a String representation of this object, which exposes only
     * information that should be public.
     * 
     * @return A string representing the user.
     */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer("Principal[");
        sb.append(_login);
        sb.append(" : ");
        sb.append(_fullName);
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
        
        return _login != null  || _login.equals(otherUser.getName());
    }
    
    @Override
    public int hashCode()
    {
        return _login.hashCode();
    }
}
