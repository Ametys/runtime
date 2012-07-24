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
package org.ametys.runtime.plugins.core.administrator.jvmstatus.monitoring.sample;

import java.awt.Color;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphConstants;
import org.rrd4j.graph.RrdGraphDef;

import org.ametys.runtime.plugins.core.administrator.jvmstatus.monitoring.SampleManager;

/**
 * {@link SampleManager} for collecting the uptime of the JVM.
 */
public class UptimeSampleManager extends AbstractSampleManager
{
    @Override
    protected void _configureDatasources(RrdDef rrdDef)
    {
        _registerDatasources(rrdDef, "uptime", DsType.GAUGE, 0, Double.NaN);
    }

    @Override
    protected void _internalCollect(Sample sample) throws IOException
    {
        sample.setValue("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
    }

    @Override
    protected String _getGraphTitle()
    {
        return "Uptime";
    }
    
    @Override
    protected void _populateGraphDefinition(RrdGraphDef graphDef, String rrdFilePath)
    {
        graphDef.datasource("uptime", rrdFilePath, "uptime", ConsolFun.AVERAGE);
        // Divide uptime by 24*60*60*1000
        graphDef.datasource("uptime_in_days", "uptime,1000,60,60,24,*,*,*,/");
        graphDef.line("uptime_in_days", new Color(148, 30, 109), "Uptime", 2);
        
        graphDef.gprint("uptime_in_days", ConsolFun.LAST, "Cur: %.0f day(s)");
        graphDef.gprint("uptime_in_days", ConsolFun.MAX, "Max: %.0f day(s)");
        
        // Do not scale units
        graphDef.setUnitsExponent(0);
        graphDef.setVerticalLabel("days");
        graphDef.setColor(RrdGraphConstants.COLOR_BACK, new Color(255, 255, 255));
        graphDef.setColor(RrdGraphConstants.COLOR_CANVAS, new Color(255, 255, 255));
        graphDef.setColor(RrdGraphConstants.COLOR_FRAME, new Color(255, 255, 255));
        graphDef.setColor(RrdGraphConstants.COLOR_MGRID, new Color(128, 128, 128));
        graphDef.setColor(RrdGraphConstants.COLOR_GRID, new Color(220, 220, 220));
        graphDef.setColor(RrdGraphConstants.COLOR_SHADEA, new Color(220, 220, 220));
        graphDef.setColor(RrdGraphConstants.COLOR_SHADEB, new Color(220, 220, 220));
    }
}
