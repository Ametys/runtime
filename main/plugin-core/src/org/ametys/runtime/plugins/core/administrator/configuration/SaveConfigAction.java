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
package org.ametys.runtime.plugins.core.administrator.configuration;

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
            Map<String, String> untypedValues = new HashMap<String, String>();
            
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
            
            Map<String, String> result = new HashMap<String, String>();
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
