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
package org.ametys.core.schedule;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.util.log.SLF4JLoggerAdapter;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;

import org.ametys.core.engine.BackgroundEngineHelper;
import org.ametys.core.schedule.Runnable.FireProcess;
import org.ametys.plugins.core.schedule.Scheduler;

/**
 * Ametys implementation of a {@link Job} which delegates the execution of the task to the right {@link Schedulable}
 */
@PersistJobDataAfterExecution
public class AmetysJob implements Job
{
    /** The key for the last duration of the {@link #execute(org.quartz.JobExecutionContext)} method which is stored in the {@link JobDataMap} */
    public static final String KEY_LAST_DURATION = "duration";
    /** The key for the previous fire time of this job which is stored in the {@link JobDataMap} */
    public static final String KEY_PREVIOUS_FIRE_TIME = "previousFireTime";
    /** The key for the success state of the last execution of the job */
    public static final String KEY_SUCCESS = "success";
    
    /** The service manager */
    protected static ServiceManager _serviceManager;
    /** The extension point for {@link Schedulable}s */
    protected static SchedulableExtensionPoint _schedulableEP;
    /** The scheduler component */
    protected static Scheduler _scheduler;
    /** The cocoon environment context. */
    protected static org.apache.cocoon.environment.Context _environmentContext;
    /** The logger */
    protected static Logger _logger;
    
    /**
     * Initialize the static fields.
     * @param serviceManager The service manager
     * @param context The context
     * @param logger The logger
     * @throws ServiceException if an error occurs during the lookup of the {@link SchedulableExtensionPoint}
     * @throws ContextException if environment context object not found
     */
    public static void initialize(ServiceManager serviceManager, Context context, Logger logger) throws ServiceException, ContextException
    {
        _serviceManager = serviceManager;
        _schedulableEP = (SchedulableExtensionPoint) serviceManager.lookup(SchedulableExtensionPoint.ROLE);
        _scheduler = (Scheduler) serviceManager.lookup(Scheduler.ROLE);
        _environmentContext = (org.apache.cocoon.environment.Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        _logger = logger;
    }
    
    /**
     * Releases and destoys used resources
     */
    public static void dispose()
    {
        _serviceManager = null;
        _schedulableEP = null;
        _scheduler = null;
        _environmentContext = null;
        _logger = null;
    }
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        boolean success = true;
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        
        // Set the previous (which is actually the current) fire time in the map (because the trigger may no longer exist in the future)
        jobDataMap.put(KEY_PREVIOUS_FIRE_TIME, context.getTrigger().getPreviousFireTime().getTime()); // possible with @PersistJobDataAfterExecution annotation
        
        String runnableId = jobDataMap.getString(Scheduler.KEY_RUNNABLE_ID);
        String schedulableId = jobDataMap.getString(Scheduler.KEY_SCHEDULABLE_ID);
        Schedulable schedulable = _schedulableEP.getExtension(schedulableId);
        Map<String, Object> environmentInformation = BackgroundEngineHelper.createAndEnterEngineEnvironment(_serviceManager, _environmentContext, new SLF4JLoggerAdapter(_logger));
        
        _logger.info("Executing the Runnable '{}' of the Schedulable '{}' with jobDataMap:\n '{}'", runnableId, schedulableId, jobDataMap.getWrappedMap().toString());
        Instant start = Instant.now();
        try
        {
            schedulable.execute(context);
        }
        catch (Exception e)
        {
            success = false;
            _logger.error(String.format("An error occured during the execution of the Schedulable '%s'", schedulableId), e);
            throw new JobExecutionException(String.format("An error occured during the execution of the job '%s'", schedulableId), e);
        }
        finally
        {
            // Set the duration in the map
            Instant end = Instant.now();
            long duration = Duration.between(start, end).toMillis();
            jobDataMap.put(KEY_LAST_DURATION, duration); // possible with @PersistJobDataAfterExecution annotation
            
            // Leave the Engine Environment
            if (environmentInformation != null)
            {
                BackgroundEngineHelper.leaveEngineEnvironment(environmentInformation);
            }
            
            // Success ?
            jobDataMap.put(KEY_SUCCESS, success);
            
            // Run at startup tasks are one-shot tasks => if so, indicates it is completed for never refiring it
            if (FireProcess.STARTUP.toString().equals(jobDataMap.getString(Scheduler.KEY_RUNNABLE_FIRE_PROCESS)))
            {
                jobDataMap.put(Scheduler.KEY_RUNNABLE_STARTUP_COMPLETED, true);
            }
        }
    }
}
