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

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;
import org.apache.commons.lang.BooleanUtils;

import org.ametys.runtime.plugin.PluginsManager;
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
            Context ctx = (Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
            String contextPath = ctx.getRealPath("/");
            
            // WorkspaceManager loading
            WorkspaceManager.getInstance().init(RuntimeConfig.getInstance().getExcludedWorkspaces(), contextPath);
            
            boolean forceSafeMode = BooleanUtils.toBoolean((Boolean) ctx.getAttribute("org.ametys.runtime.forceSafeMode"));
            PluginsComponentManager pluginCM = PluginsManager.getInstance().init(componentManager, context, contextPath, forceSafeMode);
            ctx.removeAttribute("org.ametys.runtime.forceSafeMode");
            
            // Effective substitution of the Cocoon CM
            super.compose(pluginCM);
            _pluginCM = pluginCM;
            
            // Set the new ComponentManager in the servlet context so that it can be retrieved bt the Servlet 
            ctx.setAttribute("PluginsComponentManager", pluginCM);
        }
        catch (ComponentException | RuntimeException e)
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
