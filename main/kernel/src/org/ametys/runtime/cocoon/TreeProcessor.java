/*
 *  Copyright 2012 Anyware Services
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
package org.ametys.runtime.cocoon;

import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.WrapperServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.util.log.SLF4JLoggerAdapter;
import org.slf4j.LoggerFactory;

import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.PluginsManager.FeatureInformation;
import org.ametys.runtime.plugin.component.PluginsComponentManager;
import org.ametys.runtime.servlet.RuntimeConfig;
import org.ametys.runtime.workspace.WorkspaceManager;

/**
 * Own TreeProcessor implementation used to initialize plugin stuff.<br>
 * This allows plugins to access ComponentManager and sitemaps components to access plugins.
 */
public class TreeProcessor extends org.apache.cocoon.components.treeprocessor.TreeProcessor
{
    private PluginsComponentManager _pluginCM;
    
    @Override
    public void compose(ComponentManager componentManager) throws ComponentException
    {
        try
        {
            // Insert our CM between the sitemap one and the Cocoon one
            PluginsComponentManager pluginCM = new PluginsComponentManager(componentManager);
            Logger logger = new SLF4JLoggerAdapter(LoggerFactory.getLogger("org.ametys.runtime.plugin.manager"));
            ContainerUtil.enableLogging(pluginCM, logger);
            ContainerUtil.contextualize(pluginCM, context);
            ContainerUtil.service(pluginCM, new WrapperServiceManager(pluginCM));
            
            // store the context path
            Context ctx = (Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
            
            String contextPath = ctx.getRealPath("/");
            
            Map<String, FeatureInformation> info = PluginsManager.getInstance().init(pluginCM, context, contextPath);
            
            if (info != null)
            {
                ContainerUtil.initialize(pluginCM);

                // Extensions loading
                PluginsManager.getInstance().initExtensions(pluginCM, info, contextPath);
                
                // Effective substitution of the Cocoon CM
                super.compose(pluginCM);
                _pluginCM = pluginCM;
                
                // Set the new ComponentManager in the servlet context so that it can be retrieved bt the Servlet 
                Context cocoonContext = (Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
                cocoonContext.setAttribute("PluginsComponentManager", pluginCM);
            }
            else
            {
                // In case of incomplete config, don't use our components
                super.compose(componentManager);
            }
            
            // WorkspaceManager loading
            WorkspaceManager.getInstance().init(RuntimeConfig.getInstance().getExcludedWorkspaces(), contextPath, info);
        }
        catch (ComponentException e)
        {
            throw e;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ComponentException(ROLE, "Unable to initialize the ComponentManager", e);
        }
    }
    
    @Override
    public void dispose()
    {
        super.dispose();
        
        if (_pluginCM != null)
        {
            _pluginCM.dispose();
            _pluginCM = null;
        }
    }
}
