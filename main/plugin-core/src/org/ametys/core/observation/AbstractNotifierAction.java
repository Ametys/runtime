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
package org.ametys.core.observation;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.Action;

import org.ametys.core.user.CurrentUserProvider;
import org.ametys.core.util.cocoon.AbstractCurrentUserProviderServiceableAction;

/**
 * Abstract {@link Action} providing:
 * <ul>
 *  <li>{@link CurrentUserProvider}
 *  <li>{@link ObservationManager}
 * </ul>
 */
public abstract class AbstractNotifierAction extends AbstractCurrentUserProviderServiceableAction
{
    /** Observer manager. */
    protected ObservationManager _observationManager;

    @Override
    public void service(ServiceManager serviceManager) throws ServiceException
    {
        super.service(serviceManager);
        _observationManager = (ObservationManager) serviceManager.lookup(ObservationManager.ROLE);
    }
}
