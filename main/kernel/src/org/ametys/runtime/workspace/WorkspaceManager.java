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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.XMLReader;

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
         * Constant for excluded workspaces
         */
        EXCLUDED,
        /**
         * Constant for workspaces having error in their declaration
         */
        MISDECLARED,
        /**
         * Constant for workspaces having error in their workspace.xml file
         */
        MISCONFIGURED
    }
    
    // Map<workspaceName, baseURI>
    private Map<String, Workspace> _workspaces = new HashMap<>(); 
    
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
     * Returns all active workspaces names
     * @return all active workspaces names
     */
    public Set<String> getWorkspaceNames()
    {
        return Collections.unmodifiableSet(_workspaces.keySet());
    }
    
    /**
     * Returns active workspaces declarations.
     * @return active workspaces declarations.
     */
    public Map<String, Workspace> getWorkspaces()
    {
        return Collections.unmodifiableMap(_workspaces);
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
        return _workspaces.get(workspaceName).getEmbededLocation();
    }
    
    /**
     * Returns the workspace filesystem location for the given workspace or null if the workspace is loaded from the classpath.
     * @param workspaceName the workspace name
     * @return the workspace location for the given workspace
     */
    public File getLocation(String workspaceName)
    {
        return _workspaces.get(workspaceName).getExternalLocation();
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
            String workspaceString;
            while ((workspaceString = br.readLine()) != null)
            {
                int i = workspaceString.indexOf(':');
                if (i != -1)
                {
                    String workspaceName = workspaceString.substring(0, i);       
                    String workspaceBaseURI = workspaceString.substring(i + 1);
                    
                    if (!excludedWorkspace.contains(workspaceName))
                    {
                        if (_workspaces.containsKey(workspaceName))
                        {
                            String errorMessage = "The workspace named " + workspaceName + " already exists";
                            _logger.error(errorMessage);
                            throw new IllegalArgumentException(errorMessage);
                        }
                        
                        URL workspaceConfigurationURL = getClass().getResource(workspaceBaseURI + "/" + __WORKSPACE_FILENAME); 
                        if (workspaceConfigurationURL == null || getClass().getResource(workspaceBaseURI + "/" + __WORKSPACE_FILENAME) == null)
                        {
                            if (_logger.isWarnEnabled())
                            {
                                _logger.warn("A workspace '" + workspaceName + "' is declared in a library, but files '" + __WORKSPACE_FILENAME + "' and/or 'sitemap.xmap' are missing at '" + workspaceBaseURI + "'. Workspace will be ignored.");
                            }
                            _inactiveWorkspaces.put(workspaceName, InactivityCause.MISDECLARED);
                            return;
                        }

                        Configuration workspaceConfiguration = null;
                        try (InputStream is2 = workspaceConfigurationURL.openStream())
                        {
                            workspaceConfiguration = _getConfigurationFromStream(workspaceName, is2, workspaceBaseURI);
                        }
                        
                        if (workspaceConfiguration != null)
                        {
                            Workspace workspace = new Workspace(workspaceName, workspaceBaseURI);
                            workspace.configure(workspaceConfiguration);
                            
                            _workspaces.put(workspaceName, workspace);
                            
                            if (_logger.isInfoEnabled())
                            {
                                _logger.info("Workspace '" + workspaceName + "' registered at '" + workspaceBaseURI + "'");
                            }
                        }
                        else
                        {
                            _inactiveWorkspaces.put(workspaceName, InactivityCause.MISCONFIGURED);
                        }
                    }
                    else
                    {
                        _inactiveWorkspaces.put(workspaceName, InactivityCause.EXCLUDED);
                    }
                }
            }
        }
    }
    
    private void _initFileWorkspaces(File workspaceFile, String workspaceName, Collection<String> excludedWorkspace) throws IOException
    {
        File workspaceConfigurationFile = new File(workspaceFile, __WORKSPACE_FILENAME);
        if (excludedWorkspace.contains(workspaceName))
        {
            _inactiveWorkspaces.put(workspaceName, InactivityCause.EXCLUDED);
        }
        else if (workspaceFile.exists() && workspaceFile.isDirectory() && workspaceConfigurationFile.exists() && new File(workspaceFile, "sitemap.xmap").exists())
        {
            if (_workspaces.containsKey(workspaceName))
            {
                String errorMessage = "The workspace named " + workspaceFile.getName() + " already exists";
                _logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            
            Configuration workspaceConfiguration = null;
            try (InputStream is = new FileInputStream(workspaceConfigurationFile))
            {
                workspaceConfiguration = _getConfigurationFromStream(workspaceName, is, workspaceConfigurationFile.getAbsolutePath());
            }
            
            if (workspaceConfiguration != null)
            {
                Workspace workspace = new Workspace(workspaceName, workspaceFile);
                workspace.configure(workspaceConfiguration);
                
                _workspaces.put(workspaceName, workspace);
                
                if (_logger.isInfoEnabled())
                {
                    _logger.info("Workspace '" + workspaceName + "' registered at '" + workspaceConfigurationFile.getAbsolutePath() + "'");
                }
            }
            else
            {
                _inactiveWorkspaces.put(workspaceName, InactivityCause.MISCONFIGURED);
            }
        }  
        else
        {
            if (_logger.isWarnEnabled())
            {
                _logger.warn("Workspace '" + workspaceName + "' registered at '" + workspaceFile.getAbsolutePath() + "' has no '" + __WORKSPACE_FILENAME + "' file or no 'sitemap.xmap' file.");
            }

            _inactiveWorkspaces.put(workspaceName, InactivityCause.MISDECLARED);
        }
    }
    
    private Configuration _getConfigurationFromStream(String workspaceName, InputStream is, String path)
    {
        try
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL schemaURL = getClass().getResource("workspace-4.0.xsd");
            Schema schema = schemaFactory.newSchema(schemaURL);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setSchema(schema);
            XMLReader reader = factory.newSAXParser().getXMLReader();
            DefaultConfigurationBuilder confBuilder = new DefaultConfigurationBuilder(reader);
            
            return confBuilder.build(is, path);
        }
        catch (Exception e)
        {
            _logger.error("Unable to access to workspace '" + workspaceName + "' at " + path, e);
            return null;
        }
    }

}
