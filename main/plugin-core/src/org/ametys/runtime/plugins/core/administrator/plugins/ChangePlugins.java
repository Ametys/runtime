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

package org.ametys.runtime.plugins.core.administrator.plugins;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceUtil;

/**
 * Change the plugins (activate/deactivate)
 */
public class ChangePlugins extends AbstractAction implements ThreadSafe
{
    @SuppressWarnings("unchecked")
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        Map<String, Object> jsParameters = (Map<String, Object>) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        
        // map of extension ids, true means to activate and false to deactivate
        Map<String, Boolean> extensionPoints = (Map<String, Boolean>) jsParameters.get("EP");
        Map<String, String> singleExtensionPoints = (Map<String, String>) jsParameters.get("SEP");

        if (getLogger().isDebugEnabled())
        {
            getLogger().debug("Applying following changes");
            
            for (String extensionPoint : extensionPoints.keySet())
            {
                getLogger().debug("EP " + extensionPoint + " " + extensionPoints.get(extensionPoint));
            }
            for (String extensionPoint : singleExtensionPoints.keySet())
            {
                getLogger().debug("SEP " + extensionPoint + " " + singleExtensionPoints.get(extensionPoint));
            }
            
            getLogger().debug("Applying preceding changes");
        }

        // 1 - save current runtime.xml file to runtime.bak
        Source bakFile = null;
        Source currentFile = null;
        try
        {
            bakFile = resolver.resolveURI("context://WEB-INF/param/runtime.bak");
            currentFile = resolver.resolveURI("context://WEB-INF/param/runtime.xml");
            
            SourceUtil.copy(currentFile, bakFile);
        }
        finally
        {
            resolver.release(bakFile);
            resolver.release(currentFile);
        }
        
        Source src = null;
        try
        {
            currentFile = resolver.resolveURI("context://WEB-INF/param/runtime.xml");
            src = resolver.resolveURI("cocoon:/administrator/plugins/change-runtime", null, jsParameters);
            
            SourceUtil.copy(src, currentFile);
        }
        finally
        {
            resolver.release(src);
            resolver.release(currentFile);
        }

        return EMPTY_MAP;
    }
}
