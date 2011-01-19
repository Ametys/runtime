/*
 *  Copyright 2009 Anyware Services
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
