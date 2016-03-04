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
package org.ametys.core.datasource;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

/**
 * This class is in charge to load and initialize the various {@link DataSourceCustomer} 
 */
public class DataSourceCustomerExtensionPoint extends AbstractThreadSafeComponentExtensionPoint<DataSourceCustomer>
{
    /** Avalon Role */
    public static final String ROLE = DataSourceCustomerExtensionPoint.class.getName();

    /**
     * Determines if a data source is in use by a {@link DataSourceCustomer}
     * @param id The id of the data source to check
     * @return true if the data source is used by at least one of the {@link DataSourceCustomer}
     */
    public boolean isInUse(String id)
    {
        boolean inUse = false;
        Iterator<String> iterator = getExtensionsIds().iterator();

        while (iterator.hasNext() && !inUse)
        {
            DataSourceCustomer dataSourceClient = getExtension(iterator.next());
            inUse = dataSourceClient.isInUse(id);
        }
        
        return inUse;
    }
    
    /**
     * Retrieve the ids of the used data sources
     * @return the set of ids of data source used by all of the {@link DataSourceCustomer}
     */
    public Set<String> getUsedDataSourceIds()
    {
        Set<String> extensionsIds = getExtensionsIds();
        Set<String> usedDataSourceIds = new HashSet<>();
        
        for (String extensionId : extensionsIds)
        {
            DataSourceCustomer dataSourceClient = getExtension(extensionId);
            usedDataSourceIds.addAll(dataSourceClient.getUsedDataSourceIds());
        }
        
        return usedDataSourceIds;
    }
}
