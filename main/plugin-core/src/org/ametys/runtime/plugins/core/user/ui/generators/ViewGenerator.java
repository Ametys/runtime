/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.plugins.core.user.ui.generators;

import java.io.IOException;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.UserHelper;
import org.ametys.runtime.user.UsersManager;


/**
 * Generates the users' screen information.<br/>
 */
public class ViewGenerator extends ServiceableGenerator
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
        XMLUtils.createElement(contentHandler, "AdministratorUI", UserHelper.isAdministrator(objectModel) ? "true" : "false");
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
