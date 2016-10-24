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
package org.ametys.runtime.plugin;

/**
 * Represents an issue while initializing the plugin system.
 */
public class PluginIssue
{
    /**
     * Issue code enumeration.
     */
    public enum PluginIssueCode
    {
        /**
         * A plugin referenced in a jar file has no corresponding plugin.xml
         */
        BUNDLED_PLUGIN_NOT_PRESENT, 
        
        /**
         * The plugin name does not match the regexp.
         */
        PLUGIN_NAME_INVALID, 
        
        /**
         * There's no plugin.xml in the specified directory.
         */
        PLUGIN_NOFILE,
        
        /**
         * A plugin with this name is already declared.
         */
        PLUGIN_NAME_EXIST, 
        
        /**
         * A plugin.xml is not valid against the schema.
         */
        CONFIGURATION_UNREADABLE, 

        /**
         * An extension point is already declared in another plugin.
         */
        EXTENSIONPOINT_ALREADY_EXIST,
        
        /**
         * An extension to the same point is alredy declared with the same id.
         */
        EXTENSION_ALREADY_EXIST, 
        
        /**
         * A component with the same role is alredy declared.
         */
        COMPONENT_ALREADY_EXIST,
        
        /**
         * A class referenced by an extension point, extension or component does not exist.
         */
        CLASSNOTFOUND, 
        
        /**
         * An extension point's class does not implement {@link ExtensionPoint}.
         */
        EXTENSIONPOINT_CLASS_INVALID, 
        
        /**
         * A component role is required to point to a specific component id which doesn't actually exist.
         */
        COMPONENT_NOT_DECLARED, 
        
        /**
         * Circular dependency detected on a feature.
         */
        CIRCULAR_DEPENDENCY, 
        
        /**
         * Unable to load an external configuration file.
         */
        EXTERNAL_CONFIGURATION, 
        
        /**
         * The application Init class does not implement {@link Init}.
         */
        INIT_CLASS_INVALID, 
        
        /**
         * An extension refers to a non-existing point.
         */
        INVALID_POINT
    }

    private PluginIssueCode _code;
    private String _message;
    private String _pluginName;
    private String _featureName;
    private String _location;
    private Exception _cause;
    
    PluginIssue(String pluginName, String featureName, PluginIssueCode code, String location, String message)
    {
        _pluginName = pluginName;
        _featureName = featureName;
        _code = code;
        _location = location;
        _message = message;
    }
    
    PluginIssue(String pluginName, String featureName, PluginIssueCode code, String location, String message, Exception cause)
    {
        _pluginName = pluginName;
        _featureName = featureName;
        _code = code;
        _location = location;
        _message = message;
        _cause = cause;
    }
    
    /**
     * Returns the code associated with this issue.
     * @return the code associated with this issue.
     */
    public PluginIssueCode getCode()
    {
        return _code;
    }
    
    /**
     * Returns the message associated with this issue.
     * @return the message associated with this issue.
     */
    public String getMessage()
    {
        return _message;
    }
    
    /**
     * Returns the plugin name associated with this issue, if any.
     * @return the plugin name associated with this issue, if any.
     */
    public String getPluginName()
    {
        return _pluginName;
    }
    
    /**
     * Returns the feature name associated with this issue, if any.
     * @return the feature name associated with this issue, if any.
     */
    public String getFeatureName()
    {
        return _featureName;
    }
    
    /**
     * Returns the location of this issue, if any.
     * @return the location of this issue, if any.
     */
    public String getLocation()
    {
        return _location;
    }
    
    /**
     * Returns the cause of this issue, if any.
     * @return the cause of this issue, if any.
     */
    public Exception getCause()
    {
        return _cause;
    }
    
    @Override
    public String toString()
    {
        return "[" + _code.toString() + (_location != null ? ", " + _location : "") + "] " + _message + (_cause != null ? (" (" + _cause.getMessage() + ")") : "");
    }
}
