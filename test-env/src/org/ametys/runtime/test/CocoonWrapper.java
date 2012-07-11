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
package org.ametys.runtime.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.excalibur.logger.LogKitLoggerManager;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.commandline.CommandLineContext;
import org.apache.cocoon.environment.commandline.CommandLineSession;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.log.Hierarchy;
import org.apache.log.Priority;
import org.xml.sax.ContentHandler;

import org.ametys.runtime.plugin.Init;
import org.ametys.runtime.plugin.InitExtensionPoint;
import org.ametys.runtime.plugin.component.PluginsComponentManager;

/**
 * The Cocoon Wrapper simplifies usage of the Cocoon object. Allows to create, configure Cocoon instance and process single requests.
 * 
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:nicolaken@apache.org">Nicola Ken Barozzi</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version $Id$
 */
public class CocoonWrapper
{
    // User Supplied Parameters
    private String _contextDir;

    private String _workDir;

    private String _logLevel = "ERROR";

    // Internal Objects
    private CommandLineContext _cliContext;

    private Cocoon _cocoon;

    private Logger _logger;

    CocoonWrapper(String contextDir, String workDir)
    {
        _contextDir = contextDir;
        _workDir = workDir;
    }

    @SuppressWarnings("deprecation")
    void initialize() throws Exception
    {
        Hierarchy hierarchy = Hierarchy.getDefaultHierarchy();

        Priority priority = Priority.getPriorityForName(_logLevel);
        hierarchy.setDefaultPriority(priority);

        // Install a temporary logger so that getDir() can log if needed
        _logger = new LogKitLogger(hierarchy.getLoggerFor(""));

        try
        {
            DefaultContext appContext = new DefaultContext();
            appContext.put(Constants.CONTEXT_WORK_DIR, new File(_workDir));

            LogKitLoggerManager logManager = new LogKitLoggerManager(hierarchy);
            logManager.enableLogging(_logger);

            File conf = new File(_contextDir, "WEB-INF/cocoon.xconf");

            _cliContext = new CommandLineContext(_contextDir);
            _cliContext.enableLogging(_logger);
            
            File cacheDir = new File(_workDir, "cache-dir");
            cacheDir.mkdirs();

            appContext.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, _cliContext);
            appContext.put(Constants.CONTEXT_CLASS_LOADER, CocoonWrapper.class.getClassLoader());
            appContext.put(Constants.CONTEXT_CLASSPATH, "unknown");
            appContext.put(Constants.CONTEXT_UPLOAD_DIR, _contextDir + "upload-dir");
            appContext.put(Constants.CONTEXT_CACHE_DIR, cacheDir);
            appContext.put(Constants.CONTEXT_CONFIG_URL, conf.toURL());
            appContext.put(Constants.CONTEXT_DEFAULT_ENCODING, "ISO-8859-1");

            _cocoon = new Cocoon();
            ContainerUtil.enableLogging(_cocoon, _logger);
            ContainerUtil.contextualize(_cocoon, appContext);
            _cocoon.setLoggerManager(logManager);
            ContainerUtil.initialize(_cocoon);
            
            PluginsComponentManager pluginCM = (PluginsComponentManager) _cliContext.getAttribute("PluginsComponentManager");
            
            if (pluginCM != null)
            {
                // Plugins Init class execution
                InitExtensionPoint initExtensionPoint = (InitExtensionPoint) pluginCM.lookup(InitExtensionPoint.ROLE);
                for (String id : initExtensionPoint.getExtensionsIds())
                {
                    Init init = initExtensionPoint.getExtension(id);
                    init.init();
                }
                
                // Application Init class execution if available
                if (pluginCM.hasComponent(Init.ROLE))
                {
                    Init init = (Init) pluginCM.lookup(Init.ROLE);
                    init.init();
                }
            }
            
            CommandLineSession.invalidateSession();
        }
        catch (Exception e)
        {
            _logger.fatalError("Exception caught", e);
            throw e;
        }
    }

    /**
     * Process single URI into given content handler, skipping final serializer
     * @param uri to process
     * @param handler to write generated contents into
     * @param requestParameters the request parameters
     * @param requestAttributes the request attributes
     * @param requestHeaders the request headers
     * @throws Exception if an error occurs
     */
    public void processURI(String uri, ContentHandler handler, Map<String, String> requestParameters, Map<String, Object> requestAttributes, Map<String, String> requestHeaders) throws Exception
    {
        _logger.info("Processing URI: " + uri);

        // Get parameters, headers, deparameterized URI and path from URI
        Map<String, String> headers = requestHeaders;
        if (headers == null)
        {
            headers = new HashMap<String, String>();
        }
        
        headers.put("user-agent", "Ametys Runtime");
        headers.put("accept", "text/html, */*");

        Environment env = new TestEnvironment(uri, requestAttributes, requestParameters, headers, _cliContext, _logger);

        XMLConsumer consumer = new ContentHandlerWrapper(handler);
        CocoonComponentManager.enterEnvironment(env, _cocoon.getComponentManager(), _cocoon);
        ProcessingPipeline pipeline = _cocoon.buildPipeline(env);
        try
        {
            pipeline.prepareInternal(env);
            pipeline.process(env, consumer);
        }
        finally
        {
            CocoonComponentManager.leaveEnvironment();
        }
    }
    
    /**
     * Dispose current cocoon instance.
     */
    public void dispose()
    {
        // Dispose the plugins component manager.
        PluginsComponentManager pluginCM = (PluginsComponentManager) _cliContext.getAttribute("PluginsComponentManager");
        if (pluginCM != null)
        {
            pluginCM.dispose();
        }
        
        // Dispose the cocoon instance.
        _cocoon.dispose();
    }
}
