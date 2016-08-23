/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.plugins.core.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;
import org.ametys.core.user.directory.UserDirectoryFactory;
import org.ametys.core.user.population.UserPopulationDAO;

/**
 * Simple user helper, for common function working on {@link User}
 */
public class UserHelper implements Component, Serviceable, Contextualizable
{
    /** The Avalon role */
    public static final String ROLE = UserHelper.class.getName();
    
    private static final String __USER_CACHE_REQUEST_ATTR = UserHelper.class.getName() + "$userCache";
    
    /** The user population DAO */
    private UserPopulationDAO _userPopulationDAO;

    /** The user directory factory */
    private UserDirectoryFactory _userDirectoryFactory;

    private UserManager _userManager;

    private Context _context;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        _userManager = (UserManager) smanager.lookup(UserManager.ROLE);
        _userPopulationDAO = (UserPopulationDAO) smanager.lookup(UserPopulationDAO.ROLE);
        _userDirectoryFactory = (UserDirectoryFactory) smanager.lookup(UserDirectoryFactory.ROLE);
    }

    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    /**
     * Get the user's full name handling request cache
     * @param userIdentity The user identity
     * @return user's full name or null if user does not exist
     */
    public String getUserFullName (UserIdentity userIdentity)
    {
        User user = _getUser(userIdentity);
        return user != null ? user.getFullName() : null;
    }
    
    /**
     * Get the user's sortable name handling request cache
     * @param userIdentity The user identity
     * @return user's full name or null if user does not exist
     */
    public String getUserSortableName (UserIdentity userIdentity)
    {
        User user = _getUser(userIdentity);
        return user != null ? user.getSortableName() : null;
    }
    
    /**
     * Populate a list of map, where each map representing an user.
     * @param users The list of users
     * @return The list of map.
     */
    public List<Map<String, Object>> users2json(Collection<User> users)
    {
        return users2json(users, false);
    }
    
    /**
     * Populate a list of map, where each map representing an user.
     * @param users The list of users
     * @param full Set to <code>true</code> to get full information on user
     * @return The list of map.
     */
    public List<Map<String, Object>> users2json(Collection<User> users, boolean full)
    {
        List<Map<String, Object>> userList = new ArrayList<>();
        
        for (User user : users)
        {
            userList.add(user2json(user, full));
        }
        
        return userList;
    }
    
    /**
     * Get the JSON object representing a user
     * @param userIdentity The user identity
     * @return The user as JSON object
     */
    public Map<String, Object> user2json (UserIdentity userIdentity)
    {
        return user2json(userIdentity, false);
    }
    
    /**
     * Get the JSON object representing a user
     * @param userIdentity The user identity
     * @param full Set to <code>true</code> to get full information on user
     * @return The user as JSON object
     */
    public Map<String, Object> user2json (UserIdentity userIdentity, boolean full)
    {
        User user = _getUser(userIdentity);
        if (user != null)
        {
            return user2json(user, full);
        }
        return Collections.EMPTY_MAP;
    }
    
    /**
     * Get the JSON object representing a user
     * @param user The user
     * @return The user as JSON object
     */
    public Map<String, Object> user2json(User user)
    {
        return _user2json(user, false);
    }
    
    /**
     * Get the JSON object representing a user
     * @param user The user
     * @param full Set to <code>true</code> to get full information on user
     * @return The user as JSON object
     */
    public Map<String, Object> user2json(User user, boolean full)
    {
        return _user2json(user, full);
    }
    
    private Map<String, Object> _user2json(User user, boolean full)
    {
        Map<String, Object> userInfos = new HashMap<>();
        
        userInfos.put("login", user.getIdentity().getLogin());
        
        String populationId = user.getIdentity().getPopulationId();
        userInfos.put("population", populationId);
        
        userInfos.put("fullname", user.getFullName());
        userInfos.put("sortablename", user.getSortableName());
        
        if (full)
        {
            String udModelId = user.getUserDirectory() != null ? user.getUserDirectory().getUserDirectoryModelId() : "";
            
            userInfos.put("populationLabel", _userPopulationDAO.getUserPopulation(populationId).getLabel());
            userInfos.put("directory", _userDirectoryFactory.hasExtension(udModelId) ? _userDirectoryFactory.getExtension(udModelId).getLabel() : "");
            
            userInfos.put("lastname", user.getLastName());
            userInfos.put("firstname", user.getFirstName());
            userInfos.put("email", user.getEmail());
        }
        
        return userInfos;
    }
    
    private User _getUser (UserIdentity userIdentity)
    {
        Request request = _getRequest();
        
        if (request != null)
        {
            // Try to get user from cache if request is not null
            if (request.getAttribute(__USER_CACHE_REQUEST_ATTR) == null)
            {
                request.setAttribute(__USER_CACHE_REQUEST_ATTR, new HashMap<UserIdentity, User>());
            }
            
            @SuppressWarnings("unchecked")
            Map<UserIdentity, User> userCache = (Map<UserIdentity, User>) request.getAttribute(__USER_CACHE_REQUEST_ATTR);
            
            User user = userCache.get(userIdentity);
            if (user != null)
            {
                return user;
            }

            user = _userManager.getUser(userIdentity.getPopulationId(), userIdentity.getLogin());
            if (user != null)
            {
                // Fill cache
                userCache.put(userIdentity, user);
            }
            return user;
        }
        else
        {
            // Get user ouside of a request (no cache available)
            return _userManager.getUser(userIdentity.getPopulationId(), userIdentity.getLogin());
        }
    }
    
    private Request _getRequest()
    {
        try
        {
            return ContextHelper.getRequest(_context);
        }
        catch (Exception e)
        {
            // There is no request
            return null;
        }  
    }
    
    /**
     * SAX an user
     * @param user The user
     * @param handler The content handler
     * @throws SAXException If a SAX error occurs
     */
    public void saxUser(User user, ContentHandler handler) throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
        attr.addCDATAAttribute("login", user.getIdentity().getLogin());
        attr.addCDATAAttribute("population", user.getIdentity().getPopulationId());
        
        XMLUtils.startElement(handler, "user", attr);
        
        XMLUtils.createElement(handler, "lastname", user.getLastName());
        XMLUtils.createElement(handler, "firstname", user.getFirstName());
        XMLUtils.createElement(handler, "email", user.getEmail());
        
        XMLUtils.createElement(handler, "fullname", user.getFullName());
        XMLUtils.createElement(handler, "sortablename", user.getSortableName());
        
        XMLUtils.endElement(handler, "user");
    }
}
