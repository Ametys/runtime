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
package org.ametys.runtime.request;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.servlet.RuntimeConfig;


/**
 * Action executed on the beginning of each request, responsible for : <br>
 *  - Checking the configuration and redirecting if its missing or incomplete ;<br>
 *  - Executing the InitRequestHandler if specified.
 */
public class InitRequestAction extends ServiceableAction
{
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Request request = ObjectModelHelper.getRequest(objectModel);
        String uri = _checkURI(request.getSitemapURI());
        
        // Vérification de la configuration
        // Si elle n'existe pas ou si elle est incomplète (ie le modèle a changé), on redirige vers l'admin
        ConfigManager configManager = ConfigManager.getInstance();
        if (!configManager.isComplete())
        {
            // Si c'est une URL autorisée, on laisse passer
            for (String allowedURL : RuntimeConfig.getInstance().getIncompleteConfigAllowedURLs())
            {
                if (uri.startsWith(allowedURL))
                {
                    return EMPTY_MAP;
                }
            }
            
            // Sinon, on redirige
            String redirectURL = RuntimeConfig.getInstance().getIncompleteConfigRedirectURL();
            
            if (redirectURL.startsWith("cocoon:/"))
            {
                redirector.redirect(true, redirectURL);
            }
            else
            {
                redirector.redirect(true, request.getContextPath() + redirectURL);
            }
            
            return EMPTY_MAP;
        }
        
        InitRequestHandler requestHandler = (InitRequestHandler) manager.lookup(InitRequestHandler.ROLE);
        
        requestHandler.initRequest(redirector);
        
        return EMPTY_MAP;
    }
    
    private String _checkURI(String uri)
    {
        String checkedUri = uri;

        if (checkedUri.startsWith("/"))
        {
            checkedUri = checkedUri.substring(1);
        }

        if (checkedUri.endsWith("/"))
        {
            checkedUri = checkedUri.substring(0, checkedUri.length() - 1);
        }

        return checkedUri;
    }
}
