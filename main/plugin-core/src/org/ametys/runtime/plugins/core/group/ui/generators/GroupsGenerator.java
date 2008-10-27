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
package org.ametys.runtime.plugins.core.group.ui.generators;

import java.io.IOException;
import java.util.HashMap;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.group.GroupsManager;
import org.ametys.runtime.group.ModifiableGroupsManager;
import org.ametys.runtime.user.UserHelper;


/**
 * Generates groups
 */
public class GroupsGenerator extends ServiceableGenerator
{
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
        
        XMLUtils.startElement(contentHandler, "GroupsManager");
        
        _groups.toSAX(contentHandler, Integer.MAX_VALUE, 0, new HashMap());
        
        XMLUtils.createElement(contentHandler, "Modifiable", _groups instanceof ModifiableGroupsManager ? "true" : "false");
        XMLUtils.createElement(contentHandler, "AdministratorUI", UserHelper.isAdministrator(objectModel) ? "true" : "false");
        
        XMLUtils.endElement(contentHandler, "GroupsManager");
        
        contentHandler.endDocument();

    }

}
