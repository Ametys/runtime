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
package org.ametys.plugins.core.schedule;

import java.util.Collections;
import java.util.Set;

import org.apache.avalon.framework.component.Component;

import org.ametys.core.datasource.DataSourceConsumer;
import org.ametys.runtime.config.Config;

/**
 * Implementation of {@link DataSourceConsumer} allowing to know whether a data source is used by the {@link Scheduler} or not.
 * It also allows to retrieve the data source ids that are currently in use.
 */
public class SchedulerDataSourceConsumer implements DataSourceConsumer, Component
{
    /** Avalon Role */
    public static final String ROLE = SchedulerDataSourceConsumer.class.getName();
    
    /** Name of the parameter holding the datasource id for Quartz */
    private static final String __DATASOURCE_CONFIG_NAME = "runtime.scheduler.datasource";

    @Override
    public boolean isInUse(String id)
    {
        return getUsedDataSourceIds().contains(id);
    }

    @Override
    public Set<String> getUsedDataSourceIds()
    {
        return Collections.singleton(Config.getInstance().getValueAsString(__DATASOURCE_CONFIG_NAME));
    }
}
