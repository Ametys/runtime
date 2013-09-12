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
package org.ametys.runtime.plugins.core.group.ui.generators;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.group.Group;
import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.user.User;
import org.ametys.runtime.user.UsersManager;

import com.google.common.collect.Iterators;

/**
 * Generate group members
 * 
 */
public class GroupMembersGenerator extends ServiceableGenerator
{
    private static final int _DEFAULT_COUNT_VALUE = 100;
    private static final int _DEFAULT_OFFSET_VALUE = 0;
    
    private GroupsManager _groups;

    private UsersManager _users;

    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
        _users = (UsersManager) m.lookup(UsersManager.ROLE);
        _groups = (GroupsManager) m.lookup(GroupsManager.ROLE);
    }

    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        
        // Nombre de résultats max
        int count = parameters.getParameterAsInteger("limit", _DEFAULT_COUNT_VALUE);
        if (count < 0)
        {
            count = Integer.MAX_VALUE;
        }

        // Décalage des résultats
        int offset = Math.max(parameters.getParameterAsInteger("start", _DEFAULT_OFFSET_VALUE), 0);
        
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "id", "id", "CDATA", source);
        XMLUtils.startElement(contentHandler, "GroupMembers", attr);

        Group group = _groups.getGroup(source);
        if (group != null)
        {
            // Populate a Set of User ordered by full name.
            SortedSet<User> users = new TreeSet<User>(new Comparator<User>()
            {
                @Override
                public int compare(User u1, User u2)
                {
                    String f1 = StringUtils.defaultString(u1.getFullName()).toUpperCase();
                    String f2 = StringUtils.defaultString(u2.getFullName()).toUpperCase();
                    return f1.compareTo(f2);
                }
            });
            
            Set<String> userLogins = group.getUsers();
            for (String login: userLogins)
            {
                User user = _users.getUser(login);
                if (user != null)
                {
                    users.add(user);
                }
            }
            
            // SAX user individually in the right order.
            // Take account of count and limit parameters.
            Iterator<User> it = users.iterator();
            Iterators.advance(it, offset);
            Iterator<User> subSetUserIterator = Iterators.limit(it, count);
            
            while (subSetUserIterator.hasNext())
            {
                User user = subSetUserIterator.next();
                
                attr = new AttributesImpl();
                attr.addAttribute("", "login", "login", "CDATA", user.getName());
                XMLUtils.startElement(contentHandler, "User", attr);
                XMLUtils.createElement(contentHandler, "FullName", user.getFullName());
                XMLUtils.endElement(contentHandler, "User");
            }
            
            // SAX the total number of user.
            String total = Integer.toString(users.size());
            XMLUtils.createElement(contentHandler, "total", total);
        }
        
        XMLUtils.endElement(contentHandler, "GroupMembers");

        contentHandler.endDocument();
    }
}
