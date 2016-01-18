/*
 *  Copyright 2015 Anyware Services
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
package org.ametys.runtime.plugins.admin.logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.TraversableSource;
import org.xml.sax.SAXException;

import org.ametys.runtime.parameter.ParameterHelper;

/**
 * SAXes the list of logs
 */
public class LogsGenerator extends ServiceableGenerator
{
    private static final Pattern __DATED_LOG_FILENAME = Pattern.compile("(.*)[-_]\\d{4}[-_]\\d{2}[-_]\\d{2}.*\\.log");
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "logs");
        _logs();
        XMLUtils.endElement(contentHandler, "logs");
        contentHandler.endDocument();
    }
    
    private void _logs() throws IOException, SAXException
    {
        TraversableSource logsDirectorySource = (TraversableSource) resolver.resolveURI("ametys-home://logs");
        
        try
        {
            Map<String, List<TraversableSource>> logs = _prepareLogs(logsDirectorySource);
            
            for (String logname : logs.keySet())
            {
                List<TraversableSource> logSources = logs.get(logname);
                for (TraversableSource logSource : logSources)
                {
                    XMLUtils.startElement(contentHandler, "log");
                    XMLUtils.createElement(contentHandler, "name", logname);
                    XMLUtils.createElement(contentHandler, "location", logSource.getURI().substring(logSource.getURI().lastIndexOf('/') + 1));
                    XMLUtils.createElement(contentHandler, "lastModified", ParameterHelper.valueToString(new Date(logSource.getLastModified())));
                    XMLUtils.createElement(contentHandler, "size", Long.toString(logSource.getContentLength()));
                    XMLUtils.endElement(contentHandler, "log");
                }
                
            }
        }
        finally
        {
            resolver.release(logsDirectorySource);
        }
    }
    
    private Map<String, List<TraversableSource>> _prepareLogs(TraversableSource logsDirectorySource) throws SourceException
    {
        Map<String, List<TraversableSource>> logs = new HashMap<>();
        
        if (logsDirectorySource.exists())
        {
            Collection<TraversableSource> logFiles = logsDirectorySource.getChildren();
            for (TraversableSource logSource : logFiles)
            {
                if (!logSource.isCollection())
                {
                    String name = logSource.getName();
                    String canonicalName = _getCanonicalName(name);
                    
                    if (canonicalName != null)
                    {
                        List<TraversableSource> underLogs = logs.get(canonicalName);
                        if (underLogs == null)
                        {
                            underLogs = new ArrayList<>();
                            logs.put(canonicalName, underLogs);
                        }
                        
                        underLogs.add(logSource);
                    }
                }
            }
        }
        
        return logs;
    }

    private String _getCanonicalName(String name)
    {
        Matcher matcher = __DATED_LOG_FILENAME.matcher(name);
        if (matcher.matches())
        {
            return matcher.group(1);
        }
        else if (name.endsWith(".log"))
        {
            return name;
        }
        else
        {
            return null;
        }
    }
}
