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
import java.lang.management.ThreadMXBean;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

import org.ametys.runtime.plugins.core.administrator.jvmstatus.monitoring.SampleManager;

/**
 * {@link SampleManager} for collecting the number of live threads.
 */
public class ThreadSampleManager extends AbstractSampleManager
{
    @Override
    protected void _configureDatasources(RrdDef rrdDef)
    {
        _registerDatasources(rrdDef, "daemon", DsType.GAUGE, 0, Double.NaN);
        _registerDatasources(rrdDef, "total", DsType.GAUGE, 0, Double.NaN);
    }

    @Override
    protected void _internalCollect(Sample sample) throws IOException
    {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        sample.setValue("daemon", threadMXBean.getDaemonThreadCount());
        sample.setValue("total", threadMXBean.getThreadCount());
    }

    @Override
    protected String _getGraphTitle()
    {
        return "Live thread";
    }
    
    @Override
    protected void _populateGraphDefinition(RrdGraphDef graphDef, String rrdFilePath)
    {
        graphDef.datasource("daemon", rrdFilePath, "daemon", ConsolFun.AVERAGE);
        graphDef.datasource("total", rrdFilePath, "total", ConsolFun.AVERAGE);
        // User thread are not daemon thread ;)
        graphDef.datasource("user", "total,daemon,-");
        

        graphDef.area("total", new Color(229, 229, 229), "Total thread count");
        graphDef.line("daemon", new Color(28, 76, 128), "Daemon thread count", 2);
        graphDef.line("user", new Color(148, 30, 109), "User thread count", 2);

        graphDef.gprint("user", ConsolFun.LAST, "Cur user: %.0f");
        graphDef.gprint("user", ConsolFun.MAX, "Max user: %.0f");
        graphDef.gprint("daemon", ConsolFun.LAST, "Cur daemon: %.0f");
        graphDef.gprint("daemon", ConsolFun.MAX, "Max daemon: %.0f");
        graphDef.gprint("total", ConsolFun.LAST, "Cur total: %.0f");
        graphDef.gprint("total", ConsolFun.MAX, "Max total: %.0f");

        // Do not scale units
        graphDef.setUnitsExponent(0);
        graphDef.setVerticalLabel("thread count");
    }
}
