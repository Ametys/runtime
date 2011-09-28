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
package org.ametys.runtime.plugins.core.administrator.logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.TraversableSource;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.parameter.ParameterHelper;

/**
 * SAXes the list of logs
 */
public class LogsGenerator extends ServiceableGenerator
{
    private static final Pattern __DATED_LOG_FILENAME = Pattern.compile("(.*)[-_]\\d{4}[-_]\\d{2}[-_]\\d{2}.*\\.log");
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "Logger");

        XMLUtils.startElement(contentHandler, "Logs");
        _logs();
        XMLUtils.endElement(contentHandler, "Logs");

        XMLUtils.startElement(contentHandler, "LogLevels");
        _log4jLevels();
        XMLUtils.endElement(contentHandler, "LogLevels");

        XMLUtils.endElement(contentHandler, "Logger");
        contentHandler.endDocument();
    }
    
    private void _log4jLevels() throws SAXException
    {
        try
        {
            LoggerRepository loggerRepository = LogManager.getLoggerRepository();
            List<Logger> loggers = new ArrayList<Logger>();
            Enumeration<org.apache.log4j.Logger> enumLogger = loggerRepository.getCurrentLoggers();
            
            while (enumLogger.hasMoreElements())
            {
                loggers.add(enumLogger.nextElement());
            }
            
            loggers.add(loggerRepository.getRootLogger());
            
            Collections.sort(loggers, new LoggerComparator());
            
            for (Logger logger : loggers)
            {
                String category = logger.getName();
                Level level = logger.getLevel();
                
                AttributesImpl attrs = new AttributesImpl();
                attrs.addCDATAAttribute("category", category);
                attrs.addCDATAAttribute("priority", level == null ? "inherit" : level.toString());
                
                XMLUtils.createElement(contentHandler, "logger", attrs);
            }
        }
        catch (SAXException e)
        {
            throw e;
        }
        catch (Exception e) 
        {
             // rien à faire
            getLogger().warn("Unable to access internal logger properties", e);
        }
    }

    private void _logs() throws IOException, SAXException
    {
        TraversableSource logsDirectorySource = (TraversableSource) resolver.resolveURI("context://WEB-INF/logs");
        
        try
        {
            Map<String, List<TraversableSource>> logs = _prepareLogs(logsDirectorySource);
            
            for (String logname : logs.keySet())
            {
                AttributesImpl logattrs = new AttributesImpl();
                logattrs.addCDATAAttribute("name", logname);
                XMLUtils.startElement(contentHandler, "Log", logattrs);
                
                List<TraversableSource> logSources = logs.get(logname);
                for (TraversableSource logSource : logSources)
                {
                    XMLUtils.startElement(contentHandler, "file");
                    XMLUtils.createElement(contentHandler, "location", logSource.getURI().substring(logSource.getURI().lastIndexOf('/') + 1));
                    XMLUtils.createElement(contentHandler, "lastModified", ParameterHelper.valueToString(new Date(logSource.getLastModified())));
                    XMLUtils.createElement(contentHandler, "size", Long.toString(logSource.getContentLength()));
                    XMLUtils.endElement(contentHandler, "file");
                }
                
                XMLUtils.endElement(contentHandler, "Log");
            }
        }
        finally
        {
            resolver.release(logsDirectorySource);
        }
    }
    
    private Map<String, List<TraversableSource>> _prepareLogs(TraversableSource logsDirectorySource) throws SourceException
    {
        Map<String, List<TraversableSource>> logs = new HashMap<String, List<TraversableSource>>();
        
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
                        underLogs = new ArrayList<TraversableSource>();
                        logs.put(canonicalName, underLogs);
                    }
                    underLogs.add(logSource);
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
    
    /**
     * Comparator to compare two loggers by their name 
     */
    public class LoggerComparator implements Comparator<Logger>
    {
        public int compare(Logger o1, Logger o2)
        {
            String[] o1NameParts = o1.getName().split("\\.");
            String[] o2NameParts = o2.getName().split("\\.");
            int i = 0;
            
            for (; i < o1NameParts.length && i < o2NameParts.length; i++)
            {
                int compare = o1NameParts[i].compareTo(o2NameParts[i]);
                
                if (compare != 0)
                {
                    return compare;
                }
                
                // Same category, continue
            }
            
            if (i == o1NameParts.length)
            {
                if (i != o2NameParts.length)
                {
                    // o2 has a longer category
                    return 1;
                }
            }
            else
            {
                // o1 has a longer category
                return -1;
            }
            
            return o1.getName().compareTo(o2.getName());
        }
    }
}
