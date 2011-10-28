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

import org.ametys.runtime.config.Config;
import org.ametys.runtime.workspace.WorkspaceManager;

/**
 * Tests WorkspacesManager
 */
public class WorkspacesTestCase extends AbstractRuntimeTestCase
{
    /**
     * Tests many workspaces cases : dependencies, sitemap availability, ...
     * @throws Exception if an error occurs
     */
    public void testValidWorkspace() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime10.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        
        CocoonWrapper cocoon = _startCocoon("test/environments/webapp2");
        
        assertTrue(WorkspaceManager.getInstance().getEmbeddedWorskpacesIds().contains("admin"));
        assertTrue(WorkspaceManager.getInstance().getWorkspaceNames().contains("admin"));
        
        assertEquals("/org/ametys/runtime/workspaces/admin", WorkspaceManager.getInstance().getBaseURI("admin"));
        
        assertTrue(WorkspaceManager.getInstance().getWorkspaceNames().contains("workspace-test"));
        
        // Le workspace-test2 n'a pas de workspace.xml
        assertFalse(WorkspaceManager.getInstance().getWorkspaceNames().contains("workspace-test2"));
        
        // Le workspace-test3 a une dépendance vers une feature inexistante
        assertFalse(WorkspaceManager.getInstance().getWorkspaceNames().contains("workspace-test3"));
    
        // Restart
        cocoon.dispose();
        
        _configureRuntime("test/environments/runtimes/runtime3.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        cocoon = _startCocoon("test/environments/webapp2");
        
        cocoon.dispose();
    }
    
    /**
     * Tests an invalid workspaces set : the workspace 'admin' is defined twice
     * @throws Exception if an error occurs
     */
    public void testInvalidWorkspace() throws Exception
    {
        CocoonWrapper cocoon = null;
        
        _configureRuntime("test/environments/runtimes/runtime10.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        
        try
        {
            cocoon = _startCocoon("test/environments/webapp3");
            fail("WorskpaceManager must have failed");
        }
        catch (IllegalArgumentException e)
        {
            // it is ok
        }
        finally
        {
            if (cocoon != null)
            {
                cocoon.dispose();
            }
        }
    }
}
