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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.user.User;
import org.ametys.core.user.directory.UserDirectoryFactory;
import org.ametys.core.user.population.UserPopulationDAO;

/**
 * Simple user helper, for common function working on {@link User}
 */
public final class UserHelper implements Component, Serviceable
{
    /** The avalon role */
    public static final String ROLE = UserHelper.class.getName();
    
    /** The user population DAO */
    private UserPopulationDAO _userPopulationDAO;

    /** The user directory factory */
    private UserDirectoryFactory _userDirectoryFactory;
    
    public void service(ServiceManager smanager) throws ServiceException
    {
        _userPopulationDAO = (UserPopulationDAO) smanager.lookup(UserPopulationDAO.ROLE);
        _userDirectoryFactory = (UserDirectoryFactory) smanager.lookup(UserDirectoryFactory.ROLE);
    }
    
    /**
     * Populate a list of map, where each map representing an user.
     * @param users The list of users
     * @return The list of map.
     */
    public List<Map<String, Object>> users2MapList(Collection<User> users)
    {
        List<Map<String, Object>> userList = new ArrayList<>();
        
        for (User user : users)
        {
            userList.add(user2Map(user));
        }
        
        return userList;
    }
    
    /**
     * Extract a map from an user 
     * @param user The user
     * @return The extracted map
     */
    public Map<String, Object> user2Map(User user)
    {
        Map<String, Object> userInfos = new HashMap<>();
        
        String populationId = user.getIdentity().getPopulationId();
        String udModelId = user.getUserDirectory() != null ? user.getUserDirectory().getUserDirectoryModelId() : "";
        
        userInfos.put("login", user.getIdentity().getLogin());
        userInfos.put("population", populationId);
        userInfos.put("populationLabel", _userPopulationDAO.getUserPopulation(populationId).getLabel());
        userInfos.put("directory", _userDirectoryFactory.hasExtension(udModelId) ? _userDirectoryFactory.getExtension(udModelId).getLabel() : "");
        userInfos.put("lastname", user.getLastName());
        userInfos.put("firstname", user.getFirstName());
        userInfos.put("email", user.getEmail());
        
        userInfos.put("fullname", user.getFullName());
        userInfos.put("sortablename", user.getSortableName());
        
        return userInfos;
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
        
        XMLUtils.startElement(handler, "user", attr);
        
        XMLUtils.createElement(handler, "population", user.getIdentity().getPopulationId());
        XMLUtils.createElement(handler, "lastname", user.getLastName());
        XMLUtils.createElement(handler, "firstname", user.getFirstName());
        XMLUtils.createElement(handler, "email", user.getEmail());
        
        XMLUtils.createElement(handler, "fullname", user.getFullName());
        XMLUtils.createElement(handler, "sortablename", user.getSortableName());
        
        XMLUtils.endElement(handler, "user");
    }
}
