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
package org.ametys.plugins.core.impl.user.population;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.core.user.population.PopulationConsumer;
import org.ametys.core.user.population.PopulationContextHelper;

/**
 * Implementation of {@link PopulationConsumer} allowing to know whether a population is used by the contexts or not.
 */
public class ContextPopulationConsumer implements PopulationConsumer, Serviceable
{
    /** The helper for the associations population/context */
    private PopulationContextHelper _populationContextHelper;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _populationContextHelper = (PopulationContextHelper) manager.lookup(PopulationContextHelper.ROLE);
    }
    
    @Override
    public boolean isInUse(String id)
    {
        return _populationContextHelper.isLinked(id);
    }
}
