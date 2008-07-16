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
package org.ametys.runtime.workspace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.Logger;
import org.xml.sax.SAXException;

import org.ametys.runtime.plugin.PluginsManager.FeatureInformation;
import org.ametys.runtime.util.LoggerFactory;


/**
 * Main entry point for access to workspaces.
 */
public final class WorkspaceManager
{
    // shared instance
    private static WorkspaceManager __manager;
    
    private static final String __WORKSPACE_FILENAME = "workspace.xml";

    // Map<workspaceName, baseURI>
    private Map<String, String> _workspaces = new HashMap<String, String>(); 
    
    // All workspaces' names
    // _workspaceNames is NOT the same as _workspaces.keySet(), which only contains embedded workspaces
    private Set<String> _workspaceNames = new HashSet<String>();
    
    private Logger _logger = LoggerFactory.getLoggerFor(WorkspaceManager.class); 
    
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
     * Initialize the WorkspaceManager.<br>
     * It first looks for META-INF/runtime-workspace files in the classpath, then in the "workspaces" directory of the application.
     * @param excludedWorkspace the excluded workspaces, as given by the RuntimeConfig
     * @param contextPath the servlet context path
     * @param pluginsInformations all relevant information about loaded plugins or null if the application is not correctly configured
     * @throws IOException
     */
    public void init(Collection<String> excludedWorkspace, String contextPath, Map<String, FeatureInformation> pluginsInformations) throws IOException
    {
        _workspaces.clear();
        _workspaceNames.clear();
        
        // On commence par les workspaces dans le classpath
        Enumeration<URL> workspaceResources = getClass().getClassLoader().getResources("META-INF/runtime-workspace");
        while (workspaceResources.hasMoreElements())
        {
            URL workspaceResource = workspaceResources.nextElement();
            _initResourceWorkspace(excludedWorkspace, workspaceResource);
        }

        // Puis les workspaces du répertoire "<context>/workspaces"
        File workspacesDir = new File(contextPath, "workspaces");
        if (workspacesDir.exists() && workspacesDir.isDirectory())
        {
            for (File workspace : new File(contextPath, "workspaces").listFiles())
            {
                if (workspace.exists() && workspace.isDirectory() && new File(workspace, __WORKSPACE_FILENAME).exists() && new File(workspace, "sitemap.xmap").exists() && !excludedWorkspace.contains(workspace.getName()))
                {
                    if (_workspaceNames.contains(workspace.getName()))
                    {
                        String errorMessage = "The workspace named " + workspace.getName() + " already exists";
                        _logger.error(errorMessage);
                        throw new IllegalArgumentException(errorMessage);
                    }
                    
                    String workspaceName = workspace.getName();
                    
                    boolean addWorkspace = true;
                    
                    try
                    {
                        // Si la configuration n'est pas complète, aucun plugin n'est chargé, il est inutile de tester les dépendences
                        if (pluginsInformations != null)
                        {
                            addWorkspace = _checkDependencies(workspaceName, pluginsInformations, contextPath);
                        }
                        
                        if (addWorkspace)
                        {
                            _workspaceNames.add(workspaceName);
                            
                            if (_logger.isInfoEnabled())
                            {
                                _logger.info("Workspace '" + workspaceName + "' registered at 'context://worskpaces/" + workspaceName + "'");
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        _logger.error("Exception while loading workspace " + workspaceName, e);
                    }
                }   
            }
        }
    }
    
    private void _initResourceWorkspace(Collection<String> excludedWorkspace, URL workspaceResource) throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(workspaceResource.openStream(), "UTF-8"));
        
        String workspaceName = br.readLine();            
        String workspaceBaseURI = br.readLine();
        
        if (!excludedWorkspace.contains(workspaceName))
        {
            if (_workspaceNames.contains(workspaceName))
            {
                String errorMessage = "The workspace named " + workspaceName + " already exists";
                _logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            
            if (getClass().getResource(workspaceBaseURI + "/" + __WORKSPACE_FILENAME) != null)
            {
                _workspaceNames.add(workspaceName);
                _workspaces.put(workspaceName, workspaceBaseURI);
            }
            else if (_logger.isWarnEnabled())
            {
                _logger.warn("A workspace '" + workspaceName + "' is declared in a library, but no file '" + __WORKSPACE_FILENAME + "' can be found at '" + workspaceBaseURI + "'. Workspace will be ignored.");
            }
        }
        
        br.close();
        
        if (_logger.isInfoEnabled())
        {
            _logger.info("Workspace '" + workspaceName + "' registered at '" + workspaceBaseURI + "'");
        }
    }
    
    private boolean _checkDependencies(String workspaceName, Map<String, FeatureInformation> pluginsInformations, String contextPath) throws IOException, ConfigurationException, SAXException
    {
        String workspaceBaseURI = _workspaces.get(workspaceName);
        
        InputStream is;
        
        if (workspaceBaseURI == null)
        {
            // workspace dans le filesystem
            is = new FileInputStream(new File(contextPath, "workspaces/" + workspaceName + "/" + __WORKSPACE_FILENAME));
        }
        else
        {
            // workspace dans le classpath
            is = getClass().getResourceAsStream(workspaceBaseURI + "/" + __WORKSPACE_FILENAME);
        }
        
        DefaultConfigurationBuilder confBuilder = new DefaultConfigurationBuilder();
        Configuration conf = confBuilder.build(is);
        is.close();
        
        for (Configuration dependencyConf : conf.getChild("dependencies").getChildren("dependency"))
        {
            String dependency = dependencyConf.getValue(null);
            
            if (dependency != null)
            {
                if (!pluginsInformations.containsKey(dependency))
                {
                    if (_logger.isWarnEnabled())
                    {
                        _logger.warn("The workspace '" + workspaceName + "' depends on feature " + dependency + " which is not loaded. It will be ignored.");
                    }
                    
                    return false;
                }
            }
        }
        
        return true;
    }
}
