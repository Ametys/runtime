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
package org.ametys.runtime.plugins.core.administrator.system;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.Source;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.IgnoreRootHandler;


/**
 * SAXes general information about the application : JVM status, sessions count, system properties, ...
 */
public class SystemGenerator extends ServiceableGenerator
{
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "System");
        
//        AttributesImpl maintenanceAttrs = new AttributesImpl();
//        maintenanceAttrs.addAttribute("", "state", "state", "CDATA", RuntimeServlet.getRunMode() == RuntimeServlet.RunMode.MAINTENANCE ? "on" : "off");
//        XMLUtils.createElement(contentHandler, "maintenance", maintenanceAttrs);

        // Read the file
        Source systemSource = null;
        try
        {
            systemSource = resolver.resolveURI("context://" + SystemHelper.ADMINISTRATOR_SYSTEM_FILE);
            if (systemSource.exists())
            {
                SourceUtil.parse(manager, systemSource, new IgnoreRootHandler(contentHandler));
            }
        }
        catch (Exception e)
        {
            getLogger().error("Administrator try to read the system properties but an error occured while reading the file '" + SystemHelper.ADMINISTRATOR_SYSTEM_FILE + "'", e);
            throw new ProcessingException(e);
        }
        finally 
        {
            if (systemSource != null)
            {
                resolver.release(systemSource);
            }
        }
        
        XMLUtils.endElement(contentHandler, "System");
        contentHandler.endDocument();
    }
}
