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
package org.ametys.plugins.core.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.core.group.Group;
import org.ametys.core.group.GroupManager;
import org.ametys.core.user.User;
import org.ametys.core.user.UserIdentity;
import org.ametys.core.user.UserManager;
import org.ametys.plugins.core.user.UserHelper;

/**
 * Get the users of a group
 *
 */
public class UsersGroupAction extends ServiceableAction
{
    private static final int _DEFAULT_COUNT_VALUE = Integer.MAX_VALUE;
    private static final int _DEFAULT_OFFSET_VALUE = 0;
    
    private UserHelper _userHelper;
    private UserManager _userManager;
    private GroupManager _groupManager;
    
    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _userHelper = (UserHelper) smanager.lookup(UserHelper.ROLE);
        _userManager = (UserManager) smanager.lookup(UserManager.ROLE);
        _groupManager = (GroupManager) smanager.lookup(GroupManager.ROLE);
    }
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        List<Map<String, Object>> users = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        String groupDirectoryId = (String) jsParameters.get("groupDirectoryId");

        int offset = parameters.getParameterAsInteger("limit", _DEFAULT_COUNT_VALUE);
        if (offset == -1)
        {
            offset = Integer.MAX_VALUE;
        }

        int begin = parameters.getParameterAsInteger("start", _DEFAULT_OFFSET_VALUE);
        
        Group group = _groupManager.getGroup(groupDirectoryId, source);
        if (group != null)
        {
            List<User> sortedUsers = _getSortedUsers(group);
            Iterator<User> it = sortedUsers.iterator();
            
            int index = 0;
            while (it.hasNext() && index < begin + offset)
            {
                User user = it.next();
                
                if (index >= begin)
                {
                    users.add(_userHelper.user2json(user, true));
                    
                }
                index++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("group", source);

        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);

        return EMPTY_MAP;
    }
    
    private List<User> _getSortedUsers (Group group)
    {
        List<User> users = new ArrayList<>();
        
        Set<UserIdentity> groupUsers = group.getUsers();
        for (UserIdentity identity : groupUsers)
        {
            String login = identity.getLogin();
            String populationId = identity.getPopulationId();
            User user = _userManager.getUser(populationId, login);
            if (user != null)
            {
                users.add(user);
            }
        }
        
        Collections.sort(users, new Comparator<User>()
        {
            public int compare(User u1, User u2) 
            {
                int compare = u1.getLastName().toLowerCase().compareTo(u2.getLastName().toLowerCase());
                return compare != 0 ? compare : u1.getFirstName().toLowerCase().compareTo(u2.getFirstName().toLowerCase()); 
            }
        });
        
        return users;
    }

}
