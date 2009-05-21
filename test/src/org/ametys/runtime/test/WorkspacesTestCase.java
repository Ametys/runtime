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
        
        _startCocoon("test/environments/webapp2");
        
        assertTrue(WorkspaceManager.getInstance().getEmbeddedWorskpacesIds().contains("admin"));
        assertTrue(WorkspaceManager.getInstance().getWorkspaceNames().contains("admin"));
        
        assertEquals("/org/ametys/runtime/workspaces/admin", WorkspaceManager.getInstance().getBaseURI("admin"));
        
        assertTrue(WorkspaceManager.getInstance().getWorkspaceNames().contains("workspace-test"));
        
        // Le workspace-test2 n'a pas de workspace.xml
        assertFalse(WorkspaceManager.getInstance().getWorkspaceNames().contains("workspace-test2"));
        
        // Le workspace-test3 a une dépendance vers une feature inexistante
        assertFalse(WorkspaceManager.getInstance().getWorkspaceNames().contains("workspace-test3"));
    
        // Restart
        _configureRuntime("test/environments/runtimes/runtime3.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        _startCocoon("test/environments/webapp2");
    }
    
    /**
     * Tests an invalid workspaces set : the workspace 'admin' is defined twice
     * @throws Exception if an error occurs
     */
    public void testInvalidWorkspace() throws Exception
    {
        _configureRuntime("test/environments/runtimes/runtime10.xml");
        Config.setFilename("test/environments/configs/config1.xml");
        
        try
        {
            _startCocoon("test/environments/webapp3");
            fail("WorskpaceManager must have failed");
        }
        catch (IllegalArgumentException e)
        {
            // it is ok
        }
    }
}
