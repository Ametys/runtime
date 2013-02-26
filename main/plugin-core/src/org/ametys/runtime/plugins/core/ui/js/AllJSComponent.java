/*
 *  Copyright 2010 Anyware Services
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

package org.ametys.runtime.plugins.core.ui.js;

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Session;
import org.apache.commons.lang.StringUtils;

/**
 * This component handles the lists of JS files for each request
 */
public class AllJSComponent implements ThreadSafe, Contextualizable, Component
{
    /** The avalon role */
    public static final String ROLE = AllJSComponent.class.getName();
    
    private static final String __CONTEXT_KEY = AllJSComponent.class.getName() + "$contextPath";
    private static final String __CALLBACK_KEY = AllJSComponent.class.getName() + "$callback";
    
    private static Context _context;
    
    @Override
    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }

    /**
     * Reset the js files list
     * @param callback The callback code
     */
    public static void resetJSFilesList(String callback)
    {
        Session session = ContextHelper.getRequest(_context).getSession(true);
        
        session.setAttribute(AllJSComponent.class.getName(), new ArrayList<String>());
        session.setAttribute(__CONTEXT_KEY, null);
        session.setAttribute(__CALLBACK_KEY, callback);
    }
    
    /**
     * Get the callback code
     * @return The callbackcode
     */
    public String getCallback()
    {
        String code = (String) ContextHelper.getRequest(_context).getSession(true).getAttribute(__CALLBACK_KEY);
        if (StringUtils.isNotBlank(code))
        {
            if (!code.endsWith(";"))
            {
                code += ";";
            }
            code += "\n";
        }
        else
        {
            code = "";
        }
        return code; 
    }
    
    /**
     * Set the context path.
     * @param contextPath the context path.
     */
    public static void setContextPath(String contextPath)
    {
        ContextHelper.getRequest(_context).getSession(true).setAttribute(__CONTEXT_KEY, contextPath);
    }
    
    /**
     * Get the context path.
     * @return the context path.
     */
    public String getContextPath()
    {
        return (String) ContextHelper.getRequest(_context).getSession(true).getAttribute(__CONTEXT_KEY);
    }
    
    /**
     * Adds a file to the list of js files to load.
     * resetJSFilesList method has to be called before the first call
     * @param filename A filename (relative to context path)
     */
    public static void addJSFile(String filename)
    {
        List<String> jsFilesList = (List<String>) ContextHelper.getRequest(_context).getSession(true).getAttribute(AllJSComponent.class.getName());
        jsFilesList.add(filename);
    }

    /**
     * Get the size of the current list
     * @return The size
     */
    public static int getCount()
    {
        List<String> jsFilesList = (List<String>) ContextHelper.getRequest(_context).getSession(true).getAttribute(AllJSComponent.class.getName());
        return jsFilesList.size();
    }
    
    /**
     * Get a unique hashcode for the list.
     * This allows to use navigator cache by using an url using this hash code.
     * This is needed to get the list back.
     * @return A hash code.
     */
    public static int getHashCode()
    {
        Session session = ContextHelper.getRequest(_context).getSession(true);
        List<String> jsFilesList = (List<String>) session.getAttribute(AllJSComponent.class.getName());
        
        if (jsFilesList == null)
        {
            return 0; 
        }
        else
        {
            int hashCode = jsFilesList.hashCode();
            session.setAttribute(AllJSComponent.class.getName() + "-" + hashCode, jsFilesList);
            return hashCode;
        }
    }
    
    /**
     * Get the list of js files
     * @param hashCodeAsString The hash code of the list. This will not retrieve the right list, but will ensure that the current list in the session is the same as the one required
     * @return A list of files (relative to context path). Can be null 
     */
    public List<String> getJSFilesList(String hashCodeAsString)
    {
        List<String> list = (List<String>) ContextHelper.getRequest(_context).getSession(true).getAttribute(AllJSComponent.class.getName() + "-" + hashCodeAsString);
        
        if (list == null)
        {
            throw new IllegalStateException("The js files list has a different hash code compared to the one required.");
        }
        
        return list;
    }
}
