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
package org.ametys.runtime.plugins.admin.version;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.core.version.Version;
import org.ametys.core.version.VersionsHandler;
import org.ametys.runtime.config.Config;

/**
 * SAXes the information provided by the VersionsHandler component.<br>
 * The format is : <br>
 * &lt;Versions&gt;<br>
 * &nbsp;&nbsp;&lt;Component&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;Name&gt;<i>Name of the component</i>&lt;/Name&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;Version&gt;<i>Version of the component</i>&lt;/Version&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;Date&gt;<i>Build date formatted with "dd/MM/yyyy HH:mm" format</i>&lt;/Date&gt;<br>
 * &nbsp;&nbsp;&lt;/Component&gt;<br>
 * &lt;/Versions&gt;
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
                
                if (version.getVersion() != null)
                {
                    XMLUtils.createElement(contentHandler, "Version", version.getVersion());
                }
                
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
