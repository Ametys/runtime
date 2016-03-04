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
package org.ametys.plugins.core.impl.datasource;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ametys.core.datasource.DataSourceCustomer;
import org.ametys.runtime.config.Config;
import org.ametys.runtime.config.ConfigManager;
import org.ametys.runtime.config.ConfigParameter;
import org.ametys.runtime.parameter.ParameterHelper.ParameterType;

/**
 * Implementation of {@link DataSourceCustomer} allowing to know whether a data source is used in the configuration or not.
 * It also allows to retrieve the data source ids that are currently in use.
 */
public class ConfigurationDataSourceCustomer implements DataSourceCustomer
{
    @Override
    public boolean isInUse(String id)
    {
        return getUsedDataSourceIds().contains(id);
    }
    
    @Override
    public Set<String> getUsedDataSourceIds()
    {
        Set<String> usedDataSourceIds = new HashSet<> ();
        Config config = Config.getInstance();
        if (config != null)
        {
            Map<String, ConfigParameter> parameters = ConfigManager.getInstance().getParameters();
            for (String paramName : parameters.keySet())
            {
                if (parameters.get(paramName).getType() == ParameterType.DATASOURCE)
                {
                    usedDataSourceIds.add(config.getValueAsString(paramName));
                }
            }
        }
        
        return usedDataSourceIds;
    }
}
