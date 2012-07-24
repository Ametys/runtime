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
package org.ametys.runtime.plugins.core.administrator.system;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collection;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugins.core.administrator.version.Version;
import org.ametys.runtime.plugins.core.administrator.version.VersionsHandler;

/**
 * Generate the startuptime of the server 
 */
public class StartupTimeGenerator extends ServiceableGenerator
{
    private static String __version;
    
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        if (__version == null)
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
    
            StringBuffer value = new StringBuffer();
            
            Collection<Version> versions = handler.getVersions();
            for (Version version : versions)
            {
                if (value.length() > 0)
                {
                    value.append(" / ");
                }
                
                value.append(version.getName());
                
                if (StringUtils.isNotBlank(version.getVersion()))
                {
                    value.append(" - ");
                    value.append(version.getVersion());
                }
                
                if (version.getDate() != null)
                {
                    value.append(" - ");
                    value.append(Long.toString(version.getDate().getTime()));
                }
            }
            
            __version = Integer.toString(value.toString().hashCode());
        }
        
        AttributesImpl attrs = new AttributesImpl();
        attrs.addCDATAAttribute("version", __version);
        
        contentHandler.startDocument();
        XMLUtils.createElement(contentHandler, "startup-time", attrs, Long.toString(ManagementFactory.getRuntimeMXBean().getStartTime()));
        contentHandler.endDocument();
    }
}
