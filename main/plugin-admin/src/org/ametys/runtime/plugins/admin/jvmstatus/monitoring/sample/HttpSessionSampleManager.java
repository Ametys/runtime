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
package org.ametys.runtime.plugins.admin.jvmstatus.monitoring.sample;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

import org.ametys.runtime.plugins.admin.jvmstatus.SessionCountListener;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.SampleManager;

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
    protected Map<String, Object> _internalCollect(Sample sample) throws IOException
    {
        Map<String, Object> result = new HashMap<>();
        try
        {
            int count = Math.max(0, SessionCountListener.getSessionCount());
            sample.setValue("count", count);
            result.put("count", count);
        }
        catch (IllegalStateException e)
        {
            // empty : no value means an error
        }
        return result;
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
