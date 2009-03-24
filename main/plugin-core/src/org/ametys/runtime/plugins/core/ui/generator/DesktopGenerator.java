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
package org.ametys.runtime.plugins.core.ui.generator;

import java.io.IOException;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.plugins.core.ui.item.DesktopManager;


/**
 * Generates desktop information for admin space 
 */
public class DesktopGenerator extends ServiceableGenerator implements Configurable
{
    private String _element;
    
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _element = configuration.getChild("element").getValue("Desktop");
    }
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        String role = null;
        try
        {
            role = parameters.getParameter("DesktopManager.ROLE");
        }
        catch (ParameterException e)
        {
            String errorMessage = "Parameter 'DesktopManager.ROLE' is missing";
            getLogger().error(errorMessage, e);
            throw new ProcessingException(errorMessage, e);
        }
        
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, _element);
        if (Config.getInstance() != null)
        {
            DesktopManager desktopManager = null;
            try
            {
                desktopManager = (DesktopManager) manager.lookup(role);
            }
            catch (ServiceException e)
            {
                String errorMessage = "Parameter 'DesktopManager.ROLE' specify unexisting role '" + role + "'";
                getLogger().error(errorMessage, e);
                throw new ProcessingException(errorMessage, e);
            }
            desktopManager.toSAX(contentHandler);
            
        }
        XMLUtils.endElement(contentHandler, _element);
        contentHandler.endDocument();
    }
}
