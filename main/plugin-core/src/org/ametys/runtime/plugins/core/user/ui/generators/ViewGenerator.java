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
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.UsersManager;
import org.ametys.runtime.util.cocoon.CurrentUserProviderServiceableGenerator;


/**
 * Generates the users' screen information.<br/>
 */
public class ViewGenerator extends CurrentUserProviderServiceableGenerator
{
    private UsersManager _users;

    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
        _users = (UsersManager) m.lookup(UsersManager.ROLE);
    }

    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "UsersView");
        XMLUtils.createElement(contentHandler, "AdministratorUI", _isSuperUser() ? "true" : "false");
        if (_users instanceof ModifiableUsersManager)
        {
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute("", "Modifiable", "Modifiable", "CDATA", "true");
            XMLUtils.startElement(contentHandler, "Model", attrs);
            ModifiableUsersManager mum = (ModifiableUsersManager) _users;
            mum.saxModel(contentHandler);
            XMLUtils.endElement(contentHandler, "Model");
        }
        XMLUtils.endElement(contentHandler, "UsersView");
        contentHandler.endDocument();
    }
}
