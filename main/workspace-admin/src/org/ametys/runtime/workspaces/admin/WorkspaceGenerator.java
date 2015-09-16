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

import org.ametys.runtime.plugin.PluginsManager;
import org.ametys.runtime.plugin.PluginsManager.Status;

/**
 * Admin workspace handling safe mode.
 */
public class WorkspaceGenerator extends org.ametys.plugins.core.ui.WorkspaceGenerator
{
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
            
            return getClass().getResourceAsStream("admin-safe-ribbon.xml");
        }
        
        return super.getRibbonConfiguration();
    }
    
    @Override
    protected InputStream getUIToolsConfiguration() throws IOException
    {
        if (PluginsManager.getInstance().isSafeMode())
        {
            return getClass().getResourceAsStream("admin-safe-uitools.xml");
        }
        
        return super.getUIToolsConfiguration();
    }
}
