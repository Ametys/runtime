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

import java.io.IOException;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;

import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.servlet.RuntimeConfig;

/**
 * Default implementation of a {@link InitRequestHandler}, checking the CMS
 * configuration and redirecting if it's missing or incomplete.
 */
public class DefaultInitRequestHandler extends AbstractLogEnabled implements InitRequestHandler, Contextualizable
{
    
    /** The avalon context. */
    protected Context _context;
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    public void initRequest(Redirector redirector)
    {
        Request request = ContextHelper.getRequest(_context);
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
                    return;
                }
            }
            
            // Sinon, on redirige
            String redirectURL = RuntimeConfig.getInstance().getIncompleteConfigRedirectURL();
            
            try
            {
                if (redirectURL.startsWith("cocoon:/"))
                {
                    redirector.redirect(false, redirectURL);
                }
                else
                {
                    redirector.redirect(false, request.getContextPath() + redirectURL);
                }
            }
            catch (ProcessingException e)
            {
                throw new RuntimeException("Cannot redirect to the configuration screen.", e);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Cannot redirect to the configuration screen.", e);
            }
            
            return;
        }
    }
    
    /**
     * Normalize the URI, trimming the potential slashes at the beginning and end.
     * @param uri the URI to normalize.
     * @return the normalized URI.
     */
    protected String _checkURI(String uri)
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
