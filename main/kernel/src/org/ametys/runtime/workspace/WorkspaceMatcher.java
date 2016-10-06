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

package org.ametys.runtime.workspace;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.matching.WildcardURIMatcher;
import org.apache.cocoon.sitemap.PatternException;

import org.ametys.runtime.servlet.RuntimeConfig;

/**
 * Workspace aware Cocoon matcher.<br>
 * Retrieve workspaces URI from names through the WorkspaceManager.
 */
public class WorkspaceMatcher extends WildcardURIMatcher
{
    /** The request attribute name where the current workspace is saved */
    public static final String WORKSPACE_NAME = "workspaceName";
    /** The request attribute name where the current workspace uri is saved */
    public static final String WORKSPACE_URI = "workspaceURI";
    /** The request attribute name where the current url inside the workspace is saved */
    public static final String IN_WORKSPACE_URL = "inWorkspaceURL";
    /** The request attribute name where the current workspace theme name is saved */
    public static final String WORKSPACE_THEME = "workspaceTheme";
    /** The request attribute name where the current workspace theme url is saved */
    public static final String WORKSPACE_THEME_URL = "workspaceThemeURL";
    
    @Override
    public Map match(String pattern, Map objectModel, Parameters parameters) throws PatternException
    {
        // Vérifie la partie wildcard de l'url
        Map<String, String> result = super.match(pattern, objectModel, parameters);
        if (result == null)
        {
            return null;
        }
        
        // Récupère le nom du workspace
        String workspaceName;
        boolean defaultWorkspace = "true".equals(parameters.getParameter("default", null)); 
        if (defaultWorkspace)
        {
            workspaceName = RuntimeConfig.getInstance().getDefaultWorkspace();
        }
        else
        {
            workspaceName = result.get("1"); 
        }
        
        WorkspaceManager wm = WorkspaceManager.getInstance();
        
        if (!wm.getWorkspaceNames().contains(workspaceName))
        {
            return null;
        }
        
        Request request = ObjectModelHelper.getRequest(objectModel);
        request.setAttribute(WORKSPACE_NAME, workspaceName);
        request.setAttribute(WORKSPACE_URI, defaultWorkspace ? "" : "/_" + workspaceName);
        request.setAttribute(IN_WORKSPACE_URL, result.get(defaultWorkspace ? "1" : "2"));
        request.setAttribute(WORKSPACE_THEME, wm.getWorkspaces().get(workspaceName).getThemeName());
        request.setAttribute(WORKSPACE_THEME_URL, wm.getWorkspaces().get(workspaceName).getThemeURL());
        
        result.put("workspaceName", workspaceName);
        
        return result;
    }
}
