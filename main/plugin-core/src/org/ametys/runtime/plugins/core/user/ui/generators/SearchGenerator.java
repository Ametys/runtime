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
package org.ametys.runtime.plugins.core.user.ui.generators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.user.UsersManager;

/**
 * Generates the result of a search in the users. 
 */
public class SearchGenerator extends ServiceableGenerator
{
    private static final int _DEFAULT_COUNT_VALUE = 100;
    private static final int _DEFAULT_OFFSET_VALUE = 0;

    public void generate() throws IOException, SAXException, ProcessingException
    {
        // Critère de recherche
        Map<String, String> saxParameters = new HashMap<String, String>();
        saxParameters.put("pattern", source);

        // Nombre de résultats max
        int count = parameters.getParameterAsInteger("limit", _DEFAULT_COUNT_VALUE);
        if (count == -1)
        {
            count = Integer.MAX_VALUE;
        }

        // Décalage des résultats
        int offset = parameters.getParameterAsInteger("start", _DEFAULT_OFFSET_VALUE);
        
        // Get the wanted UsersManager avalon role, defaults to runtime-declared UsersManager.
        String role = parameters.getParameter("usersManagerRole", UsersManager.ROLE);
        if (role.length() == 0)
        {
            role = UsersManager.ROLE;
        }
        
        UsersManager usersManager = null;
        
        try
        {
            usersManager = (UsersManager) manager.lookup(role);
            
            contentHandler.startDocument();
            XMLUtils.startElement(contentHandler, "Search");
            usersManager.toSAX(contentHandler, count, offset, saxParameters);
            XMLUtils.endElement(contentHandler, "Search");
            contentHandler.endDocument();
        }
        catch (ServiceException e)
        {
            getLogger().error("Error looking up UsersManager of role " + role, e);
            throw new ProcessingException("Error looking up UsersManager of role " + role, e);
        }
        finally
        {
            manager.release(usersManager);
        }
    }
}
