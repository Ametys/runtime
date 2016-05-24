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

import org.ametys.runtime.i18n.I18nizableText;

/**
 * This interface represents the entity by which a {@link Schedulable} can be scheduled.
 *
 */
public interface Runnable
{
    /**
     * Returns the label
     * @return the i18n label
     */
    public I18nizableText getLabel ();
    
    /**
     * Returns the description
     * @return the i18n description
     */
    public I18nizableText getDescription ();
    
    /**
     * Returns the cron expression to base the schedule on.
     * @return the cron expression
     */
    public String getCronExpression();
    
    /**
     * Get the identifier of {@link Schedulable} to execute
     * @return the identifier of {@link Schedulable}
     */
    public String getSchedulableId ();
    
    /**
     * Determines if this runnable can be removed
     * @return <code>true</code> if this runnable is removeable
     */
    public boolean isRemoveable();
    
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
}
