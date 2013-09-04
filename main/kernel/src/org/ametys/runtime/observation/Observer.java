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
package org.ametys.runtime.observation;

import java.util.Map;

/**
 * Observer for consuming events.
 */
public interface Observer
{
    /** Minimum priority. */
    public static final int MIN_PRIORITY = Integer.MAX_VALUE;
    /** Maximum priority. */
    public static final int MAX_PRIORITY = 0;
    
    /**
     * Checks if the event is supported. If true, the observe(Event) method will be called.
     * @param event the event.
     * @return <code>true</code> for observing this event, <code>false</code> otherwise.
     */
    boolean supports(Event event);
    
    /**
     * Retrieves the priority to observe this event.<br>
     * This can be used to process a supported event before others observers.
     * @param event the event.
     * @return the priority where 0 the max priority and Integer.MAX_VALUE the min priority.
     */
    int getPriority(Event event);
    
    /**
     * Observes an event.
     * @param event the event.
     * @param transientVars transientVars passed from one Observer to another when processing a single Event. 
     * This may allow optimizations between observers.
     * @throws Exception if an error occurs. 
     * All exceptions will be logged but not propagated, as the observation mechanism should never fail.
     */
    void observe(Event event, Map<String, Object> transientVars) throws Exception;
}
