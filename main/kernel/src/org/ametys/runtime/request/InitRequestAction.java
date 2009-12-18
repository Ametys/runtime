/*
 *  Copyright 2009 Anyware Services
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
                redirector.redirect(false, redirectURL);
            }
            else
            {
                redirector.redirect(false, request.getContextPath() + redirectURL);
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
