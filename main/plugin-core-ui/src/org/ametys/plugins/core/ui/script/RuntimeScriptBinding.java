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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.ametys.runtime.i18n.I18nizableText;

import com.google.common.collect.ImmutableMap;

/**
 * Runtime default script binding, provides default variables to the script tool, and a configuration file for the functions. 
 */
public class RuntimeScriptBinding extends StaticConfigurableScriptBinding implements Contextualizable
{
    private ServiceManager _manager;
    private Context _context;

    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        super.service(manager);

        _manager = manager;
    }
    
    @Override
    public Map<String, Object> getVariables()
    {
        Map<String, Object> variables = new HashMap<>();
        variables.put("serviceManager", _manager);
        variables.put("sourceResolver", _sourceResolver);
        variables.put("avalonContext", _context);
        return variables;
    }
    
    @Override
    public Map<String, I18nizableText> getVariablesDescriptions()
    {
        return ImmutableMap.of("serviceManager", new I18nizableText("plugin.core-ui", "PLUGINS_CORE_UI_SCRIPT_VAR_SERVICEMANAGER"),
                               "sourceResolver", new I18nizableText("plugin.core-ui", "PLUGINS_CORE_UI_SCRIPT_VAR_RESOLVER"),
                               "avalonContext", new I18nizableText("plugin.core-ui", "PLUGINS_CORE_UI_SCRIPT_VAR_AVALONCONTEXT"));
    }

}
