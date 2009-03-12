package org.ametys.runtime.plugins.core.monitoring.sample;

import java.awt.Color;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.ametys.runtime.plugins.core.monitoring.SampleManager;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

/**
 * {@link SampleManager} for collecting the number of live threads.
 */
public class ThreadSampleManager extends AbstractSampleManager
{
    public String getName()
    {
        return "thread";
    }
    
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
        

        graphDef.line("user", Color.RED, "User thread count", 2);
        graphDef.line("daemon", Color.BLUE, "Daemon thread count", 2);
        graphDef.line("total", Color.GREEN, "Total thread count", 2);

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
