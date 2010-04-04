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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
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
public class ChangeLogLevelAction extends AbstractAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);

        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Administrator change log level");
        }

        LoggerRepository loggerRepository = LogManager.getLoggerRepository();
        Logger rootLogger = loggerRepository.getRootLogger();
        
        String category = request.getParameter("category");
        String level = request.getParameter("level");
        boolean inherited = "INHERIT".equals(level);

        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Log level changing category '" + category + "' " + (inherited ? "to inherited" : ("to level " + level)));
        }

        try
        {
            changeLogkit(loggerRepository, rootLogger, category, inherited, level);
        }
        catch (Throwable t)
        {
            String errorMessage = "Cannot change log level correctly : changing category '" + category + "' " + (inherited ? "to inherited" : ("to level " + level));
            getLogger().error(errorMessage, t);
            Map<String, String> results = new HashMap<String, String>();
            results.put("error", "error");
            return results;
        }

        return EMPTY_MAP;
    }
    
    private void changeLogkit(LoggerRepository loggerRepository, Logger rootLogger, String category, boolean inherited, String mode)
    {
        Logger logger = loggerRepository.getLogger(category);
    
        boolean isRoot = false;
        if ("".equals(category))
        {
            isRoot = true;
        }
    
        if (inherited)
        {
            if (!isRoot)
            {
                logger.setLevel(null);
            }
        }
        else
        {
            Level level = Level.toLevel(mode);

            if (isRoot)
            {
                rootLogger.setLevel(level);
            }
            else
            {
                logger.setLevel(level);
            }
        }
    }
}
