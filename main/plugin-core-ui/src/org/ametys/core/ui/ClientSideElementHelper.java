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
package org.ametys.core.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.ametys.core.ui.ClientSideElement.Script;
import org.ametys.core.ui.ClientSideElement.ScriptFile;

/**
 * Helper for manipulating ClientSideElement
 */
public final class ClientSideElementHelper extends AbstractLogEnabled
{
    /**
     * Clone a ClientSideElement Script
     * @param script The script
     * @return The new script
     */
    public static Script cloneScript(Script script)
    {
        return cloneScript(script, script.getId());
    }
    
    /**
     * Clone a ClientSideElement Script
     * @param script The script
     * @param id The new script id
     * @return The new script
     */
    public static Script cloneScript(Script script, String id)
    {
        List<ScriptFile> scriptFiles = cloneScriptFile(script.getScriptFiles());
        List<ScriptFile> cssFiles = cloneScriptFile(script.getCSSFiles());
        Map<String, Object> parameters = new HashMap<>(script.getParameters());
        
        Script clone = new Script(id, script.getScriptClassname(), scriptFiles, cssFiles, parameters);
        return clone;
    }
    
    private static List<ScriptFile> cloneScriptFile(List<ScriptFile> scriptFiles)
    {
        List<ScriptFile> clonedList = new ArrayList<>();
        
        for (ScriptFile scriptFile : scriptFiles)
        {
            if (scriptFile.isLangSpecific())
            {
                clonedList.add(new ScriptFile(scriptFile.getDebugMode(), scriptFile.getLangPaths(), scriptFile.getDefaultLang()));
            }
            else
            {
                clonedList.add(new ScriptFile(scriptFile.getDebugMode(), scriptFile.getRtlMode(), scriptFile.getPath()));
            }
        }
        
        return clonedList;
    }
}
