/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.runtime.plugins.core.administrator.jvmstatus;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Date;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.parameter.ParameterHelper;


/**
 * SAXes general information about the system 
 */
public class GeneralStatusGenerator extends AbstractGenerator
{
    @Override
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean rBean = ManagementFactory.getRuntimeMXBean();
        
        XMLUtils.startElement(contentHandler, "characteristics");
        XMLUtils.createElement(contentHandler, "osTime", ParameterHelper.valueToString(new Date()));
        
        XMLUtils.createElement(contentHandler, "osName", osBean.getName());
        XMLUtils.createElement(contentHandler, "osVersion", osBean.getVersion());
        
        String osPatch = System.getProperty("sun.os.patch.level");
        if (StringUtils.isNotEmpty(osPatch))
        {
            XMLUtils.createElement(contentHandler, "osPatch", osPatch);
        }
        
        XMLUtils.createElement(contentHandler, "availableProc", String.valueOf(osBean.getAvailableProcessors()));
        XMLUtils.createElement(contentHandler, "architecture", osBean.getArch());
        
        XMLUtils.createElement(contentHandler, "javaVendor", System.getProperty("java.vendor"));
        XMLUtils.createElement(contentHandler, "javaVersion", System.getProperty("java.version"));
        XMLUtils.createElement(contentHandler, "jvmName", rBean.getVmName());
        XMLUtils.createElement(contentHandler, "startTime", ParameterHelper.valueToString(new Date(rBean.getStartTime())));
        
        XMLUtils.endElement(contentHandler, "characteristics");
        contentHandler.endDocument();
    }
}
