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
package org.ametys.core.user;

import org.apache.commons.lang3.StringUtils;

/**
 * Class containing a user identity, i.e. the login of the user and 
 * the id of its user population.
 */
public class UserIdentity
{
    /** The separator between the login and the population id for the string representation of a user identity */
    private static final String __SEPARATOR = "#"; 
    
    /** The login of the user */
    private String _login;
    
    /** The id of the user population the user belongs to */
    private String _populationId;
    
    /**
     * Constructs a user identity
     * @param login The login of the user
     * @param populationId The id of the user population the user belongs to
     */
    public UserIdentity(String login, String populationId)
    {
        _login = login;
        _populationId = populationId;
    }
    
    /**
     * Gets a string representation of a {@link UserIdentity}
     * @param userIdentity The user identity
     * @return The string representation of the user identity.
     */
    public static String toString(UserIdentity userIdentity)
    {
        if (userIdentity != null)
        {
            return userIdentity.getLogin() + __SEPARATOR + userIdentity.getPopulationId();
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Returns the {@link UserIdentity} from its string representation
     * @param string The string representation of the user identity
     * @return The user identity from its string representation
     */
    public static UserIdentity fromString(String string)
    {
        if (string != null)
        {
            String[] fields = StringUtils.split(string, __SEPARATOR);
            String login = fields[0];
            String populationId = fields[1];
            return new UserIdentity(login, populationId);
        }
        else
        {
            return null;
        }
    }
    

    /**
     * Get the login of the user
     * @return The login of the user
     */
    public String getLogin()
    {
        return _login;
    }

    /**
     * GetGet the user population the user belongs to
     * @return The id of the user population the user belongs to
     */
    public String getPopulationId()
    {
        return _populationId;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_login == null) ? 0 : _login.hashCode());
        result = prime * result + ((_populationId == null) ? 0 : _populationId.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        
        UserIdentity other = (UserIdentity) obj;
        if (_login == null)
        {
            if (other._login != null)
            {
                return false;
            }
        }
        else if (!_login.equals(other._login))
        {
            return false;
        }
        if (_populationId == null)
        {
            if (other._populationId != null)
            {
                return false;
            }
        }
        else if (!_populationId.equals(other._populationId))
        {
            return false;
        }
        
        return true;
    }

    @Override
    public String toString()
    {
        return "UserIdentity [login=" + _login + ", population=" + _populationId + "]";
    }
}
