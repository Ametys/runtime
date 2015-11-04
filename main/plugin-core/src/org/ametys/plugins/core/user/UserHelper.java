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

import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.ametys.core.user.User;

/**
 * Simple user helper, for common function working on {@link User}
 */
public final class UserHelper
{
    private UserHelper()
    {
        // cannot instantiate
    }
    
    /**
     * Populate a list of map, where each map representing an user.
     * @param users The list of users
     * @return The list of map.
     */
    public static List<Map<String, Object>> users2MapList(Collection<User> users)
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
    public static Map<String, Object> user2Map(User user)
    {
        Map<String, Object> userInfos = new HashMap<>();
        
        userInfos.put("login", user.getName());
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
    public static void saxUser(User user, ContentHandler handler) throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
        attr.addCDATAAttribute("login", user.getName());
        
        XMLUtils.startElement(handler, "user", attr);
        
        XMLUtils.createElement(handler, "lastname", user.getLastName());
        XMLUtils.createElement(handler, "firstname", user.getFirstName());
        XMLUtils.createElement(handler, "email", user.getEmail());
        
        XMLUtils.createElement(handler, "fullname", user.getFullName());
        XMLUtils.createElement(handler, "sortablename", user.getSortableName());
        
        XMLUtils.endElement(handler, "user");
    }
}
