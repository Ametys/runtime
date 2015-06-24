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
package org.ametys.plugins.adminold.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.servlet.RuntimeServlet;


/**
 * This action is in charge to get and save the config values entered by the user.<br>
 * The backup is delegated to <code>Config</code>
 */
public class SaveConfigAction extends AbstractAction implements Contextualizable, ThreadSafe
{   
    // Logger for traces
    private String _configFileName;

    public void contextualize(Context context) throws ContextException
    {
        org.apache.cocoon.environment.Context ctx = (org.apache.cocoon.environment.Context) context.get(org.apache.cocoon.Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        _configFileName = ctx.getRealPath(RuntimeServlet.CONFIG_RELATIVE_PATH);
    }
    
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters parameters) throws Exception
    {
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Starting SaveConfigAction");
        }

        // Requete
        Request request = ObjectModelHelper.getRequest(objectModel);

        try
        {
            Map<String, String> untypedValues = new HashMap<>();
            
            // Configuration
            ConfigManager configManager = ConfigManager.getInstance();
            String[] ids = configManager.getParametersIds();
            for (int i = 0; i < ids.length; i++)
            {
                String untypedValue = request.getParameter(ids[i]);
                untypedValues.put(ids[i], untypedValue);
            }

            // Sauvegarde le déploiement
            configManager.save(untypedValues, _configFileName);
        }
        catch (Exception e)
        {
            getLogger().error("An error occured while saving config modifications", e);
            
            Map<String, String> result = new HashMap<>();
            result.put("error", e.getMessage());
            return result;
        }

        // Positionne l'attribut sur la requête pour le redémarrage de Cocoon
        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Positionning org.ametys.runtime.reload=true for Cocoon reloading");
        }
        request.setAttribute("org.ametys.runtime.reload", "true");

        return EMPTY_MAP;
    }
}