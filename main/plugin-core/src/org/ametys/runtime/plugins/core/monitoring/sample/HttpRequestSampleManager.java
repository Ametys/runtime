package org.ametys.runtime.plugins.core.monitoring.sample;

import java.awt.Color;
import java.io.IOException;

import org.ametys.runtime.plugins.core.administrator.jvmstatus.RequestCountListener;
import org.ametys.runtime.plugins.core.monitoring.SampleManager;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

/**
 * {@link SampleManager} for collecting the throughput and the number
 * of active HTTP requests .
 */
public class HttpRequestSampleManager extends AbstractSampleManager
{
    public String getName()
    {
        return "http-request";
    }
    
    @Override
    protected void _configureDatasources(RrdDef rrdDef)
    {
        _registerDatasources(rrdDef, "current", DsType.GAUGE, 0, Double.NaN);
        _registerDatasources(rrdDef, "total", DsType.COUNTER, 0, Double.NaN);
    }

    @Override
    protected void _internalCollect(Sample sample) throws IOException
    {
        try
        {
            sample.setValue("current", RequestCountListener.getCurrentRequestCount());
            sample.setValue("total", RequestCountListener.getTotalRequestCount());
        }
        catch (IllegalStateException e)
        {
            // empty : no value means an error
        }
    }

    @Override
    protected String _getGraphTitle()
    {
        return "HTTP request";
    }
    
    @Override
    protected void _populateGraphDefinition(RrdGraphDef graphDef, String rrdFilePath)
    {
        graphDef.datasource("current", rrdFilePath, "current", ConsolFun.AVERAGE);
        graphDef.datasource("total", rrdFilePath, "total", ConsolFun.AVERAGE);
        graphDef.line("current", Color.BLUE, "Active HTTP request count", 2);
        graphDef.line("total", Color.GREEN, "HTTP request throughput", 2);
        
        graphDef.gprint("current", ConsolFun.LAST, "Cur current: %.0f");
        graphDef.gprint("current", ConsolFun.MAX, "Max current: %.0f");
        graphDef.gprint("total", ConsolFun.LAST, "Cur rate: %.0f req/s");
        graphDef.gprint("total", ConsolFun.MAX, "Max rate: %.0f req/s");

        // Do not scale units
        graphDef.setUnitsExponent(0);
        graphDef.setVerticalLabel("request count ; request per second");
    }
}
