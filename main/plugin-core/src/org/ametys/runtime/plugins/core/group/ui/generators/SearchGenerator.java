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
package org.ametys.runtime.plugins.core.group.ui.generators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.group.GroupsManager;

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
        Map<String, String> saxParameters = new HashMap<>();
        saxParameters.put("pattern", source);
        
        // Nombre de résultats max
        int limit = parameters.getParameterAsInteger("limit", _DEFAULT_COUNT_VALUE);
        if (limit == -1)
        {
            limit = Integer.MAX_VALUE;
        }

        // Décalage des résultats
        int start = parameters.getParameterAsInteger("start", _DEFAULT_OFFSET_VALUE);
        
        // Get the wanted GroupsManager avalon role, defaults to runtime-declared GroupsManager.
        String role = parameters.getParameter("groupsManagerRole", GroupsManager.ROLE);
        if (role.length() == 0)
        {
            role = GroupsManager.ROLE;
        }
        
        GroupsManager groupsManager = null;
        
        try
        {
            groupsManager = (GroupsManager) manager.lookup(role);
            
            contentHandler.startDocument();
            XMLUtils.startElement(contentHandler, "Search");
            groupsManager.toSAX(contentHandler, limit, start, saxParameters);
            XMLUtils.endElement(contentHandler, "Search");
            contentHandler.endDocument();
        }
        catch (ServiceException e)
        {
            getLogger().error("Error looking up GroupsManager of role " + role, e);
            throw new ProcessingException("Error looking up GroupsManager of role " + role, e);
        }
        finally
        {
            manager.release(groupsManager);
        }
    }
}
