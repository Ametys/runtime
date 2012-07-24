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
package org.ametys.runtime.plugins.core.right.profile.generators;

import java.io.IOException;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugins.core.right.RightsExtensionPoint;
import org.ametys.runtime.plugins.core.right.profile.Profile;
import org.ametys.runtime.plugins.core.right.profile.ProfileBasedRightsManager;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableGenerator;


/**
 * Generates default profiles
 */
public class ProfilesGenerator extends CurrentUserProviderServiceableGenerator
{
    private RightsExtensionPoint _rights;
    private RightsManager _rightsManager;
    
    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
        _rights = (RightsExtensionPoint) m.lookup(RightsExtensionPoint.ROLE);
        _rightsManager = (RightsManager) m.lookup(RightsManager.ROLE);
    }
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        
        XMLUtils.startElement(contentHandler, "ProfilesManager");
        
        XMLUtils.startElement(contentHandler, "rights");
        _rights.toSAX(contentHandler);
        XMLUtils.endElement(contentHandler, "rights");
        
        if (_rightsManager instanceof ProfileBasedRightsManager)
        {
            XMLUtils.startElement(contentHandler, "profiles");
            for (Profile profile : ((ProfileBasedRightsManager) _rightsManager).getProfiles())
            {
                profile.toSAX(contentHandler);
            }
            XMLUtils.endElement(contentHandler, "profiles");
        }
        else
        {
            XMLUtils.createElement(contentHandler, "not-supported");
        }
        
        
        XMLUtils.createElement(contentHandler, "AdministratorUI", _isSuperUser() ? "true" : "false");
        
        XMLUtils.endElement(contentHandler, "ProfilesManager");
        
        contentHandler.endDocument();
    }
}
