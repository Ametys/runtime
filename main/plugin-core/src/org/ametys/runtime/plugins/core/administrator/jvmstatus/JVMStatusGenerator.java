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
package org.ametys.runtime.plugins.core.administrator.jvmstatus;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.ametys.runtime.util.parameter.ParameterHelper;


/**
 * SAXes general information about the application : JVM status, sessions count, system properties, ...
 */
public class JVMStatusGenerator extends ServiceableGenerator
{
    public void generate() throws IOException, SAXException, ProcessingException
    {
        
        contentHandler.startDocument();
        contentHandler.startElement("", "status", "status", new AttributesImpl());
        
        _generalStatus();
        if (parameters.getParameterAsBoolean("properties", true))
        {
            _caracteristics();
            _properties();
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
            XMLUtils.createElement(contentHandler, "activeRequests", String.valueOf(RequestCountListener.getSessionCount()));
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
            XMLUtils.createElement(contentHandler, key, properties.get(key));
        }
        
        XMLUtils.endElement(contentHandler, "properties");
    }
}
