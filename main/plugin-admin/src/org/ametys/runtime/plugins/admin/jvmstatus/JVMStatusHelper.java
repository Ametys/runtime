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
package org.ametys.runtime.plugins.admin.jvmstatus;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.ui.Callable;
import org.ametys.core.util.I18nUtils;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.MonitoringConstants.Period;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.MonitoringExtensionPoint;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.SampleManager;

/**
 * This helper allow to get information or runs some operations on JVM system
 */
public class JVMStatusHelper extends AbstractLogEnabled implements Component, Serviceable
{
    /** The monitoring extension point */
    private MonitoringExtensionPoint _monitoringExtensionPoint;
    
    /** Component containing i18n utilitary methods */
    private I18nUtils _i18nUtils;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _monitoringExtensionPoint = (MonitoringExtensionPoint) manager.lookup(MonitoringExtensionPoint.ROLE);
        _i18nUtils = (I18nUtils) manager.lookup(I18nUtils.ROLE);
    }
    
    /**
     * Runs a garbage collector.
     * @return an empty map
     */
    @Callable
    public Map<String, Object> garbageCollect ()
    {
        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Administrator is garbage collecting");
        }
        
        System.gc();
        
        return Collections.EMPTY_MAP;
    }
    
    /**
     * Retrieves information about the general status of the system 
     * @return a map containing the general status information
     */
    @Callable
    public Map<String, Object> getGeneralStatus()
    {
        Map<String, Object> result = new HashMap<>();

        result.put("osTime", ParameterHelper.valueToString(new Date()));
        try
        {
            result.put("activeSessions", SessionCountListener.getSessionCount());
        }
        catch (IllegalStateException e)
        {
            // empty : no value in activeSession means an error
        }
        
        try
        {
            result.put("activeRequests", RequestCountListener.getCurrentRequestCount());
        }
        catch (IllegalStateException e)
        {
            // empty : no value in activeSession means an error
        }
        
        
        ThreadMXBean tBean = ManagementFactory.getThreadMXBean();
        MemoryMXBean mBean = ManagementFactory.getMemoryMXBean();
        RuntimeMXBean rBean = ManagementFactory.getRuntimeMXBean();
        
        result.put("activeThreads", tBean.getThreadCount());
        long[] lockedThreads = ManagementFactory.getThreadMXBean().findMonitorDeadlockedThreads();
        
        result.put("deadlockThreads", lockedThreads != null ? String.valueOf(lockedThreads.length) : "0");
    
        result.put("heap-memory-max", mBean.getHeapMemoryUsage().getMax());
        result.put("heap-memory-used", mBean.getHeapMemoryUsage().getUsed());
        result.put("heap-memory-commited", mBean.getHeapMemoryUsage().getCommitted());
        
        long nonHeapUsed = mBean.getNonHeapMemoryUsage().getUsed();
        long nonHeapCommited = mBean.getNonHeapMemoryUsage().getCommitted();
        long nonHeapMax = Math.max(mBean.getNonHeapMemoryUsage().getMax(), nonHeapCommited);

        result.put("non-heap-memory-max", nonHeapMax);
        result.put("non-heap-memory-used", nonHeapUsed);
        result.put("non-heap-memory-commited", nonHeapCommited);
        
        result.put("startTime", ParameterHelper.valueToString(new Date(rBean.getStartTime())));
        
        return result;
    }
    
    /**
     * Retrieves the monitoring data 
     * @return a map containing the monitoring data
     */
    @Callable
    public Map<String, Object> getMonitoringData()
    {
        Map<String, Object> result = new HashMap<> ();
        
        Map<String, Object> samples = new HashMap<> ();
        List<String> periods = new ArrayList<> ();
        
        for (Period period : Period.values())
        {
            periods.add(period.toString());
        }
        
        samples.put("periods", periods);
        
        List<Map<String, String>> sampleList = new ArrayList<> ();
        for (String extensionId : _monitoringExtensionPoint.getExtensionsIds())
        {
            Map<String, String> sample = new HashMap<> ();
            SampleManager sampleManager = _monitoringExtensionPoint.getExtension(extensionId);

            sample.put("id", sampleManager.getId());
            sample.put("label", _i18nUtils.translate(sampleManager.getLabel()));
            sample.put("description", _i18nUtils.translate(sampleManager.getDescription()));
            
            sampleList.add(sample);
        }
        
        samples.put("sampleList", sampleList);
        result.put("samples", samples);
        return result;
    }
}
