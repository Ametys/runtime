/*
 *  Copyright 2016 Anyware Services
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.graph.RrdGraphDef;

import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.DiskSpaceHelper;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.SampleManager;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.alerts.AbstractAlertSampleManager;
import org.ametys.runtime.plugins.admin.jvmstatus.monitoring.alerts.AlertSampleManager.Threshold.Operator;

/**
 * {@link SampleManager} for collecting the use of disk space (in MB).
 */
public class DiskSpaceSampleManager extends AbstractAlertSampleManager implements Serviceable
{
    private DiskSpaceHelper _diskSpaceHelper;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _diskSpaceHelper = (DiskSpaceHelper) manager.lookup(DiskSpaceHelper.ROLE);
    }
    
    @Override
    protected void _configureDatasources(RrdDef rrdDef)
    {
        _registerDatasources(rrdDef, "used", DsType.ABSOLUTE, 0, Double.NaN);
    }

    @Override
    protected Map<String, Object> _internalCollect(Sample sample) throws IOException
    {
        Map<String, Object> result = new HashMap<>();
        long size = _diskSpaceHelper.getUsedSpace() / 1024 / 1024; //B to MB
        
        sample.setValue("used", size);
        result.put("used", size);
        
        return result;
    }

    @Override
    protected String _getGraphTitle()
    {
        return "Used space on disk";
    }

    @Override
    protected void _populateGraphDefinition(RrdGraphDef graphDef, String rrdFilePath)
    {
        graphDef.datasource("used", rrdFilePath, "used", ConsolFun.AVERAGE);
        
        graphDef.area("used", new Color(148, 30, 109), "Used space on disk");
        
        graphDef.setVerticalLabel("bytes");
    }
    
    @Override
    protected Map<String, String> getThresholdConfigNames()
    {
        return Collections.singletonMap("used", "runtime.system.alerts.diskspace.threshold");
    }
    
    @Override
    protected Map<String, Operator> getOperators()
    {
        return Collections.singletonMap("used", Operator.GEQ);
    }
}
