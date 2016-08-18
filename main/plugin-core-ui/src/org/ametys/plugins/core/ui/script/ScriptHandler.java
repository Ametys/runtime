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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.right.RightManager;
import org.ametys.core.ui.Callable;
import org.ametys.core.user.CurrentUserProvider;
import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.parameter.ParameterHelper;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/**
 * Handler to describe and execute server scripts
 */
public class ScriptHandler extends AbstractLogEnabled implements Component, Serviceable
{
    private static final String __RIGHT_EXECUTE_SCRIPTS = "CORE_Rights_ExecuteScript";
    
    private static final String __SCRIPT_INSERT_CLEANUP_MANAGER = "var __cleanup_manager = { _registered:[], register: function (f) { this._registered.push(f) }, cleanup : function () { this._registered.forEach(function (f) {f()} ) } };";
    private static final String __SCRIPT_INSERT_RUN_MAIN = "var __result; try { __result = main(); } finally { __cleanup_manager.cleanup() } __result";

    private ScriptBindingExtensionPoint _scriptBindingEP;
    private RightManager _rightManager;
    private CurrentUserProvider _currentUserProvider;

    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        _scriptBindingEP = (ScriptBindingExtensionPoint) serviceManager.lookup(ScriptBindingExtensionPoint.ROLE);
        _rightManager = (RightManager) serviceManager.lookup(RightManager.ROLE);
        _currentUserProvider = (CurrentUserProvider) serviceManager.lookup(CurrentUserProvider.ROLE);
    }
    
    /**
     * Execute a script in the js admin console.
     * @param script The script as a String.
     * @return A map of information on the script execution.
     * @throws ScriptException If an error occurs
     */
    @Callable
    public Map<String, Object> executeScript(String script) throws ScriptException
    {
        Map<String, Object> results = new HashMap<>();
        
        if (_rightManager.hasRight(_currentUserProvider.getUser(), __RIGHT_EXECUTE_SCRIPTS, "/application") != RightManager.RightResult.RIGHT_ALLOW)
        {
            // FIXME Currently unable to assign rights to a user in the _admin workspace
            // throw new RightsException("Insufficient rights to execute a script");
        }
        
        results.put("start", ParameterHelper.valueToString(new Date()));
        
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("javascript");
        
        // Redirect output to a String
        StringWriter output = new StringWriter();
        PrintWriter pw = new PrintWriter(output);
        ScriptContext context = engine.getContext();
        context.setWriter(pw);
        
        StringWriter errorOutput = new StringWriter();
        PrintWriter errorPw = new PrintWriter(errorOutput);
        context.setErrorWriter(errorPw);
        
        context.setAttribute(ScriptEngine.FILENAME, "generated script", ScriptContext.GLOBAL_SCOPE);
        
        Map<String, Object> variables = new HashMap<>();
        
        try
        {
            List<String> scriptText = new ArrayList<>();
            scriptText.add(script);
            scriptText.add(__SCRIPT_INSERT_CLEANUP_MANAGER);
            setScriptBindings(variables, scriptText);
            scriptText.add(__SCRIPT_INSERT_RUN_MAIN);
            
            // Create bindings
            SimpleBindings sb = new SimpleBindings();
            for (Entry<String, Object> entry : variables.entrySet())
            {
                sb.put(entry.getKey(), entry.getValue());
            }
            
            // Execute script
            Object result = engine.eval(StringUtils.join(scriptText, "\n"), sb);
            
            results.put("end", ParameterHelper.valueToString(new Date()));
            
            // Script output and error.
            results.put("output", output.toString());
            results.put("error", errorOutput.toString());
            
            // Script result
            if (result != null)
            {
                results.put("result", processScriptResult(result));
            }
            
            return results;
        }
        finally
        {
            for (String extensionId : _scriptBindingEP.getExtensionsIds())
            {
                ScriptBinding scriptBinding = _scriptBindingEP.getExtension(extensionId);
                
                scriptBinding.cleanVariables(variables);
            }
        }
    }

    private void setScriptBindings(Map<String, Object> variables, List<String> scriptText)
    {
        for (String extensionId : _scriptBindingEP.getExtensionsIds())
        {
            ScriptBinding scriptBinding = _scriptBindingEP.getExtension(extensionId);
            Map<String, Object> scriptBindingVariables = scriptBinding.getVariables();
            if (scriptBindingVariables != null)
            {
                variables.putAll(scriptBindingVariables);
            }
            
            String scriptBindingFunctions = scriptBinding.getFunctions();
            if (scriptBindingFunctions != null)
            {
                scriptText.add(scriptBindingFunctions);
            }
        }
    }
    
    private Object processScriptResult(Object result) throws ScriptException
    {
        for (String extensionId : _scriptBindingEP.getExtensionsIds())
        {
            ScriptBinding scriptBinding = _scriptBindingEP.getExtension(extensionId);
            
            Object processedResult = scriptBinding.processScriptResult(result);
            if (processedResult != null)
            {
                return processedResult;
            }
        }
        
        if (result instanceof Collection)
        {
            // Collection
            List<Object> elements = new ArrayList<>();
            for (Object obj : (Collection<?>) result)
            {
                elements.add(processScriptResult(obj));
            }
            return elements;
        }
        else if (result instanceof Map)
        {
            // Map
            Map<Object, Object> elements = new HashMap<>();
            for (Object key : ((Map) result).keySet())
            {
                Object value = ((Map) result).get(key);
                elements.put(processScriptResult(key), processScriptResult(value));
            }
            return elements;
        }
        else
        {
            return result.toString();
        }
    }
    
    /**
     * Get the list of variables and functions descriptions currently registered for the Scripts.
     * @return The list of variables and functions, as describes by the script bindings available.
     */
    @Callable
    public Map<String, Object> getScriptBindingDescription()
    {
        Map<String, I18nizableText> variablesDesc = new HashMap<>();
        Map<String, I18nizableText> functionsDesc = new HashMap<>();
        for (String extensionId : _scriptBindingEP.getExtensionsIds())
        {
            ScriptBinding scriptBinding = _scriptBindingEP.getExtension(extensionId);
            Map<String, I18nizableText> scriptBindingVariablesDesc = scriptBinding.getVariablesDescriptions();
            if (scriptBindingVariablesDesc != null)
            {
                // Warn if any variable was already declared by another ScriptBinding.
                HashSet<String> intersection = new HashSet<>(variablesDesc.keySet());
                intersection.retainAll(scriptBindingVariablesDesc.keySet());
                if (intersection.size() > 0)
                {
                    for (String variable : intersection)
                    {
                        getLogger().warn("Multiple ScriptBinding use the same variable name : '" + variable + "'. Only one of these variables will be available.");
                    }
                }
                
                variablesDesc.putAll(scriptBindingVariablesDesc);
            }
            
            Map<String, I18nizableText> scriptBindingFunctionsDesc = scriptBinding.getFunctionsDescriptions();
            if (scriptBindingFunctionsDesc != null)
            {
                // Warn if any function was already declared by another ScriptBinding.
                HashSet<String> intersection = new HashSet<>(functionsDesc.keySet());
                intersection.retainAll(scriptBindingFunctionsDesc.keySet());
                if (intersection.size() > 0)
                {
                    for (String function : intersection)
                    {
                        getLogger().warn("Multiple ScriptBinding use the same function name : '" + function + "'. Your scripts may not be able to run properly.");
                    }
                }
                
                functionsDesc.putAll(scriptBindingFunctionsDesc);
            }
        }
        
        HashMap<String, Object> result = new HashMap<>();
        if (variablesDesc.size() > 0)
        {
            result.put("variables", variablesDesc);
        }
        if (functionsDesc.size() > 0)
        {
            result.put("functions", functionsDesc);
        }
        return result;
    }
}
