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
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _element = configuration.getChild("element").getValue("Desktop");
    }
    
    @Override
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
