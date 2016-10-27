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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.avalon.framework.component.Component;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.directory.UserDirectory;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * This implementation only uses predefined users 
 */
public class StaticUserDirectory extends AbstractLogEnabled implements UserDirectory, Component
{
    private static final String __PARAM_USERS = "runtime.users.static.users";
    
    private Map<String, User> _staticUsers;

    private String _udModelId;

    private Map<String, Object> _paramValues;

    private String _populationId;

    private String _label;

    private String _id;

    private boolean _grantAllCredentials = true;
    
    public String getId()
    {
        return _id;
    }
    
    public String getLabel()
    {
        return _label;
    }
    
    @Override
    public void init(String id, String udModelId, Map<String, Object> paramValues, String label)
    {
        _id = id;
        _udModelId = udModelId;
        _staticUsers = new HashMap<>();
        _label = label;
        
        _paramValues = paramValues;
        
        String usersAsText = (String) paramValues.get(__PARAM_USERS);
        for (String userLine : usersAsText.split("\\n"))
        {
            User user = _createUser(userLine);
            _staticUsers.put(user.getIdentity().getLogin(), user);
        }
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
    
    /**
     * Set to false to disallow any user to be authenticated by its credentials
     * @param grantAllCredentials 
     */
    public void setGrantAllCredentials(boolean grantAllCredentials)
    {
        _grantAllCredentials  = grantAllCredentials;
    }
    
    @Override
    public String getUserDirectoryModelId()
    {
        return _udModelId;
    }
    
    @Override
    public Collection<User> getUsers()
    {
        return _staticUsers.values();
    }

    @Override
    public List<User> getUsers(int count, int offset, Map<String, Object> parameters)
    {
        String pattern = StringUtils.defaultIfEmpty((String) parameters.get("pattern"), "");
        int boundedCount = count >= 0 ? count : Integer.MAX_VALUE;
        int boundedOffset = offset >= 0 ? offset : 0;
        
        List<User> result = _staticUsers.values().stream().filter(user-> _isLike(user, pattern)).collect(Collectors.toList());
        
        int toIndex = boundedOffset + boundedCount; 
        toIndex = toIndex > result.size() ? result.size() : toIndex;
        return result.subList(boundedOffset, toIndex);
    }

    @Override
    public User getUser(String login)
    {
        return _staticUsers.get(login);
    }

    @Override
    public boolean checkCredentials(String login, String password)
    {
        return _grantAllCredentials && _staticUsers.containsKey(login);
    }
    
    @SuppressWarnings("fallthrough")
    private User _createUser(String userLine)
    {
        String[] userInfo = userLine.split(":");
        
        String login = null;
        String lastName = null;
        String firstName = null;
        String email = null;
        
        switch (userInfo.length)
        {
            case 4:
                email = userInfo[3];
                //fall through
            case 3:
                firstName = userInfo[2];
                //fall through
            case 2:
                lastName = userInfo[1];
                login = userInfo[0];
                break;
            case 1:
                login = userInfo[0];
                lastName = login;
                break;
            default:
                getLogger().error("Error while reading StaticUserDirectory users, cannot create an user with the data from line {}", userLine);
                break;
        }
        
        return new User(new UserIdentity(login, _populationId), lastName, firstName, email, this);
    }
    
    private boolean _isLike(User user, String pattern)
    {
        String modifiedPattern = StringUtils.stripAccents(pattern);
        return StringUtils.containsIgnoreCase(StringUtils.stripAccents(user.getIdentity().getLogin()), modifiedPattern) 
                || StringUtils.containsIgnoreCase(StringUtils.stripAccents(user.getFirstName()), modifiedPattern)
                || StringUtils.containsIgnoreCase(StringUtils.stripAccents(user.getLastName()), modifiedPattern);
    }
}
