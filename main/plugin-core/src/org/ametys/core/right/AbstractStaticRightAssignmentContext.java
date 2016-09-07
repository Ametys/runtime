/*
 *  Copyright 2016 Anyware Services
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
package org.ametys.core.right;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.commons.lang3.StringUtils;

import org.ametys.core.ui.StaticClientSideElement;
import org.ametys.runtime.workspace.WorkspaceMatcher;

/**
 * This static impl is a static client side element that take care that the current workspace matches a configured regexp 
 */
public abstract class AbstractStaticRightAssignmentContext extends StaticClientSideElement implements RightAssignmentContext, Contextualizable
{
    /** The avalon context */
    protected Context _context;
    /** The regexp that should match current workspace */
    protected Pattern _workspaceMatcher;
    /** Consider _workspaceMatcher negatively */
    protected boolean _reverseWorkspaceMather;

    public void contextualize(Context context) throws ContextException
    {
        _context = context;
    }
    
    @Override
    public void configure(Configuration configuration) throws ConfigurationException
    {
        super.configure(configuration);
     
        String workspace = configuration.getChild("workspace").getValue();
        if (StringUtils.isNotBlank(workspace))
        {
            if (workspace.startsWith("!"))
            {
                _reverseWorkspaceMather = true;
                workspace = workspace.substring(1);
            }
            _workspaceMatcher = Pattern.compile(workspace);
        }
    }
    
    @Override
    public List<Script> getScripts(boolean ignoreRights, Map<String, Object> contextParameters)
    {
        if (!matchWorkspace(contextParameters))
        {
            return new ArrayList<>();
        }
        
        return super.getScripts(ignoreRights, contextParameters);
    }
    
    /**
     * Determines if the current workspace matches the configured workspace regexp
     * @param contextParameters The contextual parameters 
     * @return true if the current workspace matches
     */
    protected boolean matchWorkspace (Map<String, Object> contextParameters)
    {
        String currentWorkspace = (String) contextParameters.get(WorkspaceMatcher.WORKSPACE_NAME);
        if (_workspaceMatcher != null)
        {
            boolean match = _workspaceMatcher.matcher(currentWorkspace).matches();
            if (match && _reverseWorkspaceMather || !match && !_reverseWorkspaceMather)
            {
                return false;
            }
        }
        
        return true;
    }
}
