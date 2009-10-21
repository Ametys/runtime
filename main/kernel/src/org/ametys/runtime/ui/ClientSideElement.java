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
package org.ametys.runtime.ui;

import java.util.List;
import java.util.Map;

import org.ametys.runtime.util.I18nizableText;

/**
 * A client side element
 */
public interface ClientSideElement
{
    /**
     * This method return the script that will be used on client side.
     * This class will be parametrized by initial and current parameters.
     * @return The script. Can not be null.
     */
    public Script getScript();
    
    /**
     * This method return the right that will be needed on client side.
     * This class will be parametrized by initial and current parameters.
     * @return The right. Can be null.
     */
    public String getRight();
    
    /**
     * This method returns the parameters initially given to the control script class.
     * Initial parameters must be sufficient to allow the script to render the control without waiting for a refresh by the current parameters.
     * @param contextParameters Contextuals parameters transmitted by the environment.
     * @return a map of parameters. Key represents ids of the parameters and values represents its values. Can not be null.
     */
    public Map<String, I18nizableText> getParameters(Map<String, Object> contextParameters);

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
