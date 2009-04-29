package org.ametys.runtime.plugins.core.monitoring.sample;

import java.awt.Color;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.ametys.runtime.plugins.core.monitoring.SampleManager;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

/**
 * {@link SampleManager} for collecting the uptime of the JVM.
 */
public class UptimeSampleManager extends AbstractSampleManager
{
    public String getName()
    {
        return "uptime";
    }
    
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
        graphDef.area("uptime_in_days", Color.GREEN, "Uptime");
        
        graphDef.gprint("uptime_in_days", ConsolFun.LAST, "Cur: %.0f day(s)");
        graphDef.gprint("uptime_in_days", ConsolFun.MAX, "Max: %.0f day(s)");
        
        // Do not scale units
        graphDef.setUnitsExponent(0);
        graphDef.setVerticalLabel("days");
    }
}
