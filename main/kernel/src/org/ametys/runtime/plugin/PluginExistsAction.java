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
package org.ametys.runtime.plugin;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.AbstractAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

/**
 * Tests if a given plugin exists. 
 */
public class PluginExistsAction extends AbstractAction implements ThreadSafe
{
    @Override
    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception
    {
        if (PluginsManager.getInstance().getPluginNames().contains(source))
        {
            return EMPTY_MAP;
        }
        
        return null;
    }
}
