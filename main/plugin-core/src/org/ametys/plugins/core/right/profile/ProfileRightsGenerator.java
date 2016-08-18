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
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import org.ametys.core.right.Profile;
import org.ametys.core.right.Right;
import org.ametys.core.right.RightsExtensionPoint;
import org.ametys.core.right.RightManager;

/**
 * Generates the rights of a profile
 */
public class ProfileRightsGenerator extends ServiceableGenerator
{
    private RightsExtensionPoint _rights;
    private RightManager _rightManager;
    
    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
        _rights = (RightsExtensionPoint) m.lookup(RightsExtensionPoint.ROLE);
        _rightManager = (RightManager) m.lookup(RightManager.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String id = request.getParameter("id");
        
        contentHandler.startDocument();
        
        if (StringUtils.isEmpty(id))
        {
            XMLUtils.startElement(contentHandler, "profiles");
            
                
            Set<Profile> profiles = _rightManager.getAllProfiles();
            for (Profile profile : profiles)
            {
                _saxProfileRights(profile);
            }
            
            XMLUtils.endElement(contentHandler, "profiles");
        }
        else
        {
            Profile profile = _rightManager.getProfile(id);
            if (profile != null)
            {
                _saxProfileRights (profile);
            }
            else
            {
                XMLUtils.createElement(contentHandler, "profile");
            }
        }
        
        contentHandler.endDocument();
    }

    private void _saxProfileRights (Profile profile) throws SAXException
    {
        AttributesImpl attr = new AttributesImpl();
        attr.addCDATAAttribute("id", profile.getId());
        XMLUtils.startElement(contentHandler, "profile", attr);
        
        Set<String> rights = profile.getRights();
        for (String rightId : rights)
        {
            Right right = _rights.getExtension(rightId);
            if (right != null)
            {
                right.toSAX(contentHandler);
            }
        }
        
        XMLUtils.endElement(contentHandler, "profile");
    }
}
