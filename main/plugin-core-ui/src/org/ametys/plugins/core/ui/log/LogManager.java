/*
 *  Copyright 2014 Anyware Services
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

package org.ametys.plugins.core.ui.log;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.Constants;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import org.ametys.core.ui.Callable;
import org.ametys.runtime.log.MemoryAppender;
import org.ametys.runtime.log.MemoryLogRecord;

/**
 * Manager for interacting with the MemoryAppender logs
 */
public class LogManager implements Component, Contextualizable
{
    /** The Avalon role */
    public static final String ROLE = LogManager.class.getName();
    
    private String _contextPath;
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        org.apache.cocoon.environment.Context cocoonContext = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        _contextPath = cocoonContext.getRealPath("/");
        _contextPath = _contextPath.replace('\\', '/');
        if (!_contextPath.endsWith(File.separator))
        {
            _contextPath += File.separator;
        }
        
        _contextPath = "file:" + _contextPath;
    }
    
    /**
     * Get the existing target types
     * @param timestamp Events after this timestamp will be retrieved
     * @param categories Events will be filtered by these categories. If empty, all categories are accepted.
     * @return the target types
     */
    @Callable
    public List<Map<String, Object>> getEvents (Long timestamp, List<String> categories)
    {
        long timeDelimiter;
        if (timestamp == null)
        {
            LocalDateTime time = LocalDateTime.now().minusMinutes(10);
            timeDelimiter = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        else
        {
            // Add one millisecond to take only events that happened after the specified time.
            timeDelimiter = timestamp + 1;
        }
        
        Appender appender = Logger.getRootLogger().getAppender("memory-appender");
        
        if (appender == null || !(appender instanceof MemoryAppender))
        {
            throw new RuntimeException("Unable to get the Memory Appender from the root logger.");
        }

        MemoryAppender memoryAppender = (MemoryAppender) appender;
        
        SortedSet<MemoryLogRecord> recentEvents = memoryAppender.getRecentEvents(timeDelimiter);
        
        List<Map<String, Object>> events = new ArrayList<>();
        
        for (MemoryLogRecord event : recentEvents)
        {
            if (isEventInCategories(event.getCategory(), categories))
            {
                HashMap<String, Object> jsonEvent = new HashMap<>();
                
                jsonEvent.put("timestamp", event.getMillis());
                jsonEvent.put("category", event.getCategory());
                jsonEvent.put("message", hideRealPaths(event.getMessage()));
                jsonEvent.put("location", event.getLocation());
                jsonEvent.put("callstack", hideRealPaths(event.getThrownStackTrace()));
                jsonEvent.put("level", event.getLevel().getLabel());
                jsonEvent.put("thread", event.getThreadDescription());
                jsonEvent.put("user", event.getUser());
                jsonEvent.put("requestURI", event.getRequestURI());
                
                events.add(jsonEvent);
            }
        }
        
        return events;
    }
    
    private String hideRealPaths(String text)
    {
        if (text == null)
        {
            return text;
        }
        
        return text.replace(_contextPath, "context:/");
    }
    
    private boolean isEventInCategories(String event, List<String> categories)
    {
        if (categories.isEmpty() || categories.contains(event))
        {
            return true;
        }
        
        for (String category : categories)
        {
            if (event.startsWith(category.concat(".")))
            {
                return true;
            }
        }
        
        return false;
    }

}
