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
package org.ametys.plugins.core.ui.script;

import java.util.Map;

import javax.script.ScriptException;

import org.ametys.runtime.i18n.I18nizableText;

/**
 * This interface describes a console data, variables and functions. 
 * Each implementation of this interface will enhance the console, by providing additional variables and functions that can be used in scripts.
 */
public interface ScriptBinding
{
    /**
     * Returns the list of variables this ScriptBinding provides, mapped by variable name.
     * @return The list of variables, or null if no variable is provided.
     */
    public Map<String, Object> getVariables();
    
    /**
     * Returns the list of variables descriptions, mapped by variable name.
     * This list does not have to match the getVariables return value, but the description is used to inform the user of the existence and usability of each variable.
     * @return The list of variables descriptions, or null if no description is provided.
     */
    public Map<String, I18nizableText> getVariablesDescriptions();

    /**
     * Allows clean up of variables created during the getVariables call.
     * @param variables The map of variables.
     */
    public void cleanVariables(Map<String, Object> variables);
    
    /**
     * Returns the JavaScript functions to inject at the start of the script, in the form of a single String prepended to the script.
     * @return The functions text, or null if no function is provided.
     */
    public String getFunctions();
    
    /**
     * Returns the list of functions descriptions, mapped by function name.
     * This list does not have to match the functions returned by getFunctions, but the description is used to inform the user of the existence and usability of each function.
     * @return The list of functions descriptions, or null if no description is provided.
     */
    public Map<String, I18nizableText> getFunctionsDescriptions();
    
    /**
     * Process the script result if there are any specificities for this console data.
     * @param result The result
     * @return The result processed, or null if this console data does not have any processing to do. 
     * @throws ScriptException If a processing error occurs.
     */
    public Object processScriptResult(Object result) throws ScriptException;
}
