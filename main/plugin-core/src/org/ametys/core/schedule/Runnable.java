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

import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Trigger;

import org.ametys.runtime.i18n.I18nizableText;

/**
 * This interface represents the entity by which a {@link Schedulable} can be scheduled.
 */
public interface Runnable
{
    /** The possible misfire policies */
    public static enum MisfirePolicy
    {
        /** 
         * Ignore that there were misfired triggers, and try to fire them all as soon as it can.
         * See {@link CronScheduleBuilder#withMisfireHandlingInstructionIgnoreMisfires()} and {@link Trigger#MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY}
         */
        IGNORE,
        /** 
         * Try to fire one of them as soon as it can.
         * See {@link CronScheduleBuilder#withMisfireHandlingInstructionFireAndProceed()} and {@link CronTrigger#MISFIRE_INSTRUCTION_FIRE_ONCE_NOW}
         */
        FIRE_ONCE,
        /** 
         * The misfired triggers will never be fired and the next trigger to be fired will be the next one.
         * See {@link CronScheduleBuilder#withMisfireHandlingInstructionDoNothing()} and {@link CronTrigger#MISFIRE_INSTRUCTION_DO_NOTHING}
         */
        DO_NOTHING
    }
    
    /**
     * Returns the id
     * @return the id
     */
    public String getId();
    
    /**
     * Returns the label
     * @return the i18n label
     */
    public I18nizableText getLabel();
    
    /**
     * Returns the description
     * @return the i18n description
     */
    public I18nizableText getDescription();
    
    /**
     * When returns true, the runnable will be a one-shot task and will fire as soon as possible when the application starts up.
     * If so, {@link #getCronExpression()} will be ignored whatever its value.
     * @return true to run the task as soon as possible after the application starts up.
     */
    public boolean runAtStartup();
    
    /**
     * Returns the cron expression to base the schedule on. Ignored if {@link #runAtStartup()} is true.
     * @return the cron expression
     */
    public String getCronExpression();
    
    /**
     * Gets the identifier of {@link Schedulable} to execute
     * @return the identifier of {@link Schedulable}
     */
    public String getSchedulableId();
    
    /**
     * Determines if this runnable can be removed
     * @return <code>true</code> if this runnable is removable
     */
    public boolean isRemovable();
    
    /**
     * Determines if this runnable can be modified
     * @return <code>true</code> if this runnable is modifiable
     */
    public boolean isModifiable();
    
    /**
     * Determines if this runnable can be activate or deactivate
     * @return <code>true</code> if this runnable is deactivatable
     */
    public boolean isDeactivatable();
    
    /**
     * Gets the misfire policy, i.e. what the runnable msut do if it missed a trigger.
     * @return The misfire policy
     */
    public MisfirePolicy getMisfirePolicy();
    
    /**
     * Determines if the runnable must not survive to a server restart
     * @return true if the runnable must not survive to a server restart
     */
    public boolean isVolatile();
    
    /**
     * Gets the values of the parameters (from the linked {@link Schedulable})
     * @return the parameter values
     */
    public Map<String, Object> getParameterValues();
}
