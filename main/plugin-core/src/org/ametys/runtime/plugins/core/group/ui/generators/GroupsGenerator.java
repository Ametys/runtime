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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.group.ModifiableGroupsManager;
import org.ametys.runtime.util.cocoon.AbstractCurrentUserProviderServiceableGenerator;


/**
 * Generates groups.
 */
public class GroupsGenerator extends AbstractCurrentUserProviderServiceableGenerator
{
    private static final int _DEFAULT_COUNT_VALUE = Integer.MAX_VALUE;
    private static final int _DEFAULT_OFFSET_VALUE = 0;
    private GroupsManager _groups;
    
    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
        _groups = (GroupsManager) m.lookup(GroupsManager.ROLE);
    }
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        
        // Nombre de résultats max
        int count = parameters.getParameterAsInteger("limit", _DEFAULT_COUNT_VALUE);
        if (count == -1)
        {
            count = Integer.MAX_VALUE;
        }

        // Décalage des résultats
        int offset = parameters.getParameterAsInteger("start", _DEFAULT_OFFSET_VALUE);
        
        XMLUtils.startElement(contentHandler, "GroupsManager");
        
        _groups.toSAX(contentHandler, count, offset, new HashMap());
        
        XMLUtils.createElement(contentHandler, "total", String.valueOf(_groups.getGroups().size()));
        XMLUtils.createElement(contentHandler, "Modifiable", _groups instanceof ModifiableGroupsManager ? "true" : "false");
        XMLUtils.createElement(contentHandler, "AdministratorUI", _isSuperUser() ? "true" : "false");
        
        XMLUtils.endElement(contentHandler, "GroupsManager");
        
        contentHandler.endDocument();

    }

}
