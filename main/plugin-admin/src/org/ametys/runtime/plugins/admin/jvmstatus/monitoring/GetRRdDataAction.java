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
package org.ametys.runtime.plugins.admin.jvmstatus.monitoring;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.io.FileUtils;
import org.rrd4j.core.Archive;
import org.rrd4j.core.RrdDb;

import org.ametys.core.cocoon.JSonReader;
import org.ametys.runtime.servlet.RuntimeConfig;

/**
 * Get the RRD sample data to JSON format
 */
public class GetRRdDataAction extends ServiceableAction implements Initializable, MonitoringConstants
{
    private String _rrdStoragePath;
    
    @Override
    public void initialize() throws Exception
    {
        _rrdStoragePath = FileUtils.getFile(RuntimeConfig.getInstance().getAmetysHome(), RRD_STORAGE_DIRECTORY).getPath();
    }
    
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String sampleId = parameters.getParameter("sampleId");
        
        File rrdFile = new File(_rrdStoragePath, sampleId + RRD_EXT);
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Using RRD file: " + rrdFile);
        }
        
        if (!rrdFile.exists())
        {
            throw new ProcessingException("None sample manager exists for: " + sampleId);
        }
        RrdDb rrdDb = new RrdDb(rrdFile.getPath());
        
        SortedMap<Long, Map<String, Object>> data = new TreeMap<>();
        
        for (Archive archive : _getRelevantArchives(rrdDb))
        {
            long archiveStartTime = archive.getStartTime();
            for (int row = 0; row < archive.getRows(); row++)
            {
                long time = (archiveStartTime + row * archive.getArcStep()) * 1000; // time in ms
                Map<String, Object> values;
                if (data.containsKey(time))
                {
                    values = data.get(time);
                }
                else
                {
                    values = new HashMap<>();
                    data.put(time, values);
                }
                
                String[] dsNames = rrdDb.getDsNames();
                for (int dsIndex = 0; dsIndex < rrdDb.getDsCount(); dsIndex++)
                {
                    String dsName = archive.getConsolFun().toString() + "_" + dsNames[dsIndex];
                    double value = archive.getRobin(dsIndex).getValue(row);
                    if (!values.containsKey(dsName))
                    {
                        if (Double.isNaN(value))
                        {
                            value = 0;
                        }
                        values.put(dsName, value);
                    }
                }
            }
        }
        
        List<Map<String, Object>> result = _convertData(data);
        
        request.setAttribute(JSonReader.OBJECT_TO_READ, result);
        return EMPTY_MAP;
    }
    
    private List<Archive> _getRelevantArchives(RrdDb rrdDb)
    {
        // Return all the archives, but in case of bad performance, it could
        // be interesting to return jsut some of them
        List<Archive> result = new ArrayList<>();
        for (int i = 0; i < rrdDb.getArcCount(); i++)
        {
            Archive archive = rrdDb.getArchive(i);
            result.add(archive);
        }
        
        return result;
    }
    
    private List<Map<String, Object>> _convertData(SortedMap<Long, Map<String, Object>> data)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Long time : data.keySet())
        {
            Map<String, Object> row = data.get(time);
            row.put("time", time);
            result.add(row);
        }
        
        return result;
    }
}
