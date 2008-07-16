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
import java.util.TimeZone;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

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
        
        contentHandler.startDocument();
        
        XMLUtils.startElement(contentHandler, "Versions");
        
        for (Version version : versions)
        {
            XMLUtils.startElement(contentHandler, "Component");
            
            XMLUtils.createElement(contentHandler, "Name", version.getName());
            XMLUtils.createElement(contentHandler, "Version", version.getVersion());
            
            Date date = version.getDate();
            
            if (date != null)
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                
                String formattedDate = dateFormat.format(date);
                XMLUtils.createElement(contentHandler, "Date", formattedDate);
                
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                
                String formattedTime = timeFormat.format(date);
                XMLUtils.createElement(contentHandler, "Time", formattedTime);
            }
            
            XMLUtils.endElement(contentHandler, "Component");
        }
        
        XMLUtils.endElement(contentHandler, "Versions");
        
        contentHandler.endDocument();
    }
}
