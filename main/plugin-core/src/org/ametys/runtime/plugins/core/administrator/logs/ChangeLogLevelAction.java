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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;

/**
 * Change the log level for specified categories.<br>
 * The categories are specified through request parameters with the following syntax :
 * priority_&lt;my.log.category>=&lt;log-level>
 */
public class ChangeLogLevelAction extends AbstractAction implements ThreadSafe
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Administrator change log level");
        }

        LoggerRepository loggerRepository = LogManager.getLoggerRepository();
        
        String category = request.getParameter("category");
        String level = request.getParameter("level");

        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Log level changing category '" + category + "' " + level);
        }

        try
        {
            changeLogLevel(loggerRepository, category, level);
        }
        catch (Throwable t)
        {
            String errorMessage = "Cannot change log level correctly : changing category '" + category + "'";
            getLogger().error(errorMessage, t);
            Map<String, String> results = new HashMap<String, String>();
            results.put("error", "error");
            return results;
        }

        return EMPTY_MAP;
    }
    
    private void changeLogLevel(LoggerRepository loggerRepository, String category, String mode)
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
                    changeLogLevel(loggerRepository, l.getName(), "INHERITFORCED");
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
