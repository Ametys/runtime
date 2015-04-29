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
package org.ametys.runtime.ui;

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
     * This method return the script that will be used on client side.
     * This class will be parametrized by initial and current parameters.
     * @param contextParameters Contextuals parameters transmitted by the environment.
     * @return The script or null.
     */
    public Script getScript(Map<String, Object> contextParameters);
    
    /**
     * This method return the right that will be needed on client side.
     * This class will be parametrized by initial and current parameters.
     * @param contextParameters Contextuals parameters transmitted by the environment.
     * @return The rights in a Map of (rightId, context). Can be empty.
     */
    public Map<String, String> getRights(Map<String, Object> contextParameters);
    
    /**
     * This method returns the parameters initially given to the control script class.
     * Initial parameters must be sufficient to allow the script to render the control without waiting for a refresh by the current parameters.
     * @param contextParameters Contextuals parameters transmitted by the environment.
     * @return a map of parameters. Key represents ids of the parameters and values represents its values. Can not be null.
     */
    public Map<String, Object> getParameters(Map<String, Object> contextParameters);

    /**
     * Get the plugin name where the control was declared
     * @return The plugin name. Can not be null.
     */
    public String getPluginName();

    /**
     * This class represents a script
     */
    public class Script
    {
        /** The script class name of the script */
        protected String _classname;
        /** The script files of the script (url is relative to webapp context) */
        protected List<String> _scriptFiles;
        /** The css files of the script (url is relative to webapp context) */
        protected List<String> _cssFiles;

        /**
         * Creates a script
         * @param classname The script classname. Can not be null nor empty.
         * @param scriptFiles The list of files needed to execute the classname. Must not be null.
         * @param cssFiles The list of css files needed to correctly display the script. Must not be null.
         */
        public Script(String classname, List<String> scriptFiles, List<String> cssFiles)
        {
            _classname = classname;
            _scriptFiles = scriptFiles;
            _cssFiles = cssFiles;
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
        public List<String> getScriptFiles()
        {
            return _scriptFiles;
        }
        
        /**
         * The list of css files needed to correctly display the script.
         * @return The list of css files needed to correctly display the script. Must not be null.
         */
        public List<String> getCSSFiles()
        {
            return _cssFiles;
        }
    }
}
