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
package org.ametys.runtime.plugins.core.right.ui.generators;

import java.io.IOException;
import java.util.Set;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.group.Group;
import org.ametys.runtime.plugins.core.right.profile.DefaultProfileBasedRightsManager;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.user.User;


/**
 * Generates the list of users and groups with their profile for a given context
 */
public class UsersAndGroupsGenerator extends ServiceableGenerator
{
    /** The rights manager */
    protected DefaultProfileBasedRightsManager _rightsManager;
    
    @Override
    public void service(ServiceManager sManager) throws ServiceException
    {
        super.service(sManager);
        _rightsManager = (DefaultProfileBasedRightsManager) manager.lookup(RightsManager.ROLE);
    }
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String context = request.getParameter("context");

        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "info");

        XMLUtils.startElement(contentHandler, "groups");
        Set<Group> groups = _rightsManager.getGroupsByContext(context);
        for (Group userGroup : groups)
        {
            AttributesImpl gAttrs = new AttributesImpl();
            gAttrs.addAttribute("", "id", "id", "CDATA", userGroup.getId());
            XMLUtils.startElement(contentHandler, "group", gAttrs);

            XMLUtils.createElement(contentHandler, "label", userGroup.getLabel());

            XMLUtils.startElement(contentHandler, "profiles");
            Set<String> profiles = _rightsManager.getProfilesByGroup(userGroup.getId(), context);
            for (String profileId : profiles)
            {
                AttributesImpl pAttrs = new AttributesImpl();
                pAttrs.addAttribute("", "id", "id", "CDATA", profileId);
                XMLUtils.createElement(contentHandler, "profile", pAttrs);
            }
            XMLUtils.endElement(contentHandler, "profiles");
            
            XMLUtils.endElement(contentHandler, "group");
        }
        
        XMLUtils.endElement(contentHandler, "groups");

        XMLUtils.startElement(contentHandler, "users");

        Set<User> users = _rightsManager.getUsersByContext(context);
        for (User p : users)
        {
            AttributesImpl gAttrs = new AttributesImpl();
            gAttrs.addAttribute("", "id", "id", "CDATA", p.getName());
            XMLUtils.startElement(contentHandler, "user", gAttrs);

            XMLUtils.createElement(contentHandler, "label", p.getFullName() + " (" + p.getName() + ")");

            XMLUtils.startElement(contentHandler, "profiles");
            Set<String> profiles = _rightsManager.getProfilesByUser(p.getName(), context);
            for (String profileId : profiles)
            {
                AttributesImpl pAttrs = new AttributesImpl();
                pAttrs.addAttribute("", "id", "id", "CDATA", profileId);
                XMLUtils.createElement(contentHandler, "profile", pAttrs);
            }
            XMLUtils.endElement(contentHandler, "profiles");
            
            XMLUtils.endElement(contentHandler, "user");
        }
        
        XMLUtils.endElement(contentHandler, "users");

        XMLUtils.endElement(contentHandler, "info");
        contentHandler.endDocument();
    }
}