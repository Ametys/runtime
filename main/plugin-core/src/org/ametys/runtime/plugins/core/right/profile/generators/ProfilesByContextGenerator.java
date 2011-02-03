/*
 *  Copyright 2010 Anyware Services
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
package org.ametys.runtime.plugins.core.right.profile.generators;

import java.io.IOException;
import java.util.Set;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.group.Group;
import org.ametys.runtime.plugins.core.right.profile.DefaultProfileBasedRightsManager;
import org.ametys.runtime.plugins.core.right.profile.Profile;
import org.ametys.runtime.plugins.core.right.profile.ProfileBasedRightsManager;
import org.ametys.runtime.right.HierarchicalRightsHelper;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.user.User;

/**
 * SAX the users and groups linked to a profile on a given context  
 */
public class ProfilesByContextGenerator extends ServiceableGenerator
{
    private DefaultProfileBasedRightsManager _rightsManager;

    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
        _rightsManager = (DefaultProfileBasedRightsManager) m.lookup(RightsManager.ROLE);
    }

    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String context = request.getParameter("context");
        String id = request.getParameter("profile");

        contentHandler.startDocument();

        if (StringUtils.isEmpty(context))
        {
            XMLUtils.createElement(contentHandler, "profiles");
        }
        else if (StringUtils.isNotEmpty(id))
        {
            Profile profile = _rightsManager.getProfile(id);
            _saxProfile(profile, context);
        }
        else
        {
            XMLUtils.startElement(contentHandler, "profiles");
            
            for (Profile profile : ((ProfileBasedRightsManager) _rightsManager).getProfiles())
            {
                _saxProfile(profile, context);
            }
            
            XMLUtils.endElement(contentHandler, "profiles");
        }

       
        contentHandler.endDocument();
    }

    private void _saxProfile(Profile profile, String context) throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "id", "id", "CDATA", "profile-" + profile.getId());
        attr.addAttribute("", "label", "label", "CDATA", profile.getName());
        XMLUtils.startElement(contentHandler, "profile", attr);

        Set<User> users = _rightsManager.getUsersByContextAndProfile(profile.getId(), context);
        for (User user : users)
        {
            _saxUser(user, context, false);
        }
        
        // SAX inheritance
        _saxUsersByInheritance (profile.getId(), context);

        Set<Group> groups = _rightsManager.getGroupsByContextAndProfile(profile.getId(), context);
        for (Group group : groups)
        {
            _saxGroup(group, context, false);
        }
        
        // SAX inheritance
        _saxGroupsByInheritance (profile.getId(), context);

        XMLUtils.endElement(contentHandler, "profile");
    }
    
    private void _saxUsersByInheritance (String profileId, String context) throws SAXException
    {
        String transiantContext = context;
        
        transiantContext = HierarchicalRightsHelper.getParentContext(transiantContext);
        
        while (transiantContext != null)
        {
            Set<User> users = _rightsManager.getUsersByContextAndProfile(profileId, transiantContext);
            for (User user : users)
            {
                _saxUser(user, transiantContext, true);
            }
            transiantContext = HierarchicalRightsHelper.getParentContext(transiantContext);
        }
    }
    
    private void _saxGroupsByInheritance (String profileId, String context) throws SAXException
    {
        String transiantContext = context;
        
        transiantContext = HierarchicalRightsHelper.getParentContext(transiantContext);
        
        while (transiantContext != null)
        {
            Set<Group> groups = _rightsManager.getGroupsByContextAndProfile(profileId, transiantContext);
            for (Group group : groups)
            {
                _saxGroup(group, transiantContext, true);
            }
            transiantContext = HierarchicalRightsHelper.getParentContext(transiantContext);
        }
    }

    private void _saxUser(User user, String context, boolean inherit) throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "login", "login", "CDATA", user.getName());
        attr.addAttribute("", "name", "name", "CDATA", user.getFullName());
        attr.addAttribute("", "context", "context", "CDATA", context);
        attr.addAttribute("", "inherit", "inherit", "CDATA", String.valueOf(inherit));
        
        XMLUtils.createElement(contentHandler, "user", attr);
    }

    private void _saxGroup(Group group, String context, boolean inherit) throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "id", "id", "CDATA", "group-" + group.getId());
        attr.addAttribute("", "label", "label", "CDATA", group.getLabel());
        attr.addAttribute("", "context", "context", "CDATA", context);
        attr.addAttribute("", "inherit", "inherit", "CDATA", String.valueOf(inherit));
        
        XMLUtils.createElement(contentHandler, "group", attr);
    }

}
