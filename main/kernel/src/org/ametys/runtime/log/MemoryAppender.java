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
package org.ametys.runtime.log;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.LogLevelFormatException;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * Log appender that stores a pile of logs in memory.
 */
public class MemoryAppender extends org.apache.log4j.AppenderSkeleton
{
    private ExpiringSortedSetQueue<MemoryLogRecord> _logsPile;

    /**
     * Default constructor for the memory appender
     */
    public MemoryAppender()
    {
        Comparator<MemoryLogRecord> comparator = new Comparator<MemoryLogRecord> ()
        {
            public int compare(MemoryLogRecord record, MemoryLogRecord compareTo)
            {
                long recordTime = record.getMillis();
                long compareToTime = compareTo.getMillis();
                if (recordTime == compareToTime)
                {
                    return record.getSequenceNumber() > compareTo.getSequenceNumber() ? -1 : record.getSequenceNumber() < compareTo.getSequenceNumber() ? 1 : 0;
                }
                return recordTime > compareToTime ? 1 : -1;
            }
        };

        _logsPile = new ExpiringSortedSetQueue<>(5 * 60 * 1000, comparator);
    }

    @Override
    protected void append(LoggingEvent event)
    {
        // Retrieve the information from the log4j LoggingEvent.
        String category = event.getLoggerName();
        String logMessage = event.getRenderedMessage();
        String level = event.getLevel().toString();
        long time = event.timeStamp;
        LocationInfo locationInfo = event.getLocationInformation();
        String user = (String) event.getMDC("user");
        String requestURI = (String) event.getMDC("requestURI");

        // Add the logging event information to a LogRecord
        MemoryLogRecord record = new MemoryLogRecord();

        record.setCategory(category);
        record.setMessage(logMessage);
        record.setLocation(locationInfo.fullInfo);
        record.setMillis(time);
        record.setUser(user);
        record.setRequestURI(requestURI);

        ThrowableInformation throwableInformation = event.getThrowableInformation();
        if (throwableInformation != null)
        {
            record.setThrownStackTrace(throwableInformation);
        }

        try 
        {
            record.setLevel(LogLevel.valueOf(level));
        } 
        catch (LogLevelFormatException e)
        {
            // If the priority level doesn't match one of the predefined
            // log levels, then set the level to warning.
            record.setLevel(LogLevel.WARN);
        }
        
        _logsPile.put(record);
    }
    
    /**
     * Retrieve a list of events that are more recent than the timestamp parameter
     * @param timestamp The time delimiter.
     * @return a list of events.
     */
    public SortedSet<MemoryLogRecord> getRecentEvents(long timestamp)
    {
        // New events can occur at current time, retrieve only the fixed logs list: 1ms before now.
        long now = System.currentTimeMillis() - 1;
        if (now < timestamp)
        {
            return new TreeSet<>();
        }
        
        MemoryLogRecord fromEvent = new MemoryLogRecord();
        fromEvent.setMillis(timestamp);
        MemoryLogRecord toEvent = new MemoryLogRecord();
        toEvent.setMillis(now);
        return this._logsPile.subSet(fromEvent, toEvent);
    }
    
    @Override
    public boolean requiresLayout()
    {
        return false;
    }

    @Override
    public void close() 
    {
        // Empty implementation, do nothing
    }

}
