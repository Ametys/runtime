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
package org.ametys.runtime.workspaces.admin;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.xml.sax.SAXException;

import org.ametys.core.ui.ClientSideElementDependenciesManager;
import org.ametys.core.ui.ClientSideElementManager;
import org.ametys.core.ui.StaticFileImportsManager;
import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.PluginsManager.Status;

/**
 * Admin workspace handling safe mode.
 */
public class WorkspaceGenerator extends org.ametys.plugins.core.ui.WorkspaceGenerator
{
    @Override
    public void generate() throws IOException , SAXException, ProcessingException 
    {
        Map<String, Object> contextParameters = new HashMap<>();
        contextParameters.put("workspace", "admin");
        doGenerate(contextParameters); //FIXME handle rights for workspace admin, here is a temporary workaround
    }
    
    @Override
    protected InputStream getRibbonConfiguration() throws IOException
    {
        if (PluginsManager.getInstance().isSafeMode())
        {
            Status status = PluginsManager.getInstance().getStatus();
            if (status == Status.RUNTIME_NOT_LOADED || status == Status.WRONG_DEFINITIONS)
            {
                return getClass().getResourceAsStream("admin-safe-ribbon-noconfig.xml");
            }
            else
            {
                return getClass().getResourceAsStream("admin-safe-ribbon.xml");
            }
        }
        
        return super.getRibbonConfiguration();
    }
    
    @Override
    protected InputStream getUIToolsConfiguration() throws IOException
    {
        if (PluginsManager.getInstance().isSafeMode())
        {
            Status status = PluginsManager.getInstance().getStatus();
            if (status == Status.CONFIG_INCOMPLETE || status == Status.NO_CONFIG)
            {
                return getClass().getResourceAsStream("admin-safe-uitools-config.xml");
            }
            else
            {
                return getClass().getResourceAsStream("admin-safe-uitools.xml");
            }
        }
        
        return super.getUIToolsConfiguration();
    }
    
    @Override
    protected void registerStaticDependencies(ClientSideElementDependenciesManager dependenciesManager) throws SAXException
    {
        super.registerStaticDependencies(dependenciesManager);
        try
        {
            ClientSideElementManager staticImportManager = (ClientSideElementManager) this.manager.lookup(StaticFileImportsManager.ROLE);
            if (staticImportManager.hasExtension("org.ametys.runtime.plugins.admin.AdministratorNotifications"))
            {
                dependenciesManager.register(StaticFileImportsManager.ROLE, "org.ametys.runtime.plugins.admin.AdministratorNotifications");
            }
            else
            {
                getLogger().warn("Unable to add static core-ui dependency to the extension 'org.ametys.runtime.plugins.admin.AdministratorNotifications'");
            }
        }
        catch (ServiceException e)
        {
            throw new SAXException("An error occurred while retrieving a ClientSideElementManager", e);
        }
    }
}
