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
        CocoonWrapper cocoon = _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp2");
        
        assertTrue(WorkspaceManager.getInstance().getWorkspaceNames().contains("admin"));
        
        assertTrue(WorkspaceManager.getInstance().getWorkspaceNames().contains("workspace-test"));
        assertEquals(WorkspaceManager.getInstance().getWorkspaces().get("workspace-test").getThemeName(), "ametys-admin");
        
        // workspace-test2 don't have workspace.xml
        assertFalse(WorkspaceManager.getInstance().getWorkspaceNames().contains("workspace-test2"));
        
        // workspace-test3 has a dependency to an unexisting feature
        assertTrue(WorkspaceManager.getInstance().getWorkspaceNames().contains("workspace-test3"));
        assertEquals(WorkspaceManager.getInstance().getWorkspaces().get("workspace-test3").getThemeName(), "ametys-base");
    
        cocoon.dispose();
    }
    
    /**
     * Tests an invalid workspaces set : the workspace 'admin' is defined twice
     * @throws Exception if an error occurs
     */
    public void testInvalidWorkspace() throws Exception
    {
        CocoonWrapper cocoon = null;
        
        try
        {
            cocoon = _startApplication("test/environments/runtimes/runtime01.xml", "test/environments/configs/config1.xml", "test/environments/webapp3");
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
