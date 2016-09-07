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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.core.right.Profile;
import org.ametys.core.right.RightsExtensionPoint;
import org.ametys.plugins.core.right.ProfilesListGenerator;


/**
 * Generates default profiles
 */
public class ProfilesGenerator extends ProfilesListGenerator
{
    private RightsExtensionPoint _rights;
    
    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
        _rights = (RightsExtensionPoint) m.lookup(RightsExtensionPoint.ROLE);
    }
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        
        XMLUtils.startElement(contentHandler, "ProfilesManager");
        
        XMLUtils.startElement(contentHandler, "rights");
        _rights.toSAX(contentHandler);
        XMLUtils.endElement(contentHandler, "rights");
        
        XMLUtils.startElement(contentHandler, "profiles");
        for (Profile profile : _profilesDAO.getProfiles())
        {
            saxProfile(profile);
        }
        XMLUtils.endElement(contentHandler, "profiles");
        
        XMLUtils.createElement(contentHandler, "AdministratorUI", "false");
        
        XMLUtils.endElement(contentHandler, "ProfilesManager");
        
        contentHandler.endDocument();
    }
}
