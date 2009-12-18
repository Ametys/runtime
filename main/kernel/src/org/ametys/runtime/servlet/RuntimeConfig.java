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
package org.ametys.runtime.servlet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.Logger;

import org.ametys.runtime.util.LoggerFactory;


/**
 * Java representation of the WEB-INF/param/runtime.xml file.<br>
 * Contains all runtime configuration values.
 */
public final class RuntimeConfig
{
    private static final Logger __LOGGER = LoggerFactory.getLoggerFor(RuntimeConfig.class);
    
    // shared instance
    private static RuntimeConfig __config;
    
    private String _defaultWorkspace;
    private String _initClass;
    private Collection<String> _pluginsLocations = new ArrayList<String>();
    private Collection<String> _excludedFeatures = new ArrayList<String>();
    private Collection<String> _excludedWorkspaces = new ArrayList<String>();
    private Map<String, String> _extensionsPoints = new HashMap<String, String>();

    private String _configRedirectURL;

    private Collection<String> _configAllowedURLs;
    
    private String _version;
    private Date _buildDate;
    
    private RuntimeConfig()
    {
        // empty constructor
    }
    
    /**
     * Returns the shared instance of the <code>RuntimeConfig</code>
     * @return the shared instance of the <code>RuntimeConfig</code>
     */
    public static RuntimeConfig getInstance()
    {
        if (__config == null)
        {
            throw new IllegalStateException("RuntimeConfig has not been initialized.");
        }
        
        return __config;
    }
    
    /**
     * Configures the Runtime kernel.<br>
     * This method must be called <i>before</i> getting the RuntimConfig instance.<br><br>
     * <b>Warning : the implementation allows this method to be called twice or more. This is only to allow the Runtime to be re-started dynamically.<br>
     * Be aware that this can cause the application to become unstable.</b>
     * @param conf the Configuration of the Runtime kernel (ie the contents of the WEB-INF/param/runtime.xml file)
     */
    public static synchronized void configure(Configuration conf)
    {
        __config = new RuntimeConfig();
        
        __config._initClass = conf.getChild("initClass").getValue(null);
        
        _configureWorkspaces(conf.getChild("workspaces"));
        _configurePlugins(conf.getChild("plugins"));
        _configureExtensions(conf.getChild("extensions"));
        _configureConfig(conf.getChild("incompleteConfig"));
        _configureApplication(conf.getChild("application"));
    }
    
    private static void _configureWorkspaces(Configuration config)
    {
        __config._defaultWorkspace = config.getAttribute("default", null);
        
        for (Configuration excluded : config.getChild("exclude").getChildren("workspace"))
        {
            String workspace = excluded.getValue(null);
            
            if (workspace != null)
            {
                __config._excludedWorkspaces.add(workspace);
            }
        }
    }
    
    private static void _configurePlugins(Configuration config)
    {
        for (Configuration excluded : config.getChild("exclude").getChildren("feature"))
        {
            String plugin = excluded.getValue(null);
            
            if (plugin != null)
            {
                __config._excludedFeatures.add(plugin);
            }
        }
        
        for (Configuration locationConf : config.getChild("locations").getChildren("location"))
        {
            String location = locationConf.getValue(null);
            
            if (location != null)
            {
                __config._pluginsLocations.add(location);
            }
        }
        
        // On ajoute aux emplacements de plugins le répertoire "plugins"
        if (!__config._pluginsLocations.contains("plugins") && !__config._pluginsLocations.contains("plugins/"))
        {
            __config._pluginsLocations.add("plugins/");
        }
        
    }
    
    private static void _configureExtensions(Configuration config)
    {
        for (Configuration extension : config.getChildren())
        {
            String point = extension.getName();
            String id = extension.getValue(null);
            
            if (id != null)
            {
                __config._extensionsPoints.put(point, id);
            }
        }
        
    }
    
    private static void _configureConfig(Configuration config)
    {
        __config._configRedirectURL = config.getChild("redirectURL").getValue("");
        __config._configAllowedURLs = new ArrayList<String>();
        
        for (Configuration allowedURLConf : config.getChild("allowedURLs").getChildren("allowedURL"))
        {
            String url = allowedURLConf.getValue(null);
            
            if (url != null)
            {
                __config._configAllowedURLs.add(url);
            }
        }
    }
    
    private static void _configureApplication(Configuration config)
    {
        __config._version = config.getChild("version").getValue("");
        
        String strDate = config.getChild("date").getValue(null);
        
        if (strDate != null && !"".equals(strDate))
        {
            try
            {
                __config._buildDate = new SimpleDateFormat("yyyyMMdd'T'HHmm z").parse(strDate);
            }
            catch (ParseException e)
            {
                __LOGGER.warn("Unable to parse date '" + strDate + "' with format \"yyyyMMdd'T'HHmm z\". It will be ignored.");
            }
        }
    }
        
    /**
     * Returns the name of the default workspace. Null if none.
     * @return the name of the default workspace
     */
    public String getDefaultWorkspace()
    {
        return _defaultWorkspace;
    }
    
    /**
     * Returns the name of the class to be excuted at the end of the initialization process, if any.<br>
     * May be null.
     * @return Returns the name of the class to be excuted at the end of the initialization process, if any
     */
    public String getInitClassName()
    {
        return _initClass;
    }
    
    /**
     * Returns a Collection containing the locations of the plugins
     * @return a Collection containing the locations of the plugins
     */
    public Collection<String> getPluginsLocations()
    {
        return _pluginsLocations;
    }
    
    /**
     * Returns a Collection containing the names of the excluded (deactivated) plugins
     * @return a Collection containing the names of the excluded (deactivated) plugins
     */
    public Collection<String> getExcludedFeatures()
    {
        return _excludedFeatures;
    }
    
    /**
     * Returns a Collection containing the names of the excluded (deactivated) workspaces
     * @return a Collection containing the names of the excluded (deactivated) workspaces
     */
    public Collection<String> getExcludedWorkspaces()
    {
        return _excludedWorkspaces;
    }
    
    /**
     * Returns a Map&lt;extension point, extension id> containing the choosen extension for each single extension point
     * @return a Map&lt;extension point, extension id> containing the choosen extension for each single extension point
     */
    public Map<String, String> getExtensionsPoints()
    {
        return _extensionsPoints;
    }
    
    /**
     * Returns the redirection URL used when the configuration is missing or incomplete
     * @return the redirection URL used when the configuration is missing or incomplete
     */
    public String getIncompleteConfigRedirectURL()
    {
        return _configRedirectURL;
    }
    
    /**
     * Returns the allowed URLs, even when the configuration is missing or incomplete
     * @return the allowed URLs, even when the configuration is missing or incomplete
     */
    public Collection<String> getIncompleteConfigAllowedURLs()
    {
        return _configAllowedURLs;
    }
    
    /**
     * Returns the application version name
     * @return the application version name
     */
    public String getApplicationVersion()
    {
        return _version;
    }
    
    /**
     * Returns the application build date, if provided. May be null.
     * @return the application build date.
     */
    public Date getApplicationBuildDate()
    {
        return _buildDate;
    }
}
