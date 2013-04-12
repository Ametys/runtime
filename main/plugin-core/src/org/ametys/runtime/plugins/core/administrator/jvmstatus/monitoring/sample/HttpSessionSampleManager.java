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

import org.ametys.runtime.plugins.core.administrator.jvmstatus.SessionCountListener;
import org.ametys.runtime.plugins.core.administrator.jvmstatus.monitoring.SampleManager;

/**
 * {@link SampleManager} for collecting the number of active HTTP sessions.
 */
public class HttpSessionSampleManager extends AbstractSampleManager
{
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
            sample.setValue("count", Math.max(0, SessionCountListener.getSessionCount()));
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
        graphDef.area("count", new Color(148, 30, 109), "Active HTTP session count");

        graphDef.gprint("count", ConsolFun.LAST, "Cur: %.0f");
        graphDef.gprint("count", ConsolFun.MAX, "Max: %.0f");

        // Do not scale units
        graphDef.setUnitsExponent(0);
        graphDef.setVerticalLabel("session count");
    }
}
