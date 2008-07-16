package org.ametys.runtime.plugins.core.administrator.logs;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.log.Priority;

import org.ametys.runtime.util.LoggerFactory;

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

        int nbParameters = Integer.parseInt(request.getParameter("nb"));

        if (getLogger().isInfoEnabled())
        {
            getLogger().info(nbParameters + " log levels to change");
        }

        Hierarchy hierarchy = LoggerFactory.getHierarchy();
        Logger rootLogger = hierarchy.getRootLogger();

        for (int i = 0; i < nbParameters; i++)
        {
            boolean logkit = "logkit".equals(request.getParameter("type_" + i));
            String category = request.getParameter("cat_" + i);
            boolean inherited = "true".equals(request.getParameter("inherit_" + i));
            String mode = request.getParameter("mode_" + i);
            
            if (getLogger().isInfoEnabled())
            {
                getLogger().info("Log level (n°" + i + ") changing " + (logkit ? "LOGKIT" : "") + " category '" + category + "' " + (inherited ? "to inherited" : ("to mode " + mode)));
            }

            try
            {
                changeLogkit(hierarchy, rootLogger, category, inherited, mode);
            }
            catch (Throwable t)
            {
                String errorMessage = "Cannot change log level correctly : log level (n°" + i + ") changing " + (logkit ? "LOGKIT" : "") + " category '" + category + "' " + (inherited ? "to inherited" : ("to mode " + mode));
                getLogger().error(errorMessage, t);
                Map<String, String> results = new HashMap<String, String>();
                results.put("error", "error");
                return results;
            }
        }
        
        if (getLogger().isInfoEnabled())
        {
            getLogger().info("Process terminated correctly");
        }

        return EMPTY_MAP;
    }

    private void changeLogkit(Hierarchy hierarchy, Logger rootLogger, String category, boolean inherited, String mode)
    {
        Logger logger = hierarchy.getLoggerFor(category);
    
        boolean isRoot = false;
        if ("".equals(category))
        {
            isRoot = true;
        }
    
        if (inherited)
        {
            logger.unsetPriority();
            
            if (isRoot)
            {
                rootLogger.unsetPriority();
            }
        }
        else
        {
            Priority priority;
            
            if ("ERROR".equalsIgnoreCase(mode))
            {
                priority = Priority.ERROR;
            }
            else if ("WARNING".equalsIgnoreCase(mode))
            {
                priority = Priority.WARN;
            }
            else if ("INFO".equalsIgnoreCase(mode))
            {
                priority = Priority.INFO;
            }
            else
            {
                priority = Priority.DEBUG;
            }

            logger.setPriority(priority);
            
            if (isRoot)
            {
                rootLogger.setPriority(priority);
            }
        }
    }
}
