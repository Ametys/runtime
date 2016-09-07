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
package org.ametys.plugins.core.right;

import java.io.IOException;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.core.right.Profile;
import org.ametys.core.right.RightProfilesDAO;


/**
 * Generates profiles
 */
public class ProfilesListGenerator extends ServiceableGenerator
{
    /** The profiles DAO */
    protected RightProfilesDAO _profilesDAO;
    
    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
        _profilesDAO = (RightProfilesDAO) m.lookup(RightProfilesDAO.ROLE);
    }

    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();

        XMLUtils.startElement(contentHandler, "Profiles");
        
        for (Profile profile : _profilesDAO.getProfiles())
        {
            saxProfile(profile);
        }
        
        XMLUtils.endElement(contentHandler, "Profiles");
        
        contentHandler.endDocument();
    }
    
    /**
     * SAX a profile with its rights
     * @param profile The profile
     * @throws SAXException if an error occurred while saxing
     */
    protected void saxProfile (Profile profile) throws SAXException
    {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "id", "id", "CDATA", profile.getId());
        XMLUtils.startElement(contentHandler, "profile", atts);

        XMLUtils.createElement(contentHandler, "label", profile.getLabel());

        String context = profile.getContext();
        if (context != null)
        {
            XMLUtils.createElement(contentHandler, "context", context);
        }

        contentHandler.startElement("", "rights", "rights", new AttributesImpl());

        for (String right : _profilesDAO.getRights(profile))
        {
            AttributesImpl attsRight = new AttributesImpl();
            attsRight.addAttribute("", "id", "id", "CDATA", right);
            XMLUtils.createElement(contentHandler, "right", attsRight);
        }

        XMLUtils.endElement(contentHandler, "rights");
        XMLUtils.endElement(contentHandler, "profile");
    }
}
