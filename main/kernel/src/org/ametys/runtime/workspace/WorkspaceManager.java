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
package org.ametys.runtime.workspace;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.ametys.runtime.servlet.RuntimeConfig;


/**
 * Main entry point for access to workspaces.
 */
public final class WorkspaceManager
{
    // shared instance
    private static WorkspaceManager __manager;
    
    private static final String __WORKSPACE_FILENAME = "workspace.xml";

    /**
     * Cause of the deactivation of a workspace
     */
    public enum InactivityCause
    {
        /**
         * Constant for excluded features
         */
        EXCLUDED
    }
    
    // Map<workspaceName, baseURI>
    private Map<String, String> _workspaces = new HashMap<>(); 
    
    // workspaces/locations association
    private Map<String, File> _locations = new HashMap<>();

    // All workspaces' names
    // _workspaceNames is NOT the same as _workspaces.keySet(), which only contains embedded workspaces
    private Set<String> _workspaceNames = new HashSet<>();
    
    private Map<String, InactivityCause> _inactiveWorkspaces = new HashMap<>();

    private Logger _logger = LoggerFactory.getLogger(WorkspaceManager.class); 

    
    private WorkspaceManager()
    {
        // empty constructor
    }
    
    /**
     * Returns the shared instance of the <code>WorkspaceManager</code>
     * @return the shared instance of the <code>WorkspaceManager</code>
     */
    public static WorkspaceManager getInstance()
    {
        if (__manager == null)
        {
            __manager = new WorkspaceManager();
        }
        
        return __manager;
    }
    
    /**
     * Returns all workspaces names
     * @return all workspaces names
     */
    public Set<String> getWorkspaceNames()
    {
        return _workspaceNames;
    }
    
    /** 
     * Return the inactive workspaces
     * @return All the inactive workspaces
     */
    public Map<String, InactivityCause> getInactiveWorkspaces()
    {
        return Collections.unmodifiableMap(_inactiveWorkspaces);
    }
    
    /**
     * Returns a String array containing the ids of the plugins embedded in jars
     * @return a String array containing the ids of the plugins embedded in jars
     */
    public Set<String> getEmbeddedWorskpacesIds()
    {
        return _workspaces.keySet();
    }

    /**
     * Returns the base URI associated with the given workspace, or null if the workspace does not exist
     * @param workspaceName the name of the working workspace
     * @return the URI associated with the given workspace
     */
    public String getBaseURI(String workspaceName)
    {
        return _workspaces.get(workspaceName);
    }
    
    /**
     * Returns the workspace filesystem location for the given workspace or null if the workspace is loaded from the classpath.
     * @param workspaceName the workspace name
     * @return the workspace location for the given workspace
     */
    public File getLocation(String workspaceName)
    {
        return _locations.get(workspaceName);
    }

    /**
     * Initialize the WorkspaceManager.<br>
     * It first looks for META-INF/runtime-workspace files in the classpath, then in the "workspaces" directory of the application.
     * @param excludedWorkspace the excluded workspaces, as given by the RuntimeConfig
     * @param contextPath the servlet context path
     * @throws IOException if an error occurs while retrieving resources
     */
    public void init(Collection<String> excludedWorkspace, String contextPath) throws IOException
    {
        _workspaces.clear();
        _workspaceNames.clear();
        
        // Begin with workspace embedded in jars
        Enumeration<URL> workspaceResources = getClass().getClassLoader().getResources("META-INF/ametys-workspaces");
        while (workspaceResources.hasMoreElements())
        {
            URL workspaceResource = workspaceResources.nextElement();
            _initResourceWorkspace(excludedWorkspace, workspaceResource);
        }

        // Then workspace from the "<context>/workspaces" directory
        File workspacesDir = new File(contextPath, "workspaces");
        if (workspacesDir.exists() && workspacesDir.isDirectory())
        {
            for (File workspace : new File(contextPath, "workspaces").listFiles())
            {
                _initFileWorkspaces(workspace, workspace.getName(), excludedWorkspace);
            }
        }
        
        Map<String, File> externalWorkspaces = RuntimeConfig.getInstance().getExternalWorkspaces();
        
        // external workspaces
        for (String externalWorkspace : externalWorkspaces.keySet())
        {
            File workspaceDir = externalWorkspaces.get(externalWorkspace);

            if (workspaceDir.exists() && workspaceDir.isDirectory())
            {
                _initFileWorkspaces(workspaceDir, externalWorkspace, excludedWorkspace);
            }
            else
            {
                throw new RuntimeException("The configured external workspace is not an existing directory: " + workspaceDir.getAbsolutePath());
            }
        }
    }
    
    private void _initResourceWorkspace(Collection<String> excludedWorkspace, URL workspaceResource) throws IOException
    {
        try (InputStream is = workspaceResource.openStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8")))
        {
            String workspace;
            while ((workspace = br.readLine()) != null)
            {
                int i = workspace.indexOf(':');
                if (i != -1)
                {
                    String workspaceName = workspace.substring(0, i);       
                    String workspaceBaseURI = workspace.substring(i + 1);
                    
                    if (!excludedWorkspace.contains(workspaceName))
                    {
                        if (_workspaceNames.contains(workspaceName))
                        {
                            String errorMessage = "The workspace named " + workspaceName + " already exists";
                            _logger.error(errorMessage);
                            throw new IllegalArgumentException(errorMessage);
                        }
                        
                        if (getClass().getResource(workspaceBaseURI + "/" + __WORKSPACE_FILENAME) == null)
                        {
                            if (_logger.isWarnEnabled())
                            {
                                _logger.warn("A workspace '" + workspaceName + "' is declared in a library, but no file '" + __WORKSPACE_FILENAME + "' can be found at '" + workspaceBaseURI + "'. Workspace will be ignored.");
                                return;
                            }
                        }
                        
                        _workspaceNames.add(workspaceName);
                        _workspaces.put(workspaceName, workspaceBaseURI);
                        
                        if (_logger.isInfoEnabled())
                        {
                            _logger.info("Workspace '" + workspaceName + "' registered at '" + workspaceBaseURI + "'");
                        }
                    }
                }
            }
        }
    }
    
    private void _initFileWorkspaces(File workspace, String workspaceName, Collection<String> excludedWorkspace)
    {
        if (workspace.exists() && workspace.isDirectory() && new File(workspace, __WORKSPACE_FILENAME).exists() && new File(workspace, "sitemap.xmap").exists() && !excludedWorkspace.contains(workspaceName))
        {
            if (_workspaceNames.contains(workspaceName))
            {
                String errorMessage = "The workspace named " + workspace.getName() + " already exists";
                _logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            
            _workspaceNames.add(workspaceName);
            _locations.put(workspaceName, workspace);
            
            if (_logger.isInfoEnabled())
            {
                _logger.info("Workspace '" + workspaceName + "' registered at 'context://workspaces/" + workspaceName + "'");
            }
        }   
    }
}
