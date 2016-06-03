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
package org.ametys.core.ui;

import java.util.List;
import java.util.Map;

/**
 * Object binding of a client side element, ie something that is loaded and executed by the browser.<br>
 * Such elements may be UI controls (buttons, menu, tools, ...) but also only JS or CSS files.<br><br>
 * 
 * This interface only covers files to be loaded, but its implementations are also meant to hold associated business-logic, if any.<br>
 * To implement such logic, implementing classes should write any method, annotated with {@link Callable}, 
 * that will be directly called by the kernel upon execution of the JavaScript method <code>serverCall('methodeName', params)</code>.<br><br>
 * 
 * All <code>Map&lt;String, Object&gt;</code> instances found in this class and its implementations 
 * are directly converted from and to JSON to interact with browser-site JavaScript.
 */
public interface ClientSideElement
{
    /**
     * Get the id of the element.
     * @return the id. Can not be null.
     */
    public String getId();
    
    /**
     * This method return the scripts that will be used on client side.
     * This class will be parametrized by initial and current parameters.
     * @param contextParameters Contextuals parameters transmitted by the environment.
     * @return The list of scripts or an empty list.
     */
    public List<Script> getScripts(Map<String, Object> contextParameters);
    
    /**
     * This method return the scripts that will be used on client side.
     * This class will be parametrized by initial and current parameters.
     * @param ignoreRights True to ignore the rights verification.
     * @param contextParameters Contextuals parameters transmitted by the environment.
     * @return The list of scripts or an empty list.
     */
    public List<Script> getScripts(boolean ignoreRights, Map<String, Object> contextParameters);
    
    /**
     * This method return the right that will be needed on client side.
     * This class will be parametrized by initial and current parameters.
     * @param contextParameters Contextuals parameters transmitted by the environment.
     * @return The rights in a Map of (rightId, context). Can be empty.
     */
    public Map<String, String> getRights(Map<String, Object> contextParameters);
    

    /**
     * Get the plugin name where the control was declared
     * @return The plugin name. Can not be null.
     */
    public String getPluginName();
    
    /**
     * This method returns the list of dependencies, sorted by extension point. 
     * @return a map of dependencies ids by extension point.
     */
    public Map<String, List<String>> getDependencies();

    /**
     * This class represents a script file
     */
    public class ScriptFile
    {
        private String _debugMode;
        private String _rtlMode;
        private boolean _langMode;
        private String _path;
        private Map<String, String> _langPaths;
        private String _defaultLang;
        
        /**
         * Default constructor. Create a new script only defined by its path.
         * @param path The script path
         */
        public ScriptFile(String path)
        {
            _path = path;
            _langMode = false;
        }

        /**
         * Create a new script file that is not language aware.
         * @param debug The debug mode. Can be null.
         * @param rtl The rtl mode. Can be null.
         * @param path The file path.
         */
        public ScriptFile(String debug, String rtl, String path)
        {
            _debugMode = debug;
            _rtlMode = rtl;
            _path = path;
            _langMode = false;
        }
        
        /**
         * Create a new script file with a language specific configuration.
         * @param debug The debug mode. Can be null.
         * @param langPaths The list of languages with their paths.
         * @param defaultLang The default language code. Can be null.
         */
        public ScriptFile(String debug, Map<String, String> langPaths, String defaultLang)
        {
            _debugMode = debug;
            _langPaths = langPaths;
            _defaultLang = defaultLang;
            _langMode = true;
        }
        
        /**
         * Get the debug mode
         * @return the debug mode
         */
        public String getDebugMode()
        {
            return _debugMode;
        }
        
        /**
         * Get the rtl mode
         * @return the the rtl mode
         */
        public String getRtlMode()
        {
            return _rtlMode;
        }
        
        /**
         * Retrieve the language mode
         * @return True if the script file is language specific
         */
        public boolean isLangSpecific()
        {
            return _langMode;
        }
        
        /**
         * Get the file path
         * @return the path
         */
        public String getPath()
        {
            return _path;
        }
        
        /**
         * Get the path mapping by language
         * @return the paths by languages
         */
        public Map<String, String> getLangPaths()
        {
            return _langPaths;
        }
        
        /**
         * Get the default language
         * @return the the default language
         */
        public String getDefaultLang()
        {
            return _defaultLang;
        }
        
        @Override
        public String toString()
        {
            return _path;
        }
    }
    
    /**
     * This class represents a script
     */
    public class Script
    {
        /** The id associated with this script */
        protected String _id;
        /** The script class name of the script */
        protected String _classname;
        /** The script files of the script (url is relative to webapp context) */
        protected List<ScriptFile> _scriptFiles;
        /** The css files of the script (url is relative to webapp context) */
        protected List<ScriptFile> _cssFiles;
        /** The parameters objects of the script */
        protected Map<String, Object> _parameters;
        /** The server element id for this script */
        protected String _serverId;
        
        /**
         * Creates a script
         * @param id The script id
         * @param classname The script classname. Can not be null nor empty.
         * @param scriptFiles The list of files needed to execute the classname. Must not be null.
         * @param cssFiles The list of css files needed to correctly display the script. Must not be null.
         * @param parameters The parameters associated with this Script.
         */
        public Script(String id, String classname, List<ScriptFile> scriptFiles, List<ScriptFile> cssFiles, Map<String, Object> parameters)
        {
            this(id, id, classname, scriptFiles, cssFiles, parameters);
        }
        
        /**
         * Creates a script
         * @param id The script id
         * @param serverId The script server id
         * @param classname The script classname. Can not be null nor empty.
         * @param scriptFiles The list of files needed to execute the classname. Must not be null.
         * @param cssFiles The list of css files needed to correctly display the script. Must not be null.
         * @param parameters The parameters associated with this Script.
         */
        public Script(String id, String serverId, String classname, List<ScriptFile> scriptFiles, List<ScriptFile> cssFiles, Map<String, Object> parameters)
        {
            _id = id;
            _serverId = serverId;
            _classname = classname;
            _scriptFiles = scriptFiles;
            _cssFiles = cssFiles;
            _parameters = parameters;
        }
        
        /**
         * The script id.
         * @return The script id. Can not be null nor empty.
         */
        public String getId()
        {
            return _id;
        }
        
        /**
         * The id server-side associated with this script
         * @return The server id;
         */
        public String getServerId()
        {
            return _serverId;
        }
        
        /**
         * The script classname.
         * @return The script classname. Can not be null nor empty.
         */
        public String getScriptClassname()
        {
            return _classname;
        }
        
        /**
         * The list of files needed to execute the classname.
         * @return The list of files needed to execute the classname. Must not be null.
         */
        public List<ScriptFile> getScriptFiles()
        {
            return _scriptFiles;
        }
        
        /**
         * The list of css files needed to correctly display the script.
         * @return The list of css files needed to correctly display the script. Must not be null.
         */
        public List<ScriptFile> getCSSFiles()
        {
            return _cssFiles;
        }
        
        /**
         * This method returns the parameters initially given to the control script class.
         * Initial parameters must be sufficient to allow the script to render the control without waiting for a refresh by the current parameters.
         * @return a map of parameters. Key represents ids of the parameters and values represents its values. Can not be null.
         */
        public Map<String, Object> getParameters()
        {
            return _parameters;
        }
    }
}
