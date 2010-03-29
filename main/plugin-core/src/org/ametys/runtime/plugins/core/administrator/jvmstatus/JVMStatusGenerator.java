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
package org.ametys.runtime.plugins.core.administrator.jvmstatus;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugins.core.monitoring.MonitoringExtensionPoint;
import org.ametys.runtime.plugins.core.monitoring.SampleManager;
import org.ametys.runtime.plugins.core.monitoring.MonitoringConstants.Period;
import org.ametys.runtime.util.parameter.ParameterHelper;


/**
 * SAXes general information about the application : JVM status, sessions count, system properties, ...
 */
public class JVMStatusGenerator extends ServiceableGenerator
{
    private MonitoringExtensionPoint _monitoringExtensionPoint;

    @Override
    public void service(ServiceManager smanager) throws ServiceException
    {
        super.service(smanager);
        _monitoringExtensionPoint = (MonitoringExtensionPoint) smanager.lookup(MonitoringExtensionPoint.ROLE);
    }
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        
        contentHandler.startDocument();
        contentHandler.startElement("", "status", "status", new AttributesImpl());
        
        _generalStatus();
        if (parameters.getParameterAsBoolean("properties", true))
        {
            _caracteristics();
            _properties();
            _samples();
        }
        
        contentHandler.endElement("", "status", "status");
        contentHandler.endDocument();
    }

    private void _caracteristics() throws SAXException
    {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        RuntimeMXBean rBean = ManagementFactory.getRuntimeMXBean();
        
        XMLUtils.startElement(contentHandler, "caracteristics");

        XMLUtils.createElement(contentHandler, "osName", String.valueOf(osBean.getName()));
        XMLUtils.createElement(contentHandler, "osVersion", String.valueOf(osBean.getVersion()));
        XMLUtils.createElement(contentHandler, "osPatch", String.valueOf(rBean.getSystemProperties().get("sun.os.patch.level")));
        
        XMLUtils.createElement(contentHandler, "availableProc", String.valueOf(osBean.getAvailableProcessors()));
        XMLUtils.createElement(contentHandler, "architecture", String.valueOf(osBean.getArch()));
        
        XMLUtils.createElement(contentHandler, "javaVendor", String.valueOf(rBean.getVmVendor()));
        XMLUtils.createElement(contentHandler, "javaVersion", String.valueOf(rBean.getVmVersion()));
        XMLUtils.createElement(contentHandler, "startTime", ParameterHelper.valueToString(new Date(rBean.getStartTime())));
        
        XMLUtils.endElement(contentHandler, "caracteristics");        
    }

    private void _generalStatus() throws SAXException
    {
        XMLUtils.startElement(contentHandler, "general");

        XMLUtils.createElement(contentHandler, "osTime", ParameterHelper.valueToString(new Date()));

        try
        {
            XMLUtils.createElement(contentHandler, "activeSessions", String.valueOf(SessionCountListener.getSessionCount()));
        }
        catch (IllegalStateException e)
        {
            // empty : no value in activeSession means an error
        }
        
        try
        {
            XMLUtils.createElement(contentHandler, "activeRequests", String.valueOf(RequestCountListener.getCurrentRequestCount()));
        }
        catch (IllegalStateException e)
        {
            // empty : no value in activeSession means an error
        }
        
        
        ThreadMXBean tBean = ManagementFactory.getThreadMXBean();
        MemoryMXBean mBean = ManagementFactory.getMemoryMXBean();
        
        XMLUtils.createElement(contentHandler, "activeThreads", String.valueOf(tBean.getThreadCount()));
        long[] lockedThreads = ManagementFactory.getThreadMXBean().findMonitorDeadlockedThreads();
        XMLUtils.createElement(contentHandler, "deadlockThreads", lockedThreads != null ? String.valueOf(lockedThreads.length) : "0");
        
        XMLUtils.startElement(contentHandler, "memory");
        
        XMLUtils.startElement(contentHandler, "heap");
        XMLUtils.createElement(contentHandler, "max", String.valueOf(mBean.getHeapMemoryUsage().getMax()));
        XMLUtils.createElement(contentHandler, "used", String.valueOf(mBean.getHeapMemoryUsage().getUsed()));
        XMLUtils.createElement(contentHandler, "commited", String.valueOf(mBean.getHeapMemoryUsage().getCommitted()));
        XMLUtils.endElement(contentHandler, "heap");

        XMLUtils.startElement(contentHandler, "nonHeap");
        XMLUtils.createElement(contentHandler, "max", String.valueOf(mBean.getNonHeapMemoryUsage().getMax()));
        XMLUtils.createElement(contentHandler, "used", String.valueOf(mBean.getNonHeapMemoryUsage().getUsed()));
        XMLUtils.createElement(contentHandler, "commited", String.valueOf(mBean.getNonHeapMemoryUsage().getCommitted()));
        XMLUtils.endElement(contentHandler, "nonHeap");

        XMLUtils.endElement(contentHandler, "memory");

        XMLUtils.endElement(contentHandler, "general");
    }
    
    private void _properties() throws SAXException
    {
        RuntimeMXBean rBean = ManagementFactory.getRuntimeMXBean();

        XMLUtils.startElement(contentHandler, "properties");

        Map<String, String> properties = rBean.getSystemProperties();
        for (String key : properties.keySet())
        {
            if (key.indexOf(":") == -1)
            {
                XMLUtils.createElement(contentHandler, key, properties.get(key));
            }
        }
        
        XMLUtils.endElement(contentHandler, "properties");
    }
    
    private void _samples() throws SAXException
    {
        XMLUtils.startElement(contentHandler, "samples");

        XMLUtils.startElement(contentHandler, "periods");
        for (Period period : Period.values())
        {
            XMLUtils.createElement(contentHandler, "period", period.toString());
        }
        XMLUtils.endElement(contentHandler, "periods");
        
        for (String extensionId : _monitoringExtensionPoint.getExtensionsIds())
        {
            SampleManager sampleManager = _monitoringExtensionPoint.getExtension(extensionId);

            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("id", sampleManager.getId());
            XMLUtils.startElement(contentHandler, "sample", attrs);
            sampleManager.getLabel().toSAX(contentHandler, "label");
            sampleManager.getDescription().toSAX(contentHandler, "description");
            XMLUtils.endElement(contentHandler, "sample");
        }
        
        XMLUtils.endElement(contentHandler, "samples");
    }
}
