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
package org.ametys.core.user.population;

import java.util.Iterator;

import org.ametys.core.ui.Callable;
import org.ametys.runtime.plugin.component.AbstractThreadSafeComponentExtensionPoint;

/**
 * This class is in charge to load and initialize the various {@link PopulationConsumer}
 */
public class PopulationConsumerExtensionPoint extends AbstractThreadSafeComponentExtensionPoint<PopulationConsumer>
{
    /** Avalon Role */
    public static final String ROLE = PopulationConsumerExtensionPoint.class.getName();
    
    /**
     * Determines if a user population is in use by a {@link PopulationConsumer}
     * @param id The id of the user population to check
     * @return true if the user population is used by at least one of the {@link PopulationConsumer}
     */
    @Callable
    public boolean isInUse(String id)
    {
        boolean inUse = false;
        Iterator<String> iterator = getExtensionsIds().iterator();

        while (iterator.hasNext() && !inUse)
        {
            PopulationConsumer consumer = getExtension(iterator.next());
            inUse = consumer.isInUse(id);
        }
        
        return inUse;
    }

}
