package org.ametys.runtime.plugins.core.monitoring.sample;

import java.awt.Color;
import java.io.IOException;

import org.ametys.runtime.plugins.core.administrator.jvmstatus.SessionCountListener;
import org.ametys.runtime.plugins.core.monitoring.SampleManager;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

/**
 * {@link SampleManager} for collecting the number of active HTTP sessions.
 */
public class HttpSessionSampleManager extends AbstractSampleManager
{
    public String getName()
    {
        return "http-session";
    }
    
    @Override
    protected void _configureDatasources(RrdDef rrdDef)
    {
        _registerDatasources(rrdDef, "count", DsType.GAUGE, 0, Double.NaN);
    }

    @Override
    protected void _internalCollect(Sample sample) throws IOException
    {
        try
        {
            sample.setValue("count", SessionCountListener.getSessionCount());
        }
        catch (IllegalStateException e)
        {
            // empty : no value means an error
        }
    }

    @Override
    protected String _getGraphTitle()
    {
        return "Active HTTP session";
    }
    
    @Override
    protected void _populateGraphDefinition(RrdGraphDef graphDef, String rrdFilePath)
    {
        graphDef.datasource("count", rrdFilePath, "count", ConsolFun.AVERAGE);
        graphDef.line("count", Color.GREEN, "Active HTTP session count", 2);

        graphDef.gprint("count", ConsolFun.LAST, "Cur: %.0f");
        graphDef.gprint("count", ConsolFun.MAX, "Max: %.0f");

        // Do not scale units
        graphDef.setUnitsExponent(0);
        graphDef.setVerticalLabel("session count");
    }
}
