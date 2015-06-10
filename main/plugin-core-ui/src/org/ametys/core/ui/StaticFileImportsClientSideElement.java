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
package org.ametys.core.ui;

import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;


/**
 * This implementation creates an element from a static configuration
 */
public class StaticFileImportsClientSideElement extends StaticClientSideElement
{
    @Override
    protected Script _configureScript(Configuration configuration) throws ConfigurationException
    {
        List<String> scriptsImports = _configureImports(configuration.getChild("scripts"));
        List<String> cssImports = _configureImports(configuration.getChild("css"));
        
        return new Script("", scriptsImports, cssImports);
    }
}
