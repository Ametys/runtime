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

import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * This implementation creates an element for adding a new task
 */
public class AddTaskClientSideElement extends StaticClientSideElement
{
    @Override
    protected String _configureClass(Configuration configuration) throws ConfigurationException
    {
        return "Ametys.plugins.coreui.schedule.AddTaskButtonController";
    }
    
    @Override
    protected Map<String, Object> configureInitialParameters(Configuration configuration) throws ConfigurationException
    {
        Map<String, Object> initialParameters = super.configureInitialParameters(configuration);
        initialParameters.put("action", "Ametys.plugins.coreui.schedule.AddTaskButtonController.act");
        return initialParameters;
    }
    
    @Override
    protected Script _configureScript(Configuration configuration) throws ConfigurationException
    {
        List<ScriptFile> scriptsImports = _configureImports(configuration.getChild("scripts"));
        scriptsImports.add(new ScriptFile("all", "all", "/plugins/core/resources/js/Ametys/plugins/core/schedule/Scheduler.js"));
        scriptsImports.add(new ScriptFile("all", "all", "/plugins/core-ui/resources/js/Ametys/plugins/coreui/schedule/AddTaskButtonController.js"));
        List<ScriptFile> cssImports = _configureImports(configuration.getChild("css"));
        String jsClassName = _configureClass(configuration.getChild("class"));
        Map<String, Object> initialParameters = configureInitialParameters(configuration);
        
        return new Script(this.getId(), jsClassName, scriptsImports, cssImports, initialParameters);
    }
}
