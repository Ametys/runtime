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
package org.ametys.runtime.plugins.core.administrator.logs;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.serialization.ZipArchiveSerializer;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

/**
 * Generate the list of file to include in a zip for downloading logs 
 */
public class LogsDownloadGenerator extends AbstractGenerator
{
    
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String[] files = request.getParameterValues("file");
        
        contentHandler.startDocument();
        contentHandler.startPrefixMapping("zip", ZipArchiveSerializer.ZIP_NAMESPACE);
        XMLUtils.startElement(contentHandler, ZipArchiveSerializer.ZIP_NAMESPACE, "archive");
        
        for (String file : files)
        {
            AttributesImpl zipAttrs = new AttributesImpl();
            zipAttrs.addAttribute("", "name", "name", "CDATA", file);
            zipAttrs.addAttribute("", "src", "src", "CDATA", "context://WEB-INF/logs/" + file);
            XMLUtils.startElement(contentHandler, ZipArchiveSerializer.ZIP_NAMESPACE, "entry", zipAttrs);
            XMLUtils.endElement(contentHandler, ZipArchiveSerializer.ZIP_NAMESPACE, "entry");
        }
        
        XMLUtils.endElement(contentHandler, ZipArchiveSerializer.ZIP_NAMESPACE, "archive");
        contentHandler.endPrefixMapping("zip");
        contentHandler.endDocument();
    }
}
