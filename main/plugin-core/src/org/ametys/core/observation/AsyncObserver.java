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
package org.ametys.core.observation;


/**
 * An interface to mark an observer as asynchronous. The
 * {@link #observe(Event, java.util.Map)} method will be run in another thread
 * and then will not block the main process. <br/>
 * Priority between AsyncObserver is still respected. For a given {@link Event},
 * a higher priority AsyncObserver will not run until lower ones are finished.
 */
public interface AsyncObserver extends Observer
{
    /**
     * Indicates if the observer can be run in parallel with others.
     * If not, the observer will be run in a single worker thread that will consume the queue of non-parallelizable observers.
     * However parallelizable observers could be run in parallel with other observers. In this case could not rely on priority anymore.
     * @return true if parallelizable
     */
    public default boolean parallelizable()
    {
        return true;
    }
}
