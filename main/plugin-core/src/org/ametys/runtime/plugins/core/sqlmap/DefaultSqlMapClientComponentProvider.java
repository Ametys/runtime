/*
 *  Copyright 2013 Anyware Services
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
package org.ametys.runtime.plugins.core.sqlmap;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Implementation of {@link SqlMapClientComponentProvider} use the which use the configuration element <role>
 * to get the component to retrieve.<br>
 * This target component must implements <code>SqlMapClientsAware</code> in order
 * to be injected with one or more <code>SqlMapClient</code> instances.
 */
public class DefaultSqlMapClientComponentProvider implements SqlMapClientComponentProvider, Configurable
{
    private String _role;

    public void configure(Configuration configuration) throws ConfigurationException
    {
        _role = configuration.getChild("role", true).getValue(null);
        
        if (_role == null)
        {
         // Use the extension id for selecting the component to inject SqlMap with
            _role = configuration.getAttribute("id");
        }
    }
    
    public String getComponentRole()
    {
        return _role;
    }
}
