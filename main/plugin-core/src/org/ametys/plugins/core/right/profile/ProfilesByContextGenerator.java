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
package org.ametys.plugins.core.right.profile;

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

import org.ametys.core.group.Group;
import org.ametys.core.right.HierarchicalRightsHelper;
import org.ametys.core.right.RightsManager;
import org.ametys.core.right.profile.Profile;
import org.ametys.core.user.User;
import org.ametys.plugins.core.impl.right.profile.DefaultProfileBasedRightsManager;
import org.ametys.plugins.core.impl.right.profile.ProfileBasedRightsManager;

/**
 * SAX the users and groups linked to a profile on a given context  
 */
public class ProfilesByContextGenerator extends ServiceableGenerator
{
    /** The rights manager */
    protected DefaultProfileBasedRightsManager _rightsManager;

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
        String context = request.getParameter("profileContext");
        String id = request.getParameter("profile");

        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "nodes");
        
        if (StringUtils.isNotEmpty(id))
        {
            Profile profile = _rightsManager.getProfile(id);
            _saxProfile(profile, context, false);
        }
        else
        {
            for (Profile profile : ((ProfileBasedRightsManager) _rightsManager).getProfiles())
            {
                _saxProfile(profile, context, true);
            }
        }
    
        XMLUtils.endElement(contentHandler, "nodes");
        contentHandler.endDocument();
    }

    /**
     * SAX Profile
     * @param profile the profile to SAX
     * @param context the context of right
     * @param includeRootNode Should include a root node
     * @throws SAXException if an error occurred
     */
    protected void _saxProfile(Profile profile, String context, boolean includeRootNode) throws SAXException
    {
        if (includeRootNode)
        {
            AttributesImpl attr = new AttributesImpl();
            _addProfileAttributes (profile, attr);
            
            XMLUtils.startElement(contentHandler, "node", attr);
        }
     
        
        String profileId = profile.getId();
       
        _saxUsers(profileId, context);
        _saxUsersByInheritance(profileId, context);
       
        _saxGroups(profileId, context);
        _saxGroupsByInheritance(profileId, context);
        
        if (includeRootNode)
        {
            XMLUtils.endElement(contentHandler, "node");
        }
    }
    
    private void _saxUsers (String profileId, String context) throws SAXException
    {
        Set<User> users = _rightsManager.getUsersByContextAndProfile(profileId, context);
        for (User user : users)
        {
            AttributesImpl attrs = new AttributesImpl();
            _addUserAttributes (attrs, user, context, false);
            XMLUtils.createElement(contentHandler, "node", attrs);
        }
    }
    
    private void _saxGroups (String profileId, String context) throws SAXException
    {
        Set<Group> groups = _rightsManager.getGroupsByContextAndProfile(profileId, context);
        for (Group group : groups)
        {
            AttributesImpl attrs = new AttributesImpl();
            _addGroupAttributes (attrs, group, context, false);
            XMLUtils.createElement(contentHandler, "node", attrs);
        }
    }
    
    private void _saxUsersByInheritance(String profileId, String context) throws SAXException
    {
        String transiantContext = context;
        
        transiantContext = HierarchicalRightsHelper.getParentContext(transiantContext);
        
        while (transiantContext != null)
        {
            Set<User> users = _rightsManager.getUsersByContextAndProfile(profileId, transiantContext);
            for (User user : users)
            {
                AttributesImpl attrs = new AttributesImpl();
                _addUserAttributes (attrs, user, transiantContext, true);
                XMLUtils.createElement(contentHandler, "node", attrs);
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
                AttributesImpl attrs = new AttributesImpl();
                _addGroupAttributes (attrs, group, transiantContext, true);
                XMLUtils.createElement(contentHandler, "node", attrs);
            }
            transiantContext = HierarchicalRightsHelper.getParentContext(transiantContext);
        }
    }
    
    private void _addProfileAttributes(Profile profile, AttributesImpl attrs)
    {
        attrs.addAttribute("", "profileId", "profileId", "CDATA", profile.getId());
        attrs.addAttribute("", "name", "name", "CDATA", profile.getName());
        attrs.addAttribute("", "type", "type", "CDATA", "profile");
    }
    
    private void _addUserAttributes (AttributesImpl attrs, User user, String context, boolean inherit)
    {
        attrs.addAttribute("", "login", "login", "CDATA", user.getName());
        attrs.addAttribute("", "name", "name", "CDATA", user.getFullName());
        attrs.addAttribute("", "context", "context", "CDATA", context);
        attrs.addAttribute("", "inherit", "inherit", "CDATA", String.valueOf(inherit));
        attrs.addAttribute("", "type", "type", "CDATA", "user");
    }
    
    private void _addGroupAttributes(AttributesImpl attrs, Group group, String context, boolean inherit)
    {
        attrs.addAttribute("", "groupId", "groupId", "CDATA", "group-" + group.getId());
        attrs.addAttribute("", "name", "name", "CDATA", group.getLabel());
        attrs.addAttribute("", "context", "context", "CDATA", context);
        attrs.addAttribute("", "inherit", "inherit", "CDATA", String.valueOf(inherit));
        attrs.addAttribute("", "type", "type", "CDATA", "group");
    }
}
