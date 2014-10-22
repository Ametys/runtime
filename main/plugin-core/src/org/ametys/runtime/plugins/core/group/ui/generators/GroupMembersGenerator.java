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
package org.ametys.runtime.plugins.core.group.ui.generators;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.group.Group;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;

/**
 * Generate group members
 * 
 */
public class GroupMembersGenerator extends ServiceableGenerator
{
    private static final int _DEFAULT_COUNT_VALUE = Integer.MAX_VALUE;
    private static final int _DEFAULT_OFFSET_VALUE = 0;
    
    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
    }

    public void generate() throws IOException, SAXException, ProcessingException
    {
        String role = parameters.getParameter("groupsManagerRole", GroupsManager.ROLE);
        if (role.length() == 0)
        {
            role = GroupsManager.ROLE;
        }
        String usersrole = parameters.getParameter("usersManagerRole", UsersManager.ROLE);
        if (usersrole.length() == 0)
        {
            usersrole = UsersManager.ROLE;
        }
        
        GroupsManager groupsManager = null;
        UsersManager usersManager = null;
        
        try
        {
            groupsManager = (GroupsManager) manager.lookup(role);
            usersManager = (UsersManager) manager.lookup(usersrole);

            int offset = parameters.getParameterAsInteger("limit", _DEFAULT_COUNT_VALUE);
            if (offset == -1)
            {
                offset = Integer.MAX_VALUE;
            }
    
            int begin = parameters.getParameterAsInteger("start", _DEFAULT_OFFSET_VALUE);
            
            contentHandler.startDocument();
    
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute("", "id", "id", "CDATA", source);
            XMLUtils.startElement(contentHandler, "GroupMembers", attr);
    
            Group group = groupsManager.getGroup(source);
            if (group != null)
            {
                Set<User> users = _getSortedUsers(group, usersManager);
                
                int total = users.size();
                Iterator<User> it = users.iterator();
                int index = 0;
                while (it.hasNext() && index < begin + offset)
                {
                    User user = it.next();
                    if (index >= begin)
                    {
                        attr = new AttributesImpl();
                        attr.addAttribute("", "login", "login", "CDATA", user.getName());
                        XMLUtils.startElement(contentHandler, "User", attr);
                        XMLUtils.createElement(contentHandler, "FullName", user.getFullName());
                        XMLUtils.endElement(contentHandler, "User");
                    }
                    index++;
                }
                
                XMLUtils.createElement(contentHandler, "total", String.valueOf(total));
            }
            
            XMLUtils.endElement(contentHandler, "GroupMembers");
    
            contentHandler.endDocument();
        }
        catch (ServiceException e)
        {
            getLogger().error("Error looking up GroupsManager of role " + role + " or UsersManager of role " + usersrole, e);
            throw new ProcessingException("Error looking up GroupsManager of role " + role + " or UsersManager of role " + usersrole, e);
        }
        finally
        {
            manager.release(groupsManager);
            manager.release(usersManager);
        }
    }
    
    private Set<User> _getSortedUsers (Group group, UsersManager usersManager)
    {
        Set<User> users = new TreeSet<User>(new Comparator<User>()
        {
            public int compare(User u1, User u2) 
            {
                return u1.getFullName().toLowerCase().compareTo(u2.getFullName().toLowerCase());
            }
        });
        
        Set<String> logins = group.getUsers();
        for (String login : logins)
        {
            User user = usersManager.getUser(login);
            if (user != null)
            {
                users.add(user);
            }
        }
        
        return users;
    }

}
