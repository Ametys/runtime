/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.plugins.core.administrator.logs;

import java.io.IOException;
import java.lang.reflect.Field;
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
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.TraversableSource;
import org.apache.log.Logger;
import org.apache.log.Priority;
import org.xml.sax.SAXException;

import org.ametys.runtime.util.LoggerFactory;
import org.ametys.runtime.util.parameter.ParameterHelper;


/**
 * SAXes the list of logs
 */
public class LogsGenerator extends ServiceableGenerator
{
    private static final Pattern __DATED_LOG_FILENAME = Pattern.compile("(.*)((-|_)[0-9]{4}(-|_)[0-9]{2}(-|_)[0-9]{2})(.*).log");
    
    public void generate() throws IOException, SAXException, ProcessingException
    {
        
        contentHandler.startDocument();
        XMLUtils.startElement(contentHandler, "Logger");

        XMLUtils.startElement(contentHandler, "Logs");
        _logs();
        XMLUtils.endElement(contentHandler, "Logs");

        AttributesImpl attrs = new AttributesImpl();
        attrs.addCDATAAttribute("type", "logkit");
        XMLUtils.startElement(contentHandler, "LogLevels", attrs);
        _logkitLevels(LoggerFactory.getHierarchy().getRootLogger(), 0);
        XMLUtils.endElement(contentHandler, "LogLevels");

        XMLUtils.endElement(contentHandler, "Logger");
        contentHandler.endDocument();
    }
    
    private static Object _getField(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException
    {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
    
    private void _logkitLevels(Logger logger, int depth) throws SAXException
    {
        try 
        {
            boolean inherited = !((Boolean) _getField(logger, "m_priorityForceSet")).booleanValue();
            String priority = ((Priority) _getField(logger, "m_priority")).getName();
            String fullCategory = (String) _getField(logger, "m_category");
            
            if (depth == 0 || !"".equals(fullCategory))
            {
                int last = fullCategory.lastIndexOf(Logger.CATEGORY_SEPARATOR);
                String category = (last == -1) ? fullCategory : fullCategory.substring(last + 1);
                      
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute("", "name", "name", "CDATA", category);
                attrs.addAttribute("", "category", "category", "CDATA", fullCategory);
                attrs.addAttribute("", "inherited", "inherited", "CDATA", String.valueOf(inherited));
                attrs.addAttribute("", "priority", "priority", "CDATA", priority);
                
                XMLUtils.startElement(contentHandler, "logger", attrs);
                
                for (Logger log : logger.getChildren())
                {
                    _logkitLevels(log, depth + 1);
                }
                 
                XMLUtils.endElement(contentHandler, "logger");
            }
        }
        catch (SAXException e)
        {
            throw e;
        }
        catch (Exception e) 
        {
             // rien � faire
            getLogger().warn("Unable to access internal logger properties", e);
        }
    }

    private void _logs() throws IOException, SAXException
    {
        TraversableSource logsDirectorySource = (TraversableSource) resolver.resolveURI("context://WEB-INF/logs");
        Map<String, List<TraversableSource>> logs = _prepareLogs(logsDirectorySource);
        
        for (String logname : logs.keySet())
        {
            AttributesImpl logattrs = new AttributesImpl();
            logattrs.addAttribute("", "name", "name", "CDATA", logname);
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
}
