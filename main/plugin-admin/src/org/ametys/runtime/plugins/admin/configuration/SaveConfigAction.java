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
package org.ametys.runtime.plugins.admin.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.core.cocoon.ActionResultGenerator;
import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.parameter.Errors;
import org.ametys.runtime.servlet.RuntimeServlet;
import org.ametys.runtime.util.AmetysHomeHelper;


/**
 * This action is in charge to get and save the config values entered by the user.<br>
 * The backup is delegated to <code>Config</code>
 */
public class SaveConfigAction extends AbstractAction implements ThreadSafe
{   
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting SaveConfigAction");
        }

        Request request = ObjectModelHelper.getRequest(objectModel);
        Map<String, Object> result = new HashMap<> ();
        
        try
        {
            Map<String, String> untypedValues = new HashMap<>();
            
            ConfigManager configManager = ConfigManager.getInstance();
            String[] ids = configManager.getParametersIds();
            for (int i = 0; i < ids.length; i++)
            {
                String untypedValue = request.getParameter(ids[i]);
                untypedValues.put(ids[i], untypedValue);
            }

            // Save configuration
            Map<String, Errors> errorFields = configManager.save(untypedValues, new File(AmetysHomeHelper.getAmetysHomeConfig(), RuntimeServlet.CONFIG_FILE_NAME).getCanonicalPath());
            if (errorFields.size() > 0)
            {
                for (String paramId : errorFields.keySet())
                {
                    Errors errors = errorFields.get(paramId);
                    result.put(paramId, errors);
                }
                
                request.setAttribute(ActionResultGenerator.MAP_REQUEST_ATTR, result);
                return EMPTY_MAP;
            }
        }
        catch (Exception e)
        {
            getLogger().error("An error occured while saving config modifications", e);
            
            result.put("error", e.getMessage());
            request.setAttribute(ActionResultGenerator.MAP_REQUEST_ATTR, result);
            return EMPTY_MAP;
        }

        // Set the request attribute for Cocoon reloading
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Positionning org.ametys.runtime.reload=true for Cocoon reloading");
        }
        request.setAttribute("org.ametys.runtime.reload", true);

        return EMPTY_MAP;
    }
}
