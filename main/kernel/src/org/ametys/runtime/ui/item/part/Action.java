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
package org.ametys.runtime.ui.item.part;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class handle all the action aspect of a widget
 */
public class Action
{
    /** The plugin */
    protected String _plugin;
    /** the script classname */
    protected String _classname;
    /** the list of script to imports */
    protected Set<String> _imports;
    /** the list of parameters */
    protected Map<String, String> _parameters;
    
    /**
     * Create an action in a script by its classname, the files needed and the parameters of the methode 'act'
     * @param plugin the name of the plugin. Can be null.
     * @param scriptClassname the name of the script class. Can be null. See getScriptImports.
     * @param imports The script files to import. Can be null.
     * @param parameters The parameters given to the method act. &lt;parameter name, parameter value&gt;
     */
    protected Action(String plugin, String scriptClassname, Set<String> imports, Map<String, String> parameters)
    {
        _plugin = plugin;
        _classname = scriptClassname;
        _imports = imports;
        _parameters = parameters;
    }
    
    /**
     * Create an action in a script by its classname, the files needed and the parameters of the methode 'act'
     * @param plugin the name of the plugin. Can be null.
     * @param scriptClassname the name of the script class. See getScriptImports.
     * @param imports The script files to import. Can be null.
     * @param parameters The parameters given to the method act. &lt;parameter name, parameter value&gt;. Can be null.
     * @return The created action
     */
    public static Action createClassAction(String plugin, String scriptClassname, Set<String> imports, Map<String, String> parameters)
    {
        return new Action(plugin, scriptClassname, imports, parameters);
    }
    
    /**
     * Create an action that launch a simply function, by adding correct imports and launching a builtin class
     * @param plugin the name of the plugin. Can be null.
     * @param functionName The name of the function to launch
     * @param imports The current needed imports. Can be null.
     * @param parameters The current parameters. Can be null.
     * @return The action
     */
    public static Action createFunctionAction(String plugin, String functionName, Set<String> imports, Map<String, String> parameters)
    {
        // Ajoute l'import qui va bien
        Set<String> modifiedImports = imports;
        if (modifiedImports == null)
        {
            modifiedImports = new HashSet<String>();
        }
        modifiedImports.add("/kernel/resources/js/Runtime_InteractionActionLibrary.js");
        
        // Ajoute le paramètre qui va bien
        Map<String, String> modifiedParameters = parameters;
        if (modifiedParameters == null)
        {
            modifiedParameters = new java.util.HashMap<String, String>();
        }
        modifiedParameters.put("FunctionName", functionName);
        
        return new Action(plugin, "Runtime_InteractionActionLibrary_FunctionToClass", modifiedImports, modifiedParameters);
    }
    
    /**
     * Create an action that simply redirects inside the current workspace, by adding correct imports and launching a builtin class
     * @param plugin the name of the plugin. Can be null.
     * @param url The url.
     * @return The action
     */
    public static Action createLinkAction(String plugin, String url)
    {
        // Ajoute l'import qui va bien
        Set<String> imports = new HashSet<String>();
        imports.add("/kernel/resources/js/Runtime_InteractionActionLibrary.js");
        
        // Ajoute le paramètre qui va bien
        Map<String, String> parameters = new java.util.HashMap<String, String>();
        parameters.put("Link", url);
        
        return new Action(plugin, "Runtime_InteractionActionLibrary_Link", imports, parameters);
    }
    
    /**
     * Get the plugin
     * @return the plugin. Can be null.
     */
    public String getPlugin()
    {
        return _plugin;
    }
    
    /**
     * Set the plugin of the interaction
     * @param plugin The plugins of the interaction
     */
    public void setPlugin(String plugin)
    {
        _plugin = plugin;
    }
    
    /**
     * Get the parameters.
     * @return The parameters key associated to parameters values. Can be null or empty.
     */
    public Map<String, String> getParameters()
    {
        return _parameters;
    }
    
    /**
     * Get the list of needed imports for the class to execute properly.
     * @return The list of path files to imports
     */
    public Set<String> getScriptImports()
    {
        return _imports;
    }
    
    /**
     * Get the script classname. This class will have at least the static method 'act' with parameters depending on context.
     * @return The classname. Cannot be null or empty.
     */
    public String getScriptClassname()
    {
        return _classname;
    }
}
