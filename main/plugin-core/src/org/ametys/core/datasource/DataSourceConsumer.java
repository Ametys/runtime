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

import java.util.Set;

/**
 * Interface for entities that use data sources
 */
public interface DataSourceConsumer
{
    /**
     * Determines if a data source is used
     * @param id The id of data source to check
     * @return true if the data source is currently in use
     */
    public boolean isInUse(String id);
    
    /**
     * Retrieve the ids of the used data sources
     * @return the set of ids of the data source(s) used by this data source customer
     */
    public Set<String> getUsedDataSourceIds();
}
