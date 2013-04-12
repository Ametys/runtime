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
package org.ametys.runtime.plugins.core.administrator.jvmstatus.monitoring.sample;

import java.awt.Color;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

import org.ametys.runtime.plugins.core.administrator.jvmstatus.monitoring.SampleManager;

import com.sun.management.OperatingSystemMXBean;

/**
 * {@link SampleManager} for collecting JVM heap memory status.
 */
public class CPUSampleManager extends AbstractSampleManager
{
    private static MXBeanCPUMonitor _mxBeanCPUMonitor;
    
    @Override
    protected void _configureDatasources(RrdDef rrdDef)
    {
        _registerDatasources(rrdDef, "current", DsType.GAUGE, 0, Double.NaN);
    }

    @Override
    protected void _internalCollect(Sample sample) throws IOException
    {
        if (_mxBeanCPUMonitor == null)
        {
            _mxBeanCPUMonitor = new MXBeanCPUMonitor();
        }
        
        sample.setValue("current", _mxBeanCPUMonitor.getCpuUsage());
    }

    @Override
    protected String _getGraphTitle()
    {
        return "CPU use";
    }
    
    @Override
    protected void _populateGraphDefinition(RrdGraphDef graphDef, String rrdFilePath)
    {
        graphDef.datasource("current", rrdFilePath, "current", ConsolFun.AVERAGE);
        graphDef.area("current", new Color(148, 30, 109), "HTTP request processed");
        
        graphDef.gprint("current", ConsolFun.LAST, "Cur current: %.0f");
        graphDef.gprint("current", ConsolFun.MAX, "Max current: %.0f");

        // Do not scale units
        graphDef.setUnitsExponent(0);
        graphDef.setVerticalLabel("CPU use %");
    }
    
    /**
     * MXBean to monitor CPU 
     */
    public class MXBeanCPUMonitor
    {
        private int _availableProcessors = getOperatingSystemMXBean().getAvailableProcessors();
        private long _lastProcessCpuTime;
        private long _lastSystemTime;

        MXBeanCPUMonitor()
        {
            // empty
        }

        private void baselineCounters()
        {
            _lastSystemTime = System.nanoTime();

            if (getOperatingSystemMXBean() != null)
            {
                _lastProcessCpuTime = (getOperatingSystemMXBean()).getProcessCpuTime();
            }
        }

        private OperatingSystemMXBean getOperatingSystemMXBean()
        {
            Object bean = ManagementFactory.getOperatingSystemMXBean();
            if (bean instanceof OperatingSystemMXBean)
            {
                return (OperatingSystemMXBean) bean;
            }
            else
            {
                return null;
            }
        }

        /**
         * The cpu usage
         * @return The cpu usage
         */
        public synchronized double getCpuUsage()
        {
            if (_lastSystemTime == 0)
            {
                baselineCounters();
                return 0;
            }

            long systemTime = System.nanoTime();
            long processCpuTime = 0;

            if (getOperatingSystemMXBean() != null)
            {
                processCpuTime = (getOperatingSystemMXBean()).getProcessCpuTime();
            }
            else
            {
                return 0;
            }

            double cpuUsage = (double) (processCpuTime - _lastProcessCpuTime) / (systemTime - _lastSystemTime);

            _lastSystemTime = systemTime;
            _lastProcessCpuTime = processCpuTime;

            return (cpuUsage / _availableProcessors) * 100;
        }
    }
}
