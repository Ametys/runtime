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
package org.ametys.runtime.plugins.core.monitoring.sample;

import java.awt.Color;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import org.ametys.runtime.plugins.core.monitoring.SampleManager;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

/**
 * Abstract {@link SampleManager} for collecting JVM memory status.
 */
public abstract class AbstractMemorySampleManager extends AbstractSampleManager
{
    @Override
    protected void _configureDatasources(RrdDef rrdDef)
    {
        rrdDef.addDatasource("commited", DsType.GAUGE, 2 * FEEDING_PERIOD, 0, Double.NaN);
        rrdDef.addDatasource("used", DsType.GAUGE, 2 * FEEDING_PERIOD, 0, Double.NaN);
        rrdDef.addDatasource("max", DsType.GAUGE, 2 * FEEDING_PERIOD, 0, Double.NaN);
    }

    @Override
    protected void _internalCollect(Sample sample) throws IOException
    {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage memoryUsage = _getMemoryUsage(memoryMXBean);
        
        sample.setValue("commited", memoryUsage.getCommitted());
        sample.setValue("used", memoryUsage.getUsed());
        sample.setValue("max", memoryUsage.getMax());
    }

    /**
     * Select the memory usage to use.
     * @param memoryMXBean the memory MXBean.
     * @return the memory usage.
     */
    protected abstract MemoryUsage _getMemoryUsage(MemoryMXBean memoryMXBean);
    
    @Override
    protected void _populateGraphDefinition(RrdGraphDef graphDef, String rrdFilePath)
    {
        graphDef.datasource("commited", rrdFilePath, "commited", ConsolFun.AVERAGE);
        graphDef.datasource("used", rrdFilePath, "used", ConsolFun.AVERAGE);
        graphDef.datasource("max", rrdFilePath, "max", ConsolFun.AVERAGE);

        graphDef.area("max", Color.BLUE, "Max");
        graphDef.area("commited", Color.GREEN, "Commited");
        graphDef.area("used", Color.RED, "Used");

        graphDef.setVerticalLabel("bytes");

        graphDef.gprint("used", ConsolFun.LAST, "Cur used: %4.0f %s");
        graphDef.gprint("used", ConsolFun.MAX, "Max used: %4.0f %S");
        graphDef.gprint("commited", ConsolFun.LAST, "Cur commited: %4.0f %S");
        graphDef.gprint("commited", ConsolFun.MAX, "Max commited: %4.0f %S");
    }
}
