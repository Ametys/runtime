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

package org.ametys.runtime.plugins.core.ui;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.lang.StringEscapeUtils;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.workspace.WorkspaceMatcher;

/**
 * Helper component to be used from XSL stylesheets.
 */
public class AmetysXSLTHelper implements Contextualizable
{
    private static Context _context;
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    /**
     * Returns the current URI prefix.
     * @return the current URI prefix.
     */
    public static String uriPrefix()
    {
        return getUriPrefix();
    }
    
    /**
     * Returns the absolute URI prefix.
     * @return the absolute URI prefix.
     */
    public static String absoluteUriPrefix()
    {
        return getAbsoluteUriPrefix();
    }
    
    /**
     * Get the application context path. Can be empty if the application
     * resides in the root context. Use it to create a link beginning with
     * the application root.
     * @return The application context path.
     * @see Request#getContextPath()
     */
    protected static String getUriPrefix()
    {
        Request request = ContextHelper.getRequest(_context);
        String workspaceURI = (String) request.getAttribute(WorkspaceMatcher.WORKSPACE_URI);
        
        return request.getContextPath() + workspaceURI;
    }
    
    /**
     * Get the absolutized version of the context path. Use it to create an absolute
     * link beginning with the application root, for instance when sending a mail
     * linking to the application.
     * @return The absolute context path.
     */
    protected static String getAbsoluteUriPrefix()
    {
        Request request = ContextHelper.getRequest(_context);
        
        String uriPrefix = getUriPrefix();
        
        if (!uriPrefix.startsWith("http"))
        {
            uriPrefix = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() != 80 ? ":" + request.getServerPort() : "") + uriPrefix;
        }
        
        return uriPrefix;
    }  
    
    /**
     * Returns the configuration value associated with the given parameter.
     * @param id the configuration parameter.
     * @return the configuration value associated with the given parameter.
     */
    public static String config(String id)
    {
        if (Config.getInstance() != null)
        {
            return Config.getInstance().getValueAsString(id);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Return the value of a request parameter.
     * @param parameter the parameter name.
     * @return the request parameter.
     */
    public static String requestParameter(String parameter)
    {
        Request request = ContextHelper.getRequest(_context);
        return request.getParameter(parameter);
    }
    
    /**
     * Escape the given string to be used as JS variable.
     * @param str the string to escape.
     * @return the escaped String.
     */
    public static String escapeJS(String str)
    {
        return StringEscapeUtils.escapeJavaScript(str);
    }
}
