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
package org.ametys.plugins.core.impl.schedule;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import org.ametys.core.schedule.Schedulable;
import org.ametys.plugins.core.schedule.Scheduler;
import org.ametys.plugins.core.ui.script.ScriptHandler;

/**
 * A {@link Schedulable} job for executing scripts.
 */
public class ScriptSchedulable extends AbstractStaticSchedulable
{
    /** The key for the script (as string) to execute */
    public static final String SCRIPT_KEY = Scheduler.PARAM_VALUES_PREFIX + "script";
    
    /** The script handler */
    protected ScriptHandler _scriptHandler;
    
    @Override
    public void service(ServiceManager manager) throws ServiceException
    {
        super.service(manager);
        _scriptHandler = (ScriptHandler) manager.lookup(ScriptHandler.class.getName());
    }
    
    @Override
    public void execute(JobExecutionContext context) throws Exception
    {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String script = (String) jobDataMap.get(SCRIPT_KEY);
        _scriptHandler.executeScript("function main() { \n " + script + " \n }");
    }
}
