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
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.user.UsersManager;


/**
 * Generate information to view or edit one user 
 */
public class UserInformationGenerator extends ServiceableGenerator
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
        // Envoie les données
        contentHandler.startDocument();
        
        AttributesImpl attrs = new AttributesImpl();
        XMLUtils.startElement(contentHandler, "users-info", attrs);
        
        if (source != null && source.length() != 0)
        {
            Map<String, String> params = new HashMap<String, String>();
            params.put("pattern", source);
            _users.toSAX(contentHandler, Integer.MAX_VALUE, 0, params);
        }

        XMLUtils.endElement(contentHandler, "users-info");
        
        contentHandler.endDocument();
    }

}
