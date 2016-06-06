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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.ametys.runtime.plugin.Init;

/**
 * An init class to start the {@link Scheduler}
 */
public class SchedulerInit implements Init, Serviceable
{
    /** The scheduler */
    protected Scheduler _scheduler;

    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        _scheduler = (Scheduler) manager.lookup(Scheduler.ROLE);
    }
    
    @Override
    public void init() throws Exception
    {
        _scheduler.start();
    }
}
