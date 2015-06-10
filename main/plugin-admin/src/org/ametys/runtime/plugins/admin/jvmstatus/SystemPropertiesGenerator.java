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
package org.ametys.runtime.plugins.admin.jvmstatus;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

/**
 * SAXes the system properties 
 */
public class SystemPropertiesGenerator extends AbstractGenerator
{
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        RuntimeMXBean rBean = ManagementFactory.getRuntimeMXBean();

        XMLUtils.startElement(contentHandler, "properties");

        Map<String, String> properties = rBean.getSystemProperties();
        for (String key : properties.keySet())
        {
            if (key.indexOf(":") == -1)
            {   
                AttributesImpl attrs = new AttributesImpl();
                attrs.addCDATAAttribute("name", key);
                attrs.addCDATAAttribute("value", properties.get(key));
                
                XMLUtils.createElement(contentHandler, "property", attrs);
            }
        }
        
        XMLUtils.endElement(contentHandler, "properties");
        contentHandler.endDocument();
    }
}
