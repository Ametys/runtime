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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugins.core.right.profile.Profile;
import org.ametys.runtime.plugins.core.right.profile.ProfileBasedRightsManager;
import org.ametys.runtime.right.RightsManager;


/**
 * Generates profiles
 */
public class ProfilesListGenerator extends ServiceableGenerator
{
    private RightsManager _rightsManager;
    
    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
        _rightsManager = (RightsManager) m.lookup(RightsManager.ROLE);
    }

    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();

        XMLUtils.startElement(contentHandler, "Profiles");
        
        if (!(_rightsManager instanceof ProfileBasedRightsManager))
        {
            throw new IllegalStateException("You cannot display/edit profiles with the current rights manager '" + _rightsManager.getClass().getName() + "'");
        }
        
        for (Profile profile : ((ProfileBasedRightsManager) _rightsManager).getProfiles())
        {
            profile.toSAX(contentHandler);
        }
        
        XMLUtils.endElement(contentHandler, "Profiles");
        
        contentHandler.endDocument();
    }
}
