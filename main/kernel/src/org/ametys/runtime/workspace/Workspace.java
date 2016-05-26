/*
 *  Copyright 2016 Anyware Services
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

import java.io.File;

import org.apache.avalon.framework.configuration.Configuration;

/**
 * Object representation of a workspace.xml file
 */
public class Workspace
{
    private String _name;
    private Configuration _configuration;

    private String _theme;
    private String _themeLocation;
    private String _themeURL;
    private String _baseURI;
    private File _location;
    
    Workspace(String name, String baseURI)
    {
        _name = name;
        _baseURI = baseURI;
    }

    Workspace(String name, File location)
    {
        _name = name;
        _location = location;
    }

    /**
     * Returns this workspace's name.
     * @return this workspace's name.
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * Get the workspace base uri if embeded
     * @return the workspace base uri. Can be null if external
     */
    public String getEmbededLocation()
    {
        return _baseURI;
    }
    
    /**
     * GetThe workspace location if external
     * @return the workspace location. Can be null if embeded
     */
    public File getExternalLocation()
    {
        return _location;
    }
    
    /**
     * Get the name of the theme associated to this workspace 
     * @return The name such as ametys-base
     */
    public String getThemeName()
    {
        return _theme;
    }
    
    /**
     * Get the location of the theme associated to this workspace 
     * @return The location. Such as plugin:core-ui://resources/themes/theme-ametys-base
     */
    public String getThemeLocation()
    {
        return _themeLocation;
    }

    /**
     * Get the location of the theme associated to this workspace 
     * @return The relative location. Such as /plugins/core-ui/resources/themes/theme-ametys-base
     */
    public String getThemeURL()
    {
        return _themeURL;
    }
    
    Configuration getConfiguration()
    {
        return _configuration;
    }
    
    void configure(Configuration configuration)
    {
        _configuration = configuration;
        
        Configuration themeConfiguration = configuration.getChild("theme");
        _theme = themeConfiguration.getAttribute("name", "ametys-base");
        _themeLocation = themeConfiguration.getAttribute("location", "plugin:core-ui://resources/themes/theme-" + _theme);
        _themeURL = themeConfiguration.getAttribute("url", "/plugins/core-ui/resources/themes/theme-" + _theme);
    }
}
