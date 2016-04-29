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

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.io.FileUtils;
import org.rrd4j.core.Archive;
import org.rrd4j.core.RrdDb;

import org.ametys.core.ui.Callable;
import org.ametys.core.util.I18nUtils;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.MonitoringConstants;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.MonitoringExtensionPoint;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.SampleManager;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.alerts.AlertSampleManager;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.alerts.AlertSampleManager.Threshold;
import org.ametys.runtime.servlet.RuntimeConfig;

/**
 * This helper allow to get information or runs some operations on JVM system
 */
public class JVMStatusHelper extends AbstractLogEnabled implements Component, Serviceable, Initializable, MonitoringConstants
{
    /** The monitoring extension point */
    private MonitoringExtensionPoint _monitoringExtensionPoint;
    
    /** Component containing i18n utilitary methods */
    private I18nUtils _i18nUtils;

    private String _rrdStoragePath;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _monitoringExtensionPoint = (MonitoringExtensionPoint) manager.lookup(MonitoringExtensionPoint.ROLE);
        _i18nUtils = (I18nUtils) manager.lookup(I18nUtils.ROLE);
    }
    
    public void initialize() throws Exception
    {
        _rrdStoragePath = FileUtils.getFile(RuntimeConfig.getInstance().getAmetysHome(), RRD_STORAGE_DIRECTORY).getPath();
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
        
        List<Map<String, Object>> sampleList = new ArrayList<> ();
        for (String extensionId : _monitoringExtensionPoint.getExtensionsIds())
        {
            Map<String, Object> sample = new HashMap<> ();
            SampleManager sampleManager = _monitoringExtensionPoint.getExtension(extensionId);

            sample.put("id", sampleManager.getId());
            sample.put("label", _i18nUtils.translate(sampleManager.getLabel()));
            sample.put("description", _i18nUtils.translate(sampleManager.getDescription()));
            if (sampleManager instanceof AlertSampleManager)
            {
                Map<String, Object> thresholdValues = new HashMap<>(); 
                
                Map<String, Threshold> thresholds = ((AlertSampleManager) sampleManager).getThresholdValues();
                for (String datasourceName : thresholds.keySet())
                {
                    thresholdValues.put(datasourceName, thresholds.get(datasourceName).getValue());
                }
                
                sample.put("thresholds", thresholdValues);
            }

            File rrdFile = new File(_rrdStoragePath, sampleManager.getId() + RRD_EXT);
            if (getLogger().isDebugEnabled())
            {
                getLogger().debug("Using RRD file: " + rrdFile);
            }
            
            RrdDb rrdDb = null;
            try
            {
                rrdDb = new RrdDb(rrdFile.getPath());
                
                sample.put("ds", rrdDb.getDsNames());
                
                Set<String> consolidationFunction = new HashSet<>();
                for (int i = 0; i < rrdDb.getArcCount(); i++)
                {
                    Archive archive = rrdDb.getArchive(i);
                    consolidationFunction.add(archive.getConsolFun().toString());
                }
                sample.put("consolFun", consolidationFunction);
            }
            catch (Exception e)
            {
                getLogger().error("Unable to collect sample for: " + sampleManager.getId(), e);
            }
            finally
            {
                if (rrdDb != null)
                {
                    try
                    {
                        rrdDb.close();
                    }
                    catch (IOException e)
                    {
                        getLogger().warn("Unable to close RRD file: " + rrdFile, e);
                    }
                }
            }
            
            sampleList.add(sample);
        }
        
        samples.put("sampleList", sampleList);
        result.put("samples", samples);
        return result;
    }
}
