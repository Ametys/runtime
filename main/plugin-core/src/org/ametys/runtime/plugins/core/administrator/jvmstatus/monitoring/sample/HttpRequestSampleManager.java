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

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

import org.ametys.runtime.plugins.core.administrator.jvmstatus.RequestCountListener;
import org.ametys.runtime.plugins.core.administrator.jvmstatus.monitoring.SampleManager;

/**
 * {@link SampleManager} for collecting the throughput and the number
 * of active HTTP requests .
 */
public class HttpRequestSampleManager extends AbstractSampleManager
{
    private long _lastCount;
    
    @Override
    protected void _configureDatasources(RrdDef rrdDef)
    {
        _registerDatasources(rrdDef, "processed", DsType.GAUGE, 0, Double.NaN);
    }

    @Override
    protected void _internalCollect(Sample sample) throws IOException
    {
        try
        {
            sample.setValue("processed", RequestCountListener.getTotalRequestCount() - _lastCount);
            _lastCount = RequestCountListener.getTotalRequestCount();
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
        graphDef.datasource("processed", rrdFilePath, "processed", ConsolFun.AVERAGE);
        graphDef.area("processed", new Color(148, 30, 109), "HTTP request processed");
        
        graphDef.gprint("processed", ConsolFun.LAST, "Cur processed: %.0f");
        graphDef.gprint("processed", ConsolFun.MAX, "Max processed: %.0f");

        // Do not scale units
        graphDef.setUnitsExponent(0);
        graphDef.setVerticalLabel("request processed");
    }
}
