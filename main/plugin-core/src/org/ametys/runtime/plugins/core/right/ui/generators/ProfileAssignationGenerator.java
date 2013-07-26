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
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.group.Group;
import org.ametys.runtime.plugins.core.right.profile.DefaultProfileBasedRightsManager;
import org.ametys.runtime.plugins.core.right.profile.Profile;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.user.User;

/**
 * Generates the list of profiles with the users and groups affected
 *
 */
public class ProfileAssignationGenerator extends ServiceableGenerator
{
    /** The rights manager */
    protected DefaultProfileBasedRightsManager _rightsManager;
    
    @Override
    public void service(ServiceManager sManager) throws ServiceException
    {
        super.service(sManager);
        _rightsManager = (DefaultProfileBasedRightsManager) manager.lookup(RightsManager.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String profileID = request.getParameter("profileId");
        String context = request.getParameter("context");
        
        contentHandler.startDocument();
        
        XMLUtils.startElement(contentHandler, "assignment");
        
        Profile profile = _rightsManager.getProfile(profileID);
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "id", "id", "CDATA", profile.getId());
        XMLUtils.createElement(contentHandler, "profile", attr, profile.getName());
        XMLUtils.createElement(contentHandler, "context", context);
        
        Set<User> users = _rightsManager.getUsersByContextAndProfile(profileID, context);
        _saxUsers (users);
        
        Set<Group> groups = _rightsManager.getGroupsByContextAndProfile(profileID, context);
        _saxGroups(groups);
        
        XMLUtils.endElement(contentHandler, "assignment");
        contentHandler.endDocument();
    }
    
    
    
    private void _saxUsers (Set<User> users) throws SAXException
    {
        XMLUtils.startElement(contentHandler, "users");
        
        for (User user : users)
        {
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute("", "login", "login", "CDATA", user.getName());
            XMLUtils.createElement(contentHandler, "user", attr, user.getFullName());
        }
        
        XMLUtils.endElement(contentHandler, "users");
    }
    
    private void _saxGroups (Set<Group> groups) throws SAXException
    {
        XMLUtils.startElement(contentHandler, "groups");
        
        for (Group group : groups)
        {
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute("", "id", "id", "CDATA", group.getId());
            XMLUtils.createElement(contentHandler, "group", attr, group.getLabel());
        }
        
        XMLUtils.endElement(contentHandler, "groups");
    }

}
