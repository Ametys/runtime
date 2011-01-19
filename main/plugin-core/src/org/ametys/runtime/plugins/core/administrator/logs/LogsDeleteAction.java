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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.ModifiableTraversableSource;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.TraversableSource;

/**
 * Delete given logs
 */
public class LogsDeleteAction extends ServiceableAction implements ThreadSafe
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        StringBuffer failure = new StringBuffer(); 
        StringBuffer done = new StringBuffer(); 
        
        Request request = ObjectModelHelper.getRequest(objectModel);

        // Liste les fichiers
        String[] files;
        if ("true".equals(parameters.getParameter("purge", "false")))
        {
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Administrator starts a purge of logged file");
            }
            files = _purgeFiles(resolver);
        }
        else
        {
            files = request.getParameterValues("file");
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Administrator starts a deletion of " + files.length + " logged file");
            }
        }
        
        // Efface les fichier listés
        for (String file : files)
        {
            _deleteFile(file, resolver, failure, done);
        }
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Process terminated with following results : failure '" + failure.toString() + "' and done '" + done.toString() + "'");
        }

        Map<String, String> result = new HashMap<String, String>();
        result.put("failure", failure.toString());
        result.put("done", done.toString());
        return result;
    }
    
    private void _deleteFile(String file, SourceResolver resolver, StringBuffer failure, StringBuffer done) throws IOException, ProcessingException
    {
        if (file.indexOf("/") != -1 || file.indexOf('\\') != -1)
        {
            String message = "The LogsDeleteAction has been call with the forbiden parameter '" + file + "'";
            getLogger().error(message);
            throw new ProcessingException(message);
        }
        
        ModifiableSource logsource = null;
        try
        {
            if (file.endsWith(".log"))
            {
                logsource = (ModifiableSource) resolver.resolveURI("context://WEB-INF/logs/" + file);
                if (getLogger().isInfoEnabled())
                {
                    getLogger().info("Removing log file " + logsource.getURI());
                }
                logsource.delete();
                done.append('/');
                done.append(file);
                done.append('/');
            }
            else if (getLogger().isWarnEnabled())
            {
                getLogger().warn("Ignoring this file during deletion '" + "' because it does not ends with .log");
            }
        }
        catch (SourceException e)
        {
            failure.append('/');
            failure.append(file);
            failure.append('/');
            getLogger().error("The administrator tried unsuccessfully to remove the following log file '" + (logsource != null ? logsource.getURI() : file) + "'.", e);
        }
        finally
        {
            if (logsource != null)
            {
                resolver.release(logsource);
            }
        }
    }

    private String[] _purgeFiles(SourceResolver resolver) throws ProcessingException
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
            logsSources = (TraversableSource) resolver.resolveURI("context://WEB-INF/logs");
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
            
            String[] files = new String[filesList.size()];
            filesList.toArray(files);
            return files;
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
                resolver.release(logsSources);
            }
        }  
    }
}
