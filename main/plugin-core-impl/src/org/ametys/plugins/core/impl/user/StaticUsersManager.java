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
package org.ametys.plugins.core.impl.user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.core.authentication.Credentials;
import org.ametys.core.user.CredentialsAwareUsersManager;
import org.ametys.core.user.User;

/**
 * This implementation only use predefined users 
 */
public class StaticUsersManager implements Configurable, Component, CredentialsAwareUsersManager
{
    Map<String, User> _users;
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _users = new HashMap<>();
        
        Configuration[] usersConfigurations = configuration.getChildren("user");
        
        if (usersConfigurations.length > 0)
        {
            for (Configuration userConfiguration : usersConfigurations)
            {
                String id = userConfiguration.getAttribute("id", "");
                if (id.length() == 0)
                {
                    String message = "The defined user is not valid : no 'id' attribute is specified";
                    throw new ConfigurationException(message, configuration);
                }
                
                String fullname = userConfiguration.getChild("fullname").getValue(id);
                
                User user = new User(id, fullname, "");
                _users.put(id, user);
            }
        }
        else
        {
            User user = new User("anonymous", "Anonymous user", "");
            _users.put("anonymous", user);
        }
    }
    
    public User getUser(String login)
    {
        return _users.get(login);
    }

    public Collection<User> getUsers()
    {
        return Collections.unmodifiableCollection(_users.values());
    }

    public void toSAX(ContentHandler handler, int count, int offset, Map parameters) throws SAXException
    {
        XMLUtils.startElement(handler, "users");

        for (User user : _users.values())
        {
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute("", "login", "login", "CDATA", user.getName());
            XMLUtils.startElement(handler, "user", attr);
    
            String lastName = user.getFullName();
            XMLUtils.createElement(handler, "lastname", lastName != null ? lastName : "");
    
            String email = user.getEmail();
            XMLUtils.createElement(handler, "email", email != null ? email : "");
    
            XMLUtils.endElement(handler, "user");
        }
        
        XMLUtils.createElement(handler, "total", Integer.toString(_users.size()));

        XMLUtils.endElement(handler, "users");
    }
    
    public void saxUser(String login, ContentHandler handler) throws SAXException
    {
        User user = _users.get(login);
        
        if (user == null)
        {
            return;
        }
        
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "login", "login", "CDATA", user.getName());
        XMLUtils.startElement(handler, "user", attr);

        String lastName = user.getFullName();
        XMLUtils.createElement(handler, "lastname", lastName != null ? lastName : "");

        String email = user.getEmail();
        XMLUtils.createElement(handler, "email", email != null ? email : "");

        XMLUtils.endElement(handler, "user");
    }
    
    public boolean checkCredentials(Credentials credentials)
    {
        return _users.containsKey(credentials.getLogin());
    }
}
