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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.lang3.StringUtils;
import org.apache.excalibur.source.SourceResolver;

import org.ametys.runtime.i18n.I18nizableText;
import org.ametys.runtime.plugin.component.AbstractLogEnabled;

/*
 * The expected file configuration format is : 
 * <function class="org.ametys.plugins.core.ui.script.StaticConfigurableScriptBinding">
 *     <descriptions>
 *         <description>
 *             <name><!-- A function name --></name>
 *             <text><!-- A function description text --></text>
 *         </description>
 *         <description>...</description>
 *     </descriptions>
 *     <script>
 *         <!-- The functions code -->
 *     </script>
 * </function>
 */

/**
 * Static implementation of the ScriptBinding that can read the functions from an xml file
 */
public class StaticConfigurableScriptBinding extends AbstractLogEnabled implements ScriptBinding, Configurable, Serviceable
{
    /** Source Resolver */
    protected SourceResolver _sourceResolver;
    
    /** List of functions per configuration file */
    protected List<String> _functions;
    
    /** List of function descriptions */
    protected Map<String, I18nizableText> _functionsDescriptions;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _sourceResolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        _functions = new ArrayList<>();
        _functionsDescriptions = new HashMap<>();
        
        Configuration scriptConf = configuration.getChild("script", false);
        if (scriptConf != null)
        {
            _functions.add(scriptConf.getValue());
        }
        
        Configuration descriptionsConf = configuration.getChild("descriptions", false);
        if (descriptionsConf != null)
        {
            for (Configuration descriptionConf : descriptionsConf.getChildren("description"))
            {
                Configuration nameConf = descriptionConf.getChild("name", false);
                Configuration textConf = descriptionConf.getChild("text", false);
                
                if (nameConf != null && textConf != null)
                {
                    _functionsDescriptions.put(nameConf.getValue(), I18nizableText.parseI18nizableText(textConf, "plugin.core-ui", ""));
                }
            }
        }
    }

    public String getFunctions()
    {
        return StringUtils.join(_functions, "\n");
    }

    public Map<String, I18nizableText> getFunctionsDescriptions()
    {
        return _functionsDescriptions;
    }

    public Map<String, Object> getVariables()
    {
        // Do nothing
        return null;
    }

    public Map<String, I18nizableText> getVariablesDescriptions()
    {
        // Do nothing
        return null;
    }

    public void cleanVariables(Map<String, Object> variables)
    {
        // Do nothing
    }

    public Object processScriptResult(Object result) throws ScriptException
    {
        // Do nothing
        return null;
    }
}
