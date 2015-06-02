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
package org.ametys.runtime.plugins.core.administrator.logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;

import org.ametys.runtime.ui.Callable;
import org.ametys.runtime.ui.StaticClientSideElement;

/**
 * Client side element for log actions
 */
public class LogsClientSideElement extends StaticClientSideElement
{
    /** The path to the application's logs folder */
    private static final String __LOGS_BASE = "context://WEB-INF/logs/";
    
    /** The Excalibur source resolver */
    private SourceResolver _sourceResolver;
    
    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _sourceResolver = (SourceResolver) serviceManager.lookup(SourceResolver.ROLE);
    }
    
    /**
     * Delete the log files
     * @param files the files to delete
     * @return the result map with the successes and failures files. 
     * @throws IOException if an error occurred during deletion
     * @throws ProcessingException if an error occurred during deletion
     */
    @Callable
    public Map<String, Object> deleteLogs(List<String> files) throws ProcessingException, IOException
    {
        Map<String, Object> result = new HashMap<>();
        List<String> failures = new ArrayList<> ();
        List<String> successes = new ArrayList<> ();
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Administrator starts a deletion of " + files.size() + " logged file");
        }
        
        // Delete the selected files one by one
        for (String location : files)
        {
            if (_deleteFile(location))
            {
                successes.add(location);
            }
            else
            {
                failures.add(location);
            }
        }
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Process terminated with following results : failure '" + failures.toString() + " successes '" + successes.toString());
        }
        
        result.put("failures", failures);
        result.put("successes", successes);
        return result;
    }
    
    /**
     * Delete the log entries that are at least 12 days old
     * @return the result 
     * @throws ProcessingException if an exception occurs while processing the deletion
     */
    @Callable
    public Map<String, Object> purgeLogs() throws ProcessingException
    {
        Calendar purgeCalendar = new GregorianCalendar();
        purgeCalendar.add(Calendar.DAY_OF_MONTH, -12);
        Date purgeDate = new Date(purgeCalendar.getTimeInMillis());

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting purge...");
            getLogger().debug("Purge date is " + purgeDate);
        }

        List<String> filesList = new ArrayList<String>();
        TraversableSource logsSources = null;
        try
        {
            logsSources = (TraversableSource) _sourceResolver.resolveURI(__LOGS_BASE);
            for (Object log : logsSources.getChildren())
            {
                ModifiableTraversableSource logSource = (ModifiableTraversableSource) log;
                if (!logSource.isCollection() && logSource.getURI().endsWith(".log") && new Date(logSource.getLastModified()).before(purgeDate))
                {
                    String location = logSource.getURI();
                    String name = location.substring(location.lastIndexOf('/') + 1);
                    filesList.add(name);
                    if (getLogger().isDebugEnabled())
                    {
                        getLogger().debug("Adding file to purge : " + name);
                    }
                }
            }
            
            return deleteLogs(filesList);
        }
        catch (SourceException e)
        {
            String message = "The purge of old log files failed";
            getLogger().error(message, e);
            throw new ProcessingException(message, e);
        }
        catch (IOException e)
        {
            String message = "The log directory was not found";
            getLogger().error(message, e);
            throw new ProcessingException(message, e);
        }
        finally
        {
            if (logsSources != null)
            {
                _sourceResolver.release(logsSources);
            }
        }  
    }
    
    private boolean _deleteFile(String fileLocation) throws IOException, ProcessingException
    {
        if (fileLocation.indexOf("/") != -1 || fileLocation.indexOf('\\') != -1)
        {
            String message = "The LogsDeleteAction has been call with the forbiden parameter '" + fileLocation + "'";
            getLogger().error(message);
            throw new ProcessingException(message);
        }
        
        ModifiableSource logsource = null;
        try
        {
            if (fileLocation.endsWith(".log"))
            {
                logsource = (ModifiableSource) _sourceResolver.resolveURI(__LOGS_BASE + fileLocation);
                if (getLogger().isInfoEnabled())
                {
                    getLogger().info("Removing log file " + logsource.getURI());
                }
                logsource.delete();
                return true;
            }
            else if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Ignoring this file during deletion '" + "' because it does not ends with .log");
            }
            return false;
        }
        catch (SourceException e)
        {
            getLogger().error("The administrator tried unsuccessfully to remove the following log file '" + (logsource != null ? logsource.getURI() : fileLocation) + "'.", e);
            return false;
        }
        finally
        {
            if (logsource != null)
            {
                _sourceResolver.release(logsource);
            }
        }
    }
    
    /**
     * Change the log level of the selected category to the selected level
     * @param level the selected level
     * @param category the selected category
     * @return a map
     */
    @Callable
    public Map<String, Object> changeLogLevel(String level, String category)
    {
        Map<String, Object> result = new HashMap<>();
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Administrator change log level");
        }

        LoggerRepository loggerRepository = LogManager.getLoggerRepository();
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Log level changing category '" + category + "' " + level);
        }

        try
        {
            _changeLogLevel(loggerRepository, category, level);
        }
        catch (Throwable t)
        {
            String errorMessage = "Cannot change log level correctly : changing category '" + category + "'";
            getLogger().error(errorMessage, t);
            result.put("error", "error");
            return result;
        }
        
        return result;
    }
    
    private void _changeLogLevel(LoggerRepository loggerRepository, String category, String mode)
    {
        boolean inherited = "INHERIT".equals(mode) || "INHERITFORCED".equals(mode);
        boolean force = "FORCE".equals(mode) || "INHERITFORCED".equals(mode);

        Logger logger;
        boolean isRoot = false;
        
        if ("root".equals(category))
        {
            isRoot = true;
            logger = loggerRepository.getRootLogger();
        }
        else
        {
            logger = loggerRepository.getLogger(category);
        }
    
        if (inherited && !isRoot)
        {
            logger.setLevel(null);
        }
        
        if (force)
        {
            Enumeration<Logger> e = loggerRepository.getCurrentLoggers();
            while (e.hasMoreElements())
            {
                Logger l = e.nextElement();
                if (l.getParent() == logger)
                {
                    _changeLogLevel(loggerRepository, l.getName(), "INHERITFORCED");
                }
            }
        }

        if (!inherited && !force)
        {
            Level level = Level.toLevel(mode);
            logger.setLevel(level);
        }
    }
}
