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
package org.ametys.runtime.plugins.core.group.ui.generators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.user.UsersManager;


/**
 * Generate information to view or edit one user 
 */
public class GroupInformationGenerator extends ServiceableGenerator
{
    public void generate() throws IOException, SAXException, ProcessingException
    {
        String role = parameters.getParameter("groupsManagerRole", UsersManager.ROLE);
        if (role.length() == 0)
        {
            role = GroupsManager.ROLE;
        }
        
        try
        {
            GroupsManager groups = (GroupsManager) manager.lookup(role);
            
            contentHandler.startDocument();
            
            AttributesImpl attrs = new AttributesImpl();
            XMLUtils.startElement(contentHandler, "groups-info", attrs);
            
            if (source != null && source.length() != 0)
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("pattern", source);
                groups.toSAX(contentHandler, Integer.MAX_VALUE, 0, params);
            }

            XMLUtils.endElement(contentHandler, "groups-info");
            
            contentHandler.endDocument();
        }
        catch (ServiceException e)
        {
            throw new ProcessingException(e);
        }
    }

}