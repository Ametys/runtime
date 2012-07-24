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
package org.ametys.runtime.plugins.core.user.ui.generators;

import java.io.IOException;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.UsersManager;


/**
 * Generate information to create the user's dialog box 
 */
public class ModelGenerator extends ServiceableGenerator
{
    public void generate() throws IOException, SAXException, ProcessingException
    {
        String role = parameters.getParameter("usersManagerRole", UsersManager.ROLE);
        if (role.length() == 0)
        {
            role = UsersManager.ROLE;
        }
        
        try
        {
            UsersManager u = (UsersManager) manager.lookup(role);
            if (!(u instanceof ModifiableUsersManager))
            {
                throw new IllegalArgumentException("Users are not modifiable !");
            }
            
            ModifiableUsersManager users = (ModifiableUsersManager) u;
            
            contentHandler.startDocument();
            
            XMLUtils.startElement(contentHandler, "Model");
            users.saxModel(contentHandler);
            XMLUtils.endElement(contentHandler, "Model");
            
            contentHandler.endDocument();
        }
        catch (ServiceException e)
        {
            throw new ProcessingException(e);
        }
    }

}
