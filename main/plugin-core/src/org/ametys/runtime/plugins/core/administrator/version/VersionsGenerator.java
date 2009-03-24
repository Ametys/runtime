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
package org.ametys.runtime.plugins.core.administrator.version;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.config.Config;

/**
 * SAXes the information provided by the VersionsHandler component.<br>
 * The format is : <br>
 * &lt;Versions><br>
 * &nbsp;&nbsp;&lt;Component><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;Name><i>Name of the component</i>&lt;/Name><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;Version><i>Version of the component</i>&lt;/Version><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;Date><i>Build date formatted with "dd/MM/yyyy HH:mm" format</i>&lt;/Date><br>
 * &nbsp;&nbsp;&lt;/Component><br>
 * &lt;/Versions>
 */
public class VersionsGenerator extends ServiceableGenerator
{
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "Versions");
        
        if (Config.getInstance() != null)
        {
            VersionsHandler handler;
            
            try
            {
                handler = (VersionsHandler) manager.lookup(VersionsHandler.ROLE);
            }
            catch (ServiceException e)
            {
                String errorMessage = "Unable to get the VersionsHandler";
                getLogger().error(errorMessage, e);
                throw new ProcessingException(errorMessage, e);
            }
            
            Collection<Version> versions = handler.getVersions();
            for (Version version : versions)
            {
                XMLUtils.startElement(contentHandler, "Component");
                
                XMLUtils.createElement(contentHandler, "Name", version.getName());
                XMLUtils.createElement(contentHandler, "Version", version.getVersion());
                
                Date date = version.getDate();
                
                if (date != null)
                {
                    String formattedDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
                    XMLUtils.createElement(contentHandler, "Date", formattedDate);
                    
                    String formattedTime = new SimpleDateFormat("HH:mm").format(date);
                    XMLUtils.createElement(contentHandler, "Time", formattedTime);
                }
                
                XMLUtils.endElement(contentHandler, "Component");
            }
        }
        
        XMLUtils.endElement(contentHandler, "Versions");
        contentHandler.endDocument();
    }
}
