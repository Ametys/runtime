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
package org.ametys.runtime.plugins.core.right.profile.generators;

import java.io.IOException;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugins.core.right.RightsExtensionPoint;
import org.ametys.runtime.plugins.core.right.profile.Profile;
import org.ametys.runtime.plugins.core.right.profile.ProfileBasedRightsManager;
import org.ametys.runtime.right.RightsManager;
import org.ametys.runtime.user.UserHelper;


/**
 * Generates profiles
 */
public class ProfilesGenerator extends ServiceableGenerator
{
    private RightsExtensionPoint _rights;
    private ProfileBasedRightsManager _rightsManager;
    
    @Override
    public void service(ServiceManager m) throws ServiceException
    {
        super.service(m);
        _rights = (RightsExtensionPoint) m.lookup(RightsExtensionPoint.ROLE);
        _rightsManager = (ProfileBasedRightsManager) m.lookup(RightsManager.ROLE);
    }
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        
        XMLUtils.startElement(contentHandler, "ProfilesManager");
        
        XMLUtils.startElement(contentHandler, "rights");
        _rights.toSAX(contentHandler);
        XMLUtils.endElement(contentHandler, "rights");
        
        XMLUtils.startElement(contentHandler, "profiles");
        for (Profile profile : _rightsManager.getProfiles())
        {
            profile.toSAX(contentHandler);
        }
        XMLUtils.endElement(contentHandler, "profiles");
        
        XMLUtils.createElement(contentHandler, "AdministratorUI", UserHelper.isAdministrator(objectModel) ? "true" : "false");
        
        XMLUtils.endElement(contentHandler, "ProfilesManager");
        
        contentHandler.endDocument();
    }
}
