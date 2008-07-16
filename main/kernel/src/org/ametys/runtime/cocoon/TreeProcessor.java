/*
 * Copyright (c) 2007 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 */
package org.ametys.runtime.cocoon;

import java.util.Map;

import org.ametys.runtime.plugin.Init;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.PluginsManager.FeatureInformation;
import org.ametys.runtime.plugin.component.PluginsComponentManager;
import org.ametys.runtime.servlet.RuntimeConfig;
import org.ametys.runtime.util.LoggerFactory;
import org.ametys.runtime.workspace.WorkspaceManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.WrapperServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Context;

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
            // On "intercale" notre CM entre celui de la sitemap et le CocoonECM
            PluginsComponentManager pluginCM = new PluginsComponentManager(componentManager);
            ContainerUtil.enableLogging(pluginCM, LoggerFactory.getLoggerFor("org.ametys.runtime.plugin.manager"));
            ContainerUtil.contextualize(pluginCM, context);
            ContainerUtil.service(pluginCM, new WrapperServiceManager(pluginCM));
            
            // le context path
            Context ctx = (Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
            
            String contextPath = ctx.getRealPath("/");
            
            // Chargement des plugins
            Map<String, FeatureInformation> info = PluginsManager.getInstance().init(pluginCM, context, contextPath);
            
            if (info != null)
            {
                ContainerUtil.initialize(pluginCM);

                // Chargement des extensions
                PluginsManager.getInstance().initExtensions(pluginCM, info, contextPath);
                
                // Le remplacement effectif du CM de Cocoon par le nôtre
                super.compose(pluginCM);
                _pluginCM = pluginCM;
                
                // Exécution de la classe d'init si elle existe
                if (pluginCM.hasComponent(Init.ROLE))
                {
                    Init init = (Init) pluginCM.lookup(Init.ROLE);
                    init.init();
                }
            }
            else
            {
                // Si la config n'est pas chargée, on n'utilise pas nos composants
                super.compose(componentManager);
            }
            
            // Chargement du WorkspaceManager
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
