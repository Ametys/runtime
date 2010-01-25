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
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.user.ModifiableUsersManager;
import org.ametys.runtime.user.UsersManager;


/**
 * Generate information to create the user's dialog box 
 */
public class ModelGenerator extends ServiceableGenerator
{
    private ModifiableUsersManager _users;
    
    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
        
        UsersManager um = (UsersManager) m.lookup(UsersManager.ROLE);
        if (!(um instanceof ModifiableUsersManager))
        {
            throw new ServiceException(UsersManager.ROLE, "The component [" + UsersManager.ROLE + "] is not instanceof ModifiableUsersManager");
        }
        _users = (ModifiableUsersManager) um;
    }
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        // Envoie les données
        contentHandler.startDocument();
        
        XMLUtils.startElement(contentHandler, "Model");
        _users.saxModel(contentHandler);
        XMLUtils.endElement(contentHandler, "Model");
        
        contentHandler.endDocument();
    }

}
