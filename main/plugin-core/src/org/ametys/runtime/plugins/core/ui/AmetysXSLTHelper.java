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

import java.util.Arrays;
import java.util.List;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.commons.lang.StringEscapeUtils;

import org.ametys.runtime.config.Config;
import org.ametys.runtime.util.I18nUtils;
import org.ametys.runtime.util.I18nizableText;
import org.ametys.runtime.workspace.WorkspaceMatcher;

/**
 * Helper component to be used from XSL stylesheets.
 */
public class AmetysXSLTHelper implements Contextualizable, Serviceable
{
    private static Context _context;
    /** The i18n utils instance */
    protected static I18nUtils _i18nUtils;
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    public void service(ServiceManager manager) throws ServiceException
    {
        _i18nUtils = (I18nUtils) manager.lookup(I18nUtils.ROLE);
    }
    
    /**
     * Returns the current URI prefix.
     * @return the current URI prefix.
     */
    public static String uriPrefix()
    {
        return uriPrefix(true);
    }
    
    /**
     * Returns the current URI prefix.
     * @param withWorkspaceURI true to add the workspace URI (recommended)
     * @return the current URI prefix.
     */
    public static String uriPrefix(boolean withWorkspaceURI)
    {
        return getUriPrefix(withWorkspaceURI);
    }
    
    /**
     * Returns the absolute URI prefix.
     * @return the absolute URI prefix.
     */
    public static String absoluteUriPrefix()
    {
        return absoluteUriPrefix(true);
    }
    
    /**
     * Returns the absolute URI prefix.
     * @param withWorkspaceURI true to add the workspace URI (recommended)
     * @return the absolute URI prefix.
     */
    public static String absoluteUriPrefix(boolean withWorkspaceURI)
    {
        return getAbsoluteUriPrefix(withWorkspaceURI);
    }
    
    /**
     * Return the current workspace name
     * @return The workspace name. Cannot be empty.
     */
    public static String workspaceName()
    {
        return getWorkspaceName();
    }
    
    /**
     * Return the current workspace URI
     * @return The workspace name. Can be empty.
     */
    public static String workspacePrefix()
    {
        return getWorkspacePrefix();
    }
    
    /**
     * Get the application context path. Can be empty if the application
     * resides in the root context. Use it to create a link beginning with
     * the application root.
     * @param withWorkspaceURI true to add the workspace URI (recommended)
     * @return The application context path with workspace URI
     * @see Request#getContextPath()
     */
    protected static String getUriPrefix(boolean withWorkspaceURI)
    {
        Request request = ContextHelper.getRequest(_context);
        String workspaceURI = withWorkspaceURI ? getWorkspacePrefix() : "";
        
        return request.getContextPath() + workspaceURI;
    }
    
    /**
     * Get the absolutized version of the context path. Use it to create an absolute
     * link beginning with the application root, for instance when sending a mail
     * linking to the application.
     * @param withWorkspaceURI true to add the workspace URI (recommended)
     * @return The absolute context path.
     */
    protected static String getAbsoluteUriPrefix(boolean withWorkspaceURI)
    {
        Request request = ContextHelper.getRequest(_context);
        
        String uriPrefix = getUriPrefix(withWorkspaceURI);
        
        if (!uriPrefix.startsWith("http"))
        {
            uriPrefix = request.getScheme() + "://" + request.getServerName() + (request.getServerPort() != 80 ? ":" + request.getServerPort() : "") + uriPrefix;
        }
        
        return uriPrefix;
    }  
    
    /**
     * Return the current workspace name
     * @return The workspace name. Cannot be empty.
     */
    protected static String getWorkspaceName()
    {
        Request request = ContextHelper.getRequest(_context);
        return (String) request.getAttribute(WorkspaceMatcher.WORKSPACE_NAME);
    }
    
    /**
     * Return the current workspace URI
     * @return The workspace name. Can be empty for the default workspace.
     */
    protected static String getWorkspacePrefix()
    {
        Request request = ContextHelper.getRequest(_context);
        return (String) request.getAttribute(WorkspaceMatcher.WORKSPACE_URI);
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
     * Translate an i18n key using current user language.
     * @param key The key to translate. Specify the catalog this way: "catalogue:KEY"
     * @return The translation or null.
     */
    public static String translate(String key)
    {
        return translate(key, null, null);
    }
    
    /**
     * Translate an i18n key
     * @param key The key to translate. Specify the catalog this way: "catalogue:KEY"
     * @param lang The language. Can be null to use current user language.
     * @param parameters The key parameters. Can be empty.
     * @return The translation or null.
     */
    public static String translate(String key, String lang, String[] parameters)
    {
        List<String> parametersAsString = parameters != null ? Arrays.asList(parameters) : null;
        
        I18nizableText i18nKey = new I18nizableText(null, key, parametersAsString);
        return _i18nUtils.translate(i18nKey, lang);
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
