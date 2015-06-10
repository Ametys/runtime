/*
 *  Copyright 2013 Anyware Services
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
package org.ametys.runtime.plugins.admin.jvmstatus.monitoring.sample;

import java.awt.Color;
import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

/**
 * Sample manager collecting disk I/O operations.
 */
public class DiskIOSampleManager extends AbstractSampleManager implements Initializable
{
    
    private DiskIOMonitor _ioMonitor;
    private Map<String, Double> _previousMetrics;
    
    @Override
    public void initialize() throws Exception
    {
        _logger.debug("Initializing DiskIOSampleManager");
        
        try
        {
            _ioMonitor = new SigarDiskIOMonitor();
            _logger.info("Loaded Sigar native library");
        }
        catch (Exception e)
        {
            _logger.warn("Cannot load Sigar native library");
        }
    }
    
    @Override
    protected String _getGraphTitle()
    {
        return "Filesystem disk I/O";
    }

    @Override
    protected void _configureDatasources(RrdDef rrdDef)
    {
        _registerDatasources(rrdDef, "reads", DsType.GAUGE, 0, Double.NaN);
        _registerDatasources(rrdDef, "writes", DsType.GAUGE, 0, Double.NaN);
    }

    @Override
    protected void _internalCollect(Sample sample) throws IOException
    {
        double reads = Double.NaN;
        double writes = Double.NaN;

        if (_ioMonitor != null)
        {
            _ioMonitor.refresh();
            Map<String, Double> metrics = _ioMonitor.toMap();

            if (_previousMetrics != null)
            {
                reads = Math.max(0, metrics.get(DiskIOMonitor.READS) - _previousMetrics.get(DiskIOMonitor.READS));
                writes = Math.max(0, metrics.get(DiskIOMonitor.WRITES) - _previousMetrics.get(DiskIOMonitor.WRITES));
                
                sample.setValue("reads", reads);
                sample.setValue("writes", writes);
            }
            
            _previousMetrics = metrics;
        }
        
        if (_logger.isDebugEnabled())
        {
            _logger.debug("reads=" + reads + ", writes=" + writes);
        }
    }

    @Override
    protected void _populateGraphDefinition(RrdGraphDef graphDef, String rrdFilePath)
    {
        graphDef.datasource("reads", rrdFilePath, "reads", ConsolFun.AVERAGE);
        graphDef.datasource("writes", rrdFilePath, "writes", ConsolFun.AVERAGE);

        graphDef.line("reads", Color.BLUE, "Reads", 2);
        graphDef.line("writes", Color.RED, "Writes", 2);

        graphDef.gprint("reads", ConsolFun.LAST, "Cur reads: %.0f");
        graphDef.gprint("reads", ConsolFun.MAX, "Max reads: %.0f");

        graphDef.gprint("writes", ConsolFun.LAST, "Cur writes: %.0f");
        graphDef.gprint("writes", ConsolFun.MAX, "Max writes: %.0f");

        graphDef.setUnitsExponent(0);
        graphDef.setVerticalLabel("I/O operations");
    }
}
