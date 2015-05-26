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
package org.ametys.runtime.servlet;

import java.io.File;
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
    // shared instance
    private static RuntimeConfig __config;

    private String _defaultWorkspace;
    private String _initClass;
    private final Collection<String> _pluginsLocations = new ArrayList<>();
    private final Collection<String> _excludedFeatures = new ArrayList<>();
    private final Collection<String> _excludedWorkspaces = new ArrayList<>();
    private final Map<String, String> _extensionsPoints = new HashMap<>();

    private Logger _logger = LoggerFactory.getLoggerFor(RuntimeConfig.class);
    
    private String _contextPath;

    private String _configRedirectURL;

    private Collection<String> _configAllowedURLs;

    private String _version;
    private Date _buildDate;
    
    /* External location of the kernel, if any */
    private File _externalKernel;
    
    /* Locations of external plugins */
    private Map<String, File> _externalPlugins = new HashMap<>();
    
    /* Locations of external workspaces */
    private Map<String, File> _externalWorkspaces = new HashMap<>();

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
     * @param runtimeConf the Configuration of the Runtime kernel (ie the contents of the WEB-INF/param/runtime.xml file)
     * @param externalConf the Configuration of external locations (ie the contents of the WEB-INF/param/external.xml file)
     * @param contextPath the application context path
     */
    public static synchronized void configure(Configuration runtimeConf, Configuration externalConf, String contextPath)
    {
        __config = new RuntimeConfig();
        
        __config._contextPath = contextPath;

        __config._initClass = runtimeConf.getChild("initClass").getValue(null);

        __config._configureWorkspaces(runtimeConf.getChild("workspaces"));
        __config._configurePlugins(runtimeConf.getChild("plugins"));
        __config._configureExtensions(runtimeConf.getChild("extensions"));
        __config._configureConfig(runtimeConf.getChild("incompleteConfig", false));
        __config._configureApplication(runtimeConf.getChild("application"));
        
        if (externalConf != null)
        {
            __config._configureExternal(externalConf);
        }
    }

    private void _configureWorkspaces(Configuration config)
    {
        _defaultWorkspace = config.getAttribute("default", null);

        for (Configuration excluded : config.getChild("exclude").getChildren("workspace"))
        {
            String workspace = excluded.getValue(null);

            if (workspace != null)
            {
                _excludedWorkspaces.add(workspace);
            }
        }
    }

    private void _configurePlugins(Configuration config)
    {
        for (Configuration excluded : config.getChild("exclude").getChildren("feature"))
        {
            String plugin = excluded.getValue(null);

            if (plugin != null)
            {
                _excludedFeatures.add(plugin);
            }
        }

        for (Configuration locationConf : config.getChild("locations").getChildren("location"))
        {
            String location = locationConf.getValue(null);

            if (location != null)
            {
                _pluginsLocations.add(location);
            }
        }

        // On ajoute aux emplacements de plugins le répertoire "plugins"
        if (!_pluginsLocations.contains("plugins") && !_pluginsLocations.contains("plugins/"))
        {
            _pluginsLocations.add("plugins/");
        }

    }

    private void _configureExtensions(Configuration config)
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

    private void _configureConfig(Configuration config)
    {
        _configAllowedURLs = new ArrayList<>();

        if (config == null)
        {
            _configRedirectURL = "cocoon://_admin/public/load-config.html?uri=core/administrator/config/edit.html";

            _configAllowedURLs.add("_admin/public");
            _configAllowedURLs.add("_admin/resources");
            _configAllowedURLs.add("_admin/_plugins/core/administrator/config");
            _configAllowedURLs.add("_admin/plugins/core/administrator/config");
            _configAllowedURLs.add("_admin/plugins/core/jsfilelist");
            _configAllowedURLs.add("_admin/plugins/core/cssfilelist");

            return;
        }

        _configRedirectURL = config.getChild("redirectURL").getValue("");

        for (Configuration allowedURLConf : config.getChild("allowedURLs").getChildren("allowedURL"))
        {
            String url = allowedURLConf.getValue(null);

            if (url != null)
            {
                _configAllowedURLs.add(url);
            }
        }
    }

    private void _configureApplication(Configuration config)
    {
        String version = config.getChild("version").getValue("");
        if (!"@VERSION@".equals(version) && !"VERSION".equals(version))
        {
            _version = version;
        }

        String strDate = config.getChild("date").getValue(null);

        if (strDate != null && !"".equals(strDate) && !"@DATE@".equals(strDate) && !"DATE".equals(strDate))
        {
            try
            {
                _buildDate = new SimpleDateFormat("yyyyMMdd'T'HHmm z").parse(strDate);
            }
            catch (ParseException e)
            {
                _logger.warn("Unable to parse date '" + strDate + "' with format \"yyyyMMdd'T'HHmm z\". It will be ignored.");
            }
        }
    }
    
    private void _configureExternal(Configuration config)
    {
        String externalKernel = config.getChild("kernel").getValue(null);
        _externalKernel = _getFile(externalKernel);
        
        for (Configuration pluginConf : config.getChild("plugins").getChildren("plugin"))
        {
            String name = pluginConf.getAttribute("name", null);
            String location = pluginConf.getValue(null);
            
            if (name != null && location != null)
            {
                _externalPlugins.put(name, _getFile(location));
            }
        }
        
        for (Configuration workspaceConf : config.getChild("workspaces").getChildren("workspace"))
        {
            String name = workspaceConf.getAttribute("name", null);
            String location = workspaceConf.getValue(null);
            
            if (location != null)
            {
                _externalWorkspaces.put(name, _getFile(location));
            }
        }
    }
    
    /*
     * Returns the correspoding file, either absolute or relative to the context path
     */
    private File _getFile(String path)
    {
        File file = path == null ? null : new File(path);
        File result = file == null ? null : file.isAbsolute() ? file : new File(_contextPath, path);
        return result;
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
     * Returns the declared external plugins (ie. not located in the webapp context).
     * @return the declared external plugins
     */
    public Map<String, File> getExternalPlugins()
    {
        return _externalPlugins;
    }

    /**
     * Returns the declared external workspaces (ie. not located in the webapp context).
     * @return the declared external workspaces
     */
    public Map<String, File> getExternalWorkspaces()
    {
        return _externalWorkspaces;
    }

    /**
     * Returns the absolute external location of the kernel, if any.<br>
     * Returns null if the kernel is not externalized.
     * @return the absolute external location of the kernel, if any.
     */
    public File getExternalKernel()
    {
        return _externalKernel;
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
     * Returns a Map&lt;extension point, extension id&gt; containing the choosen extension for each single extension point
     * @return a Map&lt;extension point, extension id&gt; containing the choosen extension for each single extension point
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
